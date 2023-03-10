package com.yjkj.chainup.new_version.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.TimeUtil
import org.json.JSONObject


class ClContractHistoricalPositionAdapter(ctx: Context, data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cll_item_historical_position, data), LoadMoreModule {



    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        helper.run {
            var mMarginCoinPrecision = LogicContractSetting.getContractMarginCoinPrecisionById(context, item.optInt("contractId"))
            var mMarginCoin = LogicContractSetting.getContractMarginCoinById(context, item.optInt("contractId"))
            val mMultiplierPrecision = LogicContractSetting.getContractMultiplierPrecisionById(context, item.optInt("contractId"))
            val mMultiplier = LogicContractSetting.getContractMultiplierById(context, item.optInt("contractId"))
            val mMultiplierCoin = LogicContractSetting.getContractMultiplierCoinPrecisionById(context, item.optInt("contractId"))
            val orderSide= if(item.optString("orderSide").isNullOrEmpty()){
                item.optString("side")
            }else{
                item.optString("orderSide")
            }
            val typeStr = if (orderSide.equals("BUY")) {
                context.getString(R.string.cl_HistoricalPosition_1)
            } else {
                context.getString(R.string.cl_HistoricalPosition_2)
            }
            if (!item.optString("traderName").isNullOrEmpty()&&!item.optString("traderName").equals("null")){
                setGone(R.id.tv_tradle_name, false)
                setText(R.id.tv_tradle_name, "${context.getString(com.chainup.contract.R.string.traders_apply_text9)}???${item.optString("traderName")}")
            }else{

                setGone(R.id.tv_tradle_name, true)
            }
            val typeColor = if (orderSide.equals("BUY")) {
                R.color.main_green
            } else {
                R.color.main_red
            }
            setText(R.id.tv_type_value, typeStr)
            setTextColor(R.id.tv_type_value, context.resources.getColor(typeColor))

            val contractName= if(item.optString("symbol").isNullOrEmpty()){
                item.optString("contractName")
            }else{
                item.optString("symbol")
            }

            setText(R.id.tv_symbol_value, contractName)
            //cl_currentsymbol_marginmodel1
            //cl_currentsymbol_marginmodel2
            setText(R.id.tv_level_value, (if (item.optString("positionType") .equals("1") ) context.getString(R.string.cl_currentsymbol_marginmodel1) else context.getString(R.string.cl_currentsymbol_marginmodel2)) + item.optString("leverageLevel") + "X")
            val time=item.optString("mtime");
            if(!time.isNullOrEmpty()){
                if (time.contains("T")){
                    setText(R.id.tv_time_value, time.replaceFirst("T"," "))
                }else {

                    setText(R.id.tv_time_value, time.toLongOrNull()?.let { TimeUtil.instance.getFormatDateTime(it) })
                }
            }

            val profitLossColor = if (BigDecimalUtils.compareTo(BigDecimalUtils.showSNormal(item.optString("historyRealizedAmount"), mMarginCoinPrecision), "0") == 1) {
                R.color.main_green
            } else {
                R.color.main_red
            }

            val positionVolume = if (LogicContractSetting.getContractUint(context) == 0) item.optString("positionVolume") else BigDecimalUtils.mulStr(item.optString("positionVolume"), mMultiplier, mMultiplierPrecision)
            setTextColor(R.id.tv_value1, context.resources.getColor(profitLossColor))
            setText(R.id.tv_value1, BigDecimalUtils.showSNormal(item.optString("historyRealizedAmount"), mMarginCoinPrecision)) //???????????????
            setText(R.id.tv_value2, BigDecimalUtils.showSNormal(item.optString("openPrice"), mMarginCoinPrecision))//????????????
            setText(R.id.tv_value3, BigDecimalUtils.showSNormal(item.optString("closePrice"), mMarginCoinPrecision))//????????????
            setText(R.id.tv_value4, positionVolume)//????????????
            setText(R.id.tv_key1, context.getString(R.string.cl_realized_profit_and_loss_str) + "(" + mMarginCoin + ")")
            setText(R.id.tv_key2, context.getString(R.string.cl_open_price_str) + "(" + mMarginCoin + ")")//cl_open_price_str
            setText(R.id.tv_key3, context.getString(R.string.sl_str_avg_close_px) + "(" + mMarginCoin + ")")//sl_str_avg_close_px
            setText(R.id.tv_key4, if (LogicContractSetting.getContractUint(context) == 0) context.getString(R.string.cl_calculator_text21) + "("+context.getString(R.string.sl_str_contracts_unit)+")" else context.getString(R.string.cl_calculator_text21) + "(" + mMultiplierCoin + ")")
        }
    }
}