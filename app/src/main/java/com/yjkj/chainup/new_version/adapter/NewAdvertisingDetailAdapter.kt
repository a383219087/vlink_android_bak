package com.yjkj.chainup.new_version.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.util.GlideUtils
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020-06-16-11:05
 * @Email buptjinlong@163.com
 * @description
 */
class NewAdvertisingDetailAdapter(data: ArrayList<JSONObject>?) : BaseQuickAdapter<JSONObject,
        BaseViewHolder>(R.layout.paymethod_item, data) {
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        var title = item?.optString("title", "") ?: ""
        var icon = item?.optString("icon")
        var img_icon = helper?.getView<ImageView>(R.id.img_icon)
        var checkbox = helper?.getView<CheckBox>(R.id.checkbox)
        checkbox?.visibility = View.GONE
        GlideUtils.loadPaymentImage(context, icon, img_icon)
        helper?.setText(R.id.textview, title)

    }

}
