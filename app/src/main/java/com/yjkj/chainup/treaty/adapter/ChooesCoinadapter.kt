package com.yjkj.chainup.treaty.adapter

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.coin.CoinBean
import com.yjkj.chainup.util.GlideUtils

/**
 * @Author lianshangljl
 * @Date 2019/1/18-10:17 AM
 * @Email buptjinlong@163.com
 * @description
 */
open class ChooesCoinadapter(data: ArrayList<CoinBean>) :
        BaseQuickAdapter<CoinBean, BaseViewHolder>(R.layout.item_change_coin_map, data) {
    override fun convert(helper: BaseViewHolder, item: CoinBean) {

        GlideUtils.loadCoinIcon(context, item?.icon, helper?.getView(R.id.iv_coin))
        helper?.setText(R.id.tv_coin, item?.name)
        if (item?.isSelected!!) {
            helper?.getView<View>(R.id.iv_selected)?.visibility = View.VISIBLE
        } else {
            helper?.getView<View>(R.id.iv_selected)?.visibility = View.GONE
        }
    }

}