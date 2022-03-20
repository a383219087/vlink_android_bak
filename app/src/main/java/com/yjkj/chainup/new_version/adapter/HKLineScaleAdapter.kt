package com.yjkj.chainup.new_version.adapter

import androidx.core.content.ContextCompat
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.util.KLineUtil
import com.yjkj.chainup.util.StringUtils

/**
 * @Author: Bertking
 * @Date：2019/3/16-7:18 PM
 * @Description: 横屏 KLine刻度适配器
 */
class HKLineScaleAdapter(scales: ArrayList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_kline_scale_h, scales) {
    override fun convert(helper: BaseViewHolder, item: String) {

        var value = when (item) {
            "4h" -> {
                "4" + LanguageUtil.getString(context,"hour")
            }

            "1day" -> {
                LanguageUtil.getString(context,"day")
            }

            "1week" -> {
                LanguageUtil.getString(context,"week")
            }

            "1month" -> {
                LanguageUtil.getString(context,"month")
            }

            else -> {
                item
            }
        }

        if (KLineUtil.getCurTime4Index() == helper?.adapterPosition) {
            helper.getView<TextView>(R.id.tv_scale)?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.kline_item_selected_shape)
            helper.getView<TextView>(R.id.tv_scale)?.setTextColor(ContextCompat.getColor(context, R.color.text_color))
        } else {
            helper?.getView<TextView>(R.id.tv_scale)?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            helper?.getView<TextView>(R.id.tv_scale)?.setTextColor(ContextCompat.getColor(context, R.color.normal_text_color))
        }
        if(helper?.adapterPosition == 0){
            helper.setText(R.id.tv_scale, "line")
        }else{
            helper?.setText(R.id.tv_scale, value)
        }
    }
}