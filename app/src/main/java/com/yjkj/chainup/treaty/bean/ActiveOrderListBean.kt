package com.yjkj.chainup.treaty.bean

import com.google.gson.annotations.SerializedName

data class ActiveOrderListBean(
        @SerializedName("orderCount")
        val count: Int? = 0, // 2
        @SerializedName("orderList")
        val orderList: ArrayList<Order?>? = arrayListOf()
) {
    data class Order(
            @SerializedName("avgPrice")
            val avgPrice: Double? = 0.0, // 0E-16
            @SerializedName("baseSymbol")
            val baseSymbol: String? = "", // BTC
            @SerializedName("contractId")
            val contractId: Int? = 0, // 19
            @SerializedName("contractName")
            val contractName: String? = "", // BitCoin
            @SerializedName("ctime")
            val ctime: String? = "", // 2019-01-21 20:43:44
            @SerializedName("ctimeStr")
            val ctimeStr: String? = "", // 1548074580000
            @SerializedName("dealVolume")
            val dealVolume: String? = "0", // 0
            @SerializedName("multiplier")
            val multiplier: Int? = 0, // 1
            @SerializedName("leverageLevel")
            val leverageLevel: Int? = 0, // 1
            @SerializedName("orderId")
            val orderId: Int? = 0, // 7554
            @SerializedName("orderPriceValue")
            val orderPriceValue: String? = "", // 0.00028113
            @SerializedName("price")
            val price: String? = "", // 3557.0500000000000000
            @SerializedName("pricePrecision")
            val pricePrecision: Int? = 0, // 2
            @SerializedName("quoteSymbol")
            val quoteSymbol: String? = "", // USD
            @SerializedName("side")
            val side: String? = "", // BUY
            @SerializedName("action")
            val action: String? = "", // OPEM
            @SerializedName("status")
            val status: Int? = 0, // 0
            @SerializedName("statusText")
            val statusText: String? = "", //
            @SerializedName("symbol")
            val symbol: String? = "", // E_BTCUSD
            @SerializedName("type")
            val type: Int? = 0, // 1
            @SerializedName("undealVolume")
            val undealVolume: Int? = 0, // 1
            @SerializedName("valuePrecision")
            val valuePrecision: Int? = 0, // 4
            @SerializedName("volume")
            val volume: Int? = 0 // 1
    )
}