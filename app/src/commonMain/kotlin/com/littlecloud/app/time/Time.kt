package com.littlecloud.app.time

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/*
 * A small subset of java.time on top of kotlinx-datetime, so the calendar and the screens
 * read the same on Android and iOS. Only what Little Cloud uses is here.
 */

typealias LocalDate = kotlinx.datetime.LocalDate
typealias LocalTime = kotlinx.datetime.LocalTime
typealias DayOfWeek = kotlinx.datetime.DayOfWeek

fun kotlinx.datetime.LocalDate.Companion.of(year: Int, month: Int, day: Int): LocalDate = LocalDate(year, month, day)
fun kotlinx.datetime.LocalDate.Companion.now(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

fun LocalDate.plusDays(n: Long): LocalDate = plus(n, DateTimeUnit.DAY)
fun LocalDate.plusDays(n: Int): LocalDate = plus(n, DateTimeUnit.DAY)
fun LocalDate.minusDays(n: Long): LocalDate = minus(n, DateTimeUnit.DAY)
fun LocalDate.minusDays(n: Int): LocalDate = minus(n, DateTimeUnit.DAY)
fun LocalDate.plusWeeks(n: Int): LocalDate = plus(n * 7, DateTimeUnit.DAY)
fun LocalDate.plusWeeks(n: Long): LocalDate = plus(n * 7, DateTimeUnit.DAY)
fun LocalDate.plusMonths(n: Int): LocalDate = plus(DatePeriod(months = n))
fun LocalDate.minusMonths(n: Int): LocalDate = minus(DatePeriod(months = n))
fun LocalDate.isBefore(other: LocalDate): Boolean = this < other
fun LocalDate.isAfter(other: LocalDate): Boolean = this > other
val LocalDate.monthValue: Int get() = monthNumber
val LocalDate.isLeapYear: Boolean get() = isLeap(year)
fun LocalDate.lengthOfMonth(): Int = monthLength(year, monthNumber)
fun LocalDate.with(adjuster: TemporalAdjuster): LocalDate = adjuster.adjust(this)
fun LocalDate.withDayOfMonth(day: Int): LocalDate = LocalDate(year, monthNumber, day)

/** ISO day number: Monday = 1 … Sunday = 7, as java.time's DayOfWeek.getValue(). */
val DayOfWeek.value: Int get() = isoDayNumber

fun kotlinx.datetime.LocalTime.Companion.of(hour: Int, minute: Int): LocalTime = LocalTime(hour, minute)
fun kotlinx.datetime.LocalTime.Companion.now(): LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time

fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

private fun isLeap(y: Int) = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0
private fun monthLength(y: Int, m: Int) = when (m) {
    2 -> if (isLeap(y)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

enum class ChronoUnit {
    DAYS;

    fun between(a: LocalDate, b: LocalDate): Long = a.daysUntil(b).toLong()
}

fun interface TemporalAdjuster {
    fun adjust(d: LocalDate): LocalDate
}

object TemporalAdjusters {
    fun nextOrSame(dow: DayOfWeek) = TemporalAdjuster { d ->
        d.plusDays((dow.isoDayNumber - d.dayOfWeek.isoDayNumber + 7) % 7)
    }

    fun previousOrSame(dow: DayOfWeek) = TemporalAdjuster { d ->
        d.minusDays((d.dayOfWeek.isoDayNumber - dow.isoDayNumber + 7) % 7)
    }
}

data class MonthDay(val month: Int, val day: Int) : Comparable<MonthDay> {
    override fun compareTo(other: MonthDay) = compareValuesBy(this, other, { it.month }, { it.day })

    companion object {
        fun of(month: Int, day: Int) = MonthDay(month, day)
        fun from(d: LocalDate) = MonthDay(d.monthNumber, d.dayOfMonth)
    }
}

data class YearMonth(val year: Int, val month: Int) {
    val monthValue: Int get() = month
    fun atDay(day: Int): LocalDate = LocalDate(year, month, day)
    fun lengthOfMonth(): Int = monthLength(year, month)
    fun plusMonths(n: Long): YearMonth = plusMonths(n.toInt())
    fun minusMonths(n: Long): YearMonth = plusMonths(-n.toInt())
    fun plusMonths(n: Int): YearMonth {
        val total = year * 12 + (month - 1) + n
        return YearMonth(MathX.floorDiv(total, 12), MathX.floorMod(total, 12) + 1)
    }
    fun minusMonths(n: Int): YearMonth = plusMonths(-n)

    companion object {
        fun from(d: LocalDate) = YearMonth(d.year, d.monthNumber)
        fun of(year: Int, month: Int) = YearMonth(year, month)
        fun now() = from(LocalDate.now())
    }
}

private object MathX {
    fun floorDiv(a: Int, b: Int): Int { val q = a / b; return if ((a % b != 0) && ((a xor b) < 0)) q - 1 else q }
    fun floorMod(a: Int, b: Int): Int = a - floorDiv(a, b) * b
}

private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)
private val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

/**
 * English date patterns in the java.time style. Supported letters: EEEE (weekday), MMMM (month),
 * yyyy, d, h, H, mm, a. Anything else is copied as written.
 */
class DateTimeFormatter private constructor(private val tokens: List<String>) {

    fun format(date: LocalDate?, time: LocalTime?): String = buildString {
        for (t in tokens) append(
            when (t) {
                "EEEE" -> DAYS[date!!.dayOfWeek.isoDayNumber - 1]
                "MMMM" -> MONTHS[date!!.monthNumber - 1]
                "yyyy" -> date!!.year.toString()
                "d" -> date!!.dayOfMonth.toString()
                "dd" -> date!!.dayOfMonth.toString().padStart(2, '0')
                "h" -> (time!!.hour % 12).let { if (it == 0) 12 else it }.toString()
                "H" -> time!!.hour.toString()
                "HH" -> time!!.hour.toString().padStart(2, '0')
                "mm" -> time!!.minute.toString().padStart(2, '0')
                "a" -> if (time!!.hour < 12) "AM" else "PM"
                else -> t
            }
        )
    }

    companion object {
        private val letters = setOf('E', 'M', 'y', 'd', 'h', 'H', 'm', 'a')

        fun ofPattern(pattern: String): DateTimeFormatter {
            val out = mutableListOf<String>()
            var i = 0
            while (i < pattern.length) {
                val c = pattern[i]
                var j = i
                if (c in letters) {
                    while (j < pattern.length && pattern[j] == c) j++
                } else {
                    while (j < pattern.length && pattern[j] !in letters) j++
                }
                out += pattern.substring(i, j)
                i = j
            }
            return DateTimeFormatter(out)
        }
    }
}

fun LocalDate.format(f: DateTimeFormatter): String = f.format(this, null)
fun LocalTime.format(f: DateTimeFormatter): String = f.format(null, this)
