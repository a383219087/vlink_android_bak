package com.yjkj.chainup.new_version.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.OTCOrderBean
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.StringUtil

/**
 * @Author lianshangljl
 * @Date 2019/4/26-6:39 PM
 * @Email buptjinlong@163.com
 * @description
 */
class NewVersionOTCOrderAdapter(data: ArrayList<OTCOrderBean.Order>) :
        BaseQuickAdapter<OTCOrderBean.Order, BaseViewHolder>(R.layout.item_new_otc_order, data) {


    override fun convert(helper: BaseViewHolder, item: OTCOrderBean.Order) {
        /**
         * 买或者卖
         */
        if (item?.side == "BUY") {
            helper?.setText(R.id.tv_title_pay_type,  LanguageUtil.getString(context, "otc_text_tradeObjectBuy"))
        } else {
            helper?.setText(R.id.tv_title_pay_type,  LanguageUtil.getString(context, "otc_text_tradeObjectSell"))
        }


        if (item?.side == "BUY") {
            helper?.getView<TextView>(R.id.tv_title_pay_type)?.setTextColor(ColorUtil.getColor(R.color.main_green))
        } else {
            helper?.getView<TextView>(R.id.tv_title_pay_type)?.setTextColor(ColorUtil.getColor(R.color.main_red))
        }

        /**
         * 购买币种
         */
        helper?.setText(R.id.tv_payment_coin, NCoinManager.getShowMarket(item?.coinSymbol))
        /**
         * 状态
         */
        helper?.setText(R.id.tv_status, item?.statusText)
        /**
         * 创建时间
         */
        helper?.setText(R.id.tv_pay_time, DateUtil.longToString("yyyy-MM-dd HH:mm:ss", item?.createTime
                ?: 0L))
        /**
         * 单价title 和 content
         */
        helper?.setText(R.id.tv_price_title,  LanguageUtil.getString(context, "otc_text_price") + "(${item?.paySymbol})")

        var paySymbol = item?.paySymbol
        var precision = RateManager.getFiat4Coin(item?.paySymbol)
        var price = item?.price
        if(StringUtil.checkStr(paySymbol)){
            if(StringUtil.checkStr(price) && price!!.contains(paySymbol!!)){
                price = price.replace(paySymbol,"")
            }
        }

        var priceN = BigDecimalUtils.divForDown(price,precision).toPlainString()
        helper?.setText(R.id.tv_price,priceN)
        //helper?.setText(R.id.tv_price, BigDecimalUtils.divForDown(item?.price?.replace(item?.paySymbol, ""), RateManager.getRatesByPayCoin(item?.paySymbol).toInt()).toPlainString())


        /**
         * 数量
         */
        helper?.setText(R.id.tv_otc_amount_title, LanguageUtil.getString(context, "charge_text_volume") + "(${NCoinManager.getShowMarket(item?.coinSymbol)})")
        helper?.setText(R.id.tv_otc_amount, BigDecimalUtils.divForDown(item?.volume, NCoinManager.getCoinShowPrecision(item?.coinSymbol)).toPlainString())

        /**
         * 总额
         */
        helper?.setText(R.id.tv_total_title,  LanguageUtil.getString(context, "otc_text_orderTotal") + "(${item?.paySymbol})")
        var totalPriceN = BigDecimalUtils.divForDown(item?.totalPrice,precision).toPlainString()
        helper?.setText(R.id.tv_total_content, totalPriceN)
        //helper?.setText(R.id.tv_total_content, BigDecimalUtils.divForDown(item?.totalPrice, RateManager.getRatesByPayCoin(item?.paySymbol).toInt()).toPlainString())
    }


}