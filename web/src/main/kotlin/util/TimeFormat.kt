package util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJSDate
import kotlinx.datetime.toLocalDateTime
import kotlin.js.Date

fun Instant.formatPretty(): String {
    val d: Date = this.toJSDate()

    val month = (d.getMonth() + 1).toString().padStart(2, '0')
    val day = d.getDate().toString().padStart(2, '0')
    val year = (d.getFullYear() % 100).toString().padStart(2, '0')

    val hours = d.getHours().toString().padStart(2, '0')
    val minutes = d.getMinutes().toString().padStart(2, '0')

    return "$month/$day/$year, $hours:$minutes"
}

fun String.toInstantOrNull(): Instant? {
    if (isBlank()) return null
    val local = LocalDateTime.parse(this)
    return local.toInstant(TimeZone.currentSystemDefault())
}

fun Instant.toDatetimeLocalString(): String {
    val dt = this.toLocalDateTime(TimeZone.currentSystemDefault())

    fun Int.pad2() = toString().padStart(2, '0')
    return "${dt.date.year}-${dt.date.monthNumber.pad2()}-${dt.date.dayOfMonth.pad2()}" +
        "T${dt.hour.pad2()}:${dt.minute.pad2()}"
}
