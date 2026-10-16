package com.littlecloud.app

import com.littlecloud.app.data.Abstinence
import com.littlecloud.app.data.Calendar1962
import com.littlecloud.app.data.Discipline
import com.littlecloud.app.data.LColor
import com.littlecloud.app.data.LittleOffice
import com.littlecloud.app.time.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Checks the java.time stand-in and the 1962 calendar against known dates. */
class TimeAndCalendarTest {
    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day)

    @Test fun easter() {
        assertEquals(d(2026, 4, 5), Calendar1962.easter(2026))
        assertEquals(d(2027, 3, 28), Calendar1962.easter(2027))
        assertEquals(d(2038, 4, 25), Calendar1962.easter(2038))
    }

    @Test fun adjusters() {
        assertEquals(d(2026, 11, 29), Calendar1962.adventStart(2026))   // 27 Nov 2026 is a Friday
        assertEquals(d(2027, 11, 28), Calendar1962.adventStart(2027))   // a Sunday: same day
        assertEquals(d(2026, 10, 25), d(2026, 10, 31).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)))
        assertEquals(d(2026, 10, 31), d(2026, 10, 31).with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)))
    }

    @Test fun arithmetic() {
        assertEquals(d(2027, 1, 1), d(2026, 12, 31).plusDays(1))
        assertEquals(d(2028, 2, 29), d(2028, 3, 1).minusDays(1))
        assertEquals(9L, ChronoUnit.DAYS.between(d(2026, 10, 7), d(2026, 10, 16)))
        assertEquals(-9L, ChronoUnit.DAYS.between(d(2026, 10, 16), d(2026, 10, 7)))
        assertEquals(29, d(2028, 2, 10).lengthOfMonth())
        assertTrue(d(2028, 1, 1).isLeapYear)
        assertEquals(YearMonth(2027, 1), YearMonth(2026, 12).plusMonths(1))
        assertEquals(YearMonth(2025, 12), YearMonth(2026, 1).minusMonths(1))
        assertEquals(5, d(2026, 10, 16).dayOfWeek.value)                 // Friday
        assertEquals("2026-10-16", d(2026, 10, 16).toString())           // keys saved by 0.6 stay valid
    }

    @Test fun formatting() {
        val day = d(2026, 10, 16)
        assertEquals("Friday · 16 October 2026", day.format(DateTimeFormatter.ofPattern("EEEE · d MMMM yyyy")))
        assertEquals("October 2026", day.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
        assertEquals("Friday, 16 October", day.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")))
        assertEquals("5:30 PM", LocalTime.of(17, 30).format(DateTimeFormatter.ofPattern("h:mm a")))
        assertEquals("12:05 AM", LocalTime.of(0, 5).format(DateTimeFormatter.ofPattern("h:mm a")))
    }

    @Test fun calendar1962() {
        val christmas = Calendar1962.day(d(2026, 12, 25), Discipline.UNIVERSAL_1917, false)
        assertEquals("Nativity of Our Lord", christmas.title)
        assertEquals(LColor.WHITE, christmas.color)
        val ash = Calendar1962.day(d(2027, 2, 10), Discipline.UNIVERSAL_1917, false)
        assertEquals("Ash Wednesday", ash.title)
        assertTrue(ash.fast.fast && ash.fast.abstinence == Abstinence.FULL)
        val gaudete = Calendar1962.day(d(2026, 12, 13), Discipline.UNIVERSAL_1917, false)
        assertEquals(LColor.ROSE, gaudete.color)
        val friday = Calendar1962.fasting(d(2026, 10, 16), Discipline.UNIVERSAL_1917)
        assertEquals(Abstinence.FULL, friday.abstinence)
    }

    @Test fun littleOfficeKeys() {
        assertEquals("year|norm|lauds", LittleOffice.key(d(2026, 10, 16), "lauds"))
        assertEquals("adv|norm|matins|A", LittleOffice.key(d(2026, 12, 3), "matins"))   // Thursday of Advent
        assertEquals("year|pasch|vespers", LittleOffice.key(d(2026, 4, 20), "vespers"))
    }
}
