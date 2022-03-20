package com.chainup.contract.bean

/**
 * @Author: Bertking
 * @Date：2019-05-22-11:20
 * @Description:
 */
class CpFlagBean(var isContract: Boolean, var contractId: String, var symbol: String, var baseSymbol: String, var quotesSymbol: String, var pricePrecision: Int = 0, var volumePrecision: Int = 0, var mMultiplier: String = "0", var coUnit: Int = 0) {
    override fun toString(): String {
        return "FlagBean(isContract=$isContract, symbol='$symbol', baseSymbol='$baseSymbol', quotesSymbol='$quotesSymbol')"
    }

}