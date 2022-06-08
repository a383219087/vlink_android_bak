package com.yjkj.chainup.bean

import java.io.Serializable

data class BuyInfo(

    val coins: Map<String,String>?=null,
    val fiat: String,
    val logo: String



) : Serializable








