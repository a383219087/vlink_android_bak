package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.getTradeCoinPrice
import com.yjkj.chainup.util.getTradeCoinPriceNumber8
import com.yjkj.chainup.util.getTradeCoinVolume
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2020-03-11-00:42
 * @Email buptjinlong@163.com
 * @description
 */
class EntrusDetialsAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_entrus_detials) {

    var countCoin = ""
    var baseCoin = ""

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        var precision = NCoinManager.getCoinShowPrecision(item?.optString("feeCoin", "")
                ?: "")
        helper?.run {

            /**
             * 时间
             */
            setText(R.id.tv_deal_time, LanguageUtil.getString(context, "transaction_text_dealTime"))
            setText(R.id.tv_deal_amount, "${LanguageUtil.getString(context, "transaction_text_dealNum")}(${NCoinManager.getShowMarket(baseCoin)})")
            setText(R.id.tv_time, DateUtil.longToString("MM/dd HH:mm:ss", item?.optLong("time_long")
                    ?: 0L))

            val symbol = baseCoin.toLowerCase() + countCoin.toLowerCase()
            val coinMap = NCoinManager.getSymbolObj(symbol)

            /**
             * 成交价格
             */
            setText(R.id.tv_deal_price, LanguageUtil.getString(context, "transaction_text_dealPrice") + "(${NCoinManager.getShowMarket(countCoin)})")
            setText(R.id.tv_price, item?.optString("price").getTradeCoinPrice(coinMap))

            /**
             * 成交数量
             */
            setText(R.id.tv_amount, item?.optString("volume").getTradeCoinVolume(coinMap))

            /**
             * 手续费
             */

            setText(R.id.tv_deal_fee, "${LanguageUtil.getString(context, "withdraw_text_fee")}(${NCoinManager.getShowMarket(item?.optString("feeCoin", "")?.toUpperCase())})")
            setText(R.id.tv_fee, item?.optString("fee").getTradeCoinPriceNumber8())

        }

    }
}