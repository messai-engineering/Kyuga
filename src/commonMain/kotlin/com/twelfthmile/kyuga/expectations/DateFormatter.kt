package com.twelfthmile.kyuga.expectations


expect fun formatDateDefault(multDate: MultDate): String
expect fun formatDateByFormat(date: String, format: String): MultDate
expect fun formatForEnglishLocale(date: String, format: String): MultDate

expect class KyugaDateFormatter()