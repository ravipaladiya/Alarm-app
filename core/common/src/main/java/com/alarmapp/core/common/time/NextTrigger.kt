package com.alarmapp.core.common.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Compute the next epoch-ms trigger for an alarm.
 *
 * [repeatDaysMask] is a 7-bit mask where bit 0 = Monday ... bit 6 = Sunday.
 * If the mask is zero, the alarm is treated as one-shot: the next occurrence today
 * if [time] is in the future, otherwise tomorrow.
 *
 * DST transitions are resolved via [ZonedDateTime] — when a local time does not exist
 * (spring-forward gap) Java adjusts forward; when it occurs twice (fall-back overlap)
 * the earlier offset is taken. Callers should recompute on `ACTION_TIMEZONE_CHANGED`.
 */
fun nextTriggerEpochMillis(
    time: LocalTime,
    repeatDaysMask: Int,
    clock: Clock,
): Long {
    val zone = clock.zone()
    val now = ZonedDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(clock.nowMillis()),
        zone,
    )
    return if (repeatDaysMask == 0) {
        oneShotNext(time, now, zone).toInstant().toEpochMilli()
    } else {
        repeatNext(time, repeatDaysMask, now, zone).toInstant().toEpochMilli()
    }
}

private fun oneShotNext(time: LocalTime, now: ZonedDateTime, zone: ZoneId): ZonedDateTime {
    val todayAt = ZonedDateTime.of(LocalDateTime.of(now.toLocalDate(), time), zone)
    return if (todayAt.isAfter(now)) todayAt else todayAt.plusDays(1)
}

private fun repeatNext(
    time: LocalTime,
    mask: Int,
    now: ZonedDateTime,
    zone: ZoneId,
): ZonedDateTime {
    val today = now.toLocalDate()
    for (offset in 0..7) {
        val candidate = today.plusDays(offset.toLong())
        if (!isDayEnabled(candidate, mask)) continue
        val zdt = ZonedDateTime.of(LocalDateTime.of(candidate, time), zone)
        if (zdt.isAfter(now)) return zdt
    }
    error("No enabled day found in 7-day window; mask=$mask")
}

private fun isDayEnabled(date: LocalDate, mask: Int): Boolean {
    val bit = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }
    return (mask and (1 shl bit)) != 0
}
