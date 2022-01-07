package com.example.mooncalendar.utils

import net.time4j.Moment
import net.time4j.scale.TimeScale
import java.time.Instant
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset

fun YearMonth.startOfTheMonth(): Instant {
    return this
        .atDay(1)
        .atTime(LocalTime.MIN)
        .toInstant(ZoneOffset.UTC)
}

fun YearMonth.endOfTheMonth(): Instant {
    return this
        .atEndOfMonth()
        .atTime(LocalTime.MAX)
        .toInstant(ZoneOffset.UTC)
}

fun Instant.toMoment(): Moment {
    return Moment.of(this.epochSecond, this.nano, TimeScale.POSIX)
}

fun Moment.toInstant(): Instant {
    return Instant.ofEpochSecond(this.posixTime, this.nanosecond.toLong())
}

fun Moment.isBeforeOrEqual(temporal: Moment): Boolean {
    return !this.isAfter(temporal)
}