import json, re
S='/tmp/src/'
t1=json.load(open(S+'t1.json')); t2=json.load(open(S+'t2.json')); t3=json.load(open(S+'t3.json')); t4=json.load(open(S+'t4.json')); t5=json.load(open(S+'t5.json'))
P=json.load(open(S+'parsed1.json'))
office=json.load(open('/tmp/harvest/office.json'))
OUT={}
def vr(l):
    l=re.sub(r'^V\.\s*','℣. ',l); l=re.sub(r'^R\.\s*','℟. ',l); return l
# ---------- Loreto la/es
RESP={'miserére nobis','ora pro nobis','ten misericordia de nosotros','ruega por nosotros'}
def fill(lines):
    out=[]; cur=None
    for l in lines:
        l=l.strip()
        if not l:
            if out and out[-1]!='': out.append('')
            continue
        if re.match(r'^(Agnus|Cordero|V\.|Ruega por nosotros, Santa|Oremus|Oremos)',l): cur=None
        hit=[r for r in RESP if r in l.lower() or r in l]
        if hit: cur=hit[0]
        elif cur and not re.match(r'^(Kyrie|Christe|Señor|Cristo)',l):
            l=l.rstrip('.,;: ')+', '+cur+'.'
        out.append(l)
    return out
la=fill(P['loreto_la']); es=fill(P['loreto_es'])
def split_vr(lines, rx):
    o=[]
    for l in lines:
        m=re.search(rx,l)
        if l.startswith('V.') and m: o+= [vr(l[:m.start()].strip()), vr('R. '+l[m.end():].strip())]
        else: o.append(vr(l))
    return o
la=split_vr(la, r'\s+R\.\s*')
# spanish V/R line "Ruega por nosotros, Santa Madre de Dios. Para que..." -> split at first ". "
es2=[]
for l in es:
    if l.startswith('Ruega por nosotros, Santa Madre'):
        a,b=l[:39],l[40:].strip(); es2+=['℣. '+a+'.','℟. '+b[0].upper()+b[1:]]
    else: es2.append(l)
es=es2
la=[('Orémus.' if l=='Oremus:' else l) for l in la]
# English
en=[re.sub(r', Have mercy',', have mercy',l) for l in P['loreto_en_head']]
tail=[x.strip() for x in P['loreto_en_tail']]
ag=[]
for i in (0,3,6):
    ag.append(tail[i].rstrip(',')+', '+tail[i+1].strip('_').rstrip('.,')[0].lower()+tail[i+1].strip('_').rstrip('.,')[1:]+'.')
concede=[l for b in office['nat|norm|vespers'] for l in b['en'] if 'mind and body' in l][0]
en_full=en+['']+ag+['',vr(tail[15]),vr(tail[16]),'','Let us pray.',concede]
# check counts
OUT['loreto']=dict(la='\n'.join(la),en='\n'.join(en_full),es='\n'.join(es))
print('loreto la/es/en lines', len(la),len(es),len(en_full), 'la resp-less marian:', sum(1 for l in la if l and not re.search(r'(nobis|nos|Dómine|eléison|nobis\.|\.)$',l)))
# ---------- Holy Name (Gahan)
g=[l.strip() for l in t4['holyname'].replace('\r','').split('\n')]
body=g[2:149]+g[213:223]
hn=[]; 
for l in body:
    if not l:
        if hn and hn[-1]!='': hn.append('')
        continue
    if re.fullmatch(r'\{\d+\}',l): continue
    if l.startswith('_') and hn:
        r=l.strip('_').rstrip('.!,')
        hn[-1]=hn[-1].rstrip(',')+', '+r[0].lower()+r[1:]+'.'; continue
    hn.append(l.replace('_',''))
# join prayer paragraph (lines after last blank)
k=max(i for i,x in enumerate(hn) if x=='')
pray=' '.join(hn[k+1:]); hn=hn[:k+1]+['Let us pray.',re.sub(r'\s+',' ',pray)]
hn=[re.sub(r'\s+',' ',x).replace("Thro' ","Through ") for x in hn]
OUT['holyname']=dict(en='\n'.join(hn))
print('holy name lines',len(hn),'Jesus inv',sum(1 for x in hn if x.startswith('Jesus,')))
# ---------- Sacred Heart (wiki)
L=t1['sacredheart']['w'].split('\n')
sh=[]
for l in L[12:67]:
    l=re.sub(r'<ref.*','',l).replace('<br>','').strip()
    if not l:
        if sh and sh[-1]!='': sh.append('')
        continue
    sh.append(vr(l))
sh=[ (x+' on us.' if x in ('Lord, have mercy.','Christ, have mercy.') else x) for x in sh]
sh=[('Let us pray.' if x=='Let us pray' else x) for x in sh]
# merge trailing Amen into prayer
if sh[-1]=='Amen.': sh=sh[:-2]+[sh[-2].rstrip()+(' Amen.' if not sh[-2].rstrip().endswith('Amen.') else '')]
OUT['sacredheart']=dict(en='\n'.join(sh))
print('sacred heart lines',len(sh),'heart inv',sum(1 for x in sh if x.startswith('Heart of Jesus')))
# ---------- Holy Ghost
L=[l.replace('\xa0',' ').strip() for l in t5['hg'].split('\n')][9:75]
hg=[]; buf=''
for l in L:
    if not l:
        if buf: hg.append(buf); buf=''
        if hg and hg[-1]!='': hg.append('')
        continue
    if buf and (l[0].islower() or buf.startswith('Grant, O merciful') or l.startswith('ever.')):
        buf+=' '+l; continue
    if buf: hg.append(buf)
    buf=l
if buf: hg.append(buf)
hg2=[]
for x in hg:
    x=vr(x)
    if x.startswith('Let Us Pray'): x='Let us pray.'
    if ' R. Amen.' in x: hg2+= [x.replace(' R. Amen.',''),'℟. Amen.']; continue
    hg2.append(x)
while hg2 and hg2[-1]=='': hg2.pop()
OUT['holyghost']=dict(en='\n'.join(hg2))
print('holy ghost lines',len(hg2),'lambs',sum(1 for x in hg2 if x.startswith('Lamb')), 'last', hg2[-1][:10])
# ---------- Veni Creator
d=json.load(open('/tmp/harvest/do_texts.json'))['veni_creator']
import re as _r
def sec(path,head):
    t=open(path,encoding='utf-8').read()
    for s in _r.split(r'\n(?=\[)',t):
        if s.split('\n')[0]==head: return [x for x in s.split('\n')[1:] if x.strip()]
H='/tmp/do/divinum-officium-master/web/www/horas/'
cla=sec(H+'Latin/Tempora/Pasc7-0.txt','[Oratio]')[0].replace('hodiérna die ','')
cen=sec(H+'English/Tempora/Pasc7-0.txt','[Oratio]')[0].replace('Who on this day didst','Who didst')
pla=sec(H+'Latin/Psalterium/Common/Prayers.txt','[Per Dominum eiusdem]')[0][3:]
pen=sec(H+'English/Psalterium/Common/Prayers.txt','[Per Dominum eiusdem]')[0][3:]
assert 'hodiérna' not in cla and 'on this day' not in cen
vla='\n'.join(d['la'])+'\n\n℣. Emítte Spíritum tuum, et creabúntur.\n℟. Et renovábis fáciem terræ.\n\nOrémus.\n'+cla+' '+pla+'\n℟. Amen.'
ven='\n'.join(d['en']).rstrip()+('.' if not d['en'][-1].endswith('.') else '')+'\n\n℣. Send forth thy Spirit, and they shall be created.\n℟. And thou shalt renew the face of the earth.\n\nLet us pray.\n'+cen+' '+pen+'\n℟. Amen.'
OUT['veni']=dict(la=vla,en=ven)
print('veni ok', len(vla), len(ven))
# ---------- Litany of Saints English fixes
ls=open('/mnt/user-data/outputs/LittleCloud/app/src/main/java/com/littlecloud/app/data/LitanySaints.kt',encoding='utf-8').read()
m=re.search(r'val english: String = "(.*)"\n',ls); lse=json.loads('"'+m.group(1)+'"')
lines=lse.split('\n'); o=[]
for x in lines:
    if x.startswith('Be merciful, Spare'): x='Be merciful, spare us, O Lord.'
    elif x.startswith('Be merciful, Hear'): x='Be merciful, graciously hear us, O Lord.'
    elif re.match(r'^(From|Through|In the day)',x) and 'Spare us, O Lord' in x: x=re.sub(r',\s*Spare us, O Lord\.?$',', O Lord, deliver us.',x)
    elif 'We beg of thee, hear us' in x:
        x=x.replace('That thou wouldst be so kind as to answer our prayers Son of God','That thou wouldst vouchsafe to hear us')
        x=x.replace(', We beg of thee, hear us.',', we beseech thee, hear us.').replace('We sinners,','We sinners,')
    x=x.replace('Holy Spirit','Holy Ghost').replace('let my cry come unto you','let my cry come unto thee').replace('Christ, hear us,','Christ, hear us.').strip()
    o.append(x)
OUT['saints_en']='\n'.join(o)
print('saints en lines',len(o),'deliver', sum('deliver us.' in x for x in o), 'leftover Spare', sum('Spare us' in x for x in o))
# ---------- Therese morning prayer
L=t2['therese_offering'].replace('\r','').split('\n')
p1=' '.join(x.strip() for x in L[14:20]); p2=' '.join(x.strip() for x in L[21:25])
OUT['therese']=dict(en=re.sub(r'\s+',' ',p1)+'\n\n'+re.sub(r'\s+',' ',p2))
print('therese', len(p1),len(p2), p2[-12:])
# ---------- Nada te turbe
L=t1['nada_es']['w'].split('\n')
nes='\n'.join(x.strip() for x in L[10:19])
L2=t2['longfellow'].split('\n'); nen='\n'.join(x.strip() for x in L2[10:19])
OUT['nada']=dict(es=nes,en=nen)
# ---------- Montfort
def ocr(lines):
    keep=[]
    for x in lines:
        s=x.strip()
        if not s: keep.append(''); continue
        if re.search(r'TRUE DEVOTION|BLESSED VIRGIN\. ?.?\s*\d+|^> 9 hl|^th S,',s): continue
        keep.append(s)
    paras=[]; cur=''
    for s in keep:
        if not s:
            continue
        cur = (cur[:-1] if cur.endswith('-') else (cur+' ' if cur else ''))+s
        if re.search(r'(\.|!)\s*$',s) and len(s)<30: paras.append(cur); cur=''
    if cur: paras.append(cur)
    return paras
C=t3['consecration'].replace('\r','').split('\n')
seg=C[14:131]
txt=[]
for x in seg:
    s=x.strip()
    if re.search(r'TRUE DEVOTION|BLESSED VIRGIN\.|^> 9 hl|^th S,',s): continue
    txt.append(s)
# rebuild paragraphs: blank line followed by line starting with capital after a line ending with . => para
paras=[]; cur=''
for s in txt:
    if not s: 
        continue
    if cur.endswith('- '): cur=cur[:-2]+s+' '
    else: cur+=s+' '
    if s in ('Thine Incarnation.','ever been before.','in eternity.','in all things.','thy slaves.','Amen.') : paras.append(cur.strip()); cur=''
    elif s.endswith('in eternity.'): paras.append(cur.strip()); cur=''
if cur.strip(): paras.append(cur.strip())
fixes=[(r'\s+',' '),(r'(\w)- (\w)',r'\1\2'),(r'\| dare','I dare'),(r'\bi choose','I choose'),(r'interior and\. exterior','interior and exterior'),(r'Inca:inate','Incarnate'),(r'_\s*',''),(r'in all \. things','in all things'),(r' ;',';'),(r' ,',','),(r'—',' — '),(r'\s+',' '),(r' — I',' — I')]
out=[]
for p in paras:
    for a,b in fixes: p=re.sub(a,b,p)
    out.append(p.strip())
# split "Mistress..." para: the 'Amen.' collapses
mont='\n\n'.join(out)
OUT['montfort']=dict(en=mont)
words=set(re.findall(r"[A-Za-z]+",mont))
import collections
odd=[w for w in re.findall(r"\S+",mont) if re.search(r'[^A-Za-z.,;:!?\'’"“”()—-]',w) or re.search(r'[a-z][A-Z]',w)]
print('montfort paras',len(out),[len(p) for p in out],'odd tokens',odd[:30])
# Olier
O=t3['olier'].replace('\r','').split('\n')
ol=' '.join(x.strip() for x in O[8:10]+O[15:20])
ol=ol[ol.find('O Jesus'):] if 'O Jesus' in ol else ol
ol=re.sub(r'(\w)- (\w)',r'\1\2',ol).replace('”','').strip()
OUT['olier']=dict(en=re.sub(r'\s+',' ',ol))
print('olier',len(OUT['olier']['en']), 'starts', OUT['olier']['en'][:8], 'ends', OUT['olier']['en'][-8:])
json.dump(OUT,open('/tmp/gen/out.json','w'),ensure_ascii=False)
