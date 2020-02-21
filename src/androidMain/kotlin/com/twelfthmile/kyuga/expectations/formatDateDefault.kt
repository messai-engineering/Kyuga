package com.twelfthmile.kyuga.expectations

import java.text.SimpleDateFormat
import java.util.*

private const val DATE_TIME_FORMAT_STR = "yyyy-MM-dd HH:mm:ss"
private val DATE_TIME_FORMATTER = SimpleDateFormat(DATE_TIME_FORMAT_STR)

actual fun formatDateDefault(multDate: MultDate): String = DATE_TIME_FORMATTER.format(multDate.date)

actual fun formatDateByFormat(date: String, format: String): MultDate {
    val dt = MultDate()
    dt.date = SimpleDateFormat(format).parse(date)
    return dt
}

actual fun formatForEnglishLocale(
    date: String,
    format: String
): MultDate {
    val dt = MultDate()
    dt.date = SimpleDateFormat(format, Locale.ENGLISH).parse(date)
    return dt
}