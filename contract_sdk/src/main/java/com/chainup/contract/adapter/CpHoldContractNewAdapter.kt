package com.chainup.contract.adapter

import android.annotation.SuppressLint
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.utils.*
import com.coorchice.library.SuperTextView

class CpHoldContractNewAdapter(data: ArrayList<CpContractPositionBean>) : BaseQuickAdapter<CpContractPositionBean, BaseViewHolder>(
    R.layout.cp_item_position, data) {


    private var isMe=true


    @SuppressLint("NotifyDataSetChanged")
    fun setMySelf(boolean: Boolean){
        this.isMe=boolean
        notifyDataSetChanged()
    }




    override fun convert(helper: BaseViewHolder, item: CpContractPositionBean) {



        val mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(context, item.contractId)

        val mMarginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(context, item.contractId)

        val mMultiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(context, item.contractId)

        val mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(context, item.contractId)

        val mMultiplier = CpClLogicContractSetting.getContractMultiplierById(context, item.contractId)
        helper.run {
            setGone(R.id.ll_button, !isMe)
            when (item.orderSide) {
                "BUY" -> {
                    setText(R.id.tv_type, context.getString(R.string.cp_order_text6111))
                    setTextColor(R.id.tv_profit_loss_value, context.resources.getColor(R.color.main_green))
                    setTextColor(R.id.tv_floating_gains_value, context.resources.getColor(R.color.main_green))
                }
                "SELL" -> {
                    setText(R.id.tv_type, context.getString(R.string.cp_order_text152))

                    setTextColor(R.id.tv_profit_loss_value, context.resources.getColor(R.color.main_red))
                    setTextColor(R.id.tv_floating_gains_value, context.resources.getColor(R.color.main_red))
                }
                else -> {
                }
            }
            if (CpBigDecimalUtils.compareTo(
                    CpBigDecimalUtils.showSNormal(item.openRealizedAmount, mMarginCoinPrecision),"0")==1){
                setTextColor(R.id.tv_profit_loss_value, context.resources.getColor(R.color.main_green))
                setTextColor(R.id.tv_floating_gains_value, context.resources.getColor(R.color.main_green))
                getView<SuperTextView>(R.id.tv_open_type).solid = context.resources.getColor(R.color.main_green)
            }else{
                setTextColor(R.id.tv_profit_loss_value, context.resources.getColor(R.color.main_red))
                setTextColor(R.id.tv_floating_gains_value, context.resources.getColor(R.color.main_red))
                getView<SuperTextView>(R.id.tv_open_type).solid = context.resources.getColor(R.color.main_red)
            }

            //只有逐仓才有调整保证金，全仓没有调整保证金
            when (item.positionType) {
                1 -> {
                    setVisible(R.id.tv_adjust_margins, false)
                    setText(R.id.tv_open_type, context.getString(R.string.cp_contract_setting_text1)+" "+item.leverageLevel.toString() + "X")
                }
                2 -> {
                    setVisible(R.id.tv_adjust_margins, true)
                    setText(R.id.tv_open_type, context.getString(R.string.cp_contract_setting_text2)+" "+item.leverageLevel.toString() + "X")
                }
                else -> {
                }
            }
            val symbolName = CpClLogicContractSetting.getContractShowNameById(context, item.contractId)
            setText(R.id.tv_contract_name, symbolName)
            //开仓均价
            setText(R.id.tv_open_price_value, CpBigDecimalUtils.showSNormal(item.openAvgPrice, mPricePrecision))
            //盈亏
            setText(R.id.tv_profit_loss_value, CpBigDecimalUtils.showSNormal(item.openRealizedAmount, mMarginCoinPrecision))
            //盈亏Key
            setText(R.id.tv_floating_gains_balance_key, context.getString(R.string.cp_order_text8) + "(" + CpClLogicContractSetting.getContractMarginCoinById(context, item.contractId) + ")")
            //预估强平价
            if (CpBigDecimalUtils.compareTo(item.reducePrice, "0") == 1) {
                setText(R.id.tv_forced_close_price_value, CpBigDecimalUtils.showSNormal(item.reducePrice, mPricePrecision))
            } else {
                setText(R.id.tv_forced_close_price_value, "--")
            }
            val  prefix=if (CpBigDecimalUtils.compareTo(item.returnRate.toString(),"0")==-1) "" else "+"
            //回报率
            setText(R.id.tv_floating_gains_value, prefix+CpNumberUtil().getDecimal(2).format(
                CpMathHelper.round(CpMathHelper.mul(item.returnRate.toString(), "100"), 2)).toString() + "%")
            //总持仓
            setText(R.id.tv_total_position_value, if (CpClLogicContractSetting.getContractUint(context) == 0) item.positionVolume else CpBigDecimalUtils.mulStr(item.positionVolume,mMultiplier, mMultiplierPrecision))
            //总持仓Key
            setText(R.id.tv_total_position_key, if (CpClLogicContractSetting.getContractUint(context) == 0) context.getString(R.string.cp_order_text11) + "("+context.getString(R.string.cp_overview_text9)+")" else context.getString(R.string.cp_order_text11) + "(" + mMultiplierCoin + ")")
            //保证金
            setText(R.id.tv_margins_value, CpBigDecimalUtils.showSNormal(item.holdAmount, CpClLogicContractSetting.getContractMarginCoinPrecisionById(context, item.contractId)))
            //保证金Key
            setText(R.id.tv_margins_key, context.getString(R.string.cp_order_text12) + "(" + CpClLogicContractSetting.getContractMarginCoinById(context, item.contractId) + ")")
            //止盈价格
            setText(R.id.tv_holdings_value, "--")
            //止损价格
            setText(R.id.tv_holdings_value1, "--")


        }
    }
}