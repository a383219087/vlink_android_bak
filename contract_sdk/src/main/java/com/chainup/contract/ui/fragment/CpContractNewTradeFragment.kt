package com.chainup.contract.ui.fragment

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.LogUtils
import com.chainup.contract.R
import com.chainup.contract.adapter.CpKLineScaleAdapter
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.chainup.contract.app.CpCommonConstant
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.app.CpParamConstant
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.*
import com.chainup.contract.eventbus.*
import com.chainup.contract.ui.activity.CpContractEntrustNewActivity
import com.chainup.contract.ui.activity.CpMarketDetail4Activity
import com.chainup.contract.utils.*
import com.chainup.contract.view.*
import com.chainup.contract.ws.CpWsContractAgentManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.yjkj.chainup.bean.kline.cp.DepthItem
import com.yjkj.chainup.manager.CpLanguageUtil
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.activity.CpContractCalculateActivity
import com.yjkj.chainup.new_contract.adapter.CpContractKlineCtrlAdapter
import com.yjkj.chainup.new_contract.bean.CpKlineCtrlBean
import com.google.android.material.appbar.AppBarLayout.Behavior.DragCallback
import com.chainup.contract.kline1.bean.CpKLineBean
import com.chainup.contract.kline1.data.CpDataManager
import com.chainup.contract.kline1.data.CpKLineChartAdapter
import com.chainup.contract.kline1.view.MainKlineViewStatus
import com.chainup.contract.kline1.view.vice.CpViceViewStatus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.cp_activity_contract_k_line_h.*
import kotlinx.android.synthetic.main.cp_activity_market_detail4.*
import kotlinx.android.synthetic.main.cp_depth_chart_com.*
import kotlinx.android.synthetic.main.cp_fragment_cl_contract.tv_capital_rate
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.*
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.customize_depth_chart
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.kline_tab_indicator
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rb_boll
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rb_kdj
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rb_ma
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rb_macd
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rb_rsi
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rb_wr
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rl_kline_ctrl
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rv_kline_ctrl
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rv_kline_scale
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.v_kline
import kotlinx.android.synthetic.main.cp_trade_header_tools.*
import kotlinx.android.synthetic.main.cp_trade_header_tools.ib_collect
import kotlinx.android.synthetic.main.cp_trade_header_tools.ib_kline
import kotlinx.android.synthetic.main.cp_trade_header_tools.iv_more
import kotlinx.android.synthetic.main.cp_trade_header_tools.tv_contract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * 合约
 */
class CpContractNewTradeFragment : CpNBaseFragment(), CpWsContractAgentManager.WsResultCallback {

    override fun setContentView() = R.layout.cp_fragment_cl_contract_trade_new

    var currentSymbol = "e_btcusdt"
    var quote = ""
    var base = ""
    var mSymbol = "btcusdt"
    var mContractId = 1
    var depthLevel = "0"
    var contractType = "0"
    var symbolPricePrecision = 2
    var couponTag = 1 //体验金标识 ： 0：未领取 1：已领取
    var openContract = 0 //是否开通了合约交易 1已开通, 0未开通
    var futuresLocalLimit = 0 //1 区域限制范围内  0 不在限制范围
    var authLevel = 0 //	 0、未审核，1、通过，2、未通过  3未认证
    var subscribe: Disposable? = null
    var isContractHidden: Boolean = true
    var isContractFirst: Boolean = false

    //k线图start
    private var mCpContractKlineCtrlAdapter: CpContractKlineCtrlAdapter? = null
    private var mklineCtrlList = ArrayList<CpKlineCtrlBean>()
    var isFrist = true
    var aG = true

    /*
     * KLine参数数据初始化
     */
    private var main_index = 0
    private var vice_index = 0
    private var curTime: String? = ""
    private var cur_time_index = 0;
    private var klineScale = ArrayList<String>()
    private var themeMode = 0
    private fun initKLineData() {
        main_index = CpKLineUtil.getMainIndex(type = 0)
        vice_index = CpKLineUtil.getViceIndex(type = 0)
        cur_time_index = CpKLineUtil.getCurTime4Index(type = 0)

        curTime = CpKLineUtil.getCurTime(type = 0)
        klineScale = CpKLineUtil.getKLineScale()
        themeMode = 0
    }

    var klineData: ArrayList<CpKLineBean> = arrayListOf()
    var symbol = ""
    private var hasInit = false
    private var isShowPage = false
    var contractId = -1
    var baseSymbol = ""
    var quoteSymbol = ""
    var mPricePrecision = 0
    var coUnit = 0
    var mMultiplierPrecision = 0
    var mMultiplier = "0"
    var tv24hVolUnit = ""
    var mMultiplierCoin = ""

    var mClDepthFragment: CpDepthFragment? = null
    var dealtRecordFragment: CpDealtRecordFragment? = null
    var pageAdapter: CpNVPagerAdapter? = null
    var selectPosition = 0
    private val adapter by lazy { CpKLineChartAdapter() }


    var calibrationAdapter: CpKLineScaleAdapter? = null

    /**
     * 主图指标的子view
     */
    private val mainViewStatusViews: ArrayList<RadioButton?> by lazy(LazyThreadSafetyMode.NONE) {
        arrayListOf(rb_ma, rb_boll, rb_hide_main)
    }

    /**
     * 副图指标的子view
     */
    private val viceViewStatusViews: ArrayList<RadioButton?> by lazy(LazyThreadSafetyMode.NONE) {
        arrayListOf(rb_macd, rb_kdj, rb_wr, rb_rsi, rb_hide_vice)
    }
    private var lastTick: KlineQuotesData? = null
    private var isRealNew = false

    /**
     * 显示指标orScale
     */
    var isShow = false
    var klineState = 0
    private var datas: DepthItem? = null //k线图end


    override fun loadData() {
        super.loadData()
        CpWsContractAgentManager.instance.addWsCallback(this)
    }

    override fun initView() {
        initTabInfo()
        tv_contract.setOnClickListener {
            showLeftCoinWindow()
        }

        appbarlayout?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            swipeLayout.isEnabled = verticalOffset >= 0
            if (verticalOffset <= -100) {
                img_top.visibility = View.VISIBLE
            } else if (verticalOffset >= 0) {
                img_top.visibility = View.GONE
            }
        })
        img_top.setOnClickListener { //            appbarlayout.setExpanded(true, true)
            val behavior: CoordinatorLayout.Behavior<*>? = (appbarlayout.layoutParams as CoordinatorLayout.LayoutParams).behavior
            if (behavior is AppBarLayout.Behavior) {
                val topAndBottomOffset = behavior.topAndBottomOffset
                if (topAndBottomOffset != 0) {
                    behavior.topAndBottomOffset = 0
                }
                behavior.setDragCallback(object : DragCallback() {
                    override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                        return true
                    }
                })
            }


        } //去k线图页面
        ib_kline.setOnClickListener {
            if (!CpChainUtil.isFastClick()) {
                val mIntent = Intent(mActivity!!, CpMarketDetail4Activity::class.java)
                mIntent.putExtra(CpParamConstant.symbol, currentSymbol)
                mIntent.putExtra("contractId", mContractId)
                mIntent.putExtra("baseSymbol", base)
                mIntent.putExtra("quoteSymbol", quote)
                mIntent.putExtra("pricePrecision", symbolPricePrecision)
                mIntent.putExtra(CpParamConstant.TYPE, CpParamConstant.BIBI_INDEX)
                startActivity(mIntent)
            }
        } //合约上面的三个点
        iv_more.setOnClickListener {
            CpSlDialogHelper.createContractSettingNew(activity, iv_more, mContractId, "", openContract)
        } //合约计算器
        ib_jisuan.setOnClickListener {
            if (mContractId > 0) {
                CpContractCalculateActivity.show(context as Activity, mContractId, "")
            }
        } //全部
        ll_all_entrust_order.setSafeListener {
            if (!CpClLogicContractSetting.isLogin()) {
                CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
            } else if (openContract == 0) {
                CpDialogUtil.showCreateContractDialog(this.activity!!, object : CpNewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_open_contract_event))
                    }
                })
            } else {
                CpContractEntrustNewActivity.show(mActivity!!, mContractId, mSymbol)
            }
        }

        swipeLayout.setOnRefreshListener {
            loopStart()
        }
        iv_pull_up.setOnClickListener {
            v_kline.visibility = View.GONE
            rl_kline_ctrl.visibility = View.GONE
            iv_pull_up.visibility = View.GONE
            iv_pull_up1.visibility = View.VISIBLE

        }
        iv_pull_up1.setOnClickListener {
            v_kline.visibility = View.VISIBLE
            rl_kline_ctrl.visibility = View.VISIBLE
            iv_pull_up.visibility = View.VISIBLE
            iv_pull_up1.visibility = View.GONE
        }
        setTextConetnt()
        v_kline.hideVolDrawView()
    }

    private fun setTextConetnt() {
        mklineCtrlList.add(CpKlineCtrlBean("15min", CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("15min")), 1))
        mklineCtrlList.add(CpKlineCtrlBean("60min", CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("60min")), 1))
        mklineCtrlList.add(CpKlineCtrlBean("4h", CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("4h")), 1))
        mklineCtrlList.add(CpKlineCtrlBean("1day", CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("1day")), 1))

        if (CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("line"))) {
            mklineCtrlList.add(CpKlineCtrlBean("line", true, 2))
        } else if (CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("1min"))) {
            mklineCtrlList.add(CpKlineCtrlBean("1min", true, 2))
        } else if (CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("5min"))) {
            mklineCtrlList.add(CpKlineCtrlBean("5min", true, 2))
        } else if (CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("30min"))) {
            mklineCtrlList.add(CpKlineCtrlBean("30min", true, 2))
        } else if (CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("1week"))) {
            mklineCtrlList.add(CpKlineCtrlBean("1week", true, 2))
        } else if (CpKLineUtil.getCurTime4Index(type = 0).equals(CpKLineUtil.getKLineScale().indexOf("1month"))) {
            mklineCtrlList.add(CpKlineCtrlBean("1month", true, 2))
        } else {
            mklineCtrlList.add(CpKlineCtrlBean(CpLanguageUtil.getString(activity, "cp_extra_text152"), false, 2))
        }
        mCpContractKlineCtrlAdapter = CpContractKlineCtrlAdapter(mklineCtrlList)
        rv_kline_ctrl.layoutManager = GridLayoutManager(activity, 7)
        rv_kline_ctrl.adapter = mCpContractKlineCtrlAdapter
        mCpContractKlineCtrlAdapter?.setOnItemClickListener { adapter, view, position ->
            for (index in mklineCtrlList.indices) {
                if (position == 4 && index < 4 && mklineCtrlList[index].isSelect) {
                } else {
                    mklineCtrlList[index].isSelect = (index == position)
                }
            }
            mCpContractKlineCtrlAdapter?.notifyDataSetChanged()
            if (position != 4) {
                customize_depth_chart.visibility = if (position == 5) View.VISIBLE else View.GONE
            }
            if (position < 4) {
                v_kline?.setMainDrawLine(false)
                CpKLineUtil.setCurTime(mklineCtrlList[position].time, type = 0)
                CpKLineUtil.setCurTime4KLine(klineScale.indexOf(mklineCtrlList[position].time), type = 0)
                switchKLineScale(mklineCtrlList[position].time)
            }
            if (position != 4) {
                textClickTab(view.findViewById(R.id.tv_time), null)
            }
            if (position == 4) {
                var isSel = false
                CpDialogUtil.createMoreTimeKlinePop(activity, rl_kline_ctrl, object : CpNewDialogUtils.DialogOnSigningItemClickListener {
                    override fun clickItem(position: Int, text: String) {
                        mklineCtrlList[4].time = text
                        for (index in mklineCtrlList.indices) {
                            mklineCtrlList[index].isSelect = (index == 4)
                        }
                        mCpContractKlineCtrlAdapter?.notifyDataSetChanged()
                        customize_depth_chart.visibility = View.GONE
                        v_kline?.setMainDrawLine(position == 0)
                        switchKLineScale(text)
                        isSel = true
                        textClickTab(view.findViewById(R.id.tv_scale), null)
                    }
                }, object : CpNewDialogUtils.DialogOnDismissClickListener {
                    override fun clickItem() {
                        if (isSel) {

                        } else {
                            mklineCtrlList[4].isSelect = false
                        }
                        mCpContractKlineCtrlAdapter?.notifyDataSetChanged()
                    }
                }, type = 0)
            } else {
                mklineCtrlList[4].time = CpLanguageUtil.getString(activity, "cp_extra_text152")
            }
        }
    }

    private fun showLeftCoinWindow() {
        if (CpChainUtil.isFastClick()) return
        val mContractList = CpClLogicContractSetting.getContractJsonListStr(activity)
        if (!TextUtils.isEmpty(mContractList)) {
            var mContractCoinSearchDialog = CpContractCoinSearchDialog()
            var bundle = Bundle()
            bundle.putString("contractList", mContractList)
            mContractCoinSearchDialog.arguments = bundle
            mContractCoinSearchDialog.showDialog(childFragmentManager, "SlContractFragment")
        }
    }

    private var mFragments: ArrayList<Fragment>? = null
    private fun initTabInfo() {
        mFragments = ArrayList()
        /**
         * 当前持仓
         */
        mFragments?.add(CpContractHoldNewFragment())
        mFragments?.add(CpContractCurrentEntrustNewFragment())
        mFragments?.add(CpContractPlanEntrustNewFragment())
        tab_order.setViewPager(vp_order,
            arrayOf(getString(R.string.cp_order_text1), getString(R.string.cp_order_text2), getString(R.string.cp_order_text3)),
            this.activity,
            mFragments)
        vp_order.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position != 0) {
                    if (!CpClLogicContractSetting.isLogin()) {
                        CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
                        return
                    }
                    if (openContract == 0) {
                        CpDialogUtil.showCreateContractDialog(activity!!, object : CpNewDialogUtils.DialogBottomListener {
                            override fun sendConfirm() {
                                CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_open_contract_event))
                            }
                        })
                        return
                    }
                }
            }

        })
    }

    private fun getContractPublicInfo() {
        addDisposable(getContractModel().getPublicInfo(consumer = object : CpNDisposableObserver(mActivity, true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                saveContractPublicInfo(jsonObject)
            }
        }))
    }

    private fun loopStart() {
        loopStop()
        subscribe =
            Observable.interval(0L, CpCommonConstant.capitalRateLoopTime, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                getContractUserConfig()
                getMarkertInfo()
                getMarkertInfo2()
            }
    }

    private fun getContractUserConfig() {
        if (!CpClLogicContractSetting.isLogin()) {
            CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_logout_event))
            tab_order.getTitleView(0).text = getString(R.string.cp_order_text1)
            tab_order.getTitleView(1).text = getString(R.string.cp_order_text2)
            tab_order.getTitleView(2).text = getString(R.string.cp_order_text3)
            v_horizontal_depth.setUserLogout()
            val event = CpMessageEvent(CpMessageEvent.sl_contract_clear_event)
            CpEventBusUtil.post(event)
            return
        }
        addDisposable(getContractModel().getUserConfig(mContractId.toString(), consumer = object : CpNDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                jsonObject.optJSONObject("data").run {
                    openContract = optInt("openContract")
                    couponTag = optInt("couponTag")
                    futuresLocalLimit = optInt("futuresLocalLimit")
                    authLevel = optInt("authLevel")
                    v_horizontal_depth.setLoginContractLayout(CpClLogicContractSetting.isLogin(), openContract == 1)
                    v_horizontal_depth.setUserConfigInfo(this)
                    receiveCoupon()
                }
                if (openContract == 0) {
                    ll_contract_account.visibility = View.GONE
                    val event = CpMessageEvent(CpMessageEvent.sl_contract_clear_event)
                    CpEventBusUtil.post(event)
                } else {
                    ll_contract_account.visibility = View.VISIBLE
                }
                swipeLayout.isRefreshing = false
                getPositionAssetsList()
                getCurrentOrderList()
                getCurrentPlanOrderList()
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                swipeLayout.isRefreshing = false
            }
        }))
    }

    private fun getMarkertInfo() {
        addDisposable(getContractModel().getMarkertInfo(mSymbol, mContractId.toString(), consumer = object : CpNDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                LogUtils.e("合约更新---标记价格")
                jsonObject.optJSONObject("data").run {
                    activity?.runOnUiThread {
                        v_horizontal_depth.setMarkertInfo(this)
                    }
                }
                jsonObject.optJSONObject("data").run {
                    tv_capital_rate?.apply {
                        var tagPrice = optString("tagPrice")
                        var fundRate = optString("currentFundRate")
                        var indexPrice = optString("indexPrice")
                        var obj = JSONObject()
                        obj.put("tagPrice", CpBigDecimalUtils.scaleStr(tagPrice, mPricePrecision))
                        obj.put("indexPrice", CpBigDecimalUtils.scaleStr(indexPrice, mPricePrecision))
                        obj.put("fundRate", "--")
                        val msgEvent = CpMessageEvent(CpMessageEvent.sl_contract_change_tagPrice_event)
                        msgEvent.msg_content = obj
                        CpEventBusUtil.post(msgEvent)
                        text = DecimalFormat("0.000000%").format(optDouble("currentFundRate"))
                    }
                }
            }
        }))
    }

    private fun getPositionAssetsList() {
        if (!CpClLogicContractSetting.isLogin()) return
        if (openContract == 0) return
        Log.d("getPositionAssetsList", "我是7")
        addDisposable(getContractModel().getPositionAssetsList(consumer = object : CpNDisposableObserver(true) {
            @SuppressLint("SetTextI18n")
            override fun onResponseSuccess(jsonObject: JSONObject) {
                jsonObject.optJSONObject("data")?.run {
                    if (!isNull("accountList")) {
                        val mAccountListJson = optJSONArray("accountList")
                        for (i in 0 until mAccountListJson.length()) {
                            val data: JSONObject = mAccountListJson?.get(i) as JSONObject
                            if (data.optString("symbol") == "USDT") {
                                val bibi1 =
                                    CpBigDecimalUtils.showSNormal(CpBigDecimalUtils.divForDown(data?.optString("totalAmount"), 2).toPlainString(), 2)
                                val bibi2 = CpBigDecimalUtils.showSNormal(CpBigDecimalUtils.divForDown(data?.optString("openRealizedAmount"), 2)
                                    .toPlainString(), 2)
                                tv_contract_account_equity.text = bibi1
                                tv_opsition_gain_loss.text = bibi2
                                tv_contract_account_equity.textColor =
                                    CpColorUtil.getMainColorType(bibi1.contains("-"))
                                tv_opsition_gain_loss.textColor =
                                    CpColorUtil.getMainColorType(bibi2.contains("-"))


                            }


                        }
                    }
                    tab_order.getTitleView(0).text = getString(R.string.cp_order_text1) + " " + this.optJSONArray("positionList").length()
                    val msgEvent = CpMessageEvent(CpMessageEvent.sl_contract_refresh_position_list_event)
                    msgEvent.msg_content = this
                    CpEventBusUtil.post(msgEvent)
                    v_horizontal_depth.setUserAssetsInfo(this)
                }
            }
        }))
    }

    private fun getCurrentOrderList() {
        if (!CpClLogicContractSetting.isLogin()) return
        if (openContract == 0) return
        addDisposable(getContractModel().getCurrentOrderListAll(0, 1, consumer = object : CpNDisposableObserver(true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                jsonObject.optJSONObject("data").run {
                    tab_order.getTitleView(1).text = getString(R.string.cp_order_text2) + " " + this.optString("count")
                    val msgEvent = CpMessageEvent(CpMessageEvent.sl_contract_refresh_current_entrust_list_event)
                    msgEvent.msg_content = this
                    CpEventBusUtil.post(msgEvent)
                    v_horizontal_depth.setCurrentOrderJsonInfo(this)
                }
            }
        }))
    }

    private fun getCurrentPlanOrderList() {
        if (!CpClLogicContractSetting.isLogin()) return
        if (openContract == 0) return
        addDisposable(getContractModel().getCurrentPlanOrderListAll(0, 1, consumer = object : CpNDisposableObserver(true) {
            @SuppressLint("SetTextI18n")
            override fun onResponseSuccess(jsonObject: JSONObject) {
                LogUtils.e("合约更新---计划委托")
                jsonObject.optJSONObject("data").run {
                    tab_order.getTitleView(2).text = getString(R.string.cp_order_text3) + " " + this.optString("count")
                    val msgEvent = CpMessageEvent(CpMessageEvent.sl_contract_refresh_plan_entrust_list_event)
                    msgEvent.msg_content = this
                    CpEventBusUtil.post(msgEvent)
                }
            }
        }))
    }

    private fun doCreateContractAccount() {
        if (authLevel != 3) {
            if (authLevel == 0) {
                kycTips(getString(R.string.cl_kyc_4))
            } else if (authLevel == 1) { //审核通过
                if (futuresLocalLimit == 1) { // 区域限制范围内 提示
                    kycTips(getString(R.string.cl_kyc_3))

                } else { // 不在区域限制范围内
                    addDisposable(getContractModel().createContract(consumer = object : CpNDisposableObserver(true) {
                        override fun onResponseSuccess(jsonObject: JSONObject) {
                            getContractUserConfig()
                        }
                    }))
                }
            } else if (authLevel == 2) { //审核不通过
                if (futuresLocalLimit == 1) { // 区域限制范围内 提示
                    kycTips(getString(R.string.cl_kyc_3))
                } else {
                    goKycTips(getString(R.string.cl_kyc_5))
                }

            } else {
                kycTips(getString(R.string.cl_kyc_7))
            }
        } else {
            goKycTips(getString(R.string.cl_kyc_2))
        }
    }

    private fun kycTips(s: String) {
        CpNewDialogUtils.showDialog(context!!, s, true, null, getString(R.string.cp_extra_text27), getString(R.string.cp_overview_text56))
    }

    private fun goKycTips(s: String) {
        CpNewDialogUtils.showDialog(context!!, s.replace("\n", "<br/>"), false, object : CpNewDialogUtils.DialogBottomListener {
            override fun sendConfirm() {
                CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_kyc_page))
            }
        }, getString(R.string.cl_kyc_1), getString(R.string.cl_kyc_6), getString(R.string.cp_overview_text56))
    }

    private fun modifyMarginModel(marginModel: String) {
        addDisposable(getContractModel().modifyMarginModel(mContractId.toString(), marginModel, consumer = object : CpNDisposableObserver(true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                getContractUserConfig()
            }
        }))
    }

    private fun saveContractPublicInfo(jsonObject: JSONObject) {
        jsonObject.optJSONObject("data").run {
            var contractList = optJSONArray("contractList")
            var marginCoinList = optJSONArray("marginCoinList")
            CpClLogicContractSetting.setContractJsonListStr(context, contractList.toString())
            CpClLogicContractSetting.setContractMarginCoinListStr(context, marginCoinList.toString())
            if (contractList.length() == 0) {
                return
            }
            var obj: JSONObject = contractList.get(0) as JSONObject
            val id = CpClLogicContractSetting.getContractCurrentSelectedId(activity)
            for (i in 0..(contractList.length() - 1)) {
                var obj1 = contractList.get(i) as JSONObject
                if (id == obj1.optInt("id")) {
                    obj = (obj1)
                }
            }
            mContractId = obj.optInt("id")
            mSymbol = obj.optString("symbol")
            symbolPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, mContractId)
            showTabInfo(obj)

            //通知子页面更新合约id
            val event = CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_contract_id)
            event.msg_content = mContractId
            CpEventBusUtil.post(event)
            getContractUserConfig() //            //更新k线图为上次的操作记录
            var msgEvent = CpMessageEvent(CpMessageEvent.sl_contract_left_coin_type)
            msgEvent.msg_content = obj
            CpEventBusUtil.post(msgEvent)
        }
    }

    private fun loopStop() {
        if (subscribe != null) {
            subscribe?.dispose()
        }
    }


    override fun onWsMessage(json: String) {
        val jsonObj = JSONObject(json)
        val channel = jsonObj.optString("channel")
        val m24HLinkChannel = WsLinkUtils.tickerFor24HLink(currentSymbol, isChannel = true)
        val mDepthChannel = WsLinkUtils.getDepthLink(currentSymbol, isSub = true, step = depthLevel).channel
        when (channel) {
            mDepthChannel -> {
                activity?.runOnUiThread {
                    v_horizontal_depth.refreshDepthView(jsonObj)
                }
            }
            m24HLinkChannel -> {
                activity?.runOnUiThread {
                    v_horizontal_depth.setTickInfo(jsonObj)
                }
            }
        }
        handleData(json)
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        if (event.msg_type == CpMessageEvent.market_switch_curTime && isShowPage) {
            curTime = event.msg_content as String
            switchKLineScale(curTime ?: "15min") //            tv_scale?.text = curTime ?: "15min"
            calibrationAdapter?.notifyDataSetChanged()

            rv_kline_ctrl.postDelayed(Runnable {
                LogUtils.e("positiongetCurTime-----" + CpKLineUtil.getCurTime())
                val position = CpKLineUtil.getKLineDefaultScale().indexOf(CpKLineUtil.getCurTime())
                LogUtils.e("position-----" + position)
                if (position != -1) {
                    val childView: View = rv_kline_ctrl.getChildAt(position)
                    childView.apply {
                        val tvSale = this.findViewById<TextView>(R.id.tv_time)
                        tvSale?.let { textClickTab(it, null) }
                    }
                } else {
                    val childView: View = rv_kline_ctrl.getChildAt(4)
                    childView.apply {
                        val tvSale = this.findViewById<TextView>(R.id.tv_scale)
                        tvSale?.let { textClickTab(it, null) }
                    }
                }
                mklineCtrlList.clear()
                mklineCtrlList.add(CpKlineCtrlBean("15min", CpKLineUtil.getCurTime().equals("15min"), 1))
                mklineCtrlList.add(CpKlineCtrlBean("60min", CpKLineUtil.getCurTime().equals("60min"), 1))
                mklineCtrlList.add(CpKlineCtrlBean("4h", CpKLineUtil.getCurTime().equals("4h"), 1))
                mklineCtrlList.add(CpKlineCtrlBean("1day", CpKLineUtil.getCurTime().equals("1day"), 1))

                if (CpKLineUtil.getCurTime().equals("line")) {
                    mklineCtrlList.add(CpKlineCtrlBean("line", true, 2))
                } else if (CpKLineUtil.getCurTime().equals("1min")) {
                    mklineCtrlList.add(CpKlineCtrlBean("1min", true, 2))
                } else if (CpKLineUtil.getCurTime().equals("5min")) {
                    mklineCtrlList.add(CpKlineCtrlBean("5min", true, 2))
                } else if (CpKLineUtil.getCurTime().equals("30min")) {
                    mklineCtrlList.add(CpKlineCtrlBean("30min", true, 2))
                } else if (CpKLineUtil.getCurTime().equals("1week")) {
                    mklineCtrlList.add(CpKlineCtrlBean("1week", true, 2))
                } else if (CpKLineUtil.getCurTime().equals("1month")) {
                    mklineCtrlList.add(CpKlineCtrlBean("1month", true, 2))
                } else {
                    mklineCtrlList.add(CpKlineCtrlBean(CpLanguageUtil.getString(activity, "cl_assets_text4"), false, 2))
                }
                mklineCtrlList.add(CpKlineCtrlBean(CpLanguageUtil.getString(activity, "cl_depth_text4"), false, 3))
                mklineCtrlList.add(CpKlineCtrlBean(CpLanguageUtil.getString(activity, "kline_text_scale"), false, 2))
                mCpContractKlineCtrlAdapter?.notifyDataSetChanged()
            }, 300)
        }

        if (event.msg_type == CpMessageEvent.sl_contract_left_coin_type && isShowPage) {
            val ticker = event.msg_content as JSONObject
            showTabInfo(ticker)
            contractId = ticker.getInt("id")
            baseSymbol = ticker.getString("base")
            quoteSymbol = ticker.getString("quote")
            symbol = (ticker.getString("contractType") + "_" + ticker.getString("symbol").replace("-", "")).toLowerCase()
            mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, contractId)
            mMultiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(activity, contractId)

            mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, contractId)

            coUnit = CpClLogicContractSetting.getContractUint(CpMyApp.instance())

            mMultiplier = CpClLogicContractSetting.getContractMultiplierById(activity, contractId)

            tv24hVolUnit =
                if (CpClLogicContractSetting.getContractUint(activity) == 0) " " + getString(R.string.cp_overview_text9) else " " + mMultiplierCoin

            v_kline?.setPricePrecision(mPricePrecision)

            isFrist = true
            klineData.clear()
            getSymbol(symbol)
            collectCoin()
        }

        when (event.msg_type) {
            CpMessageEvent.sl_contract_open_contract_event -> {
                doCreateContractAccount()
            }
            CpMessageEvent.sl_contract_switch_lever_event -> {
                modifyMarginModel(event.msg_content as String)
            }

            CpMessageEvent.sl_contract_create_order_event -> {
                val obj = event.msg_content as CpCreateOrderBean
                addDisposable(getContractModel().createOrder(obj, consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        CpNToastUtil.showTopToastNet(this.mActivity, true, getString(R.string.cp_extra_text53))
                    }
                }))
            }
            CpMessageEvent.sl_contract_req_current_entrust_list_event -> {
                getCurrentOrderList()
                getPositionAssetsList()
            }
            CpMessageEvent.sl_contract_req_plan_entrust_list_event -> {
                getCurrentPlanOrderList()
                getPositionAssetsList()
            }
            CpMessageEvent.sl_contract_req_modify_leverage_event -> {
                modifyLevel(event.msg_content as String)
                v_horizontal_depth.cleanInputData()
            }
            CpMessageEvent.sl_contract_change_unit_event -> {
                v_horizontal_depth.cleanInputData()
                v_horizontal_depth.swicthUnit()
            }
            CpMessageEvent.sl_contract_depth_level_event -> {
                depthLevel = event.msg_content as String
                var para: HashMap<String, Any> = hashMapOf("symbol" to currentSymbol, "step" to depthLevel)
                CpWsContractAgentManager.instance.sendMessage(para, this@CpContractNewTradeFragment)
                Log.d("我是发送消息2", para.toString())
            }
            CpMessageEvent.sl_contract_receive_coupon -> { //领取模拟合约体验金
                receiveCoupon()
            }
            CpMessageEvent.sl_contract_modify_depth_event -> { //处理深度显示条数
                v_horizontal_depth.swicthShowNum(event.msg_content as CpContractBuyOrSellHelper)
            }
            CpMessageEvent.sl_contract_change_position_model_event -> {
                v_horizontal_depth.cleanInputData()
                v_horizontal_depth.swicthUnit()
            }
        }
    }

    private fun receiveCoupon() {
        if (!CpClLogicContractSetting.isLogin()) return
        if (openContract == 0) return
        if (!contractType.equals("S")) return
        if (couponTag == 1) return
        addDisposable(getContractModel().receiveCoupon(consumer = object : CpNDisposableObserver(true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                getContractUserConfig()
            }
        }))
    }

    private fun modifyLevel(s: String) {
        addDisposable(getContractModel().modifyLevel(mContractId.toString(), s, consumer = object : CpNDisposableObserver(true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                getContractUserConfig()
            }
        }))
    }

    private fun showTabInfo(obj: JSONObject) {
        base = obj.getString("base")
        quote = obj.getString("quote")
        mContractId = obj.getInt("id")
        mSymbol = obj.optString("symbol")
        contractType = obj.getString("contractType")
        currentSymbol = (obj.getString("contractType") + "_" + obj.getString("symbol").replace("-", "")).toLowerCase()
        depthLevel = "0"
        tv_contract.text = CpClLogicContractSetting.getContractShowNameById(activity, mContractId)
        v_horizontal_depth.setContractJsonInfo(obj) //        var para: HashMap<String, Any> = hashMapOf("symbol" to currentSymbol, "step" to depthLevel)
        //        CpWsContractAgentManager.instance.sendMessage(para, this@CpContractNewTradeFragment)
        //        Log.d("我是发送消息1",para.toString())

        //通知子页面更新合约id
        val event = CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_contract_id)
        event.msg_content = mContractId
        CpEventBusUtil.post(event)
    }

    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
        if (isVisible) {
            isShowPage = true
            initResumeData()
        } else {
            isShowPage = false
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isContractHidden = hidden
        if (hidden) {
            isShowPage = false
            loopStop()
        } else {
            isShowPage = true
        }
    }


    private fun initResumeData() {
        LogUtils.e("合约更新---onVisibleChanged")
        loopStart()
        isContractFirst = true
        getContractPublicInfo()
        v_horizontal_depth.setLoginContractLayout(CpClLogicContractSetting.isLogin(), openContract == 1)

        //ybc  这个如果放在onCreate里面，会先调用这个方法，再去刷新adapter，导致getChildAt报空指针异常
        rv_kline_ctrl.postDelayed(Runnable {
            try {
                val position = CpKLineUtil.getKLineDefaultScale().indexOf(CpKLineUtil.getCurTime())
                if (position != -1) {
                    val childView: View = rv_kline_ctrl.getChildAt(position)
                    childView.apply {
                        val tvSale = this.findViewById<TextView>(R.id.tv_time)
                        tvSale?.let { textClickTab(it, null) }
                    }
                } else {
                    val childView: View = rv_kline_ctrl.getChildAt(4)
                    childView.apply {
                        val tvSale = this.findViewById<TextView>(R.id.tv_scale)
                        tvSale?.let { textClickTab(it, null) }
                    }
                }
            } catch (e: Exception) {

            }
        }, 300)

        initData()
        CpWsContractAgentManager.instance.changeKlineKey(this.javaClass.simpleName)
        cur_time_index = CpKLineUtil.getCurTime4Index(type = 0)

        curTime = CpKLineUtil.getCurTime()
        v_kline?.setMainDrawLine(CpKLineUtil.getCurTime4Index(type = 0) == 0) //        tv_scale?.text = curTime
        isFrist = true
        klineData.clear()

        if (!hasInit) {
            initView2()
            setDepthSymbol()
            hasInit = true
        }
        initSocket()
        collectCoin()
    }

    override fun onResume() {
        super.onResume()
        isShowPage = true
        initResumeData()
    }


    override fun onPause() {
        super.onPause()
        loopStop()
        isShowPage = false
        CpWsContractAgentManager.instance.removeWsCallback(this)
        CpWsContractAgentManager.instance.unbind(this, true)
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
            curTime = kLineScale
            var scale2 = if (curTime == "line") "1min" else curTime
            initSocket()
        }
    }

    //初次 和 切换币对时触发
    private fun initSocket() {
        if (isNotEmpty(symbol)) { // sub ticker
            val scale: String = if (curTime == "line") "1min" else curTime ?: "15min"
            CpWsContractAgentManager.instance.sendMessage(hashMapOf("symbol" to symbol, "line" to scale), this)
            Log.d("我是发送消息2", hashMapOf("symbol" to symbol, "line" to scale).toString())
        }
    }

    fun isNotEmpty(str: String?): Boolean {
        return !isEmpty(str)
    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

    fun textClickTab(textView: TextView, view: View?) {
        val iArr = IntArray(2)
        textView.getLocationInWindow(iArr)
        if (!this.aG) {
            this.aG = true
            textView.post { changeTabIndicatorIng(textView, iArr) }
            return
        }
        LogUtils.e(TAG, "textClickTab ${iArr[0]}")
        this.kline_tab_indicator.animate().translationX(iArr[0].toFloat())
        val ofInt = ValueAnimator.ofInt(this.kline_tab_indicator.width, textView.width / 2)
        ofInt.addUpdateListener { valueAnimator -> changeTabIndicator(valueAnimator) }
        ofInt.start()

    }

    fun changeTabIndicatorIng(textView: TextView, iArr: IntArray) {
        textView.getLocationInWindow(iArr)
        this.kline_tab_indicator.setTranslationX(iArr[0].toFloat())
        val layoutParams = this.kline_tab_indicator.layoutParams
        layoutParams.width = textView.width / 2
        LogUtils.e(TAG, "changeTabIndicatorIng ${textView.width}")
        this.kline_tab_indicator.layoutParams = layoutParams
    }

    fun changeTabIndicator(valueAnimator: ValueAnimator) {
        val layoutParams = this.kline_tab_indicator.layoutParams
        layoutParams.width = (valueAnimator.animatedValue as Int).toInt()
        LogUtils.i(TAG, "changeTabIndicator ${valueAnimator.animatedValue}")
        this.kline_tab_indicator.layoutParams = layoutParams
    }

    private fun initData() {
        initCoinData()
        initKLineData()
    }

    /**
     * 初始化币对数据
     */
    private fun initCoinData() {
        symbol = currentSymbol
        contractId = mContractId
        baseSymbol = base
        quoteSymbol = quote
    }

    /**
     * todo 待优化
     * 更新深度数据。liveData方式，是否更新修改为其他方式
     */
    private fun setDepthSymbol() {
        CpDepthFragment.liveData.value = CpFlagBean(isContract = true,
            contractId = contractId.toString(),
            symbol = symbol,
            baseSymbol = baseSymbol!!,
            quotesSymbol = quoteSymbol!!,
            pricePrecision = mPricePrecision,
            volumePrecision = if (coUnit == 0) 0 else mMultiplierPrecision,
            mMultiplier = mMultiplier,
            coUnit = coUnit)
    }

    private fun initView2() {
        mPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, contractId)
        coUnit = CpClLogicContractSetting.getContractUint(CpMyApp.instance())
        mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(activity, contractId)
        mMultiplier = CpClLogicContractSetting.getContractMultiplierById(activity, contractId)
        mMultiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(activity, contractId)
        tv24hVolUnit =
            if (CpClLogicContractSetting.getContractUint(activity) == 0) " " + getString(R.string.cp_overview_text9) else " " + mMultiplierCoin

        v_kline?.adapter = adapter
        v_kline?.startAnimation()
        v_kline?.justShowLoading()
        v_kline?.setPricePrecision(mPricePrecision)
        CpNLiveDataUtil.observeForeverData {
            if (null != it && CpMessageEvent.symbol_switch_type == it.msg_type) {
                if (null != it.msg_content) {
                    symbol = it.msg_content as String
                    isFrist = true
                    klineData.clear()
                    getSymbol(symbol)
                }
            }
        }
        initKLineScale()
        action4KLineIndex()
        initDepthChart()
    }

    fun getSymbol(symbol: String) {
        setDepthSymbol()
        initSocket()
    }

    /**
     * 处理K线刻度
     */
    private fun initKLineScale() {
        rv_kline_scale?.isLayoutFrozen = true
        rv_kline_scale?.setHasFixedSize(true) //        tv_scale?.text = if (cur_time_index == 0) "line" else curTime

        val layoutManager = GridLayoutManager(mActivity, 4)
        layoutManager.isAutoMeasureEnabled = false
        rv_kline_scale?.layoutManager = layoutManager
        calibrationAdapter = CpKLineScaleAdapter(klineScale)
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
            if (position != CpKLineUtil.getCurTime4Index(type = 0)) {
                for (i in 0 until klineScale.size) {
                    val boxView = viewHolder?.getViewByPosition(i, R.id.cbtn_view) as CpCustomCheckBoxView
                    boxView.setCenterColor(CpColorUtil.getColor(R.color.normal_text_color))
                    boxView.setCenterSize(12f)
                    boxView.setIsNeedDraw(false)
                    boxView.isChecked = false
                }
                val boxView = viewHolder?.getViewByPosition(position, R.id.cbtn_view) as CpCustomCheckBoxView
                boxView.isChecked = true
                boxView.setIsNeedDraw(true)
                boxView.setCenterSize(12f)
                boxView.setCenterColor(CpColorUtil.getColor(R.color.text_color))
                CpKLineUtil.setCurTime4KLine(position, type = 0)
                CpKLineUtil.setCurTime(klineScale[position], type = 0)
                switchKLineScale(klineScale[position])

                //                tv_scale?.text = if (position == 0) "line" else curTime

            } else {
                val boxView = viewHolder?.getViewByPosition(position, R.id.cbtn_view) as CpCustomCheckBoxView
                boxView.isChecked = true
                boxView.setIsNeedDraw(true)
                boxView.setCenterSize(12f)
                boxView.setCenterColor(CpColorUtil.getColor(R.color.text_color))
                CpKLineUtil.setCurTime4KLine(position, type = 0)
                CpKLineUtil.setCurTime(klineScale[position], type = 0)
                switchKLineScale(klineScale[position])
            }
        }
    }

    /**
     * K线的指标处理
     */
    private fun action4KLineIndex() {
        when (main_index) {
            MainKlineViewStatus.MA.status -> {
                v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
                CpKLineUtil.setMainIndex(MainKlineViewStatus.MA.status, type = 0)
            }

            MainKlineViewStatus.BOLL.status -> {
                v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
                CpKLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status, type = 0)
            }
        }

        when (vice_index) {
            CpViceViewStatus.MACD.status -> {
                v_kline?.setChildDraw(0)
                CpKLineUtil.setViceIndex(CpViceViewStatus.MACD.status, type = 0)
            }

            CpViceViewStatus.KDJ.status -> {
                v_kline?.setChildDraw(1)
                CpKLineUtil.setViceIndex(CpViceViewStatus.KDJ.status, type = 0)
            }

            CpViceViewStatus.RSI.status -> {
                v_kline?.setChildDraw(2)
                CpKLineUtil.setViceIndex(CpViceViewStatus.RSI.status, type = 0)
            }

            CpViceViewStatus.WR.status -> {
                v_kline?.setChildDraw(3)
                CpKLineUtil.setViceIndex(CpViceViewStatus.WR.status, type = 0)
            }
        }

        mainViewStatusViews.forEach {
            it?.setOnClickListener {
                val index = mainViewStatusViews.indexOf(it)
                (mainViewStatusViews[0] as CpLabelRadioButton?)?.isLabelEnable = (index == 0)
                (mainViewStatusViews[1] as CpLabelRadioButton?)?.isLabelEnable = (index == 1)
                mainViewStatusViews[2]?.isChecked = (index == 2)
                when (index) {
                    MainKlineViewStatus.MA.status -> {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
                        CpKLineUtil.setMainIndex(MainKlineViewStatus.MA.status, type = 0)
                    }

                    MainKlineViewStatus.BOLL.status -> {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
                        CpKLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status, type = 0)
                    }

                    MainKlineViewStatus.NONE.status -> {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.NONE)
                        CpKLineUtil.setMainIndex(MainKlineViewStatus.NONE.status, type = 0)
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
                (viceViewStatusViews[0] as CpLabelRadioButton?)?.isLabelEnable = (index == 0)
                (viceViewStatusViews[1] as CpLabelRadioButton?)?.isLabelEnable = (index == 1)
                (viceViewStatusViews[2] as CpLabelRadioButton?)?.isLabelEnable = (index == 2)
                (viceViewStatusViews[3] as CpLabelRadioButton?)?.isLabelEnable = (index == 3)
                viceViewStatusViews[4]?.isChecked = (index == 4)

                when (index) {
                    CpViceViewStatus.MACD.status -> {
                        v_kline?.setChildDraw(0)
                        CpKLineUtil.setViceIndex(CpViceViewStatus.MACD.status, type = 0)
                    }

                    CpViceViewStatus.KDJ.status -> {
                        v_kline?.setChildDraw(1)
                        CpKLineUtil.setViceIndex(CpViceViewStatus.KDJ.status, type = 0)
                    }

                    CpViceViewStatus.WR.status -> {
                        v_kline?.setChildDraw(2)
                        CpKLineUtil.setViceIndex(CpViceViewStatus.WR.status, type = 0)
                    }

                    CpViceViewStatus.RSI.status -> {
                        v_kline?.setChildDraw(3)
                        CpKLineUtil.setViceIndex(CpViceViewStatus.RSI.status, type = 0)
                    }
                    else -> {
                        v_kline?.hideChildDraw()
                        CpKLineUtil.setViceIndex(CpViceViewStatus.NONE.status, type = 0)
                    }
                }

            }
        }
    }

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
                    val quotesData = CpJsonUtils.convert2Quote(jsonObj.toString())
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

                    doAsync {
                        /**
                         * 这里需要处理：重复添加的问题
                         */
                        val kLineEntity = CpKLineBean()
                        val data = jsonObj.optJSONObject("tick")
                        kLineEntity.id = data.optLong("id")
                        val open = if (coUnit == 0) data.optString("open") else CpBigDecimalUtils.showSNormal(data.optString("open"), mPricePrecision)
                        val close =
                            if (coUnit == 0) data.optString("close") else CpBigDecimalUtils.showSNormal(data.optString("close"), mPricePrecision)
                        val high = if (coUnit == 0) data.optString("high") else CpBigDecimalUtils.showSNormal(data.optString("high"), mPricePrecision)
                        val low = if (coUnit == 0) data.optString("low") else CpBigDecimalUtils.showSNormal(data.optString("low"), mPricePrecision)
                        val vol = if (coUnit == 0) data.optString("vol") else CpBigDecimalUtils.mulStr(data.optString("vol"),
                            mMultiplier,
                            mMultiplierPrecision)

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
                                    CpDataManager.calculate(klineData)
                                    adapter.changeItem(klineData.lastIndex, kLineEntity)
                                } else {
                                    klineData.add(kLineEntity)
                                    CpDataManager.calculate(klineData)
                                    uiThread {
                                        adapter.addFooterData(klineData)
                                        v_kline?.refreshEnd()
                                    }
                                }
                            } else {
                                klineData.add(kLineEntity)
                                CpDataManager.calculate(klineData)
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
    private fun render24H(tick: KlineTick) {
        val price = mPricePrecision

        CpDepthFragment.liveData4closePrice.postValue(arrayListOf(tick.close, price.toString(), symbol))
    }

    /**
     * 处理K线历史数据
     * @param data K线历史数据
     */
    private fun handlerKLineHistory(data: String) {
        doAsync {
            val json = JSONObject(data)
            val type = object : TypeToken<ArrayList<CpKLineBean>>() {}.type
            val typeNew = object : TypeToken<CpKLineBean>() {}.type
            val gson = GsonBuilder().setPrettyPrinting().create()
            if (isFrist) {
                klineData.clear()
                var objKline = json.getJSONArray("data")
                if (objKline.length() > 1) {
                    for (i in 0 until objKline.length()) {
                        var obj: JSONObject = objKline.get(i) as JSONObject
                        val mKLineBean: CpKLineBean = gson.fromJson(obj.toString(), typeNew)
                        if (coUnit != 0) {
                            mKLineBean.volume = CpBigDecimalUtils.mulStr(mKLineBean.volume.toString(), mMultiplier, mMultiplierPrecision).toFloat()
                            mKLineBean.openPrice = CpBigDecimalUtils.showSNormal(mKLineBean.openPrice.toString(), mPricePrecision).toFloat()
                            mKLineBean.closePrice = CpBigDecimalUtils.showSNormal(mKLineBean.closePrice.toString(), mPricePrecision).toFloat()
                            mKLineBean.highPrice = CpBigDecimalUtils.showSNormal(mKLineBean.highPrice.toString(), mPricePrecision).toFloat()
                            mKLineBean.lowPrice = CpBigDecimalUtils.showSNormal(mKLineBean.lowPrice.toString(), mPricePrecision).toFloat()
                        }
                        klineData.add(mKLineBean)
                    }
                }

                if (klineData.size == 0) {
                    initKlineData()
                } else {
                    val scale = if (curTime == "line") "1min" else curTime
                    CpWsContractAgentManager.instance.sendData(WsLinkUtils.getKlineHistoryOther(symbol, scale, klineData[0].id.toString()))
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
        activity?.runOnUiThread {
            CpDataManager.calculate(klineData)
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
        activity?.runOnUiThread {
            val scale = if (curTime == "line") "1min" else curTime
            CpWsContractAgentManager.instance.sendData(WsLinkUtils.getKlineNewLink(symbol, scale))
        }

    }

    private fun realtData(jsonObj: JSONObject, data: String) {
        val depthReal =
            jsonObj.getString("channel") == WsLinkUtils.getDealHistoryLink(symbol).channel || jsonObj.getString("channel") == WsLinkUtils.getDealNewLink(
                symbol).channel
        if (depthReal) {
            if (dealtRecordFragment != null) {
                if (!isRealNew) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)     //延时500ms
                        dealtRecordFragment?.onCallback(data)
                        CpWsContractAgentManager.instance.sendData(WsLinkUtils.getDealNewLink(symbol).json)
                        isRealNew = true
                    }
                } else {
                    dealtRecordFragment?.onCallback(data)
                }
            }
        }
    }

    /**
     * 配置深度图的基本属性
     */
    @SuppressLint("NewApi")
    private fun initDepthChart() {
        depth_chart?.setNoDataText(getString(R.string.cp_extra_text52))
        depth_chart?.setNoDataTextColor(resources.getColor(R.color.normal_text_color))
        depth_chart?.setTouchEnabled(true)

        v_buy_tape?.backgroundColor = CpColorUtil.getMainColorType()
        v_sell_tape?.backgroundColor = CpColorUtil.getMainColorType(isRise = false)
        tv_buy_tape_title?.textColor = CpColorUtil.getMainColorType()
        tv_sell_tape_title?.textColor = CpColorUtil.getMainColorType(isRise = false)

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
        xAxis.setLabelCount(3, true) // 不绘制竖直方向的方格线
        xAxis.setDrawGridLines(false)
        xAxis.axisLineWidth = 0.5f //x坐标轴不可见
        xAxis.isEnabled = true //禁止x轴底部标签
        xAxis.setDrawLabels(true) //最小的间隔设置
        xAxis.textColor = CpColorUtil.getColor(R.color.normal_text_color)
        xAxis.textSize = 10f //在绘制时会避免“剪掉”在x轴上的图表或屏幕边缘的第一个和最后一个坐标轴标签项。
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.setDrawAxisLine(false)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toString().toTextPrice(mPricePrecision)
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
        yAxis.setDrawAxisLine(false) //设置Y轴的Label显示在图表的内侧还是外侧，默认外侧
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART) //不绘制水平方向的方格线
        yAxis.textColor = CpColorUtil.getColor(R.color.normal_text_color)
        yAxis.textSize = 10f //设置Y轴显示的label个数
        yAxis.setLabelCount(6, true) //控制上下左右坐标轴显示的距离
        depth_chart?.setViewPortOffsets(0f, 20f, 0f, CpDisplayUtil.dip2px(14f))
        yAxis.valueFormatter = CpDepthYValueFormatter()


        depth_chart?.setOnClickListener {
            if (depth_chart.marker != null) {
                depth_chart.marker = null
            }
        }
        depth_chart?.setOnLongClickListener {
            val mv = CpDepthMarkView(activity!!, R.layout.cp_layout_depth_marker)
            mv.chartView = depth_chart // For bounds control
            depth_chart?.marker = mv // Set the marker to the ch
            false
        }
    }

    private fun getMarkertInfo2() {
        if (contractId == -1) {
            return
        }

        addDisposable(getContractModel().getCoinDepth(contractId, symbol, consumer = object : CpNDisposableObserver(true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                jsonObject.optJSONObject("data")?.run {
                    datas = Gson().fromJson<DepthItem>(this.toString(), DepthItem::class.java)
                    this@CpContractNewTradeFragment.activity?.runOnUiThread {
                        setData4DepthChart()
                    }
                }
            }
        }))
    }

    /**
     * 设置深度图数据
     */
    private fun setData4DepthChart() {

        if (datas == null) {
            clearDepthChart()
            return
        }
        datas = datas?.parseDepth()
        val sellList = ArrayList<CpDepthBean>()
        val buyList = ArrayList<CpDepthBean>()

        sellList.addAll(datas?.sellItem!! as ArrayList<CpDepthBean>)
        buyList.addAll(datas?.buyItem!! as ArrayList<CpDepthBean>)
        if (sellList.size == 0 || buyList.size == 0) {
            return
        }
        val yData = arrayListOf<Entry>()
        var buyVolumeSum = 0.0 // TODO 优化
        for (i in buyList.indices) {
            buyVolumeSum = CpBigDecimalUtils.add(buyVolumeSum.toString(), buyList[i].vol).toDouble()
            val volSum = buyList[i].sum.toFloat()
            val entry = Entry((buyList[i].price).toFloat(), volSum, buyList[i].price.toTextPrice(mPricePrecision))
            yData.add(0, entry)
        }
        /*************处理卖盘数据*********/
        var sellVolumeSum = 0.0
        val sellYData = ArrayList<Entry>()
        for (i in sellList.indices) {
            sellVolumeSum = CpBigDecimalUtils.add(sellVolumeSum.toString(), sellList[i].vol).toDouble()
            val volSum = sellList[i].sum.toFloat()
            val entry = Entry(sellList[i].price.toFloat(), volSum, sellList[i].price.toTextPrice(mPricePrecision))
            sellYData.add(entry)
        }

        /**
         * Y 轴最大值 和 最小值
         */
        var maxVolume: Float = max(buyList.get(buyList.size - 1).sum.toFloat(), sellList.get(sellList.size - 1).sum.toFloat())
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

    /**
     * 清理深度
     */
    fun clearDepthChart() {
        depth_chart?.clear()
        depth_chart?.notifyDataSetChanged()
        depth_chart?.invalidate()
    }

    /**
     * 设置lineDataSet  in深度图
     */
    private fun lineDataSet(yData: ArrayList<Entry>, isBuy: Boolean): LineDataSet {
        val lineDataSet: LineDataSet?
        if (isBuy) {
            lineDataSet = LineDataSet(yData, "Buy")
            lineDataSet.color = CpColorUtil.getMainColorType()
            lineDataSet.fillColor = CpColorUtil.getMainColorType()
            /**
             * 设置折线的颜色
             */
            lineDataSet.color = CpColorUtil.getMainColorType()

        } else {
            lineDataSet = LineDataSet(yData, "Sell")
            lineDataSet.color = CpColorUtil.getMainColorType(isRise = false)
            lineDataSet.fillColor = CpColorUtil.getMainColorType(isRise = false)
            /**
             * 设置折线的颜色
             */
            lineDataSet.color = CpColorUtil.getMainColorType(isRise = false)
        }
        /**
         * 是否填充折线以及填充色设置
         */
        lineDataSet.setDrawFilled(true)

        /**
         * 控制MarkView的显示与隐藏
         * 点击是否显示高亮线
         */
        lineDataSet.isHighlightEnabled = true
        lineDataSet.highLightColor = Color.TRANSPARENT


        /**
         * 设置折线的宽度
         */
        lineDataSet.lineWidth = 2.0f


        /**
         * 隐藏每个数据点的值
         */
        lineDataSet.setDrawValues(false)

        /**
         * 数据点是否用小圆圈表示
         */
        lineDataSet.setDrawCircles(false)
        return lineDataSet
    }

    override fun onDestroy() {
        super.onDestroy()
        CpWsContractAgentManager.instance.removeWsCallback(this)
        loopStop()
        isShowPage = false
    }

    ////ybc start
    /*
     * 获取服务器用户的自选币对数据
     */
    var serverSelfSymbols = ArrayList<String>()
    var sync_status = ""

    /**
     * 添加收藏
     */
    private var operationType = 0
    val getUserSelfDataReqType = 2 // 服务器用户自选数据
    val addCancelUserSelfDataReqType = 1 // 服务器用户自选数据

    private fun collectCoin() {
        serverSelfSymbols.clear()
        /**
         * 根据是否存在于"自选"列表中
         */
        EventBus.getDefault().post(CpCollectionEvent(CpCollectionEvent.TYPE_REQUEST))/*if (isLogined && isOptionalSymbolServerOpen) {
            getOptionalSymbol()
        } else {
            var hasCollect = LikeDataService.getInstance().hasCollect(symbol)
            showImgCollect(hasCollect, false, false)
        }*/

        ib_collect?.setOnClickListener {
            if (serverSelfSymbols.contains("e-" + baseSymbol.lowercase() + "-" + quoteSymbol.lowercase())) {
                operationType = 2
            } else {
                operationType = 1
            } //addOrDeleteSymbol(operationType, symbol)
            EventBus.getDefault()
                .post(CpCollectionEvent(CpCollectionEvent.TYPE_ADD_DEL, operationType, "e-" + baseSymbol.lowercase() + "-" + quoteSymbol.lowercase()))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCpCollectionEvent2(event: CpCollectionEvent2) {
        Log.e("yubch", "bbb:" + event.type)
        if (!isShowPage) {
            return
        }
        if (getUserSelfDataReqType == event.type) {
            showServerSelfSymbols(event.jsonObject.optJSONObject("data"))
        } else if (addCancelUserSelfDataReqType == event.type) {
            var hasCollect = false
            if (operationType == 2) {
                serverSelfSymbols.remove("e-" + baseSymbol.lowercase() + "-" + quoteSymbol.lowercase())
            } else {
                hasCollect = true
                serverSelfSymbols.add("e-" + baseSymbol.lowercase() + "-" + quoteSymbol.lowercase())
            }
            showImgCollect(hasCollect, true, true)
        }
    }

    /*
    * 收藏图标状态及其行为处理
    */
    private fun showImgCollect(hasCollect: Boolean, isShowToast: Boolean, isAddRemove: Boolean) {
        if (hasCollect) {
            ib_collect?.setImageResource(R.drawable.quotes_optional_selected2)
            if (isShowToast) {
                CpNToastUtil.showTopToastNet(mActivity, true, CpLanguageUtil.getString(activity, "kline_tip_addCollectionSuccess"))
            }

        } else {
            ib_collect?.setImageResource(R.drawable.quotes_optional_default2)
            if (isShowToast) {
                CpNToastUtil.showTopToastNet(mActivity, true, CpLanguageUtil.getString(activity, "kline_tip_removeCollectionSuccess"))
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
        if (serverSelfSymbols.contains("e-" + baseSymbol.lowercase() + "-" + quoteSymbol.lowercase())) {
            ib_collect?.setImageResource(R.drawable.quotes_optional_selected2)
        } else {
            ib_collect?.setImageResource(R.drawable.quotes_optional_default2)
        }
    }
}