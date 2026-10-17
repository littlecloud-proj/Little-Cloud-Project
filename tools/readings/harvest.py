import subprocess, re, html, json, sys, datetime, os, glob
from concurrent.futures import ThreadPoolExecutor
CGI = glob.glob('/tmp/do/*/web/cgi-bin/missa')[0]
VERS = {'1954': 'Divino%20Afflatu%20-%201954', '1955': 'Reduced%20-%201955', '1962': 'Rubrics%201960%20-%201960'}

def txt(x):
    x = re.sub(r'<BR\s*/?>', '\n', x, flags=re.I); x = re.sub(r'<[^>]+>', '', x)
    return html.unescape(x).replace('\xa0', ' ')

def missa(d, v, lang2='English', n=1):
    q = f"command=praySanctaMissa&date={d.month}-{d.day}-{d.year}&version={VERS[v]}&lang2={lang2}&missanumber={n}"
    env = dict(os.environ, REQUEST_METHOD='GET', QUERY_STRING=q)
    return subprocess.run(['perl', '-I/tmp/perlshim', 'missa.pl'], cwd=CGI, env=env, capture_output=True).stdout.decode('utf-8', 'replace')

CIT = re.compile(r'<FONT COLOR="red"><I>([^<]*\d[^<]*)</I></FONT>')

def parse_cell(cell, kind):
    """-> (title, citation, body) from one column of a Lectio or Evangelium row."""
    m = CIT.search(cell)
    if not m: return None
    cit = html.unescape(m.group(1)).strip()
    before, after = cell[:m.start()], cell[m.end():]
    lines = [l.strip() for l in txt(before).split('\n') if l.strip()]
    # title: the last line before the citation that is not a response
    lines = [l for l in lines if not l.startswith(('℟', '℣', 'Top'))]
    title = lines[-1] if lines else ''
    title = re.sub(r'\s*[+✠]\s*', ' ', title).strip()
    body = []
    for l in txt(after).split('\n'):
        l = l.strip()
        if not l: continue
        # The reading ends at the people's answer (Deo grátias, Laus tibi, Christe) or at
        # the priest's "Per evangélica dicta". In the Passion, J. C. S. mark the speakers.
        if body and (l.startswith(('℟.', 'R. ')) or 'evangélica dicta' in l or 'evangelica dicta' in l): break
        body.append(l)
    return [title, cit, '\n'.join(body)]

def readings(s):
    out = []
    for r in re.findall(r'<TR[^>]*>(.*?)</TR>', s, re.S | re.I):
        c = re.findall(r'<TD[^>]*>(.*?)</TD>', r, re.S | re.I)
        if len(c) < 2: continue
        head = txt(c[0]).strip().split('\n')
        name = next((h.strip() for h in head if h.strip() and not h.strip().startswith('Top')), '')
        if name in ('Lectio', 'Evangelium'):
            la, en = parse_cell(c[0], name), parse_cell(c[1], name)
            if la: out.append({'k': 'E' if name == 'Evangelium' else 'L', 'la': la, 'en': en})
    return out

RED = re.compile(r'^<FONT COLOR="red"><I>(.*?)</I></FONT>$', re.S)
CITE = re.compile(r'^[1-4]?\s?[A-Z][A-Za-zæë]+\.?,?\s*\d+[,:.]\s*\d')

def readings_by_segments(s):
    """For days set out as rubrics and readings (Good Friday, Holy Saturday): walk the <br/>
    segments; a red citation followed by plain text is a reading, unless it is a Tract."""
    out = []
    for r in re.findall(r'<TR[^>]*>(.*?)</TR>', s, re.S | re.I):
        c = re.findall(r'<TD[^>]*>(.*?)</TD>', r, re.S | re.I)
        if len(c) < 2: continue
        segs = [x.strip() for x in re.split(r'<br\s*/?>', c[0], flags=re.I)]
        prev_red, title = '', ''
        i = 0
        while i < len(segs):
            m = RED.match(segs[i])
            if m:
                t = html.unescape(m.group(1)).strip()
                if CITE.match(t) and not t.startswith(('Ps', 'Habacuc')) and prev_red != 'Tractus.':
                    body = []
                    j = i + 1
                    while j < len(segs) and not RED.match(segs[j]) and segs[j]:
                        line = txt(segs[j]).strip()
                        if line.startswith('℟.') and body: break
                        body.append(line); j += 1
                    if body:
                        kind = 'E' if re.search(r'Evangél|^P[áa]ssio D', title) else 'L'
                        out.append({'k': kind, 'la': [title or '', t, '\n'.join(body)], 'en': None})
                    i = j; prev_red = t; title = ''; continue
                prev_red = t
            else:
                plain = txt(segs[i]).strip()
                if plain and len(plain) < 120: title = re.sub(r'\s*[+✠]\s*', ' ', plain)
            i += 1
    return out

def run(args):
    d, v = args
    try:
        s = missa(d, v)
        count = len(set(re.findall(r"name=.?missanumber.? [^>]*value=.?(\d)", s, re.I))) or 1
        n = 1
        if count > 1:
            # Holy Thursday: the Mass of the Lord's Supper; Christmas: the Mass of the Day.
            holy_thursday = d.weekday() == 3 and count == 2 and d.month in (3, 4)
            n = 3 if (d.month, d.day) == (12, 25) else 2 if holy_thursday else 1
            n = min(n, count)
            if n != 1: s = missa(d, v, n=n)
        r = readings(s)
        if not any(x['k'] == 'E' for x in r):
            alt = readings_by_segments(s)
            if any(x['k'] == 'E' for x in alt): r = alt
        return {'d': d.isoformat(), 'v': v, 'n': n, 'count': count, 'r': r}
    except Exception as e:
        return {'d': d.isoformat(), 'v': v, 'err': str(e)}

if __name__ == '__main__':
    y0, y1, out = int(sys.argv[1]), int(sys.argv[2]), sys.argv[3]
    done = set()
    if os.path.exists(out):
        for l in open(out):
            j = json.loads(l); done.add((j['d'], j['v']))
    jobs = []
    d = datetime.date(y0, 1, 1)
    while d <= datetime.date(y1, 12, 31):
        for v in os.environ.get('VERSIONS', ','.join(VERS)).split(','):
            if (d.isoformat(), v) not in done: jobs.append((d, v))
        d += datetime.timedelta(1)
    print('todo', len(jobs), flush=True)
    with ThreadPoolExecutor(int(os.environ.get('T', '4'))) as ex, open(out, 'a') as f:
        for i, r in enumerate(ex.map(run, jobs)):
            f.write(json.dumps(r, ensure_ascii=False) + '\n')
            if i % 50 == 0: f.flush(); print(i, flush=True)
