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
            val buyColor = CpColorUtil.getMainColorType(true)
            val sellColor = CpColorUtil.getMainColorType(false)
            when (item.orderSide) {
                "BUY" -> {
                    setText(R.id.tv_type, context.getString(R.string.cp_order_text6111))
                    setTextColor(R.id.tv_profit_loss_value, buyColor)
                    setTextColor(R.id.tv_floating_gains_value, buyColor)
                    getView<SuperTextView>(R.id.tv_open_type).solid = buyColor
                }
                "SELL" -> {
                    setText(R.id.tv_type, context.getString(R.string.cp_order_text152))
                    setTextColor(R.id.tv_profit_loss_value, sellColor)
                    setTextColor(R.id.tv_floating_gains_value, sellColor)
                    getView<SuperTextView>(R.id.tv_open_type).solid = sellColor
                }
                else -> {
                }
            }
            if (CpBigDecimalUtils.compareTo(
                    CpBigDecimalUtils.showSNormal(item.openRealizedAmount, mMarginCoinPrecision),"0")==1){
                setTextColor(R.id.tv_profit_loss_value, buyColor)
                setTextColor(R.id.tv_floating_gains_value, buyColor)

            }else{
                setTextColor(R.id.tv_profit_loss_value, sellColor)
                setTextColor(R.id.tv_floating_gains_value, sellColor)

            }

            //???????????????????????????????????????????????????????????????
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
            //????????????
            setText(R.id.tv_open_price_value, CpBigDecimalUtils.showSNormal(item.openAvgPrice, mPricePrecision))
            //??????
            setText(R.id.tv_profit_loss_value, CpBigDecimalUtils.showSNormal(item.openRealizedAmount, mMarginCoinPrecision))
            //??????Key
            setText(R.id.tv_floating_gains_balance_key, context.getString(R.string.cp_order_text8) + "(" + CpClLogicContractSetting.getContractMarginCoinById(context, item.contractId) + ")")
            //???????????????
            if (CpBigDecimalUtils.compareTo(item.reducePrice, "0") == 1) {
                setText(R.id.tv_forced_close_price_value, CpBigDecimalUtils.showSNormal(item.reducePrice, mPricePrecision))
            } else {
                setText(R.id.tv_forced_close_price_value, "--")
            }
            val  prefix=if (item.returnRate.toString().contains("-")) "" else "+"
            //?????????
            setText(R.id.tv_floating_gains_value, prefix+CpNumberUtil().getDecimal(2).format(
                CpMathHelper.round(CpMathHelper.mul(item.returnRate.toString(), "100"), 2)).toString() + "%")
            //?????????
            setText(R.id.tv_total_position_value, if (CpClLogicContractSetting.getContractUint(context) == 0) item.positionVolume else CpBigDecimalUtils.mulStr(item.positionVolume,mMultiplier, mMultiplierPrecision))
            //?????????Key
            setText(R.id.tv_total_position_key, if (CpClLogicContractSetting.getContractUint(context) == 0) context.getString(R.string.cp_order_text11) + "("+context.getString(R.string.cp_overview_text9)+")" else context.getString(R.string.cp_order_text11) + "(" + mMultiplierCoin + ")")
            //?????????
            setText(R.id.tv_margins_value, CpBigDecimalUtils.showSNormal(item.openAmount.toString(), CpClLogicContractSetting.getContractMarginCoinPrecisionById(context, item.contractId)))
            //?????????Key
            setText(R.id.tv_margins_key, context.getString(R.string.cp_order_text12) + "(" + CpClLogicContractSetting.getContractMarginCoinById(context, item.contractId) + ")")
            //??????
            setText(R.id.tv_gains_balance_value, if (CpClLogicContractSetting.getContractUint(context) == 0) CpDecimalUtil.cutValueByPrecision(item.canCloseVolume,0)  else CpBigDecimalUtils.mulStr(item.canCloseVolume, mMultiplier, mMultiplierPrecision))
            //??????Key
            setText(R.id.tv_gains_balance_key, if (CpClLogicContractSetting.getContractUint(context) == 0) context.getString(R.string.cp_order_text35) + "("+context.getString(R.string.cp_overview_text9)+")" else context.getString(R.string.cp_order_text35) + "(" + mMultiplierCoin + ")")
            //????????????
            setText(R.id.tv_tag_price_value, CpNumberUtil().getDecimal(2).format(
                CpMathHelper.round(
                    CpMathHelper.mul(item.marginRate, "100"), 2)).toString() + "%")
            //????????????
            setText(R.id.tv_holdings_value, CpBigDecimalUtils.showSNormal(item.indexPrice, mPricePrecision))
            //??????
            //            setText(R.id.tv_amount_can_be_liquidated_value, item.leverageLevel.toString() + "X")
            //???????????????
            setText(R.id.tv_settled_profit_loss_value, CpBigDecimalUtils.showSNormal(item.profitRealizedAmount, mMarginCoinPrecision))

        }
    }
}