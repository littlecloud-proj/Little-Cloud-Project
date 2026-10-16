import subprocess, re, html, json, sys, datetime, os, glob
from concurrent.futures import ThreadPoolExecutor
CGI = glob.glob('/tmp/do/*/web/cgi-bin/horas')[0]
V = 'Divino%20Afflatu%20-%201954'
def txt(x):
    x = re.sub(r'<BR\s*/?>', '\n', x, flags=re.I); x = re.sub(r'<[^>]+>', '', x)
    return html.unescape(x)
def oration(block):
    lines = [l.strip() for l in block.split('\n')]
    out, on = [], False
    for l in lines:
        if not on and l in ('Orémus.', 'Let us pray.'):
            on = True; continue
        if on:
            if l.startswith('℟. Amen'): break
            if l: out.append(l)
    return '\n'.join(out)
def perl(script, d):
    q = f"command=prayLaudes&date={d.month}-{d.day}-{d.year}&version={V}&lang2=English"
    env = dict(os.environ, REQUEST_METHOD='GET', QUERY_STRING=q)
    return subprocess.run(['perl', '-I/tmp/perlshim', script], cwd=CGI, env=env, capture_output=True).stdout.decode('utf-8', 'replace')
def run(d):
    p = perl('probe.pl', d)
    info = {}
    for l in p.split('\n'):
        if '\t' in l:
            k, v = l.split('\t', 1); info[k] = v
    s = perl('officium.pl', d)
    la = en = ''
    for r in re.findall(r'<TR[^>]*>(.*?)</TR>', s, re.S | re.I):
        cells = re.findall(r'<TD[^>]*>(.*?)</TD>', r, re.S | re.I)
        if len(cells) >= 2:
            a = txt(cells[0])
            if re.search(r'^\s*(Top\s+Next\s*)?Oratio', a.strip()[:40]):
                la, en = oration(a), oration(txt(cells[1])); break
    head = re.search(r'<P ALIGN=CENTER><FONT COLOR="(\w+)">(.*?)</FONT>', s)
    return {'d': d.isoformat(), 'winner': info.get('WINNER',''), 'commemo': info.get('COMMEMO',''),
            'dayname': info.get('DAYNAME',''), 'wrank': info.get('WRANK',''), 'rank': info.get('RANK',''),
            'color': head.group(1) if head else '', 'head': txt(head.group(2)) if head else '', 'la': la, 'en': en}
if __name__=="__main__":
    start, end = datetime.date(int(sys.argv[1]), 1, 1), datetime.date(int(sys.argv[2]), 12, 31)
    days = [start + datetime.timedelta(n) for n in range((end - start).days + 1)]
    with ThreadPoolExecutor(3) as ex, open(sys.argv[3], 'w') as f:
        for i, r in enumerate(ex.map(run, days)):
            f.write(json.dumps(r, ensure_ascii=False) + '\n')
            if i % 200 == 0: f.flush()
