import json,re,collections,sys
F='/home/claude/little-cloud-project/app/src/commonMain/composeResources/files/'
issues=collections.defaultdict(list)
def flat(bl,c): return [l for b in bl for l in ([b['hl'] if c=='la' else b['he']] if (b['hl'] if c=='la' else b['he']) else [])+b[c]]
FINAL={'Salve':'Salve, Regína','Alma':'Alma Redemptóris','AveRegina':'Ave, Regína cælórum','Regina':'Regína cæli'}
for rub,f in [('1955','office1955.json'),('1954','office1954.json')]:
    d=json.load(open(F+f))
    for k,bl in sorted(d.items()):
        season,tempus,hour=k.split('|')[:3]
        la=flat(bl,'la'); en=flat(bl,'en'); L='\n'.join(la); E='\n'.join(en)
        tag=f'{rub} {k}'
        # markup leftovers
        for c,txt in (('la',L),('en',E)):
            for pat in [r'\{',r'\}',r'\$',r'@',r'&\w+;',r'\brubrica\b',r'\[\d+\]',r'<',r'>',r'\s{2,}',r' ,',r' \.']:
                m=re.findall(pat,txt)
                if m and pat!=r'\[\d+\]': issues[tag].append(f'{c}: markup {pat!r} x{len(m)}')
        # parallel blocks
        if len(la)==0 or len(en)==0: issues[tag].append('empty column')
        for i,b in enumerate(bl):
            if bool(b['la'])!=bool(b['en']): issues[tag].append(f'block {i} ({b["hl"]}) one language missing')
            if abs(len(b['la'])-len(b['en']))>2: issues[tag].append(f'block {i} ({b["hl"]}) la {len(b["la"])} lines vs en {len(b["en"])}')
        # duplicates
        for c,lines in (('la',la),('en',en)):
            for a,b in zip(lines,lines[1:]):
                if a==b and len(a)>20 and not a.startswith(('Ant.','℣','℟')): issues[tag].append(f'{c}: repeated line {a[:50]!r}')
        # alleluia rules
        if tempus=='sept' and re.search(r'Allelúja|Allelúia',L) and 'Laus tibi' not in L: issues[tag].append('Alleluia in Septuagesima')
        if tempus=='sept' and 'Laus tibi' not in L and hour not in ('compline',): pass
        if tempus=='pasch' and hour in('lauds','vespers') and L.count('allelúja')+L.count('allelúia')<3: issues[tag].append('few alleluias in Paschaltide')
        # Te Deum
        if hour=='matins':
            td='Te Deum' in L
            if tempus=='sept' or season=='adv':
                if td: issues[tag].append('Te Deum in Advent/Septuagesima')
            elif not td: issues[tag].append('no Te Deum')
            n=len(re.findall(r'^(Lectio|Lesson) ?\d|^Lectio \d',L,re.M)) or len(re.findall(r'Lectio',L))
            if L.count('Lectio')<3: issues[tag].append(f'lessons? {L.count("Lectio")}')
        # final antiphon at compline
        if hour=='compline':
            found=[n for n,s in FINAL.items() if s in L]
            want={'adv':'Alma','nat':'Alma'}.get(season) or {'pasch':'Regina','sept':'AveRegina'}.get(tempus) or 'Salve'
            if found!=[want]: issues[tag].append(f'final antiphon {found}, expected {want}')
        # canticles
        need={'lauds':'Benedíctus','vespers':'Magníficat','compline':'Nunc dimíttis'}.get(hour)
        if need and need not in L: issues[tag].append(f'missing {need}')
        if 'Orémus' not in L: issues[tag].append('no Oremus/prayer')
        if 'Deo grátias' not in L and hour!='compline': issues[tag].append('no Benedicamus/Deo gratias')
        # psalm counts
        np=len(set(re.findall(r'^(Psalmus \d+|Canticum Trium)',L,re.M)))
        want={'matins':3,'lauds':5,'vespers':5,'compline':3,'prime':3,'terce':3,'sext':3,'none':3}[hour]
        if np!=want: issues[tag].append(f'psalms {np}, expected {want}')
        # English left in Latin or vice versa
        if re.search(r'\b(the|and|thou|thee)\b',L): issues[tag].append('English words in Latin column: '+re.search(r'.{0,30}\b(the|and|thou|thee)\b.{0,30}',L).group(0))
        if re.search(r'[áéíóúǽ]',E): issues[tag].append('accented Latin in English column: '+re.search(r'.{0,30}[áéíóúǽ].{0,30}',E).group(0))
for k,v in issues.items():
    print(k); [print('   ',x) for x in v]
print('keys with issues', len(issues))
