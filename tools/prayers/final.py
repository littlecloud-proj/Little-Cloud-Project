import json,re
O=json.load(open('/tmp/gen/out.json'))
HL=json.load(open('hammer_litanies.json'))
V=json.load(open('veni.json'))
H='/tmp/do/divinum-officium-master/web/www/horas/'
def sec(path,head):
    t=open(path,encoding='utf-8').read()
    for s in re.split(r'\n(?=\[)',t):
        if s.split('\n')[0]==head: return [x for x in s.split('\n')[1:] if x.strip()]
def k(s): return '"'+s.replace('\\','\\\\').replace('"','\\"').replace('$','\\$').replace('\n','\\n')+'"'

# ---- Veni Creator
en_h=V['en_hymn'].replace('The Father and the Son to know;','The Father and the Son to know,')
cla=sec(H+'Latin/Tempora/Pasc7-0.txt','[Oratio]')[0].replace('hodiérna die ','')
cen=sec(H+'English/Tempora/Pasc7-0.txt','[Oratio]')[0].replace('Who on this day didst','Who didst')
pla=sec(H+'Latin/Psalterium/Common/Prayers.txt','[Per Dominum eiusdem]')[0][3:]
pen=sec(H+'English/Psalterium/Common/Prayers.txt','[Per Dominum eiusdem]')[0][3:].strip()
veni_la=V['la_hymn'].strip()+'\n\n℣. Emítte Spíritum tuum, et creabúntur.\n℟. Et renovábis fáciem terræ.\n\nOrémus.\n'+cla+' '+pla+'\n℟. Amen.'
g=open('/tmp/gen/gahan.txt').read().split('\n'); gi=[n for n,l in enumerate(g) if l.startswith('O God, who by the light of the Holy Ghost, didst instruct the')][0]
gcol=re.sub(r'\s+',' ',' '.join(x.strip() for x in g[gi:gi+4])).strip()
veni_en=en_h+'\n\n℣. Send forth Thy Spirit, and they shall be created.\n℟. And Thou shalt renew the face of the earth.\n\nLet us pray.\n'+gcol

# ---- Our Lady of Mount Carmel (replaces the Scapular explanation)
carm_la_or=sec(H+'Latin/Sancti/07-16.txt','[Oratio]')[0]
qv_la=sec(H+'Latin/Psalterium/Common/Prayers.txt','[Qui vivis]')[0][3:]
c=open('/tmp/gen/carmel1850.txt').read().split('\n')
i=[n for n,l in enumerate(c) if l.strip().startswith('Oh  God,   who   hast  adorned')][0]
raw=' '.join(x.strip() for x in c[i:i+25])
raw=re.sub(r'\s+',' ',raw); raw=raw[:raw.index('Amen.')+5]
raw=re.sub(r'\b115\b ?','',raw).replace('of- Carmelites','of Carmelites').replace('as- sisted','assisted').replace('happi- ness','happiness')
raw=raw.replace('OF THY BLESSED AND EVER VIRGIN MOTHER MARY','of Thy blessed and ever Virgin Mother Mary').replace('Oh God, who','O God, who').replace('the Society of Carmelites','the Order of Carmelites')
carm_en_or=raw.replace('Who liveth and reigneth with the Father in the unity of the Holy Ghost, one God, world without end. Amen.','Who livest and reignest with God the Father, in the unity of the Holy Ghost, one God, world without end. Amen.')
carm_la='℣. Ora pro nobis, sancta Dei Génetrix.\n℟. Ut digni efficiámur promissiónibus Christi.\n\nOrémus.\n'+carm_la_or+' '+qv_la+'\n℟. Amen.'
carm_en='℣. Pray for us, O holy Mother of God.\n℟. That we may be made worthy of the promises of Christ.\n\nLet us pray.\n'+carm_en_or

# ---- Loreto Latin orthography + spacing; Spanish fixes
la=O['loreto']['la']
for a,b in [('Speculum iustitiæ','Spéculum justítiæ'),('Causa nostræ laetítiæ','Causa nostræ lætítiæ'),('Foederis arca','Fœ́deris arca'),('Iánua cæli','Jánua cæli'),('Rosa mystica','Rosa mýstica'),('Virgo veneranda','Virgo veneránda'),
            ('℣. Ora pro nobis Sancta Dei Génetrix.','℣. Ora pro nobis, sancta Dei Génetrix.'),('Regína sine labe originali concépta','Regína sine labe originháli concépta'.replace('originháli','originháli')) ]:
    la=la.replace(a,b)
la=la.replace('Regína sine labe originháli concépta','Regína sine labe originális concépta')
pre=la[:la.index('Orémus.')]
la=pre+'Orémus.\nConcéde nos fámulos tuos, quǽsumus, Dómine Deus, perpétua mentis et córporis sanitáte gaudére: et, gloriósa beátæ Maríæ semper Vírginis intercessióne, a præsénti liberári tristítia, et ætérna pérfrui lætítia. Per Christum Dóminum nostrum. Amen.'
es=O['loreto']['es'].replace('Santa María, ruega por nosotros.\n\nSanta Madre','Santa María, ruega por nosotros.\nSanta Madre').replace('Cristo, escúchanos.\nDios Padre','Cristo, escúchanos.\n\nDios Padre').replace('Por Cristo nuestro Señor.\nAmén.','Por Cristo nuestro Señor. Amén.')

# ---- Holy Ghost punctuation
hg=O['holyghost']['en'].split('\n')
hg2=[]
for l in hg:
    if l and l[-1] not in '.!?,:;' : l+='.'
    l=l.replace(', Send us',', send us')
    hg2.append(l)
hg='\n'.join(hg2)
hg=re.sub(r'(Lamb of God[^\n]*)\n\n(?=Lamb of God)',r'\1\n',hg)
hg=hg.replace('℣. Come, Holy Ghost! Fill the hearts of Thy faithful,','℣. Come, O Holy Ghost, fill the hearts of Thy faithful.')

# ---- Montfort fixes
m=O['montfort']['en']
m=m.replace('I salute Thee, then, O immaculate Mary','\n\nI salute thee, then, O immaculate Mary').replace(' \n\nI salute thee','\n\nI salute thee').replace('I salute Thee, O sure refuge','I salute thee, O sure refuge')
m=re.sub(r'\n{3,}','\n\n',m).replace('. \n\n','.\n\n')

# ---- Litany of Saints Latin tidy
ls_path='/mnt/user-data/outputs/LittleCloud/app/src/main/java/com/littlecloud/app/data/LitanySaints.kt'
src=open(ls_path,encoding='utf-8').read()
mla=re.search(r'val latin: String = "(.*)"\n',src); lsla=json.loads('"'+mla.group(1)+'"')
lsla=lsla.replace('\nOratio\nOrémus\n','\n\nOrémus.\n')
for s in ['Exáudi, quǽsumus','Ineffábilem nobis','Deus, qui culpa','Omnípotens sempitérne Deus, miserére','Deus, a quo sancta','Ure igne','Fidélium, Deus, ómnium','Actiónes nostras','Omnípotens sempitérne Deus, qui vivórum']:
    lsla=lsla.replace('\n'+s,'\n\n'+s)
lsla=lsla.replace('\nPer Dóminum nostrum Jesum Christum, Fílium tuum: qui tecum',' Per Dóminum nostrum Jesum Christum, Fílium tuum: qui tecum').replace('\n℟. Amen.\n℣. Dómine, exáudi oratiónem meam.\n℟. Et clamor meus ad te véniat.\n℣. Exáudiat','\n℟. Amen.\n\n℣. Dómine, exáudi oratiónem meam.\n℟. Et clamor meus ad te véniat.\n℣. Exáudiat')
lsla=lsla.replace('Kýrie, eléison.\nPater noster','Kýrie, eléison.\n\nPater noster').replace('Psalmus 69\n','Psalmus 69\n').replace('\n\nPsalmus 69','\n\nPsalmus 69')
lsla=lsla.replace('℟. Et clamor meus ad te véniat.\n℣. Dómine, exáudi oratiónem meam.\n℟. Et clamor meus ad te véniat.\n\nOrémus.','℟. Et clamor meus ad te véniat.\n\nOrémus.')
src=re.sub(r'val latin: String = ".*"\n', lambda _:'val latin: String = '+k(lsla)+'\n', src)
src=re.sub(r'val english: String = ".*"\n', lambda _:'val english: String = '+k(HL['saints_en'])+'\n', src)
open(ls_path,'w',encoding='utf-8').write(src)

nada_en=O['nada']['en']+'\n\nSt. Teresa’s bookmark, found in her breviary. English: H. W. Longfellow.'
P=[
 ('litany_loreto','Litany of Loreto','Litany of the Blessed Virgin Mary',la,HL['loreto_en']),
 ('litany_saints','Litany of the Saints','Litaniæ Sanctorum','LitanySaints.latin','LitanySaints.english'),
 ('veni_creator','Veni Creator Spiritus','Hymn to the Holy Ghost',veni_la,veni_en),
 ('scapular','Our Lady of Mount Carmel','Collect of 16 July · for those who wear the Scapular',carm_la,carm_en),
 ('litany_holy_ghost','Litany of the Holy Ghost','For private recitation',None,hg),
 ('litany_holy_name','Litany of the Holy Name','Of Jesus · approved by Leo XIII, 1886',None,HL['holyname']),
 ('litany_sacred_heart','Litany of the Sacred Heart','Approved by Leo XIII, 1899',None,HL['sacred']),
 ('therese_morning','Morning Prayer of St. Thérèse','St. Thérèse of the Child Jesus',None,O['therese']['en']),
 ('nada_te_turbe','Nada te turbe','St. Teresa of Jesus',None,nada_en),
 ('montfort_consecration','Act of Consecration','St. Louis de Montfort · to Jesus through Mary',None,m),
 ('o_jesu_vivens','O Jesu, vivens in Maria','O Jesus, living in Mary · Fr. Olier',None,O['olier']['en']),
]
out=['package com.littlecloud.app.data','','/**',' * Further prayers, litanies and Carmelite and Montfortian devotions.',' * English litanies: Hammer, General Catholic Devotions (Benziger, 1909, Imprimatur).',' * Veni Creator: Caswall, Lyra Catholica (1849). Latin: Roman Breviary via Divinum Officium.',' */','internal object MorePrayers {','    val prayers: List<Prayer> = listOf(']
for id,t,sub,la_,en_ in P:
    v=lambda x: 'null' if x is None else (x if x.startswith('LitanySaints.') else k(x))
    out.append(f'        Prayer({k(id)}, {k(t)}, {k(sub)},\n            {v(la_)},\n            {v(en_)}),')
out+=['    )','','    /** Spanish texts from traditional sources. */','    val spanish: Map<String, String> = mapOf(',f'        "litany_loreto" to {k(es)},',f'        "nada_te_turbe" to {k(O["nada"]["es"])},','    )','}']
open('/mnt/user-data/outputs/LittleCloud/app/src/main/java/com/littlecloud/app/data/MorePrayers.kt','w',encoding='utf-8').write('\n'.join(out)+'\n')
print('ok', carm_en_or[:60], '...', carm_en_or[-40:])
