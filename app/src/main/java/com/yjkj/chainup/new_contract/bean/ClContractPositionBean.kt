package com.yjkj.chainup.new_contract.bean

import java.io.Serializable

data class ClContractPositionBean(
    val canCloseVolume: String,
    val canSubMarginAmount: String,
    val canUseAmount: String,
    val contractName: String,
    val indexPrice: String,
    val isolatedMargin: String,
    val marginRate: String,
    val openAvgPrice: String,
    val orderSide: String,
    val positionType: Int,
    val contractId: Int,
    val id: Int,
    val leverageLevel: Int,
    val positionVolume: String,
    val realizedAmount: String,
    val unRealizedAmount: String,
    val reducePrice: String,
    val returnRate: String,
    val symbol: String,
    val keepRate: String,
    val maxFeeRate: String,
    val openRealizedAmount: String,
    val holdAmount: String,
    val totalMargin: String,
    val tradeFee: String,
    val capitalFee: String,
    val closeProfit: String,
    val shareAmount: String,
    val positionBalance: String,
    val profitRealizedAmount: String
):Serializable