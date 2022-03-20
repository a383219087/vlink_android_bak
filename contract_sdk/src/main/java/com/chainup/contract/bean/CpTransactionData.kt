package com.chainup.contract.bean

import com.google.gson.JsonArray

data class CpTransactionData(val channel: String = "", val tick: Tick?,
                             val ts: String = "") {
    data class Tick(val buys: MutableList<JsonArray> = mutableListOf(), val asks: MutableList<JsonArray> = mutableListOf())
}
