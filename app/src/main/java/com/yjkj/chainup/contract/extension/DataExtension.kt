package com.yjkj.chainup.contract.extension

import com.contract.sdk.data.Contract
import com.yjkj.chainup.manager.NCoinManager

/**
 * @author ZhongWei
 * @time 2020/8/12 17:04
 * @description 数据处理
 **/
fun Contract.showMarginName(): String {
    return NCoinManager.getShowMarket(margin_coin)
}

fun Contract.showPriceName(): String {
    return NCoinManager.getShowMarket(price_coin)
}

fun Contract.showBaseName(): String {
    return NCoinManager.getShowMarket(base_coin)
}

fun Contract.showQuoteName(): String {
    return NCoinManager.getShowMarket(quote_coin)
}