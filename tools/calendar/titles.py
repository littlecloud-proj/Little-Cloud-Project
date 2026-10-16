import re,glob,os,json
H=glob.glob('/tmp/do/*/web/www/horas')[0]
def sec(lang,f,name):
    if not f.endswith('.txt'): f+='.txt'
    p=f'{H}/{lang}/{f}'
    if not os.path.exists(p): return None
    t=open(p,encoding='utf-8').read()
    m=re.search(r'^\['+re.escape(name)+r'\]\s*\n(.*)$',t,re.M)
    return m.group(1).strip() if m else None
LAT=re.compile(r'\b(Dominica|Feria|infra|Octav\w*|Beatæ|Virginis|Confessoris|Martyris|Episcopi|Vigilia|Ss?\.|Pontificis|Doctoris|Hebdomad\w*|Quattuor|Abbatis|Sanct\w+|Domini|Apostoli|Martyrum|Viduæ|Pœnitentis|Ecclesiæ|Diei|Die|Sabbato|Cineres|Albis|Cinerum)\b')
def title_en(f):
    e=sec('English',f,'Rank'); e=e.split(';;')[0].strip() if e else ''
    if not e: e=(sec('English',f,'Officium') or '').strip()
    if e and not LAT.search(e): return e
    return None
def title_la(f):
    l=sec('Latin',f,'Rank'); l=l.split(';;')[0].strip() if l else ''
    return l or (sec('Latin',f,'Officium') or '').strip()
if __name__=='__main__':
    files=set()
    for l in open('/tmp/da/y2026.jsonl'):
        r=json.loads(l)
        if r['winner']: files.add(r['winner'])
        for c in r['commemo'].split('|'):
            if c: files.add(c)
    miss=[(f,title_la(f)) for f in sorted(files) if not title_en(f)]
    print(len(files),'missing',len(miss))
    for m in miss: print(m)
