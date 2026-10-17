import json,datetime,difflib,re,sys
from concurrent.futures import ThreadPoolExecutor
SRC='/home/claude/little-cloud-project/app/src/commonMain/composeResources/files/office1954.json'
a=json.load(open(SRC))
def easter(y):
    a_=y%19;b=y//100;c=y%100;d=b//4;e=b%4;f=(b+8)//25;g=(b-f+1)//3;hh=(19*a_+b-d-g+15)%30;i=c//4;k=c%4;l=(32+2*e+2*i-hh-k)%7;m=(a_+11*hh+22*l)//451
    return datetime.date(y,(hh+l-7*m+114)//31,((hh+l-7*m+114)%31)+1)
def advent(y):
    d=datetime.date(y,12,25)
    while d.weekday()!=6: d-=datetime.timedelta(1)
    return d-datetime.timedelta(21)
def key(d,hour):
    adv=advent(d.year)
    if adv<=d<datetime.date(d.year,12,25): s='adv'
    elif (d.month==12 and d.day>=25) or d.month==1 or (d.month==2 and d.day==1): s='nat'
    else: s='year'
    e=(d-easter(d.year)).days
    t='pasch' if 0<=e<=55 else 'sept' if -63<=e<=-1 else 'norm'
    if hour!='matins': return f'{s}|{t}|{hour}'
    wd=d.isoweekday()%7  # 0 sunday
    g={0:'A',1:'A',4:'A',2:'B',5:'B'}.get(wd,'C')
    return f'{s}|{t}|matins|{g}'
HOURS={'matins':'Matutinum','lauds':'Laudes','prime':'Prima','terce':'Tertia','sext':'Sexta','none':'Nona','vespers':'Vespera','compline':'Completorium'}
# choose a date per key: avoid Holy Week/Triduum and great feasts; prefer weekday ferias
pick={}
d=datetime.date(2026,1,2)
while d<datetime.date(2030,12,31):
    e=(d-easter(d.year)).days
    if not (-7<=e<=1) and (d.month,d.day) not in [(12,25),(1,1),(1,6),(2,2)]:
        for h in HOURS:
            k=key(d,h)
            if k in a and k not in pick: pick[k]=d
    d+=datetime.timedelta(1)
miss=set(a)-set(pick); print('unpicked',miss)
