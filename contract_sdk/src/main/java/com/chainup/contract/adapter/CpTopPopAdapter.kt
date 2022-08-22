package com.chainup.contract.adapter

import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.bean.CpTabInfo

/**
 * 底部弹出dailog适配器
 */
open class CpTopPopAdapter(data: ArrayList<CpTabInfo>, var position: Int) : BaseQuickAdapter<CpTabInfo, BaseViewHolder>(R.layout.cp_item_string_top_pop_adapter, data) {

    override fun convert(helper: BaseViewHolder, item: CpTabInfo) {
        helper.setText(R.id.tv_content, item.name)
        if (position == helper.adapterPosition) {
            helper.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.main_blue))
        } else {
            helper.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.text_color))
        }
        helper.setGone(R.id.img_line,(data.size-1==position))
    }

}