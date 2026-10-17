"""Parses the Gutenberg Douay-Rheims (#8300) into {book: {chapter: {verse: text}}}."""
import re, json
lines = open('/tmp/rd/bible/dr2.txt', encoding='utf-8').read().split('\n')
start = next(i for i, l in enumerate(lines) if l.strip() == 'Genesis Chapter 1')
end = next(i for i, l in enumerate(lines) if l.startswith('*** END OF'))
bible, book, ch, cur = {}, None, None, None
chap = re.compile(r'^(.+) Chapter (\d+)$')
verse = re.compile(r'^(\d+):(\d+)\. (.*)$')
para = []
def flush():
    global cur, para
    if cur and para:
        b, c, v = cur
        bible.setdefault(b, {}).setdefault(c, {})[v] = re.sub(r'\s+', ' ', ' '.join(para)).strip()
    cur, para = None, []
for l in lines[start:end]:
    s = l.strip()
    m = chap.match(s)
    if m and len(s) < 40:
        flush(); book, ch = m.group(1), int(m.group(2)); continue
    m = verse.match(s)
    if m and book:
        flush()
        cur = (book, int(m.group(1)), int(m.group(2))); para = [m.group(3)]; continue
    if not s:
        flush(); continue
    if cur: para.append(s)
flush()
json.dump(bible, open('/tmp/rd/bible/dr.json', 'w'), ensure_ascii=False)
print(len(bible), sum(len(c) for c in bible.values()), sum(len(v) for c in bible.values() for v in c.values()))
print(bible['Matthew'][13][44]); print(bible['Proverbs'][31][10]); print(bible['Psalms'][22][1]); print(bible['John'][1][1])
