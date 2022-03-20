package com.yjkj.chainup.freestaking.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.freestaking.bean.UserGainListBean
import com.yjkj.chainup.freestaking.formatAmount
import com.yjkj.chainup.manager.NCoinManager

/**
 * 收益明细的Adapter
 */
class IncomeRecyclerAdapter(data:ArrayList<UserGainListBean>?):BaseQuickAdapter<UserGainListBean,BaseViewHolder>(R.layout.item_income_layout,data) {
    override fun convert(helper: BaseViewHolder, item: UserGainListBean) {
        helper?.setText(R.id.tv_income_time, item?.gainTime)
        helper?.setText(R.id.tv_income_number, item?.gainAmount?.formatAmount(NCoinManager.getCoinShowPrecision(item?.gainCoin.toString()))?.toPlainString())
    }

}