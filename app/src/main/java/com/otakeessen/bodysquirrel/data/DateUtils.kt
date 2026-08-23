package com.otakeessen.bodysquirrel.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun todayIsoDate(): String = isoFormat.format(Date())
}
