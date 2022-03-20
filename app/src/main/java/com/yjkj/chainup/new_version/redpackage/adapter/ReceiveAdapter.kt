package com.yjkj.chainup.new_version.redpackage.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.util.DateUtils
import com.yjkj.chainup.new_version.redpackage.bean.ReceiveRedPackageBean
import com.yjkj.chainup.util.setGoneV3
import java.math.BigDecimal
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2019-07-02-11:08
 * @Description: 红包详情 & 收到红包列表的适配器
 */
class ReceiveAdapter(data: ArrayList<ReceiveRedPackageBean>, isDetail: Boolean = false) : BaseQuickAdapter<ReceiveRedPackageBean
        , BaseViewHolder>(R.layout.item_red_package_detail, data), LoadMoreModule {

    var isDetail = isDetail
    override fun convert(helper: BaseViewHolder, item: ReceiveRedPackageBean) {

        helper?.setText(R.id.tv_nickname, item?.nickName)
        helper.setGoneV3(R.id.iv_new_flag, item?.isNew == 1)

        /**
         * 红包详情显示:月日时分秒
         */
        val date = if (isDetail) {
            DateUtils.getYearMonthDayHourMinSecondMS(item?.ctime ?: 0)
        } else {
            DateUtils.getYearMonthDayMS(item?.ctime ?: 0)
        }
        helper?.setText(R.id.tv_date, date)
        /**
         * 红包额度
         */
        helper?.setText(R.id.tv_amount, "${BigDecimal(item?.amount?.toString()).toPlainString()} ${NCoinManager.getShowMarket(item?.coinSymbol)?.toUpperCase().toString()}")
        helper.setGoneV3(R.id.iv_new_flag, item?.isNew == 1)
        helper.setGoneV3(R.id.iv_lucky_flag, item?.isLucky == 1)

        helper?.setText(R.id.tv_convert,
                RateManager.getCNYByCoinName(coinName = item?.coinSymbol
                        ?: "", close = item?.amount.toString()))

    }
}