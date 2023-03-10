package com.yjkj.chainup.new_version.view.depth

import android.util.Log
import com.github.mikephil.charting.formatter.ValueFormatter
import com.yjkj.chainup.util.BigDecimalUtils

/**
 * @Author: Bertking
 * @Date：2019-07-29-15:47
 * @Description: 自定义的Y轴显示格式
 */
class DepthYValueFormatter : ValueFormatter() {
    val TAG = DepthYValueFormatter::class.java.simpleName

    override fun getFormattedValue(value: Float): String {
        Log.d(TAG, "======value:$value===")
        if (value == 0f) {
            return ""
        }
        return BigDecimalUtils.showDepthVolume(value.toString())
    }
}