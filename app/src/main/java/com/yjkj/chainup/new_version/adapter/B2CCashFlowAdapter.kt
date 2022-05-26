package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.StringUtil
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2019-10-23-10:16
 * @Description: B2C的资金流水
 */
class B2CCashFlowAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_cash_flow4) {

    var isRecharge = true
        set(value) {
            field = value
        }

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {
            helper?.run {
                val title = if (isRecharge) {
                     LanguageUtil.getString(context, "coin_text_recharge")
                } else {
                     LanguageUtil.getString(context, "b2c_text_withdraw")
                }

                setText(R.id.tv_title, title)

                val createTimeAt = optString("createdAtTime", "")
                val createTime = if (StringUtil.checkStr(createTimeAt)) {
                    DateUtil.longToString("yyyy/MM/dd HH:mm", createTimeAt.toLong())
                } else {
                    ""
                }
                setText(R.id.tv_date, createTime)
                setText(R.id.tv_amount,
                        "+" + BigDecimalUtils.showNormal(optString("amount"))
                                + NCoinManager.getShowMarket(optString("coinSymbol")
                                ?: ""))
            }

        }
    }
}