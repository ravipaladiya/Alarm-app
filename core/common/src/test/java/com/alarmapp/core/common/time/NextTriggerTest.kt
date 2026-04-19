package com.alarmapp.core.common.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

private class FixedClock(
    private val localDateTime: LocalDateTime,
    private val zoneId: ZoneId,
) : Clock {
    override fun nowMillis(): Long =
        localDateTime.atZone(zoneId).toInstant().toEpochMilli()
    override fun zone(): ZoneId = zoneId
}

class NextTriggerTest {

    private val utc = ZoneOffset.UTC

    @Test fun `one-shot alarm picks today when time still ahead`() {
        val clock = FixedClock(LocalDateTime.of(2026, 4, 19, 6, 0), utc)
        val next = nextTriggerEpochMillis(LocalTime.of(7, 30), repeatDaysMask = 0, clock)
        val expected = LocalDateTime.of(2026, 4, 19, 7, 30).toInstant(utc).toEpochMilli()
        assertEquals(expected, next)
    }

    @Test fun `one-shot alarm rolls to tomorrow when time already passed`() {
        val clock = FixedClock(LocalDateTime.of(2026, 4, 19, 8, 0), utc)
        val next = nextTriggerEpochMillis(LocalTime.of(7, 30), repeatDaysMask = 0, clock)
        val expected = LocalDateTime.of(2026, 4, 20, 7, 30).toInstant(utc).toEpochMilli()
        assertEquals(expected, next)
    }

    @Test fun `repeat alarm weekdays picks next weekday when weekend`() {
        // 2026-04-18 is a Saturday; weekdays mask = bits 0..4 = 0b0011111 = 31.
        val clock = FixedClock(LocalDateTime.of(2026, 4, 18, 10, 0), utc)
        val next = nextTriggerEpochMillis(LocalTime.of(7, 30), repeatDaysMask = 31, clock)
        // Next trigger should be Monday 2026-04-20 at 07:30.
        assertEquals(
            LocalDate.of(2026, 4, 20),
            LocalDateTime.ofEpochSecond(next / 1000, 0, ZoneOffset.UTC).toLocalDate(),
        )
    }
}
