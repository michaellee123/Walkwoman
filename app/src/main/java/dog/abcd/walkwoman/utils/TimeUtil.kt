package dog.abcd.walkwoman.utils

import java.util.*

fun Int.toReadableDurationString(): String {
    var minutes = this / 1000 / 60
    val seconds = this / 1000 % 60
    return if (minutes < 60) {
        String.format(
            Locale.getDefault(),
            "%02d:%02d",
            minutes,
            seconds
        )
    } else {
        val hours = minutes / 60
        minutes %= 60
        String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )
    }
}