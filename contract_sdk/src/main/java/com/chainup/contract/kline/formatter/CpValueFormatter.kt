package com.chainup.contract.kline.formatter

import com.chainup.contract.utils.CpBigDecimalUtils
import com.yjkj.chainup.new_version.kline.base.CpIValueFormatter

/**
 * @Author: Bertking
 * @Date：2019/3/11-11:16 AM
 * @Description:
 */
class CpValueFormatter : CpIValueFormatter {
    override fun format(value: Float): String {
        return CpBigDecimalUtils.showSNormal(value.toString())
    }
}