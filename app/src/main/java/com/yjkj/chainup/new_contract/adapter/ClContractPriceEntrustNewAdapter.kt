package com.yjkj.chainup.new_contract.adapter

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.common.sdk.utlis.TimeFormatUtils
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.widget.ContractUpDownItemLayout
import com.yjkj.chainup.net.NLoadingDialog
import com.yjkj.chainup.new_contract.bean.ClCurrentOrderBean
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LogUtil
import java.math.BigDecimal
import java.util.*

/**
 * 合约限价委托
 */
class ClContractPriceEntrustNewAdapter(ctx: Context, data: ArrayList<ClCurrentOrderBean>) : BaseQuickAdapter<ClCurrentOrderBean, BaseViewHolder>(R.layout.cl_item_contract_price_entrust_new, data), LoadMoreModule {

    //是否是当前委托
    private var isCurrentEntrust = true
    private var loadDialog: NLoadingDialog? = null


    var sl_str_buy_open0 = ""
    var sl_str_sell_open0 = ""
    var contract_flat_short = ""
    var contract_flat_long = ""
    var sl_str_latest_price_simple = ""
    var sl_str_fair_price_simple = ""
    var sl_str_index_price_simple = ""
    var sl_str_trigger_price = ""
    var sl_str_execution_price = ""
    var sl_str_execution_volume = ""
    var sl_str_contracts_unit = ""
    var sl_str_market_price_simple = ""
    var sl_str_deadline = ""
    var sl_str_trigger_time = ""
    var sl_str_cancel_order = ""
    var sl_str_order_complete = ""
    var sl_str_user_canceled = ""
    var sl_str_order_timeout = ""
    var sl_str_trigger_failed = ""
    var cl_order_price_str = ""
    var cl_order_volume_str = ""
    var cl_open_value_str = ""
    var cl_average_price_str = ""
    var cl_limit_order_str = ""
    var cl_market_order_str = ""
    var cl_reduce_only_str = ""
    var contract_text_orderWaitInHandicap = ""
    var statusText2 = ""
    var statusText3 = ""
    var statusText4 = ""
    var sl_str_pl = ""
    var cl_expration_date_str = ""
    var transaction_text_dealNum = ""
    var mMultiplierCoin = ""
    var mPricePrecision = 0
    var mMultiplierPrecision = 0
    var mMultiplier = "0"
    var coUnit = 0

    init {

    }

    fun setIsCurrentEntrust(isCurrentEntrust: Boolean = true) {
        this.isCurrentEntrust = isCurrentEntrust
    }

    override fun convert(helper: BaseViewHolder, item: ClCurrentOrderBean) {
        sl_str_buy_open0 = context.getString(R.string.sl_str_buy_open0) //开多
        sl_str_sell_open0 = context.getString(R.string.sl_str_sell_open0)//开空
        contract_flat_short = context.getString(R.string.contract_flat_short)//平空
        contract_flat_long = context.getString(R.string.contract_flat_long)//平多
        sl_str_latest_price_simple = context.getString(R.string.sl_str_latest_price_simple)//最新价
        sl_str_fair_price_simple = context.getString(R.string.sl_str_fair_price_simple)//合理价
        sl_str_index_price_simple = context.getString(R.string.sl_str_index_price_simple)//指数价
        sl_str_trigger_price = context.getString(R.string.sl_str_trigger_price)//触发价格
        sl_str_execution_price = context.getString(R.string.sl_str_execution_price)//执行价格
        sl_str_execution_volume = context.getString(R.string.sl_str_execution_volume)//执行数量
        sl_str_contracts_unit = context.getString(R.string.sl_str_contracts_unit)//张
        sl_str_market_price_simple = context.getString(R.string.sl_str_market_price_simple)//市价
        sl_str_deadline = context.getString(R.string.sl_str_deadline)//到期时间
        sl_str_trigger_time = context.getString(R.string.sl_str_trigger_time)//触发时间
        sl_str_cancel_order = context.getString(R.string.sl_str_cancel_order)//撤销
        sl_str_order_complete = context.getString(R.string.sl_str_order_complete)//订单完成
        sl_str_user_canceled = context.getString(R.string.sl_str_user_canceled)//用户取消
        sl_str_order_timeout = context.getString(R.string.sl_str_order_timeout)//订单过期
        sl_str_trigger_failed = context.getString(R.string.sl_str_trigger_failed)//执行失败
        cl_order_price_str = context.getString(R.string.cl_order_price_str)//委托价格
        cl_order_volume_str = context.getString(R.string.cl_order_volume_str)//委托数量
        cl_open_value_str = context.getString(R.string.cl_open_value_str)//开仓价值
        cl_average_price_str = context.getString(R.string.cl_average_price_str)//成交均价
        cl_limit_order_str = context.getString(R.string.cl_limit_order_str)//限价单
        cl_market_order_str = context.getString(R.string.cl_market_order_str)//市价单
        transaction_text_dealNum = context.getString(R.string.transaction_text_dealNum)//成交数量
        cl_reduce_only_str = context.getString(R.string.cl_reduce_only_str)//只减仓
        sl_str_pl = context.getString(R.string.sl_str_pl)//盈亏
        cl_expration_date_str = context.getString(R.string.cl_expration_date_str)//过期时间
        contract_text_orderWaitInHandicap = context.getString(R.string.contract_text_orderWaitInHandicap)//初始订单
        statusText2 = context.getString(R.string.cl_order_statustext2)//已过期
        statusText3 = context.getString(R.string.cl_order_statustext3)//"已完成"
        statusText4 = context.getString(R.string.cl_order_statustext4)//"触发失败"


        coUnit = LogicContractSetting.getContractUint(context)

        //保证金币种
        val marginCoin = LogicContractSetting.getContractMarginCoinById(context, item.contractId.toInt())
        val marginCoinPrecision = LogicContractSetting.getContractMarginCoinPrecisionById(context, item.contractId.toInt())
        //合约面值
        val multiplier = LogicContractSetting.getContractMultiplierById(context, item.contractId.toInt())
        //合约面值单位
        val multiplierCoin = LogicContractSetting.getContractMultiplierCoinById(context, item.contractId.toInt())

        val multiplierBuff = BigDecimal(multiplier).stripTrailingZeros().toPlainString()

        //合约名称
        var symbolName = LogicContractSetting.getContractShowNameById(context, item.contractId.toInt())

        var contractSide = LogicContractSetting.getContractSideById(context, item.contractId.toInt())

        //面值精度
        val multiplierPrecision = if (multiplierBuff.contains(".")) {
            LogUtil.e("------------", multiplierBuff)
            LogUtil.e("------------", multiplierBuff.split(".".toRegex()).toTypedArray().size.toString() + "")
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
            showEntrustUnit = cl_order_volume_str + "(" + sl_str_contracts_unit + ")"
        } else {
            showEntrustUnit = cl_order_volume_str + "(" + multiplierCoin + ")"
        }

        //成交数量展示单位
        var showDealUnit = ""
        if (coUnit == 0) {
            showDealUnit = transaction_text_dealNum + "(" + sl_str_contracts_unit + ")"
        } else {
            showDealUnit = transaction_text_dealNum + "(" + multiplierCoin + ")"
        }
        if (coUnit == 0) {
            //成交数量
            showDealNumber = item.dealVolume
            //委托数量
            showEntrustNumber = item.volume
            showDealUnit = transaction_text_dealNum + "(" + sl_str_contracts_unit + ")"
        } else {
            //成交数量
            showDealNumber = BigDecimalUtils.mulStr(item.dealVolume, multiplier, multiplierPrecision)
            //委托数量
            showEntrustNumber = BigDecimalUtils.mulStr(item.volume, multiplier, multiplierPrecision)
            showDealUnit = transaction_text_dealNum + "(" + multiplierCoin + ")"
        }

        if (item.type.equals("2") && item.open.equals("OPEN")) {
            showEntrustNumber = item.volume
        }


        var openStr = item.open
        var sideStr = item.side
        var typeStr = ""
        var only_reduce_position = context.getString(R.string.cl_no_str)//"否"
        //context.getLineText("sl_str_buy_open")
        if (openStr.equals("OPEN") && sideStr.equals("BUY")) {
            typeStr = context.getString(R.string.sl_str_buy_open)//买入开多
        } else if (openStr.equals("OPEN") && sideStr.equals("SELL")) {
            typeStr = context.getString(R.string.sl_str_sell_open)//卖出开空
        } else if (openStr.equals("CLOSE") && sideStr.equals("BUY")) {
            typeStr = context.getString(R.string.sl_str_buy_close)//买入平空
        } else if (openStr.equals("CLOSE") && sideStr.equals("SELL")) {
            typeStr = context.getString(R.string.sl_str_sell_close)//卖出平多
        }
        if (openStr.equals("CLOSE")) {
            only_reduce_position = context.getString(R.string.cl_yes_str)//"是"
        }

        if (!item.isPlan) {
//            //普通
            helper.setGone(R.id.ll_plan, true)
            helper.setGone(R.id.ll_common, false)
            when (sideStr) {
                "BUY" -> {
                    helper?.setTextColor(R.id.tv_type_common, context.resources.getColor(R.color.main_green))
                }
                "SELL" -> {
                    helper?.setTextColor(R.id.tv_type_common, context.resources.getColor(R.color.main_red))
                }
                else -> {
                }
            }

            //限价单，市价单，IOC，FOK，Post Only
            var orderType = when (item.type) {
                "1" -> context.getString(R.string.cl_limit_order_str)//"限价单"
                "2" -> context.getString(R.string.cl_market_order_str)//"市价单"
                "3" -> "IOC"
                "4" -> "FOK"
                "5" -> "Post Only"
                "6" -> context.getString(R.string.cl_orderlist_typestatus4)//强制减仓
                "7" -> context.getString(R.string.cl_contract_add_text22)//仓位合并
                else -> "error"
            }

            //0 初始, 1 新订单, 2 完全成交, 3 部分成交, 4 已撤销, 5 待撤销, 6 异常订单
            var orderStatus = when (item.status) {
                "2" -> context.getString(R.string.cl_full_filled_str)//完全成交
                "3" -> context.getString(R.string.cl_partial_filled_str)//"部分成交"
                "4" -> context.getString(R.string.cl_cancelled_str)//"已撤销"
                "5" -> context.getString(R.string.cl_cancelling_str)//"待撤销"
                "6" -> context.getString(R.string.cl_abnormal_order_str)//"异常订单"
                else -> "error"
            }
            helper.setText(R.id.tv_type_common, typeStr)
            helper.setText(R.id.tv_contract_name_common, symbolName)
            helper.setText(R.id.tv_time_common, TimeFormatUtils.timeStampToDate(item.ctime.toLong(), "yyyy-MM-dd  HH:mm:ss"))
            if (item.type.equals("6")) {
                //如果 强制减仓 则显示"--"
                helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_price_common).content = "--"
            } else {
                helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_price_common).content = if (item.type.equals("2")) sl_str_market_price_simple else BigDecimalUtils.showSNormal(item.price, item.pricePrecision)
            }
            helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_price_common).title = cl_order_price_str + "(" + item.quote + ")"
            val unitBuff = if (contractSide == 1) item.quote else item.base
            helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_volume_common).title = if (item.type.equals("2") && openStr.equals("OPEN")) cl_open_value_str + "(" + unitBuff + ")" else showEntrustUnit
            helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_volume_common).content = showEntrustNumber
            helper.setText(R.id.tv_only_reduce_position_common_key, if (isCurrentEntrust) cl_reduce_only_str else sl_str_pl + "(" + marginCoin + ")")
            val profitLossColor = if (BigDecimalUtils.compareTo(BigDecimalUtils.showSNormal(item.realizedAmount, marginCoinPrecision), "0") == 1) {
                R.color.main_green
            } else {
                R.color.main_red
            }
            helper.setTextColor(R.id.tv_only_reduce_position_common_value, ContextCompat.getColor(context, if (isCurrentEntrust) R.color.text_color else profitLossColor))
            helper.setText(R.id.tv_only_reduce_position_common_value, if (isCurrentEntrust) only_reduce_position else BigDecimalUtils.showSNormal(item.realizedAmount, marginCoinPrecision))
//            helper.getView<ContractUpDownItemLayout>(R.id.item_only_reduce_position_common).contentTextColor = if (isCurrentEntrust) R.color.text_color else profitLossColor
//            helper.getView<ContractUpDownItemLayout>(R.id.item_only_reduce_position_common).content =if (isCurrentEntrust) only_reduce_position else BigDecimalUtils.showSNormal(item.realizedAmount, item.pricePrecision)
            if (item.type.equals("6")) {
                //如果 强制减仓 则显示"--"
                helper.setVisible(R.id.img_liquidation_tip, true)
                helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_value_common).content = "--"
            } else {
                helper.setVisible(R.id.img_liquidation_tip, false)
                helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_value_common).content = if (BigDecimalUtils.compareTo(item.avgPrice, "0") == 0) "--" else BigDecimalUtils.showSNormal(item.avgPrice, item.pricePrecision)
            }
            helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_value_common).title = cl_average_price_str + "(" + item.quote + ")"
            helper.getView<ContractUpDownItemLayout>(R.id.item_volume_value_common).content = showDealNumber
            helper.getView<ContractUpDownItemLayout>(R.id.item_volume_value_common).title = showDealUnit
            helper.getView<TextView>(R.id.tv_item_order_type_common_value).text = orderType
            helper.setVisible(R.id.tv_cancel_common, isCurrentEntrust)
            helper.setVisible(R.id.ll_order_type_common, !isCurrentEntrust)
            helper.setVisible(R.id.tv_order_type_common, !isCurrentEntrust)
            helper.setVisible(R.id.img_more, !item.type.equals("6"))
            helper.setVisible(R.id.img_liquidation_tip, (item.type.equals("6")||item.type.equals("7")))
            helper.setText(R.id.tv_order_type_common, orderStatus)
        } else {
            //计划委托
            helper.setGone(R.id.ll_plan, false)
            helper.setGone(R.id.ll_common, true)
            when (sideStr) {
                "BUY" -> {
                    helper?.setTextColor(R.id.tv_type_plan, context.resources.getColor(R.color.main_green))
                }
                "SELL" -> {
                    helper?.setTextColor(R.id.tv_type_plan, context.resources.getColor(R.color.main_red))
                }
                else -> {
                }
            }

            if (openStr.equals("CLOSE")) {
                only_reduce_position = context.getString(R.string.cl_yes_str)
            }
            var orderType = when (item.timeInForce) {
                "1" -> cl_limit_order_str
                "2" -> cl_market_order_str
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
                "3" -> "执行失败"//"触发失败"
                "4" -> "已撤销"//"已撤销"
                else -> "error"
            }

            helper?.run {
                setText(R.id.tv_type_plan, typeStr)
                setText(R.id.tv_contract_name_plan, symbolName)
                setText(R.id.tv_time_plan, TimeFormatUtils.timeStampToDate(item.ctime.toLong(), "yyyy-MM-dd  HH:mm:ss"))
                setText(R.id.tv_hold_value_1_plan, BigDecimalUtils.showSNormal(item.triggerPrice, item.pricePrecision))
                setText(R.id.tv_trigger_price, sl_str_trigger_price + "(" + item.quote + ")")
                setText(R.id.tv_hold_value_2_plan, if (item.timeInForce.equals("2") && sideStr.equals("BUY")) sl_str_market_price_simple else BigDecimalUtils.showSNormal(item.price, item.pricePrecision))
                setText(R.id.tv_hold_2, cl_order_price_str + "(" + item.quote + ")")
                val unitBuff = if (contractSide == 1) item.quote else item.base
                setText(R.id.tv_hold_3_plan, if (item.timeInForce.equals("2") && openStr.equals("OPEN")) cl_open_value_str + "(" + unitBuff + ")" else showEntrustUnit)
                setText(R.id.tv_hold_value_3_plan, showEntrustNumber)
                setText(R.id.tv_hold_value_4_plan, only_reduce_position)
                setText(R.id.tv_hold_value_5_plan, orderType)
                setText(R.id.tv_hold_value_6_plan, TimeFormatUtils.timeStampToDate(item.mtime.toLong(), "MM-dd  HH:mm"))
                setText(R.id.tv_status_plan, orderStatus)
                setVisible(R.id.tv_status_plan, !item.status.equals("0") && !item.status.equals("1"))
                setVisible(R.id.tv_cancel_plan, isCurrentEntrust)
                setVisible(R.id.tv_status_plan, !isCurrentEntrust)
                setVisible(R.id.img_error_tips, item.status.equals("4"))


                if (isCurrentEntrust) {
                    //如果是当前委托则显示过期时间
                    setText(R.id.tv_time, cl_expration_date_str)
                    setText(R.id.tv_hold_value_6_plan, TimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM-dd  HH:mm"))
                } else {
                    //如果是历史委托则按照如下显示
                    //   "1" -> "已过期"   显示过期时间
                    //   "2" -> "已完成"   显示触发时间
                    //   "3" -> "触发失败"  显示过期时间
                    when (item.status) {
                        "1", "3" -> {
                            setText(R.id.tv_time, cl_expration_date_str)
                            setText(R.id.tv_hold_value_6_plan, TimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM-dd  HH:mm"))
                        }
                        "2" -> {
                            setText(R.id.tv_time, sl_str_trigger_time)
                            setText(R.id.tv_hold_value_6_plan, TimeFormatUtils.timeStampToDate(item.mtime.toLong(), "MM-dd  HH:mm"))
                        }
                        else -> {
                            setText(R.id.tv_time, cl_expration_date_str)
                            setText(R.id.tv_hold_value_6_plan, TimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM-dd  HH:mm"))

                        }
                    }

                }
            }
//
//
        }


    }

}