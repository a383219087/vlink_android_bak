package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.dev.NoticeBean
import java.util.ArrayList

/**
 *
 *Created by Bertking on 2018/6/23.
 */

open class NoticeAdapter(data: ArrayList<NoticeBean.NoticeInfo>) : BaseQuickAdapter<NoticeBean.NoticeInfo, BaseViewHolder>(R.layout.item_notice, data) {
    override fun convert(helper: BaseViewHolder, item: NoticeBean.NoticeInfo) {
        helper!!.setText(R.id.tv_notice_title, item?.title)
        var date = DateUtil.longToString("yyyy-MM-dd HH:mm:ss", item!!.timeLong)
        helper.setText(R.id.tv_notice_date, date)

    }
}
