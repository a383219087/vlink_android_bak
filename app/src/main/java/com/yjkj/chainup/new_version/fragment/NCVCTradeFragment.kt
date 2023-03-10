package com.yjkj.chainup.new_version.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.chainup.contract.view.CpDialogUtil
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.TradeTypeEnum
import com.yjkj.chainup.db.service.LikeDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.ui.NewMainActivity
import com.yjkj.chainup.new_version.activity.leverage.TradeFragment
import com.yjkj.chainup.new_version.adapter.NCurrentEntrustAdapter
import com.yjkj.chainup.new_version.dialog.DialogUtil
import com.yjkj.chainup.new_version.home.NetworkDataService
import com.yjkj.chainup.new_version.home.sendWsHomepage
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.CoinSearchDialogFg
import com.yjkj.chainup.ws.WsAgentManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_market_detail4.*
import kotlinx.android.synthetic.main.depth_horizontal_layout.*
import kotlinx.android.synthetic.main.depth_horizontal_layout.view.*
import kotlinx.android.synthetic.main.depth_vertical_layout.*
import kotlinx.android.synthetic.main.depth_vertical_layout.view.*
import kotlinx.android.synthetic.main.fragment_cvctrade.*
import kotlinx.android.synthetic.main.fragment_cvctrade.rv_current_entrust
import kotlinx.android.synthetic.main.fragment_cvctrade.swipe_refresh
import kotlinx.android.synthetic.main.trade_amount_view.*
import kotlinx.android.synthetic.main.trade_header_tools.*
import kotlinx.android.synthetic.main.trade_header_tools.ctv_content
import kotlinx.android.synthetic.main.trade_header_tools.ib_collect
import kotlinx.android.synthetic.main.trade_header_tools.ll_coin_map
import kotlinx.android.synthetic.main.trade_header_tools.tv_coin_map
import kotlinx.android.synthetic.main.trade_header_tools.tv_rose
import kotlinx.android.synthetic.main.trade_header_view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.textColor
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class NCVCTradeFragment : NBaseFragment(), WsAgentManager.WsResultCallback {

    val currentOrderList = arrayListOf<JSONObject>()
    val curEntrustAdapter = NCurrentEntrustAdapter(currentOrderList)


    private var tDialog: TDialog? = null

    var coinMapData: JSONObject? = null

    var symbol: String = ""

    var etfIsShow = true

    var netValueDisposable: CompositeDisposable? = CompositeDisposable()
    var subscribe: Disposable? = null //???????????????
    var subscribeCoin: Disposable? = null //???????????????
    var isScrollStatus = true

    private var isLogined = false
    private var isOptionalSymbolServerOpen = false

    companion object {
        var curDepthIndex = 0
        var tradeOrientation = ParamConstant.TYPE_BUY
        var tapeLevel = 0
    }

    override fun setContentView() = R.layout.fragment_cvctrade

    override fun initView() {
        isLogined = UserDataService.getInstance().isLogined
        isOptionalSymbolServerOpen = PublicInfoDataService.getInstance().isOptionalSymbolServerOpen(null)
        iv_more.visibility = View.VISIBLE
        changeInitData()
        getETFValue()

        WsAgentManager.instance.addWsCallback(this)
        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))
        setTopBar()

        setOnClick()

        initEntrustOrder()

        observeData()
        initTap()
        setTextContent()
    }

    override fun onResume() {
        super.onResume()
        collectCoin()
    }


    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
        if (isVisible) {
            collectCoin()
        }
    }


    fun setTextContent() {
        tv_currentEntrust?.text = LanguageUtil.getString(context, "contract_text_currentEntrust")
        tv_all?.text = LanguageUtil.getString(context, "common_action_sendall")
    }

    /**
     * ??????tag
     */
    private fun setTagView(name: String) {
        var tagCoin = NCoinManager.getMarketCoinName(name)
        if (!TextUtils.isEmpty(NCoinManager.getCoinTag4CoinName(tagCoin))) {
            ctv_content?.visibility = View.GONE
            ctv_content?.text = NCoinManager.getCoinTag4CoinName(tagCoin)
        } else {
            ctv_content?.visibility = View.GONE
        }

    }


    private fun setOnClick() {
        /**
         * ????????????????????????
         */
        ll_all_entrust_order?.setOnClickListener {
            if (LoginManager.checkLogin(context, true)) {
                ArouterUtil.navigation(RoutePath.EntrustActivity, Bundle().apply {
                    putString(ParamConstant.TYPE, ParamConstant.BIBI_INDEX)
                    putString("coinName", tv_coin_map.text.toString())
                })
            }
        }
        /**
         * ??????????????????
         */
        ll_yincang?.setOnClickListener {
            if (img_on.visibility == View.VISIBLE) {
                img_on.visibility = View.GONE
                img_off.visibility = View.VISIBLE
            } else {
                img_on.visibility = View.VISIBLE
                img_off.visibility = View.GONE
            }
            getAvailableBalance()
        }
        /**
         * ????????????
         */
        tv_status?.setOnClickListener {
            if (currentOrderList.isEmpty()) {
                return@setOnClickListener
            }
            CpDialogUtil.showNewDoubleDialog(context!!,
                context!!.getString(R.string.cp_extra_text_hold4),
                object : CpDialogUtil.DialogBottomListener {
                    override fun sendConfirm() {
                        for (i in 0 until currentOrderList.size) {
                            try {
                                (currentOrderList[i] as JSONObject?)?.run {
                                    val status = this.optString("status")
                                    val id = this.optString("id")
                                    val baseCoin = optString("baseCoin").toLowerCase()
                                    val countCoin = optString("countCoin").toLowerCase()

                                    when (status) {
                                        "0", "1", "3" -> {
                                            deleteOrder(id, baseCoin + countCoin, i)
                                        }
                                    }
                                }

                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }


                    }

                }

            )


        }


        /**
         * ??????KLine
         */
        ib_kline?.setOnClickListener {
            ArouterUtil.forwardKLine(symbol, ParamConstant.BIBI_INDEX)
        }

        /**
         * ??????
         */
        iv_more?.setOnClickListener {
            if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                ToastUtils.showToast(context?.getString(R.string.important_hint1))
                return@setOnClickListener
            }
            DialogUtil.createCVCPop(context, iv_more, this)
        }
        /**
         * ??????
         */
        iv_more_coin?.setOnClickListener {
            if (!LoginManager.checkLogin(context, true)) {
                return@setOnClickListener
            }
            val coinMapBean = DataManager.getCoinMapBySymbol(PublicInfoDataService.getInstance().currentSymbol)
            val coin = NCoinManager.getMarketName(coinMapBean.name)
            ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, coin)
        }

        /**
         * ????????????
         */
        ll_coin_map?.setOnClickListener {
            showLeftCoinPopup()
        }

        trade_topleft?.setOnClickListener {
            changeSymbal(0)
        }
        trade_topright?.setOnClickListener {
            changeSymbal(1)
        }
        tradev_topleft?.setOnClickListener {
            changeSymbal(0)
        }
        tradev_topright?.setOnClickListener {
            changeSymbal(1)
        }
        /**
         * ????????????
         */
        swipe_refresh?.setOnRefreshListener {
            /**
             * ??????????????????
             */
            observeData()
            initEntrustOrder()
            swipe_refresh?.isRefreshing = false
        }
    }

    // ???????????????symblo
    fun changeSymbal(index: Int) {

        var etfUpAndDown = coinMapData?.optJSONArray("etfUpAndDown")
        var coin = etfUpAndDown?.getString(index)
        var messageEvent = MessageEvent(MessageEvent.symbol_switch_type)
        messageEvent.msg_content = coin
        NLiveDataUtil.postValue(messageEvent)
    }

    private fun showLeftCoinPopup() {
        if (Utils.isFastClick()) return
        var mCoinSearchDialogFg: CoinSearchDialogFg? = null
        if (mCoinSearchDialogFg == null) {
            mCoinSearchDialogFg = CoinSearchDialogFg()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.TYPE, TradeTypeEnum.COIN_TRADE.value)
            bundle.putString(ParamConstant.COIN_SYMBOL, symbol)
            mCoinSearchDialogFg.arguments = bundle
        }
        mCoinSearchDialogFg.showDialog(childFragmentManager, "NCVCTradeFragment")
    }

    private fun setTopBar() {
        v_horizontal_depth?.apply {
            var tradeType = 0
            if (isFromOtherPage) {
                tradeType = tradeOrientation
                isFromOtherPage = false
            } else {
                tradeType = trade_amount_view.transactionType
            }
            trade_amount_view?.buyOrSell(tradeType, false)
        }
        v_vertical_depth?.trade_amount_view_l?.buyOrSell(0, false)
    }


    var isCreatedOrder: Boolean = false
    private fun observeData() {
        NLiveDataUtil.observeForeverData {
            if (null == it || it.isLever) return@observeForeverData

            when (it.msg_type) {
                /**
                 * ????????????/??????
                 */
                MessageEvent.TAB_TYPE -> {

                    setTopBar()
                }
                /**
                 * ?????????????????????
                 */
                MessageEvent.symbol_switch_type -> {
                    var msg_content = it.msg_content
                    if (it.isBibi && null != msg_content && msg_content is String) {
                        val nSymbol = it.msg_content as String
                        setTagView(NCoinManager.getNameForSymbol(nSymbol))
                        showSymbolSwitchData(nSymbol)
                        setTopBar()
                        v_vertical_depth.coinSwitch(nSymbol)
                        v_horizontal_depth.coinSwitch(nSymbol)
                    }
                    collectCoin()
                } // ????????????
                MessageEvent.CREATE_ORDER_TYPE -> { //                    Log.d(TAG, "====isCreatedOrder:$isCreatedOrder=========")
                    isCreatedOrder = it.msg_content as Boolean
                    if (isCreatedOrder) {
                        if (it.dataIsNotNull()) {
                            val item = it.dataJson
                            curEntrustAdapter.addData(0, item)
                            curEntrustAdapter.notifyDataSetChanged()
                            displayHeaderWithNum()
                        }
                        getEachEntrust()
                    }
                }

                //??????
                MessageEvent.DEPTH_LEVEL_TYPE -> {
                    if (null != it.msg_content) {
                        val level =
                            it.msg_content as Int //                        LogUtil.d(TAG, "tv_change_depth==level is $level,curDepthIndex is $curDepthIndex") //                        Log.d(TAG, "tv_change_depth====????????????:$curDepthIndex,???????????????$it======")
                        sendAgentData(level.toString())
                        curDepthIndex = level
                        if (curDepthIndex != level) {

                        }
                    }
                }

                MessageEvent.login_operation_type -> {
                    if (!LoginManager.isLogin(context)) {
                        currentOrderList.clear()
                        curEntrustAdapter.setList(currentOrderList)
                        displayHeaderWithNum()
                    }
                    v_vertical_depth?.loginSwitch()
                    v_horizontal_depth?.loginSwitch()
                }
            }
        }

    }

    private fun displayHeaderWithNum() {
        val size = curEntrustAdapter.data.size
        tv_currentEntrust?.text = "${LanguageUtil.getString(context, "contract_text_currentEntrust")}???${size}???"
    }

    private fun showSymbolSwitchData(newSymbol: String?) { //        LogUtil.d(TAG, "observeData==newSymbol is ${newSymbol}")
        if (null == newSymbol) return
        if (newSymbol != symbol) {
            PublicInfoDataService.getInstance().currentSymbol = newSymbol
            initTap()
            coinMapData = NCoinManager.getSymbolObj(newSymbol)
            etfIsShow = true
            getETFStateData()
            symbol = coinMapData?.optString("symbol", "") ?: return
            curDepthIndex = 0

            sendAgentData()
            getAvailableBalance()
            if (null != coinMapData) {
                getETFValue() //                Log.d(TAG, "========HERE=======")
                v_horizontal_depth?.coinMapData = coinMapData
                v_vertical_depth?.coinMapData = coinMapData
            }
            activity?.runOnUiThread {
                showTopCoin()
                et_price?.setText("")
            }
            if (img_on.visibility == View.VISIBLE) {
                currentOrderList.clear()
                curEntrustAdapter.notifyDataSetChanged()
            }
            displayHeaderWithNum()
        }
        getTradeLimitInfo(coinMapData)

    }


    private fun showTopCoin() {
        val symbol = coinMapData?.getMarketName()
        tv_coin_map?.text = symbol
        v_vertical_depth?.initCoinSymbol(symbol)
        v_horizontal_depth?.initCoinSymbol(symbol)
    }

    private var hasInit = false
    private fun initEntrustOrder() {
        if (hasInit) {
            return
        }
        hasInit = true

        rv_current_entrust?.run {
            layoutManager = LinearLayoutManager(context)
            curEntrustAdapter.setEmptyView(EmptyForAdapterView(context))
            curEntrustAdapter.notifyDataSetChanged()
            curEntrustAdapter.setOnItemChildClickListener { adapter, view, position ->
                if (adapter.data.isNotEmpty()) {
                    try {
                        (adapter.data[position] as JSONObject?)?.run {
                            val status = this.optString("status")
                            val id = this.optString("id")
                            val baseCoin = optString("baseCoin").toLowerCase()
                            val countCoin = optString("countCoin").toLowerCase()

                            when (status) {
                                "0", "1", "3" -> {
                                    deleteOrder(id, baseCoin + countCoin, position)
                                }
                            }
                        }

                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
            adapter = curEntrustAdapter
            curEntrustAdapter.addChildClickViewIds(R.id.tv_status)
        }
    }

    /**
     * ???????????????&???????????????
     */
    private fun initTap() {
        v_vertical_depth?.clearDepthView()
        v_horizontal_depth?.changeTape(AppConstant.DEFAULT_TAPE, needData = false)
    }


    /**
     * ????????????????????????
     */
    private var startReq = 0

    private fun getTradeLimitInfo(coinMapData: JSONObject?, isNeedCreated: Boolean = true) {
        if (!mIsVisibleToUser) return
        if (PublicInfoDataService.getInstance().isHasTradeLimitOpen(null)) {
            if (1 == startReq) return

            startReq = 1


            addDisposable(getMainModel().getTradeLimitInfo(symbol = coinMapData?.optString("symbol"), consumer = object : NDisposableObserver() {
                override fun onResponseFailure(code: Int, msg: String?) {
                    super.onResponseFailure(code, msg)
                    startReq = 0
                }

                override fun onResponseSuccess(data: JSONObject) {
                    startReq = 0
                    data.optJSONObject("data")?.run {
                        val tradeLimitBuyInfo = optString("trade_limit_buy_info")
                        val tradeLimitSellInfo = optString("trade_limit_sell_info")

                        val tradeSymbolBuyLimit = optString("trade_symbol_buy_limit")
                        val tradeSymbolSellLimit = optString("trade_symbol_sell_limit")

                        if (tradeLimitBuyInfo == "0" && tradeLimitSellInfo == "0") {
                            return
                        }

                        val limitTips = LanguageUtil.getString(context, "tradeLimit_text_everyDayCount") + ", "
                        val precision = coinMapData?.optString("volume", "2")?.toInt() ?: 2

                        val coinName = NCoinManager.getMarketCoinName(coinMapData?.optString("name", ""))
                        val everyDayBuyVolume = LanguageUtil.getString(context, "tradeLimit_text_everyDayBuy")
                            .format("${DecimalUtil.cutValueByPrecision(tradeSymbolBuyLimit, precision)} ${coinName}")
                        val everyDaySellVolume = LanguageUtil.getString(context, "tradeLimit_text_everyDaySell")
                            .format("${DecimalUtil.cutValueByPrecision(tradeSymbolSellLimit, precision)} ${coinName}")

                        val noLimitBuy = ", " + LanguageUtil.getString(context, "tradeLimit_text_noLimitBuy")
                        val noLimitSell = ", " + LanguageUtil.getString(context, "tradeLimit_text_noLimitSell")
                        var content = ""

                        //??????????????????
                        if (tradeLimitBuyInfo == "1" && tradeLimitSellInfo == "1") {
                            content = "$limitTips$everyDayBuyVolume, $everyDaySellVolume"
                        } else if (tradeLimitBuyInfo == "1" && tradeLimitSellInfo == "0") {
                            content = limitTips + everyDayBuyVolume + noLimitSell
                        } else if (tradeLimitBuyInfo == "0" && tradeLimitSellInfo == "1") {
                            content = limitTips + everyDaySellVolume + noLimitBuy
                        } else if (tradeLimitBuyInfo == "0" && tradeLimitSellInfo == "0") {
                            return
                        }

                        //                        LogUtil.d(TAG, "getTradeLimitInfo==content is $content")
                        if (isNeedCreated) { //                            Log.d(TAG, "=====11=====tDialog.isVisible is ${tDialog?.isVisible},content is $content")
                            if (StringUtil.checkStr(content)) {
                                tDialog = showDialog(content)
                            }
                        }

                    }
                }

            }))
        }
    }


    /**
     * ??????????????????
     */
    var availableBalanceData: JSONObject? = null

    private fun getAvailableBalance() {
        if (!LoginManager.checkLogin(context, false)) return
        var symbolData = ""
        if (img_on.visibility == View.VISIBLE) {
            symbolData = symbol
        }
        addDisposable(getMainModel().getNewEntrust(symbol = symbolData, consumer = object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                jsonObject.optJSONObject("data")?.run {

                    availableBalanceData = this
                    showBalanceData()

                    val orderList = optJSONArray("orderList")
                    orderList?.run { //                        Log.d(TAG, "======mList=()====" + orderList.length())
                        currentOrderList.clear()
                        for (i in 0 until orderList.length()) {
                            currentOrderList.add(orderList.optJSONObject(i))
                        }
                        curEntrustAdapter.setList(currentOrderList)
                        displayHeaderWithNum()
                    }

                }
            }
        }))
    }

    private fun showBalanceData() {
        if (availableBalanceData == null) {
            return
        }
        v_horizontal_depth?.changeData(availableBalanceData)
        v_vertical_depth?.changeData(availableBalanceData)
    }

    private var lastTick: JSONObject? = null

    /**
     * ?????? 24H,KLine??????
     */
    fun handleData(data: String) {
        try {
            val jsonObj = JSONObject(data)
            if (!jsonObj.isNull("tick")) {
                val tick = jsonObj.optJSONObject("tick")
                val channel = jsonObj.optString("channel")
                /**
                 * 24H??????
                 */
                if (!StringUtil.checkStr(symbol)) {
                    return
                }


                if (channel == WsLinkUtils.tickerFor24HLink(symbol, isChannel = true)) {
                    if (tick == null) {
                        return
                    } else {
                        if (lastTick != null) {
                            val lastTime = lastTick?.optLong("ts") ?: 0L
                            val time = jsonObj.optLong("ts")
                            if (time < lastTime) {
                                return
                            }
                        }
                    }
                    lastTick = jsonObj
                    doAsync {
                        activity?.runOnUiThread { //                            /**
                            //                             * ????????????????????????
                            //                             */
                            v_horizontal_depth?.changeTickData(tick)
                            v_vertical_depth?.changeTickData(tick) //
                            val rose = tick.getRose()
                            RateManager.getRoseText(tv_rose, rose)
                            val roseRes = ColorUtil.getMainColorBgType(RateManager.getRoseTrend(rose) >= 0)
                            tv_rose?.textColor = roseRes.first //tv_rose?.backgroundResource = roseRes.second

                        }
                    }
                }
                /**
                 * ??????
                 */
                if (channel == WsLinkUtils.getDepthLink(PublicInfoDataService.getInstance().currentSymbol,
                        isSub = true,
                        step = curDepthIndex.toString()).channel
                ) { //                    LogUtil.d(TAG, "=======?????????$data")
                    val temp = System.currentTimeMillis() - klineTime
                    if (temp <= 3000) {
                        klineTime = temp
                    }
                    activity?.runOnUiThread {
                        v_horizontal_depth?.refreshDepthView(jsonObj)
                        v_vertical_depth?.refreshDepthView(jsonObj)
                    }
                } // ????????????
                var etfUpAndDown = coinMapData?.optJSONArray("etfUpAndDown")
                for (item in 0 until (etfUpAndDown?.length() ?: 0)) {
                    val coin = etfUpAndDown?.getString(item)!!
                    if (channel == WsLinkUtils.tickerFor24HLink(coin, isChannel = true)) {
                        if (tick == null) {
                            return
                        }
                        activity?.runOnUiThread {
                            val rose = tick.getRose()
                            val roseRes = ColorUtil.getMainColorBgType(RateManager.getRoseTrend(rose) >= 0)
                            if (item == 0) {
                                RateManager.getRoseText(trade_topleft_coin_ratio, rose)
                                trade_topleft_coin_ratio?.textColor = roseRes.first

                                RateManager.getRoseText(tradev_topleft_coin_ratio, rose)
                                tradev_topleft_coin_ratio?.textColor = roseRes.first
                            } else {
                                RateManager.getRoseText(trade_topright_coin_ratio, rose)
                                trade_topright_coin_ratio?.textColor = roseRes.first

                                RateManager.getRoseText(tradev_topright_coin_ratio, rose)
                                tradev_topright_coin_ratio?.textColor = roseRes.first
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getEntrustInterval5(status: Boolean = true) { //        Log.d(TAG, "========getEntrustInterval5()${disposables?.isDisposed},::${disposables?.size()}=======")
        loopData(status)
    }

    private fun loopData(status: Boolean = true) { //        LogUtil.e(TAG, "ETF value loopData  $mIsVisibleToUser $cvcFragment")
        if (!mIsVisibleToUser || !cvcFragment) return
        if (subscribeCoin == null || (subscribeCoin != null && subscribeCoin?.isDisposed != null && subscribeCoin?.isDisposed!!)) {
            subscribeCoin = Observable.interval(0L, CommonConstant.coinLoopTime, TimeUnit.SECONDS) //??????????????????????????????Observable
                .observeOn(AndroidSchedulers.mainThread()) //????????????????????????UI
                .subscribe {
                    getEachEntrust(status)
                }
        }

    }


    //????????????
    fun getEachEntrust(status: Boolean = true) {
        if (!LoginManager.checkLogin(activity, false)) {
            return
        }
        var symbolData = ""
        if (img_on.visibility == View.VISIBLE) {
            symbolData = symbol
        }

        addDisposableTrade(getMainModel().getNewEntrust(symbol = symbolData, consumer = object : NDisposableObserver(null, showToast = false) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                val data = jsonObject.optJSONObject("data")
                data?.run {
                    availableBalanceData = data
                    showBalanceData()
                    val jsonArray =
                        data.getJSONArray("orderList") //                    Log.d(TAG, "====length:${jsonArray.length()},is:$isCreatedOrder======")
                    if (jsonArray.length() == 0) {
                        currentOrderList.clear()
                        curEntrustAdapter.notifyDataSetChanged()
                        curEntrustAdapter.setList(currentOrderList)
                        displayHeaderWithNum() //                        if (isCreatedOrder) {
                        //                        } else {
                        //                            disposables?.clear()
                        //                            isCreatedOrder = false
                        //                        }
                    } else { //                        isCreatedOrder = false
                        currentOrderList.clear()
                        jsonArray.run { //                            Log.d(TAG, "======mList=()====" + jsonArray.length())
                            currentOrderList.clear()
                            for (i in 0 until jsonArray.length()) {
                                currentOrderList.add(jsonArray.optJSONObject(i))
                            }
                            curEntrustAdapter.setList(currentOrderList)
                            displayHeaderWithNum()
                        }
                    }
                    v_vertical_depth?.changeOrder(currentOrderList)
                    v_horizontal_depth?.changeOrder(currentOrderList)

                }
                if (status) {
                    loopData()
                }

            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                if (status) {
                    loopData()
                }
            }
        }))
    }

    /**
     * ????????????
     */
    private fun deleteOrder(order_id: String, symbol: String, pos: Int) {
        addDisposable(getMainModel().cancelOrder(order_id = order_id, symbol = symbol, consumer = object : NDisposableObserver(activity, true) {
            override fun onResponseSuccess(data: JSONObject) {
                NToastUtil.showTopToastNet(this.mActivity, true, LanguageUtil.getString(context, "common_tip_cancelSuccess"))
                val obj = curEntrustAdapter.getItem(pos)
                curEntrustAdapter.remove(pos)
                clearToolHttp()
                displayHeaderWithNum()
            }
        }))
    }


    /*
     * ????????????
     */
    private fun hideDialog() {
        if (tDialog?.isVisible == true) {
            tDialog?.dismiss()
            hasShowDialog = false
        }

    }

    /**
     * ????????????
     */
    private var hasShowDialog = false

    fun showDialog(content: String): TDialog? {
        val a = (activity as NewMainActivity).curPosition == 2 && TradeFragment.currentIndex == ParamConstant.CVC_INDEX_TAB
        if (!a) {
            return null
        }
        if (hasShowDialog) {
            return tDialog
        }
        hasShowDialog = true
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager).setLayoutRes(R.layout.item_normal_dialog)
            .setScreenWidthAspect(context, 0.8f).setGravity(Gravity.CENTER).setDimAmount(0.8f).setCancelableOutside(false)
            .setOnBindViewListener { viewHolder: BindViewHolder? ->
                viewHolder?.run {
                    setGone(R.id.tv_title, true)
                    setText(R.id.tv_title, LanguageUtil.getString(context, "tradeLimit_text_instructions"))
                    setGone(R.id.tv_cancel, false)
                    setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "alert_common_iknow"))
                    setText(R.id.tv_content, content)
                }

            }.addOnClickListener(R.id.tv_cancel, R.id.tv_confirm_btn).setOnViewClickListener { _, view, tDialog ->
                when (view.id) {
                    R.id.tv_cancel -> {
                        tDialog.dismiss()
                        hasShowDialog = false
                    }
                    R.id.tv_confirm_btn -> {
                        tDialog.dismiss()
                        hasShowDialog = false
                    }
                }
            }.create().show()

    }


    private var cvcFragment = true

    override fun background() {
        super.background()
        cvcFragment = false
    }

    override fun foreground() {
        super.foreground()
        cvcFragment = true
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        val mainActivity = activity
        if (mainActivity != null) {
            if (mainActivity is NewMainActivity) {
                if (isVisibleToUser && mainActivity.curPosition == 2) {
                    cvcFragment = true
                    if (etfIsShow) {
                        etfIsShow = false
                        getETFStateData()
                    }

                    if (PublicInfoDataService.getInstance().isHorizontalDepth) {
                        v_horizontal_depth?.visibility = View.VISIBLE
                        v_vertical_depth?.visibility = View.GONE
                    } else {
                        v_horizontal_depth?.visibility = View.GONE
                        v_vertical_depth?.visibility = View.VISIBLE
                    }

                    setTopBar()
                    getETFValue()
                    getAvailableBalance()
                    getEntrustInterval5()

                    Handler().postDelayed({
                        if (!isFromOtherPage) {
                            getTradeLimitInfo(coinMapData = coinMapData)
                        }
                    }, 200)

                } else {
                    cvcFragment = false
                    isFromOtherPage = false
                    startReq = 0
                    hideDialog()
                    subscribeCoin?.dispose()
                    subscribe?.dispose()
                }
            }
        }


    }


    private var isFromOtherPage = false

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.coinTrade_topTab_type == event?.msg_type) {
            val msg_content = event.msg_content //            LogUtil.d(TAG, "observeData==msg_content is ${event.msg_content}")
            if (null != msg_content && msg_content is Bundle) {
                isFromOtherPage = true
                var bundle = msg_content as Bundle
                tradeOrientation = bundle.getInt(ParamConstant.transferType)
                var symbol = bundle.getString(ParamConstant.symbol) //                LogUtil.d(TAG, "observeData==symbol is ${symbol}")
                et_price?.text?.clear()
                showSymbolSwitchData(symbol ?: "")
                setTagView(NCoinManager.getNameForSymbol(symbol))
                setTopBar()
            }
        } else if (MessageEvent.symbol_switch_type == event.msg_type) {
            val msg_content = event.msg_content
            if (null != msg_content && event.isBibi) {
                val nSymbol = msg_content as String

                setTagView(NCoinManager.getNameForSymbol(nSymbol))

                showSymbolSwitchData(nSymbol)
                setTopBar()
                v_vertical_depth.coinSwitch(nSymbol)
                v_horizontal_depth.coinSwitch(nSymbol)
            }
        } else if (MessageEvent.CREATE_ORDER_TYPE == event.msg_type) { // ????????????
            isCreatedOrder = event.msg_content as Boolean
            if (isCreatedOrder) {
                if (event.dataIsNotNull()) {
                    val item = event.dataJson
                    curEntrustAdapter.addData(0, item)
                    curEntrustAdapter.notifyDataSetChanged()
                    displayHeaderWithNum()
                }
                clearToolHttp()
            }
        }
    }


    /**
     * ??????ETF??????????????????
     */
    private fun getETFStateData() {
        var url = ""
        if (coinMapData?.optInt("etfOpen", 0) == 1) {
            v_vertical_depth?.changeEtf(null)
            v_horizontal_depth?.changeEtf(null)
            addDisposable(getMainModel().getETFInfo(object : NDisposableObserver() {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    val json = jsonObject.optJSONObject("data")
                    url = json?.optString("faqUrl") ?: ""
                    v_vertical_depth?.changeEtfInfo(json)
                    v_horizontal_depth?.changeEtfInfo(json)
                }
            }))

        } else {
            v_vertical_depth?.resetEtf()
            v_horizontal_depth?.resetEtf()
        }


    }


    private fun getETFValue() {
        v_vertical_depth?.resetEtf(false)
        v_horizontal_depth?.resetEtf(false)
        if (coinMapData?.optInt("etfOpen", 0) == 1) {
            netValueDisposable?.clear()
            subscribe?.dispose()
            loopPriceRiskPosition()
        } else {
            subscribe?.dispose()
            netValueDisposable?.clear()
        }
    }


    /**
     * ???5s??????????????????
     */
    private fun loopPriceRiskPosition() {
        subscribe?.dispose()
        subscribe = Observable.interval(0, CommonConstant.etfLoopTime, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribeWith(getObserver())
    }

    private fun getObserver(): DisposableObserver<Long> {
        return object : DisposableObserver<Long>() {
            override fun onComplete() {
            }

            override fun onNext(t: Long) {
                val name = coinMapData?.optString("name")
                val base = NCoinManager.getMarketCoinName(name)
                val quote = NCoinManager.getMarketName(name)
                (netValueDisposable ?: CompositeDisposable()).add((getMainModel()).getETFValue(base = base,
                    quote = quote,
                    consumer = object : NDisposableObserver() {
                        override fun onResponseSuccess(data: JSONObject) {

                            v_vertical_depth?.changeEtf(data)
                            v_horizontal_depth?.changeEtf(data)
                        }
                    })!!)
            }

            override fun onError(e: Throwable) {
                e.printStackTrace()
            }
        }

    }


    override fun onStop() {
        super.onStop()
        netValueDisposable?.clear()
        subscribe?.dispose()
        subscribeCoin?.dispose()

    }

    fun sendAgentData(step: String = "") {
        var stepTemp = curDepthIndex.toString()
        if (step.isNotEmpty()) {
            stepTemp = step
        }
        if (symbol.isNotEmpty()) {
            klineTime = System.currentTimeMillis()
            wsNetworkChange()

            var etfUpAndDown = coinMapData?.optJSONArray("etfUpAndDown")
            var subsymbol = symbol
            for (item in 0 until (etfUpAndDown?.length() ?: 0)) {
                val coin = etfUpAndDown?.getString(item)!!
                subsymbol += "," + coin
            }
            WsAgentManager.instance.sendMessage(hashMapOf("symbol" to subsymbol, "step" to stepTemp), this)
        }
    }

    fun unbindAgentData() {
        if (symbol.isNotEmpty()) {
            WsAgentManager.instance.unbind(this, true)
        }
        lastTick = null
    }

    override fun onWsMessage(json: String) {
        handleData(json)
    }

    private fun clearToolHttp() {
        clearDisposableTrade()
        subscribeCoin?.dispose()
        loopData(false)
    }


    fun isInitSymbol(): Boolean {
        return symbol.isNotEmpty()
    }

    fun changeInitData() {
        coinMapData = NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol)
        showTopCoin()
        symbol = coinMapData?.optString("symbol") ?: "" //return
        setTagView(coinMapData?.optString("name", "").toString())
    }


    var klineTime = 0L

    private fun wsNetworkChange() {
        GlobalScope.launch {
            delay(3000L)
            if (v_horizontal_depth != null && v_horizontal_depth.depthBuyOrSell().isNotEmpty()) {
                val statusType = v_horizontal_depth.depthBuyOrSell().getKlineByType(WsAgentManager.instance.pageSubWs(this@NCVCTradeFragment))
                sendWsHomepage(mIsVisibleToUser,
                    statusType,
                    NetworkDataService.KEY_PAGE_TRANSACTION,
                    NetworkDataService.KEY_SUB_TRAN_DEPTH,
                    klineTime)

            }
        }
    }

    /*
     * ????????????????????????????????????
     */
    private fun showImgCollect(hasCollect: Boolean, isShowToast: Boolean, isAddRemove: Boolean) {
        if (hasCollect) {
            ib_collect?.setImageResource(R.drawable.quotes_optional_selected2)
            if (isShowToast) {
                NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(activity, "kline_tip_addCollectionSuccess"))
            }
            if (isAddRemove) {
                LikeDataService.getInstance().saveCollecData(symbol, null)
            }
        } else {
            ib_collect?.setImageResource(R.drawable.quotes_optional_default2)
            if (isShowToast) {
                NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(activity, "kline_tip_removeCollectionSuccess"))
            }
            if (isAddRemove) {
                LikeDataService.getInstance().removeCollect(symbol)
            }
        }
    }

    /*
    * ??????????????????????????????????????????
    */
    var serverSelfSymbols = ArrayList<String>()

    /**
     * ????????????
     */
    private var operationType = 0
    val addCancelUserSelfDataReqType = 1 // ???????????????????????????
    val getUserSelfDataReqType = 2 // ???????????????????????????
    var sync_status = ""

    private fun collectCoin() {
        serverSelfSymbols.clear()
        /**
         * ?????????????????????"??????"?????????
         */
        LogUtil.d("collectCoin", "isLogined is $isLogined,isOptionalSymbolServerOpen is $isOptionalSymbolServerOpen")

        if (isLogined && isOptionalSymbolServerOpen) {
            getOptionalSymbol()
        } else {
            var hasCollect = LikeDataService.getInstance().hasCollect(symbol)
            showImgCollect(hasCollect, false, false)
        }

        ib_collect?.setOnClickListener {

            if (isLogined && isOptionalSymbolServerOpen) {

                if (serverSelfSymbols.contains(symbol)) {
                    operationType = 2
                } else {
                    operationType = 1
                }
                addOrDeleteSymbol(operationType, symbol)

            } else {
                val hasCollect = LikeDataService.getInstance().hasCollect(symbol)
                var isExist = !hasCollect

                showImgCollect(isExist, true, true)

            }
        }
    }

    /*
     * ?????????????????????????????????
     * var req_type = type
     */
    private fun getOptionalSymbol() {
        addDisposable(getMainModel().getOptionalSymbol(MyNDisposableObserver(getUserSelfDataReqType), ""))
    }

    inner class MyNDisposableObserver(type: Int) : NDisposableObserver(mActivity) {

        var req_type = type
        override fun onResponseSuccess(jsonObject: JSONObject) {
            if (getUserSelfDataReqType == req_type) {
                showServerSelfSymbols(jsonObject.optJSONObject("data"))
            } else if (addCancelUserSelfDataReqType == req_type) {
                var hasCollect = false
                if (operationType == 2) {
                    serverSelfSymbols.remove(symbol)
                } else {
                    hasCollect = true
                    serverSelfSymbols.add(symbol)
                }
                showImgCollect(hasCollect, true, true)
            }
        }
    }

    private fun showServerSelfSymbols(data: JSONObject?) {

        if (null == data || data.length() <= 0) return

        var array = data.optJSONArray("symbols")
        sync_status = data.optString("sync_status")

        if (null == array || array.length() <= 0) {
            return
        }
        for (i in 0 until array.length()) {
            serverSelfSymbols.add(array.optString(i))
        }
        if (serverSelfSymbols.contains(symbol)) {
            ib_collect?.setImageResource(R.drawable.quotes_optional_selected2)
        } else {
            ib_collect?.setImageResource(R.drawable.quotes_optional_default2)
        }
    }

    /**
     * ??????????????????????????????
     * @param operationType ?????? 0(????????????)/1(????????????)/2(????????????)
     * @param symbol ??????????????????
     */
    private fun addOrDeleteSymbol(operationType: Int = 0, symbol: String?) {

        if (null == symbol) return
        var list = ArrayList<String>()
        list.add(symbol)
        addDisposable(getMainModel().addOrDeleteSymbol(operationType, list, "", MyNDisposableObserver(addCancelUserSelfDataReqType)))
    }

}

