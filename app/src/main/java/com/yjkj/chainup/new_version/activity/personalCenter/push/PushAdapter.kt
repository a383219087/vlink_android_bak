package com.yjkj.chainup.new_version.activity.personalCenter.push

import android.view.View
import android.widget.RelativeLayout
import android.widget.Switch
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.PushBean
import com.yjkj.chainup.new_version.view.AccountItemView
import com.yjkj.chainup.util.SystemUtils
import com.yjkj.chainup.util.pushOpenStatus
import com.yjkj.chainup.util.visiable
import java.util.ArrayList


/**
 * Created by Bertking on 2018/9/14.
 */

class PushAdapter(data: ArrayList<PushBean>) : BaseQuickAdapter<PushBean, BaseViewHolder>(R.layout.item_push_item, data) {
    var status: String = "1"
    override fun convert(helper: BaseViewHolder, item: PushBean) {
        val reItem = helper?.getView<RelativeLayout>(R.id.rl_content)
        val reAll = helper?.getView<AccountItemView>(R.id.rl_all)
        val type = item?.type ?: ""
        if (type == "all") {
            reAll?.visibility = View.VISIBLE
            reItem?.visibility = View.GONE
            reAll?.setTitle(item?.title ?: "")
            reAll?.setStatusText(SystemUtils.isOpenNotifications().pushOpenStatus())
            addChildClickViewIds(R.id.rl_all)
        } else {
            reAll?.visibility = View.GONE
            reItem?.visibility = View.VISIBLE
            val switchPush = helper?.getView<Switch>(R.id.switch_push_item)
            helper?.setText(R.id.tv_push_text, item?.title ?: "")
            val isShow = item?.value ?: false
            switchPush?.isChecked = isShow
            if (isShow) {
                switchPush?.setBackgroundResource(R.drawable.open)
            } else {
                switchPush?.setBackgroundResource(R.drawable.shut_down)
            }
            val isVisibility = status == "1"
            switchPush?.visibility = isVisibility.visiable()
            if (status == "1") {
                addChildClickViewIds(R.id.switch_push_item)
            }

        }
    }
}