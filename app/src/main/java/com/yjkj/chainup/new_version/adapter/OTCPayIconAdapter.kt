package com.yjkj.chainup.new_version.adapter

import android.app.Activity
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.util.GlideUtils
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2018/10/11-下午4:47
 * @Email buptjinlong@163.com
 * @description 首页支付icon
 */
open class OTCPayIconAdapter(data: ArrayList<JSONObject>?) :
        BaseQuickAdapter<JSONObject,
                BaseViewHolder>(R.layout.item_otc_pay_icon_adapter, data) {


    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        var options = RequestOptions().placeholder(R.drawable.ic_sample)
                .error(R.drawable.ic_sample)
        var key = item?.optString("key")
        var icon = item?.optString("icon")

        when (key) {
            "otc.payment.wxpay" -> {
                options = RequestOptions().placeholder(R.drawable.wechat).error(R.drawable.wechat)
            }

            "otc.payment.alipay" -> {
                options = RequestOptions().placeholder(R.drawable.alipay).error(R.drawable.alipay)
            }

            "otc.payment.domestic.bank.transfer" -> {
                options = RequestOptions().placeholder(R.drawable.bankcard).error(R.drawable.bankcard)
            }
        }

        GlideUtils.load(context as Activity, icon, helper?.getView(R.id.item_otc_icon), options)

    }

}