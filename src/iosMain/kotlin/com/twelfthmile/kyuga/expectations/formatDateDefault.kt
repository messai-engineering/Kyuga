package com.twelfthmile.kyuga.expectations

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

private const val DATE_TIME_FORMAT_STR = "yyyy-MM-dd HH:mm:ss"

actual fun formatDateDefault(multDate: MultDate): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.setLocalizedDateFormatFromTemplate(DATE_TIME_FORMAT_STR)
    return dateFormatter.stringFromDate(multDate.date)
}

actual fun formatDateByFormat(date: String, format: String): MultDate {
    val dateFormatter = NSDateFormatter()
    dateFormatter.setLocalizedDateFormatFromTemplate(format)
    val dt = MultDate()
    dt.date = dateFormatter.dateFromString(date)!!
    return dt
}

actual fun formatForEnglishLocale(
    date: String,
    format: String
): MultDate {
    val dateFormatter = NSDateFormatter()
    dateFormatter.locale = NSLocale("en_GB")
    val dt = MultDate()
    dt.date = dateFormatter.dateFromString(date)!!
    return dt
}