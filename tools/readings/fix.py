import json, datetime, sys
sys.path.insert(0, '/tmp/rd')
import harvest as h
from concurrent.futures import ThreadPoolExecutor
def easter(y):
    a=y%19;b=y//100;c=y%100;d=b//4;e=b%4;f=(b+8)//25;g=(b-f+1)//3;hh=(19*a+b-d-g+15)%30;i=c//4;k=c%4;l=(32+2*e+2*i-hh-k)%7;m=(a+11*hh+22*l)//451
    return datetime.date(y,(hh+l-7*m+114)//31,((hh+l-7*m+114)%31)+1)
rows = {}
for l in open('all.jsonl'):
    j = json.loads(l); rows[(j['d'], j['v'])] = j
redo = set()
for (d, v), j in rows.items():
    if 'r' not in j or not any(x['k'] == 'E' for x in j['r']) or not any(x['k'] == 'L' for x in j['r']): redo.add((d, v))
for y in range(2026, 2051):
    e = easter(y)
    for k in range(-7, 0):
        for v in ('1954', '1962'): redo.add(((e + datetime.timedelta(k)).isoformat(), v))
print('redo', len(redo))
jobs = [(datetime.date.fromisoformat(d), v) for d, v in sorted(redo)]
with ThreadPoolExecutor(4) as ex:
    for r in ex.map(h.run, jobs): rows[(r['d'], r['v'])] = r
with open('all_fixed.jsonl', 'w') as f:
    for k in sorted(rows): f.write(json.dumps(rows[k], ensure_ascii=False) + '\n')
still = [(d, v, [x['k'] for x in j.get('r', [])]) for (d, v), j in rows.items() if 'r' not in j or not any(x['k'] == 'E' for x in j['r']) or not any(x['k'] == 'L' for x in j['r'])]
print('still missing', len(still)); print(still[:30])
