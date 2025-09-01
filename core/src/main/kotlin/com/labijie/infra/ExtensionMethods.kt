
/**
 * @author Anders Xiao
 * @date 2025/9/1
 */

package com.labijie.infra

import java.time.*


/**
 * Returns a LocalDateTime representing the end of the day.
 *
 * @receiver the original LocalDate
 * @return a LocalDateTime at 23:59:59.999999999 of the same day
 */
fun LocalDate.atEndOfDay(): LocalDateTime {
    // Create a LocalDateTime at the last nanosecond of the day
    return this.atTime(23, 59, 59, 999_999_999)
}

/**
 * Returns a ZonedDateTime representing the end of the day in the given time zone.
 * Handles possible DST gaps by using the last valid time before the gap.
 *
 * @receiver the original LocalDate
 * @param zone the time zone to consider for the ZonedDateTime
 * @return a ZonedDateTime at the last valid nanosecond of the day in the given time zone
 */
fun LocalDate.atEndOfDay(zone: ZoneId): ZonedDateTime {
    // Create a LocalDateTime at the last nanosecond of the day
    var ldt = atTime(23, 59, 59, 999_999_999)

    // If the zone has DST rules, check if this LocalDateTime falls into a gap
    if (zone !is ZoneOffset) {
        val rules = zone.rules
        val trans = rules.getTransition(ldt)
        if (trans != null && trans.isGap) {
            // Adjust to the last valid time before the gap to stay in the same day
            ldt = trans.dateTimeBefore
        }
    }

    // Return the ZonedDateTime in the given time zone
    return ZonedDateTime.of(ldt, zone)
}

/**
 * Returns the start of day in milliseconds (UTC) for the given LocalDate.
 *
 * @receiver the original LocalDate
 * @return UTC milliseconds of the start of day (00:00:00.000) in the system default zone
 */
fun LocalDate.startOfDayEpochMillis(): Long {
    val ldt = this.atStartOfDay() // LocalDateTime at 00:00:00.000000000
    return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

/**
 * Returns the start of day in milliseconds (UTC) for the given LocalDate and time zone.
 *
 * @receiver the original LocalDate
 * @param zone the time zone to consider
 * @return UTC milliseconds of the start of day in the given time zone
 */
fun LocalDate.startOfDayEpochMillis(zone: ZoneId): Long {
    val zdt = this.atStartOfDay(zone) // ZonedDateTime at 00:00:00, DST safe
    return zdt.toInstant().toEpochMilli()
}

/**
 * Returns the end of day in milliseconds (UTC) for the given LocalDate.
 *
 * @receiver the original LocalDate
 * @return UTC milliseconds of the end of day (23:59:59.999999999) in the system default zone
 */
fun LocalDate.endOfDayEpochMillis(): Long {
    val ldt = this.atEndOfDay() // LocalDateTime at 23:59:59.999999999
    return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

/**
 * Returns the end of day in milliseconds (UTC) for the given LocalDate and time zone.
 *
 * @receiver the original LocalDate
 * @param zone the time zone to consider
 * @return UTC milliseconds of the end of day in the given time zone
 */
fun LocalDate.endOfDayEpochMillis(zone: ZoneId): Long {
    val zdt = this.atEndOfDay(zone) // ZonedDateTime at 23:59:59.999999999, DST safe
    return zdt.toInstant().toEpochMilli()
}

/**
 * Computes the minimum Snowflake ID for the given LocalDateTime.
 *
 * @param dateTime the LocalDateTime to compute the minimum ID for
 * @param zone the time zone of the input LocalDateTime, defaults to system default
 * @return the minimum Snowflake ID corresponding to the given LocalDateTime
 */
fun ITimeBasedIdGenerator.minIdOfDateTime(
    dateTime: LocalDateTime,
    zone: ZoneId = ZoneId.systemDefault()
): Long {
    // Convert LocalDateTime to UTC milliseconds
    val utcMillis = dateTime.atZone(zone)
        .withZoneSameInstant(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()

    // Compute the minimum ID for this millisecond
    return computeMinId(utcMillis)
}

/**
 * Computes the maximum Snowflake ID for the given LocalDateTime.
 *
 * @param dateTime the LocalDateTime to compute the maximum ID for
 * @param zone the time zone of the input LocalDateTime, defaults to system default
 * @return the maximum Snowflake ID corresponding to the given LocalDateTime
 */
fun ITimeBasedIdGenerator.maxIdOfDateTime(
    dateTime: LocalDateTime,
    zone: ZoneId = ZoneId.systemDefault()
): Long {
    // Convert LocalDateTime to UTC milliseconds
    val utcMillis = dateTime.atZone(zone)
        .withZoneSameInstant(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()

    // Compute the maximum ID for this millisecond
    return computeMaxId(utcMillis)
}