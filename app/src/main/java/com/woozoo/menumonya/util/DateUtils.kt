package com.woozoo.menumonya.util

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {
    companion object {
        fun getTodayDate(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val todayDate = dateFormat.format(Date())

            return todayDate
        }
    }
}