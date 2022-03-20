package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.HelpCenterBean

/**
 * Created by Bertking on 2018/7/14.
 */

open class HelpCenterAdapter(data: ArrayList<HelpCenterBean>?) : BaseQuickAdapter<HelpCenterBean, BaseViewHolder>(R.layout.item_help_center, data) {
    override fun convert(helper: BaseViewHolder, item: HelpCenterBean) {
//        helper!!.setText(R.id.tv_title, item?.title)

    }
}
