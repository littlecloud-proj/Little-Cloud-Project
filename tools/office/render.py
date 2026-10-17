import subprocess,os,glob,re,html,sys
CGI=glob.glob('/tmp/do/*/web/cgi-bin/horas')[0]
def render(date,hour,ver,lang2='English'):
    q=f"command=pray{hour}&date={date}&version={ver}&lang2={lang2}&votive=C12"
    env=dict(os.environ,REQUEST_METHOD='GET',QUERY_STRING=q)
    return subprocess.run(['perl','-I/tmp/perlshim','officium.pl'],cwd=CGI,env=env,capture_output=True).stdout.decode('utf-8','replace')
def txt(x):
    x=re.sub(r'<BR\s*/?>','\n',x,flags=re.I);x=re.sub(r'<[^>]+>','',x);return html.unescape(x).replace('\xa0',' ')
def rows(s):
    out=[]
    for r in re.findall(r'<TR[^>]*>(.*?)</TR>',s,re.S|re.I):
        c=re.findall(r'<TD[^>]*>(.*?)</TD>',r,re.S|re.I)
        if len(c)>=2: out.append((txt(c[0]),txt(c[1])))
    return out
if __name__=='__main__':
    for v in ['Divino%20Afflatu%20-%201954','Reduced%20-%201955']:
        open(f'{sys.argv[2]}_{v[:4]}.txt','w').write('\n=====\n'.join(a for a,b in rows(render('7-7-2026',sys.argv[2],v))))
