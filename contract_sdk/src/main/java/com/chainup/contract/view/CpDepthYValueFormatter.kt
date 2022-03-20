package com.chainup.contract.view

import android.util.Log
import com.chainup.contract.utils.CpBigDecimalUtils
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * @Author: Bertking
 * @Date：2019-07-29-15:47
 * @Description: 自定义的Y轴显示格式
 */
class CpDepthYValueFormatter : ValueFormatter() {
    val TAG = CpDepthYValueFormatter::class.java.simpleName

    override fun getFormattedValue(value: Float): String {
        Log.d(TAG, "======value:$value===")
        if (value == 0f) {
            return ""
        }
        return CpBigDecimalUtils.showDepthVolume(value.toString())
    }
}