package com.chainup.contract.adapter

import android.view.View
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R

/**
 * @Author lianshangljl
 * @Date 2019/3/8-5:37 PM
 * @Email buptjinlong@163.com
 * @description
 */
open class CpNewDialogAdapter(data: ArrayList<String>, var position: Int) : BaseQuickAdapter<String, BaseViewHolder>(
    R.layout.cp_item_string_dialog_adapter, data) {

    var listSize = 0

    fun setList(index: Int) {
        listSize = index
    }

    override fun convert(helper: BaseViewHolder, item: String) {
        helper?.setText(R.id.tv_content, item)
        if (position == helper?.position) {
            helper.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.main_blue))
        } else {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.text_color))
        }
        if (helper.adapterPosition == (listSize - 1)) {
            helper?.getView<View>(R.id.view_line)?.visibility = View.GONE
        } else {
            helper?.getView<View>(R.id.view_line)?.visibility = View.VISIBLE
        }
    }

}