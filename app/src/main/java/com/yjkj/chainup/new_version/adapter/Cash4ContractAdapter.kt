package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.treaty.bean.ContractCashFlowBean
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.DateUtils

/**
 * @Author lianshangljl
 * @Date 2019/6/25-2:15 PM
 * @Email buptjinlong@163.com
 * @description
 */
class Cash4ContractAdapter(data: ArrayList<ContractCashFlowBean.Transactions>) :
        BaseQuickAdapter<ContractCashFlowBean.Transactions, BaseViewHolder>(R.layout.item_cashflow_com, data) {
    override fun convert(helper: BaseViewHolder, item: ContractCashFlowBean.Transactions) {
        //日期
        helper.setText(R.id.tv_date, DateUtils.getYearMonthDayHourMinSecond(item?.ctimeL
                ?: 0))

        // 划转数量
        helper?.setText(R.id.tv_count, BigDecimalUtils.showNormal(item?.amountStr.toString()))

        // 类型
        helper?.setText(R.id.tv_status, item?.sceneStr)

    }

}