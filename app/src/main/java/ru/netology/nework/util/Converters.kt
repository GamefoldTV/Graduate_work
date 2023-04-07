package ru.netology.nework.util

import android.icu.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

fun convertString2DateTime2String(dateString: String): String {
    val string2date = Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(dateString)))
    val date2string = SimpleDateFormat("dd MMMM yyyy HH:mm").format(string2date)
    return date2string
}

fun convertString2Date2String(dateString: String): String {
    val string2date = Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(dateString)))
    val date2string = SimpleDateFormat("dd MMMM yyyy").format(string2date)
    return date2string
}

fun convertDateTime2ISO_Instant(date : String, time : String) : String {
    val string2date = SimpleDateFormat("dd.MM.yyyy HH:mm").parse("$date $time")
    val date2string = string2date.toInstant().toString()
    return date2string
}
