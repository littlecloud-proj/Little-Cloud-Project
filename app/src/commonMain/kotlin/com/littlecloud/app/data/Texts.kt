package com.littlecloud.app.data

import com.littlecloud.app.platform.readDataFile
import com.littlecloud.app.time.*
import kotlin.concurrent.Volatile
import kotlinx.serialization.json.*

/**
 * The Little Office of the Blessed Virgin Mary, by the rubrics chosen, rendered from the
 * Divinum Officium Project in Latin and English. Stored per seasonal form and hour
 * in assets/office.json.
 */
object LittleOffice {
    data class Block(val headLa: String?, val headEn: String?, val la: List<String>, val en: List<String>)

    @Volatile private var office1954: JsonObject? = null
    @Volatile private var office1955: JsonObject? = null

    /** office1955.json follows the rubrics of 1955, office1954.json those before. */
    private fun load(rubrics: String): JsonObject = when (rubrics) {
        "1954" -> office1954 ?: parse("office1954.json").also { office1954 = it }
        "1955" -> office1955 ?: parse("office1955.json").also { office1955 = it }
        // The 1962 option is set aside (its office.json is not bundled); anything else reads 1955.
        else -> office1955 ?: parse("office1955.json").also { office1955 = it }
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

    private fun resolve(root: JsonObject, k: String): String =
        // Christmastide falling in Septuagesima uses the Christmas form with the Lenten ending.
        if (k in root) k else k.replace("nat|norm", "nat|sept").replace("nat|pasch", "nat|sept")

    private fun parseBlocks(arr: JsonArray): List<Block> = arr.map { e ->
        val o = e.jsonObject
        Block(
            o.str("hl")?.takeIf { it.isNotEmpty() && it != "null" },
            o.str("he")?.takeIf { it.isNotEmpty() && it != "null" },
            o.getValue("la").jsonArray.strings(),
            o.getValue("en").jsonArray.strings(),
        )
    }

    /**
     * The hour as said on [d]. Vespers and Compline already belong to the morrow when a new form
     * begins then: the Saturday before Advent, Christmas Eve, the eve of the Purification, the Saturday
     * before Septuagesima, Holy Saturday and the Saturday before Trinity.
     */
    fun blocks(d: LocalDate, hour: String, rubrics: String): List<Block> {
        val root = load(rubrics)
        val evening = hour == "vespers" || hour == "compline"
        val formDate = if (evening && key(d.plusDays(1), hour) != key(d, hour)) d.plusDays(1) else d
        val arr = root[resolve(root, key(formDate, hour))] as? JsonArray ?: return emptyList()
        val blocks = parseBlocks(arr)
        if (hour != "compline") return blocks
        // The final antiphon of Our Lady follows its own dates, which do not match the seasonal forms.
        val source = root[finalAntiphonKey(d)] as? JsonArray ?: return blocks
        val wanted = parseBlocks(source).firstOrNull { it.headLa?.startsWith("Antiphona finalis") == true } ?: return blocks
        return blocks.map { if (it.headLa?.startsWith("Antiphona finalis") == true) wanted else it }
    }

    /**
     * Where to take the final antiphon said at Compline on [d]: the Alma Redemptoris from the Saturday
     * before Advent to February 1, the Ave Regina caelorum from February 2 to Holy Week, the Regina caeli
     * from Holy Saturday to the Friday after Pentecost, and the Salve Regina from Trinity to Advent.
     */
    private fun finalAntiphonKey(d: LocalDate): String {
        val adv = Calendar1962.adventStart(d.year)
        val e = ChronoUnit.DAYS.between(Calendar1962.easter(d.year), d).toInt()
        return when {
            !d.isBefore(adv.minusDays(1)) && d.isBefore(LocalDate.of(d.year, 12, 25)) -> "adv|norm|compline"
            !d.isBefore(LocalDate.of(d.year, 12, 25)) || d.monthValue == 1 || (d.monthValue == 2 && d.dayOfMonth == 1) -> "nat|norm|compline"
            e in -1..54 -> "year|pasch|compline"
            e < -1 -> "year|sept|compline"
            else -> "year|norm|compline"
        }
    }}

/** The collect of each day, 2026–2050, rendered from the Divinum Officium Project. */
object Collects {
    data class Collect(val la: String, val en: String)

    @Volatile private var data: JsonObject? = null

    private fun load(): JsonObject = data ?: parse("collects.json").also { data = it }

    fun forDate(d: LocalDate): Collect? {
        // collects.json (1962) is not bundled while that option is set aside.
        val root = runCatching { load() }.getOrNull() ?: return null
        val idx = root.getValue("years").jsonObject[d.year.toString()] as? JsonArray ?: return null
        val i = idx.getOrNull(d.dayOfYear - 1)?.jsonPrimitive?.intOrNull ?: -1
        if (i < 0) return null
        val c = root.getValue("texts").jsonArray[i].jsonArray
        return Collect(c[0].jsonPrimitive.content, c[1].jsonPrimitive.content)
    }
}

internal fun JsonArray.strings(): List<String> = map { it.jsonPrimitive.content }

/** Parses a bundled data file. */
internal fun parse(name: String): JsonObject = Json.parseToJsonElement(readDataFile(name)).jsonObject
