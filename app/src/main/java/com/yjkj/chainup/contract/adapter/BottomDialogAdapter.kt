package com.yjkj.chainup.contract.adapter

import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.data.bean.TabInfo

/**
 * 底部弹出dailog适配器
 */
open class BottomDialogAdapter(data: ArrayList<TabInfo>, var position: Int) : BaseQuickAdapter<TabInfo, BaseViewHolder>(R.layout.item_string_dialog_adapter, data) {

    override fun convert(helper: BaseViewHolder, item: TabInfo) {
        helper?.setText(R.id.tv_content, item?.name)
        if (position == item?.index) {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.main_blue))
        } else {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.text_color))
        }
    }

}