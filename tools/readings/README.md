# Readings

How `composeResources/files/readings.json` (the Epistle and Gospel of every day, 2026–2050) is made:

1. `harvest.py 2026 2050 all.jsonl` renders the Mass of each day from a local Divinum Officium
   install (`missa.pl`), under "Reduced - 1955", "Divino Afflatu - 1954" and "Rubrics 1960 - 1960" (`VERSIONS=1955` limits it to one), and keeps the Latin
   lessons and Gospel with their citations. Holy Thursday takes the Mass of the Lord's Supper,
   Christmas the Mass of the Day.
2. `fix.py` renders Holy Week again, along with any day missing a reading.
3. `parse_dr.py` turns the Douay-Rheims text from Project Gutenberg (#8300) into verses.
4. `build_readings.py all_fixed.jsonl` picks the Epistle (the last lesson before the Gospel) and
   the Gospel. It takes the English from the Douay-Rheims by each citation, adds the liturgical
   opening ("At that time", "Brethren"), and writes `readings.json`.
