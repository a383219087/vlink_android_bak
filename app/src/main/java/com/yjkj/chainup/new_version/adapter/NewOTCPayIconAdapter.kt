package com.yjkj.chainup.new_version.adapter

import android.app.Activity
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.PersonAdsBean
import com.yjkj.chainup.util.GlideUtils

/**
 * @Author lianshangljl
 * @Date 2019/5/23-11:44 AM
 * @Email buptjinlong@163.com
 * @description
 */
open class NewOTCPayIconAdapter(data: ArrayList<PersonAdsBean.Payments>) :
        BaseQuickAdapter<PersonAdsBean.Payments, BaseViewHolder>(R.layout.item_otc_pay_icon_adapter, data) {


    override fun convert(helper: BaseViewHolder, item: PersonAdsBean.Payments) {
        var options = RequestOptions().placeholder(R.drawable.ic_sample)
                .error(R.drawable.ic_sample)
        when (item?.key) {
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

        GlideUtils.load(context as Activity, item?.icon, helper?.getView(R.id.item_otc_icon), options)
    }

}
