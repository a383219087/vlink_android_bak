package com.yjkj.chainup.new_version.adapter

import android.widget.RelativeLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.new_version.bean.CashFlowSceneBean
import com.yjkj.chainup.util.setGoneV3
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2019/6/3-11:16 AM
 * @Email buptjinlong@163.com
 * @description
 */
class LineSceneAdapter(data: ArrayList<CashFlowSceneBean.Scene>, var position: Int = 0) :
        BaseQuickAdapter<CashFlowSceneBean.Scene, BaseViewHolder>(R.layout.item_new_screening_label, data) {


    fun setSelectPosition(index: Int) {
        position = index
        notifyDataSetChanged()
    }

    override fun convert(helper: BaseViewHolder, item: CashFlowSceneBean.Scene) {
        if (position == helper?.adapterPosition) {
            helper.getView<RelativeLayout>(R.id.ll_layout).setBackgroundResource(R.drawable.bg_new_select_style)
            helper.setGone(R.id.cut_view, false)
        } else {
            helper?.getView<RelativeLayout>(R.id.ll_layout)?.setBackgroundResource(R.drawable.bg_new_unselect_style)
            helper.setGone(R.id.cut_view, true)
        }

        helper?.setText(R.id.tv_parent_content, item?.keyText)
    }

}
