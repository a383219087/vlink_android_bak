package com.yjkj.chainup.new_version.adapter

import android.text.TextUtils
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.home.callback.MarketTabDiffCallback
import com.yjkj.chainup.new_version.view.CustomTagView
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.DecimalUtil
import com.yjkj.chainup.util.setGoneV3
import com.yjkj.chainup.util.*
import org.json.JSONObject
import java.util.ArrayList

/**
 * @Author: Bertking
 * @Date：2018/12/10-2:55 PM
 * @Description:
 */

class MarketDetailAdapter : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_market_detail_new) {


    val TAG = MarketDetailAdapter::class.java.simpleName
    var isMarketLike = false
    var isMarketSort = false
    override fun convert(helper: BaseViewHolder, item: JSONObject) {

        if (null == data || data.size <= 0)
            return

        if (null == item)
            return
        var name = NCoinManager.showAnoterName(item)
        var newcoinFlag = item.optInt("newcoinFlag")
        var vol = item.optString("vol", "")
        var close = item.optString("close", "")
        var price = item.optInt("price", 0)
        var rose = item.optString("rose", "")
        var rateResult = item.optString("rateResult")
        if (TextUtils.isEmpty(rateResult)) {
            try {
                var split = item.optString("name").split("/")
                rateResult = RateManager.getCNYByCoinName(split[1], close)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val isShowArea = !(isMarketLike || isMarketSort)
        val itemArea = helper.adapterPosition == 0 || data[helper.adapterPosition - 1].optInt("newcoinFlag") != newcoinFlag
        if (isShowArea && itemArea) {
            helper.setGoneV3(R.id.ll_main_area, true)
            helper.setText(R.id.ll_title_content, newcoinFlag.byMarketGroupTypeGetName(context))
        } else {
            helper.setGoneV3(R.id.ll_main_area, false)
        }

        if (!name.contains("/")!!) {
            return
        }

        val split = name.split("/")

        helper?.setText(R.id.tv_coin_name, split[0])
        helper?.setText(R.id.tv_market_name, "/" + split[1])

        if (TextUtils.isEmpty(vol)) {
            helper?.setText(R.id.tv_volume, LanguageUtil.getString(context, "common_text_dayVolume") + " --")
        } else {
            helper?.setText(R.id.tv_volume, LanguageUtil.getString(context, "common_text_dayVolume") + " " + BigDecimalUtils.showDepthVolume(vol))
        }


        /**
         * 收盘价
         */
        if (TextUtils.isEmpty(close)) {
            helper?.setText(R.id.tv_close_price, "--")
            helper.setGoneV3(R.id.tv_close_price_rmb, false)
        } else {
            helper?.setText(R.id.tv_close_price, DecimalUtil.cutValueByPrecision(close, price))
            helper.setGoneV3(R.id.tv_close_price_rmb, true)
            /**
             * 收盘价的汇率换算结果
             */
            helper?.setText(R.id.tv_close_price_rmb, rateResult + "")
        }
        var tagCoin = NCoinManager.getMarketShowCoinName(item?.optString("name"))
        if (!TextUtils.isEmpty(NCoinManager.getCoinTag4CoinName(tagCoin))) {
            helper?.getView<CustomTagView>(R.id.ctv_content)?.setTextViewContent(NCoinManager.getCoinTag4CoinName(tagCoin))
            helper?.apply {
                setGoneV3(R.id.ctv_content, true)
            }
        } else {
            helper?.apply {
                setGoneV3(R.id.ctv_content, false)
            }
        }


        /**
         * 涨幅
         */
        RateManager.getRoseText(helper.getView<SuperTextView>(R.id.tv_rose), rose)

        var colorRiseType = ColorUtil.getColorType()
        helper?.getView<SuperTextView>(R.id.tv_rose)?.solid = ColorUtil.getMainColorV2Type(colorRiseType, isRise = RateManager.getRoseTrend(rose))
    }

    fun setDiffData(diffCallback: MarketTabDiffCallback) {
        if (emptyLayout!=null &&  emptyLayout?.childCount == 1) {
            setList(diffCallback.getNewData())
            return
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback, true)
        data = diffCallback.getNewData() as ArrayList<JSONObject>
        diffResult.dispatchUpdatesTo(this)
    }

}

