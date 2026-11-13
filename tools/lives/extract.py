#!/usr/bin/env python3
"""Pulls the "Short Accounts of Certain Feasts and Lives of Saints" out of the scan text of the
Lasance missal and writes lasance-accounts.txt, the file build.py reads.

    python3 tools/lives/extract.py            # from the section kept in sources/
    python3 tools/lives/extract.py /path/to/fr-lasance-missal-1937_djvu.txt   # cut the section afresh

The scan text is the "Full Text" download of archive.org/details/fr-lasance-missal-1937. Only the
section used (sources/lasance-accounts-scan.txt) and a list of the book's words, which decides
whether a word broken at the end of a line had a hyphen, are kept in this repository. Nothing is reworded here. The script only
  * cuts the section out and drops the page furniture (running heads, page numbers, web address);
  * joins the printed lines, closing words the printer broke across lines;
  * splits the text into entries at each date and each saint's heading;
  * applies corrections.tsv, the list of misread words put right, one per line, for review.
Entries named in hold.txt are left out of the app until someone has read them against the page.
"""
import collections, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
MONTHS = ["Januar", "Februar", "March", "April", "Ma", "June", "Jul", "August", "September", "October", "ovember", "December"]
ROLE = r"(Martyrs?|Confessors?|Virgins?|Bishops?|Popes?|Abbots?|Abbess|Widows?|Apostles?|Evangelist|Doctors?|Kings?|Queen|Vigil|Feast|Octave|Dedication|Commemoration|Archangels?|Hermit|Priest|Deacon|Penitent|Emperor|Empress)"
SUBHEAD = re.compile(r"^((?:The |Octave |Vigil |Commemoration )?[^:]{0,40}?\b(?:Sts?|SS)[.,] ?[^:]{2,110}?|The [^:]{3,90}?):\s")
DATELINE = re.compile(r"^(?:\W|\w\s){0,3}[A-Za-z][A-Za-z0-9]{1,3}[.,]{0,2} ?\d{1,2} ?[.,] ?(\(|\d|St|Sts|SS|The|Feast|Vigil|Of|Octave|Our|Dedication|Conversion|Commemoration|Immaculate)\b")
SAYS = r"\b(says?|said|writes?|wrote|replied|adds?|words?|follows?)$"
DATEHEAD = re.compile(r"^(?:\W|\w\s){0,3}([A-Za-z][A-Za-z0-9]{1,3})[.,]{0,2} ?(\d{1,2})\s?[.,]? ?(?:\(([^)]*)\)\.? ?)?(.*)$", re.S)


def section(raw):
    a = raw.index("Feasts of Januar")
    b = raw.index("Descriptions of S", a)
    return raw[a:b]


def vocabulary(raw):
    return collections.Counter(re.findall(r"[A-Za-zÀ-ÿ]+", raw))


def clean_lines(text):
    out = []
    for line in text.split("\n"):
        s = line.strip()
        if "ccwatershed" in s:
            continue
        if re.search(r"A[CO]COUNTS [CO][FP] FEASTS AND LIVES O[FP] SAINTS", s) or re.fullmatch(r"\d{4}", s):
            out.append("\f")  # a page turned here
            continue
        out.append(s)
    return out


def paragraphs(lines, vocab):
    """Joins lines into paragraphs. A blank line ends a paragraph only when the sentence is over."""
    paras, cur, gap = [], "", False
    def push():
        nonlocal cur
        if cur.strip():
            paras.append(re.sub(r"\s+", " ", cur).strip())
        cur = ""
    for s in lines:
        if s in ("", "\f"):
            gap = True
            continue
        if re.fullmatch(r"Feasts of \w+", s):
            push(); paras.append(s); gap = False
            continue
        if DATELINE.match(s) or (gap and SUBHEAD.match(s) and re.search(ROLE, SUBHEAD.match(s).group(1))
                                 and not re.search(SAYS, SUBHEAD.match(s).group(1))):
            push(); gap = False
        if gap and cur:
            ended = re.search(r"[.!?:][”\"’')]*$", cur.rstrip()) is not None
            starts = re.match(r"[‘“\"']?[A-Z0-9]", s) is not None
            if ended and starts:
                push()
        gap = False
        if cur.endswith("-"):
            head = re.search(r"([A-Za-zÀ-ÿ]+)-$", cur)
            tail = re.match(r"[A-Za-zÀ-ÿ]+", s)
            if head and tail:
                joined, hyph = head.group(1) + tail.group(0), None
                # Keep the hyphen only when the word is printed with one elsewhere and never without.
                if vocab[joined] == 0 and vocab[joined.lower()] == 0 and tail.group(0)[0].isupper():
                    cur = cur + s  # e.g. Mass-book: keep
                elif vocab[joined] == 0 and vocab[joined.lower()] == 0 and vocab[head.group(1)] > 3 and vocab[tail.group(0)] > 3:
                    cur = cur + s
                else:
                    cur = cur[:-1] + s
                continue
        cur = (cur + " " + s) if cur else s
    push()
    return paras


def entries(paras):
    out, month, day, cur = [], 0, 0, None
    for p in paras:
        m = re.match(r"^Feasts of (\w+)$", p)
        if m:
            name = m.group(1)
            month = next((i + 1 for i, k in enumerate(MONTHS) if k.lower() in name.lower()), month + 1)
            day = 0
            continue
        title = body = None
        d = DATEHEAD.match(p)
        if d and month and len(d.group(1)) <= 4 and ":" in d.group(4)[:260] and not p.startswith(("St.", "Sts.", "The ")):
            day = int(d.group(2))
            rest = d.group(4)
            title, body = rest.split(":", 1)
            if d.group(3):
                body = "(" + d.group(3).strip() + ".) " + body.strip()
        else:
            s = SUBHEAD.match(p)
            if s and cur is not None and re.search(ROLE, s.group(1)) and not re.search(SAYS, s.group(1)):
                title, body = s.group(1), p[s.end():]
        if title is not None:
            title = re.sub(r"\s+", " ", title).strip(" .,;")
            # Specks the scan left before a heading, and the comma it often reads for the point in "St."
            title = re.sub(r"^(?:[^A-Za-z]|\b[a-zà]\b)+\s*(?=(?:Sts?|SS|The|Vigil|Of|Our)\b)", "", title)
            title = re.sub(r"\b(Sts?),(?= )", r"\1.", title)
            # "…, Martyrs; Station at …" : the station goes to the head of the text.
            st = re.search(r"[;.,] ?((?:Station|Collecta|Night Vigil)\b.*)$", title)
            body = body.strip()
            if st:
                title = title[:st.start()].strip(" .,;")
                body = st.group(1).strip(" .;:") + ".\n" + body
            cur = {"date": f"{month:02d}-{day:02d}", "title": title, "paras": [body]}
            out.append(cur)
        elif cur is not None:
            cur["paras"].append(p)
    return out


SPECK = re.compile(r"(?<![\w.’'-])(?:[`^|{}\[\]$*~_<>=\\§¢£¥©®°+]+|[b-zìòùýÜ]|[BE-HJKN-UW-Z])(?![\w.’'-])")


MISREAD = [(r"(?<!\w)à(?!\w)", "a"), (r"&nd\b", "and"), (r"&bout\b", "about"), (r"&n\b", "an"), (r"(?<= )&(?= [a-z])", "a"),
           (r"\bihe\b", "the"), (r"\bihere\b", "there"), (r"\btne\b", "the"), (r"\btbe\b", "the"), (r"\bwbo\b", "who"), (r"\b1n\b", "in"),
           (r"\b1t\b", "it"), (r"\bal[il]\b(?<=\bali|\bail)", "all"), (r"(?<=[a-z])- (?=[A-Za-z])", " "), (r"\bJater\b", "later"), (r"\bDy\b", "by")]


def tidy(s):
    """Marks the scan leaves that are no part of the text: specks, and points and colons inside a phrase."""
    for a, b in MISREAD:                                    # letters the scan regularly takes for others
        s = re.sub(a, b, s)
    s = SPECK.sub(" ", s)                                   # a stray sign or lone letter between words
    s = re.sub(r"(?<=[a-z]{2})[.:](?=[a-z]{2})", " ", s)     # "Ursula.and", "was:martyred"
    s = re.sub(r"\b([a-z]{4,})\. (?=[a-z]{2,}\b)", r"\1 ", s)  # "innumerable. band"
    s = re.sub(r"\b(the|of|and|to|in|is|was|by|for|an|as|at|on|his|her)[.:] (?=[a-z])", r"\1 ", s)  # "the: whole"
    s = re.sub(r"\b(Sts?),(?= [A-Z])", r"\1.", s)            # "St, Peter"
    s = re.sub(r"\s+([,;:.!?])", r"\1", s)
    s = re.sub(r"([,;])\1+", r"\1", s)
    s = re.sub(r"\.\.(?!\.)", ".", s)
    s = re.sub(r'(?<![\w.,;:!?’”])"(?=\w)', "“", s).replace('"', "”")   # typewriter quotes, as the book prints them
    s = re.sub(r"\s{2,}", " ", s)
    return s.strip(" -")


def corrections():
    rows = []
    path = os.path.join(HERE, "corrections.tsv")
    if os.path.exists(path):
        for line in open(path, encoding="utf-8"):
            line = line.rstrip("\n")
            if not line or line.startswith("#"):
                continue
            parts = line.split("\t")
            if len(parts) >= 2:
                rows.append((parts[0], parts[1]))
    return rows


def held():
    path = os.path.join(HERE, "hold.txt")
    if not os.path.exists(path):
        return set()
    return {l.split("#")[0].strip() for l in open(path, encoding="utf-8") if l.split("#")[0].strip()}


def main():
    scan = os.path.join(HERE, "sources", "lasance-accounts-scan.txt")
    words = os.path.join(HERE, "sources", "scan-words.txt")
    if len(sys.argv) > 1:  # the full scan text: cut the section out and note the book's vocabulary
        raw = open(sys.argv[1], encoding="utf-8").read()
        os.makedirs(os.path.dirname(scan), exist_ok=True)
        open(scan, "w", encoding="utf-8").write(section(raw))
        v = vocabulary(raw)
        open(words, "w", encoding="utf-8").write("".join(f"{w}\t{n}\n" for w, n in sorted(v.items()) if n >= 2))
    vocab = collections.Counter()
    for line in open(words, encoding="utf-8"):
        w, n = line.rstrip("\n").split("\t"); vocab[w] = int(n)
    rows, hold, used = corrections(), held(), collections.Counter()
    text = open(scan, encoding="utf-8").read()
    for wrong, right in rows:
        if wrong.startswith("~"):  # put right in the scan itself, where a heading was lost: \n is a new line
            w, r = wrong[1:].replace("\\n", "\n"), right.replace("\\n", "\n")
            used[wrong] += text.count(w)
            text = text.replace(w, r)
    es = entries(paragraphs(clean_lines(text), vocab))
    lines = [
        "# Short Accounts of Certain Feasts and Lives of Saints Found in the Proper of Saints of This Missal",
        "# From: The New Roman Missal, Rev. F. X. Lasance and Rev. F. A. Walsh, O.S.B. (Benziger Brothers, 1937), pp. 1675-1753.",
        "# Public domain. Made by tools/lives/extract.py from the scan text at archive.org/details/fr-lasance-missal-1937,",
        "# with the misread words listed in corrections.tsv put right. Do not edit by hand: fix corrections.tsv and run again.",
        "#",
        "# Format: \"@ MM-DD | Title as printed\" begins an entry (\"@!\" marks one held back, see hold.txt).",
        "",
    ]
    for e in es:
        text = "\n".join(tidy(p) for p in e["paras"] if p.strip())
        head = e["title"]
        key = f"{e['date']} {head}"
        for wrong, right in rows:
            if wrong.startswith("~"):
                continue
            if wrong.startswith("@"):  # a correction to a heading: "@old heading<TAB>new heading" or "@MM-DD old<TAB>MM-DD new"
                if wrong[1:] == key:
                    used[wrong] += 1
                    e["date"], head = right.split(" ", 1)
                elif wrong[1:] == head:
                    used[wrong] += 1
                    head = right
                continue
            n = text.count(wrong)
            if n:
                used[wrong] += n
                text = text.replace(wrong, right)
        e["title"], e["text"] = head, text
        mark = "@!" if f"{e['date']} {head}" in hold or key in hold else "@"
        lines += [f"{mark} {e['date']} | {head}", *text.split("\n"), ""]
    open(os.path.join(HERE, "lasance-accounts.txt"), "w", encoding="utf-8").write("\n".join(lines))
    print(f"{len(es)} entries")
    unused = [w for w, _ in rows if not used[w]]
    if unused:
        print("corrections that matched nothing:", unused)
    return es


if __name__ == "__main__":
    main()
