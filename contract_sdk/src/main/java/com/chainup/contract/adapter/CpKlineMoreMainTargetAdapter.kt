package com.chainup.contract.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.utils.CpColorUtil
import com.chainup.contract.utils.CpKLineUtil


open class CpKlineMoreMainTargetAdapter(data: ArrayList<CpTabInfo>) : BaseQuickAdapter<CpTabInfo, BaseViewHolder>(R.layout.cp_item_kline_ctrl_more, data) {

    override fun convert(helper: BaseViewHolder, item: CpTabInfo) {
        helper?.setText(R.id.tv_content, item?.name)
        if (CpKLineUtil.getMainIndex()==helper.adapterPosition){
            helper?.setTextColor(R.id.tv_content,CpColorUtil.getColor(R.color.main_color))
            helper?.setBackgroundResource(R.id.tv_content,R.drawable.cp_border_corner_blue)
        }else{
            helper?.setTextColor(R.id.tv_content,CpColorUtil.getColor(R.color.normal_text_color))
            helper?.setBackgroundResource(R.id.tv_content,R.drawable.cp_border_grey_fill)
        }

    }

}