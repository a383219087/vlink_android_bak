package com.yjkj.chainup.new_version.activity.oldgrid

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.TradeTypeEnum
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.NewMainActivity
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.dialog.DialogUtil
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.CoinSearchDialogFg
import com.yjkj.chainup.ws.WsAgentManager
import kotlinx.android.synthetic.main.fragment_new_grid_old.*
import kotlinx.android.synthetic.main.trade_amount_view.*
import kotlinx.android.synthetic.main.trade_header_tools.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.textColor
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2021/1/28-7:44 PM
 * @Email buptjinlong@163.com
 * @description
 */
class OldNGridFragment : NBaseFragment(), WsAgentManager.WsResultCallback {


    var titles = arrayListOf<String>()
    val fragments = arrayListOf<Fragment>()

    var coinMapData: JSONObject? = null

    /*当前价格 */
    var currentPrice = ""

    var aiFragment = OldNAIGridFragment()
    var customFragment = OldNCustomGridFragment()

    var symbol = "btcusdt"

    override fun initView() {
        changeInitData()

        iv_more_coin?.visibility = View.GONE
        ctv_content?.visibility = View.GONE
        iv_more?.visibility = View.GONE
        img_coin_map?.visibility = View.GONE


        /**
         * 进入KLine
         */
        ib_kline?.setOnClickListener {
            ArouterUtil.forwardKLine(symbol, ParamConstant.BIBI_INDEX)
        }
        /**
         * 侧边栏
         */
        img_coin_map?.setOnClickListener {
            showLeftCoinPopup()
        }

        /**
         * 入口
         */
        iv_more?.setOnClickListener {
            DialogUtil.createGridPop(context, iv_more, this)
        }
        initFragment()
        observeData()
    }

    private fun observeData() {
        NLiveDataUtil.observeForeverData {
            if (null == it || !it.isGrid) {
                return@observeForeverData
            }
            when (it.msg_type) {
                //切换币种事件
                MessageEvent.symbol_switch_type -> {
                    if (null != it.msg_content) {
                        val nSymbol = it.msg_content as String
                        if (symbol != nSymbol) {
                            clearPriceView()
                        }
                        showSymbolSwitchData(nSymbol)

                    }
                }
            }
        }
    }

    private fun showSymbolSwitchData(newSymbol: String?) {
        if (null == newSymbol)
            return

        if (newSymbol != symbol) {
            PublicInfoDataService.getInstance().currentSymbol4Grid = newSymbol
            coinMapData = NCoinManager.getSymbolObj(newSymbol)
            changeInitData()
            symbol = coinMapData?.optString("symbol", "") ?: return

        }
    }

    private fun showLeftCoinPopup() {
        if (Utils.isFastClick())
            return
        val mCoinSearchDialogFg = CoinSearchDialogFg()
        val bundle = Bundle()
        bundle.putInt(ParamConstant.TYPE, TradeTypeEnum.GRID_TRADE.value)
        mCoinSearchDialogFg.arguments = bundle
        mCoinSearchDialogFg.showDialog(childFragmentManager, "TradeFragment")
    }

    override fun setContentView() = R.layout.fragment_new_grid_old
    var selectPosition = 0

    fun initFragment() {
        titles.clear()
        titles.add(getString(R.string.quant_ai_strategy))
        titles.add(getString(R.string.quant_custom_strategy))
        fragments.clear()

        fragments.add(aiFragment)
        fragments.add(customFragment)
        var pageAdapter = NVPagerAdapter(childFragmentManager, titles, fragments)

        fragment_market?.adapter = pageAdapter
        fragment_market?.offscreenPageLimit = fragments.size
        fragment_market?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                selectPosition = position
            }
        })
        stl_grid_list?.setViewPager(fragment_market, titles.toTypedArray())

        aiFragment.symbol = coinMapData?.optString("name") ?: ""
        customFragment.symbol = coinMapData?.optString("name") ?: ""
    }

    private fun showTopCoin() {
        val symbol = coinMapData?.getMarketName()
        tv_coin_map?.text = symbol
        Log.e("jinlong", "coinMapData:${coinMapData}")
    }


    fun changeInitData() {
        var symbol4LastCoin = PublicInfoDataService.getInstance().currentSymbol4Grid ?: ""
        var gridLit = NCoinManager.getGridCroupList(null)
        if (symbol4LastCoin.isEmpty()) {
            if (gridLit != null && gridLit.size > 0) {
                coinMapData = NCoinManager.getGridCroupList(null)[0]
            }

        } else {
            coinMapData = NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol4Grid)
            if (coinMapData?.optString("is_grid_open") != "1") {
                if (gridLit != null && gridLit.size > 0) {
                    coinMapData = NCoinManager.getGridCroupList(null)[0]
                }
            }
        }

        getAccountBalanceByMarginCoin()
        showTopCoin()
        LogUtil.d(TAG, "NCVCTradeFragment==coinMapData is $coinMapData")
        symbol = coinMapData?.optString("symbol") ?: ""
        setTagView(coinMapData?.optString("name", "").toString())
        sendAgentData()

    }

    /**
     * 显示tag
     */
    fun setTagView(name: String) {
        var tagCoin = NCoinManager.getMarketCoinName(name)
        if (!TextUtils.isEmpty(NCoinManager.getCoinTag4CoinName(tagCoin))) {
            ctv_content?.visibility = View.GONE
            ctv_content?.text = NCoinManager.getCoinTag4CoinName(tagCoin)
        } else {
            ctv_content?.visibility = View.GONE
        }
    }

    override fun onWsMessage(json: String) {
        handleData(json)
    }

    private var lastTick: JSONObject? = null

    /**
     * 处理 24H,KLine数据
     */
    fun handleData(data: String) {
        try {
            val jsonObj = JSONObject(data)
            if (!jsonObj.isNull("tick")) {
                val tick = jsonObj.optJSONObject("tick")
                val channel = jsonObj.optString("channel")
                /**
                 * 24H行情
                 */
                if (!StringUtil.checkStr(symbol)) {
                    return
                }
                if (channel == WsLinkUtils.tickerFor24HLink(symbol, isChannel = true)) {
                    if (tick == null) {
                        return
                    }
                    lastTick = jsonObj
                    doAsync {
                        activity?.runOnUiThread {
//                            /**
//                             * 收盘价的法币换算
//                             */

                            //
                            val rose = tick.getRose()
                            val close = tick.getClose()
                            RateManager.getRoseText(tv_rose, rose)
                            val roseRes = ColorUtil.getMainColorBgType(RateManager.getRoseTrend(rose) >= 0)
                            val pricePrecision = coinMapData?.optString("price", "4")?.toInt() ?: 4
                            tv_rose?.textColor = roseRes.first
                            tv_rose?.backgroundResource = roseRes.second
                            tv_close_price?.run {
                                textColor = ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0)
                                text = DecimalUtil.cutValueByPrecision(close, pricePrecision)
                                aiFragment.currentPrice = close
                                customFragment.currentPrice = close
                                currentPrice = close
                            }
                            val marketName = NCoinManager.getMarketName(coinMapData?.optString("name", ""))
                            tv_converted_close_price?.text = RateManager.getCNYByCoinName(marketName, close)

                            tv_high_price?.text = DecimalUtil.cutValueByPrecision(tick.getHigh(), pricePrecision)
                            tv_low_price?.text = DecimalUtil.cutValueByPrecision(tick.getLow(), pricePrecision)

                        }
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearPriceView() {
        tv_close_price?.text = "--"
        tv_converted_close_price?.text = "--"
        tv_high_price?.text = "--"
        tv_low_price?.text = "--"
        tv_rose?.text = "--"
    }


    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.grid_topTab_type == event?.msg_type) {
            val msg_content = event.msg_content
            if (null != msg_content && msg_content is Bundle) {
                var bundle = msg_content as Bundle
                symbol = bundle.getString(ParamConstant.symbol).toString()
                LogUtil.d(TAG, "observeData==symbol is ${symbol}")
                et_price?.text?.clear()
                showSymbolSwitchData(symbol ?: "")
                setTagView(NCoinManager.getNameForSymbol(symbol))

            }
        }
    }

    fun getAccountBalanceByMarginCoin() {
        if (UserDataService.getInstance().isLogined) {
            if (coinMapData == null) return
            var name = coinMapData.getMarketCoinName().split("/")
            addDisposable(getMainModel().getAccountBalanceByMarginCoin("${name[0]},${name[1]}", object : NDisposableObserver() {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    var data = jsonObject.optJSONObject("data") ?: return
                    var json = data.optJSONObject("allCoinMap") ?: return
                    var coinSymbol = json.optJSONObject(name[0]) ?: return
                    var baseSymbol = json.optJSONObject(name[1]) ?: return
                    if (aiFragment != null) {
                        aiFragment.setAccountBalance(name[0], coinSymbol.optString("normal_balance"), name[1], baseSymbol.optString("normal_balance"))
                    }
                    if (customFragment != null) {
                        customFragment.setAccountBalance(name[0], coinSymbol.optString("normal_balance"), name[1], baseSymbol.optString("normal_balance"))
                    }
                }

                override fun onResponseFailure(code: Int, msg: String?) {
                    super.onResponseFailure(code, msg)

                }
            }))
        }
    }


    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)

        val mainActivity = activity
        if (mainActivity != null) {
            if (mainActivity is NewMainActivity) {
                if (isVisibleToUser && mainActivity.curPosition == 2) {
                    getAccountBalanceByMarginCoin()
                    sendAgentData()
                    when (selectPosition) {
                        0 -> {
                            aiFragment.fragmentVisibile(true)
                            customFragment.fragmentVisibile(false)
                        }
                        1 -> {
                            aiFragment.fragmentVisibile(false)
                            customFragment.fragmentVisibile(true)
                        }
                    }
                }
            }
        }
    }

    fun sendAgentData(step: String = "") {
        LogUtil.d(TAG, "sendAgentData() symbol 是否存在 ${symbol.isNotEmpty()}")
        if (symbol.isNotEmpty()) {
            WsAgentManager.instance.sendMessage(hashMapOf("symbol" to symbol, "step" to step), this)
        }
    }

    fun unbindAgentData() {
        LogUtil.d(TAG, "unbindAgentData() symbol 是否存在 ${symbol.isNotEmpty()}")
        if (symbol.isNotEmpty()) {
            WsAgentManager.instance.unbind(this, true)
        }
        lastTick = null
    }


}