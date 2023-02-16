package ru.netology.nework.util

import android.icu.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*


fun convertCount2String(count: Long): String {
    val result = when {
        count < 1_000 -> count.toString()
        count in 1_000..1_099 -> "1K"
        count in 1_100..9_999 -> String.format("%.1fK", (count / 100).toDouble() / 10)
        count in 10_000..999_999 -> (count / 1000).toString() + "K"
        count in 1_000_000..1_999_999 -> "1M"
        else -> String.format("%.1fРњ", (count / 100_000).toDouble() / 10)
    }
    return result
}

fun convertString2Date2String(dateString: String): String {
    val string2date = Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(dateString)))
    val date2string = SimpleDateFormat("dd MMMM yyyy HH:mm").format(string2date)
    return date2string
}

fun convertDateTime2ISO_Instant(date : String, time : String) : String {
    val string2date = SimpleDateFormat("dd.MM.yyyy HH:mm").parse("$date $time")
    val date2string = string2date.toInstant().toString()
    return date2string
}
