package com.yjkj.chainup.new_contract.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.common.sdk.utlis.TimeFormatUtils
import com.yjkj.chainup.R
import com.yjkj.chainup.new_contract.bean.ClCurrentOrderBean
import java.util.*

/**
 * 合约计划委托
 */
class ClContractPlanEntrustAdapter(data: ArrayList<ClCurrentOrderBean>) : BaseQuickAdapter<ClCurrentOrderBean, BaseViewHolder>(R.layout.cl_item_contract_plan_entrust, data), LoadMoreModule {

    private var isCurrentEntrust = true

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

        if (openStr.equals("CLOSE")) {
            only_reduce_position = "是"
        }
        var orderType = when (item.timeInForce) {
            "1" -> "限价单"
            "2" -> "市价单"
            "3" -> "IOC"
            "4" -> "FOK"
            "5" -> "Post Only"
            else -> "error"
        }
        //0 初始, 1 已过期, 2 已完成, 3 触发失败
        var orderStatus = when (item.status) {
            "0" -> "初始"
            "1" -> "已过期"
            "2" -> "已完成"
            "3" -> "触发失败"
            else -> "error"
        }

        helper?.run {
            setText(R.id.tv_type, typeStr)
            setText(R.id.tv_contract_name, item.symbol)
            setText(R.id.tv_time, TimeFormatUtils.timeStampToDate(item.ctime.toLong(), "yyyy-MM-dd  HH:mm:ss"))
            setText(R.id.tv_hold_value_1, item.triggerPrice)
            setText(R.id.tv_hold_value_2, if (item.timeInForce.equals("2")&&sideStr.equals("BUY")) "市价" else item.price)
            setText(R.id.tv_hold_3,if (item.timeInForce.equals("2")&&openStr.equals("OPEN")) "开仓价值" else "委托数量（张）")
            setText(R.id.tv_hold_value_3, item.volume)
            setText(R.id.tv_hold_value_4, only_reduce_position)
            setText(R.id.tv_hold_value_5, orderType)
            setText(R.id.tv_hold_value_6, TimeFormatUtils.timeStampToDate(item.mtime.toLong(), "MM-dd  HH:mm"))
            setText(R.id.tv_status, orderStatus)
            setVisible(R.id.tv_status, !item.status.equals("0") && !item.status.equals("1"))
            setVisible(R.id.tv_cancel, isCurrentEntrust)
            setVisible(R.id.tv_status, !isCurrentEntrust)
        }
        addChildClickViewIds(R.id.tv_cancel)
    }
}