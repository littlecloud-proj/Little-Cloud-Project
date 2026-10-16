import json,re,sys,glob
sys.path.insert(0,'/tmp/da')
from titles import title_en, title_la, sec
from latin_en import tr
K=open(glob.glob('/mnt/user-data/outputs/LittleCloud/app/src/main/java/com/littlecloud/app/data/Sanctoral1962.kt')[0],encoding='utf-8').read()
S62={}
for m in re.finditer(r'md\((\d+), (\d+)\) to f\("([^"]+)"',K): S62[f'{int(m.group(1)):02d}-{int(m.group(2)):02d}']=m.group(3)
def color(la, rank):
    t=la
    if re.search(r'(?:Beat|Sanct)(?:ae|æ) Mari',t) and not re.search('Vigil',t): c='WHITE'
    elif re.search(r'Vigilia Pentecostes|Quattuor Temporum Pentecostes|Decollatione|Martyr|Reliquia',t,re.I): c='RED'
    elif re.search(r'Defunctorum|Parasceve|Morte',t,re.I): c='BLACK'
    elif re.search(r'^In Vigilia Ascensionis|^In Vigilia Epiphaniæ',t): c='WHITE'
    elif re.search(r'Vigilia|Quattuor|Rogatio|Passion|Palmis|gesim|(?:Majoris )?Hebdomadæ(?: Sanctæ)?|Sabbato Sancto|Dolorum|Ciner|Adventus|Quadragesim',t,re.I) and not re.search('commemoratione|votivum',t,re.I): c='VIOLET'
    elif re.search(r'Conversione|Dedicatione|Cathedra|oann|Pasch|Confessor|Ascensio|Cena',t,re.I): c='WHITE'
    elif re.search(r'Pentecosten(?!.*infra octavam)|Epiphaniam|post octavam',t,re.I): c='GREEN'
    elif re.search(r'Pentecostes|Evangel|Innocentium|Sanguinis|Cruc|Apostol',t,re.I): c='RED'
    else: c='WHITE'
    if re.match(r'^Dominica (III Adventus|IV Quadragesimæ)',t) or 'Laetare' in t or 'Gaudete' in t: c='ROSE'
    if re.match(r'^Ss\. Innocentium',t): c='VIOLET'
    return c
RANKS=[(r'Duplex (I|1)\.? class(is)?','Double of the I class',1),(r'Duplex (II|2)\.? class(is)?','Double of the II class',2),(r'(I|1)\.? classis Semiduplex|Semiduplex (I|1)\.? class','Semidouble of the I class',1),(r'(II|2)\.? classis Semiduplex|Semiduplex (II|2)\.? class','Semidouble of the II class',2),(r'Duplex majus','Greater double',3),
       (r'Duplex','Double',3),(r'Semiduplex','Semidouble',4),(r'Simplex','Simple',4),(r'Feria privilegiata','Privileged feria',1),(r'Feria major','Greater feria',3),(r'Feria','Feria',4),(r'Vigilia','Vigil',3)]
def rank(r, la, f):
    r=r.strip(); en,cls='',4
    for p,e,c in RANKS:
        if re.match(p,r,re.I): en,cls=e,c; break
    if not en: en,cls=(r or 'Feria'),4
    if re.search(r'cum Octava',r,re.I):
        m=re.search(r'Octava (privilegiata|communi|simplici)?',r,re.I)
        kind={'privilegiata':'privileged ','communi':'common ','simplici':'simple '}.get((m.group(1) or '').lower(),'') if m else ''
        en+=f' with {kind}octave'
    if la.startswith('Dominica'):
        sund='Sunday'
        if cls==4: cls=2
        en=f'{sund} · {en[0].lower()+en[1:]}'
    return en,cls
def name_en(f, la=None):
    la=la or title_la(f)
    if re.search(r'C10',f) or re.search(r'Sanctæ Mariæ Sabbato|Sancta Maria Sabbato',la or ''): return 'Saturday of Our Lady'
    e=title_en(f)
    if e: return e
    t=tr(la) if la else None
    if t: return t
    m=re.search(r'Sancti/(\d\d)-(\d\d)',f)
    if m and la and la.startswith('In Vigilia'):
        import datetime
        nd=(datetime.date(2026,int(m.group(1)),int(m.group(2)))+datetime.timedelta(1)).strftime('%m-%d')
        if nd in S62: return 'Vigil of '+S62[nd].split(',')[0]
    m=re.search(r'Sancti/(\d\d-\d\d)',f)
    if m and m.group(1) in S62 and not re.search(r'infra|Octav|Vigil',la or ''): return S62[m.group(1)]
    return None
MISS={}
def build(rows):
    days=[];idx={};texts=[];tidx={};years={}
    for r in rows:
        f=r['winner']; wr=r['wrank'].split(';;') if r['wrank'] else ['','']
        la=(wr[0] or title_la(f) or '').strip()
        en=name_en(f,la) or la
        if en==la: MISS[f]=la
        rk,cls=rank(wr[1] if len(wr)>1 else '', la, f)
        col=color(la, wr[1] if len(wr)>1 else '')
        comms=[]
        for c in [x for x in r['commemo'].split('|') if x]:
            cl=title_la(c); ce=name_en(c,cl) or cl
            if ce==cl and cl: MISS[c]=cl
            if ce and ce not in comms and ce!=en: comms.append(ce)
        ci=-1
        if r['la']:
            k=(r['la'],r['en'])
            if k not in tidx: tidx[k]=len(texts); texts.append(list(k))
            ci=tidx[k]
        key=json.dumps([en,rk,cls,col,comms,ci],ensure_ascii=False)
        if key not in idx: idx[key]=len(days); days.append(json.loads(key))
        years.setdefault(r['d'][:4],[]).append(idx[key])
    return {'years':years,'days':days,'texts':texts}
if __name__=='__main__':
    rows=[]
    for p in sys.argv[2:]:
        rows+= [json.loads(l) for l in open(p)]
    rows.sort(key=lambda r:r['d'])
    out=build(rows)
    json.dump(out,open(sys.argv[1],'w'),ensure_ascii=False,separators=(',',':'))
    print('days',len(rows),'unique',len(out['days']),'texts',len(out['texts']),'miss',len(MISS))
    for k,v in sorted(MISS.items()): print('  MISS',k,'|',v)
