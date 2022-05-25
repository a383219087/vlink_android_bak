package com.yjkj.chainup.new_version.adapter

import android.app.Activity
import android.text.TextUtils
import android.widget.RelativeLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractTicker
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.yjkj.chainup.contract.activity.SlContractKlineActivity
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import java.util.*


/**
 * @Author lianshangljl
 * @Date 2020-05-20-17:12
 * @Email buptjinlong@163.com
 * @description
 */
class NewContractDropAdapter(data: ArrayList<ContractTicker>) : BaseQuickAdapter<ContractTicker, BaseViewHolder>(R.layout.new_sl_item_contract_drop, data) {

    override fun convert(helper: BaseViewHolder, ticker: ContractTicker) {
        helper?.setText(R.id.tv_ranking, (helper.adapterPosition + 1).toString())

        if (helper?.adapterPosition == 0 || helper?.adapterPosition == 1 || helper?.adapterPosition == 2) {
            helper.getView<TextView>(R.id.tv_ranking).setTextColor(ColorUtil.getMainColorType())
        } else {
            helper?.getView<TextView>(R.id.tv_ranking)?.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
        }

        //合约名
        val contract = ContractPublicDataAgent.getContract(ticker?.instrument_id ?: 0)
        helper?.setText(R.id.tv_coin_name, contract?.symbol)

        /**
         * 收盘价
         */
        var close = ticker?.close
        if (TextUtils.isEmpty(close)) {
            helper?.setText(R.id.tv_close_price, "--")
        } else {
            helper?.setText(R.id.tv_close_price, BigDecimalUtils.showNormal(close))
        }

        /**
         * 收盘价的汇率换算结果
         */
        helper?.setText(R.id.tv_close_price_rmb, RateManager.getCNYByCoinName(contract?.quote_coin, close))

        var vol = ticker?.qty24
        if (TextUtils.isEmpty(vol)) {
            helper?.setText(R.id.tv_volume, LanguageUtil.getString(context, "common_text_dayVolume") + "--")
        } else {
            helper?.setText(R.id.tv_volume, LanguageUtil.getString(context, "common_text_dayVolume") + " " + BigDecimalUtils.showDepthVolume(vol))
        }

        /**
         * 涨幅
         */
        val chg = ticker?.change_rate ?: "0.0"
        //比例
        RateManager.getRoseText(helper?.getView<SuperTextView>(R.id.tv_rose), chg)

        helper?.getView<SuperTextView>(R.id.tv_rose)?.solid = ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(chg) >= 0)


        helper?.getView<RelativeLayout>(R.id.rl_main_layout)?.setOnClickListener {
            SlContractKlineActivity.show(context as Activity, ticker?.instrument_id ?: 0)
        }
    }


}