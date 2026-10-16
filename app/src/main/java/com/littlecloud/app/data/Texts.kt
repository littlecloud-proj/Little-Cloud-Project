package com.littlecloud.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The Little Office of the Blessed Virgin Mary (rubrics of 1960), rendered from the
 * Divinum Officium Project in Latin and English. Stored per seasonal form and hour
 * in assets/office.json.
 */
object LittleOffice {
    data class Block(val headLa: String?, val headEn: String?, val la: List<String>, val en: List<String>)

    private val cache = mutableMapOf<String, JSONObject>()

    /** office.json follows the rubrics of 1960; office1954.json those before 1955. */
    private fun load(ctx: Context, pre1955: Boolean): JSONObject = synchronized(this) {
        val file = if (pre1955) "office1954.json" else "office.json"
        cache.getOrPut(file) { JSONObject(ctx.assets.open(file).bufferedReader(Charsets.UTF_8).readText()) }
    }

    fun key(d: LocalDate, hour: String): String {
        val adv = Calendar1962.adventStart(d.year)
        val season = when {
            !d.isBefore(adv) && d.isBefore(LocalDate.of(d.year, 12, 25)) -> "adv"
            (d.monthValue == 12 && d.dayOfMonth >= 25) || d.monthValue == 1 ||
                (d.monthValue == 2 && d.dayOfMonth == 1) -> "nat"
            else -> "year"
        }
        val e = ChronoUnit.DAYS.between(Calendar1962.easter(d.year), d).toInt()
        val tempus = when {
            e in 0..55 -> "pasch"
            e in -63..-1 -> "sept"
            else -> "norm"
        }
        if (hour != "matins") return "$season|$tempus|$hour"
        val group = when (d.dayOfWeek) {
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.THURSDAY -> "A"
            DayOfWeek.TUESDAY, DayOfWeek.FRIDAY -> "B"
            else -> "C"
        }
        return "$season|$tempus|matins|$group"
    }

    fun blocks(ctx: Context, d: LocalDate, hour: String, pre1955: Boolean = false): List<Block> {
        val root = load(ctx, pre1955)
        var k = key(d, hour)
        // Christmastide falling in Septuagesima uses the Christmas form with the Lenten ending.
        if (!root.has(k)) k = k.replace("nat|norm", "nat|sept").replace("nat|pasch", "nat|sept")
        val arr = root.optJSONArray(k) ?: return emptyList()
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Block(
                o.optString("hl").takeIf { it.isNotEmpty() && it != "null" },
                o.optString("he").takeIf { it.isNotEmpty() && it != "null" },
                o.getJSONArray("la").strings(),
                o.getJSONArray("en").strings(),
            )
        }
    }
}

/** The collect of each day, 2026–2050, rendered from the Divinum Officium Project. */
object Collects {
    data class Collect(val la: String, val en: String)

    @Volatile private var data: JSONObject? = null

    private fun load(ctx: Context): JSONObject =
        data ?: synchronized(this) {
            data ?: JSONObject(ctx.assets.open("collects.json").bufferedReader(Charsets.UTF_8).readText()).also { data = it }
        }

    fun forDate(ctx: Context, d: LocalDate): Collect? {
        val root = load(ctx)
        val years = root.getJSONObject("years")
        val idx = years.optJSONArray(d.year.toString()) ?: return null
        val i = idx.optInt(d.dayOfYear - 1, -1)
        if (i < 0) return null
        val c = root.getJSONArray("texts").getJSONArray(i)
        return Collect(c.getString(0), c.getString(1))
    }
}

private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
