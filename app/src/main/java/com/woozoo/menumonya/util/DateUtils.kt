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

        /**
         * 해당 메뉴의 날짜를 표시함
         * 형식 : 05월 08일 (월요일)
         */
        fun getTodayMenuDateText(): String {
            val dateFormat = SimpleDateFormat("MM월 dd일 (E요일)", Locale.KOREAN)
            val todayDate = dateFormat.format(Date())

            return todayDate
        }
    }
}