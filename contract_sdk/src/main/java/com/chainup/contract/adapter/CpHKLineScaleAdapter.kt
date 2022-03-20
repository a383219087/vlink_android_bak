package com.chainup.contract.adapter

import androidx.core.content.ContextCompat
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpKLineUtil
import com.yjkj.chainup.manager.CpLanguageUtil

/**
 * @Author: Bertking
 * @Date：2019/3/16-7:18 PM
 * @Description: 横屏 KLine刻度适配器
 */
class CpHKLineScaleAdapter(scales: ArrayList<String>) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.cp_item_kline_scale_h, scales) {
    override fun convert(helper: BaseViewHolder, item: String) {

        var value = when (item) {
            "4h" -> {
                "4" + CpLanguageUtil.getString(context,"hour")
            }

            "1day" -> {
                CpLanguageUtil.getString(context,"day")
            }

            "1week" -> {
                CpLanguageUtil.getString(context,"week")
            }

            "1month" -> {
                CpLanguageUtil.getString(context,"month")
            }

            else -> {
                CpKLineUtil.getShowKLineScaleName(item,context)
            }
        }

        if (CpKLineUtil.getCurTime4Index() == helper?.adapterPosition) {
            helper.getView<TextView>(R.id.tv_scale)?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.cp_kline_item_selected_shape)
            helper.getView<TextView>(R.id.tv_scale)?.setTextColor(ContextCompat.getColor(context, R.color.main_blue))
        } else {
            helper?.getView<TextView>(R.id.tv_scale)?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            helper?.getView<TextView>(R.id.tv_scale)?.setTextColor(ContextCompat.getColor(context, R.color.normal_text_color))
        }
        if(helper?.adapterPosition == 0){
            helper.setText(R.id.tv_scale,  CpKLineUtil.getShowKLineScaleName("line",context))
        }else{
            helper?.setText(R.id.tv_scale, CpKLineUtil.getShowKLineScaleName(value,context) )
        }
    }
}