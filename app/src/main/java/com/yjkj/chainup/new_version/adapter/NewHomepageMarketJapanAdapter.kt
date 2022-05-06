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
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.wedegit.LineChartGreenDataSet
import com.yjkj.chainup.wedegit.LineChartRedDataSet
import com.yjkj.chainup.wedegit.SimpleLineChart
import org.jetbrains.anko.textColor
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-09-02-16:28
 * @Email buptjinlong@163.com
 * @description
 */
class NewHomepageMarketJapanAdapter : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_homepage_rank_japan) {


    fun getKlineData(jsonObject: JSONObject): List<Entry> {

        var klineDateMap: ArrayList<Entry> = arrayListOf()
        var historyList = JSONUtil.arrayToList(jsonObject.optJSONArray("historyData"))
        if (historyList == null || historyList.size == 0) {
            return klineDateMap
        }
        if (historyList.size > 20) {
            historyList = ArrayList(historyList.takeLast(20))
        }

        for (i in 0 until historyList.size) {
            klineDateMap.add(Entry(i.toFloat(), historyList[i].optDouble("close", 0.0).toFloat()))
        }

        return klineDateMap
    }

    override fun convert(helper: BaseViewHolder, tick: JSONObject) {
        if (tick == null) return
        /**
         * Tick(amount='39.96450966', vol='17.56774781', high='2.30000000', low='2.18970000', rose=0.0, close='2.30000000', open='2.30000000', name='BCH/BTC', symbol='bchbtc')
         */
        var name = NCoinManager.showAnoterName(tick)
        if (name.isNotEmpty() && name.contains("/")) {
            val split = name.split("/")
            helper?.setText(R.id.tv_coin_name, split[0])
            helper?.setText(R.id.tv_market_name, "/" + split[1])
        }
        /**
         * 收盘价
         */
        if (TextUtils.isEmpty(tick.optString("close", ""))) {
            helper?.setText(R.id.tv_close_price, "--")
        } else {
            helper?.setText(R.id.tv_close_price, BigDecimalUtils.showSNormal(tick.optString("close", "")))
        }

        /**
         * 涨幅
         */
        val rose = tick.optString("rose", "0")
        RateManager.getRoseText(helper?.getView<TextView>(R.id.tv_rose), rose)
        var date = getKlineData(tick)
        if (RateManager.getRoseTrend(rose) >= 0) {
            helper?.getView<SimpleLineChart>(R.id.slc_simplelinechart)?.data = LineData(LineChartGreenDataSet(date))
        } else {
            helper?.getView<SimpleLineChart>(R.id.slc_simplelinechart)?.data = LineData(LineChartRedDataSet(date))
        }




        helper?.getView<TextView>(R.id.tv_rose)?.textColor = ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0)


    }
}
