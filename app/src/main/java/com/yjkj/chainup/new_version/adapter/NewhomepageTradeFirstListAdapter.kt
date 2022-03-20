package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.view.LineChartDataSet
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.wedegit.SimpleLineChart
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020-04-29-22:03
 * @Email buptjinlong@163.com
 * @description
 */
open class NewhomepageTradeFirstListAdapter : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_new_home_page_trade_first) {


    var klineDataMap: HashMap<String, List<Entry>> = hashMapOf()

    fun setKlineData(data: HashMap<String, List<Entry>>) {
        klineDataMap = data
        notifyDataSetChanged()
    }

    override fun convert(helper: BaseViewHolder, item: JSONObject) {


        var klineKey = ArrayList(klineDataMap.keys)
        if(klineKey.isNotEmpty()){
            for (it: Int in 0 until klineKey.size) {
                var key = klineKey[it]
                var date = klineDataMap.get(key)
                if (klineKey[it] == item?.optString("symbol")) {
                    helper?.getView<SimpleLineChart>(R.id.slc_simplelinechart)?.data = LineData(LineChartDataSet(date
                            ?: arrayListOf()))
                    break
                }
            }
        }


        var name = NCoinManager.showAnoterName(item)
        if (StringUtil.checkStr(name) && name.contains("/")) {
            var split = name.split("/")
            helper?.setText(R.id.item_new_home_page_trade_symbol, split[0])
            helper?.setText(R.id.item_new_home_page_trade_market, "/" + split[1])
        }
        /**
         * 价格
         */
        var close = item?.optString("close")
        if (TextUtils.isEmpty(close)) {
            helper?.setText(R.id.item_new_home_page_trade_assets, "--")
        } else {
            helper?.setText(R.id.item_new_home_page_trade_assets, BigDecimalUtils.showSNormal(close))
        }

        /**
         * 收盘价的汇率
         */
        var marketName = NCoinManager.getMarketName(item?.optString("name", ""))
        val result = RateManager.getCNYByCoinName(marketName, close)
        helper?.setText(R.id.item_new_home_page_trade_value, result)


        /**
         * 涨幅
         */
        var rose = item?.optString("rose") ?: ""
        RateManager.getRoseText(helper?.getView<TextView>(R.id.item_new_home_page_trade_gains), rose)

        helper?.setTextColor(R.id.item_new_home_page_trade_gains, ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0))
    }

}