package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.fund.CashFlowBean
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.DateUtils

/**
 * @Description: 提币记录
 */
class WithDrawAdapter() :
    CashFlow4Adapter("",R.layout.item_cash_flow4_new), LoadMoreModule {
    override fun convert(holder: BaseViewHolder, item: CashFlowBean.Finance) {

        holder.setText(R.id.tv_title, item.transactionType_text)

        holder.setText(R.id.tv_amount, BigDecimalUtils.showSNormal(item.amount))

        holder.setText(R.id.tv_symbol, item.symbol)

        holder.setText(R.id.tv_date, DateUtils.getYearMonthDayHourMinSecond(item.createdAtTime))

    }
}