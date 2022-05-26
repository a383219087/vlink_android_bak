package com.yjkj.chainup.new_version.activity.oldgrid.adapter

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.util.DateUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.activity.oldgrid.OldGridStopStrategyListener
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.wedegit.GridTextView
import org.json.JSONObject
import java.util.*

/**
 * @Author lianshangljl
 * @Date 2021/2/3-3:56 PM
 * @Email buptjinlong@163.com
 * @description
 */
class OldAiGridAdapter(data: ArrayList<JSONObject>, var listener: OldGridStopStrategyListener?, var isHistory: Boolean) : BaseQuickAdapter<JSONObject,
        BaseViewHolder>(R.layout.item_grid_adapter, data), LoadMoreModule {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        item?.run {

            var startTime = optString("startTime")
            var ctime = optString("ctime")
            var endTime = optString("endTime")
            if (!isHistory || endTime == "0") {
                endTime = System.currentTimeMillis().toString()
            }
            var symbol = optString("symbol")

            var coinBase = symbol.split("/")[0]
            var coinQuote = symbol.split("/")[1]

            var name = NCoinManager.getMarket4Name((coinBase + coinQuote).toLowerCase())
            var pricePrecision = name.optInt("price")
            var volumePrecision = name.optInt("volume")

            var time = BigDecimalUtils.sub(endTime, startTime).toPlainString()


            if (BigDecimalUtils.compareTo(time, "0") != 0) {
                val day: Long = time.toLong() / (1000 * 3600 * 24)
                val hour: Long = (time.toLong() - day * (1000 * 3600 * 24)) / (1000 * 3600)
                val minute: Long = (time.toLong() - day * (1000 * 3600 * 24) - hour * (1000 * 3600)) / (1000 * 60)
                helper?.setText(R.id.tv_running_time, "${LanguageUtil.getString(context, "quant_run_time")} $day${LanguageUtil.getString(context, "noun_date_day")}$hour${LanguageUtil.getString(context, "noun_date_hour")}$minute${LanguageUtil.getString(context, "noun_date_minute")}")
            } else {
                helper?.setText(R.id.tv_running_time, "${LanguageUtil.getString(context, "quant_run_time")} 0${LanguageUtil.getString(context, "noun_date_day")}0${LanguageUtil.getString(context, "noun_date_hour")}0${LanguageUtil.getString(context, "noun_date_minute")}")
            }
            if (startTime == "0") {
                helper?.setText(R.id.tv_running_time, "${LanguageUtil.getString(context, "quant_run_time")} 0${LanguageUtil.getString(context, "noun_date_day")}0${LanguageUtil.getString(context, "noun_date_hour")}0${LanguageUtil.getString(context, "noun_date_minute")}")
            }


            var updateTime2 = ""
            var updateTime1 = ""
            if (StringUtil.checkStr(ctime) && BigDecimalUtils.compareTo(ctime, "0") != 0) {
                updateTime1 = DateUtil.longToString("yyyy/MM/dd HH:mm", ctime.toLong())
                updateTime2 = DateUtil.longToString("yyyy/MM/dd HH:mm:ss", ctime.toLong())
            } else {
                ""
            }


            var configParamMap = optJSONObject("configParamMap")

            /*价格区间 高*/
            var highestPrice = configParamMap.optString("highestPrice")
            /*价格区间 低*/
            var lowestPrice = configParamMap.optString("lowestPrice")
            var gridLineType = configParamMap.optString("gridLineType")
            var totalQuoteAmount = configParamMap.optString("totalQuoteAmount")
            var totalBaseAmount = configParamMap.optString("totalBaseAmount")
            var gridNumber = configParamMap.optString("gridNumber")


            helper?.setText(R.id.tv_symbol, NCoinManager.getShowMarketName(symbol))
            helper?.setText(R.id.tv_time, updateTime1)

            helper?.getView<GridTextView>(R.id.gt_grid_profits).setTitleContent("${LanguageUtil.getString(context, "quant_grid_profit")}(${NCoinManager.getShowMarket(coinQuote)})")
            helper?.getView<GridTextView>(R.id.gt_grid_profits).setContentText(BigDecimalUtils.divForDown(optString("totalProfit"), pricePrecision).toPlainString())

            helper?.getView<GridTextView>(R.id.gt_annual_earnings).setContentText(BigDecimalUtils.divForDown(optString("annualizedYield"), 2).toPlainString() + "%")

            helper?.getView<GridTextView>(R.id.gt_position_and).setTitleContent("${LanguageUtil.getString(context, "quant_position_profit")}(${NCoinManager.getShowMarket(coinQuote)})")
            helper?.getView<GridTextView>(R.id.gt_position_and).setContentText(BigDecimalUtils.divForDown(optString("positionProfit"), pricePrecision).toPlainString())

            helper?.getView<GridTextView>(R.id.gt_price_range).setContentTextInterval("${BigDecimalUtils.divForDown(lowestPrice, pricePrecision).toPlainString()}~${BigDecimalUtils.divForDown(highestPrice, pricePrecision)}")


            if (gridLineType == "1") {
                helper?.getView<GridTextView>(R.id.gt_network_type).setContentTextInterval(context.getString(R.string.quant_grid_line_type1))
            } else {
                helper?.getView<GridTextView>(R.id.gt_network_type).setContentTextInterval(context.getString(R.string.quant_grid_line_type2))
            }

            if (totalBaseAmount.isNotEmpty()) {
                helper?.getView<GridTextView>(R.id.gt_investment_assets).setContentTextInterval("${BigDecimalUtils.divForDown(totalQuoteAmount, pricePrecision).toPlainString()} ${NCoinManager.getShowMarket(coinQuote)}+${BigDecimalUtils.divForDown(totalBaseAmount, volumePrecision).toPlainString()} ${NCoinManager.getShowMarket(coinBase)}")
            } else {
                helper?.getView<GridTextView>(R.id.gt_investment_assets).setContentTextInterval("${BigDecimalUtils.divForDown(totalQuoteAmount, pricePrecision)} ${NCoinManager.getShowMarket(coinQuote)}")
            }



            helper?.getView<GridTextView>(R.id.gt_grid_number).setContentTextInterval(BigDecimalUtils.divForDown(gridNumber, 0).toPlainString())

            helper?.getView<GridTextView>(R.id.gt_entrust_time).setContentTextInterval(updateTime2)

            helper?.getView<TextView>(R.id.tv_check_details).setOnClickListener {
                ArouterUtil.navigation(RoutePath.GridExecutionDetailsActivity, Bundle().apply {
                    putString(ParamConstant.GIRD_ID, optString("id"))
                    putString(ParamConstant.GIRD_COIN, coinQuote)
                    putString(ParamConstant.symbol, symbol)
                })
            }
            helper?.getView<TextView>(R.id.termination_network).setOnClickListener {
                if (listener != null) {
                    Log.e("jinlong", "listener")
                    listener?.stopStrategy(optString("id"))
                }
            }
            var strategyStatus = optString("strategyStatus")
            when (strategyStatus) {
                "0", "1" -> {
                    helper?.setGone(R.id.termination_network, false)
                }
                "2", "3" -> {
                    helper?.setGone(R.id.termination_network, true)
                }
                else -> helper?.setGone(R.id.termination_network, false)
            }

        }

    }
}

