package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.fengniao.news.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.BigDecimalUtils
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2019-11-13-17:27
 * @Description:借贷记录详情
 */
class BorrowDetailAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_borrow_detail) {
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {
            val coin = optString("coin", "")
            val type = optString("type", "")
            val returnMoney = optString("returnMoney", "0")
            val repaymentTime = optString("repaymentTime", "0")
            var precision = NCoinManager.getCoinShowPrecision(coin)



            helper?.run {
                setText(R.id.tv_coin_name, NCoinManager.getShowMarket(coin))
                /**
                 * 日期yyyy-MM-dd HH:mm:ss
                 */
                setText(R.id.tv_date, DateUtil.longToString("yyyy/MM/dd HH:mm:ss", repaymentTime.toLong()))

                setText(R.id.tv_volume, BigDecimalUtils.showNormal(BigDecimalUtils.divForDown(returnMoney, 8).toPlainString()))

                /**
                 * 归还类型：1本金，2利息，3本金+利息
                 */
                val typeText = when (type) {
                    "1" -> {
                         LanguageUtil.getString(context, "leverage_principal")
                    }

                    "2" -> {
                         LanguageUtil.getString(context,"leverage_interest")
                    }

                    else -> {
                        "${ LanguageUtil.getString(context, "leverage_principal")}+${ LanguageUtil.getString(context, "leverage_interest")}"
                    }
                }
                setText(R.id.tv_rate, typeText)

            }

        }
    }
}