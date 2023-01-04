package com.chainup.contract.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpTimeFormatUtils
import com.chainup.contract.bean.CpCurrentOrderBean
import com.chainup.contract.utils.CpColorUtil
import com.coorchice.library.SuperTextView
import java.math.BigDecimal
import java.util.*

/**
 * 合约计划委托
 */
class CpContractPlanEntrustNewAdapter(ctx: Context, data: ArrayList<CpCurrentOrderBean>) : BaseQuickAdapter<CpCurrentOrderBean, BaseViewHolder>(
        R.layout.cp_item_plan_entrust, data), LoadMoreModule {

    //是否是当前委托
    private var isCurrentEntrust = true


    var cp_overview_text9 = ""
    var cl_order_volume_str = ""
    var cp_overview_text3 = ""
    var cp_overview_text4 = ""
    var statusText7 = ""
    var statusText8 = ""
    var statusText9 = ""
    var transaction_text_dealNum = ""
    var coUnit = 0

    init {

    }

    fun setIsCurrentEntrust(isCurrentEntrust: Boolean = true) {
        this.isCurrentEntrust = isCurrentEntrust
    }

    override fun convert(helper: BaseViewHolder, item: CpCurrentOrderBean) {
        cp_overview_text9=context.getString(R.string.cp_overview_text9)
        cl_order_volume_str = context.getString(R.string.cp_order_text66)//委托数量
        cp_overview_text3 = context.getString(R.string.cp_overview_text3)//限价单
        cp_overview_text4 = context.getString(R.string.cp_overview_text4)//市价单
        transaction_text_dealNum = context.getString(R.string.cp_order_text66)//成交数量
        statusText7 = context.getString(R.string.cp_order_text63)//"止盈单"
        statusText8 = context.getString(R.string.cp_order_text62)//"止损单"
        statusText9 = context.getString(R.string.cp_overview_text5)//"条件单"


        coUnit = CpClLogicContractSetting.getContractUint(context)

        //合约面值
        val multiplier = CpClLogicContractSetting.getContractMultiplierById(context, item.contractId.toInt())
        //合约面值单位
        val multiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinById(context, item.contractId.toInt())

        val multiplierBuff = BigDecimal(multiplier).stripTrailingZeros().toPlainString()

        //合约名称
        val symbolName = CpClLogicContractSetting.getContractShowNameById(context, item.contractId.toInt())

        val contractSide = CpClLogicContractSetting.getContractSideById(context, item.contractId.toInt())

        //面值精度
        val multiplierPrecision = if (multiplierBuff.contains(".")) {
            val index = multiplierBuff.indexOf(".")
            if (index < 0) 0 else multiplierBuff.length - index - 1
        } else {
            multiplierBuff.length
        }


        //成交数量展示单位
        val showDealUnit = if (coUnit == 0) {

            "$transaction_text_dealNum($cp_overview_text9)"
        } else {
            "$transaction_text_dealNum($multiplierCoin)"
        }


        val openStr = item.open
        val sideStr = item.side
        var typeStr = ""
        if (openStr == "OPEN" && sideStr == "BUY") {
            typeStr = context.getString(R.string.cp_overview_text13)//买入开多
        } else if (openStr == "OPEN" && sideStr == "SELL") {
            typeStr = context.getString(R.string.cp_overview_text14)//卖出开空
        } else if (openStr == "CLOSE" && sideStr == "BUY") {
            typeStr = context.getString(R.string.cp_extra_text4)//买入平空
        } else if (openStr == "CLOSE" && sideStr == "SELL") {
            typeStr = context.getString(R.string.cp_extra_text5)//卖出平多
        }


        when (sideStr) {
            "BUY" -> {
                helper.getView<SuperTextView>(R.id.tv_side).solid = CpColorUtil.getMainColorType(true)
            }
            "SELL" -> {
                helper.getView<SuperTextView>(R.id.tv_side).solid = CpColorUtil.getMainColorType(false)
            }
            else -> {
            }
        }


        var orderTypeStr = when (item.triggerType) {
            1 -> statusText8 //止损单
            2 -> statusText7 //止盈单
            3, 4 -> statusText9//条件单
            else -> "error"
        }

        var orderTypeNewStr = when (item.timeInForce) {
            "1" -> context.getString(R.string.cp_overview_text3)//"限价单"
            "2" -> context.getString(R.string.cp_overview_text4)//"市价单"
            "3" -> "IOC"
            "4" -> "FOK"
            "5" -> "Post Only"
            else -> "error"
        }

//        helper.setGone(R.id.tv_only_reduce_position,( item.timeInForce == "1" || item.timeInForce == "2"))

        helper.setText(R.id.tv_side, typeStr)
        helper.setText(R.id.tv_coin_name, symbolName)
        helper.setText(R.id.tv_date, CpTimeFormatUtils.timeStampToDate(item.ctime.toLong(), "MM-dd  HH:mm"))
//        helper.setText(R.id.tv_order_type, orderTypeStr)
        helper.setText(R.id.tv_trigger_price, CpBigDecimalUtils.showSNormal(item.triggerPrice))
        helper.setText(R.id.tv_entrust_price, if (item.timeInForce == "2") context.getString(R.string.cp_overview_text53) else item.price)
        helper.setText(R.id.tv_expiration_date, CpTimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM-dd  HH:mm"))
        helper.setText(R.id.tv_only_reduce_position, orderTypeStr)

        if (openStr == "OPEN" && item.type == "2") {
            helper.setText(R.id.tv_entrust_amount_key, context.getString(R.string.cp_extra_text9) + "(" + (if (contractSide == 1) item.quote else item.base) + ")")
            helper.setText(R.id.tv_entrust_amount_value, item.volume)
        } else {
            helper.setText(R.id.tv_entrust_amount_key,  showDealUnit)
            helper.setText(R.id.tv_entrust_amount_value, if (coUnit == 0) item.volume else CpBigDecimalUtils.mulStr(item.volume, multiplier, multiplierPrecision))
        }

    }

}