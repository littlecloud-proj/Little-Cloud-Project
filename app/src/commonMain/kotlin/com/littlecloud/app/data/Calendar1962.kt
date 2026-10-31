package com.littlecloud.app.data

import com.littlecloud.app.time.*

enum class LColor(val label: String, val argb: Long) {
    WHITE("White", 0xFFFFFFFF),
    RED("Red", 0xFFA33A2E),
    GREEN("Green", 0xFF4E7A57),
    VIOLET("Violet", 0xFF6B4C8A),
    ROSE("Rose", 0xFFC98A9A),
    BLACK("Black", 0xFF2B2B2B),
}

enum class Discipline(val label: String, val detail: String) {
    UNIVERSAL_1917(
        "Universal · Code of 1917",
        "Canon 1252 as in force in 1962"
    ),
    US_1951(
        "United States",
        "As kept by the American bishops from 1951, with the changes of 1956 and 1957"
    ),
}

enum class Abstinence { NONE, PARTIAL, FULL }

data class FastInfo(
    val fast: Boolean,
    val abstinence: Abstinence,
    val reason: String,
    val note: String,
    /**
     * Why a fast or abstinence someone might expect does not apply today (or was moved), on the few
     * days where that is so; empty otherwise.
     */
    val exemption: String = "",
) {
    val any: Boolean get() = fast || abstinence != Abstinence.NONE
    val headline: String
        get() = when {
            fast && abstinence == Abstinence.FULL -> "Fast and abstinence"
            fast && abstinence == Abstinence.PARTIAL -> "Fast and partial abstinence"
            fast -> "Fast"
            abstinence == Abstinence.FULL -> "Abstinence"
            abstinence == Abstinence.PARTIAL -> "Partial abstinence"
            else -> "No fast or abstinence"
        }
}

data class LiturgicalDay(
    val date: LocalDate,
    val title: String,
    val cls: Int,
    val color: LColor,
    val commemorations: List<String>,
    val carmelite: String?,
    val season: String,
    val fast: FastInfo,
    /** The rank as named by older rubrics ("Double of the II class"); null under 1962. */
    val rankText: String? = null,
) {
    val rankLabel: String get() = rankText ?: listOf("", "I class", "II class", "III class", "IV class")[cls]
}

/**
 * Traditional Roman calendar according to the rubrics of 1960/1962.
 *
 * The temporal cycle is computed in full. The sanctoral below covers the principal
 * feasts of the year and all of October; it is meant to be completed and checked
 * against a printed Ordo before relying on it.
 */
object Calendar1962 {

    private val W = LColor.WHITE
    private val R = LColor.RED
    private val G = LColor.GREEN
    private val V = LColor.VIOLET

    private fun md(m: Int, d: Int) = MonthDay.of(m, d)

    /**
     * The fixed-date entry for [d], allowing for leap years (the feasts of 24–28 February
     * move one day later) and for feasts that must be transferred.
     */
    private fun sanctoralFor(d: LocalDate): Feast? {
        val map = Sanctoral1962.map
        val e = daysFromEaster(d)
        // St. Joseph: on a Sunday, to Monday; in Holy Week, to the Saturday before Palm Sunday.
        val joseph = LocalDate.of(d.year, 3, 19)
        val josephE = daysFromEaster(joseph)
        val josephDay = when {
            josephE in -7..-1 -> easter(d.year).minusDays(8)
            joseph.dayOfWeek == DayOfWeek.SUNDAY -> joseph.plusDays(1)
            else -> joseph
        }
        // Annunciation: from Palm Sunday to Low Sunday, to the Monday after Low Sunday;
        // on another Sunday, to Monday.
        val ann = LocalDate.of(d.year, 3, 25)
        val annE = daysFromEaster(ann)
        val annDay = when {
            annE in -7..7 -> easter(d.year).plusDays(8)
            ann.dayOfWeek == DayOfWeek.SUNDAY -> ann.plusDays(1)
            else -> ann
        }
        // All Souls: on a Sunday, to Monday.
        val souls = LocalDate.of(d.year, 11, 2)
        val soulsDay = if (souls.dayOfWeek == DayOfWeek.SUNDAY) souls.plusDays(1) else souls
        if (d == josephDay) return map[md(3, 19)]
        if (d == annDay) return map[md(3, 25)]
        if (d == soulsDay) return map[md(11, 2)]
        val key = MonthDay.from(d)
        if (key == md(3, 19) || key == md(3, 25) || key == md(11, 2)) return null
        if (d.isLeapYear && d.monthValue == 2) {
            if (d.dayOfMonth == 24) return null
            if (d.dayOfMonth in 25..29) return map[md(2, d.dayOfMonth - 1)]
        }
        return map[key]
    }

    // ---------- movable dates ----------

    fun easter(y: Int): LocalDate {
        val a = y % 19
        val b = y / 100
        val c = y % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(y, month, day)
    }

    fun adventStart(y: Int): LocalDate =
        LocalDate.of(y, 11, 27).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    private fun christTheKing(y: Int): LocalDate =
        LocalDate.of(y, 10, 31).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    private fun daysFromEaster(d: LocalDate) = ChronoUnit.DAYS.between(easter(d.year), d).toInt()

    fun isTriduum(d: LocalDate) = daysFromEaster(d) in -3..-1
    fun isPaschaltide(d: LocalDate) = daysFromEaster(d) in 0..55

    fun isEmberDay(d: LocalDate): Boolean = emberName(d) != null

    private fun emberName(d: LocalDate): String? {
        val dow = d.dayOfWeek
        if (dow != DayOfWeek.WEDNESDAY && dow != DayOfWeek.FRIDAY && dow != DayOfWeek.SATURDAY) return null
        val day = dowName(dow)
        val e = daysFromEaster(d)
        if (e in listOf(-39, -37, -36)) return "Ember $day of Lent"
        if (e in listOf(52, 54, 55)) return "Ember $day in the Octave of Pentecost"
        // September: after the third Sunday of September (1960 rubrics)
        val thirdSun = LocalDate.of(d.year, 9, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusWeeks(2)
        val sep = ChronoUnit.DAYS.between(thirdSun, d).toInt()
        if (sep in listOf(3, 5, 6)) return "Ember $day in September"
        // Advent: after the third Sunday of Advent
        val gaudete = adventStart(d.year).plusWeeks(2)
        val adv = ChronoUnit.DAYS.between(gaudete, d).toInt()
        if (adv in listOf(3, 5, 6)) return "Ember $day of Advent"
        return null
    }

    private fun dowName(d: DayOfWeek) =
        d.name.lowercase().replaceFirstChar { it.uppercase() }

    private fun ord(n: Int): String {
        val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
            1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th"
        }
        return "$n$suffix"
    }

    // ---------- temporal cycle ----------

    private data class Temporal(val feast: Feast, val season: String)

    private fun temporal(d: LocalDate): Temporal {
        val y = d.year
        val e = daysFromEaster(d)
        val sun = d.dayOfWeek == DayOfWeek.SUNDAY
        val adv = adventStart(y)
        val m = d.monthValue
        val dom = d.dayOfMonth
        val ember = emberName(d)

        // Advent
        if (!d.isBefore(adv) && d.isBefore(LocalDate.of(y, 12, 24))) {
            val s = "Advent"
            if (sun) {
                val n = (ChronoUnit.DAYS.between(adv, d) / 7 + 1).toInt()
                return Temporal(Feast("${ord(n)} Sunday of Advent", 1, if (n == 3) LColor.ROSE else V), s)
            }
            if (ember != null) return Temporal(Feast(ember, 2, V), s)
            return Temporal(Feast("Feria of Advent", if (dom >= 17 && m == 12) 2 else 3, V), s)
        }
        if (m == 12 && dom == 24) return Temporal(Feast("Vigil of the Nativity", 1, V), "Advent")

        // Christmastide: 25 Dec – 13 Jan
        if (m == 12 && dom >= 25) {
            val s = "Christmastide"
            if (dom == 25) return Temporal(Feast("Nativity of Our Lord", 1, W), s)
            val sundayInOctave = (26..31).map { LocalDate.of(y, 12, it) }
                .firstOrNull { it.dayOfWeek == DayOfWeek.SUNDAY && it.dayOfMonth >= 29 }
                ?: LocalDate.of(y, 12, 30)
            if (d == sundayInOctave) return Temporal(Feast("Sunday within the Octave of the Nativity", 2, W), s)
            return Temporal(Feast("Day within the Octave of the Nativity", if (dom <= 28) 3 else 2, W), s)
        }
        if (m == 1 && dom <= 13) {
            val s = "Christmastide"
            if (dom == 1) return Temporal(Feast("Octave Day of the Nativity", 1, W), s)
            if (dom == 6) return Temporal(Feast("Epiphany of Our Lord", 1, W), s)
            val holyName = (2..5).map { LocalDate.of(y, 1, it) }
                .firstOrNull { it.dayOfWeek == DayOfWeek.SUNDAY } ?: LocalDate.of(y, 1, 2)
            if (d == holyName) return Temporal(Feast("Most Holy Name of Jesus", 2, W), s)
            if (sun && dom >= 7) return Temporal(Feast("Holy Family", 2, W), s)
            return Temporal(Feast("Feria of Christmastide", 4, W), s)
        }

        // Time after Epiphany, up to Septuagesima
        if (e < -63) {
            val s = "Time after Epiphany"
            if (sun) {
                val first = LocalDate.of(y, 1, 7).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val n = (ChronoUnit.DAYS.between(first, d) / 7 + 1).toInt()
                return Temporal(Feast("${ord(n)} Sunday after Epiphany", 2, G), s)
            }
            return Temporal(Feast("Feria", 4, G), s)
        }

        // Septuagesima
        if (e in -63..-47) {
            val s = "Septuagesima"
            if (sun) {
                val name = when (e) { -63 -> "Septuagesima Sunday"; -56 -> "Sexagesima Sunday"; else -> "Quinquagesima Sunday" }
                return Temporal(Feast(name, 2, V), s)
            }
            return Temporal(Feast("Feria", 4, V), s)
        }

        // Lent
        if (e in -46..-15) {
            val s = "Lent"
            if (e == -46) return Temporal(Feast("Ash Wednesday", 1, V), s)
            if (sun) {
                val n = (e + 49) / 7
                return Temporal(Feast("${ord(n)} Sunday of Lent", 1, if (n == 4) LColor.ROSE else V), s)
            }
            if (ember != null) return Temporal(Feast(ember, 2, V), s)
            return Temporal(Feast("Feria of Lent", 3, V), s)
        }

        // Passiontide and Holy Week
        if (e in -14..-1) {
            val s = if (e >= -7) "Holy Week" else "Passiontide"
            return Temporal(
                when (e) {
                    -14 -> Feast("1st Sunday of the Passion", 1, V)
                    -7 -> Feast("2nd Sunday of the Passion (Palm Sunday)", 1, V)
                    -6 -> Feast("Monday of Holy Week", 1, V)
                    -5 -> Feast("Tuesday of Holy Week", 1, V)
                    -4 -> Feast("Wednesday of Holy Week", 1, V)
                    -3 -> Feast("Holy Thursday", 1, W)
                    -2 -> Feast("Good Friday", 1, LColor.BLACK)
                    -1 -> Feast("Holy Saturday", 1, V)
                    else -> Feast("Feria of Passiontide", 3, V)
                }, s
            )
        }

        // Paschaltide
        if (e in 0..55) {
            val s = "Paschaltide"
            when (e) {
                0 -> return Temporal(Feast("Easter Sunday", 1, W), s)
                in 1..6 -> return Temporal(Feast("Easter ${dowName(d.dayOfWeek)}", 1, W), s)
                7 -> return Temporal(Feast("Low Sunday", 1, W), s)
                38 -> return Temporal(Feast("Vigil of the Ascension", 2, W), s)
                39 -> return Temporal(Feast("Ascension of Our Lord", 1, W), s)
                42 -> return Temporal(Feast("Sunday after the Ascension", 2, W), s)
                48 -> return Temporal(Feast("Vigil of Pentecost", 1, R), s)
                49 -> return Temporal(Feast("Pentecost Sunday", 1, R), s)
            }
            if (e in 50..55) {
                return Temporal(Feast(ember ?: "Pentecost ${dowName(d.dayOfWeek)}", 1, R), s)
            }
            if (sun) return Temporal(Feast("${ord(e / 7)} Sunday after Easter", 2, W), s)
            return Temporal(Feast("Feria of Paschaltide", 4, W), s)
        }

        // Time after Pentecost
        val s = "Time after Pentecost"
        when (e) {
            56 -> return Temporal(Feast("Most Holy Trinity", 1, W), s)
            60 -> return Temporal(Feast("Corpus Christi", 1, W), s)
            68 -> return Temporal(Feast("Most Sacred Heart of Jesus", 1, W), s)
        }
        if (d == christTheKing(y)) return Temporal(Feast("Our Lord Jesus Christ the King", 1, W), s)
        if (sun) {
            val n = (e - 49) / 7
            val name = if (!d.plusWeeks(1).isBefore(adv)) "Last Sunday after Pentecost" else "${ord(n)} Sunday after Pentecost"
            return Temporal(Feast(name, 2, G), s)
        }
        if (ember != null) return Temporal(Feast(ember, 2, V), s)
        return Temporal(Feast("Feria", 4, G), s)
    }

    // ---------- the day ----------

    fun day(d: LocalDate, discipline: Discipline, carmelite: Boolean): LiturgicalDay {
        val t = temporal(d)
        val tf = t.feast
        val sanc = sanctoralFor(d)
        val univ = sanc?.takeIf { it.cls > 0 }
        val sunday = d.dayOfWeek == DayOfWeek.SUNDAY
        val lent = t.season == "Lent" || t.season == "Passiontide" || t.season == "Holy Week"

        // Precedence (Codex Rubricarum 1960): the lower class number wins. On a tie:
        //  I   – the temporal day wins, except the Immaculate Conception over an Advent Sunday;
        //  II  – a feast beats a ferial day or vigil, but a Sunday beats a feast, unless it is a feast of the Lord;
        //  III – a feast beats an Advent feria, but a Lenten feria beats a feast.
        val sanctWins = when {
            univ == null -> false
            univ.cls < tf.cls -> true
            univ.cls > tf.cls -> false
            univ.cls == 1 -> MonthDay.from(d) == md(12, 8)
            univ.cls == 2 -> !sunday || MonthDay.from(d) in Sanctoral1962.feastsOfTheLord
            univ.cls == 3 -> !lent
            else -> false
        }
        val winner = if (sanctWins) univ!! else tf

        // Commemorations
        val comms = mutableListOf<String>()
        if (winner.cls == 1) {
            if (sanctWins && sunday) comms += tf.title
        } else {
            if (sanctWins) {
                if (tf.cls <= 3) comms += tf.title
            } else if (univ != null) {
                // On a Sunday of the II class only a feast of the II class is commemorated.
                if (!(sunday && univ.cls == 3)) comms += univ.title
            }
            if (!(sunday && !sanctWins)) sanc?.comm?.let { comms += it }
        }

        var title = winner.title
        var color = winner.color
        // Saturday Office of Our Lady, outside Advent, Lent and Passiontide
        val freeSeason = t.season in setOf("Christmastide", "Time after Epiphany", "Septuagesima", "Paschaltide", "Time after Pentecost")
        if (d.dayOfWeek == DayOfWeek.SATURDAY && !sanctWins && winner.cls == 4 && freeSeason) {
            title = "Saturday of Our Lady"
            color = W
        }

        val carm = if (carmelite) sanc?.carm else null
        return LiturgicalDay(
            date = d, title = title, cls = winner.cls, color = color,
            commemorations = comms, carmelite = carm, season = t.season,
            fast = fasting(d, discipline),
        )
    }

    /** The liturgical season of a day ("Advent", "Lent", "Time after Pentecost" …). */
    fun seasonOf(d: LocalDate): String = temporal(d).season

    /** The Carmelite proper for this date, if any. */
    fun carmeliteFor(d: LocalDate): String? = sanctoralFor(d)?.carm

    /** For tests: "S" when a fixed-date feast is kept, "T" for the temporal day, "B" for Our Lady on Saturday. */
    internal fun source(d: LocalDate): String {
        val day = day(d, Discipline.UNIVERSAL_1917, false)
        val t = temporal(d).feast
        return when {
            day.title == "Saturday of Our Lady" -> "B"
            day.title == t.title -> "T"
            else -> "S"
        } + day.cls
    }

    // ---------- Little Office of Our Lady ----------

    fun littleOfficeSeason(d: LocalDate): String {
        val adv = adventStart(d.year)
        return when {
            !d.isBefore(adv) && d.isBefore(LocalDate.of(d.year, 12, 25)) -> "Advent"
            (d.monthValue == 12 && d.dayOfMonth >= 25) || d.monthValue == 1 ||
                (d.monthValue == 2 && d.dayOfMonth == 1) -> "Christmastide · Christmas to the Purification"
            else -> "Throughout the year · Purification to Advent"
        }
    }

    fun finalMarianAntiphon(d: LocalDate): Pair<String, String?> {
        val e = daysFromEaster(d)
        val adv = adventStart(d.year)
        return when {
            e in 0..55 -> "Regina Cæli" to "regina_caeli"
            !d.isBefore(adv) || d.monthValue == 1 || (d.monthValue == 2 && d.dayOfMonth == 1) -> "Alma Redemptoris Mater" to null
            e < 0 && (d.monthValue >= 2) -> "Ave Regina Cælorum" to null
            else -> "Salve Regina" to "salve_regina"
        }
    }

    fun alleluiaSuspended(d: LocalDate): Boolean = daysFromEaster(d) in -63..-1

    // ---------- fasting and abstinence ----------

    private fun holyDayOfObligation(d: LocalDate, discipline: Discipline): Boolean {
        val e = daysFromEaster(d)
        val m = MonthDay.from(d)
        val us = setOf(md(1, 1), md(8, 15), md(11, 1), md(12, 8), md(12, 25))
        val universal = us + setOf(md(1, 6), md(3, 19), md(6, 29))
        return when (discipline) {
            Discipline.US_1951 -> m in us || e == 39
            Discipline.UNIVERSAL_1917 -> m in universal || e == 39 || e == 60
        }
    }

    fun fasting(d: LocalDate, discipline: Discipline, pre1955: Boolean = false, ignoreHolyDay: Boolean = false): FastInfo {
        val e = daysFromEaster(d)
        val dow = d.dayOfWeek
        val sunday = dow == DayOfWeek.SUNDAY
        val friday = dow == DayOfWeek.FRIDAY
        val m = MonthDay.from(d)
        val lent = e in -46..-1 && !sunday
        val ember = emberName(d) != null
        val vigilPentecost = e == 48
        val vigilAssumption = m == md(8, 14) && !sunday
        val vigilAllSaints = m == md(10, 31) && !sunday
        val vigilChristmas = m == md(12, 24) && !sunday
        val holyDay = holyDayOfObligation(d, discipline)

        val fastNote = "One full meal; two smaller meals permitted which together do not equal it. Binds from 21 to 59."
        val abstNote = "No flesh meat or soup made from meat. Binds from age 7."
        val partialNote = "Meat may be taken once, at the principal meal."
        val none = FastInfo(false, Abstinence.NONE, "", "")

        val after1957 = discipline == Discipline.US_1951 && !pre1955
        if (sunday) {
            // Canon 1252 §4: no fast on Sunday, and a vigil's fast is not moved to the Saturday.
            val vigil = when {
                m == md(12, 24) -> "Vigil of the Nativity"
                m == md(12, 7) && after1957 -> "Eve of the Immaculate Conception"
                m == md(8, 14) && !after1957 -> "Vigil of the Assumption"
                m == md(10, 31) && !after1957 -> "Vigil of All Saints"
                else -> null
            }
            return if (vigil == null) none
            else none.copy(exemption = "The fast of the $vigil is not kept on a Sunday, nor moved to the Saturday (canon 1252 §4).")
        }
        // A holy day of obligation outside Lent lifts fast and abstinence (canon 1252 §4).
        if (holyDay && !lent && !ignoreHolyDay) {
            val base = fasting(d, discipline, pre1955, ignoreHolyDay = true)
            if (!base.any) return none
            val what = if (base.fast) "fast and abstinence" else "abstinence"
            val text = if (base.reason == "Friday" || base.reason.isEmpty()) "Friday $what is lifted on a holy day of obligation (canon 1252 §4)."
            else "The $what of the ${base.reason} is lifted on a holy day of obligation (canon 1252 §4)."
            return none.copy(exemption = text)
        }
        // The day after Thanksgiving (the fourth Thursday of November) in the United States.
        val afterThanksgiving = after1957 && d.monthValue == 11 && friday && d.dayOfMonth in 23..29
        if (afterThanksgiving && !lent) {
            return none.copy(
                exemption = "Abstinence on the Friday after Thanksgiving is dispensed in the United States, " +
                    "by an indult first granted in 1958 and by the American dioceses afterward.",
            )
        }
        // Days the American rules after 1957 no longer keep.
        val usNote = when {
            !after1957 -> ""
            m == md(10, 31) -> "The Vigil of All Saints was a day of fast and abstinence under the Code of 1917; the American bishops dispensed it in 1957."
            m == md(8, 14) -> "The fast of the Vigil of the Assumption was moved to December 7 in 1957."
            else -> ""
        }

        val result = when (discipline) {
            Discipline.UNIVERSAL_1917 -> {
                val reason = when {
                    e == -46 -> "Ash Wednesday"
                    e == -2 -> "Good Friday"
                    e == -1 -> "Holy Saturday"
                    lent -> "Lent"
                    ember -> "Ember day"
                    vigilPentecost -> "Vigil of Pentecost"
                    vigilAssumption -> "Vigil of the Assumption"
                    vigilAllSaints -> "Vigil of All Saints"
                    vigilChristmas -> "Vigil of the Nativity"
                    friday -> "Friday"
                    else -> ""
                }
                val fastAndAbst = e == -46 || (lent && (friday || dow == DayOfWeek.SATURDAY)) || ember ||
                    vigilPentecost || vigilAssumption || vigilAllSaints || vigilChristmas
                val extra = if (e == -1) (if (pre1955) " Fast and abstinence end at noon." else " Fast and abstinence end at midnight (1956).") else ""
                when {
                    fastAndAbst -> FastInfo(true, Abstinence.FULL, reason, "$fastNote $abstNote$extra")
                    lent -> FastInfo(true, Abstinence.NONE, reason, fastNote)
                    friday -> FastInfo(false, Abstinence.FULL, reason, abstNote)
                    else -> none
                }
            }
            Discipline.US_1951 -> {
                // With the rubrics of 1955: the 1956 Holy Saturday (partial abstinence, to midnight), the
                // 1957 dispensation of the All Saints vigil, and the Assumption fast moved to 7 December.
                // With the older rubrics: the rules of 1951 as first set.
                val vigilImmaculate = after1957 && m == md(12, 7) && !sunday
                val assumption = vigilAssumption && !after1957
                val allSaints = vigilAllSaints && !after1957
                val holySatFull = e == -1 && !after1957
                val full = friday || e == -46 || holySatFull || assumption || vigilImmaculate || vigilChristmas
                val isFast = lent || ember || vigilPentecost || assumption || allSaints || vigilImmaculate || vigilChristmas
                val reason = when {
                    e == -46 -> "Ash Wednesday"
                    e == -2 -> "Good Friday"
                    e == -1 -> "Holy Saturday"
                    lent -> "Lent"
                    ember -> "Ember day"
                    vigilPentecost -> "Vigil of Pentecost"
                    assumption -> "Vigil of the Assumption"
                    vigilImmaculate -> "Vigil of the Immaculate Conception"
                    allSaints -> "Vigil of All Saints"
                    vigilChristmas -> "Vigil of the Nativity (by indult, may be kept on 23 December)"
                    friday -> "Friday"
                    else -> ""
                }
                val holySat = if (e == -1) (if (after1957) " Fast and abstinence end at midnight (1956)." else " Fast and abstinence end at noon.") else ""
                val moved = if (vigilImmaculate) " The fast of the Vigil of the Assumption was moved here in 1957." else ""
                when {
                    isFast && full -> FastInfo(true, Abstinence.FULL, reason, "$fastNote $abstNote$holySat$moved")
                    isFast -> FastInfo(true, Abstinence.PARTIAL, reason, "$fastNote $partialNote$holySat")
                    full -> FastInfo(false, Abstinence.FULL, reason, abstNote)
                    else -> none
                }
            }
        }
        return if (usNote.isEmpty()) result else result.copy(exemption = usNote)
    }

    /** The next day (after [from]) that carries fast or abstinence, within ~6 weeks. */
    fun nextPenitentialDay(from: LocalDate, discipline: Discipline, pre1955: Boolean = false): Pair<LocalDate, FastInfo>? {
        var d = from.plusDays(1)
        repeat(45) {
            val f = fasting(d, discipline, pre1955)
            if (f.any) return d to f
            d = d.plusDays(1)
        }
        return null
    }

    fun monthDevotion(month: Int): String = when (month) {
        1 -> "Month of the Holy Name of Jesus"
        2 -> "Month of the Holy Family"
        3 -> "Month of St. Joseph"
        4 -> "Month of the Holy Eucharist"
        5 -> "Month of Our Lady"
        6 -> "Month of the Sacred Heart"
        7 -> "Month of the Precious Blood"
        8 -> "Month of the Immaculate Heart"
        9 -> "Month of the Seven Sorrows"
        10 -> "Month of the Holy Rosary"
        11 -> "Month of the Holy Souls"
        else -> "Month of the Immaculate Conception"
    }
}
