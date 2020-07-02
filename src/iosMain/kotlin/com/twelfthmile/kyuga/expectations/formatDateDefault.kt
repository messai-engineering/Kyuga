package com.twelfthmile.kyuga.expectations

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

private const val DATE_TIME_FORMAT_STR = "yyyy-MM-dd HH:mm:ss"
private const val DATE_FORMAT_STR = "yyyy-MM-dd"

actual fun formatDateDefault(multDate: MultDate): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.setDateFormat(DATE_TIME_FORMAT_STR)
    val dateString = dateFormatter.stringFromDate(multDate.date)
    return dateString
}

actual fun formatDateByFormat(date: String, format: String): MultDate {
    val dateFormatter = NSDateFormatter()
    dateFormatter.setDateFormat(format)
    val dt = MultDate()
    dt.date = dateFormatter.dateFromString(date)!!
    return dt
}

actual fun formatForEnglishLocale(
    date: String,
    format: String
): MultDate {
    val dateFormatter = NSDateFormatter()
    dateFormatter.setDateFormat(format)
    dateFormatter.locale = NSLocale("en_GB")
    val dt = MultDate()
    dt.date = dateFormatter.dateFromString(date)!!
    return dt
}

actual class KyugaDateFormatter {
    val dateTimeFormatter: NSDateFormatter
        get() = getDefaulFormatter(DATE_TIME_FORMAT_STR)

    val dateFormatter : NSDateFormatter
        get() = getDefaulFormatter(DATE_FORMAT_STR)

    private fun getDefaulFormatter(format: String): NSDateFormatter {
        val fmt = NSDateFormatter()
        fmt.setDateFormat(format)
        return fmt
    }
}