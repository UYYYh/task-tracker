import kotlinx.datetime.Instant
import kotlinx.datetime.toJSDate
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
