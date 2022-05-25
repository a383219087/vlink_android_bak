package com.yjkj.chainup.new_version.activity.grid.adapter

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.*
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2021/2/4-3:24 PM
 * @Email buptjinlong@163.com
 * @description
 */
class AlreadyPerformedAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject,
        BaseViewHolder>(R.layout.item_already_perform_adapter, data), LoadMoreModule {
    var tagList: ArrayList<Int> = arrayListOf()


    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {
            var buyTime = DateUtils.getYearMonthDayHourMinSecond(optLong("buyTime"))

            helper?.setText(R.id.tv_fiat_type, buyTime)

            var symbol = optString("symbol")
            var base = symbol.split("/")[0]
            var coin = symbol.split("/")[1]

            var name = NCoinManager.getMarket4Name((base + coin).toLowerCase())
            var pricePrecision = name.optInt("price")
            var volumePrecision = name.optInt("volume")
            val strategy = optInt("strategyStatus")
            var buyOrder = optJSONObject("buyOrder")
            var sellOrder = optJSONObject("sellOrder")
            var profit = optString("profit", "--")
            var sellList = sellOrder != null && sellOrder.length() > 0
            var buyList = buyOrder != null && buyOrder.length() > 0

//            profit = strategy.getQuaintEndStatus(context, sellList, buyList, BigDecimalUtils.divForDown(profit, pricePrecision).toPlainString())
//            helper.setText(R.id.tv_coin_trading, profit)


            if (StringUtil.checkStr(profit)) {
                helper?.setText(R.id.tv_coin_trading, BigDecimalUtils.divForDown(profit, pricePrecision).toPlainString())
            } else {
                helper?.setText(R.id.tv_coin_trading, "--")
            }


            helper.setTextColor(R.id.tv_coin_trading, strategy.getQuaintEndStatusColor(context, sellList, profit))

            helper?.setText(R.id.tv_clinch_deal_price_title, "${LanguageUtil.getString(context, "contract_text_dealAverage")}(${NCoinManager.getShowMarket(coin)})")
            helper?.setText(R.id.tv_clinch_deal_price_title_sell, "${LanguageUtil.getString(context, "contract_text_dealAverage")}(${NCoinManager.getShowMarket(coin)})")


            helper?.setText(R.id.tv_clinch_deal_quantity_title, "${LanguageUtil.getString(context, "transaction_text_dealNum")}(${NCoinManager.getShowMarket(base)})")
            helper?.setText(R.id.tv_clinch_deal_quantity_title_sell, "${LanguageUtil.getString(context, "transaction_text_dealNum")}(${NCoinManager.getShowMarket(base)})")

            helper?.setText(R.id.tv_clinch_deal_amount_amount, "${LanguageUtil.getString(context, "sl_str_deal_money")}(${NCoinManager.getShowMarket(coin)})")
            helper?.setText(R.id.tv_clinch_deal_amount_amount_sell, "${LanguageUtil.getString(context, "sl_str_deal_money")}(${NCoinManager.getShowMarket(coin)})")


            if (buyOrder != null && buyOrder.length() > 0) {
                var time4Buy = DateUtils.getYearMonthDayHourMinSecond(buyOrder.optLong("orderCtime"))
                helper?.setText(R.id.tv_clinch_deal_price, BigDecimalUtils.divForDown(buyOrder.optString("avgPrice"), pricePrecision).toPlainString())
                helper?.setText(R.id.tv_clinch_deal_quantity, BigDecimalUtils.divForDown(buyOrder.optString("dealVolume"), volumePrecision).toPlainString())
                helper?.setText(R.id.tv_clinch_deal_amount, BigDecimalUtils.divForDown(buyOrder.optString("dealMoney"), 8).toPlainString())
                helper?.setText(R.id.tv_time, time4Buy)
                helper?.setGone(R.id.rl_buy_title_layout, false)
                helper?.setGone(R.id.ll_buy_layout, false)
            } else {
                helper?.setGone(R.id.rl_buy_title_layout, true)
                helper?.setGone(R.id.ll_buy_layout, true)

            }
            if (sellOrder != null && sellOrder.length() > 0) {
                var time4sell = DateUtils.getYearMonthDayHourMinSecond(sellOrder.optLong("orderCtime"))
                helper?.setText(R.id.tv_clinch_deal_price_sell, BigDecimalUtils.divForDown(sellOrder.optString("avgPrice"), pricePrecision).toPlainString())
                helper?.setText(R.id.tv_clinch_deal_quantity_sell, BigDecimalUtils.divForDown(sellOrder.optString("dealVolume"), volumePrecision).toPlainString())
                helper?.setText(R.id.tv_clinch_deal_amount_sell, BigDecimalUtils.divForDown(sellOrder.optString("dealMoney"), 8).toPlainString())
                helper?.setText(R.id.tv_time_sell, time4sell)
                helper?.setGone(R.id.ll_sell_layout, false)
                helper?.setGone(R.id.rl_sell_title_layout, false)
            } else {
                helper?.setGone(R.id.ll_sell_layout, true)
                helper?.setGone(R.id.rl_sell_title_layout, true)
            }
        }
        if (tagList.contains(helper?.adapterPosition)) {
            helper?.getView<LinearLayout>(R.id.rl_main_layout)?.visibility = View.VISIBLE
            helper?.getView<ImageView>(R.id.iv_change_fiat).setImageResource(R.drawable.collapse)
        } else {
            helper?.getView<LinearLayout>(R.id.rl_main_layout)?.visibility = View.GONE
            helper?.getView<ImageView>(R.id.iv_change_fiat).setImageResource(R.drawable.dropdown)
        }

        helper?.getView<RelativeLayout>(R.id.rl_fiat_layout).setOnClickListener {
            if (tagList.contains(helper?.adapterPosition)) {
                tagList.remove(helper?.adapterPosition)
                helper?.getView<LinearLayout>(R.id.rl_main_layout)?.visibility = View.GONE
                helper?.getView<ImageView>(R.id.iv_change_fiat).setImageResource(R.drawable.dropdown)
            } else {
                tagList.add(helper?.adapterPosition)
                helper?.getView<LinearLayout>(R.id.rl_main_layout)?.visibility = View.VISIBLE
                helper?.getView<ImageView>(R.id.iv_change_fiat).setImageResource(R.drawable.collapse)
            }
        }
    }

}