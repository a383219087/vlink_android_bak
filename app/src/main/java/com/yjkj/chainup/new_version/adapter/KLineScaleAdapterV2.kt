package com.yjkj.chainup.new_version.adapter

import androidx.appcompat.widget.AppCompatRadioButton
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.KLineUtil
import com.yjkj.chainup.new_version.view.CustomCheckBoxView
import com.yjkj.chainup.util.getArraysSymbols
import org.jetbrains.anko.backgroundColorResource

/**
 * @Author: Bertking
 * @Date：2019/3/20-7:25 PM
 * @Description: 竖版的KLine刻度
 */
class KLineScaleAdapterV2(scales: ArrayList<String>,isblack:Boolean = false) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_kline_scale_v2, scales) {
    val TAG = KLineScaleAdapterV2::class.java.simpleName
    var is_black = false
    init {
        is_black = isblack
    }
    override fun convert(helper: BaseViewHolder, item: String) {
        val boxView = helper.getView<AppCompatRadioButton>(R.id.cbtn_view)
        boxView.text = item.getArraysSymbols(context)
        boxView.isChecked = KLineUtil.getCurTime() == item
        if (is_black){
            boxView.setBackgroundResource(R.drawable.bg_kline_options_night)
        }
    }
}