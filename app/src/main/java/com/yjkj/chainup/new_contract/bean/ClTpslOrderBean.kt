package com.yjkj.chainup.new_contract.bean

data class ClTpslOrderBean(
        var triggerType: String, //止盈止损订单类型(1止损， 2 止盈)固定枚举
        var price: String,//下单价格(市价单传0)
        var volume: String,//下单数量(开仓市价单：金额)
        var triggerPrice: String,//触发价格
        var type :String,// 订单类型(1 limit， 2 market)
        var expiredTime :Int// 有效时间(1, 7, 14, 30)天, 固定枚举;
)