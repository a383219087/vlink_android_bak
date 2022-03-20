package com.yjkj.chainup.contract.adapter

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractPosition
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.common.sdk.utlis.TimeFormatUtils
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.extension.showMarginName
import com.yjkj.chainup.contract.extension.showQuoteName
import com.yjkj.chainup.contract.utils.*
import com.yjkj.chainup.contract.widget.ContractUpDownItemLayout
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * 合约历史持仓
 */
class HistoryHoldContractAdapter(data: ArrayList<ContractPosition>) : BaseQuickAdapter<ContractPosition, BaseViewHolder>(R.layout.sl_item_history_hold_contract, data), LoadMoreModule {

    override fun convert(helper: BaseViewHolder, item: ContractPosition) {
        helper?.run {
            val contract: Contract = ContractPublicDataAgent.getContract(item.instrument_id)
                    ?: return
            val dfDefault: DecimalFormat = NumberUtil.getDecimal(-1)
            val dfPrice: DecimalFormat = NumberUtil.getDecimal(contract.price_index - 1)
            val dfValue: DecimalFormat = NumberUtil.getDecimal(contract.value_index)
            //方向
            val way: Int = item.side
            val tvType = getView<TextView>(R.id.tv_type)
            when (way) {
                1 -> {
                    tvType.onLineText("sl_str_hold_buy_open0")
                    tvType.setTextColor(context.resources.getColor(R.color.main_green))
                }
                2 -> {
                    tvType.onLineText("sl_str_hold_sell_open0")
                    tvType.setTextColor(context.resources.getColor(R.color.main_red))
                }
                else -> {
                }
            }
            //合约名称
            setText(R.id.tv_contract_name, contract.symbol)
            setText(R.id.tv_contract_name, contract.symbol)
            //时间
            setText(R.id.tv_time, TimeFormatUtils.timeStampToDate(TimeFormatUtils.getUtcTimeToMillis(item.updated_at), "yyyy-MM-dd  HH:mm:ss"))
            //开仓均价
            var itemOpenPrice = getView<ContractUpDownItemLayout>(R.id.item_open_price)
            itemOpenPrice.content = dfDefault.format(MathHelper.round(item.avg_open_px, contract.price_index))
            itemOpenPrice.title = context.getLineText("sl_str_open_price") + " (${contract.showQuoteName()})"

            //平仓均价
            var itemCostPrice = getView<ContractUpDownItemLayout>(R.id.item_cost_price)
            if (item.isForceClosePosition) {
                //强平仓位不显示具体金额
                itemCostPrice.content = "--"
            } else {
                val costPrice = if (way == 1) {
                    dfPrice.format(MathHelper.round(item.avg_close_px, contract.price_index - 1))
                } else {
                    dfPrice.format(MathHelper.roundUp(item.avg_close_px, contract.price_index - 1))
                }
                itemCostPrice.content = costPrice
            }
            itemCostPrice.title = context.getLineText("sl_str_avg_close_px") + " (${contract.showQuoteName()})"

            val profit = MathHelper.round(item.earnings)
            //已实现盈亏
            var itemGainsBalance = getView<ContractUpDownItemLayout>(R.id.item_gains_balance)
            itemGainsBalance.content = dfDefault.format(MathHelper.round(profit, contract.value_index))
            itemGainsBalance.title = context.getLineText("sl_str_gains_balance") + " (${contract.showMarginName()})"
            itemGainsBalance.contentTextColor = if (profit >= 0) {
                context.resources.getColor(R.color.main_green)
            } else {
                context.resources.getColor(R.color.main_red)
            }
            val tax = (-1) * MathHelper.round(item.tax)
            val taxStr = when {
                tax == 0.0 -> {
                    dfValue.format(0)
                }
                tax > 0 -> {
                    "+" + NumberUtil.getDecimal(contract.value_index, RoundingMode.HALF_DOWN).format(tax)
                }
                else -> {
                    NumberUtil.getDecimal(contract.value_index, RoundingMode.HALF_UP).format(tax)
                }
            }
            val fee = (-1) * (item.open_fee + item.close_fee)
            val feeStr = when {
                fee == 0.0 -> {
                    dfValue.format(0)
                }
                fee > 0 -> {
                    "+" + NumberUtil.getDecimal(contract.value_index, RoundingMode.HALF_DOWN).format(fee)
                }
                else -> {
                    NumberUtil.getDecimal(contract.value_index, RoundingMode.HALF_UP).format(fee)
                }
            }
            val loss = MathHelper.round(item.earnings) - fee - tax
            val lossStr = when {
                loss == 0.0 -> {
                    dfValue.format(0)
                }
                loss > 0 -> {
                    "+" + NumberUtil.getDecimal(contract.value_index, RoundingMode.HALF_DOWN).format(loss)
                }
                else -> {
                    NumberUtil.getDecimal(contract.value_index, RoundingMode.HALF_UP).format(loss)
                }
            }
            itemGainsBalance.setExplainListener(View.OnClickListener {
                ContractDialogUtils.showGainsBalanceTipsDialog(context, taxStr, feeStr, lossStr)
            })
        }
    }


}