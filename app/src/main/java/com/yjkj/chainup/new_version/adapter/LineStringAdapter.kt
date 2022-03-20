package com.yjkj.chainup.new_version.adapter

import android.widget.RelativeLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.setGoneV3
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2019/6/3-11:17 AM
 * @Email buptjinlong@163.com
 * @description
 */
class LineStringAdapter(data: ArrayList<String>, var position: Int = 0) :
        BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_new_screening_label, data) {


    fun setSelectPosition(index: Int) {
        position = index
        notifyDataSetChanged()
    }

    override fun convert(helper: BaseViewHolder, item: String) {
        if (position == helper.adapterPosition) {
            helper.getView<RelativeLayout>(R.id.ll_layout).setBackgroundResource(R.drawable.bg_new_v5_select_style)
        } else {
            helper.getView<RelativeLayout>(R.id.ll_layout).setBackgroundResource(R.drawable.bg_new_v5_unselect_style)
        }
        helper.setText(R.id.tv_parent_content, NCoinManager.getShowMarket(item))
    }

}
