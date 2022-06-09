package com.yjkj.chainup.bean

import java.io.Serializable

data class BuyInfo(

    val coins: Map<String,String?>?=null,
    var fiat: String="",
    var logo: String=""



) : Serializable


data class PayTypes(
    val logoUrl: String?,
    val methodId: Any?,
    val paymentMethods: List<PaymentMethod>?,
    val supplier: String?
): Serializable

data class PaymentMethod(
    val description: Any?,
    val logoUrl: String?,
    val maxAmount: String?,
    val maxTime: Any?,
    val methodId: String?,
    val methodName: String?,
    val minAmount: String?,
    val minTime: Any?,
    val price: String?,
    val supportAgent: Any?


) : Serializable


data class CreateOrderInfo(
    val checkoutUrl: String

): Serializable








