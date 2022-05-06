package com.yjkj.chainup.new_version.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.impl.ContractTickerListener
import com.gcssloop.widget.PagerGridLayoutManager
import com.gcssloop.widget.PagerGridSnapHelper
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.activity.SlContractKlineActivity
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.db.constant.*
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.db.service.v5.CommonService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.*
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.NewMainActivity
import com.yjkj.chainup.new_version.activity.personalCenter.MailActivity
import com.yjkj.chainup.new_version.activity.personalCenter.NoticeActivity
import com.yjkj.chainup.new_version.adapter.NVPagerAdapter
import com.yjkj.chainup.new_version.adapter.NewHomePageContractAdapter
import com.yjkj.chainup.new_version.adapter.NewHomePageServiceAdapter
import com.yjkj.chainup.new_version.adapter.NewhomepageTradeListAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.home.adapter.ImageNetAdapter
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.VerticalTextview4ChainUp
import com.yjkj.chainup.ws.WsAgentManager
import com.yjkj.chainup.ws.WsContractAgentManager
import com.youth.banner.config.IndicatorConfig
import com.youth.banner.indicator.RectangleIndicator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_home_title.*
import kotlinx.android.synthetic.main.fragment_new_version_homepage.*
import kotlinx.android.synthetic.main.fragment_new_version_homepage.swipe_refresh
import kotlinx.android.synthetic.main.no_network_remind.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * @Author lianshangljl
 * @Date 2019/5/5-2:54 PM
 * @Email buptjinlong@163.com
 * @description 首页
 */
class NewVersionHomepageFragment : NBaseFragment(), WsAgentManager.WsResultCallback {

    val getTopDataReqType = 1 // 首页顶部行情数据请求
    val homepageReqType = 2 // 首页数据请求
    val accountBalanceReqType = 5 //总账户资产请求
    val homeData = 11 //总账户资产请求

    /**
     * 是否开启场外
     */
    private var otcOpen = false

    private var leverOpen = false

    /**
     * 是否开启合约
     */
    private var contractOpen = false

    /**
     * 功能服务
     */
    private var serviceAdapter: NewHomePageServiceAdapter? = null

    var defaultBanner = 0
    var defaultHome = 0


    /*
     *  是否已经登录
     */
    var isLogined = false
    var subscribeCoin: Disposable? = null//保存订阅者
    var isScrollStatus = false

    override fun setContentView() = R.layout.fragment_new_version_homepage

    override fun initView() {
        otcOpen = PublicInfoDataService.getInstance().otcOpen(null)
        leverOpen = PublicInfoDataService.getInstance().isLeverOpen(null)
        contractOpen = PublicInfoDataService.getInstance().contractOpen(null)
        WsAgentManager.instance.addWsCallback(this)
        observeData()
        //设置轮播时间
        banner_looper_custom?.setLoopTime(3000)
        //设置指示器位置（当banner模式中有指示器时）
        banner_looper_custom?.setIndicatorGravity(IndicatorConfig.Direction.CENTER)

        if (ApiConstants.HOME_VIEW_STATUS != ParamConstant.CONTRACT_HOME_PAGE) {
            initTop24HourView()
        }

        setTopBar()
        initRedPacketView()
        setOnClick()
        initNetWorkRemind()
        getPublicInfo()
        LogUtil.d(TAG, "切换语言==NewVersionHomepageFragment==")

        when (ApiConstants.HOME_PAGE_STYLE) {
            ParamConstant.DEFAULT_HOME_PAGE -> {
                defaultBanner = R.drawable.banner_king

            }
            ParamConstant.INTERNATIONAL_HOME_PAGE -> {
                defaultBanner = R.drawable.banner_king
                defaultHome = R.drawable.home_king
            }

        }
        val data = CommonService.instance.getHomeData()
        showHomepageData(data, true)

        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))


    }

    private fun getPublicInfo() {
        val map = TreeMap<String, String>()
        startTask(getContractModel().contractApiService.getPublicInfo1(toRequestBody(DataHandler.encryptParams(map))), Consumer {
            val trader = it.data.enable_module_info.trader
            val increment = it.data.enable_module_info.increment
            tvDocumentary.visibility = if (trader == 1) View.VISIBLE else View.GONE
            tvFinance.visibility = if (increment == 1) View.VISIBLE else View.GONE

        }, Consumer {
            LogUtil.d("我是", it.message)
        })

    }

    private fun initNetWorkRemind() {
        val spanStrStart = SpannableString(getString(R.string.check_network_settings))
        val spanStrClick = SpannableString(getString(R.string.check_network))
        val index = spanStrStart.indexOf(spanStrClick.toString(), 0)
        var spanStrStartSub = spanStrStart.subSequence(0, index)
        var spanStrEnd = spanStrStart.subSequence(index + spanStrClick.length, spanStrStart.length)

        spanStrClick.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = context?.resources!!.getColor(R.color.color_nonetwork_btn_blue) //设置颜色
                //去掉下划线，默认是带下划线的
                ds.isUnderlineText = false
            }
        }, 0, spanStrClick.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        no_network_check.append(spanStrStartSub)
        no_network_check.append(spanStrClick)
        no_network_check.append(spanStrEnd)
        no_network_check.setMovementMethod(LinkMovementMethod.getInstance())

    }

    /*
     * 初始化红包view
     */
    private fun initRedPacketView() {
        val isRedPacketOpen = PublicInfoDataService.getInstance().isRedPacketOpen(null)
        showRedPacket(isRedPacketOpen)
    }

    /**
     * 顶部 24小时涨幅榜(推荐币种)
     */
    private var topSymbolAdapter: NewhomepageTradeListAdapter? = null
    private var topSymbol4ContractAdapter: NewHomePageContractAdapter? = null
    var scrollX = 0
    private fun initTop24HourView() {
        recycler_top_24?.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
        topSymbolAdapter = NewhomepageTradeListAdapter()
        recycler_top_24?.adapter = topSymbolAdapter
        topSymbolAdapter?.setOnItemClickListener { adapter, view, position ->
            var dataList = topSymbolAdapter!!.data
            if (null != dataList && dataList.size > 0) {
                var symbol = dataList[position].optString("symbol")
                ArouterUtil.forwardKLine(symbol)
            }
        }
        recycler_top_24?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                scrollX += dx
                val contentItem = recyclerView.computeHorizontalScrollOffset()
                if (contentItem == 0) {
                    return
                }
                val sum = recyclerView.computeHorizontalScrollRange() - recyclerView.computeHorizontalScrollExtent()
                val count = BigDecimalUtils.div(scrollX.toString(), sum.toString())
                layout_item.setRate(count.toFloat())
            }
        })

    }

    private fun initTop24Hour4Contract() {
        recycler_top_24?.layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
        topSymbol4ContractAdapter = NewHomePageContractAdapter(selectTopSymbol4Contract)
        recycler_top_24?.adapter = topSymbol4ContractAdapter
        topSymbol4ContractAdapter?.setOnItemClickListener { adapter, view, position ->
            val ticker = adapter.data[position] as ContractTicker
            if (!Utils.isFastClick()) {
                SlContractKlineActivity.show(mActivity!!, ticker?.instrument_id)
            }
        }
    }

    val networkParams = hashMapOf<String, String>()
    private fun observeData() {
        NLiveDataUtil.observeData(this, Observer {
            if (null != it) {
                if (MessageEvent.color_rise_fall_type == it.msg_type) {
                    topSymbolAdapter?.notifyDataSetChanged()
                }
            }
        })
        GlobalScope.launch {
            LogUtil.e(TAG, "首页网络统计 start ws状态 " + WsAgentManager.instance.isConnection())
            delay(3000L)
            LogUtil.e(TAG, "首页网络统计 end ws状态 " + WsAgentManager.instance.isConnection())
        }
    }

    inner class MyNDisposableObserver(type: Int) : NDisposableObserver() {

        var req_type = type
        override fun onResponseSuccess(jsonObject: JSONObject) {
            if (getTopDataReqType == req_type) {
                recycler_top_24?.visibility = View.VISIBLE
                v_top_line?.visibility = View.VISIBLE
                showTopSymbolsData(jsonObject.optJSONArray("data"))
            } else if (homepageReqType == req_type) {
                showHomepageData(jsonObject.optJSONObject("data"))
            } else if (homeData == req_type) {
                closeLoadingDialog()
                showHomepageData(jsonObject.optJSONObject("data"))
                advertTime()
            }
        }

        override fun onResponseFailure(code: Int, msg: String?) {
            super.onResponseFailure(code, msg)
            if (getTopDataReqType == req_type) {
                recycler_top_24?.visibility = View.GONE
                v_top_line?.visibility = View.GONE
            }
            if (req_type == homeData) {
                initSocket()
                advertTime(true)
            }
            closeLoadingDialog()
        }
    }


    var homepageData: JSONObject? = null

    var contractHomeRecommendNameList = arrayListOf<String>()

    var contractHomeRecommendList = arrayListOf<JSONArray>()

    /*
     * 首页数据展示
     */
    private fun showHomepageData(data: JSONObject?, isCache: Boolean = false) {
        if (null == data)
            return
        if (!isCache) {
            homepageData = data
            val jsonObjects = JSONObject(data.toString())
            if (jsonObjects != null) {
                CommonService.instance.saveHomeData(jsonObjects)
            }
            val arrayGuide = arrayOf(layout_search, iv_nation_more, layout_top_24, recycler_center_service_layout)
            showGuideHomepage(mActivity, arrayGuide, data)
        }
        var noticeInfoList = data.optJSONArray("noticeInfoList")
        var cmsAppAdvertList = data.optJSONArray("cmsAppAdvertList")
        var cmsAppDataList = data.optJSONArray("cmsAppDataList")
        var cmsAppNoteDataList = data.optJSONArray("cmsAppDataListOther")
        var cmsSymbolList = data.optJSONArray("header_symbol")
        var homeRecommendList = data.optJSONArray("home_recommend_list") ?: JSONArray()

        if (ApiConstants.HOME_VIEW_STATUS == ParamConstant.CONTRACT_HOME_PAGE) {
            /**
             * 合约首页推荐位币对
             */
            var coHeaderSymbols = data.optJSONObject("co_header_symbols")

            /**
             * {
            "id":1,
            "contract_id":1,
            "contract_name":"BTCUSDT",
            "company_id":1438,
            "rank":1,
            "created_at":"2020-09-09T15:09:01Z"
            }
             */
            if (coHeaderSymbols != null && coHeaderSymbols?.length() > 0 && !isCache) {
                var headerSymols = coHeaderSymbols?.optJSONArray("list")
                if (headerSymols != null && headerSymols.length() > 0) {
                    recycler_top_24?.visibility = View.VISIBLE
                    var topSymbolList = ArrayList<ContractTicker>()
                    selectTopSymbol4Contract?.clear()
                    for (i in 0 until headerSymols.length()) {
                        var contract = ContractPublicDataAgent.getContract(headerSymols.optJSONObject(i).optInt("contract_id"))
                        var ticker = ContractTicker()
                        ticker.symbol = contract?.symbol
                        ticker.instrument_id = contract?.instrument_id ?: 0
                        topSymbolList?.add(ticker)
                    }
                    selectTopSymbol4Contract = topSymbolList
                    loadContractData()
                    initTop24Hour4Contract()
                }
            }
            /**
             * 合约首页币对列表
             */
            var coHomeSymbolList = data.optJSONArray("co_home_symbol_list")
            if (coHomeSymbolList != null && coHomeSymbolList.length() > 0 && !isCache) {
                contractHomeRecommendNameList.clear()
                contractHomeRecommendList.clear()
                for (num in 0 until coHomeSymbolList.length()) {
                    var json = coHomeSymbolList.optJSONObject(num)
                    var contracts = json.optJSONArray("contracts")
                    var language = json?.optJSONObject("language")
                    if (contracts != null && contracts.length() > 0) {
                        var languageStr = language?.optString(SystemUtils.getSystemLanguage())
                            ?: ""
                        if (TextUtils.isEmpty(languageStr)) {
                            languageStr = language?.optString("en_US") ?: ""
                        }
                        Log.e("shengong", "langu:$languageStr")
                        contractHomeRecommendNameList.add(languageStr)
                        contractHomeRecommendList.add(contracts)
                    }
                }
                showBottom4Contract()
            }
        } else {
            showTopSymbolsData(cmsSymbolList)
            /*
             *涨幅榜等数据 显示
             */
            showBottomVp(homeRecommendList)
        }

        LogUtil.d("NewVersionHomepageFragment", "showHomepageData==cmsAppAdvertList is $cmsAppAdvertList")
        newNoticeInfoList = noticeInfoList
        showGuanggao(noticeInfoList)
        showBannerData(cmsAppAdvertList)
        initNoteBanner(cmsAppNoteDataList)
        setServiceData(cmsAppDataList)

    }


    var newNoticeInfoList = JSONArray()

    /*
     * 展示顶部轮播图
     */
    var bannerImgUrls = arrayListOf<String>()
    var bannerNoteUrls: ArrayList<String> = arrayListOf()

    private fun showBannerData(cmsAppAdvertList: JSONArray?) {

        if (null == cmsAppAdvertList || cmsAppAdvertList.length() <= 0)
            return

        bannerImgUrls.clear()
        for (i in 0 until cmsAppAdvertList.length()) {
            var obj = cmsAppAdvertList.optJSONObject(i)
            var imageUrl = obj.optString("imageUrl")
            if (StringUtil.isHttpUrl(imageUrl)) {
                bannerImgUrls.add(imageUrl)
            }
        }
        banner_looper?.apply {
            //设置图片集合
            val mAdapter = ImageNetAdapter(bannerImgUrls)
            adapter = mAdapter
            //设置轮播时间
            setLoopTime(3000)
            indicator = RectangleIndicator(context)
            //设置指示器位置（当banner模式中有指示器时）
            setIndicatorGravity(IndicatorConfig.Direction.CENTER)
        }
        banner_looper?.setOnBannerListener { data, position ->
            var obj = cmsAppAdvertList.optJSONObject(position)
            var httpUrl = obj?.optString("httpUrl") ?: ""
            var nativeUrl = obj?.optString("nativeUrl") ?: ""

            //TODO 需要一个标题
            if (TextUtils.isEmpty(httpUrl)) {
                if (StringUtil.checkStr(nativeUrl) && nativeUrl.contains("?")) {
                    enter2Activity(nativeUrl.split("?"))
                }
            } else {
                forwardWeb(obj)
            }
        }
        //banner设置方法全部调用完毕时最后调用
        banner_looper?.start()

    }


    private fun loadContractData() {
        ContractPublicDataAgent.registerTickerWsListener(this, object : ContractTickerListener() {
            override fun onWsContractTicker(ticker: ContractTicker) {
                if (isHidden || !isVisible || !isResumed) {
                    return
                }
                for (i in selectTopSymbol4Contract?.indices!!) {
                    if (selectTopSymbol4Contract?.get(i)?.instrument_id == ticker.instrument_id) {
                        selectTopSymbol4Contract[i] = ticker
                        break
                    }
                }
                topSymbol4ContractAdapter?.setNewData(selectTopSymbol4Contract)
            }
        })
    }


    private fun showAdvertising(isShow: Boolean) {
        if (null != vtc_advertising?.textList && vtc_advertising?.textList!!.size > 0) {
            if (isShow) {
                vtc_advertising?.startAutoScroll()
            } else {
                vtc_advertising?.stopAutoScroll()
            }
        }
    }

    private fun showBanner(isShow: Boolean) {
        if (null != bannerImgUrls && bannerImgUrls!!.size > 0) {
            if (isShow) {
                banner_looper?.start()
            } else {
                banner_looper?.stop()
            }
        }

        if (bannerNoteUrls.size > 0) {
            if (isShow) {
                banner_looper_custom?.start()
            } else {
                banner_looper_custom?.stop()
            }
        }
    }


    private fun getAllAccounts() {
        isLogined = UserDataService.getInstance().isLogined

        if (null == homepageData) {
            getHomeData()
        }
    }


    private fun setTopBar() {
        ns_layout?.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val distance = resources.getDimension(R.dimen.dp_64)
            if ((1 - scrollY / distance) < (0.0001)) {
                item_view_market_line.visibility = View.VISIBLE
            } else {
                item_view_market_line.visibility = View.INVISIBLE
            }
        }
    }

    /*
     * 资产tab跳转
     */
    private fun homeAssetstab_switch(type: Int) {
        var msgEvent = MessageEvent(MessageEvent.hometab_switch_type)
        var bundle = Bundle()
        var homeTabType = HomeTabMap.maps.get(HomeTabMap.assetsTab)
        bundle.putInt(ParamConstant.homeTabType, homeTabType ?: 4)
        bundle.putInt(ParamConstant.assetTabType, type)
        msgEvent.msg_content = bundle
        EventBusUtil.post(msgEvent)
    }

    /*
     * 首页底部tab跳转的处理
     */
    private fun homeTabSwitch(tabType: Int?, symbol: String? = "") {
        var msgEvent = MessageEvent(MessageEvent.hometab_switch_type)
        var bundle = Bundle()
        bundle.putInt(ParamConstant.homeTabType, tabType ?: 0)
        if (symbol != null) {
            bundle.putString(ParamConstant.symbol, symbol)
        }
        msgEvent.msg_content = bundle
        EventBusUtil.post(msgEvent)
    }

    /*
     * 跳转至 NewVersionMyAssetActivity
     */
    private fun forwardAssetsActivity(type: Int) {
        var bundle = Bundle()
        bundle.putInt(ParamConstant.assetTabType, type)//跳转到币币交易页面
        ArouterUtil.navigation(RoutePath.NewVersionMyAssetActivity, bundle)
    }


    fun setOnClick() {

        /**
         * 个人中心
         */
        iv_personal_logo?.setOnClickListener {
            ArouterUtil.navigation(RoutePath.PersonalCenterActivity, null)
        }

        layout_search?.setOnClickListener {
            ArouterUtil.greenChannel(RoutePath.CoinMapActivity, Bundle().apply {
                putString("type", ParamConstant.ADD_COIN_MAP)
            })
        }
        tvDocumentary?.setOnClickListener {
            if (!LoginManager.checkLogin(context, true)) {
                return@setOnClickListener
            }
            ArouterUtil.navigation(RoutePath.DocumentaryActivity, null)
        }
        tvFinance?.setOnClickListener {
            if (!LoginManager.checkLogin(context, true)) {
                return@setOnClickListener
            }
            ArouterUtil.navigation(RoutePath.FinancialActivity, null)
        }
        tvBinary?.setOnClickListener {
            if (!LoginManager.checkLogin(context, true)) {
                return@setOnClickListener
            }
            ArouterUtil.navigation(RoutePath.BinaryActivity, null)
        }
        iv_market_msg?.setOnClickListener {
            if (LoginManager.checkLogin(mActivity, true)) {
                startActivity(Intent(mActivity, MailActivity::class.java))
            }
        }

        iv_nation_more?.setOnClickListener {
//            if (LoginManager.checkLogin(mActivity, true)) {
            startActivity(Intent(mActivity, NoticeActivity::class.java))
//            }
        }

        if (SystemUtils.isZh()) {
            rl_red_envelope_entrance.setImageResource(R.drawable.redenvelope)
        } else {
            rl_red_envelope_entrance.setImageResource(R.drawable.redenvelope_english)
        }

        /**
         * 点击红包 跳转
         */
        rl_red_envelope_entrance?.setOnClickListener {
            if (!LoginManager.checkLogin(activity, true)) {
                return@setOnClickListener
            }

            var isEnforceGoogleAuth = PublicInfoDataService.getInstance().isEnforceGoogleAuth(null)

            var authLevel = UserDataService.getInstance().authLevel
            var googleStatus = UserDataService.getInstance().googleStatus
            var isOpenMobileCheck = UserDataService.getInstance().isOpenMobileCheck

            if (isEnforceGoogleAuth) {
                if (authLevel != 1 || googleStatus != 1) {
                    NewDialogUtils.redPackageCondition(context ?: return@setOnClickListener)
                    return@setOnClickListener
                }
            } else {
                if (authLevel != 1 || (googleStatus != 1 && isOpenMobileCheck != 1)) {
                    NewDialogUtils.redPackageCondition(context ?: return@setOnClickListener)
                    return@setOnClickListener
                }
            }
            ArouterUtil.navigation(RoutePath.CreateRedPackageActivity, null)
        }

        /**
         * 点击关闭红包
         */
        iv_close_red_envelope?.setOnClickListener {
            showRedPacket(false)
        }

        /**
         * 此处刷新
         */
        swipe_refresh?.setOnRefreshListener {
            isScrollStatus = true
            /**
             * 刷新数据操作
             */
            if (homepageData == null || fragments.size == 0 || selectTopSymbol == null) {
                getHomeData()
            } else {
                getVPTab()
            }
            swipe_refresh?.isRefreshing = false
        }
        net_wrong?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        no_network_check?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        no_network_retry_btn?.setOnClickListener {
            getHomeData()
        }
    }


    private fun showRedPacket(isVisibile: Boolean) {
        if (isVisibile) {
            rl_red_envelope_entranc_layout?.visibility = View.VISIBLE
        } else {
            rl_red_envelope_entranc_layout?.visibility = View.GONE
        }
    }

    /*
     * 首页顶部symbol 24小时行情展示
     */
    var selectTopSymbol: ArrayList<JSONObject>? = null
    var selectTopSymbol4Contract: ArrayList<ContractTicker> = arrayListOf()


    fun showTopSymbols4ContractData() {
        if (null == selectTopSymbol4Contract || selectTopSymbol4Contract?.size!! <= 0) {
            setTopViewVisible(false)
            return
        }
        setTopViewVisible(true)
        topSymbol4ContractAdapter?.setNewData(selectTopSymbol4Contract)
    }

    private fun showTopSymbolsData(topSymbol: JSONArray?) {
        selectTopSymbol = NCoinManager.getSymbols(topSymbol)
        if (null == selectTopSymbol || selectTopSymbol?.size!! <= 0) {
            setTopViewVisible(false)
            return
        }
        setTopViewVisible(true)
        topSymbolAdapter?.setList(selectTopSymbol)
    }


    private fun initSocket() {
        if (selectTopSymbol == null) {
            return
        }
        var arrays = arrayListOf<String>()
        for (item in selectTopSymbol!!) {
            arrays.add(item.getString("symbol"))
        }
        var json = ""
        homeCoins.clear()
        if (bottomCoins.isNotEmpty()) {
            val temp = bottomCoins union arrays
            homeCoins.addAll(temp)
            json = JsonUtils.gson.toJson(temp)
        } else {
            homeCoins.addAll(arrays)
            json = JsonUtils.gson.toJson(arrays)
        }
        WsAgentManager.instance.sendMessage(hashMapOf("bind" to true, "symbols" to json), this)
    }

    override fun fragmentVisibile(isVisible: Boolean) {
        super.fragmentVisibile(isVisible)
        var mainActivity = activity
        if (mainActivity != null) {
            if (mainActivity is NewMainActivity) {
                if (isVisible && mainActivity.curPosition == 0) {
                    getAllAccounts()
                    showAdvertising(true)
                    showBanner(true)
                } else {
                    showAdvertising(false)
                    showBanner(false)
                }

            }
        }

    }


    /**
     * 是否显示24小时行情
     */
    fun setTopViewVisible(isShow: Boolean) {
        if (isShow) {
            recycler_top_24?.visibility = View.VISIBLE
            if (selectTopSymbol != null && selectTopSymbol!!.size > 3) {
                layout_item?.visibility = View.VISIBLE
            } else {
                layout_item?.visibility = View.GONE
            }
            v_top_line?.visibility = View.VISIBLE
        } else {
            recycler_top_24?.visibility = View.GONE
            layout_item?.visibility = View.GONE
            v_top_line?.visibility = View.GONE
        }
    }

    /**
     * 获取顶部symbol 24小时行情
     */
    fun getHomeData() {
        showLoadingDialog()
        var type = "1"
        if (ApiConstants.HOME_VIEW_STATUS == ParamConstant.CONTRACT_HOME_PAGE) {
            type = "2"
        }
        klineTime = System.currentTimeMillis()
        var disposable = getMainModel().getHomeData(type, MyNDisposableObserver(homeData))
        addDisposable(disposable)

    }

    override fun refreshOkhttp(position: Int) {
        if (position == 0) {
            getTopData()
        }
    }

    val fragments = arrayListOf<Fragment>()
    var selectPostion = 0
    val chooseType = arrayListOf<String>()


    /**
     * 合约
     */
    fun showBottom4Contract() {

        fragments.clear()
        if (contractHomeRecommendNameList.size == 0) {
            return
        }
        for (i in contractHomeRecommendNameList.indices) {
            fragments.add(
                NewSlCoinSearchItemFragment.newInstance(
                    i,
                    fragment_market,
                    contractHomeRecommendList.get(i).toString()
                )
            )
        }
        var marketPageAdapter = NVPagerAdapter(childFragmentManager, contractHomeRecommendNameList, fragments)
        fragment_market?.adapter = marketPageAdapter
        fragment_market?.offscreenPageLimit = fragments.size
        fragment_market?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                selectPostion = position
//                WsAgentManager.instance.unbind(this@NewVersionHomepageFragment)
//                loopData()
                fragment_market?.resetHeight(selectPostion)
            }
        })

        try {
            stl_homepage_list?.setViewPager(fragment_market, contractHomeRecommendNameList.toTypedArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun showBottomVp(data: JSONArray) {
        var titles = arrayListOf<String>()
        var serviceDatas = JSONUtil.arrayToList(data)
        if (serviceDatas == null) {
            if (!NetUtil.isNetConnected(mActivity!!)) {
                no_network_bottom_vp?.visibility = View.VISIBLE
                bottom_vp_linearlayout?.visibility = View.GONE
            }
            return
        }
        no_network_bottom_vp?.visibility = View.GONE
        bottom_vp_linearlayout?.visibility = View.VISIBLE
        for (item in serviceDatas) {
            titles.add(item.getString("title"))
            chooseType.add(item.getString("key"))
        }
        fragments.clear()
        if (titles.isEmpty())
            return

        for (i in titles.indices) {
            fragments.add(
                NewHomeDetailFragmentItem.newInstance(
                    titles[i],
                    i,
                    chooseType[i],
                    fragment_market,
                    serviceDatas.get(i).getJSONArray("list").toString()
                )
            )
        }

        var marketPageAdapter = NVPagerAdapter(childFragmentManager, titles, fragments)
        fragment_market?.adapter = marketPageAdapter
        fragment_market?.offscreenPageLimit = fragments.size
        fragment_market?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                selectPostion = position
                WsAgentManager.instance.unbind(this@NewVersionHomepageFragment)
                loopData()
                fragment_market?.resetHeight(selectPostion)
            }
        })
        loopData()
        try {
            stl_homepage_list?.setViewPager(fragment_market, titles.toTypedArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取滚动的广告
     */
    fun getNoticeInfoList(notcieList: JSONArray): ArrayList<String> {
        var noticeList4String: ArrayList<String> = arrayListOf()
        for (i in 0 until notcieList.length()) {
            var obj = notcieList.optJSONObject(i)
            var title = obj.optString("title")
            if (null != title) {
                noticeList4String.add(title)
            }
        }
        return noticeList4String
    }

    /**
     * 广告根据数据加载
     * 有数据显示，无数据隐藏
     */
    private fun showGuanggao(noticeInfoList: JSONArray?) {
        if (null != noticeInfoList && noticeInfoList.length() > 0) {
            ll_advertising_layout?.visibility = View.VISIBLE
            if (null == vtc_advertising?.textList || vtc_advertising?.textList!!.size == 0) {
                vtc_advertising?.setText(12f, 0, ContextCompat.getColor(context!!, R.color.text_color), true)
                vtc_advertising?.setTextStillTime(4000)//设置停留时长间隔
                vtc_advertising?.setAnimTime(400)//设置进入和退出的时间间隔
                vtc_advertising?.setOnItemClickListener(object : VerticalTextview4ChainUp.OnItemClickListener {
                    override fun onItemClick(pos: Int) {
                        var obj = newNoticeInfoList.optJSONObject(pos)
                        forwardWeb(obj)
                    }
                })
            }
            vtc_advertising?.setTextList(getNoticeInfoList(noticeInfoList))
            vtc_advertising?.startAutoScroll()
        } else {
            ll_advertising_layout?.visibility = View.GONE
        }
    }

    private fun forwardWeb(jsonObject: JSONObject?) {
        var id = jsonObject?.optString("id")
        var title = jsonObject?.optString("title")
        var httpUrl = jsonObject?.optString("httpUrl")

        var bundle = Bundle()
        bundle.putString(ParamConstant.head_title, title)
        if (StringUtil.isHttpUrl(httpUrl)) {
            bundle.putString(ParamConstant.web_url, httpUrl)
        } else {
            bundle.putString(ParamConstant.web_url, id)
            bundle.putInt(ParamConstant.web_type, WebTypeEnum.Notice.value)
        }
        ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
    }


    private fun forUdeskWebView() {
        var bundle = Bundle()
        bundle.putString(ParamConstant.URL_4_SERVICE, PublicInfoDataService.getInstance().getOnlineService(null))
        ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)
    }

    /**
     * 如果服务器没有返回服务数据
     * 服务功能这里整体GONE
     */
    private fun setServiceView() {
        recycler_center_service_layout?.visibility = View.GONE
    }

    private fun setServiceShowView() {
        recycler_center_service_layout?.visibility = View.VISIBLE
    }

    private var servicePageSize = 0

    /**
     *
     *  从服务器获取功能服务数据后填充
     */

    private fun setServiceData(appData: JSONArray?, viewType: Int = 0) {
        if (null == appData || appData.length() <= 0) {
            setServiceView()
            return
        }
        setServiceShowView()
        val serviceDatas = JSONUtil.arrayToList(appData)
        val mardBottom = if (bannerNoteUrls.size != 0) DisplayUtil.dip2px(2) else DisplayUtil.dip2px(12)
        val linearParams = recycler_center_service_layout?.layoutParams as LinearLayout.LayoutParams
        linearParams.setMargins(0, 0, 0, mardBottom)
        recycler_center_service_layout?.layoutParams = linearParams
        val linearParams2 = recycler_center_service?.layoutParams
        linearParams2?.width = ViewGroup.LayoutParams.MATCH_PARENT
        linearParams2?.height = SizeUtils.dp2px(if (serviceDatas.size > 4) 128f else 64f)
        recycler_center_service?.layoutParams = linearParams2

        val mLayoutManager = PagerGridLayoutManager(if (serviceDatas.size > 4) 2 else 1, 4, PagerGridLayoutManager.HORIZONTAL)
        mLayoutManager.setPageListener(object : PagerGridLayoutManager.PageListener {
            override fun onPageSelect(pageIndex: Int) {
                //todo  这里是第几页 +1
                if (servicePageSize <= 1) {
                    return
                }
                when (pageIndex) {
                    0 -> {
                        iv_frist?.setBackgroundResource(R.drawable.item_bg_4_homepage_select)
                        iv_second?.setBackgroundResource(R.drawable.item_bg_4_homepage_unselect)

                    }
                    1 -> {
                        iv_frist?.setBackgroundResource(R.drawable.item_bg_4_homepage_unselect)
                        iv_second?.setBackgroundResource(R.drawable.item_bg_4_homepage_select)
                    }
                }
            }

            override fun onPageSizeChanged(pageSize: Int) {
                servicePageSize = pageSize

            }
        })
        if (serviceDatas.size > 8) {
            rl_controller?.visibility = View.VISIBLE
        } else {
            rl_controller?.visibility = View.INVISIBLE
        }
        val snapHelper = PagerGridSnapHelper()
        if (recycler_center_service?.onFlingListener == null) {
            snapHelper.attachToRecyclerView(recycler_center_service ?: return)
        }
        recycler_center_service?.layoutManager = mLayoutManager


        serviceAdapter = NewHomePageServiceAdapter(serviceDatas)
        serviceAdapter?.setOnItemClickListener { adapter, view, position ->

            var obj = serviceDatas.get(position)
            var httpUrl = obj.optString("httpUrl")
            var nativeUrl = obj.optString("nativeUrl")

            LogUtil.d(TAG, "httpUrl is $httpUrl , nativeUrl is $nativeUrl")
            if (TextUtils.isEmpty(httpUrl)) {
                if (StringUtil.checkStr(nativeUrl) && nativeUrl.contains("?")) {
                    enter2Activity(nativeUrl?.split("?"))
                }
            } else {
                if (httpUrl == PublicInfoDataService.getInstance().getOnlineService(null)) {
                    forUdeskWebView()
                } else {
                    forwardWeb(obj)
                }
            }
        }

        recycler_center_service?.adapter = serviceAdapter
    }


    /**
     * 对应的服务
     */
    fun enter2Activity(temp: List<String>?) {

        if (null == temp || temp.size <= 0)
            return

        when (temp[0]) {
            "coinmap_market" -> {
                /**
                 * 行情
                 */
                var tabType = HomeTabMap.maps[HomeTabMap.marketTab]
                homeTabSwitch(tabType)
            }
            "coinmap_trading" -> {
                /**
                 * 币对交易页
                 */
                var tabType = HomeTabMap.maps[HomeTabMap.coinTradeTab]
                homeTabSwitch(tabType, temp[1])
            }
            "coinmap_details" -> {
                /**
                 * 币对详情页
                 * MarketDetailActivity
                 */
                if (!TextUtils.isEmpty(temp[1])) {
                    ArouterUtil.forwardKLine(temp[1])
                } else {
                    NToastUtil.showTopToastNet(this.mActivity, false, LanguageUtil.getString(context, "common_tip_hasNoCoinPair"))
                }
            }
            "otc_buy" -> {
                /**
                 *场外交易-购买
                 */
                /*if (LoginManager.checkLogin(activity, true)) {
                }*/
                if (otcOpen) {
                    var bundle = Bundle()
                    bundle.putInt("tag", 0)
                    ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, bundle)
                } else {
                    NToastUtil.showTopToastNet(this.mActivity, false, LanguageUtil.getString(context, "common_tip_notSupportOTC"))
                }
            }
            "otc_sell" -> {
                /**
                 * 场外交易-出售
                 */
                if (otcOpen) {
                    var bundle = Bundle()
                    bundle.putInt("tag", 1)
                    ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, bundle)
                } else {
                    NToastUtil.showTopToastNet(this.mActivity, false, LanguageUtil.getString(context, "common_tip_notSupportOTC"))
                }
            }

            "order_record" -> {
                /**
                 *订单记录
                 */

                if (LoginManager.checkLogin(activity, true)) {
                    if (otcOpen) {
                        ArouterUtil.greenChannel(RoutePath.NewOTCOrdersActivity, null)
                    } else {
                        NToastUtil.showTopToastNet(
                            this.mActivity,
                            false,
                            LanguageUtil.getString(context, "common_tip_notSupportOTC")
                        )
                    }
                }
            }
            "account_transfer" -> {
                /**
                 * 账户划转
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, "BTC")
                }
            }
            "otc_account" -> {
                /**
                 *资产-场外账户
                 */
                if (LoginManager.checkLogin(activity, true)) {

                    if (otcOpen) {
                        if (ApiConstants.HOME_VIEW_STATUS == ParamConstant.CONTRACT_HOME_PAGE) {
                            homeAssetstab_switch(1)
                        } else {
                            if (contractOpen) {
                                forwardAssetsActivity(1)
                            } else {
                                homeAssetstab_switch(1)
                            }
                        }

                    } else {
                        NToastUtil.showTopToastNet(
                            this.mActivity,
                            false,
                            LanguageUtil.getString(context, "common_tip_notSupportOTC")
                        )
                    }
                }
            }
            "contract_follow_order" -> {
                /**
                 * 跟单页面
                 */

            }

            "coin_account" -> {
                /**
                 * 资产-币币账户
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    if (ApiConstants.HOME_VIEW_STATUS == ParamConstant.CONTRACT_HOME_PAGE) {
                        homeAssetstab_switch(0)
                    } else {
                        if (contractOpen && otcOpen) {
                            forwardAssetsActivity(0)
                        } else {
                            homeAssetstab_switch(0)
                        }
                    }

                }

            }
            "safe_set" -> {
                /**
                 *安全设置
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    ArouterUtil.navigation(RoutePath.SafetySettingActivity, null)
                }
            }
            "safe_money" -> {
                /**
                 * 安全设置-资金密码
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    if (UserDataService.getInstance()?.authLevel != 1) {
                        NToastUtil.showTopToastNet(this.mActivity, false, LanguageUtil.getString(context, "otc_please_cert"))
                        return
                    }
                    if (UserDataService.getInstance().isCapitalPwordSet == 0) {
                        ArouterUtil.forwardModifyPwdPage(ParamConstant.SET_PWD, ParamConstant.FROM_OTC)
                    } else {
                        ArouterUtil.forwardModifyPwdPage(ParamConstant.RESET_PWD, ParamConstant.FROM_OTC)
                    }
                }
            }
            "personal_information" -> {
                /**
                 *个人资料
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    ArouterUtil.greenChannel(RoutePath.PersonalInfoActivity, null)
                }

            }
            "personal_invitation" -> {
                /**
                 *个人资料-邀请码
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    ArouterUtil.navigation(RoutePath.ContractAgentActivity, null)
                }

            }
            "collection_way" -> {
                /**
                 *收款方式
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    ArouterUtil.greenChannel(RoutePath.PaymentMethodActivity, null)
                }
            }
            "real_name" -> {
                /**
                 *实名认证
                 */
                if (LoginManager.checkLogin(activity, true)) {
                    when (UserDataService.getInstance().authLevel) {
                        0 -> {
                            NToastUtil.showTopToastNet(
                                this.mActivity,
                                false,
                                LanguageUtil.getString(context, "noun_login_pending")
                            )
                        }
                        1 -> {
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                        }
                        /**
                         * 审核未通过
                         */
                        2 -> {
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                        }

                        3 -> {
                            ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                        }
                    }
                }
            }
            "contract_transaction" -> {
                /**
                 * 去合约交易页面
                 */
                forwardContractTab()
            }

            "market_etf" -> {
                /**
                 * ETF列表
                 */
                forwardMarketTab("ETF")
            }

            /**
             * 合约经纪人
             * TODO 这里需要确定key
             */
            "config_contract_agent_key" -> {
                if (!LoginManager.checkLogin(context, true)) {
                    return
                }
                ArouterUtil.navigation(RoutePath.ContractAgentActivity, null)
            }

            "account_freeStaking" -> {
                if (!LoginManager.checkLogin(context, true)) {
                    return
                }
                /**
                 * FreeStaking首页
                 */
                ArouterUtil.greenChannel(RoutePath.FreeStakingActivity, null)
            }


        }
    }

    private fun forwardContractTab() {
        var messageEvent = MessageEvent(MessageEvent.contract_switch_type)
        EventBusUtil.post(messageEvent)
    }

    private fun forwardMarketTab(coin: String) {
        var messageEvent = MessageEvent(MessageEvent.market_switch_type)
        messageEvent.msg_content = coin
        EventBusUtil.post(messageEvent)
    }

    /**
     * 获取账户信息
     */
    var accountBalance = ""
    var accountFlat = ""
    private fun getAccountBalance() {
        var disposable = getMainModel().getTotalAsset(MyNDisposableObserver(accountBalanceReqType))
        addDisposable(disposable!!)

    }

    override fun onWsMessage(json: String) {
        handleData(json)
    }

    fun handleData(data: String) {
        try {
            val json = JSONObject(data)
            if (!json.isNull("tick")) {
                doAsync {
                    val channel = json.optString("channel")
                    val temp = homeCoins.filter {
                        channel.contains(it)
                    }
                    if (temp.isNotEmpty()) {
                        val dataDiff = callDataDiff(json)
                        if (dataDiff != null) {
                            val items = dataDiff.second
                            if (bottomCoins.size != homeCoins.size) {
                                showWsData(items)
                            }
                            if (fragments.size == 0) {
                                return@doAsync
                            }
                            val fragment = fragments[selectPostion]
                            if (fragment is NewHomeDetailFragmentItem) {
                                val tempMap = HashMap<String, JSONObject>()
                                LogUtil.e(TAG, "showWsData bottom ${items.size}")
                                for (item in items) {
                                    val channelNew = item.value.optString("channel").split("_")[1]
                                    val tempBottom = bottomCoins.filter {
                                        channelNew.contains(it)
                                    }
                                    if (tempBottom.isNotEmpty()) {
                                        tempMap.put(item.key, item.value)
                                    }
                                }
                                LogUtil.e(TAG, "showWsData bottom 过滤 ${tempMap.size}")
                                if (tempMap.isEmpty()) {
                                    return@doAsync
                                }
                                fragment.dropListsAdapter(tempMap)
                            }
                            wsArrayTempList.clear()
                            wsArrayMap.clear()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showWsData(items: HashMap<String, JSONObject>) {
        if (0 == selectTopSymbol?.size)
            return
        val dates = selectTopSymbol
        var isLoad = 0
        for (item in items) {
            val channel = item.value.optString("channel").split("_")[1]
            val temp = dates?.filter {
                channel.contains(it.optString("symbol"))
            }
            if (temp != null && temp.isNotEmpty()) {
                val jsonObject = temp[0]
                val data = item.value
                val tick = data.optJSONObject("tick")
                tick?.apply {
                    jsonObject.put("rose", this.optString("rose"))
                    jsonObject.put("close", this.optString("close"))
                    jsonObject.put("vol", this.optString("vol"))
                    val index = dates.indexOf(jsonObject)
                    dates.set(index, jsonObject)
                    isLoad++
                }
            }
        }
        if (isLoad != 0) {
            activity?.runOnUiThread {
                topSymbolAdapter?.setList(dates)
            }
        }

    }

    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
        LogUtil.e(TAG, "onVisibleChanged==NewVersionHomepageFragment ${isVisible} ")
        if (isVisible) {
            Handler().postDelayed({
                isRoseHttp()
                initSocket()
                if (fragments.size == 0) {
                    return@postDelayed
                }
                val fragment = fragments[selectPostion]
                if (fragment is NewHomeDetailFragmentItem) {
                    fragment.startInit()
                }
            }, 100)
        } else {
            if (selectTopSymbol != null) {
                WsAgentManager.instance.unbind(this)
            }
            clearToolHttp()
        }
    }

    private var bottomCoins = arrayListOf<String>()
    private var homeCoins = arrayListOf<String>()

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.home_event_page_symbol_type == event.msg_type) {
            val mainActivity = activity
            if (mainActivity is NewMainActivity) {
                if (mainActivity.curPosition != 0) {
                    return
                }
            }
            val map = event.msg_content as HashMap<String, Array<String>>
            val curIndex = map.get("curIndex") as Int
            if (selectPostion != curIndex) {
                return
            }
            bottomCoins.clear()
            val array = map.get("symbols") as Array<String>
            for (item in array) {
                bottomCoins.add(item)
            }
            initSocket()
        }
        /*
         *检测网络状态
         */
        if (MessageEvent.net_status_change == event.msg_type) {
            if (mActivity?.let { NetUtil.isNetConnected(it) } == true) {
                net_wrong?.visibility = View.GONE
            } else {
                net_wrong?.visibility = View.VISIBLE
            }
        }
    }

    private var isRose = true
    private fun loopData() {
        LogUtil.e(TAG, "tradeList value loopData  ${mIsVisibleToUser} ")
        if (!mIsVisibleToUser)
            return
        clearToolHttp()
        if (subscribeCoin == null || (subscribeCoin != null && subscribeCoin?.isDisposed != null && subscribeCoin?.isDisposed!!)) {
            subscribeCoin = Observable.interval(10L, CommonConstant.homeLoopTime, TimeUnit.SECONDS)//按时间间隔发送整数的Observable
                .observeOn(AndroidSchedulers.mainThread())//切换到主线程修改UI
                .subscribe {
                    getVPTab()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        LogUtil.e(TAG, "onResume() ")
    }

    private fun isRoseHttp() {
        if (!isRose) {
            return
        }
        isRose = false
        loopData()
    }

    /**
     * 获取数据
     */
    private fun getVPTab() {
        if (chooseType.size == 0) {
            return
        }
        val type = chooseType[selectPostion]
        var disposable = getMainModel().trade_list_v4(type, object : NDisposableObserver(null, false, type) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                isRose = true
                val fragment = fragments[selectPostion]
                if (fragment is NewHomeDetailFragmentItem) {
                    if (type == this.getHomeTabType()) {
                        fragment.initV(jsonObject.optJSONArray("data"))
                    }
                }
                this.mapParams
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                isRose = true
            }
        })
        addDisposable(disposable)
    }

    private fun clearToolHttp() {
        if (subscribeCoin != null) {
            subscribeCoin?.dispose()
        }
    }

    override fun appBackGroundChange(isVisible: Boolean) {
        super.appBackGroundChange(isVisible)
        LogUtil.e(TAG, "appBackGroundChange==NewVersionHomepageFragment ${isVisible} ")
        mIsVisibleToUser = isVisible
        if (isVisible) {
            isRoseHttp()
        } else {
            clearToolHttp()
        }
    }

    private fun initNoteBanner(cmsAppDataList: JSONArray?) {
        if (cmsAppDataList != null && cmsAppDataList.length() != 0) {
            rl_custom_config?.visibility = View.VISIBLE
            var serviceDatas = JSONUtil.arrayToList(cmsAppDataList)
            val arrayBanner = arrayListOf<String>()
            serviceDatas.forEach {
                var imageUrl = it.optString("imageUrl")
                if (StringUtil.isHttpUrl(imageUrl)) {
                    arrayBanner.add(imageUrl)
                }
            }
            bannerNoteUrls.clear()
            bannerNoteUrls.addAll(arrayBanner)
            banner_looper_custom?.let {

                val mAdapter = ImageNetAdapter(bannerNoteUrls)
                it.adapter = mAdapter
            }
            banner_looper_custom?.start()
            banner_looper_custom?.setOnBannerListener { data, position ->
                val obj = cmsAppDataList.optJSONObject(position)
                routeApp(obj)
            }
        } else {
            rl_custom_config?.visibility = View.GONE
            bannerNoteUrls.clear()
        }
    }

    private fun routeApp(obj: JSONObject?) {
        var httpUrl = obj?.optString("httpUrl") ?: ""
        var nativeUrl = obj?.optString("nativeUrl") ?: ""
        if (TextUtils.isEmpty(httpUrl)) {
            if (StringUtil.checkStr(nativeUrl) && nativeUrl.contains("?")) {
                enter2Activity(nativeUrl.split("?"))
            }
        } else {
            forwardWeb(obj)
        }
    }

    /**
     * 获取顶部symbol 24小时行情
     */
    fun getTopData() {
        if (ApiConstants.HOME_VIEW_STATUS != ParamConstant.CONTRACT_HOME_PAGE) {
            var disposable = getMainModel().header_symbol(MyNDisposableObserver(getTopDataReqType))
            addDisposable(disposable)
        }
    }

    var klineTime = 0L
    private fun advertTime(isError: Boolean = false) {
        klineTime = System.currentTimeMillis() - klineTime
        val temp = if (isError) {
            4
        } else {
            val market = PublicInfoDataService.getInstance().getMarket(null)
            if (market == null) {
                5
            } else {
                if (fragments.isNotEmpty()) {
                    0
                } else {
                    3
                }

            }

        }
        sendWsHomepage(mIsVisibleToUser, temp, NetworkDataService.KEY_PAGE_HOME, NetworkDataService.KEY_HTTP_HOME, klineTime)
    }


    private val wsArrayTempList: ArrayList<JSONObject> = arrayListOf()
    private val wsArrayMap = hashMapOf<String, JSONObject>()
    private var wsTimeFirst: Long = 0L

    @Synchronized
    private fun callDataDiff(jsonObject: JSONObject): Pair<ArrayList<JSONObject>, HashMap<String, JSONObject>>? {
        if (System.currentTimeMillis() - wsTimeFirst >= 1000L && wsTimeFirst != 0L) {
            // 大于一秒
            wsTimeFirst = 0L
            if (wsArrayMap.size != 0) {
                return Pair(wsArrayTempList, wsArrayMap)
            }
        } else {
            if (wsTimeFirst == 0L) {
                wsTimeFirst = System.currentTimeMillis()
            }
            wsArrayTempList.add(jsonObject)
            wsArrayMap.put(jsonObject.optString("channel", ""), jsonObject)
        }
        return null
    }


}
