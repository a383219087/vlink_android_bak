package com.yjkj.chainup.new_version.activity

import android.animation.*
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jaeger.library.StatusBarUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.DepthBean
import com.yjkj.chainup.bean.QuotesData
import com.yjkj.chainup.bean.kline.DepthItem
import com.yjkj.chainup.databinding.ActivityMarketDetail4Binding
import com.yjkj.chainup.db.constant.*
import com.yjkj.chainup.db.service.LikeDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.KLineScaleAdapterV2
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.bean.FlagBean
import com.yjkj.chainup.new_version.dialog.DialogUtil
import com.yjkj.chainup.new_version.fragment.*
import com.yjkj.chainup.new_version.home.NetworkDataService
import com.yjkj.chainup.new_version.home.sendWsHomepage
import com.yjkj.chainup.new_version.kline.bean.KLineBean
import com.yjkj.chainup.new_version.kline.data.DataManager
import com.yjkj.chainup.new_version.kline.data.KLineChartAdapter
import com.yjkj.chainup.new_version.kline.view.MainKlineViewStatus
import com.yjkj.chainup.new_version.kline.view.vice.ViceViewStatus
import com.yjkj.chainup.new_version.view.LabelTextView
import com.yjkj.chainup.new_version.view.depth.DepthMarkView
import com.yjkj.chainup.new_version.view.depth.DepthYValueFormatter
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.CoinSearchDialogFg
import com.yjkj.chainup.wedegit.item.GridItemDecoration
import com.yjkj.chainup.ws.WsAgentManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_market_detail4.*
import kotlinx.android.synthetic.main.depth_chart_com.*
import kotlinx.android.synthetic.main.market_info_kline_panel_new.*
import kotlinx.android.synthetic.main.market_info_kline_period_panel.*
import kotlinx.coroutines.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.math.max


/**
 * @description : 币对行情的详细界面
 * @date 2019-3-2
 * @author Bertking
 *
 */
@Route(path = RoutePath.MarketDetail4Activity)
class MarketDetail4Activity : NBaseActivity(), WsAgentManager.WsResultCallback {

    override fun setContentView(): Int = R.layout.activity_market_detail4
    var subscribe: Disposable? = null//保存订阅者
    var isFrist = true
    var disposable: CompositeDisposable = CompositeDisposable()

    @JvmField
    @Autowired(name = ParamConstant.symbol)
    var symbol = ""

    @JvmField
    @Autowired(name = ParamConstant.TYPE)
    var type = ParamConstant.BIBI_INDEX

    /**
     * 显示指标orScale
     */
    var isShow = false

    var showedView: View? = null
    var klineState = 0


    private var titles: ArrayList<String> = arrayListOf()
    private val fragments = arrayListOf<Fragment>()

    var klineData: ArrayList<KLineBean> = arrayListOf()
    private val adapter by lazy { KLineChartAdapter() }

    /**
     * 主图指标的子view
     */
    private val mainViewStatusViews: ArrayList<RadioButton?> by lazy(LazyThreadSafetyMode.NONE) {
        arrayListOf<RadioButton?>(rb_ma, rb_boll)
    }

    /**
     * 副图指标的子view
     */
    private val viceViewStatusViews: ArrayList<RadioButton?> by lazy(LazyThreadSafetyMode.NONE) {
        arrayListOf<RadioButton?>(rb_macd, rb_kdj, rb_rsi, rb_wr)
    }


    companion object {
        /**
         * K线刻度
         */
        const val KLINE_SCALE = 1

        /**
         * K线指标
         */
        const val KLINE_INDEX = 2

    }

    var aF = 0
    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        WsAgentManager.instance.addWsCallback(this)
//        initSocket()
        showLoadingDialog()
        ARouter.getInstance().inject(this)
        setTextConetnt()
        this.aF = getResources().getDimensionPixelOffset(R.dimen.dimen_272)
    }

    fun setTextConetnt() {
        tv_high?.text = LanguageUtil.getString(this, "kline_text_high")
        tv_common_text_dayVolume?.text = LanguageUtil.getString(this, "common_text_dayVolume")
        tv_kline_text_low?.text = LanguageUtil.getString(this, "kline_text_low")
        tv_indicator?.text = LanguageUtil.getString(this, "kline_text_scale")
        tv_main?.text = LanguageUtil.getString(this, "kline_action_main")
        tv_vice?.text = LanguageUtil.getString(this, "kline_action_assistant")
    }

    private var hasInit = false
    override fun onResume() {
        super.onResume()
        initData()
        WsAgentManager.instance.changeKlineKey(this.javaClass.simpleName)
        cur_time_index = KLineUtil.getCurTime4Index()

        curTime = KLineUtil.getCurTime()
        v_kline?.setMainDrawLine(KLineUtil.getCurTime4Index() == 0)
//        tv_scale?.text = curTime
        isFrist = true
        klineData.clear()

        if (!hasInit) {
            initView()
            setDepthSymbol()
            hasInit = true
        }




        initSocket()

    }

    override fun onPause() {
        super.onPause()
        WsAgentManager.instance.unbind(this, true)
        lastTick = null
        sendMsg(WsLinkUtils.getKlineNewLink(symbol, curTime, false).json)
        disposable.clear()
    }

    private fun setOnclick() {
        coin_info_item.setOnTouchListener(OnTouchListener { view, motionEvent -> klinPanelTouch(view, motionEvent) })
        id_kline_period_more_bg_view.setOnClickListener {
            b(false)
        }
        id_kline_index_setting_bg_view.setOnClickListener {
            rightSettings(false)
        }
        btn_buy?.setOnClickListener {
            hometab_switch(ParamConstant.TYPE_BUY)
            finish()
        }

        btn_sell?.setOnClickListener {
            hometab_switch(ParamConstant.TYPE_SELL)
            finish()
        }

        /**
         * 横屏KLine
         */
        layout_landscape?.setOnClickListener {
            b(false)
            ArouterUtil.navigation(RoutePath.HorizonMarketDetailActivity,
                    Bundle().apply {
                        putString("curTime", curTime)
                        putString("symbolHorizon", symbol)
                    }
            )
        }

        binding.ibBack?.setOnClickListener {
            finish()
        }

        ib_share?.setOnClickListener {
            DialogUtil.showKLineShareDialog(context = mActivity)
        }

        /**
         * 切换币对
         */
        ll_coin_map?.setOnClickListener {
            if (type == ParamConstant.LEVER_INDEX) {
                showLeftCoinPopu(TradeTypeEnum.LEVER_TRADE.value)
            } else if (type == ParamConstant.GRID_INDEX) {
                showLeftCoinPopu(TradeTypeEnum.GRID_TRADE.value)
            } else {
                showLeftCoinPopu(TradeTypeEnum.COIN_TRADE.value)
            }
        }

        layout_depth_view?.setOnClickListener {
            // 切换深度 关闭 开启定时器
            textClick(layout_depth_view.getChildAt(0) as TextView)
            resetClick()
            customize_depth_chart.visibility = View.VISIBLE
            changeTab(-1)
            initTimerDepthView()
        }

        /**
         * KLine指标
         */
        layout_indicator?.setOnClickListener {
            rightSettings(!af)
        }

        val layoutParams = id_abl_kline_appbar_layout?.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior as AppBarLayout.Behavior
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return true
            }
        })

        market_info_refresh?.setOnRefreshListener {

        }
    }

    /*
     * 显示侧边栏
     */
    private fun showLeftCoinPopu(type: Int) {
        if (Utils.isFastClick())
            return
        var mCoinSearchDialogFg = CoinSearchDialogFg()
        var bundle = Bundle()
        bundle.putInt(ParamConstant.TYPE, type)
        bundle.putString(ParamConstant.COIN_SYMBOL, symbol)
        bundle.putBoolean(ParamConstant.ISBLACK, isblack)
        mCoinSearchDialogFg.arguments = bundle
        mCoinSearchDialogFg.showDialog(supportFragmentManager, "MarketDetail4Activity")
    }


    private var isLogined = false
    private var isOptionalSymbolServerOpen = false
    private fun initData() {
        isLogined = UserDataService.getInstance().isLogined
        isOptionalSymbolServerOpen = PublicInfoDataService.getInstance().isOptionalSymbolServerOpen(null)
        initCoinData()
        initKLineData()
    }

    /*
    *  币对参数数据初始化
    * */
    private var jsonObject: JSONObject? = null
    private var showName: String? = null
    private var coinName: String? = null
    private var marketName: String? = null
    private var realMarketName = ""
    private var multiple = ""
    private var isSymbolProfile = false
    private var isCoinIntroduce = false
    private var isSymbolEtf = false

    /**
     * 初始化币对数据
     */
    private fun initCoinData() {
        jsonObject = NCoinManager.getSymbolObj(symbol)
        /* 杠杆倍数 */
        multiple = jsonObject?.optString("multiple", "") ?: ""
        showName = NCoinManager.showAnoterName(jsonObject)
        realMarketName = NCoinManager.getMarketName(jsonObject?.optString("name", "") ?: "")
        if (StringUtil.checkStr(showName) && showName!!.contains("/")) {
            var split = showName!!.split("/")
            coinName = split[0]
            marketName = split[1]
        }
        isSymbolEtf = jsonObject?.optInt("etfOpen", 0) == 1
        isSymbolProfile = PublicInfoDataService.getInstance().isSymbolProfile(null)
        isCoinIntroduce = PublicInfoDataService.getInstance().getCoinsymbolIntroduceNames(null).contains(NCoinManager.getMarketCoinName(jsonObject?.optString("name", "")
                ?: ""))

    }

    /*
     * KLine参数数据初始化
     */
    private var main_index = 0
    private var vice_index = 0
    private var curTime: String? = ""
    private var cur_time_index = 0;
    private var klineScale = ArrayList<String>()
    private var klineOptions = ArrayList<String>()
    private var klineDefaults = ArrayList<String>()
    private var themeMode = PublicInfoDataService.THEME_MODE_DAYTIME
    private var kLineLogo = ""
    private fun initKLineData() {
        main_index = KLineUtil.getMainIndex()
        vice_index = KLineUtil.getViceIndex()
        cur_time_index = KLineUtil.getCurTime4Index()

        curTime = KLineUtil.getCurTime()
        klineScale = KLineUtil.getKLineScale()
        klineDefaults = KLineUtil.getKLineDefaultScale()
        val temp = klineScale subtract klineDefaults
        klineDefaults.add(LanguageUtil.getString(this, "more"))
        klineOptions.clear()
        klineOptions.addAll(temp)

        themeMode = PublicInfoDataService.getInstance().klineThemeMode
        kLineLogo = PublicInfoDataService.getInstance().getKline_background_logo_img(null, themeMode, true)

    }

    var klineTime = 0L

    override fun initView() {
        if (type == ParamConstant.LEVER_INDEX) {
            btn_buy?.text = "${LanguageUtil.getString(this, "contract_action_buy")}/${LanguageUtil.getString(this, "contract_action_long")}"
            btn_sell?.text = "${LanguageUtil.getString(this, "contract_action_sell")}/${LanguageUtil.getString(this, "contract_action_short")}"
        } else {
            btn_buy?.text = LanguageUtil.getString(this, "contract_action_buy")
            btn_sell?.text = LanguageUtil.getString(this, "contract_action_sell")
        }

        initDepthAndDeals()

        v_kline?.adapter = adapter
        v_kline?.startAnimation()
        v_kline?.justShowLoading()

        NLiveDataUtil.observeForeverData {
            if (null != it && MessageEvent.symbol_switch_type == it.msg_type) {
                if (null != it.msg_content) {
                    symbol = it.msg_content as String
                    setTagView(NCoinManager.getNameForSymbol(symbol))
                    isFrist = true
                    klineData.clear()
                    clearDepthChart()
                    getSymbol(symbol)
                    showCoinName()
                }
            }
        }

        showCoinName()

        collectCoin()

        initViewColor()

        initKLineScale()
        initDepthChart()
        action4KLineIndex()



        setTagView(NCoinManager.getNameForSymbol(symbol))
        GlobalScope.launch {
            LogUtil.e(TAG, "k线网络统计 start ws状态 " + WsAgentManager.instance.isConnection())
            delay(3000L)
            val isResult = klineData.size
            LogUtil.e(TAG, "k线网络统计 end ws状态 " + WsAgentManager.instance.isConnection() + " k线数据 ${isResult} " + " ${isFinishing}" + " time ${klineTime}")

            val statusType = klineData.getKlineByType(WsAgentManager.instance.pageSubWs(this@MarketDetail4Activity))
            sendWsHomepage(mActivity, statusType, NetworkDataService.KEY_PAGE_KLINE, NetworkDataService.KEY_SUB_KLINE_HISTORY, klineTime)
        }

        if (PublicInfoDataService.getInstance().klineThemeMode == 1) {
//            collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            icon_coin_more?.imageResource = R.mipmap.coins_sidebar_night
            stl_depth_dealt?.textSelectColor = ContextCompat.getColor(this, R.color.text_color_kline_night)
            stl_depth_dealt?.textUnselectColor = ContextCompat.getColor(this, R.color.normal_text_color_kline_night)
        } else {
            icon_coin_more?.imageResource = R.mipmap.coins_sidebar
        }

    }

    /**
     * 显示tag
     */
    fun setTagView(name: String) {
        var tagCoin = NCoinManager.getMarketCoinName(name)
        if (!TextUtils.isEmpty(NCoinManager.getCoinTag4CoinName(tagCoin))) {
            ctv_content?.visibility = View.VISIBLE
            ctv_content?.setTextViewContent(NCoinManager.getCoinTag4CoinName(tagCoin))
        } else {
            ctv_content?.visibility = View.GONE
        }

    }

    private fun showCoinName() {
        LogUtil.d(TAG, "showCoinName==showName is " + showName)
        if (StringUtil.checkStr(showName)) {
            tv_coin_map?.text = if (type == ParamConstant.LEVER_INDEX) {
                "$showName ${multiple}X"
            } else {
                showName
            }
        }
    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (event.msg_type == MessageEvent.market_switch_curTime) {
            curTime = event.msg_content as String
            Log.e("shengong", "curTime2:$curTime")
            switchKLineScale(curTime ?: "15min")
            val position = klineDefaults.indexOf(curTime ?: "15min")
            LogUtil.e(TAG, "klineDefaults index ${position}")
            if (position != -1) {
                changeTab(position)
            } else {
                changeTab(klineDefaults.size - 1)
            }
            calibrationAdapter?.notifyDataSetChanged()
        }

    }

    private fun hometab_switch(buyOrSell: Int) {

        var messageEvent2 = MessageEvent(MessageEvent.symbol_switch_type)
        messageEvent2.msg_content = symbol
        messageEvent2.isLever = type == ParamConstant.LEVER_INDEX
        messageEvent2.isGrid = type == ParamConstant.GRID_INDEX
        EventBusUtil.post(messageEvent2)

        val messageEvent = MessageEvent(MessageEvent.hometab_switch_type)
        val bundle = Bundle()
        val homeTabType = HomeTabMap.maps.get(HomeTabMap.coinTradeTab) ?: 2
        bundle.putInt(ParamConstant.homeTabType, homeTabType)//跳转到币币交易页面
        bundle.putInt(ParamConstant.transferType, buyOrSell)
        bundle.putString(ParamConstant.symbol, symbol)
        var coinTabIndex = ParamConstant.CVC_INDEX_TAB
        if (ParamConstant.LEVER_INDEX == type) {
            coinTabIndex = ParamConstant.LEVER_INDEX_TAB
        } else if (ParamConstant.GRID_INDEX == type) {
            coinTabIndex = ParamConstant.GRID_INDEX_TAB
        }
        bundle.putInt(ParamConstant.COIN_TRADE_TAB_INDEX, coinTabIndex)
        messageEvent.msg_content = bundle
        EventBusUtil.post(messageEvent)
    }


    fun getSymbol(symbol: String) {

        LogUtil.d(TAG, "=======SYM:$symbol,CUR:${jsonObject?.optJSONObject("symbol")}======")
        if (jsonObject?.optString("symbol") != symbol) {
            initCoinData()
            chooseCoinAfterAddDetailsFg()
            setDepthSymbol()
            initHeaderData()
            collectCoin()
            /**
             * 逻辑声明：
             * 先取消订阅之前的币种，再订阅新币种
             * PS :历史K线是请求的，不是订阅的
             */
            var lastSymbol = jsonObject?.optString("symbol")
            if (isNotEmpty(lastSymbol)) {
                sendMsg(WsLinkUtils.tickerFor24HLink(lastSymbol!!, isSub = false))
            }
            initSocket()
        }

    }


    /**
     * 处理K线上方View的数据
     */
    private fun initHeaderData() {
        tv_close_price?.text = "--"
        tv_converted_close_price?.text = "--"
        tv_rose?.text = "--"
        tv_high_price?.text = "--"
        tv_low_price?.text = "--"
        tv_24h_vol?.text = "--"
    }


    /**
     * todo 待优化
     * 更新深度数据。liveData方式，是否更新修改为其他方式
     */
    private fun setDepthSymbol() {
        if (jsonObject?.optInt("etfOpen", 0) == 1) {
            loopETFData()
            trade_bottom_Info_etf?.visibility = View.VISIBLE
            val etfInfo = jsonObject.etfItemInfo(this)
            trade_bottomInformation?.apply {
                text = etfInfo
                movementMethod = LinkMovementMethod.getInstance()
            }
        } else {
            trade_bottom_Info_etf?.visibility = View.GONE
            disposable?.clear()
        }

        var symbol = jsonObject?.optString("symbol")
        var price = jsonObject?.optInt("price")
        var volume = jsonObject?.optInt("volume")

        if (null == symbol || null == price || null == volume || null == coinName || null == marketName)
            return
        DepthFragment.liveData.value = FlagBean(isContract = false,
                contractId = "",
                symbol = symbol,
                baseSymbol = coinName!!,
                quotesSymbol = marketName!!,
                pricePrecision = price,
                volumePrecision = volume
        )
    }

    private fun chooseCoinAfterAddDetailsFg() {
        initChooseCoinDetailsFg()
        if (isSymbolProfile && isCoinIntroduce && !isSymbolEtf) {
            coinMapIntroFragment = CoinMapIntroFragment.newInstance(viewPager = vp_depth_dealt, symbol = symbol)
            titles.add(getString(R.string.market_text_coinInfo))
            fragments.add(coinMapIntroFragment ?: return)
        }
        if (vp_depth_dealt == null || vp_depth_dealt.adapter == null) {
            return
        }
        pageAdapter?.notifyDataSetChanged()
        vp_depth_dealt?.offscreenPageLimit = fragments.size
        stl_depth_dealt?.setViewPager(vp_depth_dealt ?: return, titles.toTypedArray())
        stl_depth_dealt?.currentTab = 0

    }

    private fun initChooseCoinDetailsFg() {
        if (coinMapIntroFragment != null) {
            fragments.remove(coinMapIntroFragment ?: return)
        }
        if (titles.size > 2) {
            titles.remove(getString(R.string.market_text_coinInfo))
        }
    }


    /**
     * 添加收藏
     */
    private var operationType = 0

    private fun collectCoin() {
        /**
         * 根据是否存在于"自选"列表中
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
     * 收藏图标状态及其行为处理
     */
    private fun showImgCollect(hasCollect: Boolean, isShowToast: Boolean, isAddRemove: Boolean) {
        if (hasCollect) {
            ib_collect?.setImageResource(R.drawable.quotes_optional_selected)
            if (isShowToast) {
                NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(this, "kline_tip_addCollectionSuccess"))
            }
            if (isAddRemove) {
                LikeDataService.getInstance().saveCollecData(symbol, null)
            }
        } else {
            ib_collect?.setImageResource(R.drawable.quotes_optional_default)
            if (isShowToast) {
                NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(this, "kline_tip_removeCollectionSuccess"))
            }
            if (isAddRemove) {
                LikeDataService.getInstance().removeCollect(symbol)
            }
        }
    }

    /**
     * K线的指标处理
     */
    private fun action4KLineIndex() {

        when (main_index) {
            MainKlineViewStatus.MA.status -> {
                rb_ma?.isChecked = true
            }

            MainKlineViewStatus.BOLL.status -> {
                rb_boll?.isChecked = true
            }

        }

        when (vice_index) {
            ViceViewStatus.MACD.status -> {
                rb_macd?.isChecked = true
            }

            ViceViewStatus.KDJ.status -> {
                rb_kdj?.isChecked = true
            }

            ViceViewStatus.RSI.status -> {
                rb_rsi?.isChecked = true
            }

            ViceViewStatus.WR.status -> {
                rb_wr?.isChecked = true
            }

        }

        mainViewStatusViews.forEach {
            it?.setOnClickListener {
                val index = mainViewStatusViews.indexOf(it)
                mainViewStatusViews[0]?.isChecked = (index == 0)
                mainViewStatusViews[1]?.isChecked = (index == 1)
                v_kline?.apply {
                    val status = getMainDrawStatus()
                    if (status?.status == index) {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.NONE)
                        KLineUtil.setMainIndex(MainKlineViewStatus.NONE.status)
                        mainViewStatusViews[index]?.isChecked = false
                        return@setOnClickListener
                    }
                }
                when (index) {
                    MainKlineViewStatus.MA.status -> {
                        v_kline?.apply {
                            v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
                            KLineUtil.setMainIndex(MainKlineViewStatus.MA.status)
                        }
                    }

                    MainKlineViewStatus.BOLL.status -> {
                        v_kline?.apply {
                            v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
                            KLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status)
                        }

                    }
                }
            }
        }

        /**
         * -----------副图--------------
         */
        viceViewStatusViews.forEach {
            it?.setOnClickListener {
                val index = viceViewStatusViews.indexOf(it)
                viceViewStatusViews[0]?.isChecked = (index == 0)
                viceViewStatusViews[1]?.isChecked = (index == 1)
                viceViewStatusViews[2]?.isChecked = (index == 2)
                viceViewStatusViews[3]?.isChecked = (index == 3)
                if (index == v_kline?.viceDrawStatus) {
                    viceViewStatusViews[index]?.isChecked = false
                    v_kline?.hideChildDraw()
                    KLineUtil.setViceIndex(ViceViewStatus.NONE.status)
                    return@setOnClickListener
                }
                when (index) {
                    ViceViewStatus.MACD.status -> {
                        v_kline?.setChildDraw(0)
                        KLineUtil.setViceIndex(ViceViewStatus.MACD.status)
                    }

                    ViceViewStatus.KDJ.status -> {
                        v_kline?.setChildDraw(1)
                        KLineUtil.setViceIndex(ViceViewStatus.KDJ.status)
                    }

                    ViceViewStatus.WR.status -> {
                        v_kline?.setChildDraw(3)
                        KLineUtil.setViceIndex(ViceViewStatus.WR.status)
                    }

                    ViceViewStatus.RSI.status -> {
                        v_kline?.setChildDraw(2)
                        KLineUtil.setViceIndex(ViceViewStatus.RSI.status)
                    }
                }

            }
        }
    }

    //初次 和 切换币对时触发
    private fun initSocket() {
        if (isNotEmpty(symbol)) {
            // sub ticker
            klineTime = System.currentTimeMillis()
            val scale: String = if (curTime == "line") "1min" else curTime ?: "15min"
            WsAgentManager.instance.sendMessage(hashMapOf("symbol" to symbol, "line" to scale), this)
            getETFData()
        }
    }

    private fun initViewColor() {

        var mainColorType = ColorUtil.getMainBgType()
        btn_buy?.backgroundResource = mainColorType
        btn_sell?.backgroundResource = ColorUtil.getMainBgType(isRise = false)

        tv_close_price?.textColor = mainColorType
        tv_rose?.textColor = mainColorType
    }

    var depthFragment: DepthFragment? = null
    var dealtRecordFragment: DealtRecordFragment? = null
    var coinMapIntroFragment: CoinMapIntroFragment? = null
    var pageAdapter: NVPagerAdapter? = null
    var etfInfoFragment: CoinMapEtfFragment? = null
    var etfInfoRuleFragment: CoinMapETFRuleFragment? = null
    private fun initDepthAndDeals() {

        if (titles.size > 0) {
            titles.clear()
        }

        if (fragments.size > 0) {
            fragments.clear()
        }
        titles.add(LanguageUtil.getString(this, "kline_action_entrustMentOrder"))
        titles.add(LanguageUtil.getString(this, "kline_action_dealHistory"))

        depthFragment = DepthFragment.newInstance(viewPager = vp_depth_dealt)
        dealtRecordFragment = DealtRecordFragment.newInstance(viewPager = vp_depth_dealt)


        fragments.add(depthFragment ?: return)
        fragments.add(dealtRecordFragment ?: return)

        if (isSymbolEtf) {
            titles.add(LanguageUtil.getString(this, "market_text_tab_etf_info"))
            etfInfoFragment = CoinMapEtfFragment.newInstance(viewPager = vp_depth_dealt, symbol = symbol, index = titles.size - 1)
            titles.add(LanguageUtil.getString(this, "market_text_tab_etf_rule"))
            etfInfoRuleFragment = CoinMapETFRuleFragment.newInstance(viewPager = vp_depth_dealt, symbol = symbol, index = titles.size - 1)
            fragments.add(etfInfoFragment ?: return)
            fragments.add(etfInfoRuleFragment ?: return)

        }
        if (isSymbolProfile && isCoinIntroduce) {
            titles.add(LanguageUtil.getString(this, "market_text_coinInfo"))
            coinMapIntroFragment = CoinMapIntroFragment.newInstance(viewPager = vp_depth_dealt, symbol = symbol, index = titles.size - 1)
            fragments.add(coinMapIntroFragment ?: return)
        }

        pageAdapter = NVPagerAdapter(supportFragmentManager, titles, fragments)

        vp_depth_dealt?.adapter = pageAdapter
        vp_depth_dealt.offscreenPageLimit = fragments.size
        vp_depth_dealt?.setScrollable(true)
        stl_depth_dealt?.setViewPager(vp_depth_dealt, titles.toTypedArray())

        vp_depth_dealt?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                selectPosition = position
                // 这里必须调用resetHeight，否则不会重新计算高度
                vp_depth_dealt?.resetHeight(position)
            }
        })

    }

    var selectPosition = 0

    var calibrationAdapter: KLineScaleAdapterV2? = null

    /**
     * 处理K线刻度
     */
    private fun initKLineScale() {
        rv_kline_scale?.isLayoutFrozen = true
        rv_kline_scale?.setHasFixedSize(true)


        rv_kline_scale_default?.apply {
            for ((index, item) in klineDefaults.withIndex()) {
                val view = View.inflate(context, R.layout.item_kline_item_scale, null)
                val boxView = view.findViewById<LabelTextView>(R.id.tv_scale)
                val itemView = view.findViewById<TextView>(R.id.tv_item)
                if (index == klineDefaults.size - 1) {
                    boxView?.text = item.getArraysSymbols(mActivity)
                    boxView?.visibility = View.VISIBLE
                    itemView?.visibility = View.GONE
                } else {
                    itemView?.text = item.getArraysSymbols(mActivity)
                    itemView?.visibility = View.VISIBLE
                    boxView?.visibility = View.GONE
                }
                val lp: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.weight = 1f
                lp.gravity = Gravity.CENTER_VERTICAL
                view.onClick {
                    if (klineDefaults.size - 1 == index) {
                        b(!ai)
                    } else {
                        val item = klineDefaults.get(index);
                        v_kline?.setMainDrawLine(item == "line")
                        KLineUtil.setCurTime4KLine(klineScale.indexOf(item))
                        KLineUtil.setCurTime(item)
                        switchKLineScale(item)
                        changeTab(index)
                        resetClick()
                        textClick(view.findViewById(R.id.tv_item))
                        customize_depth_chart.visibility = View.INVISIBLE
                        restart()
                    }
                }
                addView(view, lp)
            }
        }

        val layoutManager = GridLayoutManager(mActivity, 5)
        layoutManager.isAutoMeasureEnabled = false
        rv_kline_scale?.layoutManager = layoutManager
        val divider: GridItemDecoration = GridItemDecoration.Builder(this)
                .setVerticalSpan(R.dimen.dp_10)
                .setShowLastLine(false)
                .setColorResource(R.color.transparent)
                .build()
        rv_kline_scale?.addItemDecoration(divider)
        calibrationAdapter = KLineScaleAdapterV2(klineOptions, isblack)
        rv_kline_scale?.adapter = calibrationAdapter
        /**
         * 分时线
         */
        v_kline?.setMainDrawLine(cur_time_index == 0)
        v_kline?.apply {
            changeMainDrawType(MainKlineViewStatus.values().get(main_index))
            if (vice_index == ViceViewStatus.NONE.status) {
                v_kline?.hideChildDraw()
            } else {
                setChildDraw(vice_index)
            }
        }
        v_kline?.waterImageUrl = kLineLogo
        val position = klineDefaults.indexOf(curTime)
        LogUtil.e(TAG, "klineDefaults index ${position}")
        if (position != -1) {
            changeTab(position)
        } else {
            changeTab(klineDefaults.size - 1)
        }
        calibrationAdapter?.setOnItemClickListener { viewHolder, view, position ->
            /**
             * 分时线
             */
            v_kline?.setMainDrawLine(position == 0)
            if (position != KLineUtil.getCurTime4Index()) {
                KLineUtil.setCurTime4KLine(position)
                KLineUtil.setCurTime(klineOptions[position])
                switchKLineScale(klineOptions[position])
            } else {
                KLineUtil.setCurTime4KLine(position)
                KLineUtil.setCurTime(klineOptions[position])
                switchKLineScale(klineOptions[position])
            }
            b(false)
            changeTab(klineDefaults.size - 1)
            calibrationAdapter?.notifyDataSetChanged()
        }
    }

    /**
     * 切换K线刻度
     * @param kLineScale K线刻度
     */
    private fun switchKLineScale(kLineScale: String) {
        if (curTime != kLineScale) {
            isFrist = true
            klineData.clear()
            var scale = if (curTime == "line") "1min" else curTime
            /**
             * 取消订阅
             */
            sendMsg(WsLinkUtils.getKlineNewLink(symbol, scale, false).json)
            curTime = kLineScale
            var scale2 = if (curTime == "line") "1min" else curTime
            /**
             * 请求历史
             */
            sendMsg(WsLinkUtils.getKLineHistoryLink(symbol, scale2).json)
            /**
             * 订阅
             */
            sendMsg(WsLinkUtils.getKlineNewLink(symbol, scale2).json)
            initSocket()
        }

    }

    private var lastTick: QuotesData? = null
    private var isRealNew = false

    /**
     * 处理 24H,KLine数据
     */
    fun handleData(data: String) {
        try {
            var jsonObj = JSONObject(data)
            if (!jsonObj.isNull("tick")) {
                /**
                 * 24H行情
                 */
                if (jsonObj.getString("channel") == WsLinkUtils.tickerFor24HLink(symbol, isChannel = true)) {
                    val quotesData = JsonUtils.convert2Quote(jsonObj.toString())
                    if (lastTick != null) {
                        var lastTime = lastTick?.ts ?: 0L
                        var time = quotesData.ts
                        if (time < lastTime) {
                            return
                        }
                    }
                    lastTick = quotesData
                    render24H(quotesData.tick)
                    return
                }

                /**
                 * 最新K线数据
                 */
                var scale = if (curTime == "line") "1min" else curTime
                if (jsonObj.getString("channel") == WsLinkUtils.getKlineNewLink(symbol, scale).channel) {
                    Log.w(TAG, "=======最新K线： ${klineData.size} ||  ${klineData.lastIndex}==  adapter ${adapter.getCount()}======")
                    doAsync {
                        /**
                         * 这里需要处理：重复添加的问题
                         */
                        val kLineEntity = KLineBean()
                        val data = jsonObj.optJSONObject("tick")
                        kLineEntity.id = data.optLong("id")
                        kLineEntity.openPrice = kLineEntity.getCloseTempPrice(data.optString("open"), jsonObject?.optInt("price"))
                        kLineEntity.closePrice = kLineEntity.getCloseTempPrice(data.optString("close"), jsonObject?.optInt("price"))
                        kLineEntity.highPrice = kLineEntity.getCloseTempPrice(data.optString("high"), jsonObject?.optInt("price"))
                        kLineEntity.lowPrice = kLineEntity.getCloseTempPrice(data.optString("low"), jsonObject?.optInt("price"))
                        kLineEntity.volume = BigDecimal(data.optString("vol")).toFloat()

                        try {
                            if (klineData.isNotEmpty() && klineData.size != 0) {
                                val isRepeat = klineData.last().id == data.optLong("id")
                                if (isRepeat) {
                                    klineData[klineData.lastIndex] = kLineEntity
                                    DataManager.calculate(klineData)
                                    adapter.changeItem(klineData.lastIndex, kLineEntity)
                                } else {
                                    klineData.add(kLineEntity)
                                    DataManager.calculate(klineData)
                                    uiThread {
                                        adapter.addFooterData(klineData)
                                        v_kline?.refreshEnd()
                                    }
                                }
                            } else {
                                klineData.add(kLineEntity)
                                DataManager.calculate(klineData)
                                uiThread {
                                    adapter.addItems(arrayListOf(kLineEntity))
                                    v_kline?.refreshEnd()
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                }

                if (jsonObj.getString("channel") == WsLinkUtils.getDepthLink(symbol).channel) {
                    if (depthFragment != null) {
                        depthFragment?.onDepthFragment(data)
                    }
                }
                realtData(jsonObj, data)
                return
            } else {
                var scale = if (curTime == "line") "1min" else curTime
                if (!jsonObj.isNull("data") && jsonObj.getString("channel") == WsLinkUtils.getKLineHistoryLink(symbol, scale).channel) {
                    /**
                     * 请求(req) ----> K线历史数据
                     * 即：K线图的历史数据
                     *
                     * channel ---> channel":"market_ltcusdt_kline_1week
                     */
                    closeLoadingDialog()
                    handlerKLineHistory(data)
                    return
                }
                realtData(jsonObj, data)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 渲染24H行情数据
     */
    private fun render24H(tick: QuotesData.Tick) {
        val price = jsonObject?.optInt("price") ?: return
        val vol = jsonObject?.optInt("volume") ?: return

        DepthFragment.liveData4closePrice.postValue(arrayListOf(tick.close, price.toString(), symbol))

        runOnUiThread {
            tv_close_price?.run {
                textColor = ColorUtil.getMainColorType(tick.rose >= 0)
                text = DecimalUtil.cutValueByPrecision(tick.close, price)
            }

            /**
             * 收盘价的法币换算
             */
            tv_converted_close_price?.text = RateManager.getCNYByCoinName(realMarketName, tick.close)
            val rose = tick?.rose.toString()
            RateManager.getRoseText(tv_rose, rose)
            tv_rose?.textColor = ColorUtil.getMainColorType(RateManager.getRoseTrend(rose) >= 0)
            tv_high_price?.text = DecimalUtil.cutValueByPrecision(tick.high, price)
            tv_low_price?.text = DecimalUtil.cutValueByPrecision(tick.low, price)
            tv_24h_vol?.text = DecimalUtil.cutValueByPrecision(tick.vol, vol)
        }
    }

    /**
     * 处理K线历史数据
     * @param data K线历史数据
     */
    private fun handlerKLineHistory(data: String) {
        doAsync {
            val json = JSONObject(data)
            val type = object : TypeToken<ArrayList<KLineBean>>() {
            }.type
            val gson = GsonBuilder().setPrettyPrinting().create()
            if (isFrist) {
                klineData.clear()
                val list: ArrayList<KLineBean> = gson.fromJson(json.getJSONArray("data").toString(), type)
                klineData.addAll(list)
                if (klineData.size == 0) {
                    initKlineData()
                } else {
                    val scale = if (curTime == "line") "1min" else curTime
                    WsAgentManager.instance.sendData(WsLinkUtils.getKlineHistoryOther(symbol, scale, list[0].id.toString()))
                }
                isFrist = false
                return@doAsync
            } else {
                klineData.addAll(0, gson.fromJson(json.getJSONArray("data").toString(), type))
            }
            initKlineData()
        }

    }

    private fun initKlineData() {
        klineTime = System.currentTimeMillis() - klineTime
        runOnUiThread {
            DataManager.calculate(klineData)
            adapter.addFooterData(klineData)
            v_kline?.refreshEnd()
            if (v_kline?.minScrollX != null) {
                if (klineData.size < 30) {
                    v_kline?.scrollX = 0
                } else {
                    v_kline?.scrollX = v_kline!!.minScrollX
                }
            }
        }
        /**
         * 获取最新K线数据
         */
        runOnUiThread {
            val scale = if (curTime == "line") "1min" else curTime
            WsAgentManager.instance.sendData(WsLinkUtils.getKlineNewLink(symbol, scale))
        }

    }

    /**
     * WebSocket 发送消息
     */
    private fun sendMsg(msg: String) {

    }


    /**
     * 处理 点击控件外，隐藏
     */
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val viewRect = Rect()
            var tempView: View? = null
            if (showedView?.id == R.id.ly_kline_more_panel) {
                tempView = showedView?.findViewById<RecyclerView>(R.id.rv_kline_scale)
            } else {
                tempView = showedView?.findViewById<LinearLayout>(R.id.id_kline_index_setting_view)
            }

            tempView?.getGlobalVisibleRect(viewRect)
            LogUtil.e(TAG, "getGlobalVisibleRect ${viewRect} ${event.rawX.toInt()} || ${event.rawY.toInt()}")
            if (!viewRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                if (showedView?.visibility == View.VISIBLE) {
                    val alphaAnimation = AlphaAnimation(1f, 0f)
                    alphaAnimation.duration = 100
                    isShow = false
                    showedView?.startAnimation(alphaAnimation)
                    showedView?.visibility = View.GONE

//                    (if (klineState == KLINE_SCALE) tv_scale else tv_indicator)?.run {
//                        labelBackgroundColor = ColorUtil.getColor(R.color.normal_icon_color)
//                        textColor = ColorUtil.getColor(R.color.normal_text_color)
//                    }

                } else {
                    isShow = true
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        restart()
        WsAgentManager.instance.removeWsCallback(this)
    }

    /*
     * 获取服务器用户自选数据
     * var req_type = type
     */
    private fun getOptionalSymbol() {
        addDisposable(getMainModel().getOptionalSymbol(MyNDisposableObserver(getUserSelfDataReqType)))
    }

    /**
     * 添加或者删除自选数据
     * @param operationType 标识 0(批量添加)/1(单个添加)/2(单个删除)
     * @param symbol 单个币对名称
     */
    private fun addOrDeleteSymbol(operationType: Int = 0, symbol: String?) {

        if (null == symbol)
            return
        var list = ArrayList<String>()
        list.add(symbol)
        addDisposable(getMainModel().addOrDeleteSymbol(operationType, list, MyNDisposableObserver(addCancelUserSelfDataReqType)))
    }

    val getUserSelfDataReqType = 2 // 服务器用户自选数据
    val addCancelUserSelfDataReqType = 1 // 服务器用户自选数据

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

    /*
     * 获取服务器用户的自选币对数据
     */
    var serverSelfSymbols = ArrayList<String>()
    var sync_status = ""
    private fun showServerSelfSymbols(data: JSONObject?) {

        if (null == data || data.length() <= 0)
            return

        var array = data.optJSONArray("symbols")
        sync_status = data.optString("sync_status")

        if (null == array || array.length() <= 0) {
            return
        }
        for (i in 0 until array.length()) {
            serverSelfSymbols.add(array.optString(i))
        }
        if (serverSelfSymbols.contains(symbol)) {
            ib_collect?.setImageResource(R.drawable.quotes_optional_selected)
        } else {
            ib_collect?.setImageResource(R.drawable.quotes_optional_default)
        }
    }


    /**
     * 每5s调用一次接口
     */
    private fun loopETFData() {
        disposable.add((Observable.interval(0, 5, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver())))
    }

    private fun getObserver(): DisposableObserver<Long> {
        return object : DisposableObserver<Long>() {
            override fun onComplete() {
            }

            override fun onNext(t: Long) {
                getETFData()
            }

            override fun onError(e: Throwable) {
            }
        }

    }

    private fun getETFData() {
        LogUtil.d(TAG, "ETF Value")

        val name = jsonObject?.optString("name")
        val base = NCoinManager.getMarketCoinName(name)
        val quote = NCoinManager.getMarketName(name)

        disposable.add((getMainModel()).getETFValue(base = base, quote = quote, consumer = object : NDisposableObserver() {
            override fun onResponseSuccess(data: JSONObject) {
                //
                etfInfoFragment?.apply {
                    changeETF(data.optJSONObject("data"))
                }
                etfInfoRuleFragment?.apply {
                    changeETF(data.optJSONObject("data"))
                }
            }
        })!!)
    }


    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 兼容 华为 奇怪的生命周期
    }


    override fun onWsMessage(json: String) {
        handleData(json)
    }

    private fun realtData(jsonObj: JSONObject, data: String) {
        val historyList = jsonObj.getString("channel") == WsLinkUtils.getDealHistoryLink(symbol).channel && !jsonObj.isNull("data")
        val depthReal = historyList ||
                jsonObj.getString("channel") == WsLinkUtils.getDealNewLink(symbol).channel
        if (depthReal) {
            if (dealtRecordFragment != null) {
                if (historyList) isRealNew = false
                if (!isRealNew) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)     //延时500ms
                        dealtRecordFragment?.onCallback(data)
                        WsAgentManager.instance.sendData(WsLinkUtils.getDealNewLink(symbol).json)
                        isRealNew = true
                    }
                } else {
                    dealtRecordFragment?.onCallback(data)
                }
            }
        }
    }

    fun rightSettings(z2: Boolean) {
        a(z2, null as Animator.AnimatorListener?)
    }

    var af = false
    var aB = false
    var ah = false
    var ai = false
    var ag = false
    fun a(z2: Boolean, animatorListener: Animator.AnimatorListener?) {
        if (this.aB || this.af) {
            return
        }
        if (z2) {
            if (!this.ah) {
                this.ah = true
//                tv_scale.setSelected(true)
                if (this.ai) {
                    b(false, object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            a(ly_kline_panel, id_kline_index_setting_view, id_kline_index_setting_bg_view, object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animator: Animator) {
                                    this@MarketDetail4Activity.af = false
                                }

                                override fun onAnimationStart(animator: Animator) {
                                    this@MarketDetail4Activity.af = true
                                }
                            } as Animator.AnimatorListener)
                        }
                    })
                } else {
                    a(ly_kline_panel, id_kline_index_setting_view, id_kline_index_setting_bg_view, object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            af = false
                        }

                        override fun onAnimationStart(animator: Animator) {
                            this@MarketDetail4Activity.af = true
                        }
                    } as Animator.AnimatorListener)
                }
            }
        } else if (this.ah) {
            b(ly_kline_panel, id_kline_index_setting_view, id_kline_index_setting_bg_view, object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    af = true
                }

                override fun onAnimationEnd(animator: Animator) {
                    ah = false
                    af = false
                    // tv_scale.setSelected(false)
                    val animatorListener = animatorListener
                    animatorListener?.onAnimationEnd(animator)
                }
            })
        }
    }


    fun a(view: View?, view2: View, view3: View, animatorListener: Animator.AnimatorListener?) {
        if (view != null) {
            val layoutParams: ConstraintLayout.LayoutParams = view.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.topMargin = r()
            view.layoutParams = layoutParams
            val animatorSet = AnimatorSet()
            animatorSet.duration = 270
            animatorSet.interpolator = LinearOutSlowInInterpolator()
            animatorSet.playTogether(*arrayOf<Animator>(ObjectAnimator.ofFloat(view3, View.ALPHA, *floatArrayOf(0.0f, 1.0f)), ObjectAnimator.ofFloat(view2, View.TRANSLATION_Y, *floatArrayOf((-aF).toFloat(), 0.0f))))
            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animator: Animator) {
                    onAnimationEnd(animator)
                }

                override fun onAnimationEnd(animator: Animator) {
                    val animatorListener = animatorListener
                    animatorListener?.onAnimationEnd(animator)
                }

                override fun onAnimationStart(animator: Animator) {
                    ViewUtils.a(view, true)
                    val animatorListener = animatorListener
                    animatorListener?.onAnimationStart(animator)
                }
            })
            animatorSet.start()
        }
    }

    /* access modifiers changed from: protected */
    fun b(z2: Boolean) {
        b(z2, null as Animator.AnimatorListener?)
    }

    /* access modifiers changed from: protected */
    fun b(z2: Boolean, animatorListener: Animator.AnimatorListener?) {
        if (aB || this.ag) {
            return
        }
        if (z2) {
            if (!ai) {
                ai = true
                if (ah) {
                    a(false, object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            a(ly_kline_more_panel, id_kline_period_more_view, id_kline_period_more_bg_view, object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animator: Animator) {
                                    ag = false
                                }

                                override fun onAnimationStart(animator: Animator) {
                                    ag = true
                                }
                            } as Animator.AnimatorListener)
                        }
                    } as Animator.AnimatorListener)
                } else {
                    a(ly_kline_more_panel, id_kline_period_more_view, id_kline_period_more_bg_view, object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animator: Animator) {
                            ag = false
                        }

                        override fun onAnimationStart(animator: Animator) {
                            ag = true
                        }
                    } as Animator.AnimatorListener)
                }
            }
        } else if (ai) {
            b(ly_kline_more_panel, id_kline_period_more_view, id_kline_period_more_bg_view, object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    ag = true
                }

                override fun onAnimationEnd(animator: Animator) {
                    ai = false
                    ag = false
                    ly_kline_more_panel.setVisibility(View.GONE)
                    animatorListener?.onAnimationEnd(animator)
                }
            })
        }
    }

    private fun r(): Int {
        val iArr = IntArray(2)
        this.trade_time_group_layout.getLocationInWindow(iArr)
        return iArr[1] + this.trade_time_group_layout.getHeight() - ViewUtils.a()
    }

    private fun b(view: View, view2: View, view3: View, animatorListener: Animator.AnimatorListener) {
        if (view != null) {
            val animatorSet = AnimatorSet()
            animatorSet.setDuration(240)
            animatorSet.setInterpolator(FastOutLinearInInterpolator())
            val a = ObjectAnimator.ofFloat(view3, View.ALPHA, 1.0f, 0.0f)
            val b = ObjectAnimator.ofFloat(view2, View.TRANSLATION_Y, 0.0f, (-this.aF).toFloat())
            animatorSet.playTogether(a, b)
            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animator: Animator?) {
                    ViewUtils.a(view, false)
                    if (animatorListener != null) {
                        animatorListener.onAnimationEnd(animator)
                    }
                }

                override fun onAnimationCancel(animator: Animator?) {
                    onAnimationEnd(animator)
                }

                override fun onAnimationStart(animator: Animator?) {
                    if (animatorListener != null) {
                        animatorListener.onAnimationStart(animator)
                    }
                }
            })
            animatorSet.start()
        }
    }

    fun klinPanelTouch(view: View?, motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == 0) {
            return resetClick()
        }
        return false
    }

    private fun resetClick(): Boolean {
        if (ah) {
            rightSettings(false)
            return true
        } else if (ai) {
            b(false)
            return true
        }
        return true
    }

    private fun b(view: View) {
        ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.3f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.3f, 1.0f)).setDuration(300).start()
    }

    fun textClick(textView: TextView) {
        textClickTab(textView, null)
    }

    var aG = false
    fun textClickTab(textView: TextView, view: View?) {
        val iArr = IntArray(2)
        textView.getLocationInWindow(iArr)
        if (!this.aG) {
            this.aG = true
            textView.post { changeTabIndicatorIng(textView, iArr) }
            return
        }
        LogUtil.i(TAG, "textClickTab ${iArr[0]}")
        this.kline_tab_indicator.animate().translationX(iArr[0].toFloat())
        val ofInt = ValueAnimator.ofInt(this.kline_tab_indicator.width, textView.width)
        ofInt.addUpdateListener { valueAnimator -> changeTabIndicator(valueAnimator) }
        ofInt.start()

    }

    fun changeTabIndicatorIng(textView: TextView, iArr: IntArray) {
        textView.getLocationInWindow(iArr)
        this.kline_tab_indicator.setTranslationX(iArr[0].toFloat())
        val layoutParams = this.kline_tab_indicator.layoutParams
        layoutParams.width = textView.width
        LogUtil.i(TAG, "changeTabIndicatorIng ${textView.width}")
        this.kline_tab_indicator.layoutParams = layoutParams
    }

    fun changeTabIndicator(valueAnimator: ValueAnimator) {
        val layoutParams = this.kline_tab_indicator.layoutParams
        layoutParams.width = (valueAnimator.animatedValue as Int).toInt()
        LogUtil.i(TAG, "changeTabIndicator ${valueAnimator.animatedValue}")
        this.kline_tab_indicator.layoutParams = layoutParams
    }

    private var pricePrecision = 2

    /**
     * 配置深度图的基本属性
     */
    @SuppressLint("NewApi")
    private fun initDepthChart() {
        depth_chart?.setNoDataText(this.getString(R.string.common_tip_nodata))
        depth_chart?.setNoDataTextColor(resources.getColor(R.color.normal_text_color))
        depth_chart?.setTouchEnabled(true)

        v_buy_tape?.backgroundColor = ColorUtil.getMainColorType()
        v_sell_tape?.backgroundColor = ColorUtil.getMainColorType(isRise = false)
        tv_buy_tape_title?.textColor = ColorUtil.getMainColorType()
        tv_sell_tape_title?.textColor = ColorUtil.getMainColorType(isRise = false)

        /**
         * 图例 的相关设置
         */
        val legend = depth_chart.legend
        legend.isEnabled = false
        /**
         * 是否缩放
         */
        depth_chart?.setScaleEnabled(false)
        /**
         * X,Y同时缩放
         */
        depth_chart?.setPinchZoom(false)

        /**
         * 关闭图表的描述信息
         */
        depth_chart?.description?.isEnabled = false


        // 打开触摸手势
        depth_chart?.setTouchEnabled(true)
        depth_chart.isLongClickable = true
        depth_chart.isNestedScrollingEnabled = false

        /**
         * X
         */
        val xAxis: XAxis = depth_chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setLabelCount(3, true)
        // 不绘制竖直方向的方格线
        xAxis.setDrawGridLines(false)
        xAxis.axisLineWidth = 0.5f
        //x坐标轴不可见
        xAxis.isEnabled = true
        //禁止x轴底部标签
        xAxis.setDrawLabels(true)
        //最小的间隔设置
        xAxis.textColor = ColorUtil.getColor(R.color.normal_text_color)
        xAxis.textSize = 10f
        //在绘制时会避免“剪掉”在x轴上的图表或屏幕边缘的第一个和最后一个坐标轴标签项。
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.setDrawAxisLine(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toString().toTextPrice(pricePrecision)
            }
        }
        /**
         * Y
         */
        depth_chart.axisRight.isEnabled = true
        depth_chart.axisLeft.isEnabled = false
        /**********左边Y轴********/
        depth_chart.axisLeft.axisMinimum = 0f
        /**********右边Y轴********/
        val yAxis = depth_chart.axisRight
        yAxis.setDrawGridLines(false)
        yAxis.setDrawAxisLine(false)
        //设置Y轴的Label显示在图表的内侧还是外侧，默认外侧
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        //不绘制水平方向的方格线
        yAxis.textColor = ColorUtil.getColor(R.color.normal_text_color)
        yAxis.textSize = 10f
        //设置Y轴显示的label个数
        yAxis.setLabelCount(6, true)
        //控制上下左右坐标轴显示的距离
        depth_chart?.setViewPortOffsets(0f, 20f, 0f, DisplayUtil.dip2px(14f))
        yAxis.valueFormatter = DepthYValueFormatter()


        depth_chart?.setOnClickListener {
            if (depth_chart.marker != null) {
                depth_chart.marker = null
            }
        }

        depth_chart?.setOnLongClickListener {
            val mv = DepthMarkView(this, R.layout.layout_depth_marker)
            mv.chartView = depth_chart // For bounds control
            depth_chart?.marker = mv // Set the marker to the ch
            false
        }


    }


    /**
     * 设置lineDataSet  in深度图
     */
    private fun lineDataSet(yData: ArrayList<Entry>, isBuy: Boolean): LineDataSet {
        val lineDataSet: LineDataSet?
        if (isBuy) {
            lineDataSet = LineDataSet(yData, "Buy")
            lineDataSet.color = ColorUtil.getMainColorType()
            lineDataSet.fillColor = ColorUtil.getMainColorType()
            /**
             * 设置折线的颜色
             */
        } else {
            lineDataSet = LineDataSet(yData, "Sell")
            lineDataSet.color = ColorUtil.getMainColorType(isRise = false)
            lineDataSet.fillColor = ColorUtil.getMainColorType(isRise = false)
            /**
             * 设置折线的颜色
             */
        }
        /**
         * 是否填充折线以及填充色设置
         */
        lineDataSet.setDrawFilled(true)
        lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        /**
         * 控制MarkView的显示与隐藏
         * 点击是否显示高亮线
         */
        lineDataSet.isHighlightEnabled = true
        lineDataSet.highLightColor = Color.TRANSPARENT

        /**
         * 设置折线的宽度
         */
        lineDataSet.lineWidth = 1.0f
        /**
         * 隐藏每个数据点的值
         */
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawCircles(false)
        return lineDataSet
    }

    /**
     * 清理深度
     */
    fun clearDepthChart() {
        depth_chart?.clear()
        depth_chart?.notifyDataSetChanged()
        depth_chart?.invalidate()
    }


    fun updateDepth() {
        runOnUiThread {
            setData4DepthChart()
        }
    }

    private var datas: DepthItem? = null

    /**
     * 设置深度图数据
     */
    private fun setData4DepthChart() {

        if (datas == null) {
            clearDepthChart()
            return
        }
        datas = datas?.parseDepth()
        val sellList = ArrayList<DepthBean>()
        val buyList = ArrayList<DepthBean>()

        sellList.addAll(datas?.sellItem!! as ArrayList<DepthBean>)
        buyList.addAll(datas?.buyItem!! as ArrayList<DepthBean>)

        val yData = arrayListOf<Entry>()
        var buyVolumeSum = 0.0
        // TODO 优化
        for (i in buyList.indices) {
            buyVolumeSum = BigDecimalUtils.add(buyVolumeSum.toString(), buyList[i].vol).toDouble()
            val entry = Entry((buyList[i].price).toFloat(), buyList[i].sum.toFloat(), buyList[i].price.toTextPrice(pricePrecision))
            yData.add(0, entry)
        }
        /*************处理卖盘数据*********/
        var sellVolumeSum = 0.0
        val sellYData = ArrayList<Entry>()
        for (i in sellList.indices) {
            sellVolumeSum = BigDecimalUtils.add(sellVolumeSum.toString(), sellList[i].vol).toDouble()
            val entry = Entry(sellList[i].price.toFloat(), sellList[i].sum.toFloat(), sellList[i].price.toTextPrice(pricePrecision))
            sellYData.add(entry)
        }

        /**
         * Y 轴最大值 和 最小值
         */
        val maxVolume = max(buyList.get(buyList.size - 1).sum.toFloat(), sellList.get(sellList.size - 1).sum.toFloat())
        depth_chart ?: return
        var xAxis = depth_chart.xAxis
        xAxis.axisMinimum = buyList.get(buyList.size - 1).price.toFloat()
        xAxis.axisMaximum = sellList.get(sellList.size - 1).price.toFloat()
        var yAxis = depth_chart.axisRight
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = maxVolume * 1.1f

        val buyLineDataSet = lineDataSet(yData, true)
        val sellLineDataSet = lineDataSet(sellYData, false)

        val lineData = LineData(buyLineDataSet, sellLineDataSet)
        depth_chart.data = lineData

        depth_chart.invalidate()

    }

    private fun initTimerDepthView() {
        if (symbol.isEmpty()) {
            return
        }
        restart()
        subscribe = Observable.interval(0L, CommonConstant.depthLoopTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d(TAG, "getCoinSymbol==data is ")
                    getCoinSymbol()
                }
    }

    /**
     * 获取币种简介
     */
    private fun getCoinSymbol() {
        depthView().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d(TAG, "getCoinSymbol==data ${it}")
                    datas = it
                    updateDepth()
                }, {
                    it.printStackTrace()
                })
    }

    /**
     * 结束计时,重新开始
     */
    fun restart() {
        if (subscribe != null) {
            subscribe?.dispose()//取消订阅
            Log.e("LogUtils", "dispose DepthFragment time")
        }
    }

    private fun depthView(): Observable<DepthItem> {
        var retryCount = 0
        return HttpClient.instance.getCoinDepth(symbol, "").map {
            it.data
        }.retryWhen { throwableObservable ->
            return@retryWhen throwableObservable.flatMap {
                if (retryCount == 3) {
                    return@flatMap Observable.error<Throwable>(IllegalArgumentException(""))
                } else {
                    retryCount++
                    return@flatMap Observable.timer(2000, TimeUnit.MILLISECONDS)
                }
            }
        }.compose(RxUtil.applySchedulersToObservable())
    }

    private fun changeTab(position: Int) {
        resetCheck()
        rv_kline_scale_default?.apply {
            val count = (childCount - 1)

            for (index in 0..count) {
                val item = getChildAt(index)

                if (index != count) {
                    val textView = item?.findViewById<TextView>(R.id.tv_item)
                    textView?.apply {
                        if (position == index) {
                            textColor = ColorUtil.getColorByMode(R.color.tab_sele_text_color_day, true)
                            textClick(this)
                        } else {
                            textColor = ColorUtil.getColorByMode(R.color.tab_nor_text_color_day, true)
                        }
                    }
                } else {
                    val textView = item?.findViewById<LabelTextView>(R.id.tv_scale)
                    textView?.apply {
                        if (position == index) {
                            textClick(this)
                            text = curTime?.getArraysSymbols(context)
                            textColor = ColorUtil.getColorByMode(R.color.tab_sele_text_color_day, true)
                        } else {
                            text = LanguageUtil.getString(context, "more")
                            textColor = ColorUtil.getColorByMode(R.color.tab_nor_text_color_day, true)
                        }
                    }
                }
            }
        }
    }

    fun resetCheck() {
        calibrationAdapter?.notifyDataSetChanged()
    }

    var isblack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeTheme(true)
        setOnclick()
    }


    fun changeTheme(iskline: Boolean) {
        if (PublicInfoDataService.getInstance().klineThemeMode == 1) {
            isblack = true
            StatusBarUtil.setDarkMode(this)
            StatusBarUtil.setColor(this, ColorUtil.getColorByMode(R.color.bg_card_color_kline_night, true))
            window.getDecorView().setBackgroundColor(ColorUtil.getColor(R.color.bg_card_color_kline_night))
            window.navigationBarColor = ColorUtil.getColorByMode(R.color.bg_card_color_kline_night, true)
        }
        binding = DataBindingUtil.setContentView(
                this, R.layout.activity_market_detail4)
        binding.isblack = isblack
    }

    lateinit var binding: ActivityMarketDetail4Binding


}
