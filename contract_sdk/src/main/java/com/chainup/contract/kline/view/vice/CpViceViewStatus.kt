package com.chainup.contract.kline.view.vice

/**
 * @Author: Bertking
 * @Date：2019/3/19-2:16 PM
 * @Description: 副图指标
 */
enum class CpViceViewStatus(val status: Int) {
    MACD(0),
    KDJ(1),
    RSI(2),
    WR(3),
    NONE(4)
}