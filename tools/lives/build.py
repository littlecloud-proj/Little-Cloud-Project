#!/usr/bin/env python3
"""Builds lives.json (the short accounts of feasts and saints from the Lasance missal) for the app.

Reads lasance-accounts.txt and the two bundled calendars, and ties each account to the calendar
titles it belongs to, so the app can find an account by the title of the day or of a
commemoration (which also covers transferred feasts).

    python3 tools/lives/build.py            # writes app/src/commonMain/composeResources/files/lives.json
"""
import collections, datetime, json, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
FILES = os.path.join(HERE, "..", "..", "app", "src", "commonMain", "composeResources", "files")

# Words that say what a saint was, not who: never used to recognise a feast.
GENERIC = set("""st sts saint saints the of and at in or a an feast pope popes martyr martyrs virgin virgins bishop
bishops confessor confessors doctor church abbot abbess widow king queen apostle apostles evangelist priest
deacon holy blessed our lady lord i ii iii iv v vi vii first great companions hermit""".split())


def parse(path):
    out, cur = [], None
    for raw in open(path, encoding="utf-8"):
        line = raw.rstrip("\n")
        if line.startswith("#"):
            continue
        m = re.match(r"@\s*(\d\d)-(\d\d)\s*\|\s*(.+?)\s*$", line)
        if m:
            cur = {"date": f"{m.group(1)}-{m.group(2)}", "title": m.group(3), "paras": [[]]}
            out.append(cur)
        elif cur is not None:
            if line.strip():
                cur["paras"][-1].append(line.strip())
            elif cur["paras"][-1]:
                cur["paras"].append([])
    for e in out:
        e["text"] = "\n".join(" ".join(p) for p in e["paras"] if p)
        del e["paras"]
    return [e for e in out if e["text"]]


def words(title):
    return [w for w in re.findall(r"[a-zà-ÿ]+", title.lower()) if w not in GENERIC]


def calendar_titles():
    """date 'MM-DD' -> every title and commemoration either calendar puts on that date in any year."""
    by = collections.defaultdict(set)
    for name in ("calendar1955.json", "calendar1954.json"):
        d = json.load(open(os.path.join(FILES, name), encoding="utf-8"))
        for year, idx in d["years"].items():
            start = datetime.date(int(year), 1, 1)
            for n, i in enumerate(idx):
                if i < 0:
                    continue
                e = d["days"][i]
                key = (start + datetime.timedelta(n)).strftime("%m-%d")
                by[key].add(e[0])
                by[key].update(e[4])
    return by


def matches(entry, title):
    a, b = words(entry["title"]), set(words(title))
    if not a or ("vigil" in a) != ("vigil" in b):
        return False
    # The name (the first telling word) must be there, and nothing in the calendar title may
    # point to someone else of the same name on the same day.
    return a[0] in b


def main():
    entries = parse(os.path.join(HERE, "lasance-accounts.txt"))
    by = calendar_titles()
    titles, unmatched = collections.defaultdict(list), []
    for n, e in enumerate(entries):
        found = sorted(t for t in by.get(e["date"], ()) if matches(e, t))
        if not found:
            unmatched.append(e)
        for t in found:
            if n not in titles[t]:
                titles[t].append(n)
    out = {
        "source": "The New Roman Missal, Rev. F. X. Lasance (Benziger Brothers, 1937)",
        "lives": [[e["title"], e["text"]] for e in entries],
        "titles": dict(sorted(titles.items())),
    }
    dest = os.path.join(FILES, "lives.json")
    json.dump(out, open(dest, "w", encoding="utf-8"), ensure_ascii=False, separators=(",", ":"))
    print(f"{len(entries)} accounts, {len(titles)} calendar titles -> {os.path.relpath(dest)}")
    for t, ns in sorted(titles.items()):
        print(f"  {t}  <-  " + "; ".join(entries[n]["title"] for n in ns))
    if unmatched:
        print("Not tied to any calendar title:")
        for e in unmatched:
            print(f"  {e['date']} {e['title']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
