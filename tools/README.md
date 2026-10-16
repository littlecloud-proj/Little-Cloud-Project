# Data tools

Scripts used to generate the bundled data. They are not part of the app build.

- `calendar/` – harvests the pre-1955 (Divino Afflatu) calendar, collects and Little Office
  texts from a local Divinum Officium install, and builds `calendar1954.json` / `office1954.json`.
  Paths inside the scripts point at the original working folders (/tmp/da, /tmp/do); adjust before rerunning.
- `prayers/` – builds `MorePrayers.kt` and related prayer texts from the public-domain
  sources in `prayers/sources/` (Hammer 1909, Gahan, Lyra Liturgica, Carmelite manual 1850, Raccolta 1880).
