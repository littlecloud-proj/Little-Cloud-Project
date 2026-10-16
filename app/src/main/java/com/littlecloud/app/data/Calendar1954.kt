package com.littlecloud.app.data

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate

/**
 * The traditional Roman calendar under the rubrics in force before 1955 (Divino Afflatu,
 * as of 1954), as kept by CMRI and other communities using the older books.
 *
 * Every day from 2026 to 2050 was rendered from the Divinum Officium Project and is stored
 * in assets/calendar1954.json: the office of the day, its rank and colour, the
 * commemorations at Lauds, and the collect.
 */
object Calendar1954 {
    const val FIRST_YEAR = 2026
    const val LAST_YEAR = 2050

    @Volatile private var data: JSONObject? = null

    private fun load(ctx: Context): JSONObject =
        data ?: synchronized(this) {
            data ?: JSONObject(ctx.assets.open("calendar1954.json").bufferedReader(Charsets.UTF_8).readText()).also { data = it }
        }

    private fun entry(ctx: Context, d: LocalDate): org.json.JSONArray? {
        if (d.year !in FIRST_YEAR..LAST_YEAR) return null
        val root = load(ctx)
        val idx = root.getJSONObject("years").optJSONArray(d.year.toString()) ?: return null
        val i = idx.optInt(d.dayOfYear - 1, -1)
        if (i < 0) return null
        return root.getJSONArray("days").getJSONArray(i)
    }

    fun day(ctx: Context, d: LocalDate, discipline: Discipline, carmelite: Boolean): LiturgicalDay? {
        val e = runCatching { entry(ctx, d) }.getOrNull() ?: return null
        // [title, rank, class, colour, [commemorations], collect index]
        val comms = e.getJSONArray(4).let { a -> (0 until a.length()).map { a.getString(it) } }
        return LiturgicalDay(
            date = d,
            title = e.getString(0),
            cls = e.getInt(2),
            color = runCatching { LColor.valueOf(e.getString(3)) }.getOrDefault(LColor.WHITE),
            commemorations = comms,
            carmelite = if (carmelite) Calendar1962.carmeliteFor(d) else null,
            season = Calendar1962.seasonOf(d),
            fast = Calendar1962.fasting(d, discipline, pre1955 = true),
            rankText = e.getString(1),
        )
    }

    fun collect(ctx: Context, d: LocalDate): Collects.Collect? {
        val e = runCatching { entry(ctx, d) }.getOrNull() ?: return null
        val i = e.optInt(5, -1)
        if (i < 0) return null
        val c = load(ctx).getJSONArray("texts").getJSONArray(i)
        return Collects.Collect(c.getString(0), c.getString(1))
    }
}
