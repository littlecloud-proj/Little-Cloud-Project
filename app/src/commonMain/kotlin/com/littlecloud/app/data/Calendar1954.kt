package com.littlecloud.app.data

import com.littlecloud.app.time.*
import kotlin.concurrent.Volatile
import kotlinx.serialization.json.*

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

    @Volatile private var data: JsonObject? = null

    private fun load(): JsonObject = data ?: parse("calendar1954.json").also { data = it }

    private fun entry(d: LocalDate): JsonArray? {
        if (d.year !in FIRST_YEAR..LAST_YEAR) return null
        val root = load()
        val idx = root.getValue("years").jsonObject[d.year.toString()] as? JsonArray ?: return null
        val i = idx.getOrNull(d.dayOfYear - 1)?.jsonPrimitive?.intOrNull ?: -1
        if (i < 0) return null
        return root.getValue("days").jsonArray[i].jsonArray
    }

    private fun JsonArray.s(i: Int) = this[i].jsonPrimitive.content

    fun day(d: LocalDate, discipline: Discipline, carmelite: Boolean): LiturgicalDay? {
        val e = runCatching { entry(d) }.getOrNull() ?: return null
        // [title, rank, class, colour, [commemorations], collect index]
        val comms = e[4].jsonArray.strings()
        return LiturgicalDay(
            date = d,
            title = e.s(0),
            cls = e[2].jsonPrimitive.int,
            color = runCatching { LColor.valueOf(e.s(3)) }.getOrDefault(LColor.WHITE),
            commemorations = comms,
            carmelite = if (carmelite) Calendar1962.carmeliteFor(d) else null,
            season = Calendar1962.seasonOf(d),
            fast = Calendar1962.fasting(d, discipline, pre1955 = true),
            rankText = e.s(1),
        )
    }

    fun collect(d: LocalDate): Collects.Collect? {
        val e = runCatching { entry(d) }.getOrNull() ?: return null
        val i = e.getOrNull(5)?.jsonPrimitive?.intOrNull ?: -1
        if (i < 0) return null
        val c = load().getValue("texts").jsonArray[i].jsonArray
        return Collects.Collect(c.s(0), c.s(1))
    }
}
