package com.yjkj.chainup.new_version.adapter

import android.util.Log
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.activity.leverage.TradeFragment
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.XCRoundProgressBar
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019-09-09-10:25
 * @Description:
 */
class NCurrentEntrustAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_current_item) {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        var pb_volume = helper.getView<XCRoundProgressBar>(R.id.pb_volume)
        item.run {
            val baseCoin = optString("baseCoin")
            val countCoin = optString("countCoin")
            var source = optString("source")

            val symbol = baseCoin.toLowerCase() + countCoin.toLowerCase()

            if (!PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
                val curSymbol = if (TradeFragment.currentIndex == ParamConstant.LEVER_INDEX_TAB) {
                    PublicInfoDataService.getInstance().currentSymbol4Lever
                } else {
                    PublicInfoDataService.getInstance().currentSymbol
                }
                Log.d(TAG, "symbol:$symbol,CurSymbol:$curSymbol")

                if (symbol != curSymbol) {
                    return
                }
            }
            val coinMap = NCoinManager.getSymbolObj(symbol)

            helper.run {
                /**
                 * 市价单隐藏按钮
                 */
                val orderType = optString("type")
                setGoneV3(R.id.tv_status, "2" != orderType)
                /**
                 * 买卖方
                 */
                val side = optString("side")
                if (side == "BUY") {
                    setText(R.id.tv_side, LanguageUtil.getString(context, "contract_action_buy"))
                } else {
                    setText(R.id.tv_side, LanguageUtil.getString(context, "contract_action_sell"))
                }
                setTextColor(R.id.tv_side, ColorUtil.getMainColorType(isRise = side == "BUY"))


                setText(R.id.tv_coin_name, NCoinManager.getShowMarket(baseCoin))
                setText(R.id.tv_market_name, "/" + NCoinManager.getShowMarket(countCoin))

                /**
                 * 委托的显示时间
                 */
                val createdTime = optString("created_at")
                setText(R.id.tv_date, createdTime)

                /**
                 * 当前委托
                 * 撤单操作
                 */
                val status = optString("status")
                val statusText = optString("status_text")
                when (status) {
                    "0", "1", "3" -> {
                        setText(R.id.tv_status, LanguageUtil.getString(context, "contract_action_cancle"))
                    }

                    "2", "4", "5" -> {
                        setText(R.id.tv_status, statusText)
                    }
                }

                /**
                 * 价格 : 基准货币
                 */
                val price = optString("price")
                setText(R.id.tv_price, price.getTradeCoinPrice(coinMap))
                setText(R.id.tv_price_title, LanguageUtil.getString(context, "contract_text_price") + "(${NCoinManager.getShowMarket(countCoin)})")

                /**
                 * 委托量
                 */
                val volume = optString("volume")
                setText(R.id.tv_volume_title, LanguageUtil.getString(context, "charge_text_volume") + "(${NCoinManager.getShowMarket(baseCoin)})")
                setText(R.id.tv_volume, volume.getTradeCoinVolume(coinMap))

                /**
                 * 实际成交量
                 */
                val dealVolume = optString("deal_volume")
                setText(R.id.tv_deal_title, LanguageUtil.getString(context, "transaction_text_tradeValue") + "(${NCoinManager.getShowMarket(baseCoin)})")
                setText(R.id.tv_deal, dealVolume.getTradeCoinVolume(coinMap))
                var staText = getView<SuperTextView>(R.id.tv_status)
                if (source == "QUANT-GRID") { //staText.solid = ContextCompat.getColor(context, R.color.bg_grid_order)
                    staText.setTextColor(ContextCompat.getColor(context, R.color.text_color))
                } else { //staText.solid = ContextCompat.getColor(context, R.color.tabbar_color)
                    staText.setTextColor(ContextCompat.getColor(context, R.color.text_color))
                }
                val dealDouble = dealVolume.getTradeCoinVolume(coinMap).toDouble()
                val volumeDouble = volume.getTradeCoinVolume(coinMap).toDouble()
                var pro = dealDouble * 100 / volumeDouble
                pb_volume.progress = pro.toInt()
            }
        }

    }

}