package com.yjkj.chainup.new_contract.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.request.RequestOptions
import com.contract.sdk.ContractSDKAgent
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.QuotesData
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.db.constant.HomeTabMap
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.LikeDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_contract.fragment.ClContractCoinSearchDialog
import com.yjkj.chainup.new_contract.fragment.ClDealtRecordFragment
import com.yjkj.chainup.new_contract.fragment.ClDepthFragment
import com.yjkj.chainup.new_version.adapter.KLineScaleAdapter
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.bean.FlagBean
import com.yjkj.chainup.new_version.dialog.DialogUtil
import com.yjkj.chainup.new_version.fragment.CoinMapIntroFragment
import com.yjkj.chainup.new_version.kline.bean.KLineBean
import com.yjkj.chainup.new_version.kline.data.DataManager
import com.yjkj.chainup.new_version.kline.data.KLineChartAdapter
import com.yjkj.chainup.new_version.kline.view.MainKlineViewStatus
import com.yjkj.chainup.new_version.kline.view.vice.ViceViewStatus
import com.yjkj.chainup.new_version.view.CustomCheckBoxView
import com.yjkj.chainup.new_version.view.LabelRadioButton
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.CoinSearchDialogFg
import com.yjkj.chainup.ws.WsContractAgentManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_cl_market_detail4.*
import kotlinx.android.synthetic.main.activity_cl_market_detail4.btn_buy
import kotlinx.android.synthetic.main.activity_cl_market_detail4.btn_sell
import kotlinx.android.synthetic.main.activity_cl_market_detail4.ctv_content
import kotlinx.android.synthetic.main.activity_cl_market_detail4.ib_back
import kotlinx.android.synthetic.main.activity_cl_market_detail4.ib_collect
import kotlinx.android.synthetic.main.activity_cl_market_detail4.ib_share
import kotlinx.android.synthetic.main.activity_cl_market_detail4.iv_logo
import kotlinx.android.synthetic.main.activity_cl_market_detail4.ll_coin_map
import kotlinx.android.synthetic.main.activity_cl_market_detail4.ly_kline_panel
import kotlinx.android.synthetic.main.activity_cl_market_detail4.stl_depth_dealt
import kotlinx.android.synthetic.main.activity_cl_market_detail4.tv_24h_vol
import kotlinx.android.synthetic.main.activity_cl_market_detail4.tv_close_price
import kotlinx.android.synthetic.main.activity_cl_market_detail4.tv_coin_map
import kotlinx.android.synthetic.main.activity_cl_market_detail4.tv_common_text_dayVolume
import kotlinx.android.synthetic.main.activity_cl_market_detail4.tv_converted_close_price
import kotlinx.android.synthetic.main.activity_cl_market_detail4.tv_high
import kotlinx.android.synthetic.main.activity_cl_market_detail4.tv_high_price
import kotlinx.android.synthetic.main.activity_cl_market_detail4.tv_indicator
import kotlinx.android.synthetic.main.activity_cl_market_detail4.tv_rose
import kotlinx.android.synthetic.main.activity_cl_market_detail4.v_kline
import kotlinx.android.synthetic.main.activity_cl_market_detail4.vp_depth_dealt
import kotlinx.android.synthetic.main.activity_market_detail4.*
import kotlinx.android.synthetic.main.market_info_kline_panel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 * @description : 币对行情的详细界面
 * @date 2019-3-2
 * @author Bertking
 *
 */
class ClMarketDetail4Activity : NBaseActivity(), WsContractAgentManager.WsResultCallback {
    override fun setContentView(): Int = R.layout.activity_cl_market_detail4
    var subscribe: Disposable? = null//保存订阅者
    var isFrist = true
    var disposable: CompositeDisposable = CompositeDisposable()

    @JvmField
    @Autowired(name = "baseSymbol")
    var baseSymbol = ""

    @JvmField
    @Autowired(name = "quoteSymbol")
    var quoteSymbol = ""

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

    var contractId = -1

    var tv24hVolUnit = ""
    var mMultiplierCoin = ""
    var mPricePrecision = 0
    var mMultiplierPrecision = 0
    var mMultiplier = "0"
    var coUnit = 0

    private var titles: ArrayList<String> = arrayListOf()
    private val fragments = arrayListOf<Fragment>()

    var klineData: ArrayList<KLineBean> = arrayListOf()
    private val adapter by lazy { KLineChartAdapter() }

    /**
     * 主图指标的子view
     */
    private val mainViewStatusViews: ArrayList<RadioButton?> by lazy(LazyThreadSafetyMode.NONE) {
        arrayListOf<RadioButton?>(rb_ma, rb_boll, rb_hide_main)
    }

    /**
     * 副图指标的子view
     */
    private val viceViewStatusViews: ArrayList<RadioButton?> by lazy(LazyThreadSafetyMode.NONE) {
        arrayListOf<RadioButton?>(rb_macd, rb_kdj, rb_wr, rb_rsi, rb_hide_vice)
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

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        WsContractAgentManager.instance.addWsCallback(this)
//        initSocket()
        showLoadingDialog()
        ARouter.getInstance().inject(this)
        setOnclick()
        setTextConetnt()
    }

    fun setTextConetnt() {
        tv_contract_text_upsdowns?.text = LanguageUtil.getString(this, "contract_text_upsdowns")
        tv_clean_title?.text = LanguageUtil.getString(this, "etf_text_networth")
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
        WsContractAgentManager.instance.changeKlineKey(this.javaClass.simpleName)
        cur_time_index = KLineUtil.getCurTime4Index()

        curTime = KLineUtil.getCurTime()
        v_kline?.setMainDrawLine(KLineUtil.getCurTime4Index() == 0)
        tv_scale?.text = curTime
        isFrist = true
        klineData.clear()

        if (!hasInit) {
            initView()
            setDepthSymbol()
            hasInit = true
        }




        initSocket()

        getMarkertInfo()

    }

    override fun onPause() {
        super.onPause()
        WsContractAgentManager.instance.unbind(this, true)
        lastTick = null
        sendMsg(WsLinkUtils.getKlineNewLink(symbol, curTime, false).json)
        loopStop()
    }

    private fun setOnclick() {
        btn_buy?.setOnClickListener {
            var messageEvent = MessageEvent(MessageEvent.contract_switch_type)
            EventBusUtil.post(messageEvent)
            finish()
        }

        btn_sell?.setOnClickListener {
            var messageEvent = MessageEvent(MessageEvent.contract_switch_type)
            EventBusUtil.post(messageEvent)
            finish()
        }

        /**
         * 横屏KLine
         */
        tv_landscape?.setOnClickListener {

            val mIntent = Intent(mActivity, ClHorizonMarketDetailActivity::class.java)
            mIntent.putExtra("curTime", curTime)
            mIntent.putExtra("symbolHorizon", symbol)
            mIntent.putExtra("contractId", contractId)
            mActivity.startActivity(mIntent)
        }

        ib_back?.setOnClickListener {
            finish()
        }

        ib_share?.setOnClickListener {
            DialogUtil.showKLineShareDialog(context = mActivity)
        }

        /**
         * 切换币对
         */
        ll_coin_map?.setOnClickListener {
//            if (type == ParamConstant.LEVER_INDEX) {
//                showLeftCoinPopu(TradeTypeEnum.LEVER_TRADE.value)
//            } else {
//                showLeftCoinPopu(TradeTypeEnum.COIN_TRADE.value)
//            }
            if (Utils.isFastClick())
                return@setOnClickListener

            var mContractCoinSearchDialog = ClContractCoinSearchDialog()
            var bundle = Bundle()
            bundle.putString("contractList", LogicContractSetting.getContractJsonListStr(mActivity))
            mContractCoinSearchDialog.arguments = bundle
            mContractCoinSearchDialog.showDialog(supportFragmentManager, "SlContractFragment")
        }

        /**
         * KLine刻度
         */
        tv_scale?.setOnClickListener {
            if (isShow) {
                showedView = rv_kline_scale
                klineState = KLINE_SCALE
            }
            isShow = !isShow
            rv_kline_scale?.visibility = if (isShow) View.GONE else View.VISIBLE
            tv_scale?.run {
                labelBackgroundColor = ColorUtil.getColor(if (isShow) R.color.normal_icon_color else R.color.main_blue)
                textColor = ColorUtil.getColor(if (isShow) R.color.normal_text_color else R.color.text_color)
            }
        }

        /**
         * KLine指标
         */
        tv_indicator?.setOnClickListener {
            if (isShow) {
                showedView = ly_kline_panel
                klineState = KLINE_INDEX
            }
            isShow = !isShow
            ly_kline_panel?.visibility = if (isShow) View.GONE else View.VISIBLE

            tv_indicator?.run {
                labelBackgroundColor = ColorUtil.getColor(if (isShow) R.color.normal_icon_color else R.color.main_blue)
                textColor = ColorUtil.getColor(if (isShow) R.color.normal_text_color else R.color.text_color)
            }
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
    private var themeMode = PublicInfoDataService.THEME_MODE_DAYTIME
    private var kLineLogo = ""
    private fun initKLineData() {
        main_index = KLineUtil.getMainIndex()
        vice_index = KLineUtil.getViceIndex()
        cur_time_index = KLineUtil.getCurTime4Index()

        curTime = KLineUtil.getCurTime()
        klineScale = KLineUtil.getKLineScale()

        themeMode = PublicInfoDataService.getInstance().themeMode
        kLineLogo = PublicInfoDataService.getInstance().getKline_background_logo_img(null, themeMode == PublicInfoDataService.THEME_MODE_DAYTIME)

    }


    override fun initView() {
        contractId = intent.getIntExtra("contractId", -1)
        if (type == ParamConstant.LEVER_INDEX) {
            btn_buy?.text = "${LanguageUtil.getString(this, "contract_action_buy")}/${LanguageUtil.getString(this, "contract_action_long")}"
            btn_sell?.text = "${LanguageUtil.getString(this, "contract_action_sell")}/${LanguageUtil.getString(this, "contract_action_short")}"
        } else {
            btn_buy?.text = LanguageUtil.getString(this, "contract_action_buy")
            btn_sell?.text = LanguageUtil.getString(this, "contract_action_sell")
        }

        mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(this, contractId)

        mMultiplierCoin = LogicContractSetting.getContractMultiplierCoinPrecisionById(this, contractId)

        mMultiplierPrecision = LogicContractSetting.getContractMultiplierPrecisionById(this, contractId)

        coUnit = LogicContractSetting.getContractUint(ContractSDKAgent.context)

        mMultiplier = LogicContractSetting.getContractMultiplierById(this, contractId)

        tv24hVolUnit = if (LogicContractSetting.getContractUint(this) == 0) " " + getString(R.string.sl_str_contracts_unit) else " " + mMultiplierCoin

        initDepthAndDeals()

        v_kline?.adapter = adapter
        v_kline?.startAnimation()
        v_kline?.justShowLoading()
        v_kline?.setPricePrecision(mPricePrecision)
        NLiveDataUtil.observeForeverData {
            if (null != it && MessageEvent.symbol_switch_type == it.msg_type) {
                if (null != it.msg_content) {
                    symbol = it.msg_content as String
                    setTagView(NCoinManager.getNameForSymbol(symbol))
                    isFrist = true
                    klineData.clear()
                    getSymbol(symbol)
                    showCoinName()
                }
            }
        }

        showCoinName()

        collectCoin()

        initViewColor()

        initKLineScale()

        action4KLineIndex()
        GlideUtils.load(this, kLineLogo, iv_logo, RequestOptions())
        setTagView(NCoinManager.getNameForSymbol(symbol))

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
        if (contractId != -1) {
            val mContractJson = LogicContractSetting.getContractJsonStrById(mActivity, contractId)
            tv_coin_map?.text = LogicContractSetting.getContractShowNameById(mActivity, contractId)
        }
    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (event.msg_type == MessageEvent.market_switch_curTime) {
            curTime = event.msg_content as String
            Log.e("shengong", "curTime2:$curTime")
            switchKLineScale(curTime ?: "15min")
            tv_scale?.text = curTime ?: "15min"
            calibrationAdapter?.notifyDataSetChanged()
        }

        if (event.msg_type == MessageEvent.sl_contract_change_tagPrice_event) {
            val obj = event.msg_content as JSONObject
            tv_mark_price.setText(obj.optString("tagPrice"))
            tv_index_price.setText(obj.optString("indexPrice"))
        }
        if (event.msg_type == MessageEvent.sl_contract_rate_countdown_event) {
            val obj = event.msg_content as String
            tv_time.setText(obj)
        }
        if (event.msg_type == MessageEvent.sl_contract_left_coin_type) {
            val ticker = event.msg_content as JSONObject
            contractId = ticker.getInt("id")
            symbol = (ticker.getString("contractType") + "_" + ticker.getString("symbol").replace("-", "")).toLowerCase()
            mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(this, contractId)

            mMultiplierCoin = LogicContractSetting.getContractMultiplierCoinPrecisionById(this, contractId)

            mMultiplierPrecision = LogicContractSetting.getContractMultiplierPrecisionById(this, contractId)

            coUnit = LogicContractSetting.getContractUint(ContractSDKAgent.context)

            mMultiplier = LogicContractSetting.getContractMultiplierById(this, contractId)

            tv24hVolUnit = if (LogicContractSetting.getContractUint(this) == 0) " " + getString(R.string.sl_str_contracts_unit) else " " + mMultiplierCoin

            v_kline?.setPricePrecision(mPricePrecision)

            setTagView(NCoinManager.getNameForSymbol(symbol))
            isFrist = true
            klineData.clear()
            getSymbol(symbol)
            showCoinName()

//            symbol = event.msg_content as String
//            setTagView(NCoinManager.getNameForSymbol(symbol))
//            isFrist = true
//            klineData.clear()
//            getSymbol(symbol)
//            showCoinName()
        }

    }

    private fun hometab_switch(buyOrSell: Int) {

        var messageEvent2 = MessageEvent(MessageEvent.symbol_switch_type)
        messageEvent2.msg_content = symbol
        messageEvent2.isLever = type == ParamConstant.LEVER_INDEX
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
            ll_clean_tag?.visibility = View.VISIBLE
            loopETFData()
            tv_fund_rate?.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(PublicInfoDataService.getInstance().getfundRate(null))) {
                tv_fund_rate?.text = LanguageUtil.getString(this, "sl_str_funds_rate") + "${PublicInfoDataService.getInstance().getfundRate(null)}%"
            } else {
                tv_fund_rate?.text = "${LanguageUtil.getString(this, "sl_str_funds_rate")} --"
            }

        } else {
            ll_clean_tag?.visibility = View.GONE
            tv_fund_rate?.visibility = View.GONE
            disposable?.clear()
        }

//        var symbol = jsonObject?.optString("symbol")
        var price = jsonObject?.optInt("price")
        var volume = jsonObject?.optInt("volume")

//        if (null == symbol || null == price || null == volume || null == coinName || null == marketName)
//            return

        ClDepthFragment.liveData.value = FlagBean(isContract = true,
                contractId = contractId.toString(),
                symbol = symbol,
                baseSymbol = baseSymbol!!,
                quotesSymbol = quoteSymbol!!,
                pricePrecision = mPricePrecision,
                volumePrecision = if (coUnit == 0) 0 else mMultiplierPrecision,
                mMultiplier = mMultiplier,
                coUnit = coUnit
        )
    }

    private fun chooseCoinAfterAddDetailsFg() {
        initChooseCoinDetailsFg()
        if (isSymbolProfile && isCoinIntroduce) {
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
                NToastUtil.showTopToastNet(mActivity,true, LanguageUtil.getString(this, "kline_tip_addCollectionSuccess"))
            }
            if (isAddRemove) {
//                LikeDataService.getInstance().saveCollecData(symbol)
            }
        } else {
            ib_collect?.setImageResource(R.drawable.quotes_optional_default)
            if (isShowToast) {
                NToastUtil.showTopToastNet(mActivity,true, LanguageUtil.getString(this, "kline_tip_removeCollectionSuccess"))
            }
            if (isAddRemove) {
//                LikeDataService.getInstance().removeCollect(symbol)
            }
        }
    }

    /**
     * K线的指标处理
     */
    private fun action4KLineIndex() {

        when (main_index) {
            MainKlineViewStatus.MA.status -> {
                rb_ma?.isLabelEnable = true
                rb_hide_main?.isChecked = false
            }

            MainKlineViewStatus.BOLL.status -> {
                rb_boll?.isLabelEnable = true
                rb_hide_main?.isChecked = false
            }

            MainKlineViewStatus.NONE.status -> {
                rb_hide_main?.isChecked = true
            }
        }

        when (vice_index) {
            ViceViewStatus.MACD.status -> {
                rb_macd?.isLabelEnable = true
                rb_hide_vice?.isChecked = false
            }

            ViceViewStatus.KDJ.status -> {
                rb_kdj?.isLabelEnable = true
                rb_hide_vice?.isChecked = false
            }

            ViceViewStatus.RSI.status -> {
                rb_rsi?.isLabelEnable = true
                rb_hide_vice?.isChecked = false
            }

            ViceViewStatus.WR.status -> {
                rb_wr?.isLabelEnable = true
                rb_hide_vice?.isChecked = false
            }

            ViceViewStatus.NONE.status -> {
                rb_hide_vice?.isChecked = true
            }
        }

        mainViewStatusViews.forEach {
            it?.setOnClickListener {
                val index = mainViewStatusViews.indexOf(it)
                (mainViewStatusViews[0] as LabelRadioButton?)?.isLabelEnable = (index == 0)
                (mainViewStatusViews[1] as LabelRadioButton?)?.isLabelEnable = (index == 1)
                mainViewStatusViews[2]?.isChecked = (index == 2)
                when (index) {
                    MainKlineViewStatus.MA.status -> {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
                        KLineUtil.setMainIndex(MainKlineViewStatus.MA.status)
                    }

                    MainKlineViewStatus.BOLL.status -> {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
                        KLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status)
                    }

                    MainKlineViewStatus.NONE.status -> {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.NONE)
                        KLineUtil.setMainIndex(MainKlineViewStatus.NONE.status)
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
                (viceViewStatusViews[0] as LabelRadioButton?)?.isLabelEnable = (index == 0)
                (viceViewStatusViews[1] as LabelRadioButton?)?.isLabelEnable = (index == 1)
                (viceViewStatusViews[2] as LabelRadioButton?)?.isLabelEnable = (index == 2)
                (viceViewStatusViews[3] as LabelRadioButton?)?.isLabelEnable = (index == 3)
                viceViewStatusViews[4]?.isChecked = (index == 4)

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
                        v_kline?.setChildDraw(2)
                        KLineUtil.setViceIndex(ViceViewStatus.WR.status)
                    }

                    ViceViewStatus.RSI.status -> {
                        v_kline?.setChildDraw(3)
                        KLineUtil.setViceIndex(ViceViewStatus.RSI.status)
                    }
                    else -> {
                        v_kline?.hideChildDraw()
                        KLineUtil.setViceIndex(ViceViewStatus.NONE.status)
                    }
                }

            }
        }
    }

    //初次 和 切换币对时触发
    private fun initSocket() {
        if (isNotEmpty(symbol)) {
            // sub ticker
            val scale: String = if (curTime == "line") "1min" else curTime ?: "15min"
            WsContractAgentManager.instance.sendMessage(hashMapOf("symbol" to symbol, "line" to scale), this)
        }
    }

    private fun initViewColor() {
        tv_converted_close_price.visibility = View.GONE
        var mainColorType = ColorUtil.getMainColorType()
        btn_buy?.backgroundColor = mainColorType
        btn_sell?.backgroundColor = ColorUtil.getMainColorType(isRise = false)

        tv_close_price?.textColor = mainColorType
        tv_rose?.textColor = mainColorType
    }

    var mClDepthFragment: ClDepthFragment? = null
    var dealtRecordFragment: ClDealtRecordFragment? = null
    var coinMapIntroFragment: CoinMapIntroFragment? = null
    var pageAdapter: NVPagerAdapter? = null

    private fun initDepthAndDeals() {

        if (titles.size > 0) {
            titles.clear()
        }

        if (fragments.size > 0) {
            fragments.clear()
        }
        titles.add(LanguageUtil.getString(this, "kline_action_depth"))
        titles.add(LanguageUtil.getString(this, "kline_action_dealHistory"))

        mClDepthFragment = ClDepthFragment.newInstance(viewPager = vp_depth_dealt)
        dealtRecordFragment = ClDealtRecordFragment.newInstance(viewPager = vp_depth_dealt)
        coinMapIntroFragment = CoinMapIntroFragment.newInstance(viewPager = vp_depth_dealt, symbol = symbol)

        fragments.add(mClDepthFragment ?: return)
        fragments.add(dealtRecordFragment ?: return)

        if (isSymbolProfile && isCoinIntroduce) {
            titles.add(LanguageUtil.getString(this, "market_text_coinInfo"))
            fragments.add(coinMapIntroFragment ?: return)
        }

        pageAdapter = NVPagerAdapter(supportFragmentManager, titles, fragments)

        vp_depth_dealt?.adapter = pageAdapter
        vp_depth_dealt.offscreenPageLimit = fragments.size
        vp_depth_dealt?.setScrollable(false)
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

    var calibrationAdapter: KLineScaleAdapter? = null

    /**
     * 处理K线刻度
     */
    private fun initKLineScale() {
        rv_kline_scale?.isLayoutFrozen = true
        rv_kline_scale?.setHasFixedSize(true)
        tv_scale?.text = if (cur_time_index == 0) "line" else curTime

        val layoutManager = GridLayoutManager(mActivity, 4)
        layoutManager.isAutoMeasureEnabled = false
        rv_kline_scale?.layoutManager = layoutManager
        calibrationAdapter = KLineScaleAdapter(klineScale)
        rv_kline_scale?.adapter = calibrationAdapter
        /**
         * 分时线
         */
        v_kline?.setMainDrawLine(cur_time_index == 0)

        calibrationAdapter?.setOnItemClickListener { viewHolder, view, position ->
            /**
             * 分时线
             */
            v_kline?.setMainDrawLine(position == 0)
            if (position != KLineUtil.getCurTime4Index()) {
                for (i in 0 until klineScale.size) {
                    val boxView = viewHolder?.getViewByPosition(i, R.id.cbtn_view) as CustomCheckBoxView
                    boxView.setCenterColor(ColorUtil.getColor(R.color.normal_text_color))
                    boxView.setCenterSize(12f)
                    boxView.setIsNeedDraw(false)
                    boxView.isChecked = false
                }
                val boxView = viewHolder?.getViewByPosition(position, R.id.cbtn_view) as CustomCheckBoxView
                boxView.isChecked = true
                boxView.setIsNeedDraw(true)
                boxView.setCenterSize(12f)
                boxView.setCenterColor(ColorUtil.getColor(R.color.text_color))
                KLineUtil.setCurTime4KLine(position)
                KLineUtil.setCurTime(klineScale[position])
                switchKLineScale(klineScale[position])

                tv_scale?.text = if (position == 0) "line" else curTime

            } else {
                val boxView = viewHolder?.getViewByPosition(position, R.id.cbtn_view) as CustomCheckBoxView
                boxView.isChecked = true
                boxView.setIsNeedDraw(true)
                boxView.setCenterSize(12f)
                boxView.setCenterColor(ColorUtil.getColor(R.color.text_color))
                KLineUtil.setCurTime4KLine(position)
                KLineUtil.setCurTime(klineScale[position])
                switchKLineScale(klineScale[position])
            }
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
//                        kLineEntity.openPrice = BigDecimal(data.optString("open")).toFloat()
//                        kLineEntity.closePrice = BigDecimal(data.optString("close")).toFloat()
//                        kLineEntity.highPrice = BigDecimal(data.optString("high")).toFloat()
//                        kLineEntity.lowPrice = BigDecimal(data.optString("low")).toFloat()
                        val open = if (coUnit == 0) data.optString("open") else BigDecimalUtils.showSNormal(data.optString("open"), mPricePrecision)
                        val close = if (coUnit == 0) data.optString("close") else BigDecimalUtils.showSNormal(data.optString("close"), mPricePrecision)
                        val high = if (coUnit == 0) data.optString("high") else BigDecimalUtils.showSNormal(data.optString("high"),mPricePrecision)
                        val low = if (coUnit == 0) data.optString("low") else BigDecimalUtils.showSNormal(data.optString("low"),mPricePrecision)
                        val vol = if (coUnit == 0) data.optString("vol") else BigDecimalUtils.mulStr(data.optString("vol"), mMultiplier, mMultiplierPrecision)

                        kLineEntity.openPrice = BigDecimal(open).toFloat()
                        kLineEntity.closePrice = BigDecimal(close).toFloat()
                        kLineEntity.highPrice = BigDecimal(high).toFloat()
                        kLineEntity.lowPrice = BigDecimal(low).toFloat()
                        kLineEntity.volume = BigDecimal(vol).toFloat()

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
                    if (mClDepthFragment != null) {
                        mClDepthFragment?.onClDepthFragment(data)
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
        val price = 3

        ClDepthFragment.liveData4closePrice.postValue(arrayListOf(tick.close, price.toString(), symbol))

        runOnUiThread {
            tv_close_price?.run {
                textColor = ColorUtil.getMainColorType(tick.rose >= 0)
                text = DecimalUtil.cutValueByPrecision(tick.close, mPricePrecision)
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
            val amount = if (coUnit == 0) tick.vol else BigDecimalUtils.mulStr(tick.vol, mMultiplier, mMultiplierPrecision)
            tv_24h_vol?.text = BigDecimalUtils.showDepthVolume(amount) + tv24hVolUnit
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
            val typeNew = object : TypeToken<KLineBean>() {
            }.type
            val gson = GsonBuilder().setPrettyPrinting().create()
            if (isFrist) {
                klineData.clear()
                var objKline = json.getJSONArray("data")
                for (i in 0..(objKline.length() - 1)) {
                    var obj: JSONObject = objKline.get(i) as JSONObject
                    val mKLineBean: KLineBean = gson.fromJson(obj.toString(), typeNew)
                    if (coUnit != 0) {
                        mKLineBean.volume = BigDecimalUtils.mulStr(mKLineBean.volume.toString(), mMultiplier, mMultiplierPrecision).toFloat()
                        mKLineBean.openPrice = BigDecimalUtils.showSNormal(mKLineBean.openPrice.toString(),mPricePrecision).toFloat()
                        mKLineBean.closePrice = BigDecimalUtils.showSNormal(mKLineBean.closePrice.toString(), mPricePrecision).toFloat()
                        mKLineBean.highPrice = BigDecimalUtils.showSNormal(mKLineBean.highPrice.toString(), mPricePrecision).toFloat()
                        mKLineBean.lowPrice = BigDecimalUtils.showSNormal(mKLineBean.lowPrice.toString(),mPricePrecision).toFloat()
                    }
                    klineData.add(mKLineBean)
                }
//                val list: ArrayList<KLineBean> = gson.fromJson(json.getJSONArray("data").toString(), type)
//                klineData.addAll(list)
                if (klineData.size == 0) {
                    initKlineData()
                } else {
                    val scale = if (curTime == "line") "1min" else curTime
                    WsContractAgentManager.instance.sendData(WsLinkUtils.getKlineHistoryOther(symbol, scale, klineData[0].id.toString()))
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
            WsContractAgentManager.instance.sendData(WsLinkUtils.getKlineNewLink(symbol, scale))
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
            showedView?.getGlobalVisibleRect(viewRect)

            if (!viewRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                if (showedView?.visibility == View.VISIBLE) {
                    val alphaAnimation = AlphaAnimation(1f, 0f)
                    alphaAnimation.duration = 100
                    isShow = false
                    showedView?.startAnimation(alphaAnimation)
                    showedView?.visibility = View.GONE

                    (if (klineState == KLINE_SCALE) tv_scale else tv_indicator)?.run {
                        labelBackgroundColor = ColorUtil.getColor(R.color.normal_icon_color)
                        textColor = ColorUtil.getColor(R.color.normal_text_color)
                    }

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
        WsContractAgentManager.instance.removeWsCallback(this)
        loopStop()
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
                val price = data.optJSONObject("data")?.optString("price") ?: "--"
                tv_clean_value?.text = price
            }
        })!!)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        // 兼容 华为 奇怪的生命周期
    }


    override fun onWsMessage(json: String) {
        handleData(json)
    }

    private fun realtData(jsonObj: JSONObject, data: String) {
        val depthReal = jsonObj.getString("channel") == WsLinkUtils.getDealHistoryLink(symbol).channel ||
                jsonObj.getString("channel") == WsLinkUtils.getDealNewLink(symbol).channel
        if (depthReal) {
            if (dealtRecordFragment != null) {
                if (!isRealNew) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)     //延时500ms
                        dealtRecordFragment?.onCallback(data)
                        WsContractAgentManager.instance.sendData(WsLinkUtils.getDealNewLink(symbol).json)
                        isRealNew = true
                    }
                } else {
                    dealtRecordFragment?.onCallback(data)
                }
            }
        }
    }

    private fun getMarkertInfo() {
        if (contractId==-1){
            return
        }
        loopStop()
        subscribe = Observable.interval(0L, CommonConstant.capitalRateLoopTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    addDisposable(getContractModel().getMarkertInfo(symbol, contractId.toString(),
                            consumer = object : NDisposableObserver() {
                                override fun onResponseSuccess(jsonObject: JSONObject) {
                                    jsonObject.optJSONObject("data").run {
                                        tv_capital_rate?.apply {
                                            var tagPrice = optString("tagPrice")
                                            var fundRate = optString("currentFundRate")
                                            var indexPrice = optString("indexPrice")
                                            LogUtil.e("标记价格", tagPrice)
                                            LogUtil.e("资金费率", fundRate)
                                            LogUtil.e("指数价格", indexPrice)
                                            var obj = JSONObject()
                                            obj.put("tagPrice", BigDecimalUtils.scaleStr(tagPrice, mPricePrecision))
                                            obj.put("indexPrice", BigDecimalUtils.scaleStr(indexPrice, mPricePrecision))
                                            obj.put("fundRate", "--")
                                            val msgEvent = MessageEvent(MessageEvent.sl_contract_change_tagPrice_event)
                                            msgEvent.msg_content = obj
                                            EventBusUtil.post(msgEvent)
                                        }
                                    }
                                }
                            }))
                }
    }
    private fun loopStop() {
        if (subscribe != null) {
            subscribe?.dispose()
        }
    }
}
