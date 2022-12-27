package com.chainup.contract.adapter

import android.content.Context
import android.widget.ProgressBar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpTimeFormatUtils
import com.chainup.contract.bean.CpCurrentOrderBean
import com.coorchice.library.SuperTextView
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * 合约限价委托
 */
class CpContractCurrentEntrustNewAdapter(ctx: Context, data: ArrayList<CpCurrentOrderBean>) : BaseQuickAdapter<CpCurrentOrderBean, BaseViewHolder>(
        R.layout.cp_item_current_entrust, data), LoadMoreModule {

    //是否是当前委托
    private var isCurrentEntrust = true




    var cl_order_volume_str = ""
    var transaction_text_dealNum = ""
    var coUnit = 0

    init {

    }

    fun setIsCurrentEntrust(isCurrentEntrust: Boolean = true) {
        this.isCurrentEntrust = isCurrentEntrust
    }

    override fun convert(helper: BaseViewHolder, item: CpCurrentOrderBean) {

         cl_order_volume_str = context.getString(R.string.cp_extra_text8)
         transaction_text_dealNum = context.getString(R.string.cp_order_text66)
        coUnit = CpClLogicContractSetting.getContractUint(context)

        //合约面值
        val multiplier = CpClLogicContractSetting.getContractMultiplierById(context, item.contractId.toInt())
        //合约面值单位
        val multiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinById(context, item.contractId.toInt())

        val multiplierBuff = BigDecimal(multiplier).stripTrailingZeros().toPlainString()

        //合约名称
        val symbolName = CpClLogicContractSetting.getContractShowNameById(context, item.contractId.toInt())


        //面值精度
        val multiplierPrecision = if (multiplierBuff.contains(".")) {
            val index = multiplierBuff.indexOf(".")
            if (index < 0) 0 else multiplierBuff.length - index - 1
        } else {
            multiplierBuff.length
        }

        //委托数量展示单位
        var showEntrustUnit = ""
        showEntrustUnit = if (coUnit == 0) {
            cl_order_volume_str + "(" + context.getString(R.string.cp_overview_text9) + ")"
        } else {
            "$cl_order_volume_str($multiplierCoin)"
        }

        //成交数量展示单位
        var showDealUnit = ""

        showDealUnit = if (coUnit == 0) {

            transaction_text_dealNum + "(" + context.getString(R.string.cp_overview_text9) + ")"
        } else {

            //委托数量
            "$transaction_text_dealNum($multiplierCoin)"
        }



        val openStr = item.open
        val sideStr = item.side
        var typeStr = ""

        if (openStr == "OPEN" && sideStr == "BUY") {
            typeStr = context.getString(R.string.cp_order_text75)//买入开多
        } else if (openStr == "OPEN" && sideStr == "SELL") {
            typeStr = context.getString(R.string.cp_overview_text14)//卖出开空
        } else if (openStr == "CLOSE" && sideStr == "BUY") {
            typeStr = context.getString(R.string.cp_extra_text4)//买入平空
        } else if (openStr == "CLOSE" && sideStr == "SELL") {
            typeStr = context.getString(R.string.cp_extra_text5)//卖出平多
        }


        if (!item.isPlan) {

            //限价单，市价单，IOC，FOK，Post Only
            val orderType = when (item.type) {
                "1" ->  context.getString(R.string.cp_overview_text3)//"限价单"
                "2" -> context.getString(R.string.cp_overview_text4)//"市价单"
                "3" -> "IOC"
                "4" -> "FOK"
                "5" -> "Post Only"
                "6" -> context.getString(R.string.cp_extra_text6)//强制减仓
                "7" -> context.getString(R.string.cp_extra_text7)//仓位合并
                else -> "error"
            }

            when (sideStr) {
                "BUY" -> {
                    helper.getView<SuperTextView>(R.id.tv_side).solid = context.resources.getColor(R.color.main_green)
                }
                "SELL" -> {
                    helper.getView<SuperTextView>(R.id.tv_side).solid = context.resources.getColor(R.color.main_red)
                }
                else -> {
                }
            }
            val volumePercentBig = CpBigDecimalUtils.div(item.dealVolume, item.volume, 2)
            val volumePercentStr = DecimalFormat("0%").format(volumePercentBig)
            helper.setText(R.id.tv_side, typeStr)
            helper.setText(R.id.tv_coin_name, symbolName)
            helper.setText(R.id.tv_date, CpTimeFormatUtils.timeStampToDate(item.ctime.toLong(), "MM/dd HH:mm:ss"))
            helper.setText(R.id.tv_order_type, orderType)
            helper.setText(R.id.tv_price,  CpBigDecimalUtils.showSNormal(item.price))
//            helper.setText(R.id.tv_volume, if (CpBigDecimalUtils.compareTo(item.avgPrice,"0")==0) "--" else item.avgPrice)
            helper.setText(R.id.tv_totalvolume, (if (coUnit == 0) item.dealVolume else CpBigDecimalUtils.mulStr(item.dealVolume, multiplier, multiplierPrecision)) )
            helper.setText(R.id.tv_dealvolume, if (coUnit == 0) item.volume else CpBigDecimalUtils.mulStr(item.volume, multiplier, multiplierPrecision))
            if (item.otoOrder != null) {
                val takerProfitTrigger = if (item.otoOrder.takerProfitTrigger.toString() == "null") "--" else item.otoOrder.takerProfitTrigger.toString()
                val stopLossTrigger = if (item.otoOrder.stopLossTrigger.toString() == "null") "--" else item.otoOrder.stopLossTrigger.toString()
                helper.setText(R.id.tv_deal, "$takerProfitTrigger/$stopLossTrigger")
            }
            helper.setText(R.id.tv_dealvolume_key,  showDealUnit)
            helper.setText(R.id.tv_totalvolume_key, showEntrustUnit)
//            val pbDealVolume = helper.getView<ProgressBar>(R.id.pb_deal_volume)
//            pbDealVolume.progress = volumePercentStr.replace("%", "").toInt()
        }
    }

}