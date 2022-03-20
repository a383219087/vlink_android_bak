package com.yjkj.chainup.new_version.adapter

import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.new_version.view.NewReleaseListener
import com.yjkj.chainup.util.GlideUtils
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020-06-08-21:28
 * @Email buptjinlong@163.com
 * @description
 */
class NewReleaseAdvertisiongAdapter(data: ArrayList<JSONObject>?, var listener: NewReleaseListener) : BaseQuickAdapter<JSONObject,
        BaseViewHolder>(R.layout.paymethod_item, data) {
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        var title = item?.optString("title", "") ?: ""
        var icon = item?.optString("icon")
        var img_icon = helper?.getView<ImageView>(R.id.img_icon)
        var checkbox = helper?.getView<CheckBox>(R.id.checkbox)
        GlideUtils.loadPaymentImage(context, icon, img_icon)
        helper?.setText(R.id.textview, title)
        helper?.getView<LinearLayout>(R.id.checkbox_layout)?.setOnClickListener {
            if (null != listener) {
                if (item != null) {
                    if (checkbox != null) {
                        checkbox?.isChecked = !checkbox?.isChecked
                        listener?.addOrRemovePaymethodListener(checkbox, checkbox?.isChecked, item)
                    }
                }
            }
        }
    }

}
