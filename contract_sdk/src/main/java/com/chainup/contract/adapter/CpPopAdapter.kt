package com.chainup.contract.adapter

import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.bean.CpTabInfo

/**
 * 底部弹出dailog适配器
 */
open class CpPopAdapter(data: ArrayList<CpTabInfo>, var position: Int) : BaseQuickAdapter<CpTabInfo, BaseViewHolder>(R.layout.cp_item_string_pop_adapter, data) {

    override fun convert(helper: BaseViewHolder, item: CpTabInfo) {
        helper?.setText(R.id.tv_content, item?.name)
        if (position == item?.index) {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.main_blue))
            if (position==1){
                helper?.setBackgroundResource(R.id.tv_content,R.drawable.cp_top_coin_sel_bg)
            }else if (position==(data.size)){
                helper?.setBackgroundResource(R.id.tv_content,R.drawable.cp_bottom_coin_sel_bg)
            }else{
                helper?.setBackgroundColor(R.id.tv_content,ContextCompat.getColor(context, R.color.trade_search_radius_color))
            }
        } else {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.text_color))
            if (item?.index==1){
                helper?.setBackgroundResource(R.id.tv_content,R.drawable.cp_top_coin_nosel_bg)
            }else if (item?.index==(data.size)){
                helper?.setBackgroundResource(R.id.tv_content,R.drawable.cp_bottom_coin_nosel_bg)
            }else{
                helper?.setBackgroundColor(R.id.tv_content,ContextCompat.getColor(context, R.color.bg_card_color))

            }
        }
    }

}