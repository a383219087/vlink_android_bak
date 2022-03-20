package com.yjkj.chainup.new_version.adapter

import androidx.recyclerview.widget.DiffUtil
import android.text.TextUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.coorchice.library.SuperTextView
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.home.callback.EmployeeDiffCallback
import com.yjkj.chainup.new_version.view.CustomTagView
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.setGoneV3
import org.json.JSONObject
import java.util.ArrayList


/**
 * @Author lianshangljl
 * @Date 2018/11/9-2:39 PM
 * @Email buptjinlong@163.com
 * @description 新首页 下半部分 行情(首页涨幅榜)
 */
open class NewHomepageMarketAdapter : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_homepage_rank) {

    override fun convert(helper: BaseViewHolder, tick: JSONObject) {

        /**
         * Tick(amount='39.96450966', vol='17.56774781', high='2.30000000', low='2.18970000', rose=0.0, close='2.30000000', open='2.30000000', name='BCH/BTC', symbol='bchbtc')
         */
        val name = NCoinManager.showAnoterName(tick)

        // TODO 待解决不包含/的情况
        if (name.contains("/")) {
            val split = name.split("/")
            helper.setText(R.id.tv_coin_name, split[0])
            helper.setText(R.id.tv_market_name, "/" + split[1])
        }
        helper.apply {
            val tagCoin = NCoinManager.getMarketShowCoinName(name)
            if (!TextUtils.isEmpty(NCoinManager.getCoinTag4CoinName(tagCoin))) {
                helper.getView<CustomTagView>(R.id.ctv_content).setTextViewContent(NCoinManager.getCoinTag4CoinName(tagCoin))
                helper.apply {
                    setGoneV3(R.id.ctv_content, true)
                }
            } else {
                helper.apply {
                    setGoneV3(R.id.ctv_content, false)
                }
            }
        }
        /**
         * 收盘价
         */
        val close = tick.optString("close")
        if (TextUtils.isEmpty(close)) {
            helper.setText(R.id.tv_close_price, "--")
        } else {
            helper.setText(R.id.tv_close_price, BigDecimalUtils.divForDown(close, tick.optInt("price")).toPlainString())
        }

        /**
         * 涨幅
         */
        val rose = tick.optString("rose")
        RateManager.getRoseText(helper?.getView<SuperTextView>(R.id.tv_rose), rose)
        helper.getView<SuperTextView>(R.id.tv_rose).solid = ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0)
    }

    fun setDiffData(diffCallback: EmployeeDiffCallback) {
        if (emptyLayout != null && emptyLayout?.childCount == 1) {
            setList(diffCallback.getNewData())
            return
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback, true)
        diffResult.dispatchUpdatesTo(this)
        data = diffCallback.getNewData() as ArrayList<JSONObject>
    }


}