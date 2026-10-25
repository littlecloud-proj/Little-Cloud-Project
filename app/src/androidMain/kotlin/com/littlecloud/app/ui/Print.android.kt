package com.littlecloud.app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.littlecloud.app.data.AppState

/** Prints an HTML page through Android's print service, which also offers "Save as PDF". */
object PrayerPrinter {
    // Held until printing begins, so the WebView is not collected while it lays out the page.
    @SuppressLint("StaticFieldLeak")
    private var pending: WebView? = null

    fun print(ctx: Context, title: String, html: String) {
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

}
