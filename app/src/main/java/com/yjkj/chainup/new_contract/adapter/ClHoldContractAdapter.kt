package com.yjkj.chainup.new_contract.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.bean.CpContractPositionBean
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.util.BigDecimalUtils

class ClHoldContractAdapter(data: ArrayList<CpContractPositionBean>) : BaseQuickAdapter<CpContractPositionBean, BaseViewHolder>(R.layout.cl_item_hold_contract, data) {


    override fun convert(helper: BaseViewHolder, item: CpContractPositionBean) {
        val mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(context, item.contractId)

        val mMarginCoinPrecision = LogicContractSetting.getContractMarginCoinPrecisionById(context, item.contractId)

        val mMultiplierCoin = LogicContractSetting.getContractMultiplierCoinPrecisionById(context, item.contractId)

        val mMultiplierPrecision = LogicContractSetting.getContractMultiplierPrecisionById(context, item.contractId)

        val mMultiplier = LogicContractSetting.getContractMultiplierById(context, item.contractId)
        helper?.run {

            when (item.orderSide) {
                "BUY" -> {
                    setText(R.id.tv_type, context.getLineText("cl_str_hold_buy_open0"))
                    setTextColor(R.id.tv_type, context.resources.getColor(R.color.main_green))
                    setTextColor(R.id.tv_profit_loss_value, context.resources.getColor(R.color.main_green))
                    setTextColor(R.id.tv_floating_gains_value, context.resources.getColor(R.color.main_green))
                }
                "SELL" -> {
                    setText(R.id.tv_type, context.getLineText("cl_str_hold_sell_open0"))
                    setTextColor(R.id.tv_type, context.resources.getColor(R.color.main_red))

                    setTextColor(R.id.tv_profit_loss_value, context.resources.getColor(R.color.main_red))
                    setTextColor(R.id.tv_floating_gains_value, context.resources.getColor(R.color.main_red))
                }
                else -> {
                }
            }
            if (BigDecimalUtils.compareTo(BigDecimalUtils.showSNormal(item.openRealizedAmount, mMarginCoinPrecision),"0")==1){
                setTextColor(R.id.tv_profit_loss_value, context.resources.getColor(R.color.main_green))
                setTextColor(R.id.tv_floating_gains_value, context.resources.getColor(R.color.main_green))
            }else{
                setTextColor(R.id.tv_profit_loss_value, context.resources.getColor(R.color.main_red))
                setTextColor(R.id.tv_floating_gains_value, context.resources.getColor(R.color.main_red))
            }

            //只有逐仓才有调整保证金，全仓没有调整保证金
            when (item.positionType) {
                1 -> {
                    setGone(R.id.tv_adjust_margins, true)
                    setText(R.id.tv_open_type, context.getLineText("sl_str_full_position")+" "+item.leverageLevel.toString() + "X")
                }
                2 -> {
                    setGone(R.id.tv_adjust_margins, false)
                    setText(R.id.tv_open_type, context.getLineText("sl_str_gradually_position")+" "+item.leverageLevel.toString() + "X")
                }
                else -> {
                }
            }
            var symbolName = LogicContractSetting.getContractShowNameById(context, item.contractId)
            setText(R.id.tv_contract_name, symbolName)
            //开仓均价
            setText(R.id.tv_open_price_value, BigDecimalUtils.showSNormal(item.openAvgPrice, mPricePrecision))
            //盈亏
            setText(R.id.tv_profit_loss_value, BigDecimalUtils.showSNormal(item.openRealizedAmount, mMarginCoinPrecision))
            //盈亏Key
            setText(R.id.tv_floating_gains_balance_key, context.getString(R.string.cl_orderlist_c_columns_8) + "(" + LogicContractSetting.getContractMarginCoinById(context, item.contractId) + ")")
            //预估强平价
            if (BigDecimalUtils.compareTo(item.reducePrice, "0") == 1) {
                setText(R.id.tv_forced_close_price_value, BigDecimalUtils.showSNormal(item.reducePrice, mPricePrecision))
            } else {
                setText(R.id.tv_forced_close_price_value, "--")
            }
            //回报率
            setText(R.id.tv_floating_gains_value, NumberUtil.getDecimal(2).format(MathHelper.round(MathHelper.mul(item.returnRate.toString(), "100"), 2)).toString() + "%")
            //总持仓
            setText(R.id.tv_total_position_value, if (LogicContractSetting.getContractUint(context) == 0) item.positionVolume else BigDecimalUtils.mulStr(item.positionVolume,mMultiplier, mMultiplierPrecision))
            //总持仓Key
            setText(R.id.tv_total_position_key, if (LogicContractSetting.getContractUint(context) == 0) context.getString(R.string.cl_total_positions_str) + "("+context.getString(R.string.sl_str_contracts_unit)+")" else context.getString(R.string.cl_total_positions_str) + "(" + mMultiplierCoin + ")")
            //保证金
            setText(R.id.tv_margins_value, BigDecimalUtils.showSNormal(item.holdAmount, LogicContractSetting.getContractMarginCoinPrecisionById(context, item.contractId)))
            //保证金Key
            setText(R.id.tv_margins_key, context.getString(R.string.cl_margin_str) + "(" + LogicContractSetting.getContractMarginCoinById(context, item.contractId) + ")")
            //可平
            setText(R.id.tv_gains_balance_value, if (LogicContractSetting.getContractUint(context) == 0) item.canCloseVolume else BigDecimalUtils.mulStr(item.canCloseVolume, mMultiplier, mMultiplierPrecision))
            //可平Key
            setText(R.id.tv_gains_balance_key, if (LogicContractSetting.getContractUint(context) == 0) context.getString(R.string.cl_available_position_str) + "("+context.getString(R.string.sl_str_contracts_unit)+")" else context.getString(R.string.cl_available_position_str) + "(" + mMultiplierCoin + ")")
            //保证金率
            setText(R.id.tv_tag_price_value, NumberUtil.getDecimal(2).format(MathHelper.round(MathHelper.mul(item.marginRate, "100"), 2)).toString() + "%")
            //标记价格
            setText(R.id.tv_holdings_value, BigDecimalUtils.showSNormal(item.indexPrice, mPricePrecision))
            //杠杆
            setText(R.id.tv_amount_can_be_liquidated_value, item.leverageLevel.toString() + "X")
            //已结算盈亏
            setText(R.id.tv_settled_profit_loss_value, BigDecimalUtils.showSNormal(item.profitRealizedAmount, mMarginCoinPrecision))

//            if (BigDecimalUtils.compareTo(BigDecimalUtils.showSNormal(item.profitRealizedAmount, mMarginCoinPrecision),"0")==1){
//                setTextColor(R.id.tv_settled_profit_loss_value, context.resources.getColor(R.color.main_green))
//            }else{
//                setTextColor(R.id.tv_settled_profit_loss_value, context.resources.getColor(R.color.main_red))
//            }
        }
    }
}