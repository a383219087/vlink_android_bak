package com.yjkj.chainup.new_version.activity.grid

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_being_performed.*
import kotlinx.android.synthetic.main.item_grid_buy.view.*
import kotlinx.android.synthetic.main.item_grid_sell.view.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 * @Author lianshangljl
 * @Date 2021/2/3-6:13 PM
 * @Email buptjinlong@163.com
 * @description
 */
class BeingGridFragment : NBaseFragment() {


    companion object {
        @JvmStatic
        fun newInstance(strategyId: String, coin: String, symbol: String, coinInfo: String) =
                BeingGridFragment().apply {
                    arguments = Bundle().apply {
                        putString(ParamConstant.GIRD_ID, strategyId)
                        putString(ParamConstant.GIRD_COIN, coin)
                        putString(ParamConstant.COIN_SYMBOL, symbol)
                        putString(ParamConstant.GIRD_COIN_INFO, coinInfo)
                    }
                }
    }

    var strategyId = ""
    var coinJson = ""
    var symbol = ""
    var symbolShow = ""

    override fun initView() {
        arguments?.let {
            strategyId = it.getString(ParamConstant.GIRD_ID, "")
            coinJson = it.getString(ParamConstant.GIRD_COIN_INFO, "")
            symbol = it.getString(ParamConstant.COIN_SYMBOL, "")
        }
        symbolShow = NCoinManager.getShowMarketName(symbol)
        tv_current_grid_symbol.text = symbolShow
        progress_bar_rate.max = 100
        mapCoin = NCoinManager.getSymbolObj(getSymbolByName())
        getOrderingGridInfo()
        loopPriceRiskPosition()
    }

    private var riseColor = ColorUtil.getMainColorType(isRise = true)
    private var fallColor = ColorUtil.getMainColorType(isRise = false)

    /**
     * 卖盘的item
     */
    private var sellViewList = mutableListOf<View>()

    /**
     * 买盘的item
     */
    private var buyViewList = mutableListOf<View>()

    var sellList: JSONArray = JSONArray()
    var buyList: JSONArray = JSONArray()

    override fun setContentView() = R.layout.fragment_being_performed


    fun getOrderingGridList(strategyId: String) {
        addDisposable(getMainModel().getOrderingGridList(strategyId, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                sellList = json.optJSONArray("SELL")
                buyList = json.optJSONArray("BUY")
                if (priceClose.isNotEmpty()) {
                    initDepthView(priceClose)
                }
                if (isZero()) {
                    changeView(true)
                    if(!isUpdateIng()){
                        subscribe?.dispose()
                    }
                }
            }
        }))
    }

    var mapCoin: JSONObject? = null

    /**
     * 买卖盘
     * 初始化交易详情记录view
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initDepthView(priceClose: String = "0") {

        sellViewList.clear()
        buyViewList.clear()

        ll_buy.removeAllViews()
        ll_sell.removeAllViews()

        var tradeCount = 0
        for (i in 0 until sellList.length()) {
            /**
             * 卖盘
             */
            val price = sellList.optString(i)
            if (BigDecimalUtils.compareTo(priceClose, price) == 1) {
                continue
            }
            tradeCount++
            val view: View = layoutInflater.inflate(R.layout.item_grid_sell, null)
            view.tv_price_item_for_depth_sell?.setTextColor(fallColor)
            view.tv_price_item_for_depth_sell?.text = price.getTradeCoinPrice(mapCoin)
            view.tv_quantity_item_for_depth_sell?.text = "${tradeCount}"
            view.tv_price_item_for_sell_range?.text = price.getGridDepthSellScale(priceClose)
            view.tv_price_item_for_sell_range?.setTextColor(fallColor)
            ll_sell?.addView(view)
            sellViewList.add(view)
            /***********/
        }
        tradeCount = 0
        for (i in 0 until buyList.length()) {
            /**
             * 买盘
             */
            val price = buyList.optString(i)
            if (BigDecimalUtil.compareSize(priceClose, price) == -1) {
                continue
            }
            tradeCount++
            val view1: View = layoutInflater.inflate(R.layout.item_grid_buy, null)
            view1.tv_price_item_for_depth_buy?.setTextColor(riseColor)
            view1.tv_price_item_for_depth_buy?.text = price.getTradeCoinPrice(mapCoin)
            view1.tv_quantity_item_for_depth_buy?.text = "${tradeCount}"
            view1.tv_price_item_for_buy_range?.text = price.getGridDepthBuyScale(priceClose)
            view1.tv_price_item_for_buy_range?.setTextColor(riseColor)
            ll_buy?.addView(view1)
            buyViewList.add(view1)
        }
        // 有买1、有卖1
        // 有买1、无卖1
        // val  subA = BigDecimalUtils.subStr(priceClose, buyList.optString(0))
        var price = BigDecimal.ZERO
        if (sellList.length() == 0) {
            price = "100".toBigDecimal()
        } else if (buyList.length() == 0) {
            price = "0".toBigDecimal()
        } else {
            val buyA = BigDecimalUtils.subStr(priceClose, buyList.optString(0), 6)
            val sellA = BigDecimalUtils.subStr(sellList.optString(0), buyList.optString(0), 6)
            val number = BigDecimalUtils.div(buyA, sellA, 2)
            price = BigDecimalUtils.mul(number.toPlainString(), "100")
        }
        progress_bar_rate?.progressDrawable = resources.getDrawable(ColorUtil.getGridColorType(), null)
        progress_bar_rate.progress = price.toInt()
        progress_bar_mark.progress = price.toInt()
        // 处理前面页面 Tab
        val count = sellViewList.size + buyViewList.size
        changeView(isHide = count == 0)
        val acy = activity
        if (acy != null && acy is GridExecutionDetailsActivity) {
            acy.updateTitleOrder(count)
        }

    }

    private fun getOrderingGridInfo() {
        if (coinJson.isEmpty()) {
            return
        }
        val info = JSONObject(coinJson)
        val quote = info.optString("freezQuoteAmount")
        val base = info.optString("freezBaseAmount")
        val count = info.optString("yesterdayProfitTimes", "0")
        val sum = info.optString("totalProfitTimes", "0")
        val coinInfo = symbolShow.split("/")
        val mapCoin = NCoinManager.getSymbolObj(getSymbolByName())
        tv_quote_price.text = "${LanguageUtil.getString(context, "quant_order_pending_freeze")} (${coinInfo[1]})"
        tv_base_price.text = "${LanguageUtil.getString(context, "quant_order_pending_freeze")} (${coinInfo[0]})"


        tv_quote_price_value?.text = quote.getTradeCoinPrice(mapCoin)
        tv_base_price_value?.text = base.getTradeCoinVolume(mapCoin)

        tv_24_count_value?.text = count.getGridCount(context)
        tv_profit_sum_value?.text = sum.getGridCount(context)

        //
        if (!info.isNull("configParamMap")) {
            val infoMap = info.optJSONObject("configParamMap")
            val highestPrice = infoMap?.optString("stopHighPrice")
            val lowestPrice = infoMap?.optString("stopLowPrice")
            tv_profit_price_vale.text = highestPrice.getGridPrice(context!!)
            tv_loss_price_value.text = lowestPrice.getGridPrice(context!!)
        }
        tv_profit_price_vale.setOnClickListener {
            tv_profit_sum_value?.text = sum.getGridCount(context)
        }
    }

    private var currentTick = 0
    var priceClose = ""
    fun tick24H(tick: JSONObject) {
        if (mActivity?.isFinishing == true) {
            return
        }
        val isCurrent = isWsTick(tick)
        if (!tick.isNull("tick") && isCurrent) {
            val tickInfo = tick.optJSONObject("tick")
            tickInfo?.apply {
                priceClose = this.optString("close")
                runOnUiThread {
                    tv_current_grid_symbol.text = "${symbolShow}= ${priceClose.getTradeCoinPrice(mapCoin)}"
                    if ((buyList.length() != 0 || sellList.length() != 0)) {
                        if (currentTick == 0) {
                            changeView(isHide = false)
                            currentTick++
                        }
                        initDepthView(priceClose)
                    } else {
                        changeView(isHide = true)
                    }
                }
            }
        }
    }

    private fun getSymbolByName(): String {
        return symbol.getSymbolByMarketName()
    }

    private fun isWsTick(tick: JSONObject): Boolean {
        val isCurrent = !tick.isNull("channel")
        if (isCurrent) {
            val isChannel = tick.optString("channel").split("_")
            if (isChannel.isNotEmpty()) {
                return isChannel[1] == getSymbolByName()
            }
        }
        return false
    }

    var subscribe: Disposable? = null//保存订阅者

    /**
     * 每5s调用一次接口
     */
    private fun loopPriceRiskPosition() {
        subscribe = Observable.interval(0L, CommonConstant.coinLoopTime, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver())
    }

    private fun getObserver(): DisposableObserver<Long> {
        return object : DisposableObserver<Long>() {
            override fun onComplete() {
            }

            override fun onNext(t: Long) {
                getOrderingGridList(strategyId)
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            subscribe?.dispose()
        } else {
            loopPriceRiskPosition()
        }
    }

    override fun onStop() {
        super.onStop()
        subscribe?.dispose()
    }

    private fun isZero(): Boolean {
        return (sellList == null || sellList.length() == 0) && (buyList == null || buyList.length() == 0)
    }

    private fun changeView(isHide: Boolean) {
        if (isHide) {
            ll_depth_title.visibility = View.GONE
            layout_symbol_tick.visibility = View.GONE
            ns_layout.visibility = View.GONE
        } else {
            ll_depth_title?.visibility = View.VISIBLE
            layout_symbol_tick.visibility = View.VISIBLE
            ns_layout.visibility = View.VISIBLE
        }
    }

    private fun isUpdateIng(): Boolean {
        val info = JSONObject(coinJson)
        if (info.length() != 0) {
            if (!info.isNull("strategyStatus")) {
                val strategyStatus = info.optString("strategyStatus")
                if (strategyStatus == "0" || strategyStatus == "1") return true
            }
        }
        return false
    }

}