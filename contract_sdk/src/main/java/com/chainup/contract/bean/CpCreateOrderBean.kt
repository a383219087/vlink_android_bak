package com.yjkj.chainup.new_contract.bean

import java.io.Serializable

data class CpCreateOrderBean(
        var contractId: Int,
        var positionType: String,
        var open: String,
        var side: String,
        var type: Int,
        var leverageLevel: Int,
        var price: String,
        var volume: String,
        var isConditionOrder: Boolean,
        var triggerPrice: String,
        var expireTime: Int,
        var isOto: Boolean,
        var takerProfitTrigger: String,
        var stopLossTrigger: String
) : Serializable