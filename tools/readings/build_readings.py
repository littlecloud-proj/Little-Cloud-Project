"""Builds readings.json: the Epistle (lessons) and Gospel of every day, 2026-2050, for the
rubrics before 1955, of 1955 (Pius XII) and of 1962. Latin from Divinum Officium; English from the Douay-Rheims
(Gutenberg #8300) by the citation, with the liturgical opening ("At that time", "Brethren")."""
import json, re, sys, collections
DR = json.load(open('/tmp/rd/bible/dr.json'))
BOOKS = {
 'matt':'Matthew','mt':'Matthew','luc':'Luke','lc':'Luke','marc':'Mark','mc':'Mark',
 'joann':'John','ioann':'John','joannes':'John','ioannes':'John','jo':'John','io':'John','joh':'John','ioh':'John','john':'John','iohn':'John','joannnes':'John','ioannnes':'John',
 'eccli':'Ecclesiasticus','sap':'Wisdom','is':'Isaias','isa':'Isaias','ephes':'Ephesians','eph':'Ephesians',
 'act':'Acts','acts':'Acts','prov':'Proverbs','hebr':'Hebrews','heb':'Hebrews','gal':'Galatians','apoc':'Apocalypse',
 'phil':'Philippians','philipp':'Philippians','col':'Colossians','thess':'Thessalonians','tit':'Titus',
 'pet':'Peter','petri':'Peter','ezech':'Ezechiel','dan':'Daniel','gen':'Genesis','ier':'Jeremias','jer':'Jeremias',
 'reg':'Kings','tim':'Timothy','exod':'Exodus','exodi':'Exodus','exodus':'Exodus','ex':'Exodus','ias':'James','jas':'James','jac':'James','iac':'James','jc':'James','iacobi':'James',
 'cant':'Canticle of Canticles','judith':'Judith','iudith':'Judith','malach':'Malachias','mal':'Malachias','tob':'Tobias',
 'num':'Numbers','lev':'Leviticus','esth':'Esther','neh':'2 Esdras','osee':'Osee','os':'Osee','ionæ':'Jonas','jonæ':'Jonas','jon':'Jonas',
 'joël':'Joel','ioël':'Joel','joel':'Joel','jud':'Jude','judæ':'Jude','cor':'Corinthians','rom':'Romans','deut':'Deuteronomy','bar':'Baruch',
 'zach':'Zacharias','mich':'Micheas','agg':'Aggeus','soph':'Sophonias','abd':'Abdias','nah':'Nahum','hab':'Habacuc','amos':'Amos',
 'mach':'Machabees','esdr':'Esdras','thren':'Lamentations','lam':'Lamentations','ps':'Psalms','job':'Job','ruth':'Ruth',
 'judic':'Judges','jos':'Josue','par':'Paralipomenon','eccl':'Ecclesiastes','sir':'Ecclesiasticus','apocalypsis':'Apocalypse',
}
def book_of(raw):
    m = re.match(r'^\s*([1-4])?\.?\s*([A-Za-zæëÆ]+)\.?,?\s*(.*)$', raw)
    if not m: return None, None
    n, ab, rest = m.groups()
    name = BOOKS.get(ab.lower())
    if not name: return None, None
    if name in ('Corinthians','Thessalonians','Timothy','Peter','Kings','Machabees','Paralipomenon','Esdras') or (name == 'John' and n):
        name = f'{n or 1} {name}'
    return name, rest
def verses(rest):
    """'13:44-52' / '26:1-75; 27:1-66' / '15, 1-46' / '3:8-9; 14-19' -> [(ch, v), ...]"""
    out, ch = [], None
    for grp in re.split(r';', rest):
        grp = grp.strip().rstrip('.').strip()
        if not grp: continue
        if ':' in grp: c, vs = grp.split(':', 1); ch = int(re.sub(r'\D', '', c))
        elif re.match(r'^\d+\s*,', grp): c, vs = grp.split(',', 1); ch = int(c)
        elif re.match(r'^\d+\.\s', grp): c, vs = grp.split('.', 1); ch = int(c)
        else: vs = grp
        if ch is None: return None
        for part in vs.split(','):
            part = re.sub(r'[a-z]', '', part.strip().rstrip('.'))
            if not part: continue
            if '-' in part:
                a, b = part.split('-', 1)
                if not a.strip().isdigit() or not b.strip().isdigit(): return None
                out += [(ch, v) for v in range(int(a), int(b) + 1)]
            elif part.isdigit(): out.append((ch, int(part)))
            else: return None
    return out
def english(cit, la_body, kind):
    cit = cit.replace('21:15-10', '21:15-19')   # a slip in the source for St. Peter's Gospel
    name, rest = book_of(cit)
    if not name or name not in DR: return None
    vs = verses(rest)
    if not vs: return None
    txt = []
    for c, v in vs:
        t = DR[name].get(str(c), {}).get(str(v))
        if t is None: continue
        txt.append(t)
    if not txt or len(txt) < len(vs) * 0.7: return None
    body = ' '.join(txt)
    head = la_body.strip()
    if kind == 'E' and head.startswith(('In illo témpore', 'In illo tempore')) and not body.startswith(('At that time', 'And at that time')):
        body = 'At that time: ' + body
    elif head.startswith('Fratres'):
        body = 'Brethren: ' + body
    elif head.startswith(('Caríssimi', 'Carissimi')):
        body = 'Dearly beloved: ' + body
    return body
def pick(rs):
    """The Epistle (the last lesson before the Gospel, leaving out Tracts and canticles) and
    the Gospel. Ember days and vigils have several lessons; the Epistle is the last."""
    rs = [r for r in rs if not clean(r['la'][0]).startswith('Tractus') and not r['la'][1].startswith(('Luc. 1:46', 'Luc 1:46'))]
    gi = next((i for i, r in enumerate(rs) if r['k'] == 'E'), None)
    before = rs[:gi] if gi is not None else rs
    les = [r for r in before if r['k'] == 'L']
    return ([les[-1]] if les else []) + ([rs[gi]] if gi is not None else [])

def clean(title):
    lines = [l.strip() for l in (title or '').split('\n') if l.strip() and not l.strip().startswith('Top')]
    return lines[-1] if lines else ''

def en_title(r, la_title, cit):
    t = clean((r.get('en') or [''])[0] or '')
    if t and t != la_title and not re.search(r'[áéíóúǽ]|Léctio|Lectio|Sequéntia', t): return t
    name, _ = book_of(cit)
    if not name: return ''
    if r['k'] == 'E':
        who = {'Matthew': 'St. Matthew', 'Mark': 'St. Mark', 'Luke': 'St. Luke', 'John': 'St. John'}.get(name, name)
        if la_title.startswith(('Pássio', 'Passio')): return f'The Passion of our Lord Jesus Christ according to {who}'
        return f'Continuation of the holy Gospel according to {who}'
    base = re.sub(r'^[12] ', '', name)
    if base in ('Romans', 'Corinthians', 'Galatians', 'Ephesians', 'Philippians', 'Colossians', 'Thessalonians', 'Hebrews'):
        return f'Lesson from the Epistle of St. Paul the Apostle to the {base}'
    if base in ('Timothy', 'Titus', 'Philemon'):
        return f'Lesson from the Epistle of St. Paul the Apostle to {base}'
    if base in ('Peter', 'John', 'James', 'Jude'):
        return f'Lesson from the Epistle of St. {base} the Apostle'
    if base == 'Acts': return 'Lesson from the Acts of the Apostles'
    return f'Lesson from the Book of {name}'

if __name__ == '__main__':
    src = sys.argv[1]
    texts, index, out = [], {}, {}
    miss = collections.Counter(); total = 0
    days = collections.defaultdict(dict)
    for l in open(src):
        j = json.loads(l)
        if 'r' not in j: continue
        days[j['v']][j['d']] = j['r']
    # The restored Easter Vigil (1956 and 1962) is set out so that only the Gospel is found;
    # its Epistle (Col 3:1-4) is the same as before 1955.
    for v in ('1955', '1962'):
        for d, rs in days[v].items():
            if not any(r['k'] == 'L' for r in rs):
                old = [r for r in pick(days['1954'].get(d, [])) if r['k'] == 'L' and r['la'][1].startswith('Col 3:1')]
                if old: days[v][d] = old + rs
    for v, dd in days.items():
        years = {}
        for d in sorted(dd):
            ids = []
            for r in pick(dd[d]):
                la_title, cit, la_body = r['la']
                la_title = clean(la_title)
                cit = cit.strip().rstrip('.')
                en = english(cit, la_body, r['k']); total += 1
                if en is None: miss[cit] += 1
                key = (r['k'], la_title, cit, la_body, en_title(r, la_title, cit), en or '')
                if key not in index: index[key] = len(texts); texts.append(list(key))
                ids.append(index[key])
            y, doy = d[:4], None
            years.setdefault(y, []).append(ids)
        out[v] = years
    json.dump({'v': 1, 'days': out, 'texts': texts}, open('/tmp/rd/readings.json', 'w'), ensure_ascii=False, separators=(',', ':'))
    print('texts', len(texts), 'readings', total, 'no English', sum(miss.values()))
    print(miss.most_common(40))
