import json,re
O=json.load(open('out.json'))
def k(s): return '"'+s.replace('\\','\\\\').replace('"','\\"').replace('$','\\$').replace('\n','\\n')+'"'
scap=("The Brown Scapular is the habit of Our Lady of Mount Carmel in small form. It is received once, with the blessing and enrolment of a priest; afterwards any scapular of brown wool may be worn in its place without a new blessing.\n\n"
"Our Lady’s promise to St. Simon Stock (1251): “Whosoever dies clothed in this shall not suffer eternal fire.”\n\n"
"The Sabbatine Privilege, as taught by the Order, is granted to those who:\n"
"1. Wear the Scapular faithfully.\n"
"2. Observe chastity according to their state of life.\n"
"3. Recite daily the Little Office of the Blessed Virgin, or keep the fast and abstinence of the Church and abstain from meat on Wednesdays and Saturdays, or say another prayer (commonly five decades of the Rosary) as assigned by a priest with the faculty.\n\n"
"A daily practice for those who wear it:\n"
"Kiss the Scapular each morning and say: Our Lady of Mount Carmel, pray for us.\n"
"Pray the Flos Carmeli, and on Saturdays honour Our Lady with some small act of love.\n\n"
"Mary, Flower of Carmel, Mother and Beauty of Carmel, pray for us.")
nada_en=O['nada']['en']+'\n\nSt. Teresa’s bookmark, found in her breviary. English: H. W. Longfellow.'
P=[
 ('litany_loreto','Litany of Loreto','Litany of the Blessed Virgin Mary',O['loreto']['la'],O['loreto']['en']),
 ('litany_saints','Litany of the Saints','Litaniæ Sanctorum','LitanySaints.latin','LitanySaints.english'),
 ('veni_creator','Veni Creator Spiritus','Hymn to the Holy Ghost',O['veni']['la'],O['veni']['en']),
 ('scapular','Brown Scapular','Habit of Our Lady of Mount Carmel',None,scap),
 ('litany_holy_ghost','Litany of the Holy Ghost','For private recitation',None,O['holyghost']['en']),
 ('litany_holy_name','Litany of the Holy Name','Of Jesus · from Gahan’s Catholic Piety',None,O['holyname']['en']),
 ('litany_sacred_heart','Litany of the Sacred Heart','Approved by Leo XIII, 1899',None,O['sacredheart']['en']),
 ('therese_morning','Morning Prayer of St. Thérèse','St. Thérèse of the Child Jesus',None,O['therese']['en']),
 ('nada_te_turbe','Nada te turbe','St. Teresa of Jesus',None,nada_en),
 ('montfort_consecration','Act of Consecration','St. Louis de Montfort · to Jesus through Mary',None,O['montfort']['en']),
 ('o_jesu_vivens','O Jesu, vivens in Maria','O Jesus, living in Mary · Fr. Olier',None,O['olier']['en']),
]
out=['package com.littlecloud.app.data','','/**',' * Further prayers, litanies and Carmelite and Montfortian devotions.',' * Generated from public-domain sources; see the subtitle of each prayer.',' */','internal object MorePrayers {','    val prayers: List<Prayer> = listOf(']
for id,t,sub,la,en in P:
    def v(x):
        if x is None: return 'null'
        if x.startswith('LitanySaints.'): return x
        return k(x)
    out.append(f'        Prayer({k(id)}, {k(t)}, {k(sub)},\n            {v(la)},\n            {v(en)}),')
out.append('    )')
out.append('')
out.append('    /** Spanish texts from traditional sources. */')
out.append('    val spanish: Map<String, String> = mapOf(')
out.append(f'        "litany_loreto" to {k(O["loreto"]["es"])},')
out.append(f'        "nada_te_turbe" to {k(O["nada"]["es"])},')
out.append('    )')
out.append('}')
open('/mnt/user-data/outputs/LittleCloud/app/src/main/java/com/littlecloud/app/data/MorePrayers.kt','w').write('\n'.join(out)+'\n')
# saints english update
p='/mnt/user-data/outputs/LittleCloud/app/src/main/java/com/littlecloud/app/data/LitanySaints.kt'
s=open(p,encoding='utf-8').read()
s=re.sub(r'val english: String = ".*"\n', lambda m:'val english: String = '+k(O['saints_en'])+'\n', s)
open(p,'w',encoding='utf-8').write(s)
print('ok')
