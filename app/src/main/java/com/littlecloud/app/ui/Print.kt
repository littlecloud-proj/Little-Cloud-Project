package com.littlecloud.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.littlecloud.app.data.AppState
import com.littlecloud.app.data.PrayerBook
import com.littlecloud.app.data.PrayerItem
import com.littlecloud.app.data.SpanishPrayers
import java.time.format.DateTimeFormatter

/** Prints the morning or evening rule of prayer as a small booklet, through Android's print service. */
object PrayerPrinter {
    // Held until printing begins, so the WebView is not collected while it lays out the page.
    @SuppressLint("StaticFieldLeak")
    private var pending: WebView? = null

    fun print(ctx: Context, state: AppState, evening: Boolean) {
        val title = if (evening) "Evening Prayer" else "Morning Prayer"
        val html = html(state, title, if (evening) state.evening else state.morning)
        val web = WebView(ctx)
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val pm = ctx.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val job = "Little Cloud · $title"
                pm.print(
                    job,
                    view.createPrintDocumentAdapter(job),
                    PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.NA_LETTER).build(),
                )
                pending = null
            }
        }
        pending = web
        web.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
    }

    private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private fun para(s: String, cls: String) =
        s.trim().split(Regex("\n\\s*\n")).joinToString("") { "<p class=\"$cls\">" + esc(it).replace("\n", "<br>") + "</p>" }

    private fun body(state: AppState, item: PrayerItem): String {
        val p = state.prayer(item.prayerId) ?: return item.text?.let { para(it, "v") } ?: "<p class=\"note\">Said from memory.</p>"
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
        return """<!doctype html><html><head><meta charset="utf-8"><style>
@font-face { font-family: Forum; src: url('fonts/Forum-Regular.ttf'); }
@page { margin: 18mm 16mm; }
body { font-family: Forum, Georgia, serif; color: #2b2622; font-size: 12.5pt; line-height: 1.5; }
header { text-align: center; border-bottom: 1px solid #c9bfb3; padding-bottom: 10pt; margin-bottom: 14pt; }
header h1 { font-weight: normal; font-size: 26pt; margin: 0; letter-spacing: .02em; }
header .d { color: #7a6f64; font-size: 11pt; letter-spacing: .12em; text-transform: uppercase; }
section { break-inside: avoid-page; margin: 0 0 14pt; }
h2 { font-weight: normal; font-size: 16pt; margin: 0 0 2pt; }
.sub { color: #7a6f64; font-size: 10.5pt; margin-bottom: 6pt; }
p { margin: 0 0 7pt; }
p.la { font-style: normal; }
.cols { display: flex; gap: 14pt; } .cols > div { flex: 1; } .cols p.v { color: #4a423b; }
.note { color: #7a6f64; font-style: italic; }
footer { text-align: center; color: #9a8f84; font-size: 10pt; margin-top: 18pt; }
</style></head><body>
<header><div class="d">Little Cloud · $date</div><h1>${esc(title)}</h1></header>
$sections
<footer>Ecce nubecula parva quasi vestigium hominis</footer>
</body></html>"""
    }
}
