package com.yjkj.chainup.treaty.adapter

import android.util.Log
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.Contract2PublicInfoManager
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.new_version.view.PositionITemView
import com.yjkj.chainup.treaty.bean.ActiveOrderListBean
import com.yjkj.chainup.util.TimeUtil
import com.yjkj.chainup.util.setGoneV3
import org.jetbrains.anko.textColor

/**
 * @Author: Bertking
 * @Date：2019/5/10-17:26 PM
 * @Description: 合约4.0的当前委托
 *
 * PS:For contract,
 *
 * BTCUSDT  :
 * 价格 : USDT  此处 quoteSymbol
 * 价值 : BTC   此处 baseSymbol
 * 余额 : BTC   此处 baseSymbol
 */
open class ContractCurrentEntrustOrderAdapter(data: ArrayList<ActiveOrderListBean.Order>) :
        BaseQuickAdapter<ActiveOrderListBean.Order, BaseViewHolder>(R.layout.item_current_entrust_contract, data) {

    val TAG = ContractCurrentEntrustOrderAdapter::class.java.simpleName

    override fun convert(helper: BaseViewHolder, item: ActiveOrderListBean.Order) {
        if(item?.contractId != Contract2PublicInfoManager.currentContract()?.id){
            return
        }

        /**
         * 市价单隐藏按钮
         * （1：限价单，2：市价单）
         */
        if (item?.type == 2) {
            helper.setGoneV3(R.id.tv_status, false)
        } else {
            helper.setGoneV3(R.id.tv_status, true)
        }

        addChildClickViewIds(R.id.tv_status)

        /**
         * 买卖方
         */
        if (Contract2PublicInfoManager.isPureHoldPosition()) {
            val side = item?.side
            if (side == "BUY") {
                helper?.setText(R.id.tv_side, context.getString(R.string.contract_text_long))
                helper?.getView<TextView>(R.id.tv_side)?.textColor = ColorUtil.getMainColorType()
            } else {
                helper?.setText(R.id.tv_side, context.getString(R.string.contract_text_short))
                helper?.getView<TextView>(R.id.tv_side)?.textColor = ColorUtil.getMainColorType(isRise = false)
            }
        } else {
            val side = item?.side
            val action = item?.action
            if (side == "BUY") {
                if (action == "OPEN") {
                    //做多
                    helper?.setText(R.id.tv_side, context.getString(R.string.contract_action_long))
                } else {
                    //平多
                    helper?.setText(R.id.tv_side, context.getString(R.string.contract_flat_long))
                }
                helper?.getView<TextView>(R.id.tv_side)?.textColor = ColorUtil.getMainColorType()
            } else {
                val text = if (action == "OPEN") {
                    // 做空
                    context.getString(R.string.contract_action_short)
                } else {
                    context.getString(R.string.contract_flat_short)
                }
                helper?.setText(R.id.tv_side, text)
                helper?.getView<TextView>(R.id.tv_side)?.textColor = ColorUtil.getMainColorType(isRise = false)
            }


        }



        helper?.setText(R.id.tv_contract_symbol, item?.symbol)

        val level = if (!Contract2PublicInfoManager.isPureHoldPosition()) {
            " (${item?.leverageLevel}X)"
        } else {
            ""
        }
        helper?.setText(R.id.tv_contract_type, Contract2PublicInfoManager.getContractType(context, item?.contractId) + level)


        Log.d(TAG, "===============item=" + item?.toString())
        /**
         * 委托价格（市价单 的价格为"市价"）
         * （1：限价单，2：市价单）
         */
        val pricePositionITemView = helper?.getView<PositionITemView>(R.id.tv_entrust_price)
        pricePositionITemView?.title = context.getString(R.string.contract_text_trustPrice) + "(${item?.quoteSymbol})"
        if (item?.type == 1) {
            val price = Contract2PublicInfoManager.cutValueByPrecision(item.price.toString(), item.pricePrecision
                    ?: 4)
            pricePositionITemView?.value = price
        } else {
            pricePositionITemView?.value = (context.getString(R.string.contract_action_marketPrice))
        }

        /**
         * 仓位数量（张）
         */
        val pit_position_amount = helper?.getView<PositionITemView>(R.id.tv_position_amount)
        pit_position_amount?.title = context.getString(R.string.contract_text_positionNumber)
        if (item?.side == "BUY") {
            pit_position_amount?.value = "${item.volume.toString()}"
            pit_position_amount?.tailValueColor = ColorUtil.getMainColorType(true)
        } else {
            pit_position_amount?.value = "${item?.volume.toString()}"
            pit_position_amount?.tailValueColor = ColorUtil.getMainColorType(false)
        }

        /**
         * 订单时间
         */
        val dateItemView = helper?.getView<PositionITemView>(R.id.tv_date)
        dateItemView?.title = context.getString(R.string.kline_text_dealTime)
        dateItemView?.value = TimeUtil.instance.getTime(item?.ctime)

        /**
         * 价值
         */
        val valuePositionITemView = helper?.getView<PositionITemView>(R.id.tv_entrust_value)
        valuePositionITemView?.title = context.getString(R.string.contract_text_value) + "(BTC)"
        valuePositionITemView?.value = Contract2PublicInfoManager.cutDespoitByPrecision(item?.orderPriceValue.toString())


        /**
         * 成交均价
         */
        val avgPrice = Contract2PublicInfoManager.cutValueByPrecision(item?.avgPrice.toString(), item?.pricePrecision
                ?: 4)
        val avgPricePositionITemView = helper?.getView<PositionITemView>(R.id.tv_avg_price)
        avgPricePositionITemView?.title = context.getString(R.string.contract_text_dealAverage) + "(${item?.quoteSymbol})"
        avgPricePositionITemView?.value = (avgPrice)


        /**
         * 已成交
         */
        val dealtPositionITemView = helper?.getView<PositionITemView>(R.id.tv_deal)
        dealtPositionITemView?.title = context.getString(R.string.contract_text_dealDone) + "(${context.getString(R.string.contract_text_volumeUnit)})"
        dealtPositionITemView?.value = item?.dealVolume.toString()


        /**
         * 剩余数量
         */
        val remainVolumePositionITemView = helper?.getView<PositionITemView>(R.id.tv_remain_volume)
        remainVolumePositionITemView?.title = context.getString(R.string.contract_text_remaining) + "(${context.getString(R.string.contract_text_volumeUnit)})"
        remainVolumePositionITemView?.value = (item?.undealVolume.toString())


        /**
         * 订单状态：
         * 0：初始状态 1：新订单   2：完全成交  3：部分成交   4：已取消  5：待撤销   6：已废弃  7：部分成交已撤销（0 1 3显示撤销按钮
         */
        val statusView = helper?.getView<SuperTextView>(R.id.tv_status)
        when (item?.status) {
            0, 1, 3 -> {
                statusView?.text = context.getString(R.string.contract_action_cancle)
                statusView?.textColor = ColorUtil.getColor(R.color.main_blue)
            }
            else -> {
                helper?.setTextColor(R.id.tv_status, ColorUtil.getColor(R.color.normal_text_color))
                helper?.setText(R.id.tv_status, item?.statusText)
            }
        }

    }

}
