# Data tools

Scripts used to generate the bundled data. They are not part of the app build.

- `calendar/` – harvests the calendar and collects from a local Divinum Officium install and builds
  `calendar1955.json` (version "Reduced - 1955") and `calendar1954.json` ("Divino Afflatu - 1954").
  Set `V` in `harvest.py` to the version wanted.
- `office/` – renders the Little Office of Our Lady (votive C12) for each season and hour and writes
  `office1955.json`, `office1954.json` and `office.json` (1960). `gen.py` parses one hour; the dates
  used for each season are chosen in `derive_dates.py`.
  Paths inside the scripts point at the original working folders (/tmp/da, /tmp/do); adjust before rerunning.
- `prayers/` – builds `MorePrayers.kt` and related prayer texts from the public-domain
  sources in `prayers/sources/` (Hammer 1909, Gahan, Lyra Liturgica, Carmelite manual 1850, Raccolta 1880).
- `lives/` – builds `lives.json`, the short accounts of feasts and saints from the Lasance missal (1937).
  `extract.py` makes `lasance-accounts.txt` from the scan section in `sources/`, applying `corrections.tsv`
  and holding back the entries in `hold.txt`; `build.py` ties each account to the calendar titles
  (`aliases.tsv` where the names differ). `REVIEW.md` says what is still to be proofread.

## Note on the 1955 harvest

Divinum Officium's "Reduced - 1955" version borrows the 1960 tempora table, which turns the Seven Sorrows
on the Friday of Passion Week into a commemoration. Under the 1955 rubrics it is still a greater double, so
before harvesting, the line `Tempora/Quad5-5=Tempora/Quad5-5Feria;;1960 Newcal` in
`web/www/Tabulae/Tempora/Generale.txt` is changed to apply to `Newcal` only. The Mass of that day takes the
readings of 15 September (Judith 13, John 19).
