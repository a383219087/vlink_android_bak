package com.yjkj.chainup.new_version.adapter

import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.OTCOrderBean
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.util.BigDecimalUtil
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.GlideUtils
import com.yjkj.chainup.util.setGoneV3

/**
 * @Author lianshangljl
 * @Date 2018/10/21-下午3:43
 * @Email buptjinlong@163.com
 * @description
 */
open class OTCOrderAdapter(data: ArrayList<OTCOrderBean.Order>) :
        BaseQuickAdapter<OTCOrderBean.Order, BaseViewHolder>(R.layout.item_order_otc, data) {


    override fun convert(helper: BaseViewHolder, item: OTCOrderBean.Order) {
        GlideUtils.loadImage4OTC(context, item?.imageUrl, helper?.getView(R.id.iv_user))
        helper.setGoneV3(R.id.iv_user_onlive, item?.isOnline == 1)
        helper?.setText(R.id.tv_nickname, "${item?.otcNickName}")

        if (item?.side == "BUY") {
            helper?.setTextColor(R.id.tv_orientation, ContextCompat.getColor(context, R.color.green))
        } else {
            helper?.setTextColor(R.id.tv_orientation, ContextCompat.getColor(context, R.color.red))
        }
        helper?.setText(R.id.tv_orientation, item?.type)
        helper?.setText(R.id.tv_time, DateUtil.longToString("yyyy-MM-dd HH:mm:ss", item?.createTime!!))
        helper?.setText(R.id.tv_status, item?.statusText)
        var precision = RateManager.getFiat4Coin(item?.paySymbol)
        var totalPriceN = BigDecimalUtils.divForDown(item?.totalPrice,precision).toPlainString()
        helper?.setText(R.id.tv_total_price, "$totalPriceN")
        helper?.setText(R.id.tv_coinSymbol,"${NCoinManager.getShowMarket(item?.coinSymbol)}")
    }


}
