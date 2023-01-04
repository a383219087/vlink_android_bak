package com.chainup.contract.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpColorUtil
import com.chainup.contract.utils.CpTimeFormatUtils
import org.json.JSONObject


class CpContractHistoricalPositionAdapter(ctx: Context, data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(
    R.layout.cp_item_pl_record, data), LoadMoreModule {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        helper.run {
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
            setText(R.id.tv_side, typeStr)
            setTextColor(R.id.tv_side, CpColorUtil.getMainColorType(item.optString("orderSide").equals("BUY")))

            setText(R.id.tv_coin_name, item.optString("symbol"))
            setText(R.id.tv_level_value, (if (item.optInt("positionType") == 1) context.getString(R.string.cp_contract_setting_text1) else  context.getString(R.string.cp_contract_setting_text2)) + item.optString("leverageLevel") + "X")
            setText(R.id.tv_date, CpTimeFormatUtils.timeStampToDate(item.optString("mtime").toLong(), "yyyy-MM-dd  HH:mm:ss"))
            val positionVolume = if (CpClLogicContractSetting.getContractUint(context) == 0) CpBigDecimalUtils.showSNormal(item.optString("positionVolume"),0) else CpBigDecimalUtils.mulStr(item.optString("positionVolume"), mMultiplier, mMultiplierPrecision)
            setText(R.id.tv_pl_price, CpBigDecimalUtils.showSNormal(item.optString("profitRealizedAmount"), mMarginCoinPrecision)) //已实现盈亏
            setText(R.id.tv_open_average_price, CpBigDecimalUtils.showSNormal(item.optString("openPrice"), mSymbolPricePrecision))//开仓均价
            setText(R.id.tv_position_amount, positionVolume)//仓位数量
            setText(R.id.tv_position_amount_key, if (CpClLogicContractSetting.getContractUint(context) == 0) context.getString(R.string.cp_calculator_text38) + "("+context.getString(R.string.cp_overview_text9)+")" else context.getString(R.string.cp_calculator_text38) + "(" + mMultiplierCoin + ")" )//仓位数量
            setText(R.id.tv_key3, context.getString(R.string.cp_content_text25) + "(" + mMarginCoin + ")")//sl_str_avg_close_px
            setText(R.id.tv_value3, CpBigDecimalUtils.showSNormal(item.optString("closePrice"), mMarginCoinPrecision))//平仓均价

        }
    }
}