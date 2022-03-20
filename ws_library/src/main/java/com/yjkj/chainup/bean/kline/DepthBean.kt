package com.yjkj.chainup.bean.kline


data class DepthItem(val buys: Array<Any> = emptyArray(),
                     val asks: Array<Any> = emptyArray(),
                     var middle: String? = "",
                     var time: Long = 0L) {
    var buyItem: ArrayList<Any> = arrayListOf()
    var sellItem: ArrayList<Any> = arrayListOf()
}

data class SpeedItem(val lineNum: String? = "",
                     var speed: String? = "",
                     var error: Int = 0) {
    
}