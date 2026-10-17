import json,re,sys
from render import render,rows
from derive_dates import pick,HOURS
VER={'1954':'Divino%20Afflatu%20-%201954','1955':'Reduced%20-%201955','1960':'Rubrics%201960%20-%201960'}
HEADS=re.compile(r'^(Incipit|Start|Psalmi|Psalms|Capitulum|Chapter|Canticum:|Canticle:|Oratio|Prayer|Conclusio|Conclusion|Invitatorium|Invitatory|Hymnus|Hymn|Nocturn|Lectio|Lesson|Te Deum|Responsorium|Lectio brevis|Commemoratio|Commemoration|Absolutio|Benedictio|Versus|Verse|Preces)\b')
def lines(col):
    return [re.sub(r'\s+',' ',l).strip() for l in col.split('\n') if l.strip()]
def parse(R):
    out=[]
    for la,en in R:
        L=[x for x in lines(la) if x!='Top Next']; E=lines(en)
        if E and re.fullmatch(r'\d+',E[0]): E=E[1:]
        if L and '{omittitur}' in L[0] and len(L)==1: continue
        hl=he=None
        body=re.sub(r'^Top  Next\n','',la)
        if L and re.match(r'^[^\n]{1,80}\n\s*\n\s*\n',body):
            hl=re.sub(r'\s*\{.*?\}\s*','',L[0]).strip(); L=L[1:]
            if E: he=re.sub(r'\s*\{.*?\}\s*','',E[0]).strip(); E=E[1:]
        if not L and not E: continue
        out.append({'hl':hl,'he':he,'la':L,'en':E})
    return out
def gen(k,v,date=None):
    d=date or pick[k]; h=k.split('|')[2]
    return parse(rows(render(f'{d.month}-{d.day}-{d.year}',HOURS[h],VER[v])))
if __name__=='__main__':
    k=sys.argv[1]; v=sys.argv[2]
    print(json.dumps(gen(k,v),ensure_ascii=False)[:3000])
