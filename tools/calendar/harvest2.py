import json, sys, datetime
sys.path.insert(0,'/tmp/da')
from harvest import run, perl
from concurrent.futures import ThreadPoolExecutor
done=json.load(open('/tmp/da/done.json'))
cache={}
for r in done.values():
    if r['winner'] and r['la']: cache.setdefault(r['winner'],(r['la'],r['en']))
def run2(d):
    p=perl('probe.pl', d); info={}
    for l in p.split('\n'):
        if '\t' in l: k,v=l.split('\t',1); info[k]=v
    w=info.get('WINNER','')
    if w in cache:
        la,en=cache[w]
        return {'d':d.isoformat(),'winner':w,'commemo':info.get('COMMEMO',''),'dayname':info.get('DAYNAME',''),'wrank':info.get('WRANK',''),'rank':info.get('RANK',''),'color':'','head':'','la':la,'en':en}
    r=run(d)
    if r['winner'] and r['la']: cache[r['winner']]=(r['la'],r['en'])
    return r
days=[]; d=datetime.date(2026,1,1)
while d<=datetime.date(2050,12,31):
    if d.isoformat() not in done: days.append(d)
    d+=datetime.timedelta(1)
print('todo',len(days),'cache',len(cache),flush=True)
with ThreadPoolExecutor(2) as ex, open('/tmp/da/rest.jsonl','w') as f:
    for i,r in enumerate(ex.map(run2,days)):
        f.write(json.dumps(r,ensure_ascii=False)+'\n')
        if i%100==0: f.flush()
print('done',flush=True)
