package com.yjkj.chainup.new_version.redpackage.adapter

import android.util.Log
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.util.DateUtils
import com.yjkj.chainup.new_version.redpackage.bean.GrantRedPackageListBean
import java.math.BigDecimal

/**
 * @Author: Bertking
 * @Date：2019-07-02-11:08
 * @Description:
 */
class GrantAdapter(data: ArrayList<GrantRedPackageListBean.RedPacket>) : BaseQuickAdapter<GrantRedPackageListBean.RedPacket
        , BaseViewHolder>(R.layout.item_grant_red_package, data), LoadMoreModule {
    val TAG = GrantAdapter::class.java.simpleName
    override fun convert(helper: BaseViewHolder, item: GrantRedPackageListBean.RedPacket) {

        Log.d(TAG, "========发出的红包:name:${item?.toString()}=====")
        /**
         * 红包类型：0.普通 1.拼手气
         */
        val redPackageType = if (item?.type == 0) {
            LanguageUtil.getString(context, "redpacket_send_identical")
        } else {
            LanguageUtil.getString(context, "redpacket_send_random")
        }
        helper?.setText(R.id.tv_type, redPackageType)


        val date = DateUtils.getYearMonthDayMS(item?.stime ?: 0)
        helper?.setText(R.id.tv_date, date)
        /**
         * 红包额度
         */
        helper?.setText(R.id.tv_amount, "${BigDecimal(item?.amount?.toString()).toPlainString()} ${NCoinManager.getShowMarket(item?.coinSymbol)}")

        /**
         * 红包状态
         *   1.领取中 2.已领完 3.已过期
         */
        val redPackageStatus = when (item?.status) {
            2 -> {
                LanguageUtil.getString(context, "redpacket_sendout_gone")
            }

            3 -> {
                LanguageUtil.getString(context, "redpacket_sendout_expired")
            }

            else -> {
                ""
            }
        }


        helper?.setText(R.id.tv_num, "$redPackageStatus ${item?.redPacketGetCount}/${item?.redPacketAllCount}" + LanguageUtil.getString(context, "unit_red_package"))


    }
}