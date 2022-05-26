package com.yjkj.chainup.new_version.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.trade.Order
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.BigDecimalUtils
import org.jetbrains.anko.textColor
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2019/4/8-8:33 PM
 * @Description:
 */
class HistoryEntrustAdapter(data: ArrayList<Order>) : BaseQuickAdapter<Order, BaseViewHolder>(R.layout.item_history_entrust, data) {

    val TAG = HistoryEntrustAdapter::class.java.simpleName

    override fun convert(helper: BaseViewHolder, order: Order) {

        val side = order.side
        if (side.equals("BUY", ignoreCase = true)) {
            helper.setText(R.id.tv_side,  LanguageUtil.getString(context, "otc_text_tradeObjectBuy"))
            helper.getView<TextView>(R.id.tv_side).textColor = ColorUtil.getMainColorType()
        } else {
            helper.setText(R.id.tv_side,  LanguageUtil.getString(context, "otc_text_tradeObjectSell"))
            helper.getView<TextView>(R.id.tv_side).textColor = ColorUtil.getMainColorType(isRise = false)
        }

        /**
         * 币对
         */
        var pair = NCoinManager.getShowName(order.baseCoin ?: "", order.countCoin ?: "")

        helper?.setText(R.id.tv_coin_name, pair.first)
        helper?.setText(R.id.tv_market_name, "/" + pair.second)


        /**
         * 日期yyyy-MM-dd HH:mm:ss
         */
        helper.setText(R.id.tv_date, DateUtil.longToString("yyyy/MM/dd HH:mm", order.timeLong))


        /**
         * NIT(0,"初始订单，未成交未进入盘口"),
         * NEW_(1,"新订单，未成交进入盘口"),
         * FILLED(2,"完全成交"),
         * PART_FILLED(3,"部分成交"),
         * CANCELED(4,"已撤单"),
         * PENDING_CANCEL(5,"待撤单"),
         * EXPIRED(6,"异常订单");
         */


        val status = order.status
        helper.setText(R.id.tv_status, order.statusText)
        when (status) {
            0, 1, 3 -> {
            }

            2 -> {
                /**
                 * 完全成交 进入详情
                 */
//                helper.getView<TextView>(R.id.tv_status).setOnClickListener { v -> OrderDetailsActivity.enter2(mContext, order) }
                helper.setText(R.id.tv_status,  LanguageUtil.getString(context, "contract_text_orderComplete"))
//                helper.getView<TextView>(R.id.tv_status).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_white_arrow, 0)
            }

            4 -> helper.setText(R.id.tv_status,  LanguageUtil.getString(context, "contract_text_orderCancel"))
            else -> {
            }
        }


        /**
         * 委托数量
         */
        helper.setText(R.id.tv_volume, BigDecimalUtils.showSNormal(order.volume))
        helper.setText(R.id.tv_volume_title,  LanguageUtil.getString(context, "charge_text_volume") + "(" + pair.first + ")")

        /**
         * 委托价格
         * 1限价单2市价单
         *
         * 市价单没有委托价格
         */
        if (order.type == 2) {
            helper.setText(R.id.tv_price,  LanguageUtil.getString(context, "contract_text_typeMarket"))
        } else {
            helper.setText(R.id.tv_price, BigDecimalUtils.showSNormal(order.price))
        }
        helper.setText(R.id.tv_price_title,  LanguageUtil.getString(context, "contract_text_trustPrice") + "(" + pair.second + ")")

        /**
         * 未成交
         */
        helper.setText(R.id.tv_unsettled, BigDecimalUtils.showSNormal(order.remainVolume))
        helper.setText(R.id.tv_unsettled_title,  LanguageUtil.getString(context, "transaction_text_orderUnsettled") + "(" + pair.first + ")")

        /**
         * 实际成交
         */
        helper.setText(R.id.tv_deal_volume, BigDecimalUtils.showSNormal(order.dealVolume))
        helper.setText(R.id.tv_deal_volume_title,  LanguageUtil.getString(context, "transaction_text_tradeValue") + "(" + pair.first + ")")

        /**
         * 成交均价
         */
        helper.setText(R.id.tv_avg_price, BigDecimalUtils.showSNormal(order.avgPrice))
        helper.setText(R.id.tv_avg_price_title,  LanguageUtil.getString(context, "contract_text_dealAverage") + "(" + pair.second + ")")
    }
}
