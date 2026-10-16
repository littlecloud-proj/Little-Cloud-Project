import re
ORD={'I':1,'II':2,'III':3,'IV':4,'V':5,'VI':6,'VII':7,'VIII':8,'IX':9,'X':10,'XI':11,'XII':12,'XIII':13,'XIV':14,'XV':15,'XVI':16,'XVII':17,'XVIII':18,'XIX':19,'XX':20,'XXI':21,'XXII':22,'XXIII':23,'XXIV':24,
     'prima':1,'secunda':2,'tertia':3,'quarta':4,'quinta':5,'sexta':6,'septima':7,'Prima':1,'Secunda':2,'Tertia':3,'Quarta':4,'Quinta':5,'Sexta':6,'Septima':7}
def ordn(n):
    n=int(n); s='th' if 10<=n%100<=20 else {1:'st',2:'nd',3:'rd'}.get(n%10,'th'); return f'{n}{s}'
WD={2:'Monday',3:'Tuesday',4:'Wednesday',5:'Thursday',6:'Friday'}
def num(t): return ORD.get(t.strip().rstrip('.'), None)
OCT=[(r'Nativitatis S\.? Joannis Baptistæ','the Nativity of St. John the Baptist'),(r'S\.? Joannis Baptistæ','St. John the Baptist'),
 (r'Nativitatis Domini|Nativitatis','Christmas'),(r'Epiphaniæ','the Epiphany'),(r'[Pp]aschæ','Easter'),(r'Ascensionis','the Ascension'),(r'[Pp]entecostes','Pentecost'),
 (r'S(?:ancti|anctissimi)? ?Corporis Christi|Corporis Christi','Corpus Christi'),(r'SSmi Cordis.*|Sacratissimi Cordis.*','the Sacred Heart'),
 (r'Patrocinii S\.? Joseph|S\.? Joseph','St. Joseph'),(r'Omnium Sanctorum','All Saints'),(r'S\. Assumptionis.*|Assumptionis.*','the Assumption'),
 (r'Concept.*','the Immaculate Conception'),(r'Ss\. Apostolorum Petri et Pauli|Ss\. Petri et Pauli.*','SS. Peter and Paul'),(r'S\. Laurentii.*','St. Lawrence'),
 (r'S\. Stephani.*','St. Stephen'),(r'S\. Joannis Apostoli.*','St. John the Apostle'),(r'Ss\. Innocentium','the Holy Innocents'),(r'Nativitatis Beatæ Mariæ Virginis|Nativitatis B\.M\.V\.','the Nativity of Our Lady')]
def octname(x):
    x=x.strip()
    for p,e in OCT:
        if re.fullmatch(p,x) or re.match(p,x): return e
    return None
SEASON=[('Adventus','Advent'),('post Epiphaniam','after Epiphany'),('Post Epiphaniam','after Epiphany'),('Quadragesimæ','Lent'),('in Quadragesima','Lent'),('Passionis','Passiontide'),('post Octavam Paschæ','after Easter'),('Post Pascha','after Easter'),('post Octavam Pentecostes','after Pentecost'),('Post Pentecosten','after Pentecost')]
FIX={
 'Dominica Resurrectionis':'Easter Sunday','In Ascensione Domini':'Ascension of Our Lord','Dominica Pentecostes':'Pentecost Sunday',
 'Dominica Sanctissimæ Trinitatis':'Trinity Sunday','Festum Sanctissimi Corporis Christi':'Corpus Christi','Sacratissimi Cordis Domini Nostri Jesu Christi':'Most Sacred Heart of Jesus',
 'Sanctæ Familiæ Jesu Mariæ Joseph':'The Holy Family','Dominica in Albis in Octava Paschæ':'Low Sunday · Octave of Easter','Patrocinii St. Joseph Confessoris Sponsi B.M.V. Confessoris':'Solemnity of St. Joseph',
 'Feria Secunda in Rogationibus':'Rogation Monday','Feria Tertia in Rogationibus':'Rogation Tuesday','Feria Quarta in Rogationibus in Vigilia Ascensionis':'Rogation Wednesday · Vigil of the Ascension',
 'Sabbato in Vigilia Pentecostes':'Vigil of Pentecost','Dominica in Septuagesima':'Septuagesima Sunday','Dominica in Sexagesima':'Sexagesima Sunday','Dominica in Quinquagesima':'Quinquagesima Sunday',
 'Feria Quarta Cinerum':'Ash Wednesday','Dominica in Palmis':'Palm Sunday','Dominica de Passione':'Passion Sunday','Feria Quinta in Cena Domini':'Maundy Thursday','Feria Sexta in Parasceve':'Good Friday',
 'Sabbato Sancto':'Holy Saturday','In Epiphania Domini':'The Epiphany of Our Lord','In Nativitate Domini':'The Nativity of Our Lord','In Circumcisione Domini':'The Circumcision of Our Lord',
 'Dominica Infra Octavam Nativitatis':'Sunday within the Octave of Christmas','Dominica infra Octavam Ascensionis':'Sunday within the Octave of the Ascension','Dominica Laetare':'Laetare Sunday',
 'In Dedicatione Basilicarum Ss. Apostolorum Petri et Pauli':'Dedication of the Basilicas of SS. Peter and Paul','Ss. Nominis Jesu':'The Most Holy Name of Jesus',
 'In Commemoratione Omnium Fidelium Defunctorum':'All Souls','Domini Nostri Jesu Christi Regis':'Our Lord Jesus Christ the King','In Vigilia Nativitatis Domini':'Vigil of the Nativity','In Vigilia Epiphaniæ':'Vigil of the Epiphany',
}
EMBER={'Adventu':'Advent','Quadragesimæ':'Lent','Pentecostes':'Pentecost','Septembris':'September'}
EMBDAY={'IV':'Wednesday','Quarta':'Wednesday','VI':'Friday','Sexta':'Friday','Sabbato':'Saturday'}
WEEKS={'Passionis':'Passion Week','Majoris Hebdomadæ':'Holy Week'}
def tr(la):
    la=re.sub(r'\s+',' ',la).strip()
    la=re.sub(r' [IV]+\. (Januarii|Februarii|Martii|Aprilis|Maji|Junii|Julii|Augusti|Septembris|Octobris|Novembris|Decembris)$','',la)
    if la in FIX: return FIX[la]
    if re.match(r'^Dominica \w+ et Ultima Post Pentecosten',la): return 'Last Sunday after Pentecost'
    m=re.match(r'^Dominica (\w+) in Quadragesima$',la)
    if m and num(m.group(1)): return f"{ordn(num(m.group(1)))} Sunday of Lent"
    if la=='Sabbato in Albis': return 'Saturday in Albis · within the Octave of Easter'
    if la.startswith('Septem Dolorum'): return 'Seven Sorrows of Our Lady'
    m=re.match(r'^(?:Feria (\w+)|Sabbato) (?:infra Hebdomadam Passionis|Majoris Hebdomadæ)$',la)
    if m:
        d='Saturday' if la.startswith('Sabbato') else WD.get(num(m.group(1)) or 0,'Feria')
        return f"{d} of {'Holy Week' if 'Majoris' in la else 'Passion Week'}"
    m=re.match(r'^(?:Feria (\w+)|Sabbato) post Cineres$',la)
    if m:
        d='Saturday' if la.startswith('Sabbato') else WD.get(num(m.group(1)) or 0,'Feria')
        return f"{d} after Ash Wednesday"
    if la=='Feria IV Cinerum': return 'Ash Wednesday'
    m=re.match(r'^(?:Feria (\w+)|Sabbato) infra Hebdomadam (Septuagesimæ|Sexagesimæ|Quinquagesimæ)$',la)
    if m: return 'Feria'
    m=re.match(r'^Dominica (\w+) (?:Adventus|Quadragesimæ)$',la)
    if m and num(m.group(1)): return f"{ordn(num(m.group(1)))} Sunday of {'Advent' if 'Advent' in la else 'Lent'}"
    m=re.match(r'^Dominica (\w+) [Pp]ost (Epiphaniam|Pentecosten|Pascha)(?: infra Octavam (.*))?$',la)
    if m and num(m.group(1)):
        e=f"{ordn(num(m.group(1)))} Sunday after {dict(Epiphaniam='Epiphany',Pentecosten='Pentecost',Pascha='Easter')[m.group(2)]}"
        if m.group(3): e+=f" · within the Octave of {octname(m.group(3)) or m.group(3)}"
        return e
    m=re.match(r'^(Feria (\w+)|Sabbato) Quattuor Temporum (?:in )?(\w+)',la)
    if m:
        d='Saturday' if m.group(1)=='Sabbato' else EMBDAY.get(m.group(2),'')
        return f"Ember {d} of {EMBER.get(m.group(3),m.group(3))}"
    m=re.match(r'^(?:De )?(?:Diei?|die)? ?(\w+) (?:die )?infra [Oo]ctavam (.*)$',la) or re.match(r'^(\w+) die infra [Oo]ctavam (.*)$',la) or re.match(r'^Die (\w+) infra [Oo]ctavam (.*)$',la)
    if m and num(m.group(1)) and octname(m.group(2)):
        return f"{ordn(num(m.group(1)))} day within the Octave of {octname(m.group(2))}"
    m=re.match(r'^(?:Feria (\w+)|Sabbato) infra [Oo]ctavam (.*)$',la)
    if m and octname(m.group(2)):
        d='Saturday' if la.startswith('Sabbato') else WD.get(num(m.group(1)) or 0,'Weekday')
        return f"{d} within the Octave of {octname(m.group(2))}"
    m=re.match(r'^Feria (\w+) post Octavam (.*)$',la)
    if m and octname(m.group(2)):
        return f"{WD.get(num(m.group(1)) or 0,'Feria')} after the Octave of {octname(m.group(2))}"
    m=re.match(r'^In Octava (.*)$',la)
    if m and octname(m.group(1)): return f"Octave Day of {octname(m.group(1))}"
    m=re.match(r'^(?:Feria (\w+)|Sabbato) infra Hebdomadam (\w+) (.*)$',la)
    if m:
        d='Saturday' if la.startswith('Sabbato') else WD.get(num(m.group(1)) or 0,'Feria')
        n=num(m.group(2)); rest=m.group(3)
        for a,b in SEASON:
            if a in rest:
                if b=='Advent': return f"{d} of the {ordn(n)} week of Advent"
                if b=='Lent': return f"{d} of the {ordn(n)} week of Lent"
                return "Feria"
        return "Feria"
    m=re.match(r'^(?:Feria (\w+)|Sabbato) (?:in|post) (?:Hebdomada|Dominica)',la)
    m=re.match(r'^In Vigilia (.*)$',la)
    if m:
        x=m.group(1)
        o=octname(x)
        if o: return f"Vigil of {o}"
        return None
    return None
