import json, re
G='/tmp/gen/'
H=open(G+'hammer.txt').read().split('\n')
def block(a,b):  # 1-based inclusive line numbers from Read tool
    return [l.strip() for l in H[a-1:b]]
def norm_resp(l):
    # "X: Spare us, O Lord." -> "X, spare us, O Lord."
    m=re.match(r'^(.*?):\s+([A-Z])(.*)$',l)
    if m and l.startswith(('Lamb of God','Be merciful','All ye men')): l=m.group(1)+', '+m.group(2).lower()+m.group(3)
    l=re.sub(r'^LORD,','Lord,',l); l=re.sub(r'^(O |POUR |VOUCHSAFE|INCLINE|O GOD)', lambda m:m.group(1).capitalize() if m.group(1).isupper() else m.group(1), l)
    l=re.sub(r'^V\.\s*','℣. ',l); l=re.sub(r'^R\.\s*','℟. ',l)
    l=l.rstrip(',')
    if l.startswith('℣.') and l.endswith(':'): l=l[:-1]+'.'
    if l and l[-1] not in '.!?:;' : l+='.'
    return l
def fix_caps(l):
    # first word all-caps (drop cap style) -> Capitalized
    l=re.sub(r'^O (LORD|ALMIGHTY|GOD)\b', lambda m:'O '+m.group(1).capitalize() if m.group(1)!='ALMIGHTY' else 'O almighty', l)
    return re.sub(r'^(?!SS\.)([A-Z]{2,})\b', lambda m:m.group(1).capitalize(), l)
def group(lines, breaks_before):
    out=[]
    for l in lines:
        if not l: 
            if out and out[-1]!='': out.append('')
            continue
        if any(l.startswith(b) for b in breaks_before) and out and out[-1]!='': out.append('')
        out.append(l)
    while out and out[-1]=='': out.pop()
    return out
def prep(lines):
    return [norm_resp(fix_caps(l)) if l else '' for l in lines]

# ---------- Holy Name (Hammer 708-787)
hn=prep(block(708,781))
hn=[l for l in hn]
hn=group(hn,['God the Father','Jesus, Son of the living','Be merciful, spare','From all evil','By the mystery','Lamb of God, who takest away the sins of the world, spare','Jesus, hear us.'])
# second "Jesus, hear us." at end should be separated only once
p1=fix_caps(H[785-1].strip()); p2=H[787-1].strip()
hn+=['','Let us pray.',p1,'',p2]
# ---------- Sacred Heart (798-848)
sh=prep(block(798,844))
sh=group(sh,['God, the Father','Heart of Jesus, Son of the eternal','Lamb of God, who takest away the sins of the world, spare','℣.'])
sh=[l.replace('God, the Father of heaven','God the Father of heaven').replace('℣. Jesus, meek and humble of Heart.','℣. Jesus, meek and humble of Heart.') for l in sh]
sh+=['','Let us pray.',fix_caps(H[848-1].strip())]
# ---------- Loreto (858-918) + later invocations + Concede
lo=prep(block(858,918))
i=lo.index('Queen of the most holy rosary, pray for us.')
lo[i+1:i+1]=['Queen assumed into heaven, pray for us.','Queen of peace, pray for us.']
# 1962 order: ... Queen conceived without original sin, Queen assumed into heaven, Queen of the most holy Rosary, Queen of peace
j=lo.index('Queen assumed into heaven, pray for us.'); lo.pop(j); k=lo.index('Queen conceived without original sin, pray for us.'); lo.insert(k+1,'Queen assumed into heaven, pray for us.')
lo=[l.replace('Lord have mercy on us.','Lord, have mercy on us.') for l in lo]
lo=group(lo,['God the Father','Holy Mary,','Lamb of God, who takest away the sins of the world, spare','℣.'])
lo=[l.replace('℣. Pray for us, O holy Mother of God:','℣. Pray for us, O holy Mother of God.') for l in lo]
CONCEDE_EN="Grant, we beseech Thee, O Lord God, that we Thy servants may enjoy perpetual health of mind and body; and by the glorious intercession of Blessed Mary, ever Virgin, may be delivered from present sorrow, and enter into the joy of eternal gladness. Through Christ our Lord. Amen."
lo+=['','Let us pray.',CONCEDE_EN]
# ---------- Litany of Saints (937-1120)
ls=prep(block(937,1120))
ls=[l for l in ls]
k=ls.index('From all sin, O Lord, deliver us.'); ls.insert(k+1,'From Thy wrath, O Lord, deliver us.')
k=ls.index('That Thou vouchsafe to grant peace and unity to all Christian people, we beseech Thee, hear us.')
ls.insert(k+1,'That Thou vouchsafe to recall all who have strayed to the unity of the Church, and to lead all unbelievers to the light of the Gospel, we beseech Thee, hear us.')
ls=[l.replace('Psalm LXIX.','Psalm 69').replace('We sinners, Beseech Thee','We sinners, we beseech Thee').replace('All ye men and women, saints of God, make intercession for us.','All ye holy men and women, saints of God, make intercession for us.').replace('We sinners, beseech Thee, hear us.','We sinners, we beseech Thee, hear us.').replace('Psalm lxix.','Psalm 69').replace('PSALM LXIX.','Psalm 69') for l in ls]
ls=group(ls,['God the Father','Holy Mary,','St. John Baptist','All ye holy innocents','St. Mary Magdalen','Be merciful, spare','We sinners','Lamb of God, who takest away the sins of the world, spare','Christ, hear us.','Our Father, etc.','Psalm 69','Incline unto','℣. Save Thy','Let us pray','O God, whose property','℣. O Lord, hear my prayer.'])
# the first Christ, hear us. group break at top: remove blank created before index of first 'Christ, hear us.' if at position<6
if ls[3]=='' : ls.pop(3)
# Psalm verses: keep each prayer paragraph separated
txt='\n'.join(ls)
for s in ['Hear, we beseech','Out of Thy clemency','O God, who by sin','O almighty and eternal God, have mercy','O God, from whom all','Inflame, O Lord','O God, the Creator','Direct, we beseech','Almighty and eternal God, who hast']:
    txt=txt.replace('\n'+s,'\n\n'+s)
txt=txt.replace('Let us pray.\n\nO God, whose property','Let us pray.\nO God, whose property')
ls_en=re.sub(r'\n{3,}','\n\n',txt)
json.dump(dict(holyname='\n'.join(hn),sacred='\n'.join(sh),loreto_en='\n'.join(lo),saints_en=ls_en),open('/tmp/gen2/hammer_litanies.json','w'),ensure_ascii=False)
for k,v in dict(holyname=hn,sacred=sh,loreto=lo).items(): print(k,len(v),'blank',v.count(''), 'noperiod',[x[:20] for x in v if x and x[-1] not in '.!?'])
print('saints lines',ls_en.count('\n'))
