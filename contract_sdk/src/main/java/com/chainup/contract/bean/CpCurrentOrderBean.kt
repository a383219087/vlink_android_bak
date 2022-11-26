package com.chainup.contract.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

data class CpCurrentOrderBean(
        val avgPrice: String,
        val ctime: String,
        val dealVolume: String?,
        val id: String,
        val contractId: String,
        val open: String,
        val positionType: String,
        val price: String,
        val pricePrecision: Int,
        val triggerPrice: String,
        val expireTime: String,
        val base: String,
        val quote: String,
        val realizedAmount: String,
        val side: String,
        val status: String,
        val symbol: String,
        val type: String,
        val timeInForce: String,
        val fee: String,
        val mtime: String,
        val orderBalance: String,
        val liqPositionMsg: String,
        val triggerType: Int,
        val memo: Int,
        val volume: String,
        val traderName: String?="",
        val otoOrder: otoOrder?,
        var layoutType: Int
) : Serializable, MultiItemEntity {
    var isPlan: Boolean = false
    override val itemType: Int
        get() = layoutType
}

data class otoOrder(
        val stopLossPrice: String?="--",
        val stopLossStatus: Boolean,
        val stopLossTrigger: String?="--",
        val takerProfitPrice: String?="--",
        val takerProfitStatus: Boolean,
        val takerProfitTrigger: String?="--"
): Serializable