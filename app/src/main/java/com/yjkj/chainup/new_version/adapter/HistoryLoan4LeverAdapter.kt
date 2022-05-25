package com.yjkj.chainup.new_version.adapter

import android.os.Bundle
import androidx.core.content.ContextCompat
import android.widget.LinearLayout
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coorchice.library.SuperTextView
import com.fengniao.news.util.DateUtil
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
 * @Author: Bertking
 * @Date：2019-11-11-14:38
 * @Description: 历史委托
 *
 * TODO 字段
 */
class HistoryLoan4LeverAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_history_loan) {
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {
            val interestRate = optString("interestRate")
            val interest = optString("interest")
            val timeLong = optString("ctime")
            val borrowMoney = optString("borrowMoney")
            val symbol = "${optString("base")}/${optString("quote")}"


            val precision = NCoinManager.getCoinShowPrecision(optString("coin"))
            // 状态：1借贷 2部分归还 3全部归还 4爆仓 5穿仓
            val status = optString("status")


            helper?.run {
                setText(R.id.tv_symbol, NCoinManager.getShowMarketName(symbol))
                setText(R.id.tv_coin, NCoinManager.getShowMarket(optString("coin")))

                setText(R.id.tv_coin_title, LanguageUtil.getString(context,"common_text_coinsymbol"))
                setText(R.id.tv_amount_title, LanguageUtil.getString(context,"charge_text_volume"))
                setText(R.id.tv_rate_title, LanguageUtil.getString(context,"leverage_rate"))
                setText(R.id.tv_interest_title, LanguageUtil.getString(context,"leverage_interest"))

                /**
                 * 日期yyyy-MM-dd HH:mm:ss
                 */
                setText(R.id.tv_date, DateUtil.longToString("yyyy/MM/dd HH:mm:ss", timeLong.toLong()))

                setText(R.id.tv_amount, BigDecimalUtils.showNormal(BigDecimalUtils.divForDown(borrowMoney, 8).toPlainString()))

                // 状态：1借贷 2部分归还 3全部归还 4爆仓 5穿仓
                when (status) {
                    "1" -> {
                        setText(R.id.tv_status,  LanguageUtil.getString(context, "leverage_borrow"))
                    }

                    "2" -> {
                        setText(R.id.tv_status,  LanguageUtil.getString(context, "asste_part_return"))
                    }

                    "3" -> {
                        setText(R.id.tv_status,  LanguageUtil.getString(context, "leverage_all_return"))
                    }

                    "4" -> {
                        setText(R.id.tv_status,  LanguageUtil.getString(context, "contract_text_wipedout"))
                    }

                    "5" -> {
                        setText(R.id.tv_status,  LanguageUtil.getString(context, "asset_wear_storehouse"))
                    }
                    "6" -> {
                        setText(R.id.tv_status,  LanguageUtil.getString(context, "leverage_blowUp_end"))
                    }
                    "7" -> {
                        setText(R.id.tv_status,  LanguageUtil.getString(context, "leverage_wearStore_end"))

                    }
                }
                getView<SuperTextView>(R.id.tv_status).setSolid(ContextCompat.getColor(context, R.color.bg_card_color))
                getView<SuperTextView>(R.id.tv_status).setTextColor(ContextCompat.getColor(context, R.color.normal_text_color))

                /**
                 * 利率
                 */
                setText(R.id.tv_rate, "${BigDecimalUtils.showNormal(BigDecimalUtils.mul(interestRate, "100").toPlainString())}%")
                /**
                 * 利息
                 */
                setText(R.id.tv_interest, BigDecimalUtils.showNormal(BigDecimalUtils.divForDown(interest, 8).toPlainString()))

                var view = getView<LinearLayout>(R.id.ll_main_layout)
                view.setOnClickListener {
                    ArouterUtil.navigation(RoutePath.BorrowRecordsActivity, Bundle().apply {
                        putString(ParamConstant.ORDER_ID, item?.optString("id"))
                    })
                }

            }

        }
    }
}


