package com.yjkj.chainup.new_contract.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.common.sdk.utlis.TimeFormatUtils
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.widget.ContractUpDownItemLayout
import com.yjkj.chainup.net_new.NLoadingDialog
import com.yjkj.chainup.new_contract.bean.ClCurrentOrderBean
import java.util.*

/**
 * 合约限价委托
 */
class ClContractPriceEntrustAdapter(data: ArrayList<ClCurrentOrderBean>) : BaseQuickAdapter<ClCurrentOrderBean, BaseViewHolder>(R.layout.cl_item_contract_price_entrust, data) , LoadMoreModule {

    //是否是当前委托
    private var isCurrentEntrust = true
    private var loadDialog: NLoadingDialog? = null


    fun setIsCurrentEntrust(isCurrentEntrust: Boolean = true) {
        this.isCurrentEntrust = isCurrentEntrust
    }

    override fun convert(helper: BaseViewHolder, item: ClCurrentOrderBean) {
        if (item == null) {
            return
        }
        var openStr = item.open
        var sideStr = item.side
        var typeStr = ""
        var only_reduce_position = "否"

        if (openStr.equals("OPEN") && sideStr.equals("BUY")) {
            typeStr = "买入开多"
        } else if (openStr.equals("OPEN") && sideStr.equals("SELL")) {
            typeStr = "卖出开空"
        } else if (openStr.equals("CLOSE") && sideStr.equals("BUY")) {
            typeStr = "买入平多"
        } else if (openStr.equals("CLOSE") && sideStr.equals("SELL")) {
            typeStr = "卖出平空"
        }

        when (sideStr) {
            "BUY" -> {
                helper?.setTextColor(R.id.tv_type, context.resources.getColor(R.color.main_green))
            }
            "SELL" -> {
                helper?.setTextColor(R.id.tv_type, context.resources.getColor(R.color.main_red))
            }
            else -> {
            }
        }

        if (openStr.equals("CLOSE")){
            only_reduce_position="是"
        }
        //限价单，市价单，IOC，FOK，Post Only
      var orderType=  when(item.type){
            "1" ->"限价单"
            "2" ->"市价单"
            "3" ->"IOC"
            "4" ->"FOK"
            "5" ->"Post Only"
          else -> "error"
      }

        //0 初始, 1 新订单, 2 完全成交, 3 部分成交, 4 已撤销, 5 待撤销, 6 异常订单
        var orderStatus=  when(item.status){
            "2" ->"完全成交"
            "3" ->"部分成交"
            "4" ->"已撤销"
            "5" ->"待撤销"
            "6" ->"异常订单"
            else -> "error"
        }
        helper.setText(R.id.tv_type, typeStr)
        helper.setText(R.id.tv_contract_name, item.symbol)
        helper.setText(R.id.tv_time,  TimeFormatUtils.timeStampToDate(item.ctime.toLong(), "yyyy-MM-dd  HH:mm:ss"))
        helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_price).content = if (item.type.equals("2")) "市价" else item.price
        helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_volume).title = if (item.type.equals("2")&&openStr.equals("OPEN")) "开仓价值" else "委托数量（张）"
        helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_volume).content = item.volume
        helper.getView<ContractUpDownItemLayout>(R.id.item_only_reduce_position).content = only_reduce_position
        helper.getView<ContractUpDownItemLayout>(R.id.item_entrust_value).content = item.avgPrice
        helper.getView<ContractUpDownItemLayout>(R.id.item_volume_value).content = item.dealVolume
        helper.getView<ContractUpDownItemLayout>(R.id.item_order_type).content = orderType
        helper.setVisible(R.id.tv_cancel,isCurrentEntrust)
        helper.setVisible(R.id.tv_order_type,!isCurrentEntrust)
        helper.setText(R.id.tv_order_type, orderStatus)

        addChildClickViewIds(R.id.tv_cancel,R.id.tv_order_type)
    }

}