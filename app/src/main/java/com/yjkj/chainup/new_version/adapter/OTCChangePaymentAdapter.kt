package com.yjkj.chainup.new_version.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.util.GlideUtils
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2018/10/13-下午4:57
 * @Email buptjinlong@163.com
 * @description 选择 支付方式
 */
open class OTCChangePaymentAdapter(data: ArrayList<JSONObject>) :
        BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_change_payment, data) {


    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        if (item?.optBoolean("used") == true) {
            GlideUtils.loadImage(context, item?.optString("icon"), helper?.getView<ImageView>(R.id.iv_payment_imageview))
            helper?.setText(R.id.item_change_payment_name, item?.optString("title"))
        }
    }

}