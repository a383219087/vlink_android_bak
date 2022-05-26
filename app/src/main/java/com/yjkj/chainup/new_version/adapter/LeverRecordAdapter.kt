package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.util.setGoneV3
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author lianshangljl
 * @Date 2019-11-14-23:03
 * @Email buptjinlong@163.com
 * @description
 */
class LeverRecordAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data, R.layout.item_lever_record) {

    var status = ParamConstant.CURRENT_TYPE

    override fun convert(helper: BaseViewHolder, item: JSONObject) {


        var symbol = item?.optString("showName")
        var coin = ""
        var time = "0"
        var amount = "0"
        var interestRate = item?.optString("interestRate", "") ?: "0"
        var precision = ParamConstant.NORMAL_PRECISION
        helper?.apply {
            when (status) {
                ParamConstant.CURRENT_TYPE -> {
                    coin = item?.optString("coin", "") ?: ""
                    time = item?.optString("ctime", "") ?: ""
                    amount = item?.optString("borrowMoney", "") ?: ""
                    setText(R.id.tv_status_text,  LanguageUtil.getString(context, "leverage_current_borrow"))
                    setText(R.id.tv_no_number_title,  LanguageUtil.getString(context, "leverage_noreturn_amount"))
                    setText(R.id.tv_not_interest_title,  LanguageUtil.getString(context, "leverage_noreturn_interest"))
                    setText(R.id.tv_not_interest, BigDecimalUtils.divForDown(item?.optString("oweInterest", "0"), precision).toPlainString())
                    setText(R.id.tv_no_number, BigDecimalUtils.divForDown(item?.optString("oweAmount", "0")
                            ?: "0", precision).toPlainString())

                    setText(R.id.tv_charge, BigDecimalUtils.divForDown(amount, precision).toPlainString())
                    setText(R.id.tv_rate, "${BigDecimalUtils.mul(interestRate, "100").toPlainString()}%")
                }
                ParamConstant.HISTORY_TYPE -> {
                    setText(R.id.tv_status_text,  LanguageUtil.getString(context, "leverage_history_borrow"))
                    coin = item?.optString("coin", "") ?: ""
                    time = item?.optString("ctime", "") ?: ""
                    amount = item?.optString("borrowMoney", "") ?: ""
                    setText(R.id.tv_no_number_title,  LanguageUtil.getString(context, "leverage_interest"))
                    setText(R.id.tv_not_interest_title,  LanguageUtil.getString(context, "contract_text_type"))
                    setText(R.id.tv_no_number, BigDecimalUtils.divForDown(item?.optString("interest", "0")
                            ?: "0", precision).toPlainString())
                    var temp = item?.optString("status", "1") ?: "1"
                    when (temp) {
                        "1" -> {
                            setText(R.id.tv_not_interest,  LanguageUtil.getString(context, "leverage_borrow"))
                        }

                        "2" -> {
                            setText(R.id.tv_not_interest,  LanguageUtil.getString(context, "asste_part_return"))
                        }

                        "3" -> {
                            setText(R.id.tv_not_interest,  LanguageUtil.getString(context, "leverage_all_return"))
                        }

                        "4" -> {
                            setText(R.id.tv_not_interest,  LanguageUtil.getString(context, "contract_text_wipedout"))
                        }

                        "5" -> {
                            setText(R.id.tv_not_interest,  LanguageUtil.getString(context, "asset_wear_storehouse"))
                        }
                        "6" -> {
                            setText(R.id.tv_not_interest,  LanguageUtil.getString(context, "leverage_blowUp_end"))
                        }
                        "7" -> {
                            setText(R.id.tv_not_interest,  LanguageUtil.getString(context, "leverage_wearStore_end"))
                        }
                    }
                    setText(R.id.tv_rate, "${BigDecimalUtils.mul(interestRate, "100").toPlainString()}%")
                    setText(R.id.tv_charge, BigDecimalUtils.divForDown(amount, precision).toPlainString())
                }
                ParamConstant.TRANSFER_TYPE -> {
                    coin = item?.optString("coinSymbol", "") ?: ""
                    time = item?.optString("createTime", "") ?: ""
                    amount = item?.optString("amount", "") ?: ""
                    setText(R.id.tv_status_text,  LanguageUtil.getString(context, "transfer_text_record"))
                    setText(R.id.tv_no_number_title,  LanguageUtil.getString(context, "orientation"))
                    setText(R.id.tv_not_interest_title,  LanguageUtil.getString(context, "contract_text_type"))
                    setGoneV3(R.id.tv_charge_title, false)
                    setGoneV3(R.id.tv_charge, false)
                    setText(R.id.tv_rate_title,  LanguageUtil.getString(context, "charge_text_volume"))
                    setText(R.id.tv_rate, BigDecimalUtils.divForDown(amount, precision).toPlainString())

                    var temp = item?.optString("transferType", "1") ?: "1"
                    when (temp) {
                        "1" -> {
                            setText(R.id.tv_no_number,  LanguageUtil.getString(context, "asset_into"))
                            setText(R.id.tv_not_interest,  LanguageUtil.getString(context, "coin_to_leverage"))
                        }

                        "2" -> {
                            setText(R.id.tv_no_number,  LanguageUtil.getString(context, "leverage_roll_out"))
                            setText(R.id.tv_not_interest,  LanguageUtil.getString(context, "leverage_to_coin"))
                        }

                    }
                }
            }

            setText(R.id.tv_symbol, NCoinManager.getShowMarketName(symbol))
            setText(R.id.tv_coin, NCoinManager.getShowMarket(coin))


            if (StringUtil.checkStr(time) && !TextUtils.isEmpty(time)) {
                setText(R.id.tv_time_text, DateUtil.longToString("yyyy/MM/dd HH:mm:ss", time.toLong()))
            }
        }


    }

}