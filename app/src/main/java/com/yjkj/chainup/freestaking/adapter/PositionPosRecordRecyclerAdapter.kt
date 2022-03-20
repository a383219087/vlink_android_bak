package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.freestaking.bean.MyPosRecordBean

class PositionPosRecordRecyclerAdapter(data: ArrayList<MyPosRecordBean.PosListBean>)
    :BaseQuickAdapter<MyPosRecordBean.PosListBean, BaseViewHolder>(R.layout.item_positionpos_record,data) {
    override fun convert(helper: BaseViewHolder, item: MyPosRecordBean.PosListBean) {
        helper?.setText(R.id.tv_name, item?.baseCoin)
        helper?.setText(R.id.tv_income_time, item?.revenueTime)
        helper?.setText(R.id.tv_number, item?.baseAmount?.toPlainString())
        helper?.setText(R.id.tv_income, item?.gainRate+"%")
        helper?.setText(R.id.tv_current_income, item?.gainAmount?.toPlainString())
    }
}