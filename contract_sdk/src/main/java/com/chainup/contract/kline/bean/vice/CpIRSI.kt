package com.yjkj.chainup.new_version.kline.bean.vice

import com.yjkj.chainup.new_version.kline.bean.CpIndex


/**
 * @Author: Bertking
 * @Date：2019/2/25-10:48 AM
 * @Description:IRSI(相对强弱)指标
 * https://baike.baidu.com/item/IRSI%E6%8C%87%E6%A0%87
 */
interface CpIRSI : CpIndex {
    var RSI: Float
}