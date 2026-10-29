package com.littlecloud.app.ui

import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.LittleOffice
import com.littlecloud.app.data.PrayerItem
import com.littlecloud.app.time.*

/** Printable pages: the morning or evening rule, the day's readings, and an hour of the Office. */
object PrayerBooklet {
    fun title(evening: Boolean) = if (evening) "Evening Prayer" else "Morning Prayer"

    fun html(state: AppState, evening: Boolean): String =
        html(state, title(evening), if (evening) state.evening else state.morning)

    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private fun para(s: String, cls: String) =
        s.trim().split(Regex("\n\\s*\n")).joinToString("") { "<p class=\"$cls\">" + esc(it).replace("\n", "<br>") + "</p>" }

    private fun body(state: AppState, item: PrayerItem): String {
        val p = state.prayer(item.prayerId) ?: return item.text?.let { para(it, "v") } ?: "<p class=\"note\">Said from memory.</p>"
        if (p.latin == null && p.english.isBlank()) return "<p class=\"note\">Said from memory.</p>"
        val es = state.vernacular == "es"
        val vern = if (es) state.spanish(p.id) ?: (if (p.latin == null) p.english else null) else p.english
        val lang = state.officeLang
        val showLatin = p.latin != null && (lang != "english" || vern == null)
        val showVern = vern != null && (lang != "latin" || p.latin == null)
        return buildString {
            if (showLatin && showVern) {
                append("<div class=\"cols\"><div>"); append(para(p.latin!!, "la")); append("</div><div>")
                append(para(vern!!, "v")); append("</div></div>")
            } else {
                if (showLatin) append(para(p.latin!!, "la"))
                if (showVern) append(para(vern!!, "v"))
            }
        }
    }

    private fun html(state: AppState, title: String, items: List<PrayerItem>): String {
        val date = state.today.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        val sections = items.joinToString("") { item ->
            val p = state.prayer(item.prayerId)
            "<section><h2>${esc(p?.title ?: item.title)}</h2>" +
                (p?.subtitle ?: item.note).takeIf { it.isNotBlank() }?.let { "<div class=\"sub\">${esc(it)}</div>" }.orEmpty() +
                body(state, item) + "</section>"
        }
        return page(title, date, sections, liturgical = false)
    }

    /** The day's collect, Epistle and Gospel, in the language chosen. */
    fun readingsHtml(state: AppState, date: LocalDate): String {
        val lang = state.officeLang
        val day = state.dayInfo(date)
        fun two(la: String, en: String?): String {
            val showLatin = lang != "english" || en == null
            val showEn = en != null && lang != "latin"
            return if (showLatin && showEn) "<div class=\"cols\"><div>${para(la, "la")}</div><div>${para(en!!, "v")}</div></div>"
            else (if (showLatin) para(la, "la") else "") + (if (showEn) para(en!!, "v") else "")
        }
        val sections = buildString {
            state.collect(date)?.let { append("<section><h2>Collect</h2>"); append(two(it.la, it.en)); append("</section>") }
            state.readings(date).forEach { r ->
                append("<section><h2>${if (r.gospel) "Gospel" else "Epistle"}</h2><div class=\"sub\">${esc(r.citation)}</div>")
                append(two(r.la, r.en)); append("</section>")
            }
        }
        return page(day.title, date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")) + " · " + day.rankLabel, sections)
    }

    /** An hour of the Little Office, rubrics in red italics, in the language chosen. */
    fun officeHtml(state: AppState, date: LocalDate, hourId: String, hourName: String): String {
        val lang = state.officeLang
        fun line(raw: String): String {
            if (raw == "_") return "<br>"
            val vm = verseNum.find(raw)
            var t = vm?.groupValues?.get(2) ?: raw
            var lead = ""
            leadingRubric.find(t)?.let { lead = "<span class=\"r\">${esc(it.groupValues[1].trim())}</span> "; t = t.substring(it.range.last + 1) }
            if (t.length < 110 && rubricLine.matches(t) && !t.startsWith("℣")) return "<p class=\"r\">${esc(t)}</p>"
            val body = esc(t).let { e -> inlineRubric.replace(e) { "<span class=\"r\">${it.value}</span>" } }
                .let { e -> crossMark.replace(e) { "<span class=\"x\">${it.value}</span>" } }
            val num = vm?.groupValues?.get(1)?.let { "<span class=\"n\">$it</span> " }.orEmpty()
            return "<p>$num$lead$body</p>"
        }
        val sections = LittleOffice.blocks(date, hourId, state.rubrics).joinToString("") { b ->
            val head = (if (lang == "english") b.headEn ?: b.headLa else b.headLa ?: b.headEn)?.let { "<h2>${esc(it)}</h2>" }.orEmpty()
            val la = b.la.joinToString("") { line(it) }
            val en = b.en.joinToString("") { line(it) }
            val text = when (lang) {
                "latin" -> la
                "english" -> en
                else -> "<div class=\"cols\"><div>$la</div><div class=\"v\">$en</div></div>"
            }
            "<section class=\"flow\">$head$text</section>"
        }
        return page(hourName, date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")) + " · The Little Office of the Blessed Virgin Mary", sections)
    }

    private fun page(title: String, top: String, sections: String, liturgical: Boolean = true): String =
        """<!doctype html><html><head><meta charset="utf-8"><style>
@font-face { font-family: Forum; src: url('fonts/Forum-Regular.ttf'); }
@page { margin: 18mm 16mm; }
body { font-family: Forum, Georgia, serif; color: #2b2622; font-size: 12.5pt; line-height: 1.5; }
header { text-align: center; border-bottom: 1px solid #c9bfb3; padding-bottom: 10pt; margin-bottom: 14pt; }
header h1 { font-weight: normal; font-size: 26pt; margin: 0; letter-spacing: .02em; }
header .d { color: #7a6f64; font-size: 11pt; letter-spacing: .12em; text-transform: uppercase; }
section { break-inside: avoid-page; margin: 0 0 14pt; }
section.flow { break-inside: auto; }
h2 { font-weight: normal; font-size: 16pt; margin: 0 0 2pt; }
.sub { color: #7a6f64; font-size: 10.5pt; margin-bottom: 6pt; }
p { margin: 0 0 7pt; }
.cols { display: flex; gap: 14pt; } .cols > div { flex: 1; } .cols p.v, .cols .v p { color: #4a423b; }
.note { color: #7a6f64; font-style: italic; }
.r { color: #9b2d20; font-style: italic; font-size: 10.5pt; }
.n { color: #9a8f84; font-size: 9pt; }
.x { color: #9b2d20; }
.disc { color: #9a8f84; font-size: 9.5pt; text-align: center; margin-top: 10pt; }
footer { text-align: center; color: #9a8f84; font-size: 10pt; margin-top: 18pt; }
</style></head><body>
<header><div class="d">${esc(top)}</div><h1>${esc(title)}</h1></header>
$sections
<footer>Little Cloud · Ecce nubecula parva quasi vestigium hominis</footer>
${if (liturgical) "<p class=\"disc\">${esc(LITURGY_NOTE)}</p>" else ""}
</body></html>"""
}
