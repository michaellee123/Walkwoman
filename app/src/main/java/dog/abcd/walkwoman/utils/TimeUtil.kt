package dog.abcd.walkwoman.utils

fun Int.formatTime(): String {
    val hh = this / 3600
    val mm = (this % 3600) / 60
    val ss = this % 60
    return "$hh:$mm:$ss"
}