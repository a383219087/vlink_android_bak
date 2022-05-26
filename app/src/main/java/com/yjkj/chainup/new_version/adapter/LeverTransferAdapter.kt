package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.BigDecimalUtils
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-11-15-00:54
 * @Email buptjinlong@163.com
 * @description
 */
class LeverTransferAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_cashflow_com) {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {

        helper?.apply {
            setText(R.id.tv_date, DateUtil.longToString("yyyy/MM/dd HH:mm:ss", item?.optLong("createTime", 0)
                    ?: 0))

            var temp = item?.optString("transferType", "1") ?: "1"
            when (temp) {
                "1" -> {
                    setText(R.id.tv_status,  LanguageUtil.getString(context, "coin_to_leverage"))
                }

                "2" -> {
                    setText(R.id.tv_status,  LanguageUtil.getString(context, "leverage_to_coin"))
                }

            }

            setText(R.id.tv_count, BigDecimalUtils.divForDown(item?.optString("amount", "")
                    ?: "", 8).toPlainString())
        }

    }
}