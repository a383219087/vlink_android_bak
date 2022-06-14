package com.chainup.contract.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpTimeFormatUtils
import org.json.JSONObject


class CpContractHistoricalPositionAdapter(ctx: Context, data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(
    R.layout.cp_item_pl_record, data), LoadMoreModule {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        helper?.run {
            var mMarginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(context, item.optInt("contractId"))
            var mSymbolPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(context, item.optInt("contractId"))
            var mMarginCoin = CpClLogicContractSetting.getContractMarginCoinById(context, item.optInt("contractId"))
            val mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(context, item.optInt("contractId"))
            val mMultiplier = CpClLogicContractSetting.getContractMultiplierById(context, item.optInt("contractId"))
            val mMultiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(context, item.optInt("contractId"))
            val typeStr = if (item.optString("orderSide").equals("BUY")) {
                context.getString(R.string.cp_order_text6)
            } else {
                context.getString(R.string.cp_order_text15)
            }
            val typeColor = if (item.optString("orderSide").equals("BUY")) {
                R.color.main_green
            } else {
                R.color.main_red
            }
            setText(R.id.tv_side, typeStr)
            setTextColor(R.id.tv_side, context.resources.getColor(typeColor))

            setText(R.id.tv_coin_name, item.optString("symbol"))
            setText(R.id.tv_level_value, (if (item.optInt("positionType") == 1) context.getString(R.string.cp_contract_setting_text1) else  context.getString(R.string.cp_contract_setting_text2)) + item.optString("leverageLevel") + "X")
            setText(R.id.tv_date, CpTimeFormatUtils.timeStampToDate(item.optString("mtime").toLong(), "yyyy-MM-dd  HH:mm:ss"))

            val profitLossColor = if (CpBigDecimalUtils.compareTo(CpBigDecimalUtils.showSNormal(item.optString("historyRealizedAmount"), mMarginCoinPrecision), "0") == 1) {
                R.color.main_green
            } else {
                R.color.main_red
            }

            val positionVolume = if (CpClLogicContractSetting.getContractUint(context) == 0) CpBigDecimalUtils.showSNormal(item.optString("positionVolume"),0) else CpBigDecimalUtils.mulStr(item.optString("positionVolume"), mMultiplier, mMultiplierPrecision)
//            setTextColor(R.id.tv_pl_price, context.resources.getColor(profitLossColor))
            setText(R.id.tv_pl_price, CpBigDecimalUtils.showSNormal(item.optString("profitRealizedAmount"), mMarginCoinPrecision)) //已实现盈亏
            setText(R.id.tv_open_average_price, CpBigDecimalUtils.showSNormal(item.optString("openEndPrice"), mSymbolPricePrecision))//开仓均价
            setText(R.id.tv_position_amount, positionVolume)//仓位数量
            setText(R.id.tv_position_amount_key, if (CpClLogicContractSetting.getContractUint(context) == 0) context.getString(R.string.cp_calculator_text38) + "("+context.getString(R.string.cp_overview_text9)+")" else context.getString(R.string.cp_calculator_text38) + "(" + mMultiplierCoin + ")" )//仓位数量
//            setText(R.id.tv_key1, context.getString(R.string.cl_realized_profit_and_loss_str) + "(" + mMarginCoin + ")")
//            setText(R.id.tv_key2, context.getString(R.string.cl_open_price_str) + "(" + mMarginCoin + ")")
//            setText(R.id.tv_key3, context.getString(R.string.sl_str_avg_close_px)+ "(" + mMarginCoin + ")")
//            setText(R.id.tv_key4, if (CpClLogicContractSetting.getContractUint(context) == 0) context.getString(R.string.cp_calculator_text38) + "("+context.getString(R.string.cp_overview_text9)+")" else context.getString(R.string.cp_calculator_text38) + "(" + mMultiplierCoin + ")"+"）")
        }
    }
}