package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.manager.TAG
import com.yjkj.chainup.new_version.view.CustomTagView
import com.yjkj.chainup.util.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2018/11/8-10:20 AM
 * @Email buptjinlong@163.com
 * @description  新首页涨跌幅
 *
 * data: ArrayList<JSONObject>
 */
open class NewhomepageTradeListAdapter : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_new_home_page_trade) {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {
        val name = NCoinManager.showAnoterName(item)
        if (StringUtil.checkStr(name) && name.contains("/")) {
            val split = name.split("/")
            helper?.setText(R.id.item_new_home_page_trade_symbol, split[0])
            helper?.setText(R.id.item_new_home_page_trade_market, "/" + split[1])
        }
        /**
         * 价格
         */
        var close = item?.optString("close")

        /**
         * 涨幅
         */
        var rose = item?.optString("rose") ?: ""
        if (TextUtils.isEmpty(close)) {
            helper?.setText(R.id.item_new_home_page_trade_assets, "--")
        } else {
            helper?.setText(R.id.item_new_home_page_trade_assets, BigDecimalUtils.showNormal(close))
        }
        helper.setTextColor(R.id.item_new_home_page_trade_assets, ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0))
        /**
         * 收盘价的汇率
         */
        var marketName = NCoinManager.getMarketName(item?.optString("name", ""))
        val result = RateManager.getHomeCNYByCoinName(marketName, close)
        helper?.setText(R.id.item_new_home_page_trade_value, result)



        RateManager.getRoseText(helper?.getView<TextView>(R.id.item_new_home_page_trade_gains), rose)

        helper?.setTextColor(R.id.item_new_home_page_trade_gains, ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0))
    }

}