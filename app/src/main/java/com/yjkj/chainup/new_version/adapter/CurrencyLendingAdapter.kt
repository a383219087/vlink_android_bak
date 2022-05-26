package com.yjkj.chainup.new_version.adapter

import android.os.Bundle
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.BigDecimalUtils
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2019-11-13-19:52
 * @Email buptjinlong@163.com
 * @description
 */
class CurrencyLendingAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_currency_lending_records_adapter) {
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {
            val timeLong = optString("ctime", "")


            // 状态：1借贷 2部分归还 3全部归还 4爆仓 5穿仓
            val status = optString("status", "")
//            var precision = NCoinManager.getCoinShowPrecision(optString("coin", ""))
            var precision = 8

            helper?.run {
                setText(R.id.tv_symbol, NCoinManager.getShowMarket(optString("coin", "")))

                setText(R.id.tv_coin_title,  LanguageUtil.getString(context, "charge_text_volume"))

                setText(R.id.tv_coin, BigDecimalUtils.divForDown(optString("borrowMoney", ""), precision).toPlainString())


                setText(R.id.tv_amount_title,  LanguageUtil.getString(context, "leverage_rate"))
                setText(R.id.tv_amount, BigDecimalUtils.divForDown(BigDecimalUtils.mul(optString("interestRate", "0"), "100").toPlainString(), 2).toPlainString() + "%")


                setText(R.id.tv_rate_title,  LanguageUtil.getString(context, "leverage_noreturn_amount"))

                setText(R.id.tv_rate, BigDecimalUtils.divForDown(optString("oweAmount", ""), precision).toPlainString())

                setText(R.id.tv_interest_title,  LanguageUtil.getString(context, "leverage_noreturn_interest"))
                setText(R.id.tv_interest, BigDecimalUtils.divForDown(optString("oweInterest", ""), precision).toPlainString())


                /**
                 * 日期yyyy-MM-dd HH:mm:ss
                 */
                setText(R.id.tv_date, DateUtil.longToString("yyyy/MM/dd HH:mm:ss", timeLong.toLong()))


                when (status) {
                    "1" -> {
                    }

                    "2" -> {

                    }

                    "3" -> {

                    }

                    "4" -> {

                    }

                    "5" -> {

                    }
                }

                setText(R.id.tv_status,  LanguageUtil.getString(context, "asset_give_back"))

                var text = getView<SuperTextView>(R.id.tv_status)
                text.setOnClickListener {
                    ArouterUtil.navigation(RoutePath.GiveBackActivity, Bundle().apply {
                        putString(ParamConstant.JSON_BEAN, item.toString())
                    })
                }
            }


        }
    }
}