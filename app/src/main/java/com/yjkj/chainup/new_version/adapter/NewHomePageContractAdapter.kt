package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractTicker
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.SystemUtils

/**
 * @Author lianshangljl
 * @Date 2020-09-16-15:05
 * @Email buptjinlong@163.com
 * @description
 */
class NewHomePageContractAdapter(data: ArrayList<ContractTicker>) : BaseQuickAdapter<ContractTicker, BaseViewHolder>(R.layout.item_new_home_page_trade, data) {

    override fun convert(helper: BaseViewHolder, item: ContractTicker) {

        item?.let {
            val contract = ContractPublicDataAgent.getContract(item?.instrument_id)
            helper?.setText(R.id.item_new_home_page_trade_symbol, contract?.symbol)

            /**
             * 价格
             */
            var close = item?.close
            if (TextUtils.isEmpty(close)) {
                helper?.setText(R.id.item_new_home_page_trade_assets, "--")
            } else {
                helper?.setText(R.id.item_new_home_page_trade_assets, BigDecimalUtils.showSNormal(close))
            }
            /**
             * 收盘价的汇率
             */
            var marketName = contract?.quote_coin
            val result = RateManager.getHomeCNYByCoinName(marketName, close)
            helper?.setText(R.id.item_new_home_page_trade_value, result)

            /**
             * 涨幅
             */
            var rose = item?.change_rate
            RateManager.getRoseText(helper?.getView<TextView>(R.id.item_new_home_page_trade_gains), rose)

            helper?.setTextColor(R.id.item_new_home_page_trade_gains, ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0))


        }
    }

}