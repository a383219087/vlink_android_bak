package com.yjkj.chainup.new_contract.bean

import java.io.Serializable

data class ClCurrentOrderBean(
    val avgPrice: String,
    val ctime: String,
    val dealVolume: String,
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
    val memo: Int,
    val volume: String
):Serializable{
    var isPlan: Boolean=false
}