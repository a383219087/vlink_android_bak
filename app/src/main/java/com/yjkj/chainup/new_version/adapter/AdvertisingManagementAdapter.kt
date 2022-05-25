package com.yjkj.chainup.new_version.adapter

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.OTCPublicInfoDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.util.BigDecimalUtils
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-10-14-17:07
 * @Email buptjinlong@163.com
 * @description
 */
class AdvertisingManagementAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_advertising_management_adapter, data) {


    override fun convert(helper: BaseViewHolder, item: JSONObject) {

        if (item?.optString("side") == "BUY") {
            helper?.setTextColor(R.id.tv_advertising_direction, ContextCompat.getColor(context, R.color.new_green))
            helper?.setText(R.id.tv_advertising_direction,  LanguageUtil.getString(context, "otc_text_tradeObjectBuy"))
        } else {
            helper?.setTextColor(R.id.tv_advertising_direction, ContextCompat.getColor(context, R.color.new_red))
            helper?.setText(R.id.tv_advertising_direction,  LanguageUtil.getString(context, "otc_text_tradeObjectSell"))
        }
        helper?.setText(R.id.tv_advertising_coin, NCoinManager.getShowMarket(item?.optString("coin")
                ?: ""))

        helper?.setText(R.id.tv_pricing, LanguageUtil.getString(context,"otc_setPrice_method"))
        helper?.setText(R.id.cub_confirm, LanguageUtil.getString(context,"otc_text_adLook"))



        /**
         * 定价方式
         */
        if (item?.optString("priceRateType") == "0") {
            helper?.setText(R.id.tv_pricing_content,  LanguageUtil.getString(context,"otc_custom_price"))
        } else {
            helper?.setText(R.id.tv_pricing_content,  LanguageUtil.getString(context, "otc_text_marketPrice"))
        }

        /**
         * 价格
         */
        var payCoin = item?.optString("payCoin")?:"CNY"
        var precision = RateManager.getFiat4Coin(payCoin)
        var price = item?.optString("price")
        var priceN = BigDecimalUtils.divForDown(price,precision).toPlainString()
        helper?.setText(R.id.tv_price, "${ LanguageUtil.getString(context, "otc_text_price")}($payCoin)")
        helper?.setText(R.id.tv_price_content, priceN)

        /**
         * 剩余数量
         */
        helper?.setText(R.id.tv_remaining_text,  LanguageUtil.getString(context, "otc_text_remainingNum") + "(${NCoinManager.getShowMarket(item?.optString("coin")
                ?: "")})")

        helper?.setText(R.id.tv_remaining_content, BigDecimalUtils.intercept(item?.optString("leftVolume"), NCoinManager.getCoinShowPrecision(item?.optString("coin")
                ?: "").toInt()).toPlainString())
        /**
         * 交易限额
         */
        var minTrade = item?.optString("minTrade")
        var maxTrade = item?.optString("maxTrade")
        var minTradeN = BigDecimalUtils.divForDown(minTrade,precision).toPlainString()
        var maxTradeN = BigDecimalUtils.divForDown(maxTrade,precision).toPlainString()

        helper?.setText(R.id.trading_limits,  LanguageUtil.getString(context, "otc_text_tradingLimits") + "($payCoin)")
        helper?.setText(R.id.trading_limits_content, "${minTradeN}-${maxTradeN}")

        /**
         * 发布状态
         */
        when (item?.optInt("status")) {
            1 -> {
                helper?.setText(R.id.tv_post_status,  LanguageUtil.getString(context, "otc_text_releaseing"))
            }
            2 -> {
                helper?.setText(R.id.tv_post_status,  LanguageUtil.getString(context, "otc_text_trading"))
            }
            3 -> {
                helper?.setText(R.id.tv_post_status,  LanguageUtil.getString(context, "otc_text_expired"))
            }
            4 -> {
                helper?.setText(R.id.tv_post_status,  LanguageUtil.getString(context, "otc_have_closed"))
            }
            5 -> {
                helper?.setText(R.id.tv_post_status,  LanguageUtil.getString(context, "otc_text_expired"))
            }
        }

        addChildClickViewIds(R.id.cub_confirm)
        var adapter = OTCPayIconAdapter(OTCPublicInfoDataService.getInstance().getPaymentsListData(item?.getJSONArray("payments")))
        helper?.getView<RecyclerView>(R.id.recycler_view)?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        helper?.getView<RecyclerView>(R.id.recycler_view)?.adapter = adapter

    }



}