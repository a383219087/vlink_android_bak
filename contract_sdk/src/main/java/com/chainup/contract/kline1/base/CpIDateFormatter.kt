package com.chainup.contract.kline1.base

import java.util.*

/**
 * @Author: Bertking
 * @Date：2019/3/11-10:45 AM
 * @Description: 日期格式类
 */
interface CpIDateFormatter {
    fun format(date: Date): String
}