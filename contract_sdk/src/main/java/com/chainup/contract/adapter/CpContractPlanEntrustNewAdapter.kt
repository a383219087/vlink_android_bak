package com.yjkj.chainup.new_contract.adapter

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.ChainUpLogUtil
import com.chainup.contract.utils.CpTimeFormatUtils
import com.chainup.contract.view.CpContractUpDownItemLayout
import com.chainup.contract.view.CpNLoadingDialog
import com.yjkj.chainup.new_contract.bean.CpCurrentOrderBean
import java.math.BigDecimal
import java.util.*

/**
 * 合约限价委托
 */
class CpContractPlanEntrustNewAdapter(ctx: Context, data: ArrayList<CpCurrentOrderBean>) : BaseQuickAdapter<CpCurrentOrderBean, BaseViewHolder>(
        R.layout.cp_item_plan_entrust, data), LoadMoreModule {

    //是否是当前委托
    private var isCurrentEntrust = true
    private var loadDialog: CpNLoadingDialog? = null


//    var cp_overview_text130 = ""
//    var sl_str_sell_open0 = ""
//    var contract_flat_short = ""
//    var contract_flat_long = ""
//    var sl_str_latest_price_simple = ""
//    var sl_str_fair_price_simple = ""
//    var sl_str_index_price_simple = ""
//    var sl_str_trigger_price = ""
//    var sl_str_execution_price = ""
//    var sl_str_execution_volume = ""
    var cp_overview_text9 = ""
//    var sl_str_market_price_simple = ""
//    var sl_str_deadline = ""
//    var sl_str_trigger_time = ""
//    var sl_str_cancel_order = ""
//    var sl_str_order_complete = ""
//    var sl_str_user_canceled = ""
//    var sl_str_order_timeout = ""
//    var sl_str_trigger_failed = ""
//    var cl_order_price_str = ""
    var cl_order_volume_str = ""
//    var cl_open_value_str = ""
//    var cl_average_price_str = ""
    var cp_overview_text3 = ""
    var cp_overview_text4 = ""
//    var cl_reduce_only_str = ""
//    var contract_text_orderWaitInHandicap = ""
//    var statusText2 = ""
//    var statusText3 = ""
//    var statusText4 = ""
//    var statusText5 = ""
//    var statusText6 = ""
    var statusText7 = ""
    var statusText8 = ""
    var statusText9 = ""
//    var sl_str_pl = ""
//    var cl_expration_date_str = ""
    var transaction_text_dealNum = ""
//    var mMultiplierCoin = ""
//    var mPricePrecision = 0
//    var mMultiplierPrecision = 0
//    var mMultiplier = "0"
    var coUnit = 0

    init {

    }

    fun setIsCurrentEntrust(isCurrentEntrust: Boolean = true) {
        this.isCurrentEntrust = isCurrentEntrust
    }

    override fun convert(helper: BaseViewHolder, item: CpCurrentOrderBean) {
//        cp_overview_text130 = context.getString(R.string.cp_overview_text130) //开多
//        sl_str_sell_open0 = context.getString(R.string.sl_str_sell_open0)//开空
//        contract_flat_short = context.getString(R.string.contract_flat_short)//平空
//        contract_flat_long = context.getString(R.string.contract_flat_long)//平多
//        sl_str_latest_price_simple = context.getString(R.string.sl_str_latest_price_simple)//最新价
//        sl_str_fair_price_simple = context.getString(R.string.sl_str_fair_price_simple)//合理价
//        sl_str_index_price_simple = context.getString(R.string.sl_str_index_price_simple)//指数价
//        sl_str_trigger_price = context.getString(R.string.sl_str_trigger_price)//触发价格
//        sl_str_execution_price = context.getString(R.string.sl_str_execution_price)//执行价格
//        sl_str_execution_volume = context.getString(R.string.sl_str_execution_volume)//执行数量
//        cp_overview_text9 = context.getString(R.string.cp_overview_text9)//张
//        sl_str_market_price_simple = context.getString(R.string.sl_str_market_price_simple)//市价
//        sl_str_deadline = context.getString(R.string.sl_str_deadline)//到期时间
//        sl_str_trigger_time = context.getString(R.string.sl_str_trigger_time)//触发时间
//        sl_str_cancel_order = context.getString(R.string.sl_str_cancel_order)//撤销
//        sl_str_order_complete = context.getString(R.string.sl_str_order_complete)//订单完成
//        sl_str_user_canceled = context.getString(R.string.sl_str_user_canceled)//用户取消
//        sl_str_order_timeout = context.getString(R.string.sl_str_order_timeout)//订单过期
//        sl_str_trigger_failed = context.getString(R.string.sl_str_trigger_failed)//执行失败
//        cl_order_price_str = context.getString(R.string.cl_order_price_str)//委托价格
        cl_order_volume_str = context.getString(R.string.cp_order_text66)//委托数量
//        cl_open_value_str = context.getString(R.string.cl_open_value_str)//开仓价值
//        cl_average_price_str = context.getString(R.string.cl_average_price_str)//成交均价
        cp_overview_text3 = context.getString(R.string.cp_overview_text3)//限价单
        cp_overview_text4 = context.getString(R.string.cp_overview_text4)//市价单
        transaction_text_dealNum = context.getString(R.string.cp_order_text66)//成交数量
//        cl_reduce_only_str = context.getString(R.string.cl_reduce_only_str)//只减仓
//        sl_str_pl = context.getString(R.string.sl_str_pl)//盈亏
//        cl_expration_date_str = context.getString(R.string.cl_expration_date_str)//过期时间
//        contract_text_orderWaitInHandicap = context.getString(R.string.contract_text_orderWaitInHandicap)//初始订单
//        statusText2 = context.getString(R.string.cl_order_statustext2)//已过期
//        statusText3 = context.getString(R.string.cl_order_statustext3)//"已完成"
//        statusText4 = context.getString(R.string.cl_order_statustext4)//"触发失败"
//        statusText5 = context.getString(R.string.sl_str_trigger_failed)//"执行失败"
//        statusText6 = context.getString(R.string.cl_cancelled_str)//"已撤销"
        statusText7 = context.getString(R.string.cp_order_text63)//"止盈单"
        statusText8 = context.getString(R.string.cp_order_text62)//"止损单"
        statusText9 = context.getString(R.string.cp_overview_text5)//"条件单"


        coUnit = CpClLogicContractSetting.getContractUint(context)

        //保证金币种
        val marginCoin = CpClLogicContractSetting.getContractMarginCoinById(context, item.contractId.toInt())
        val marginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(context, item.contractId.toInt())
        //合约面值
        val multiplier = CpClLogicContractSetting.getContractMultiplierById(context, item.contractId.toInt())
        //合约面值单位
        val multiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinById(context, item.contractId.toInt())

        val multiplierBuff = BigDecimal(multiplier).stripTrailingZeros().toPlainString()

        //合约名称
        var symbolName = CpClLogicContractSetting.getContractShowNameById(context, item.contractId.toInt())

        var contractSide = CpClLogicContractSetting.getContractSideById(context, item.contractId.toInt())

        //面值精度
        val multiplierPrecision = if (multiplierBuff.contains(".")) {
            ChainUpLogUtil.e("------------", multiplierBuff)
            ChainUpLogUtil.e("------------", multiplierBuff.split(".".toRegex()).toTypedArray().size.toString() + "")
            val index = multiplierBuff.indexOf(".")
            if (index < 0) 0 else multiplierBuff.length - index - 1
        } else {
            multiplierBuff.length
        }
        var showDealNumber = ""
        var showEntrustNumber = ""
        //委托数量展示单位
        var showEntrustUnit = ""
        if (coUnit == 0) {
            showEntrustUnit = cl_order_volume_str + "(" + cp_overview_text9 + ")"
        } else {
            showEntrustUnit = cl_order_volume_str + "(" + multiplierCoin + ")"
        }

        //成交数量展示单位
        var showDealUnit = ""
        if (coUnit == 0) {
            showDealUnit = transaction_text_dealNum + "(" + cp_overview_text9 + ")"
        } else {
            showDealUnit = transaction_text_dealNum + "(" + multiplierCoin + ")"
        }
        if (coUnit == 0) {
            //成交数量
            showDealNumber = item.dealVolume
            //委托数量
            showEntrustNumber = item.volume
            showDealUnit = transaction_text_dealNum + "(" + cp_overview_text9 + ")"
        } else {
            //成交数量
            showDealNumber = CpBigDecimalUtils.mulStr(item.dealVolume, multiplier, multiplierPrecision)
            //委托数量
            showEntrustNumber = CpBigDecimalUtils.mulStr(item.volume, multiplier, multiplierPrecision)
            showDealUnit = transaction_text_dealNum + "(" + multiplierCoin + ")"
        }

        if (item.type.equals("2") && item.open.equals("OPEN")) {
            showEntrustNumber = item.volume
        }


        var openStr = item.open
        var sideStr = item.side
        var typeStr = ""
        var isOnlyReducePosition = false
        var only_reduce_position = context.getString(R.string.cp_extra_text2)//"否"
        //context.getLineText("cp_overview_text13")
        if (openStr.equals("OPEN") && sideStr.equals("BUY")) {
            typeStr = context.getString(R.string.cp_overview_text13)//买入开多
        } else if (openStr.equals("OPEN") && sideStr.equals("SELL")) {
            typeStr = context.getString(R.string.cp_overview_text14)//卖出开空
        } else if (openStr.equals("CLOSE") && sideStr.equals("BUY")) {
            typeStr = context.getString(R.string.cp_extra_text4)//买入平空
        } else if (openStr.equals("CLOSE") && sideStr.equals("SELL")) {
            typeStr = context.getString(R.string.cp_extra_text5)//卖出平多
        }
        if (openStr.equals("CLOSE")) {
            only_reduce_position = context.getString(R.string.cp_extra_text3)//"是"
            isOnlyReducePosition = true
        }

        when (sideStr) {
            "BUY" -> {
                helper?.setTextColor(R.id.tv_side, context.resources.getColor(R.color.main_green))
            }
            "SELL" -> {
                helper?.setTextColor(R.id.tv_side, context.resources.getColor(R.color.main_red))
            }
            else -> {
            }
        }

        if (openStr.equals("CLOSE")) {
            only_reduce_position = context.getString(R.string.cp_extra_text3)
        }
        var orderType = when (item.timeInForce) {
            "1" -> cp_overview_text3
            "2" -> cp_overview_text4
            "3" -> "IOC"
            "4" -> "FOK"
            "5" -> "Post Only"
            else -> "error"
        }
        //0 初始, 1 已过期, 2 已完成, 3 触发失败, 4 已撤销
//        var orderStatus = when (item.status) {
//            "0" -> contract_text_orderWaitInHandicap //"初始"
//            "1" -> statusText2//"已过期"
//            "2" -> statusText3//"已完成"
//            "3" -> statusText5//"触发失败"
//            "4" -> statusText6//"已撤销"
//            else -> "error"
//        }

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

        helper.setGone(R.id.tv_only_reduce_position,( item.timeInForce.equals("1")|| item.timeInForce.equals("2")))

        helper.setText(R.id.tv_side, typeStr)
        helper.setText(R.id.tv_coin_name, symbolName)
        helper.setText(R.id.tv_date, CpTimeFormatUtils.timeStampToDate(item.ctime.toLong(), "yyyy-MM-dd  HH:mm:ss"))
        helper.setText(R.id.tv_order_type, orderTypeStr)
        helper.setText(R.id.tv_trigger_price, item.triggerPrice)
        helper.setText(R.id.tv_entrust_price, if (item.timeInForce.equals("2")) context.getString(R.string.cp_overview_text53) else item.price)
//        helper.setText(R.id.tv_entrust_amount_value, if (coUnit == 0) item.volume else CpBigDecimalUtils.mulStr(item.volume, multiplier, multiplierPrecision))
        helper.setText(R.id.tv_expiration_date, CpTimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM-dd  HH:mm"))
        helper.setText(R.id.tv_only_reduce_position, orderTypeNewStr)
//        helper.setText(R.id.tv_entrust_amount_key, showEntrustUnit)

        if (openStr.equals("OPEN") && item.type.equals("2")) {
            helper.setText(R.id.tv_entrust_amount_key, context.getString(R.string.cp_extra_text9) + "(" + (if (contractSide == 1) item.quote else item.base) + ")")
            helper.setText(R.id.tv_entrust_amount_value, item.volume)
        } else {
            helper.setText(R.id.tv_entrust_amount_key,  showDealUnit)
            helper.setText(R.id.tv_entrust_amount_value, if (coUnit == 0) item.volume else CpBigDecimalUtils.mulStr(item.volume, multiplier, multiplierPrecision))
        }

    }

}