package com.yjkj.chainup.new_version.adapter

import androidx.core.content.ContextCompat
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.util.KLineUtil
import com.yjkj.chainup.util.StringUtils
import com.yjkj.chainup.util.getArraysSymbols

/**
 * @Author: Bertking
 * @Date：2019/3/16-7:18 PM
 * @Description: 横屏 KLine刻度适配器
 */
class HKLineScaleAdapterV2(scales: ArrayList<String> , isblack: Boolean = false) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_kline_scale_h, scales) {
    var is_black = false
    init {
        is_black = isblack
    }

    override fun convert(helper: BaseViewHolder, item: String) {

        if (KLineUtil.getCurTime4Index() == helper?.adapterPosition) {
            helper.getView<TextView>(R.id.tv_scale)?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.kline_item_selected_shape)
            if (is_black){
                helper.getView<TextView>(R.id.tv_scale)?.setTextColor(ContextCompat.getColor(context, R.color.text_color_kline_night))
            }else{
                helper.getView<TextView>(R.id.tv_scale)?.setTextColor(ContextCompat.getColor(context, R.color.text_color))
            }

        } else {
            helper?.getView<TextView>(R.id.tv_scale)?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            if (is_black){
                helper?.getView<TextView>(R.id.tv_scale)?.setTextColor(ContextCompat.getColor(context, R.color.normal_text_color_kline_night))
            }else{
                helper?.getView<TextView>(R.id.tv_scale)?.setTextColor(ContextCompat.getColor(context, R.color.normal_text_color))
            }

        }

        helper?.setText(R.id.tv_scale, item.getArraysSymbols(context))
    }
}