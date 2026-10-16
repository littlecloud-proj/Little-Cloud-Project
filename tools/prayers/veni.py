import json,re
L=open('/tmp/gen/lyra.txt').read().split('\n')[6053:6112]
stz=[];cur=[]
for l in L:
    s=l.strip()
    if not s or re.match(r'^(\d+|Digitized.*|HYMNS FROM THE|PROPER OF.*)$',s,re.I): continue
    s=re.sub(r'\s+([!;:,.?])',r'\1',s).replace('Bupply','supply').replace('’',"’")
    cur.append(s)
    if len(cur)==4: stz.append(cur); cur=[]
assert not cur and len(stz)==7, (len(stz),cur)
# OCR turned commas into periods at some line ends; restore punctuation by stanza position
fix={ (0,2):',', (3,0):',', (3,1):';', (4,0):',', (4,2):',', (5,0):'', (5,1):';', (5,2):',' }
out=[]
for i,st in enumerate(stz):
    st=list(st)
    for (a,b),p in fix.items():
        if a==i: st[b]=st[b].rstrip('.,;')+p
    st=[x.replace(' thy ',' Thy ').replace('in thy','in Thy').replace('with thy','with Thy').replace('us thy','us Thy').replace('may thy','may Thy') if True else x for x in st]
    out.append('\n'.join(st))
en='\n\n'.join(out)
en=en.replace('O Holy Ghost, eternally.','O Holy Ghost, eternally. Amen.')
d=json.load(open('/tmp/harvest/do_texts.json'))['veni_creator']
la_h=d['la'] if isinstance(d['la'],str) else '\n'.join(d['la'])
print(type(d['la']), la_h.count('\n'), la_h[:22])
json.dump({'en_hymn':en,'la_hymn':la_h},open('veni.json','w'),ensure_ascii=False)
print(len(en), en.count('\n'))
