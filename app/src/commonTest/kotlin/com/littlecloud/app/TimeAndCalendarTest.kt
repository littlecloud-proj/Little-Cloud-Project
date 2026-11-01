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

    @Test fun holyDays2026() {
        val us = listOf(d(2026, 1, 1), d(2026, 5, 14), d(2026, 8, 15), d(2026, 11, 1), d(2026, 12, 8), d(2026, 12, 25))
        us.forEach { assertTrue(Calendar1962.holyDayOfObligation(it, Discipline.US_1951), "$it") }
        listOf(d(2026, 1, 6), d(2026, 3, 19), d(2026, 6, 4), d(2026, 6, 29)).forEach {
            assertTrue(!Calendar1962.holyDayOfObligation(it, Discipline.US_1951), "$it")
            assertTrue(Calendar1962.holyDayOfObligation(it, Discipline.UNIVERSAL_1917), "$it")
        }
    }

    /** American rules against the 2026 CMRI wall calendar: F = fast and complete abstinence,
     *  P = fast and partial abstinence, f = fast only, A = abstinence only, - = neither. */
    @Test fun usFasting2026() {
        val marks = mapOf(
            d(2026, 1, 2) to 'A', d(2026, 1, 1) to '-', d(2026, 2, 13) to 'A',
            d(2026, 2, 18) to 'F', d(2026, 2, 19) to 'f', d(2026, 2, 20) to 'F', d(2026, 2, 21) to 'f',
            d(2026, 2, 22) to '-', d(2026, 2, 25) to 'P', d(2026, 2, 27) to 'F', d(2026, 2, 28) to 'P',
            d(2026, 3, 19) to 'f', d(2026, 3, 25) to 'f', d(2026, 4, 2) to 'f',
            d(2026, 4, 3) to 'F', d(2026, 4, 4) to 'F', d(2026, 4, 10) to 'A',
            d(2026, 5, 13) to '-', d(2026, 5, 23) to 'P', d(2026, 5, 27) to 'P', d(2026, 5, 29) to 'F',
            d(2026, 5, 30) to 'P', d(2026, 6, 12) to 'A', d(2026, 6, 23) to '-',
            d(2026, 8, 14) to 'A', d(2026, 9, 16) to 'P', d(2026, 9, 18) to 'F', d(2026, 9, 19) to 'P',
            d(2026, 10, 31) to '-', d(2026, 11, 27) to '-', d(2026, 12, 7) to 'F',
            d(2026, 12, 16) to 'P', d(2026, 12, 18) to 'F', d(2026, 12, 19) to 'P',
            d(2026, 12, 24) to 'F', d(2026, 12, 25) to '-',
        )
        for ((day, want) in marks) {
            val f = Calendar1962.fasting(day, Discipline.US_1951)
            val got = when {
                f.fast && f.abstinence == Abstinence.FULL -> 'F'
                f.fast && f.abstinence == Abstinence.PARTIAL -> 'P'
                f.fast -> 'f'
                f.abstinence == Abstinence.FULL -> 'A'
                else -> '-'
            }
            assertEquals(want, got, "fasting on $day")
        }
    }

    @Test fun littleOfficeKeys() {
        assertEquals("year|norm|lauds", LittleOffice.key(d(2026, 10, 16), "lauds"))
        assertEquals("adv|norm|matins|A", LittleOffice.key(d(2026, 12, 3), "matins"))   // Thursday of Advent
        assertEquals("year|pasch|vespers", LittleOffice.key(d(2026, 4, 20), "vespers"))
    }
}
