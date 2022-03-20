package com.chainup.contract.utils

import android.content.Context
import android.text.TextUtils
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.regex.Pattern

class CpNumberUtil {
    fun toInt(str: String?): Int {
        return toInt(str, 0)
    }

    private fun toInt(str: String?, defaultValue: Int): Int {
        return if (str == null || str.length == 0) {
            defaultValue
        } else try {
            str.toInt()
        } catch (nfe: NumberFormatException) {
            defaultValue
        }
    }

    fun toLong(str: String?): Long {
        return toLong(str, 0L)
    }

    private fun toLong(str: String?, defaultValue: Long): Long {
        return if (str == null) {
            defaultValue
        } else try {
            str.toLong()
        } catch (nfe: NumberFormatException) {
            defaultValue
        }
    }

    fun toFloat(str: String?): Float {
        return toFloat(str, 0.0f)
    }

    private fun toFloat(str: String?, defaultValue: Float): Float {
        return if (str == null) {
            defaultValue
        } else try {
            str.toFloat()
        } catch (nfe: NumberFormatException) {
            defaultValue
        }
    }

    fun toDouble(str: String?): Double {
        return toDouble(str, 0.0)
    }

    private fun toDouble(str: String?, defaultValue: Double): Double {
        return if (str == null) {
            defaultValue
        } else try {
            str.toDouble()
        } catch (nfe: NumberFormatException) {
            defaultValue
        }
    }

    fun createFloat(str: String?): Float? {
        return if (str == null) {
            null
        } else java.lang.Float.valueOf(str)
    }

    fun createDouble(str: String?): Double {
        return if (TextUtils.isEmpty(str)) {
            0.00
        } else str!!.toDouble()
    }


    fun createInteger(str: String?): Int? {
        return if (str == null) {
            null
        } else Integer.decode(str)
        // decode() handles 0xAABD and 0777 (hex and octal) as well.
    }

    fun createLong(str: String?): Long? {
        return if (str == null) {
            null
        } else java.lang.Long.valueOf(str)
    }

    fun createBigInteger(str: String?): BigInteger? {
        return str?.let { BigInteger(it) }
    }

    fun createBigDecimal(str: String?): BigDecimal? {
        if (str == null) {
            return null
        }
        // handle JDK1.3.1 bug where "" throws IndexOutOfBoundsException
        if (TextUtils.isEmpty(str)) {
            throw NumberFormatException("A blank string is not a valid number")
        }
        return BigDecimal(str)
    }

    fun isNum(str: String?): Boolean {
        val pattern = Pattern.compile("^-?[0-9]+")
        //数字
//非数字
        return pattern.matcher(str).matches()
    }

    fun isNum1(str: String?): Boolean {
        //带小数的
        val pattern =
            Pattern.compile("^[-+]?[0-9]+(\\.[0-9]+)?$")

        //数字
//非数字
        return pattern.matcher(str).matches()
    }

    fun addNum(num: String): String {
        if (TextUtils.isEmpty(num)) {
            return "1"
        }
        val add: Double
        var n = 0
        val index = num.indexOf(".")
        if (index == -1) {
            add = 1.0
        } else {
            n = num.length - index - 1
            add = 1.0 / Math.pow(10.0, n.toDouble())
        }
        val decimalFormat = DecimalFormat(
            "###################.###########",
            DecimalFormatSymbols(Locale.ENGLISH)
        )
        val vol =
            CpMathHelper.round(CpMathHelper.add(num, java.lang.Double.toString(add)), n)
        var ret = decimalFormat.format(vol)
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length - 2)
        }
        return ret
    }

    fun minusNum(num: String): String {
        if (TextUtils.isEmpty(num)) {
            return "0"
        }
        if (num == "0") {
            return "0"
        }
        val add: Double
        var n = 0
        val index = num.indexOf(".")
        if (index == -1) {
            add = 1.0
        } else {
            n = num.length - index - 1
            add = 1.0 / Math.pow(10.0, n.toDouble())
        }
        val decimalFormat = DecimalFormat(
            "###################.###########",
            DecimalFormatSymbols(Locale.ENGLISH)
        )
        val vol =
            CpMathHelper.round(CpMathHelper.sub(num, java.lang.Double.toString(add)), n)
        var ret = decimalFormat.format(vol)
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length - 2)
        }
        return ret
    }

    fun getDecimal(index: Int): DecimalFormat {
        return getDecimal(index, RoundingMode.FLOOR)
    }

    fun getDecimal(index: Int, roundingMode: RoundingMode = RoundingMode.FLOOR): DecimalFormat {
        val decimalFormat = when (index) {
            0 -> DecimalFormat(
                "###################",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            1 -> DecimalFormat(
                "##0.0",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            2 -> DecimalFormat(
                "##0.00",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            3 -> DecimalFormat(
                "##0.000",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            4 -> DecimalFormat(
                "##0.0000",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            5 -> DecimalFormat(
                "##0.00000",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            6 -> DecimalFormat(
                "##0.000000",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            7 -> DecimalFormat(
                "##0.0000000",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            8 -> DecimalFormat(
                "##0.00000000",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            9 -> DecimalFormat(
                "##0.000000000",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            10 -> DecimalFormat(
                "##0.0000000000",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            else -> DecimalFormat(
                "###################.###########",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
        }
        decimalFormat.roundingMode = roundingMode
        return decimalFormat
    }

    /**
     * 向上取整
     * @param num
     * @return
     */
    fun roundUp(num: Double): Int {
        return Math.ceil(num).toInt()
    }

    fun getBigVolum(
        context: Context?,
        orgDf: DecimalFormat,
        volum: Double,
        isZhEnv:Boolean = true
    ): String {
        var volum = volum
        val decimalFormat = DecimalFormat(
            "##0.000",
            DecimalFormatSymbols(Locale.ENGLISH)
        )
        val result: String
        if (isZhEnv) {
            if (volum >= 100000000) {
                volum = CpMathHelper.div(volum, 100000000.0, 3)
                result = decimalFormat.format(volum) + "亿"
            } else if (volum > 10000) {
                volum = CpMathHelper.div(volum, 10000.0, 3)
                result = decimalFormat.format(volum) + "万"
            } else {
                result = orgDf.format(volum)
            }
        } else {
            if (volum >= 1000000) {
                volum = CpMathHelper.div(volum, 1000000.0, 3)
                result = decimalFormat.format(volum) + "M"
            } else if (volum > 1000) {
                volum = CpMathHelper.div(volum, 1000.0, 3)
                result = decimalFormat.format(volum) + "K"
            } else {
                result = orgDf.format(volum)
            }
        }
        return result
    }

}