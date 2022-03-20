package com.yjkj.chainup.new_version.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.DecimalUtil
import com.yjkj.chainup.util.setGoneV3
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/5/11-11:43 AM
 * @Email buptjinlong@163.com
 * @description 成交榜
 */
class NewHomepageBottomClinchDealAdapter() :
        BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_homepage_clinch_deal_adapter) {

    override fun convert(helper: BaseViewHolder, item: JSONObject) {

        val symbol = item.optString("symbol") ?: ""

        helper.setText(R.id.tv_coin_name, NCoinManager.getShowMarket(symbol))

        /**
         * 收盘价
         */
        val rateByCoinName = RateManager.getRatesByCoinName(symbol)
        val coinPrecision = RateManager.getCurrencyPrecision()

        helper.apply {
            setGoneV3(R.id.ctv_content, false)
        }


        helper.setText(R.id.tv_close_price, BigDecimalUtils.divForDown(rateByCoinName, coinPrecision).toPlainString())
        /**
         * 24H 成交量
         */
        helper.setText(R.id.tv_rose, DecimalUtil.formatNumber(item.optString("volume") ?: ""))

    }

}