package com.yjkj.chainup.new_version.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.PersonAdsBean
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.NewOTCAdsListener
import com.yjkj.chainup.util.BigDecimalUtils

/**
 * @Author lianshangljl
 * @Date 2019/5/23-11:40 AM
 * @Email buptjinlong@163.com
 * @description
 */

class NewOTCPersonAdapter(data: ArrayList<PersonAdsBean.AdList>?,
                          var listener: NewOTCAdsListener) : BaseQuickAdapter<PersonAdsBean.AdList,
        BaseViewHolder>(R.layout.item_new_otc_person, data) {


    override fun convert(helper: BaseViewHolder, item: PersonAdsBean.AdList) {

        helper?.setText(R.id.item_otc_user_nick, item?.coin)
        /**
         * 数量
         */
        helper?.setText(R.id.item_otc_value_content, BigDecimalUtils.showSNormal(item?.volume.toString()) + " ${NCoinManager.getShowMarket(item?.coin)}")
        /**
         * 限额
         */
        var minTrade = item?.minTrade
        var maxTrade = item?.maxTrade
        var payCoin = item?.payCoin ?: ""
        var precision = RateManager.getFiat4Coin(payCoin)
        var minTradeN = BigDecimalUtils.divForDown(minTrade, precision).toPlainString()
        var maxTradeN = BigDecimalUtils.divForDown(maxTrade, precision).toPlainString()

        helper?.setText(R.id.item_otc_limit_content, "$payCoin$minTradeN-$maxTradeN")
        /**
         * 单价
         */
        var priceN = BigDecimalUtils.divForDown(item?.price, precision).toPlainString()
        helper?.setText(R.id.item_otc_unit_price, "$priceN")
        if (item?.side == "BUY" || item?.side == "buy") {
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

        var adapter = NewOTCPayIconAdapter(item?.payments ?: arrayListOf())
        helper?.getView<RecyclerView>(R.id.item_otc_pay_type_list)?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        helper?.getView<RecyclerView>(R.id.item_otc_pay_type_list)?.adapter = adapter
        helper?.apply {
            setText(R.id.item_otc_value_title,getStringContent("charge_text_volume",context))
            setText(R.id.tv_otc_unit_price,getStringContent("otc_text_price",context))
            setText(R.id.item_otc_limit_title,getStringContent("otc_text_priceLimit",context))
        }
    }

    fun getStringContent(contentId: String, context: Context): String {
        return LanguageUtil.getString(context, contentId)
    }

}