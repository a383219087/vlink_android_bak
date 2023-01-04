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
import com.chainup.contract.utils.CpTimeFormatUtils
import com.chainup.contract.view.CpContractUpDownItemLayout
import com.chainup.contract.bean.CpCurrentOrderBean
import com.chainup.contract.utils.CpColorUtil
import java.math.BigDecimal
import java.util.*

/**
 * 合约限价委托
 */
class CpContractPriceEntrustNewAdapter(ctx: Context, data: ArrayList<CpCurrentOrderBean>) : BaseQuickAdapter<CpCurrentOrderBean, BaseViewHolder>(
        R.layout.cp_item_contract_price_entrust_new, data), LoadMoreModule {

    //是否是当前委托
    private var isCurrentEntrust = true



    var sl_str_trigger_price = ""
var cp_overview_text9 = ""
    var sl_str_market_price_simple = ""
    var sl_str_trigger_time = ""
    var cl_order_price_str = ""
    var cl_order_volume_str = ""
    var cl_open_value_str = ""
    var cl_average_price_str = ""
    var cp_overview_text3 = ""
    var cp_overview_text4 = ""
    var cl_reduce_only_str = ""
    var contract_text_orderWaitInHandicap = ""
    var statusText2 = ""
    var statusText3 = ""
    var statusText4 = ""
    var statusText5 = ""
    var statusText6 = ""
    var statusText7 = ""
    var statusText8 = ""
    var statusText9 = ""
    var sl_str_pl = ""
    var cl_expration_date_str = ""
    var transaction_text_dealNum = ""
    var mMultiplierCoin = ""
    var mPricePrecision = 0
    var mMultiplierPrecision = 0
    var mMultiplier = "0"
    var coUnit = 0


    fun setIsCurrentEntrust(isCurrentEntrust: Boolean = true) {
        this.isCurrentEntrust = isCurrentEntrust
    }

    override fun convert(helper: BaseViewHolder, item: CpCurrentOrderBean) {
        cp_overview_text9 = context.getString(R.string.cp_overview_text9)
        sl_str_trigger_price = context.getString(R.string.cp_overview_text29)//触发价格
        cp_overview_text9 = context.getString(R.string.cp_overview_text9)//张
        sl_str_market_price_simple = context.getString(R.string.cp_overview_text53)//市价
        sl_str_trigger_time = context.getString(R.string.cp_extra_text68)//触发时间
        cl_order_price_str = context.getString(R.string.cp_order_text56)//委托价格
        cl_order_volume_str = context.getString(R.string.cp_order_text66)//委托数量
        cl_open_value_str = context.getString(R.string.cp_overview_text28)//开仓价值
        cl_average_price_str = context.getString(R.string.cp_order_text58)//成交均价
        cp_overview_text3 = context.getString(R.string.cp_overview_text3)//限价单
        cp_overview_text4 = context.getString(R.string.cp_overview_text4)//市价单
        transaction_text_dealNum = context.getString(R.string.cp_extra_text8)//成交数量
        cl_reduce_only_str = context.getString(R.string.cp_order_text64)//只减仓
        sl_str_pl = context.getString(R.string.cp_order_text8)//盈亏
        cl_expration_date_str = context.getString(R.string.cp_extra_text69)//过期时间
        contract_text_orderWaitInHandicap = context.getString(R.string.cp_extra_text70)//初始订单
        statusText2 = context.getString(R.string.cp_order_text95)//已过期
        statusText3 = context.getString(R.string.cp_tip_text11)//"已完成"
        statusText4 = context.getString(R.string.cp_tip_text12)//"触发失败"
        statusText5 = context.getString(R.string.cp_extra_text71)//"执行失败"
        statusText6 = context.getString(R.string.cp_status_text2)//"已撤销"
        statusText7 = context.getString(R.string.cp_order_text63)//"止盈单"
        statusText8 = context.getString(R.string.cp_order_text62)//"止损单"
        statusText9 = context.getString(R.string.cp_order_text69)//"条件单"


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
            val index = multiplierBuff.indexOf(".")
            if (index < 0) 0 else multiplierBuff.length - index - 1
        } else {
            multiplierBuff.length
        }
        var showDealNumber = ""
        var showEntrustNumber = ""
        //委托数量展示单位
        var showEntrustUnit = ""
        showEntrustUnit = if (coUnit == 0) {
            "$cl_order_volume_str($cp_overview_text9)"
        } else {
            "$cl_order_volume_str($multiplierCoin)"
        }

        //成交数量展示单位
        var showDealUnit = ""

        if (coUnit == 0) {
            //成交数量
            showDealNumber = item.dealVolume.toString()
            //委托数量
            showEntrustNumber = item.volume
            showDealUnit = "$transaction_text_dealNum($cp_overview_text9)"
        } else {
            //成交数量
            showDealNumber = CpBigDecimalUtils.mulStr(item.dealVolume, multiplier, multiplierPrecision)
            //委托数量
            showEntrustNumber = CpBigDecimalUtils.mulStr(item.volume, multiplier, multiplierPrecision)
            showDealUnit = "$transaction_text_dealNum($multiplierCoin)"
        }

        if (item.type == "2" && item.open == "OPEN") {
            showEntrustNumber = item.volume
        }


        var openStr = item.open
        var sideStr = item.side
        var typeStr = ""
        var only_reduce_position = context.getString(R.string.cp_extra_text2)//"否"
        if (openStr == "OPEN" && sideStr == "BUY") {
            typeStr = context.getString(R.string.cp_overview_text13)//买入开多
        } else if (openStr == "OPEN" && sideStr == "SELL") {
            typeStr = context.getString(R.string.cp_overview_text14)//卖出开空
        } else if (openStr == "CLOSE" && sideStr == "BUY") {
            typeStr = context.getString(R.string.cp_extra_text4)//买入平空
        } else if (openStr == "CLOSE" && sideStr == "SELL") {
            typeStr = context.getString(R.string.cp_extra_text5)//卖出平多
        }
        if (openStr == "CLOSE") {
            only_reduce_position = context.getString(R.string.cp_extra_text3)//"是"
        }

        if (!item.isPlan) {
//            //普通
            helper.setGone(R.id.ll_plan, true)
            helper.setGone(R.id.ll_common, false)
            when (sideStr) {
                "BUY" -> {
                    helper.setTextColor(R.id.tv_type_common, CpColorUtil.getMainColorType(true))
                }
                "SELL" -> {
                    helper.setTextColor(R.id.tv_type_common, CpColorUtil.getMainColorType(false))
                }
                else -> {
                }
            }

            //限价单，市价单，IOC，FOK，Post Only
            var orderType = when (item.type) {
                "1" -> context.getString(R.string.cp_overview_text3)//"限价单"
                "2" -> context.getString(R.string.cp_overview_text4)//"市价单"
                "3" -> "IOC"
                "4" -> "FOK"
                "5" -> "Post Only"
                "6" -> context.getString(R.string.cp_extra_text6)//强制减仓
                "7" -> context.getString(R.string.cp_extra_text7)//仓位合并
                else -> "error"
            }

            //0 初始, 1 新订单, 2 完全成交, 3 部分成交, 4 已撤销, 5 待撤销, 6 异常订单
            var orderStatus = when (item.status) {
                "2" -> context.getString(R.string.cp_status_text1)//完全成交
                "3" -> context.getString(R.string.cp_order_text55)//"部分成交"
                "4" -> {
                    if (!item.dealVolume.isNullOrEmpty() && item.dealVolume.toDouble() > 0) {
                        context.getString(R.string.cp_status_text5)
                    } else {
                        context.getString(R.string.cp_status_text2)
                    }
                }
                "5" -> context.getString(R.string.cp_status_text4)//"待撤销"
                "6" -> context.getString(R.string.cp_status_text3)//"异常订单"
                else -> "error"
            }
            helper.setText(R.id.tv_type_common, typeStr)
            helper.setText(R.id.tv_contract_name_common, symbolName)
            helper.setText(R.id.tv_time_common, CpTimeFormatUtils.timeStampToDate(item.ctime.toLong(), "yyyy-MM-dd  HH:mm:ss"))
            if (item.type == "6") {
                //如果 强制减仓 则显示"--"
                helper.getView<CpContractUpDownItemLayout>(R.id.item_entrust_price_common).content = "--"
            } else {
                helper.getView<CpContractUpDownItemLayout>(R.id.item_entrust_price_common).content = if (item.type.equals("2")) sl_str_market_price_simple else CpBigDecimalUtils.showSNormal(item.price, item.pricePrecision)
            }
            helper.getView<CpContractUpDownItemLayout>(R.id.item_entrust_price_common).title = cl_order_price_str + "(" + item.quote + ")"
            val unitBuff = if (contractSide == 1) item.quote else item.base
            helper.getView<CpContractUpDownItemLayout>(R.id.item_entrust_volume_common).title = if (item.type.equals("2") && openStr.equals("OPEN")) cl_open_value_str + "(" + unitBuff + ")" else showEntrustUnit
            helper.getView<CpContractUpDownItemLayout>(R.id.item_entrust_volume_common).content = showEntrustNumber
            helper.setText(R.id.tv_only_reduce_position_common_key, if (isCurrentEntrust) cl_reduce_only_str else sl_str_pl + "(" + marginCoin + ")")
            val profitLossColor = if (CpBigDecimalUtils.compareTo(
                            CpBigDecimalUtils.showSNormal(item.realizedAmount, marginCoinPrecision), "0") == 1) {
                CpColorUtil.getMainColorType(true)
            } else {
                CpColorUtil.getMainColorType(false)
            }
            helper.setTextColor(R.id.tv_only_reduce_position_common_value, if (isCurrentEntrust) ContextCompat.getColor(context, R.color.text_color) else profitLossColor)
            helper.setText(R.id.tv_only_reduce_position_common_value, if (isCurrentEntrust) only_reduce_position else CpBigDecimalUtils.showSNormal(item.realizedAmount, marginCoinPrecision))
//            helper.getView<ContractUpDownItemLayout>(R.id.item_only_reduce_position_common).contentTextColor = if (isCurrentEntrust) R.color.text_color else profitLossColor
//            helper.getView<ContractUpDownItemLayout>(R.id.item_only_reduce_position_common).content =if (isCurrentEntrust) only_reduce_position else BigDecimalUtils.showSNormal(item.realizedAmount, item.pricePrecision)
            if (item.type.equals("6")) {
                //如果 强制减仓 则显示"--"
                helper.setVisible(R.id.img_liquidation_tip, true)
                helper.getView<CpContractUpDownItemLayout>(R.id.item_entrust_value_common).content = "--"
            } else {
                helper.setVisible(R.id.img_liquidation_tip, false)
                helper.getView<CpContractUpDownItemLayout>(R.id.item_entrust_value_common).content = if (CpBigDecimalUtils.compareTo(item.avgPrice, "0") == 0) "--" else CpBigDecimalUtils.showSNormal(item.avgPrice, item.pricePrecision)
            }
            helper.getView<CpContractUpDownItemLayout>(R.id.item_entrust_value_common).title = cl_average_price_str + "(" + item.quote + ")"
            helper.getView<CpContractUpDownItemLayout>(R.id.item_volume_value_common).content = showDealNumber
            helper.getView<CpContractUpDownItemLayout>(R.id.item_volume_value_common).title = showDealUnit
            helper.getView<TextView>(R.id.tv_item_order_type_common_value).text = orderType
            helper.setVisible(R.id.tv_cancel_common, isCurrentEntrust)
            helper.setVisible(R.id.ll_order_type_common, !isCurrentEntrust)
            helper.setVisible(R.id.tv_order_type_common, !isCurrentEntrust)
            helper.setVisible(R.id.img_more, !item.type.equals("6"))
            helper.setVisible(R.id.img_liquidation_tip, (item.type.equals("6") || item.type.equals("7")))
            helper.setText(R.id.tv_order_type_common, orderStatus)

            helper.setGone(R.id.ll_item_stop_profit_loss, item.otoOrder == null)
            if (item.otoOrder != null) {
                helper.getView<CpContractUpDownItemLayout>(R.id.item_stop_profit_trigger_price_value).content= if (item.otoOrder.takerProfitTrigger.toString().equals("null")) "--" else item.otoOrder.takerProfitTrigger.toString()
                helper.getView<CpContractUpDownItemLayout>(R.id.item_stop_loss_trigger_price_value).content=if (item.otoOrder.stopLossTrigger.toString().equals("null")) "--" else item.otoOrder.stopLossTrigger.toString()
            }
        } else {
            //计划委托
            helper.setGone(R.id.ll_plan, false)
            helper.setGone(R.id.ll_common, true)
            when (sideStr) {
                "BUY" -> {
                    helper?.setTextColor(R.id.tv_type_plan, CpColorUtil.getMainColorType(true))
                }
                "SELL" -> {
                    helper?.setTextColor(R.id.tv_type_plan, CpColorUtil.getMainColorType(false))
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
            var orderStatus = when (item.status) {
                "0" -> contract_text_orderWaitInHandicap //"初始"
                "1" -> statusText2//"已过期"
                "2" -> statusText3//"已完成"
                "3" -> statusText5//"触发失败"
                "4" -> statusText6//"已撤销"
                else -> "error"
            }

            var orderTypeStr = when (item.triggerType) {
                1 -> statusText8 //止损单
                2 -> statusText7 //止盈单
                3, 4 -> statusText9//条件单
                else -> "error"
            }

            helper.run {
                setText(R.id.tv_order_type_plan, orderTypeStr)
                setText(R.id.tv_type_plan, typeStr)
                setText(R.id.tv_contract_name_plan, symbolName)
                setText(R.id.tv_time_plan, CpTimeFormatUtils.timeStampToDate(item.ctime.toLong(), "yyyy-MM-dd  HH:mm:ss"))
                setText(R.id.tv_hold_value_1_plan, CpBigDecimalUtils.showSNormal(item.triggerPrice, item.pricePrecision))
                setText(R.id.tv_trigger_price, sl_str_trigger_price + "(" + item.quote + ")")
                setText(R.id.tv_hold_value_2_plan, if (item.timeInForce.equals("2") && sideStr.equals("BUY")) sl_str_market_price_simple else CpBigDecimalUtils.showSNormal(item.price, item.pricePrecision))
                setText(R.id.tv_hold_2, cl_order_price_str + "(" + item.quote + ")")
                val unitBuff = if (contractSide == 1) item.quote else item.base
                setText(R.id.tv_hold_3_plan, if (item.timeInForce.equals("2") && openStr.equals("OPEN")) cl_open_value_str + "(" + unitBuff + ")" else showEntrustUnit)
                setText(R.id.tv_hold_value_3_plan, showEntrustNumber)
                setText(R.id.tv_hold_value_4_plan, only_reduce_position)
                setText(R.id.tv_hold_value_5_plan, orderType)
                setText(R.id.tv_hold_value_6_plan, CpTimeFormatUtils.timeStampToDate(item.mtime.toLong(), "MM-dd  HH:mm"))
                setText(R.id.tv_status_plan, orderStatus)
                setVisible(R.id.tv_status_plan, !item.status.equals("0") && !item.status.equals("1"))
                setVisible(R.id.tv_cancel_plan, isCurrentEntrust)
                setVisible(R.id.tv_status_plan, !isCurrentEntrust)
                setVisible(R.id.img_error_tips, item.status.equals("4"))

                if (isCurrentEntrust) {
                    //如果是当前委托则显示过期时间
                    setText(R.id.tv_time, cl_expration_date_str)
                    setText(R.id.tv_hold_value_6_plan, CpTimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM-dd  HH:mm"))
                } else {
                    //如果是历史委托则按照如下显示
                    //   "1" -> "已过期"   显示过期时间
                    //   "2" -> "已完成"   显示触发时间
                    //   "3" -> "触发失败"  显示过期时间
                    when (item.status) {
                        "1", "3" -> {
                            setText(R.id.tv_time, cl_expration_date_str)
                            setText(R.id.tv_hold_value_6_plan, CpTimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM-dd  HH:mm"))
                        }
                        "2" -> {
                            setText(R.id.tv_time, sl_str_trigger_time)
                            setText(R.id.tv_hold_value_6_plan, CpTimeFormatUtils.timeStampToDate(item.mtime.toLong(), "MM-dd  HH:mm"))
                        }
                        else -> {
                            setText(R.id.tv_time, cl_expration_date_str)
                            setText(R.id.tv_hold_value_6_plan, CpTimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM-dd  HH:mm"))

                        }
                    }

                }
            }
//
//
        }


    }

}