package com.yjkj.chainup.new_version.adapter

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.new_version.view.LabelTextView
import com.yjkj.chainup.util.getArraysSymbols

/**
 * @Author: Bertking
 * @Date：2019/3/20-7:25 PM
 * @Description: 竖版的KLine刻度
 */
class KLineScaleItemAdapterV2(scales: ArrayList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_kline_item_scale, scales) {
    val TAG = KLineScaleItemAdapterV2::class.java.simpleName
    override fun convert(helper: BaseViewHolder, item: String) {
        val boxView = helper?.getView<LabelTextView>(R.id.tv_scale)
        val itemView = helper?.getView<TextView>(R.id.tv_item)

        val temp = item?.getArraysSymbols(context)
        if (helper?.adapterPosition == data.size - 1) {
            boxView?.text = temp
            boxView?.visibility = View.VISIBLE
            itemView?.visibility = View.GONE
        } else {
            itemView?.text = temp
            itemView?.visibility = View.VISIBLE
            boxView?.visibility = View.GONE
        }

    }
}