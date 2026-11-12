package com.littlecloud.app.data

import kotlin.concurrent.Volatile
import kotlinx.serialization.json.*

/**
 * Short accounts of the feasts and saints of the day, from the "Short Accounts of Certain Feasts
 * and Lives of Saints" at the back of Father Lasance's New Roman Missal (Benziger Brothers, 1937).
 *
 * lives.json is built by tools/lives/build.py. It ties each account to the calendar titles it
 * belongs to, so an account is found by the title of the day or of a commemoration, wherever the
 * feast falls that year. Days with no account simply show nothing.
 */
object Lives {
    data class Life(val title: String, val text: String)

    @Volatile private var data: JsonObject? = null

    private fun load(): JsonObject = data ?: parse("lives.json").also { data = it }

    /** The accounts for a day: the feast first, then its commemorations. Empty when there are none. */
    fun forDay(day: LiturgicalDay): List<Life> = runCatching {
        val root = load()
        val titles = root.getValue("titles").jsonObject
        val lives = root.getValue("lives").jsonArray
        (listOf(day.title) + day.commemorations)
            .flatMap { t -> (titles[t] as? JsonArray)?.map { it.jsonPrimitive.int } ?: emptyList() }
            .distinct()
            .map { i -> lives[i].jsonArray.let { Life(it[0].jsonPrimitive.content, it[1].jsonPrimitive.content) } }
    }.getOrDefault(emptyList())
}
