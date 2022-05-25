package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.*
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2019-09-09-10:46
 * @Description:
 */
class NHistoryEntrustAdapter(datas: ArrayList<JSONObject>) : NBaseAdapter(datas, R.layout.item_history_entrust) {
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {
            val side = optString("side")
            val baseCoin = optString("baseCoin")
            val countCoin = optString("countCoin")
            val volume = optString("volume")
            val price = optString("price")
            val orderType = optString("type")
            val remainVolume = optString("remain_volume")
            val dealVolume = optString("deal_volume")
            val avgPrice = optString("avg_price")
            val id = optString("id")

            helper?.run {
                if (side.equals("BUY", ignoreCase = true)) {
                    setText(R.id.tv_side,  LanguageUtil.getString(context, "otc_text_tradeObjectBuy"))
                    setTextColor(R.id.tv_side, ColorUtil.getMainColorType())
                } else {
                    setText(R.id.tv_side,  LanguageUtil.getString(context, "otc_text_tradeObjectSell"))
                    setTextColor(R.id.tv_side, ColorUtil.getMainColorType(isRise = false))
                }

                /**
                 * 币对
                 */

                val symbol = baseCoin.toLowerCase() + countCoin.toLowerCase()
                val coinMap = NCoinManager.getSymbolObj(symbol)
                setText(R.id.tv_coin_name, NCoinManager.getShowMarket(baseCoin))
                setText(R.id.tv_market_name, "/" +NCoinManager.getShowMarket(countCoin))

                /**
                 * 日期yyyy-MM-dd HH:mm:ss
                 */
                val timeLong = optString("time_long")
                setText(R.id.tv_date, DateUtil.longToString("MM/dd HH:mm:ss", timeLong.toLong()))

                /**
                 * NIT(0,"初始订单，未成交未进入盘口"),
                 * NEW_(1,"新订单，未成交进入盘口"),
                 * FILLED(2,"完全成交"),
                 * PART_FILLED(3,"部分成交"),
                 * CANCELED(4,"已撤单"),
                 * PENDING_CANCEL(5,"待撤单"),
                 * EXPIRED(6,"异常订单");
                 */
                val status = optString("status")
                val statusText = optString("status_text")

                setText(R.id.tv_status, statusText)

//                helper.getView<TextView>(R.id.tv_status).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.enter, 0)
//                helper.getView<TextView>(R.id.tv_status).setOnClickListener {
//                    ArouterUtil.navigation(RoutePath.EntrustDetailActivity, Bundle().apply {
//                        putString("symbol", baseCoin.toLowerCase() + countCoin.toLowerCase())
//                        putString("id", id)
//                        putString("side",side)
//                    })
//                }

                /**
                 * 委托数量
                 */
                setText(R.id.tv_volume, volume.getTradeCoinVolume(coinMap))
                setText(R.id.tv_volume_title,  LanguageUtil.getString(context, "charge_text_volume") + "(" + NCoinManager.getShowMarket(baseCoin) + ")")

                /**
                 * 委托价格
                 * 1限价单2市价单
                 *
                 * 市价单没有委托价格
                 */
                if (orderType == "2") {
                    setText(R.id.tv_price,  LanguageUtil.getString(context, "contract_text_typeMarket"))
                } else {
                    setText(R.id.tv_price, price.getTradeCoinPrice(coinMap))
                }
                setText(R.id.tv_price_title,  LanguageUtil.getString(context, "contract_text_trustPrice") + "(" +NCoinManager.getShowMarket(countCoin) + ")")

                /**
                 * 成交总额
                 */

                setText(R.id.tv_deal_amount, LanguageUtil.getString(context,"noun_order_GMV") + "(" +NCoinManager.getShowMarket(countCoin) + ")")
                setText(R.id.tv_amount, item.optString("deal_money", "").getTradeCoinPriceNumber8())


                /**
                 * 未成交
                 */
                setText(R.id.tv_unsettled, remainVolume.getTradeCoinVolume(coinMap))
                setText(R.id.tv_unsettled_title,  LanguageUtil.getString(context, "transaction_text_orderUnsettled") + "(" + NCoinManager.getShowMarket(baseCoin) + ")")

                /**
                 * 实际成交
                 */
                setText(R.id.tv_deal_volume, dealVolume.getTradeCoinVolume(coinMap))
                setText(R.id.tv_deal_volume_title,  LanguageUtil.getString(context,"contract_text_dealDone") + "(" + NCoinManager.getShowMarket(baseCoin) + ")")

                /**
                 * 成交均价
                 */
                setText(R.id.tv_avg_price, avgPrice.getTradeCoinPrice(coinMap))
                setText(R.id.tv_avg_price_title,  LanguageUtil.getString(context, "contract_text_dealAverage") + "(" +NCoinManager.getShowMarket(countCoin) + ")")

            }


        }


    }
}



