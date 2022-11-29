package com.yjkj.chainup.util

import java.text.SimpleDateFormat
import java.util.*


object DateUtil {

    /**
     * 日期转换为字符串
     *
     * @param format 日期格式  比如：yyyyMMdd
     * @param date   日期
     * @return
     */
    fun dateToString(format: String, date: Date): String {
        val mFormat = SimpleDateFormat(format)
        return mFormat.format(date)
    }

    @JvmStatic
    fun longToString(format: String, date: Long): String {
        return dateToString(format, Date(date))
    }

    fun NewTimeReturn(): String {
        var time = dateToString("HH:mm", Date(System.currentTimeMillis()))
        var min = time.split(":")
        return min[1]
    }


    fun timestampToString(format: String, date: Long): String {
        val mFormat = SimpleDateFormat(format)
        return mFormat.format(date)
    }


    fun timestampToStringNextDay(): String {
        var time = dateToString("yyyy-MM-dd", Date(System.currentTimeMillis()+24*3600000))
        return  "$time 00:00:00"
    }


}
