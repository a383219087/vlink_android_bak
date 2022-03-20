package com.yjkj.chainup.new_version.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.OTCPublicInfoDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.activity.otcTrading.NewVersionPersonInfoActivity
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.OTCAdvertisingListener
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.util.setGoneV3
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/4/3-5:17 PM
 * @Email buptjinlong@163.com
 * @description
 */
class NewOTCTradingAdapter(data: ArrayList<JSONObject>?, var coin: String, var listener: OTCAdvertisingListener) : BaseQuickAdapter<JSONObject,
        BaseViewHolder>(R.layout.item_otc_trading, data), LoadMoreModule {

    val TAG = "NewOTCTradingAdapter"
    fun setCoinName(coinName: String) {
        coin = coinName
    }

    override fun convert(helper: BaseViewHolder, item: JSONObject) {

        var otcNickName = item?.optString("otcNickName") ?: ""
        var side = item?.optString("side") ?: ""
        var paySymbol = item?.optString("paySymbol") ?: ""
        var payCoin = item?.optString("payCoin") ?: ""
        var completeOrders = item?.optInt("completeOrders") ?: 0
        var userId = item?.optInt("userId") ?: 0
        var creditGrade = item?.optDouble("creditGrade") ?: 0.0
        var volumeBalance = item?.optDouble("volumeBalance") ?: 0.0
        var minTrade = item?.optString("minTrade")
        var maxTrade = item?.optString("maxTrade")
        var price = item?.optString("price")

        helper?.setText(R.id.item_otc_value_title, LanguageUtil.getString(context, "charge_text_volume"))
        helper?.setText(R.id.item_otc_limit_title, LanguageUtil.getString(context, "otc_text_priceLimit"))
        helper?.setText(R.id.tv_otc_unit_price, LanguageUtil.getString(context, "otc_text_price"))


        var isOnline = item?.optString("loginStatus", "0") ?: "0"
        if ("1" == isOnline) {
            helper.setGoneV3(R.id.iv_online, true)
        } else {
            helper.setGoneV3(R.id.iv_online, false)
        }

        var paySymbolPresision = RateManager.getFiat4Coin(payCoin)
        LogUtil.d(TAG, "NewOTCTradingAdapter==getFiat4Coin==paySymbol is $paySymbol,paySymbolPresision is $paySymbolPresision，item is $item")
        var minTradeN = BigDecimalUtils.divForDown(minTrade, paySymbolPresision).toPlainString()
        var maxTradeN = BigDecimalUtils.divForDown(maxTrade, paySymbolPresision).toPlainString()
        var priceN = BigDecimalUtils.divForDown(price, paySymbolPresision).toPlainString()

        if (StringUtil.checkStr(otcNickName)) {
            helper?.setText(R.id.fl_header, otcNickName!!.substring(0, 1))
        }
        helper?.setText(R.id.item_otc_user_nick, otcNickName)
        helper?.setText(R.id.item_otc_user_credit, completeOrders.toString())
        helper?.setText(R.id.item_otc_user_volume, "${BigDecimalUtils.divForDown(((1 - creditGrade) * 100).toString(), 0)}%")
        helper?.setText(R.id.item_otc_value_content, BigDecimalUtils.divForDown(BigDecimalUtils.showSNormal(volumeBalance), NCoinManager.getCoinShowPrecision(coin)).toString() + "${NCoinManager.getShowMarket(coin)}")

        helper?.setText(R.id.item_otc_limit_content, "$paySymbol$minTradeN-$paySymbol$maxTradeN")
        helper?.setText(R.id.item_otc_unit_price, "$priceN")


        if (side == "BUY") {
            helper?.getView<CommonlyUsedButton>(R.id.item_otc_buy_submit_btn)?.setContent(LanguageUtil.getString(context, "otc_action_sell"))
        } else {
            helper?.getView<CommonlyUsedButton>(R.id.item_otc_buy_submit_btn)?.setContent(LanguageUtil.getString(context, "otc_action_buy"))
        }

        helper?.getView<CommonlyUsedButton>(R.id.item_otc_buy_submit_btn)?.isEnable(true)
        helper?.getView<CommonlyUsedButton>(R.id.item_otc_buy_submit_btn)?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (listener != null) {
                    listener.setOTCClick(item!!)
                }
            }
        }
        helper?.getView<TextView>(R.id.fl_header)?.setOnClickListener {
            NewVersionPersonInfoActivity.enter(context, userId.toString())
        }
        helper?.getView<TextView>(R.id.item_otc_user_nick)?.setOnClickListener {
            NewVersionPersonInfoActivity.enter(context, userId.toString())
        }


        var adapter = OTCPayIconAdapter(OTCPublicInfoDataService.getInstance().getPaymentsListData(item?.getJSONArray("payments")))
        helper?.getView<RecyclerView>(R.id.item_otc_pay_type_list)?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        helper?.getView<RecyclerView>(R.id.item_otc_pay_type_list)?.adapter = adapter
    }


}