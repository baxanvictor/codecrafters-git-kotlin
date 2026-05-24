package utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun ZonedDateTime.toFormattedCommitTimestamp(): String {
    val millis = toInstant().toEpochMilli()
    val timezone = format(DateTimeFormatter.ofPattern("Z"))

    return "$millis $timezone"
}