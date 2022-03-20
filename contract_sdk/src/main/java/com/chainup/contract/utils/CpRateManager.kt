package com.chainup.contract.utils

import android.widget.TextView
import java.math.BigDecimal

/**
 * @Author: Bertking
 * @Date：2018/12/8-2:56 PM
 * @Description: 处理汇率的存储&计算and so on...
 */
var TAG = RateManager::class.java.simpleName

class RateManager {

    companion object {
        /**
         * 获取精度
         */
        const val coin_precision = "coin_precision"

        const val coin_fiat_precision = "coin_fiat_precision"

        /**
         * 货币符号
         */
        const val lang_logo = "lang_logo"

        /**
         * 真实货币
         */
        const val lang_coin = "lang_coin"

        /*
         * 法币默认精度
         */
        const val default_precision = 2





        fun getRose(rose: Double): Double {
            return if (rose == 0.0) {
                0.00
            } else {
                return if (rose > 0) rose * 100 - 0.005 else rose * 100 + 0.005
            }
        }

        fun getRoseTrend(rose: String?): Int {
            if (!CpStringUtil.isNumeric(rose)) return 0
            return CpBigDecimalUtils.divForDown(BigDecimal(rose).multiply(BigDecimal("100")).toPlainString(), 2).compareTo(BigDecimal("0"))
        }

        /**
         * 涨跌幅
         * 服务器会返回"0,78"数据格式
         */
        fun getRoseText(textView: TextView?, rose: String?) {
            if (!CpStringUtil.isNumeric(rose)){
                textView?.text = "--"
                return
            }
            val roseValue = CpBigDecimalUtils.divForDown(BigDecimal(rose).multiply(BigDecimal("100")).toPlainString(), 2)
            val compareTo = roseValue.compareTo(BigDecimal("0"))
            when (compareTo) {
                -1 -> {
                    textView?.text = "${roseValue.toPlainString()}%"
                }

                0 -> {
                    textView?.text = roseValue.toPlainString() + "%"
                }

                1 -> {
                    textView?.text = "+${roseValue.toPlainString()}%"
                }
            }
        }

        fun getRoseText4Kline(rose: String?): String {
            if (!CpStringUtil.isNumeric(rose)) return ""
            var lines = rose ?: ""
            val roseValue = CpBigDecimalUtils.divForDown(BigDecimal(rose).multiply(BigDecimal("100")).toPlainString(), 2)
            val compareTo = CpBigDecimalUtils.compareTo(roseValue.toPlainString(), "0")
            when (compareTo) {
                -1 -> {
                    lines = "${roseValue.toPlainString()}%"
                }

                0 -> {
                    lines = roseValue.toPlainString() + "%"
                }

                1 -> {
                    lines = "+${roseValue.toPlainString()}%"
                }
            }
            return lines
        }

        fun getAbsoluteText4Kline(rose: String): String {
            if (!CpStringUtil.isNumeric(rose)) return ""
            var lines = rose
            val compareTo = CpBigDecimalUtils.compareTo(rose, "0")
            when (compareTo) {
                -1, 0 -> {
                    lines = rose
                }

                1 -> {
                    lines = "+${rose}"
                }
            }
            return lines
        }


        /**
         * 涨跌幅
         */
        fun getRoseText(rose: Double): String {
            return String.format("%.2f", getRose(rose)) + "%"
        }




        fun getNumRose(rose: String): Boolean {
            if (!CpStringUtil.isNumeric(rose)) return false
            var lines = false
            val compareTo = CpBigDecimalUtils.compareTo(rose, "0")
            when (compareTo) {
                -1 -> {
                    lines = false
                }

                1 -> {
                    lines = true
                }
            }
            return lines
        }

    }


}


