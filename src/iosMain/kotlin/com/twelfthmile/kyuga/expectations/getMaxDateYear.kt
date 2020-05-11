package com.twelfthmile.kyuga.expectations

import platform.Foundation.NSDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSYearCalendarUnit
import platform.Foundation.NSMonthCalendarUnit
import platform.Foundation.NSDayCalendarUnit

actual fun getMaxDateYear(): Long {
    val currentDate = NSDate()
    val calender = NSCalendar.currentCalendar
    val unitFlags = NSYearCalendarUnit or NSMonthCalendarUnit or NSDayCalendarUnit
    val components = calender.components(unitFlags, currentDate)
    return components.year.toLong()
}