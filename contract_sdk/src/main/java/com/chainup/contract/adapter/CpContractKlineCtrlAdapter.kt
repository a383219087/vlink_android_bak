package com.yjkj.chainup.new_contract.adapter

import android.content.Context
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.utils.CpColorUtil
import com.chainup.contract.utils.CpKLineUtil
import com.chainup.contract.view.CpLabelTextView
import com.yjkj.chainup.new_contract.bean.CpKlineCtrlBean
import java.util.*


class CpContractKlineCtrlAdapter(data: ArrayList<CpKlineCtrlBean>) : BaseMultiItemQuickAdapter<CpKlineCtrlBean, BaseViewHolder>(data), LoadMoreModule {

    init {
        addItemType(1, R.layout.cp_item_kline_ctrl)
        addItemType(2, R.layout.cp_item_kline_ctrl_label)
        addItemType(3, R.layout.cp_item_kline_ctrl)
    }

    override fun convert(helper: BaseViewHolder, item: CpKlineCtrlBean) {

        when (helper.itemViewType) {
            1 -> {
                val sel=item.isSelect
//                val sel=CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf(item.time))
                helper.setText(R.id.tv_time, CpKLineUtil.getShowKLineScaleName(item.time,context))
                helper.setTextColor(R.id.tv_time,if (sel) CpColorUtil.getColor(R.color.main_blue) else  CpColorUtil.getColor(R.color.normal_text_color))
//                helper.setGone(R.id.tv_line,!sel)
            }
            2 -> {
                helper.setText(R.id.tv_scale, CpKLineUtil.getShowKLineScaleName(item.time,context))
                val tvScale= helper.getView<CpLabelTextView>(R.id.tv_scale)
                tvScale.labelBackgroundColor=if (item.isSelect) CpColorUtil.getColor(R.color.main_blue) else  CpColorUtil.getColor(R.color.normal_text_color)
                helper.setTextColor(R.id.tv_scale,if (item.isSelect) CpColorUtil.getColor(R.color.main_blue) else  CpColorUtil.getColor(R.color.normal_text_color))

//                if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf(item.time))) {
//                    helper.setGone(R.id.tv_line,false)
//                } else if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf(item.time))) {
//                    helper.setGone(R.id.tv_line,false)
//                } else if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf(item.time))) {
//                    helper.setGone(R.id.tv_line,false)
//                } else if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf(item.time))) {
//                    helper.setGone(R.id.tv_line,false)
//                } else if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf(item.time))) {
//                    helper.setGone(R.id.tv_line,false)
//                } else {
//                    helper.setGone(R.id.tv_line,true)
//                }
            }
            3 -> {
                val sel=item.isSelect
                helper.setText(R.id.tv_time, CpKLineUtil.getShowKLineScaleName(item.time,context))
                helper.setTextColor(R.id.tv_time,if (sel) CpColorUtil.getColor(R.color.main_blue) else  CpColorUtil.getColor(R.color.normal_text_color))
//                helper.setGone(R.id.tv_line,!sel)
            }
            4 -> {
            }
        }

    }

}