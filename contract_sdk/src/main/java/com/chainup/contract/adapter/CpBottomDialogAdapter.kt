package com.chainup.contract.adapter

import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.bean.CpTabInfo

/**
 * 底部弹出dailog适配器
 */
open class CpBottomDialogAdapter(data: ArrayList<CpTabInfo>, var position: Int) : BaseQuickAdapter<CpTabInfo, BaseViewHolder>(R.layout.cp_item_string_dialog_adapter, data) {

    override fun convert(helper: BaseViewHolder, item: CpTabInfo) {
        helper?.setText(R.id.tv_content, item?.name)
        if (position == item?.index) {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.main_blue))
        } else {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.text_color))
        }
    }

}