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
