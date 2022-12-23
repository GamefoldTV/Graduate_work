package ru.netology.nework.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {

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


}