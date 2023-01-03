package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.fund.CashFlowBean
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.DateUtils

/**
 * @Author: Bertking
 * @Date：2019-05-15-16:30
 * @Description: 资金流水(4.0)
 */
open class CashFlow4Adapter(var status: String,var layoutRes:Int) :
        BaseQuickAdapter<CashFlowBean.Finance, BaseViewHolder>(layoutRes), LoadMoreModule {

    constructor(status: String):this(status,R.layout.item_cash_flow4)

    override fun convert(helper: BaseViewHolder, item: CashFlowBean.Finance) {

        helper?.setText(R.id.tv_date_title, LanguageUtil.getString(context, "charge_text_date"))
        helper?.setText(R.id.tv_amount_title, LanguageUtil.getString(context, "charge_text_volume"))
        helper?.setText(R.id.tv_status_title, LanguageUtil.getString(context, "charge_text_state"))

        helper?.setText(R.id.tv_title, status)

        helper.setText(R.id.tv_date, DateUtils.getYearMonthDayHourMinSecond(item.createdAtTime))

        helper?.setText(R.id.tv_status, item?.statusText)

        helper?.setText(R.id.tv_amount, BigDecimalUtils.showSNormal(item?.amount) + NCoinManager.getShowMarket(item?.coinSymbol
                ?: ""))
    }
}