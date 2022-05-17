package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.AboutUSBean
import com.yjkj.chainup.util.isHttpUrl


/**
 * Created by Bertking on 2018/9/14.
 */

class AbountAdapter(data: ArrayList<AboutUSBean>) : BaseQuickAdapter<AboutUSBean, BaseViewHolder>(R.layout.item_about_us, data) {
    override fun convert(helper: BaseViewHolder, item: AboutUSBean) {
        helper.setText(R.id.tv_title, item.title)
        if (!item.content.isHttpUrl()){
            helper.setText(R.id.tv_content, item.content)
        }else{
            helper.setText(R.id.tv_content,"")

        }
    }
}