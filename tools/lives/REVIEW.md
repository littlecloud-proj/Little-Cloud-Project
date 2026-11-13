# Saints' lives: what is still to be checked

`lasance-accounts.txt` is made by `extract.py` from an OCR scan. It has NOT yet been proofread
against the page images, and should be before release. Nothing in it is written or reworded by AI:
the script only joins lines, drops scan specks, and applies the misread words listed in
`corrections.tsv`. Where the scan lost words, the entry is held back (`hold.txt`) rather than guessed.

## Edition
The archive.org scan is labelled 1937 but is a later printing: the entry for the Immaculate Heart
of Mary (22 August) cites December 1942, and the book's appendix has feasts of the 1950s. The 1937
edition is in the public domain (HathiTrust mdp.39015005356921); anything added in the 1945 edition
may not be. Before release, compare the section with the 1937 copy and drop whatever is not in it.
The 22 August entry is held back for this reason.

## Numbers
- 330 entries in the section; 16 held back; 314 in the app.
- About 230 days of a year show an account (the feast's or a commemoration's).
- Not tied to the calendar: "Of the Octave of the Apostles" (4 July). Octave days and the vigil of
  the Immaculate Conception have no day of their own in the 1955 calendar.

## Known kinds of blemish still in the text
- Stray or wrong punctuation from the scan ("the: Mother", a quote mark opened and not closed).
- Words run together or wrongly hyphenated ("she-had", "then-asked").
- Wrong digits cannot be seen without the page: check every date and year.
- Names are left as scanned unless plainly misread (e.g. "Anthanasius", "Rodalsdorf", "Illucca").
- Headings that were corrected: St. Agatha (scan "Feb. 9", set to 5 Feb.), St. Gertrude (scan
  "Nov. 18", set to 16 Nov.).
- The Solemnity of St. Joseph (Wednesday after the 2nd Sunday after Easter) has run into the entry
  of St. Catharine of Siena, which is held back.

## How to proofread
Read `lasance-accounts.txt` beside the page images (pp. 1675 to 1753). For a misread word, add a
line to `corrections.tsv`; for a heading, a line beginning `@`. Then run `extract.py` and
`build.py`. When a held entry has been put right, take its line out of `hold.txt`.
