package com.twelfthmile.kyuga.expectations

import java.util.*

actual fun getMaxDateYear(): Long = Calendar.getInstance().get(Calendar.YEAR).toLong()
