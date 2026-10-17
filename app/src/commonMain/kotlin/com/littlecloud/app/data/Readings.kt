package com.littlecloud.app.data

import com.littlecloud.app.time.*
import kotlin.concurrent.Volatile
import kotlinx.serialization.json.*

/**
 * The Epistle and Gospel of the Mass of each day, 2026–2050, under the rubrics before 1955
 * and those of 1962. Latin from the Divinum Officium Project; English from the Douay-Rheims
 * Bible (Challoner), taken by the citation, with the liturgical opening ("At that time").
 * Stored in composeResources/files/readings.json.
 */
object Readings {
    data class Reading(
        /** True for the Gospel, false for the Epistle or lesson. */
        val gospel: Boolean,
        val titleLa: String,
        val citation: String,
        val la: String,
        val titleEn: String,
        /** Null when no English is on file for this citation. */
        val en: String?,
    )

    @Volatile private var data: JsonObject? = null
    private fun load(): JsonObject = data ?: parse("readings.json").also { data = it }

    /** The day's Epistle and Gospel, in that order. Empty outside 2026–2050. */
    fun forDate(d: LocalDate, pre1955: Boolean): List<Reading> {
        val root = load()
        val year = root.getValue("days").jsonObject[if (pre1955) "1954" else "1962"]
            ?.jsonObject?.get(d.year.toString()) as? JsonArray ?: return emptyList()
        val ids = year.getOrNull(d.dayOfYear - 1) as? JsonArray ?: return emptyList()
        val texts = root.getValue("texts").jsonArray
        return ids.mapNotNull { id ->
            val t = texts.getOrNull(id.jsonPrimitive.int)?.jsonArray ?: return@mapNotNull null
            fun s(i: Int) = t[i].jsonPrimitive.content
            Reading(s(0) == "E", s(1), s(2), s(3), s(4), s(5).ifEmpty { null })
        }
    }
}
