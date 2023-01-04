package com.chainup.contract.adapter

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
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
import java.text.DecimalFormat


class CpContractEntrustNewAdapter(ctx: Context, data: ArrayList<CpCurrentOrderBean>) :
  BaseMultiItemQuickAdapter<CpCurrentOrderBean, BaseViewHolder>(data), LoadMoreModule {

  init {
    addItemType(1, R.layout.cp_item_current_entrust)
    addItemType(2, R.layout.cp_item_plan_entrust)
    addItemType(3, R.layout.cp_item_history_common_entrust)
    addItemType(4, R.layout.cp_item_history_plan_entrust)
  }

  //是否是当前委托
  private var isCurrentEntrust = true



  var statusText7 = ""
  var statusText8 = ""
  var statusText9 = ""

  var coUnit = 0

  init {

  }

  fun setIsCurrentEntrust(isCurrentEntrust: Boolean = true) {
    this.isCurrentEntrust = isCurrentEntrust
  }

  override fun convert(helper: BaseViewHolder, item: CpCurrentOrderBean) {

    statusText7 = context.getString(R.string.cp_order_text63)//"止盈单"
    statusText8 = context.getString(R.string.cp_order_text62)//"止损单"
    statusText9 = context.getString(R.string.cp_order_text69)//"条件单"


    coUnit = CpClLogicContractSetting.getContractUint(context)

    //保证金币种
    val marginCoin =
      CpClLogicContractSetting.getContractMarginCoinById(context, item.contractId.toInt())
    val quote = CpClLogicContractSetting.getContractQuoteById(context, item.contractId.toInt())
    val marginCoinPrecision =
      CpClLogicContractSetting.getContractMarginCoinPrecisionById(context, item.contractId.toInt())
    //合约面值
    val multiplier =
      CpClLogicContractSetting.getContractMultiplierById(context, item.contractId.toInt())
    //合约面值单位
    val multiplierCoin =
      CpClLogicContractSetting.getContractMultiplierCoinById(context, item.contractId.toInt())

    val multiplierBuff = BigDecimal(multiplier).stripTrailingZeros().toPlainString()

    //合约名称
    var symbolName =
      CpClLogicContractSetting.getContractShowNameById(context, item.contractId.toInt())

    var mSymbolPricePrecision =
      CpClLogicContractSetting.getContractSymbolPricePrecisionById(context, item.contractId.toInt())

    var contractSide =
      CpClLogicContractSetting.getContractSideById(context, item.contractId.toInt())

    //面值精度
    val multiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(context, item.contractId.toInt())
    var showDealNumber = ""
    var showEntrustNumber = ""


    //成交数量展示单位

    var openStr = item.open
    var sideStr = item.side
    var typeStr = ""

    //context.getLineText("cp_overview_text13")
    if(openStr.equals("OPEN") && sideStr.equals("BUY")) {
      typeStr = context.getString(R.string.cp_overview_text13)//买入开多
    } else if(openStr.equals("OPEN") && sideStr.equals("SELL")) {
      typeStr = context.getString(R.string.cp_overview_text14)//卖出开空
    } else if(openStr.equals("CLOSE") && sideStr.equals("BUY")) {
      typeStr = context.getString(R.string.cp_extra_text4)//买入平空
    } else if(openStr.equals("CLOSE") && sideStr.equals("SELL")) {
      typeStr = context.getString(R.string.cp_extra_text5)//卖出平多
    }





    var orderType = when(item.type) {
      "1" -> context.getString(R.string.cp_overview_text3)//"限价单"
      "2" -> context.getString(R.string.cp_overview_text4)//"市价单"
      "3" -> "IOC"
      "4" -> "FOK"
      "5" -> "Post Only"
      "6" -> context.getString(R.string.cp_extra_text6)//强制减仓
      "7" -> context.getString(R.string.cp_extra_text7)//仓位合并
      else -> "error"
    }

    var showDealUnit: String = if(coUnit == 0) {
      "(${context.getString(R.string.cp_overview_text9)})"
    } else {
      "($multiplierCoin)"
    }


    when(helper.itemViewType) {
      1 -> {

        //            //普通
        when(sideStr) {
          "BUY" -> {
            helper.getView<SuperTextView>(R.id.tv_side).solid = CpColorUtil.getMainColorType(true)
          }
          "SELL" -> {
            helper.getView<SuperTextView>(R.id.tv_side).solid = CpColorUtil.getMainColorType(false)
          }
          else -> {
          }
        }
        val volumePercentBig = CpBigDecimalUtils.div(item.dealVolume, item.volume, 2)
        val volumePercentStr = DecimalFormat("0%").format(volumePercentBig)
        helper.setText(R.id.tv_side, typeStr)
        helper.setText(R.id.tv_coin_name, symbolName)
        helper.setText(R.id.tv_date, CpTimeFormatUtils.timeStampToDate(item.ctime.toLong(), "MM/dd HH:mm"))
        helper.setText(R.id.tv_order_type, orderType)
        helper.setText(
          R.id.tv_price,
          CpBigDecimalUtils.showSNormal(item.price, item.pricePrecision)
        )
        helper.setText(
          R.id.tv_dealvolume,
          (if(coUnit == 0) item.dealVolume else CpBigDecimalUtils.mulStr(
            item.dealVolume,
            multiplier,
            multiplierPrecision
          ))
        )
        helper.setText(
          R.id.tv_totalvolume,
          if(coUnit == 0) item.volume else CpBigDecimalUtils.mulStr(
            item.volume,
            multiplier,
            multiplierPrecision
          )
        )
        helper.setText(
          R.id.tv_dealvolume_key,
          context.getString(R.string.cp_extra_text8) + showDealUnit
        )
        helper.setText(
          R.id.tv_totalvolume_key,
          context.getString(R.string.cp_order_text66) + showDealUnit
        )

        val takerProfitTrigger = item.otoOrder?.takerProfitTrigger?:"-"
        val stopLossTrigger = item.otoOrder?.stopLossTrigger?:"-"
        helper.setText(R.id.tv_deal, "$takerProfitTrigger/$stopLossTrigger")
//        val pbDealVolume = helper.getView<ProgressBar>(R.id.pb_deal_volume)
//        pbDealVolume.progress = volumePercentStr.replace("%", "").toInt()
      }
      2 -> {

        //     //限价单，市价单，IOC，FOK，Post Only
        when(sideStr) {
          "BUY" -> {
            helper.getView<SuperTextView>(R.id.tv_side).solid = CpColorUtil.getMainColorType(true)
          }
          "SELL" -> {
            helper.getView<SuperTextView>(R.id.tv_side).solid = CpColorUtil.getMainColorType(false)
          }
          else -> {
          }
        }
        var orderTypeStr = when(item.triggerType) {
          1 -> statusText8 //止损单
          2 -> statusText7 //止盈单
          3, 4 -> statusText9//条件单
          else -> "error"
        }
        helper.setText(R.id.tv_side, typeStr)
        helper.setText(R.id.tv_coin_name, symbolName)
        helper.setText(
          R.id.tv_date,
          CpTimeFormatUtils.timeStampToDate(item.ctime.toLong(), "MM-dd HH:mm")
        )
//        helper.setText(R.id.tv_order_type, orderTypeStr)
        helper.setText(R.id.tv_trigger_price, item.triggerPrice)
        helper.setText(
          R.id.tv_entrust_price,
          if(item.timeInForce.equals("2")) context.getString(R.string.cp_overview_text53) else item.price
        )
        if(openStr.equals("OPEN") && item.type.equals("2")) {
          helper.setText(
            R.id.tv_entrust_amount_key,
            context.getString(R.string.cp_extra_text9) + "(" + (if(contractSide == 1) item.quote else item.base) + ")"
          )
          helper.setText(R.id.tv_entrust_amount_value, item.volume)
        } else {
          helper.setText(
            R.id.tv_entrust_amount_key,
            context.getString(R.string.cp_order_text66) + showDealUnit
          )
          helper.setText(
            R.id.tv_entrust_amount_value,
            if(coUnit == 0) item.volume else CpBigDecimalUtils.mulStr(
              item.volume,
              multiplier,
              multiplierPrecision
            )
          )
        }

        helper.setText(
          R.id.tv_expiration_date,
          CpTimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM-dd HH:mm")
        )
        helper.setVisible(R.id.tv_only_reduce_position, openStr.equals("CLOSE"))

      }
      3 -> {
        when(sideStr) {
          "BUY" -> {
            helper.getView<SuperTextView>(R.id.tv_side).solid = CpColorUtil.getMainColorType(true)
          }
          "SELL" -> {
            helper.getView<SuperTextView>(R.id.tv_side).solid = CpColorUtil.getMainColorType(false)
          }
          else -> {
          }
        }
        var orderStatus = when(item.status) {
          "2" -> context.getString(R.string.cp_extra_text1)//完全成交
          "3" -> context.getString(R.string.cp_status_text5)//"部分成交"
          "4" ->{
            if(!item.dealVolume.isNullOrEmpty() && item.dealVolume.toDouble() > 0) {
              context.getString(R.string.cp_status_text5)//部分成交
            } else {
              context.getString(R.string.cp_status_text2)//已撤销
            }

          }
          "5" -> context.getString(R.string.cp_status_text4)//"待撤销"
          "6" -> context.getString(R.string.cp_status_text3)//"异常订单"
          else -> "error"
        }




        if(openStr == "OPEN" && item.type == "2") {
          helper.setText(
            R.id.tv_entrust_amount_key,
            context.getString(R.string.cp_extra_text9) + "(" + (if(contractSide == 1) item.quote else item.base) + ")"
          )
          helper.setText(R.id.tv_entrust_amount, item.volume)
        } else {
          helper.setText(
            R.id.tv_entrust_amount_key,
            context.getString(R.string.cp_order_text66) + showDealUnit
          )
          helper.setText(
            R.id.tv_entrust_amount,
            if(coUnit == 0) item.volume else CpBigDecimalUtils.mulStr(
              item.volume,
              multiplier,
              multiplierPrecision
            )
          )
        }
        helper.setText(R.id.tv_side, typeStr)
        helper.setText(R.id.tv_coin_name, symbolName)
        helper.setText(
          R.id.tv_date,
          CpTimeFormatUtils.timeStampToDate(item.ctime.toLong(), "MM/dd HH:mm")
        )
        helper.setText(R.id.tv_order_type, orderType)
        helper.setText(
          R.id.tv_entrust_price,
          if(item.type.equals("2")) context.getString(R.string.cp_overview_text53) else CpBigDecimalUtils.showSNormal(
            item.price,
            item.pricePrecision
          )
        )
        val prefix = if(CpBigDecimalUtils.compareTo(
            item.realizedAmount,
            "0"
          ) == -1 || CpBigDecimalUtils.compareTo(item.realizedAmount, "0") == 0
        ) "" else "+"
        val plText=prefix + CpBigDecimalUtils.showSNormal(item.realizedAmount, marginCoinPrecision);
        helper.setText(R.id.tv_pl,plText)
        if (plText.contains("-")&& plText != "--"){
          helper.setTextColor(R.id.tv_pl,CpColorUtil.getMainColorType(false))
        }else if (plText.contains("+")){
          helper.setTextColor(R.id.tv_pl,CpColorUtil.getMainColorType(true))
        }


        helper.setText(
          R.id.tv_pl_key,
          context.getString(R.string.cp_order_text8) + "(" + marginCoin + ")"
        )
        helper.setText(
          R.id.tv_deal_price,
          CpBigDecimalUtils.showSNormal(item.avgPrice, mSymbolPricePrecision)
        )
        helper.setText(
          R.id.tv_deal_amount,
          if(coUnit == 0) item.dealVolume else CpBigDecimalUtils.mulStr(
            item.dealVolume,
            multiplier,
            multiplierPrecision
          )
        )
        helper.setText(
          R.id.tv_deal_amount_key,
          context.getString(R.string.cp_extra_text8) + showDealUnit
        )
        helper.setText(R.id.tv_status_go, orderStatus)

        var tvOrderType = helper.getView<TextView>(R.id.tv_order_type);
        val nav_up = context.getResources().getDrawable(R.drawable.cp_contract_prompt);
        nav_up.setBounds(5, 0, nav_up.getMinimumWidth(), nav_up.getMinimumHeight());
        if(item.type == "6") {
          tvOrderType.setCompoundDrawables(null, null, nav_up, null);
        } else {
          tvOrderType.setCompoundDrawables(null, null, null, null);
        }

      }



      //历史委托下的计划委托
      4 -> {
        var orderStatus = when(item.status) {
          "0" ->context.getString(R.string.contract_text_orderWaitInHandicap)//"初始"
          "1" -> context.getString(R.string.cp_tip_text10)//"已过期"
          "2" -> context.getString(R.string.cp_tip_text11)//"已完成"
          "3" ->  context.getString(R.string.cp_tip_text12)//"触发失败"
          "4" ->        context.getString(R.string.cp_status_text2)//已撤销
          else -> "error"
        }
        var orderTypeNewStr = when(item.timeInForce) {
          "1" -> context.getString(R.string.cp_overview_text3)//"限价单"
          "2" -> context.getString(R.string.cp_overview_text4)//"市价单"
          "3" -> "IOC"
          "4" -> "FOK"
          "5" -> "Post Only"
          else -> "error"
        }
        helper.setText(R.id.tv_side, typeStr)
        helper.setText(R.id.tv_coin_name, symbolName)
        helper.setText(
          R.id.tv_date,
          CpTimeFormatUtils.timeStampToDate(item.ctime.toLong(), "MM/dd  HH:mm")
        )
        helper.setText(R.id.tv_status, orderStatus)
        helper.setText(R.id.tv_trigger_price, item.triggerPrice)
        helper.setText(
          R.id.tv_entrust_price,
          if(item.timeInForce.equals("2")) context.getString(R.string.cp_overview_text53) else item.price
        )
        helper.setText(
          R.id.tv_entrust_amount_value,
          if(coUnit == 0) item.volume else CpBigDecimalUtils.mulStr(
            item.volume,
            multiplier,
            multiplierPrecision
          )
        )
        helper.setText(
          R.id.tv_entrust_amount_key,
          context.getString(R.string.cp_order_text66) + showDealUnit
        )
        helper.setText(
          R.id.tv_expiration_date,
          CpTimeFormatUtils.timeStampToDate(item.expireTime.toLong(), "MM/dd HH:mm")
        )
//                helper.setVisible(R.id.tv_only_reduce_position, openStr.equals("CLOSE"))
        helper.setText(R.id.tv_only_reduce_position, orderTypeNewStr)

      }
    }

  }

}