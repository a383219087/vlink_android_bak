package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.Contract2PublicInfoManager
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.new_version.view.PositionITemView
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.DateUtils
import com.yjkj.chainup.util.TimeUtil
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019-09-11-19:33
 * @Description:
 */
class NContractHistoryEntrustAdapter(data: ArrayList<JSONObject>) : NBaseAdapter(data = data, layoutId = R.layout.item_history_order_contract) {
    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {
            val baseSymbol = optString("baseSymbol")
            val quoteSymbol = optString("quoteSymbol")
            val leverageLevel = optString("leverageLevel")
            val contractId = optString("contractId")
            val side = optString("side")
            val action = optString("action")
            val price = optString("price")
            val type = optString("type")
            val pricePrecision = optString("pricePrecision")
            val ctime = optString("ctime")
            val volume = optString("volume")
            val avgPrice = optString("avgPrice")
            val dealVolume = optString("dealVolume")
            val statusText = optString("statusText")

            helper?.run {

                setText(R.id.tv_contract_symbol, baseSymbol + quoteSymbol)

                val level = if (!Contract2PublicInfoManager.isPureHoldPosition()) {
                    " (${leverageLevel}X)"
                } else {
                    ""
                }
                setText(R.id.tv_contract_type, Contract2PublicInfoManager.getContractType(context, contractId.toInt()) + level)


                /**
                 * 买卖方
                 */
                setTextColor(R.id.tv_side, ColorUtil.getMainColorType(side == "BUY"))
                if (Contract2PublicInfoManager.isPureHoldPosition()) {
                    if (side == "BUY") {
                        setText(R.id.tv_side,  LanguageUtil.getString(context, "contract_text_long"))
                    } else {
                        setText(R.id.tv_side,  LanguageUtil.getString(context, "contract_text_short"))
                    }
                } else {
                    if (side == "BUY") {
                        if (action == "OPEN") {
                            //做多
                            setText(R.id.tv_side,  LanguageUtil.getString(context, "contract_action_long"))
                        } else {
                            //做空
                            setText(R.id.tv_side,  LanguageUtil.getString(context, "contract_balance_more"))
                        }
                        setTextColor(R.id.tv_side, ColorUtil.getMainColorType())
                    } else {
                        val text = if (action == "OPEN") {
                             LanguageUtil.getString(context, "contract_action_short")
                        } else {
                             LanguageUtil.getString(context, "contract_balance_empty")
                        }
                        setText(R.id.tv_side, text)
                        setTextColor(R.id.tv_side, ColorUtil.getMainColorType(isRise = false))
                    }
                }

                /**
                 * 价格（市价单 的价格为"市价"）
                 * （1：限价单，2：市价单）
                 */
                getView<PositionITemView>(R.id.tv_entrust_price)?.run {
                    title =  LanguageUtil.getString(context, "contract_text_trustPrice") + "(${quoteSymbol})"
                    value = if (type == "1") {
                        val price4Precision = Contract2PublicInfoManager.cutValueByPrecision(price.toString(), pricePrecision?.toInt()
                                ?: 4)
                        price4Precision
                    } else {
                        ( LanguageUtil.getString(context, "contract_action_marketPrice"))
                    }
                }


                /**
                 * 订单时间
                 */
                getView<PositionITemView>(R.id.tv_date)?.run {
                    title = LanguageUtil.getString(context, "kline_text_dealTime")
                    value = TimeUtil.instance.getTime(ctime)

                }


                /**
                 * 仓位数量（张）
                 */
                getView<PositionITemView>(R.id.tv_position_amount)?.run {
                    title =  LanguageUtil.getString(context, "contract_text_positionNumber")
                    value = volume
                    tailValueColor = ColorUtil.getMainColorType(side == "BUY")
                }

                /**
                 * 成交均价
                 */
                val avgPrice4Precision = Contract2PublicInfoManager.cutValueByPrecision(avgPrice, pricePrecision?.toInt()
                        ?: 4)
                getView<PositionITemView>(R.id.tv_avg_price)?.run {
                    title =  LanguageUtil.getString(context, "contract_text_dealAverage") + "(${quoteSymbol})"
                    value = (avgPrice4Precision)
                }

                /**
                 * 已成交
                 */
                getView<PositionITemView>(R.id.tv_deal)?.run {
                    title =  LanguageUtil.getString(context, "contract_text_dealDone") + "(${ LanguageUtil.getString(context, "contract_text_volumeUnit")})"
                    value = dealVolume
                }

                /**
                 * 类型:
                 * （1：限价单，2：市价单）
                 */
                getView<PositionITemView>(R.id.tv_type)?.run {
                    value = if (type == "1")  LanguageUtil.getString(context, "contract_text_limitPriceOrder") else  LanguageUtil.getString(context, "contract_text_typeMarket")
                }

                /**
                 * 订单状态：
                 * 0：初始状态 1：新订单   2：完全成交  3：部分成交   4：已取消  5：待撤销   6：已废弃  7：部分成交已撤销（0 1 3显示撤销按钮
                 */
                setText(R.id.tv_status, statusText)

            }
            


        }


    }
}