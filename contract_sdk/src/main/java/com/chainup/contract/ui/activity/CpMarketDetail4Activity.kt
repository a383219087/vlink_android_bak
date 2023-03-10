package com.chainup.contract.ui.activity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.RadioButton
import android.widget.TextView
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
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.CpDepthBean
import com.chainup.contract.bean.CpFlagBean
import com.chainup.contract.bean.KlineQuotesData
import com.chainup.contract.bean.KlineTick
import com.chainup.contract.eventbus.*
import com.chainup.contract.ui.fragment.CpContractCoinSearchDialog
import com.chainup.contract.utils.*
import com.chainup.contract.view.*
import com.chainup.contract.ws.CpWsContractAgentManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.yjkj.chainup.bean.kline.cp.DepthItem
import com.yjkj.chainup.manager.CpLanguageUtil
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.adapter.CpContractKlineCtrlAdapter
import com.yjkj.chainup.new_contract.bean.CpKlineCtrlBean
import com.chainup.contract.ui.fragment.CpDealtRecordFragment
import com.chainup.contract.ui.fragment.CpDepthFragment
import com.chainup.contract.kline.bean.CpKLineBean
import com.chainup.contract.kline.data.CpDataManager
import com.chainup.contract.kline.data.CpKLineChartAdapter
import com.chainup.contract.kline.view.MainKlineViewStatus
import com.chainup.contract.kline.view.vice.CpViceViewStatus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.cp_activity_market_detail4.*
import kotlinx.android.synthetic.main.cp_depth_chart_com.*

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
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * @description : ???????????????????????????
 * @date 2019-3-2
 * @author Bertking
 *
 */
class CpMarketDetail4Activity : CpNBaseActivity(), CpWsContractAgentManager.WsResultCallback {
    override fun setContentView(): Int = R.layout.cp_activity_market_detail4
    var subscribe: Disposable? = null//???????????????
    var isFrist = true
    var baseSymbol = ""
    var quoteSymbol = ""
    var symbol = ""
    var type = CpParamConstant.BIBI_INDEX

    /**
     * ????????????orScale
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

    var klineData: ArrayList<CpKLineBean> = arrayListOf()
    private val adapter by lazy { CpKLineChartAdapter() }


    private var mCpContractKlineCtrlAdapter: CpContractKlineCtrlAdapter? = null
    private var mklineCtrlList = ArrayList<CpKlineCtrlBean>()

    /**
     * ??????????????????view
     */
    private val mainViewStatusViews: ArrayList<RadioButton?> by lazy(LazyThreadSafetyMode.NONE) {
        arrayListOf<RadioButton?>(rb_ma, rb_boll)
    }

    /**
     * ??????????????????view
     */
    private val viceViewStatusViews: ArrayList<RadioButton?> by lazy(LazyThreadSafetyMode.NONE) {
        arrayListOf<RadioButton?>(rb_macd, rb_kdj, rb_wr, rb_rsi)
    }

    private var datas: DepthItem? = null

    companion object {
        /**
         * K?????????
         */
        const val KLINE_SCALE = 1

        /**
         * K?????????
         */
        const val KLINE_INDEX = 2

    }

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        CpWsContractAgentManager.instance.addWsCallback(this)
//        initSocket()
        showLoadingDialog()
        setOnclick()
        setTextConetnt()
        collectCoin()
    }

    fun setTextConetnt() {
        tv_cp_extra_text110?.text = CpLanguageUtil.getString(this, "cp_extra_text110")
        tv_clean_title?.text = CpLanguageUtil.getString(this, "etf_text_networth")
        tv_high?.text = CpLanguageUtil.getString(this, "cp_extra_text111")
//        tv_common_text_dayVolume?.text = CpLanguageUtil.getString(this, "common_text_dayVolume")
//        tv_cp_extra_text112?.text = LanguageUtil.getString(this, "cp_extra_text112")
//        tv_indicator?.text = CpLanguageUtil.getString(this, "kline_text_scale")

        mklineCtrlList.add(
            CpKlineCtrlBean(
                "15min",
                CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("15min")),
                1
            )
        )
        mklineCtrlList.add(
            CpKlineCtrlBean(
                "60min",
                CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("60min")),
                1
            )
        )
        mklineCtrlList.add(
            CpKlineCtrlBean(
                "4h",
                CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("4h")),
                1
            )
        )
        mklineCtrlList.add(
            CpKlineCtrlBean(
                "1day",
                CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("1day")),
                1
            )
        )

        if (CpKLineUtil.getCurTime4Index() == CpKLineUtil.getKLineScale().indexOf("line")) {
            mklineCtrlList.add(CpKlineCtrlBean("line", true, 2))
        } else if (CpKLineUtil.getCurTime4Index() == CpKLineUtil.getKLineScale().indexOf("1min")) {
            mklineCtrlList.add(CpKlineCtrlBean("1min", true, 2))
        } else if (CpKLineUtil.getCurTime4Index() == CpKLineUtil.getKLineScale().indexOf("5min")) {
            mklineCtrlList.add(CpKlineCtrlBean("5min", true, 2))
        } else if (CpKLineUtil.getCurTime4Index() == CpKLineUtil.getKLineScale().indexOf("30min")) {
            mklineCtrlList.add(CpKlineCtrlBean("30min", true, 2))
        } else if (CpKLineUtil.getCurTime4Index()
                .equals(CpKLineUtil.getKLineScale().indexOf("1week"))
        ) {
            mklineCtrlList.add(CpKlineCtrlBean("1week", true, 2))
        } else if (CpKLineUtil.getCurTime4Index()
                .equals(CpKLineUtil.getKLineScale().indexOf("1month"))
        ) {
            mklineCtrlList.add(CpKlineCtrlBean("1month", true, 2))
        } else {
            mklineCtrlList.add(
                CpKlineCtrlBean(
                    CpLanguageUtil.getString(this, "cp_extra_text152"),
                    false,
                    2
                )
            )
        }
        mklineCtrlList.add(
            CpKlineCtrlBean(
                CpLanguageUtil.getString(this, "cp_extra_text153"),
                false,
                3
            )
        )
//        mklineCtrlList.add(
//            CpKlineCtrlBean(
//                CpLanguageUtil.getString(this, "cp_extra_text154"),
//                false,
//                2
//            )
//        )
        mCpContractKlineCtrlAdapter = CpContractKlineCtrlAdapter(mklineCtrlList)
        rv_kline_ctrl.layoutManager = GridLayoutManager(this, 7)
        rv_kline_ctrl.adapter = mCpContractKlineCtrlAdapter


        mCpContractKlineCtrlAdapter?.setOnItemClickListener { adapter, view, position ->
            for (index in mklineCtrlList.indices) {
                if (position == 6 && mklineCtrlList[index].isSelect) {

                } else {
                    if (position == 4 && index < 4 && mklineCtrlList[index].isSelect) {

                    } else {
                        if (position == 4 && mklineCtrlList[5].isSelect) {
                            mklineCtrlList[index].isSelect
                        } else {
                            mklineCtrlList[index].isSelect = (index == position)
                        }
                    }
                }
            }
            mCpContractKlineCtrlAdapter?.notifyDataSetChanged()
            if (position != 6 && position != 4) {
                customize_depth_chart.visibility = if (position == 5) View.VISIBLE else View.GONE
            }
            if (position < 4) {
                v_kline?.setMainDrawLine(false)
                CpKLineUtil.setCurTime(mklineCtrlList[position].time)
                CpKLineUtil.setCurTime4KLine(klineScale.indexOf(mklineCtrlList[position].time))
                switchKLineScale(mklineCtrlList[position].time)
            }
            if (position != 4 && position != 6) {
                textClickTab(view.findViewById(R.id.tv_time), null)
            }
            if (position == 4) {
                mklineCtrlList[6].isSelect = false
                var isSel = false
                CpDialogUtil.createMoreTimeKlinePop(
                    this,
                    rl_kline_ctrl,
                    object : CpNewDialogUtils.DialogOnSigningItemClickListener {
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
                    },
                    object : CpNewDialogUtils.DialogOnDismissClickListener {
                        override fun clickItem() {
                            if (isSel) {

                            } else {
                                mklineCtrlList[4].isSelect = false
                            }
                            mCpContractKlineCtrlAdapter?.notifyDataSetChanged()
                        }
                    })
            } else {
                if (position != 6) {
                    mklineCtrlList[4].time = CpLanguageUtil.getString(this, "cp_extra_text152")
                }
            }
//            if (position == 6) {
////                mklineCtrlList[4].isSelect = false
//                CpDialogUtil.createMoreTargetKlinePop(this, rl_kline_ctrl, object : CpNewDialogUtils.DialogOnSigningItemClickListener {
//                    override fun clickItem(position: Int, text: String) {
//                        if (text.equals("main")) {
//                            when (position) {
//                                0 -> {
//                                    v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
//                                    CpKLineUtil.setMainIndex(MainKlineViewStatus.MA.status)
//                                }
//                                1 -> {
//                                    v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
//                                    CpKLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status)
//                                }
//                            }
//                        } else {
//                            when (position) {
//                                0 -> {
//                                    v_kline?.setChildDraw(0)
//                                    CpKLineUtil.setViceIndex(CpViceViewStatus.MACD.status)
//                                }
//                                1 -> {
//                                    v_kline?.setChildDraw(1)
//                                    CpKLineUtil.setViceIndex(CpViceViewStatus.KDJ.status)
//                                }
//                                2 -> {
//                                    v_kline?.setChildDraw(2)
//                                    CpKLineUtil.setViceIndex(CpViceViewStatus.RSI.status)
//                                }
//                                3 -> {
//                                    v_kline?.setChildDraw(3)
//                                    CpKLineUtil.setViceIndex(CpViceViewStatus.WR.status)
//                                }
//                            }
//                        }
//                    }
//                }, object : CpNewDialogUtils.DialogOnDismissClickListener {
//                    override fun clickItem() {
//                        mklineCtrlList[6].isSelect = false
//                        mCpContractKlineCtrlAdapter?.notifyDataSetChanged()
//                    }
//                })
//            }
        }
        rv_kline_ctrl.postDelayed(Runnable {
            LogUtils.e("positiongetCurTime-----+" + CpKLineUtil.getCurTime())
            val position = CpKLineUtil.getKLineDefaultScale().indexOf(CpKLineUtil.getCurTime())
            LogUtils.e("position-----+" + position)
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
//            LogUtils.e("childView --- " + childView)
        }, 300)
    }


    var aG = true
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


    private var hasInit = false
    override fun onResume() {
        super.onResume()
        initData()
        CpWsContractAgentManager.instance.changeKlineKey(this.javaClass.simpleName)
        cur_time_index = CpKLineUtil.getCurTime4Index()

        curTime = CpKLineUtil.getCurTime()
        v_kline?.setMainDrawLine(CpKLineUtil.getCurTime4Index() == 0)
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
        CpWsContractAgentManager.instance.removeWsCallback(this)
        lastTick = null
        sendMsg(WsLinkUtils.getKlineNewLink(symbol, curTime, false).json)
        loopStop()
    }

    private fun setOnclick() {
        btn_buy.isEnable(true)
        btn_buy.setBgColor(CpColorUtil.getMainColorType(true))
        btn_sell.isEnable(true)
        btn_sell.setBgColor(CpColorUtil.getMainColorType(false))
        btn_buy.listener = object : CpCommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                var messageEvent =
                    CpMessageEvent(CpMessageEvent.contract_switch_type)
                CpEventBusUtil.post(messageEvent)
                finish()
            }
        }
        btn_sell.listener = object : CpCommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                var messageEvent =
                    CpMessageEvent(CpMessageEvent.contract_switch_type)
                CpEventBusUtil.post(messageEvent)
                finish()
            }
        }
        /**
         * ??????KLine
         */
        tv_landscape?.setOnClickListener {

            val mIntent = Intent(mActivity, CpHorizonMarketDetailActivity::class.java)
            mIntent.putExtra("curTime", curTime)
            mIntent.putExtra("symbolHorizon", symbol)
            mIntent.putExtra("contractId", contractId)
            mActivity.startActivity(mIntent)
        }

        ib_back?.setOnClickListener {
            finish()
        }

        ib_share?.setOnClickListener {
            CpDialogUtil.showKLineShareDialog(context = mActivity, mView = ll_kline_view)
        }

        /**
         * ????????????
         */
        ll_coin_map?.setOnClickListener {
//            if (type == ParamConstant.LEVER_INDEX) {
//                showLeftCoinPopu(TradeTypeEnum.LEVER_TRADE.value)
//            } else {
//                showLeftCoinPopu(TradeTypeEnum.COIN_TRADE.value)
//            }
            if (CpChainUtil.isFastClick())
                return@setOnClickListener

            var mContractCoinSearchDialog =
                CpContractCoinSearchDialog()
            var bundle = Bundle()
            bundle.putString(
                "contractList",
                CpClLogicContractSetting.getContractJsonListStr(mActivity)
            )
            mContractCoinSearchDialog.arguments = bundle
            mContractCoinSearchDialog.showDialog(supportFragmentManager, "SlContractFragment")
        }

        /**
         * KLine??????
         */
        tv_scale?.setOnClickListener {
            if (isShow) {
                showedView = rv_kline_scale
                klineState = KLINE_SCALE
            }
            isShow = !isShow
            rv_kline_scale?.visibility = if (isShow) View.GONE else View.VISIBLE
            tv_scale?.run {
                labelBackgroundColor =
                    CpColorUtil.getColor(if (isShow) R.color.normal_icon_color else R.color.main_blue)
                textColor =
                    CpColorUtil.getColor(if (isShow) R.color.normal_text_color else R.color.text_color)
            }
        }

        /**
         * KLine??????
         */
        tv_indicator?.setOnClickListener {
            if (isShow) {
                showedView = ly_kline_panel
                klineState = KLINE_INDEX
            }
            isShow = !isShow
            ly_kline_panel?.visibility = if (isShow) View.GONE else View.VISIBLE

            tv_indicator?.run {
                labelBackgroundColor =
                    CpColorUtil.getColor(if (isShow) R.color.normal_icon_color else R.color.main_blue)
                textColor =
                    CpColorUtil.getColor(if (isShow) R.color.normal_text_color else R.color.text_color)
            }
        }
    }

    private fun initData() {
        initCoinData()
        initKLineData()
    }

    /*
    *  ???????????????????????????
    * */
    private var jsonObject: JSONObject? = null


    /**
     * ?????????????????????
     */
    private fun initCoinData() {


        symbol = intent.getStringExtra(CpParamConstant.symbol).toString()
        contractId = intent.getIntExtra("contractId", 0)
        baseSymbol = intent.getStringExtra("baseSymbol").toString()
        quoteSymbol = intent.getStringExtra("quoteSymbol").toString()
        Log.e(
            "??????????????????",
            "symbol===$symbol,,,,contractId===$contractId,,,,baseSymbol===$baseSymbol,,,,quoteSymbol===$quoteSymbol,,,,"
        )
    }

    /*
     * KLine?????????????????????
     */
    private var main_index = 0
    private var vice_index = 0
    private var curTime: String? = ""
    private var cur_time_index = 0;
    private var klineScale = ArrayList<String>()
    private var themeMode = 0
    private fun initKLineData() {
        main_index = CpKLineUtil.getMainIndex()
        vice_index = CpKLineUtil.getViceIndex()
        cur_time_index = CpKLineUtil.getCurTime4Index()

        curTime = CpKLineUtil.getCurTime()
        klineScale = CpKLineUtil.getKLineScale()

        themeMode = 0
        initMainViceIndex()
    }

    private fun initMainViceIndex() {
        when (main_index) {
            MainKlineViewStatus.MA.status -> {
                v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
                CpKLineUtil.setMainIndex(MainKlineViewStatus.MA.status)
                (mainViewStatusViews[0] as CpLabelRadioButton?)?.isLabelEnable = true
            }

            MainKlineViewStatus.BOLL.status -> {
                v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
                CpKLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status)
                (mainViewStatusViews[1] as CpLabelRadioButton?)?.isLabelEnable = true
            }
        }

        when (vice_index) {
            CpViceViewStatus.MACD.status -> {
                v_kline?.setChildDraw(0)
                CpKLineUtil.setViceIndex(CpViceViewStatus.MACD.status)
                (viceViewStatusViews[0] as CpLabelRadioButton?)?.isLabelEnable = true
            }

            CpViceViewStatus.KDJ.status -> {
                v_kline?.setChildDraw(1)
                CpKLineUtil.setViceIndex(CpViceViewStatus.KDJ.status)
                (viceViewStatusViews[1] as CpLabelRadioButton?)?.isLabelEnable = true
            }

            CpViceViewStatus.RSI.status -> {
                v_kline?.setChildDraw(2)
                CpKLineUtil.setViceIndex(CpViceViewStatus.RSI.status)
                (viceViewStatusViews[2] as CpLabelRadioButton?)?.isLabelEnable = true
            }

            CpViceViewStatus.WR.status -> {
                v_kline?.setChildDraw(3)
                CpKLineUtil.setViceIndex(CpViceViewStatus.WR.status)
                (viceViewStatusViews[3] as CpLabelRadioButton?)?.isLabelEnable = true
            }
        }
    }

    override fun initView() {
        contractId = intent.getIntExtra("contractId", -1)
        if (type == CpParamConstant.LEVER_INDEX) {
            btn_buy?.setContent(
                "${
                    CpLanguageUtil.getString(
                        this,
                        "contract_action_buy"
                    )
                }/${
                    CpLanguageUtil.getString(
                        this,
                        "contract_action_long"
                    )
                }"
            )

            btn_sell?.setContent(
                "${
                    CpLanguageUtil.getString(
                        this,
                        "contract_action_sell"
                    )
                }/${
                    CpLanguageUtil.getString(
                        this,
                        "contract_action_short"
                    )
                }"
            )

        } else {
            btn_buy?.setContent(CpLanguageUtil.getString(this, "cp_order_text75"))
            btn_sell?.setContent(CpLanguageUtil.getString(this, "cp_overview_text14"))
        }

        mPricePrecision =
            CpClLogicContractSetting.getContractSymbolPricePrecisionById(this, contractId)

        mMultiplierCoin =
            CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(this, contractId)

        mMultiplierPrecision =
            CpClLogicContractSetting.getContractMultiplierPrecisionById(this, contractId)

        coUnit = CpClLogicContractSetting.getContractUint(CpMyApp.instance())

        mMultiplier = CpClLogicContractSetting.getContractMultiplierById(this, contractId)

        tv24hVolUnit =
            if (CpClLogicContractSetting.getContractUint(this) == 0) " " + getString(R.string.cp_overview_text9) else " " + mMultiplierCoin

        initDepthAndDeals()

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
                    showCoinName()
                }
            }
        }

        showCoinName()

        initViewColor()

        initKLineScale()

        action4KLineIndex()

        initDepthChart()

    }

    private fun showCoinName() {
        if (contractId != -1) {
            tv_coin_map?.text =
                CpClLogicContractSetting.getContractShowNameById(mActivity, contractId)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onMessageEvent(event: CpMessageEvent) {
        super.onMessageEvent(event)
        if (event.msg_type == CpMessageEvent.market_switch_curTime) {
            curTime = event.msg_content as String
            Log.e("shengong", "curTime2:$curTime")
            switchKLineScale(curTime ?: "15min")
            tv_scale?.text = curTime ?: "15min"
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
                mklineCtrlList.add(
                    CpKlineCtrlBean(
                        "15min",
                        CpKLineUtil.getCurTime().equals("15min"),
                        1
                    )
                )
                mklineCtrlList.add(
                    CpKlineCtrlBean(
                        "60min",
                        CpKLineUtil.getCurTime().equals("60min"),
                        1
                    )
                )
                mklineCtrlList.add(CpKlineCtrlBean("4h", CpKLineUtil.getCurTime().equals("4h"), 1))
                mklineCtrlList.add(
                    CpKlineCtrlBean(
                        "1day",
                        CpKLineUtil.getCurTime().equals("1day"),
                        1
                    )
                )

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
                    mklineCtrlList.add(
                        CpKlineCtrlBean(
                            CpLanguageUtil.getString(
                                this,
                                "cl_assets_text4"
                            ), false, 2
                        )
                    )
                }
                mklineCtrlList.add(
                    CpKlineCtrlBean(
                        CpLanguageUtil.getString(this, "cl_depth_text4"),
                        false,
                        3
                    )
                )
                mklineCtrlList.add(
                    CpKlineCtrlBean(
                        CpLanguageUtil.getString(
                            this,
                            "kline_text_scale"
                        ), false, 2
                    )
                )
                mCpContractKlineCtrlAdapter?.notifyDataSetChanged()
//            LogUtils.e("childView --- " + childView)
            }, 300)


        }

//        if (event.msg_type == CpMessageEvent.sl_contract_change_tagPrice_event) {
//            val obj = event.msg_content as JSONObject
//            tv_mark_price.text = obj.optString("tagPrice")
//            tv_index_price.text = obj.optString("indexPrice")
//        }


        if (event.msg_type == CpMessageEvent.sl_contract_left_coin_type) {
            val ticker = event.msg_content as JSONObject
            contractId = ticker.getInt("id")
            baseSymbol = ticker.getString("base")
            quoteSymbol = ticker.getString("quote")
            symbol = (ticker.getString("contractType") + "_" + ticker.getString("symbol")
                .replace("-", "")).toLowerCase()
            mPricePrecision =
                CpClLogicContractSetting.getContractSymbolPricePrecisionById(this, contractId)

            mMultiplierCoin =
                CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(this, contractId)

            mMultiplierPrecision =
                CpClLogicContractSetting.getContractMultiplierPrecisionById(this, contractId)

            coUnit = CpClLogicContractSetting.getContractUint(CpMyApp.instance())

            mMultiplier = CpClLogicContractSetting.getContractMultiplierById(this, contractId)

            tv24hVolUnit =
                if (CpClLogicContractSetting.getContractUint(this) == 0) " " + getString(R.string.cp_overview_text9) else " " + mMultiplierCoin

            v_kline?.setPricePrecision(mPricePrecision)

            isFrist = true
            klineData.clear()
            getSymbol(symbol)
            showCoinName()
            collectCoin()
        }

    }


    fun getSymbol(symbol: String) {

        if (jsonObject?.optString("symbol") != symbol) {
            chooseCoinAfterAddDetailsFg()
            setDepthSymbol()
            initHeaderData()
            /**
             * ???????????????
             * ???????????????????????????????????????????????????
             * PS :??????K?????????????????????????????????
             */
            var lastSymbol = jsonObject?.optString("symbol")
            if (isNotEmpty(lastSymbol)) {
                sendMsg(WsLinkUtils.tickerFor24HLink(lastSymbol!!, isSub = false))
            }
            initSocket()
        }

    }


    /**
     * ??????K?????????View?????????
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
     * todo ?????????
     * ?????????????????????liveData??????????????????????????????????????????
     */
    private fun setDepthSymbol() {

        CpDepthFragment.liveData.value = CpFlagBean(
            isContract = true,
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
        if (vp_depth_dealt == null || vp_depth_dealt.adapter == null) {
            return
        }
        pageAdapter?.notifyDataSetChanged()
        vp_depth_dealt?.offscreenPageLimit = fragments.size
        stl_depth_dealt?.setViewPager(vp_depth_dealt ?: return, titles.toTypedArray())
        stl_depth_dealt?.currentTab = 0
    }

    private fun initChooseCoinDetailsFg() {
        if (titles.size > 2) {
            titles.remove(getString(R.string.cp_extra_text113))
        }
    }


    /**
     * ????????????
     */
    private var operationType = 0

    /**
     * K??????????????????
     */
    private fun action4KLineIndex() {
        mainViewStatusViews.forEach {
            it?.setOnClickListener {
                val index = mainViewStatusViews.indexOf(it)
                (mainViewStatusViews[0] as CpLabelRadioButton?)?.isLabelEnable = (index == 0)
                (mainViewStatusViews[1] as CpLabelRadioButton?)?.isLabelEnable = (index == 1)
                when (index) {
                    MainKlineViewStatus.MA.status -> {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
                        CpKLineUtil.setMainIndex(MainKlineViewStatus.MA.status)
                    }

                    MainKlineViewStatus.BOLL.status -> {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
                        CpKLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status)
                    }

                    MainKlineViewStatus.NONE.status -> {
                        v_kline?.changeMainDrawType(MainKlineViewStatus.NONE)
                        CpKLineUtil.setMainIndex(MainKlineViewStatus.NONE.status)
                    }
                }
            }
        }

        /**
         * -----------??????--------------
         */
        viceViewStatusViews.forEach {
            it?.setOnClickListener {
                val index = viceViewStatusViews.indexOf(it)
                (viceViewStatusViews[0] as CpLabelRadioButton?)?.isLabelEnable = (index == 0)
                (viceViewStatusViews[1] as CpLabelRadioButton?)?.isLabelEnable = (index == 1)
                (viceViewStatusViews[2] as CpLabelRadioButton?)?.isLabelEnable = (index == 2)
                (viceViewStatusViews[3] as CpLabelRadioButton?)?.isLabelEnable = (index == 3)

                when (index) {
                    CpViceViewStatus.MACD.status -> {
                        v_kline?.setChildDraw(0)
                        CpKLineUtil.setViceIndex(CpViceViewStatus.MACD.status)
                    }

                    CpViceViewStatus.KDJ.status -> {
                        v_kline?.setChildDraw(1)
                        CpKLineUtil.setViceIndex(CpViceViewStatus.KDJ.status)
                    }

                    CpViceViewStatus.WR.status -> {
                        v_kline?.setChildDraw(2)
                        CpKLineUtil.setViceIndex(CpViceViewStatus.WR.status)
                    }

                    CpViceViewStatus.RSI.status -> {
                        v_kline?.setChildDraw(3)
                        CpKLineUtil.setViceIndex(CpViceViewStatus.RSI.status)
                    }
                    else -> {
                        v_kline?.hideChildDraw()
                        CpKLineUtil.setViceIndex(CpViceViewStatus.NONE.status)
                    }
                }

            }
        }
    }

    //?????? ??? ?????????????????????
    private fun initSocket() {
        if (isNotEmpty(symbol)) {
            // sub ticker
            val scale: String = if (curTime == "line") "1min" else curTime ?: "15min"
            CpWsContractAgentManager.instance.sendMessage(
                hashMapOf(
                    "symbol" to symbol,
                    "line" to scale
                ), this
            )
            Log.d("??????????????????4",  hashMapOf(
                "symbol" to symbol,
                "line" to scale
            ).toString())
        }
    }

    private fun initViewColor() {
        tv_converted_close_price.visibility = View.GONE
        var mainColorType = CpColorUtil.getMainColorType()
//        btn_buy?.backgroundColor = mainColorType
//        btn_sell?.backgroundColor = CpColorUtil.getMainColorType(isRise = false)

        tv_close_price?.textColor = mainColorType
        tv_rose?.textColor = mainColorType
    }

    var mClDepthFragment: CpDepthFragment? = null
    var dealtRecordFragment: CpDealtRecordFragment? = null
    var pageAdapter: CpNVPagerAdapter? = null

    private fun initDepthAndDeals() {

        if (titles.size > 0) {
            titles.clear()
        }

        if (fragments.size > 0) {
            fragments.clear()
        }
        titles.add(CpLanguageUtil.getString(this, "kline_action_entrustMentOrder"))//????????????
        titles.add(CpLanguageUtil.getString(this, "cp_order_text71"))//????????????

        mClDepthFragment = CpDepthFragment.newInstance(
            viewPager = vp_depth_dealt,
            contractId = contractId,
            symbol = symbol
        )
        dealtRecordFragment = CpDealtRecordFragment.newInstance(viewPager = vp_depth_dealt)

        fragments.add(mClDepthFragment!!)
        fragments.add(dealtRecordFragment!!)
        pageAdapter = CpNVPagerAdapter(
            supportFragmentManager,
            titles,
            fragments
        )

        vp_depth_dealt?.adapter = pageAdapter
        vp_depth_dealt.offscreenPageLimit = fragments.size
//        vp_depth_dealt?.setScrollable(true)
        stl_depth_dealt?.setViewPager(vp_depth_dealt, titles.toTypedArray())

        vp_depth_dealt?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                selectPosition = position
                // ??????????????????resetHeight?????????????????????????????????
//                vp_depth_dealt?.resetHeight(position)
            }
        })

    }

    var selectPosition = 0

    var calibrationAdapter: CpKLineScaleAdapter? = null

    /**
     * ??????K?????????
     */
    private fun initKLineScale() {
        rv_kline_scale?.isLayoutFrozen = true
        rv_kline_scale?.setHasFixedSize(true)
        tv_scale?.text = if (cur_time_index == 0) "line" else curTime

        val layoutManager = GridLayoutManager(mActivity, 4)
        layoutManager.isAutoMeasureEnabled = false
        rv_kline_scale?.layoutManager = layoutManager
        calibrationAdapter = CpKLineScaleAdapter(klineScale)
        rv_kline_scale?.adapter = calibrationAdapter
        /**
         * ?????????
         */
        v_kline?.setMainDrawLine(cur_time_index == 0)

        calibrationAdapter?.setOnItemClickListener { viewHolder, view, position ->
            /**
             * ?????????
             */
            v_kline?.setMainDrawLine(position == 0)
            if (position != CpKLineUtil.getCurTime4Index()) {
                for (i in 0 until klineScale.size) {
                    val boxView =
                        viewHolder?.getViewByPosition(i, R.id.cbtn_view) as CpCustomCheckBoxView
                    boxView.setCenterColor(CpColorUtil.getColor(R.color.normal_text_color))
                    boxView.setCenterSize(12f)
                    boxView.setIsNeedDraw(false)
                    boxView.isChecked = false
                }
                val boxView =
                    viewHolder?.getViewByPosition(position, R.id.cbtn_view) as CpCustomCheckBoxView
                boxView.isChecked = true
                boxView.setIsNeedDraw(true)
                boxView.setCenterSize(12f)
                boxView.setCenterColor(CpColorUtil.getColor(R.color.text_color))
                CpKLineUtil.setCurTime4KLine(position)
                CpKLineUtil.setCurTime(klineScale[position])
                switchKLineScale(klineScale[position])

                tv_scale?.text = if (position == 0) "line" else curTime

            } else {
                val boxView =
                    viewHolder?.getViewByPosition(position, R.id.cbtn_view) as CpCustomCheckBoxView
                boxView.isChecked = true
                boxView.setIsNeedDraw(true)
                boxView.setCenterSize(12f)
                boxView.setCenterColor(CpColorUtil.getColor(R.color.text_color))
                CpKLineUtil.setCurTime4KLine(position)
                CpKLineUtil.setCurTime(klineScale[position])
                switchKLineScale(klineScale[position])
            }
        }
    }


    /**
     * ??????K?????????
     * @param kLineScale K?????????
     */
    private fun switchKLineScale(kLineScale: String) {
        if (curTime != kLineScale) {
            isFrist = true
            klineData.clear()
            var scale = if (curTime == "line") "1min" else curTime
            /**
             * ????????????
             */
            sendMsg(WsLinkUtils.getKlineNewLink(symbol, scale, false).json)
            curTime = kLineScale
            var scale2 = if (curTime == "line") "1min" else curTime
            /**
             * ????????????
             */
            sendMsg(WsLinkUtils.getKLineHistoryLink(symbol, scale2).json)
            /**
             * ??????
             */
            sendMsg(WsLinkUtils.getKlineNewLink(symbol, scale2).json)
            initSocket()
        }

    }

    private var lastTick: KlineQuotesData? = null
    private var isRealNew = false

    /**
     * ?????? 24H,KLine??????
     */
    fun handleData(data: String) {
        try {
            var jsonObj = JSONObject(data)
            if (!jsonObj.isNull("tick")) {
                /**
                 * 24H??????
                 */
                if (jsonObj.getString("channel") == WsLinkUtils.tickerFor24HLink(
                        symbol,
                        isChannel = true
                    )
                ) {
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
                 * ??????K?????????
                 */
                var scale = if (curTime == "line") "1min" else curTime
                if (jsonObj.getString("channel") == WsLinkUtils.getKlineNewLink(
                        symbol,
                        scale
                    ).channel
                ) {

                    doAsync {
                        /**
                         * ??????????????????????????????????????????
                         */
                        val kLineEntity = CpKLineBean()
                        val data = jsonObj.optJSONObject("tick")
                        kLineEntity.id = data.optLong("id")
//                        kLineEntity.openPrice = BigDecimal(data.optString("cp_open")).toFloat()
//                        kLineEntity.closePrice = BigDecimal(data.optString("close")).toFloat()
//                        kLineEntity.highPrice = BigDecimal(data.optString("high")).toFloat()
//                        kLineEntity.lowPrice = BigDecimal(data.optString("low")).toFloat()
                        val open =
                            if (coUnit == 0) data.optString("open") else CpBigDecimalUtils.showSNormal(
                                data.optString("open"),
                                mPricePrecision
                            )
                        val close =
                            if (coUnit == 0) data.optString("close") else CpBigDecimalUtils.showSNormal(
                                data.optString("close"),
                                mPricePrecision
                            )
                        val high =
                            if (coUnit == 0) data.optString("high") else CpBigDecimalUtils.showSNormal(
                                data.optString("high"),
                                mPricePrecision
                            )
                        val low =
                            if (coUnit == 0) data.optString("low") else CpBigDecimalUtils.showSNormal(
                                data.optString("low"),
                                mPricePrecision
                            )
                        val vol =
                            if (coUnit == 0) data.optString("vol") else CpBigDecimalUtils.mulStr(
                                data.optString("vol"), mMultiplier, mMultiplierPrecision
                            )

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
                if (!jsonObj.isNull("data") && jsonObj.getString("channel") == WsLinkUtils.getKLineHistoryLink(
                        symbol,
                        scale
                    ).channel
                ) {
                    /**
                     * ??????(req) ----> K???????????????
                     * ??????K?????????????????????
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
     * ??????24H????????????
     */
    private fun render24H(tick: KlineTick) {
        val price = mPricePrecision

        CpDepthFragment.liveData4closePrice.postValue(
            arrayListOf(
                tick.close,
                price.toString(),
                symbol
            )
        )

        runOnUiThread {
            tv_close_price?.run {
                textColor = CpColorUtil.getMainColorType(tick.rose >= 0)
                text = CpDecimalUtil.cutValueByPrecision(tick.close, mPricePrecision)
            }

//            /**
//             * ????????????????????????
//             */
//            tv_converted_close_price?.text = RateManager.getCNYByCoinName(realMarketName, tick.close)
            val rose = tick?.rose.toString()
            RateManager.getRoseText(tv_rose, rose)
            tv_rose?.textColor = CpColorUtil.getMainColorType(RateManager.getRoseTrend(rose) >= 0)
            tv_rose?.setBackgroundResource(
                CpColorUtil.getContractRateDrawable(
                    RateManager.getRoseTrend(
                        rose
                    ) >= 0
                )
            )
            tv_high_price?.text = CpDecimalUtil.cutValueByPrecision(tick.high, price)
            tv_low_price?.text = CpDecimalUtil.cutValueByPrecision(tick.low, price)
            val amount = if (coUnit == 0) tick.vol else CpBigDecimalUtils.mulStr(
                tick.vol,
                mMultiplier,
                mMultiplierPrecision
            )
            tv_24h_vol?.text = CpBigDecimalUtils.showDepthVolume(amount) + tv24hVolUnit
        }
    }

    /**
     * ??????K???????????????
     * @param data K???????????????
     */
    private fun handlerKLineHistory(data: String) {
        doAsync {
            val json = JSONObject(data)
            val type = object : TypeToken<ArrayList<CpKLineBean>>() {
            }.type
            val typeNew = object : TypeToken<CpKLineBean>() {
            }.type
            val gson = GsonBuilder().setPrettyPrinting().create()
            if (isFrist) {
                klineData.clear()
                var objKline = json.getJSONArray("data")
                for (i in 0..(objKline.length() - 1)) {
                    var obj: JSONObject = objKline.get(i) as JSONObject
                    val mKLineBean: CpKLineBean = gson.fromJson(obj.toString(), typeNew)
                    if (coUnit != 0) {
                        mKLineBean.volume = CpBigDecimalUtils.mulStr(
                            mKLineBean.volume.toString(),
                            mMultiplier,
                            mMultiplierPrecision
                        ).toFloat()
                        mKLineBean.openPrice = CpBigDecimalUtils.showSNormal(
                            mKLineBean.openPrice.toString(),
                            mPricePrecision
                        ).toFloat()
                        mKLineBean.closePrice = CpBigDecimalUtils.showSNormal(
                            mKLineBean.closePrice.toString(),
                            mPricePrecision
                        ).toFloat()
                        mKLineBean.highPrice = CpBigDecimalUtils.showSNormal(
                            mKLineBean.highPrice.toString(),
                            mPricePrecision
                        ).toFloat()
                        mKLineBean.lowPrice = CpBigDecimalUtils.showSNormal(
                            mKLineBean.lowPrice.toString(),
                            mPricePrecision
                        ).toFloat()
                    }
                    klineData.add(mKLineBean)
                }
//                val list: ArrayList<KLineBean> = gson.fromJson(json.getJSONArray("data").toString(), type)
//                klineData.addAll(list)
                if (klineData.size == 0) {
                    initKlineData()
                } else {
                    val scale = if (curTime == "line") "1min" else curTime
                    CpWsContractAgentManager.instance.sendData(
                        WsLinkUtils.getKlineHistoryOther(
                            symbol,
                            scale,
                            klineData[0].id.toString()
                        )
                    )
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
         * ????????????K?????????
         */
        runOnUiThread {
            val scale = if (curTime == "line") "1min" else curTime
            CpWsContractAgentManager.instance.sendData(WsLinkUtils.getKlineNewLink(symbol, scale))
        }

    }

    /**
     * WebSocket ????????????
     */
    private fun sendMsg(msg: String) {

    }


    /**
     * ?????? ????????????????????????
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

                    ( tv_scale )?.run {
                        labelBackgroundColor = CpColorUtil.getColor(R.color.normal_icon_color)
                        textColor = CpColorUtil.getColor(R.color.normal_text_color)
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
        CpWsContractAgentManager.instance.removeWsCallback(this)
        loopStop()
    }

    val getUserSelfDataReqType = 2 // ???????????????????????????
    val addCancelUserSelfDataReqType = 1 // ???????????????????????????

    inner class MyNDisposableObserver(type: Int) : CpNDisposableObserver(mActivity) {

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
            }
        }
    }

    /*
     * ??????????????????????????????????????????
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
        if (serverSelfSymbols.contains("e-" +baseSymbol.lowercase() + "-" + quoteSymbol.lowercase())) {
            ib_collect?.setImageResource(R.drawable.quotes_optional_selected2)
        } else {
            ib_collect?.setImageResource(R.drawable.quotes_optional_default2)
        }
    }


    override fun onWsMessage(json: String) {
        handleData(json)
    }

    private fun realtData(jsonObj: JSONObject, data: String) {
        val depthReal =
            jsonObj.getString("channel") == WsLinkUtils.getDealHistoryLink(symbol).channel ||
                    jsonObj.getString("channel") == WsLinkUtils.getDealNewLink(symbol).channel
        if (depthReal) {
            if (dealtRecordFragment != null) {
                if (!isRealNew) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(200)     //??????500ms
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

    private fun getMarkertInfo() {
        if (contractId == -1) {
            return
        }
        loopStop()
        subscribe = Observable.interval(0L, CpCommonConstant.capitalRateLoopTime, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                addDisposable(
                    getContractModel().getCoinDepth(contractId, symbol,
                        consumer = object : CpNDisposableObserver(true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                jsonObject.optJSONObject("data")?.run {
                                    datas =
                                        Gson().fromJson<DepthItem>(
                                            this.toString(),
                                            DepthItem::class.java
                                        )
                                    this@CpMarketDetail4Activity.runOnUiThread {
                                        setData4DepthChart()
                                    }
                                }
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                            }
                        })
                )
            }
    }





    /**
     * ??????????????????????????????
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
         * ?????? ???????????????
         */
        val legend = depth_chart.legend
        legend.isEnabled = false
        /**
         * ????????????
         */
        depth_chart?.setScaleEnabled(false)
        /**
         * X,Y????????????
         */
        depth_chart?.setPinchZoom(false)

        /**
         * ???????????????????????????
         */
        depth_chart?.description?.isEnabled = false


        // ??????????????????
        depth_chart?.setTouchEnabled(true)
        depth_chart.isLongClickable = true
        depth_chart.isNestedScrollingEnabled = false

        /**
         * X
         */
        val xAxis: XAxis = depth_chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setLabelCount(3, true)
        // ?????????????????????????????????
        xAxis.setDrawGridLines(false)
        xAxis.axisLineWidth = 0.5f
        //x??????????????????
        xAxis.isEnabled = true
        //??????x???????????????
        xAxis.setDrawLabels(true)
        //?????????????????????
        xAxis.textColor = CpColorUtil.getColor(R.color.normal_text_color)
        xAxis.textSize = 10f
        //????????????????????????????????????x??????????????????????????????????????????????????????????????????????????????
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
        /**********??????Y???********/
        depth_chart.axisLeft.axisMinimum = 0f
        /**********??????Y???********/
        val yAxis = depth_chart.axisRight
        yAxis.setDrawGridLines(false)
        yAxis.setDrawAxisLine(false)
        //??????Y??????Label???????????????????????????????????????????????????
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        //?????????????????????????????????
        yAxis.textColor = CpColorUtil.getColor(R.color.normal_text_color)
        yAxis.textSize = 10f
        //??????Y????????????label??????
        yAxis.setLabelCount(6, true)
        //??????????????????????????????????????????
        depth_chart?.setViewPortOffsets(0f, 20f, 0f, CpDisplayUtil.dip2px(14f))
        yAxis.valueFormatter = CpDepthYValueFormatter()


        depth_chart?.setOnClickListener {
            if (depth_chart.marker != null) {
                depth_chart.marker = null
            }
        }

        depth_chart?.setOnLongClickListener {
            val mv = CpDepthMarkView(this, R.layout.cp_layout_depth_marker)
            mv.chartView = depth_chart // For bounds control
            depth_chart?.marker = mv // Set the marker to the ch
            false
        }


    }

    /**
     * ?????????????????????
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
        var buyVolumeSum = 0.0
        // TODO ??????
        for (i in buyList.indices) {
            buyVolumeSum = CpBigDecimalUtils.add(buyVolumeSum.toString(), buyList[i].vol).toDouble()
            val volSum = buyList[i].sum.toFloat()
            val entry = Entry(
                (buyList[i].price).toFloat(),
                volSum,
                buyList[i].price.toTextPrice(mPricePrecision)
            )
            yData.add(0, entry)
        }
        /*************??????????????????*********/
        var sellVolumeSum = 0.0
        val sellYData = ArrayList<Entry>()
        for (i in sellList.indices) {
            sellVolumeSum =
                CpBigDecimalUtils.add(sellVolumeSum.toString(), sellList[i].vol).toDouble()
            val volSum = sellList[i].sum.toFloat()
            val entry = Entry(
                sellList[i].price.toFloat(),
                volSum,
                sellList[i].price.toTextPrice(mPricePrecision)
            )
            sellYData.add(entry)
        }

        /**
         * Y ???????????? ??? ?????????
         */
        var maxVolume: Float = max(
            buyList.get(buyList.size - 1).sum.toFloat(),
            sellList.get(sellList.size - 1).sum.toFloat()
        )


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
     * ??????lineDataSet  in?????????
     */
    private fun lineDataSet(yData: ArrayList<Entry>, isBuy: Boolean): LineDataSet {
        val lineDataSet: LineDataSet?
        if (isBuy) {
            lineDataSet = LineDataSet(yData, "Buy")
            lineDataSet.color = CpColorUtil.getMainColorType()
            lineDataSet.fillColor = CpColorUtil.getMainColorType()
            /**
             * ?????????????????????
             */
            lineDataSet.color = CpColorUtil.getMainColorType()

        } else {
            lineDataSet = LineDataSet(yData, "Sell")
            lineDataSet.color = CpColorUtil.getMainColorType(isRise = false)
            lineDataSet.fillColor = CpColorUtil.getMainColorType(isRise = false)
            /**
             * ?????????????????????
             */
            lineDataSet.color = CpColorUtil.getMainColorType(isRise = false)
        }
        /**
         * ???????????????????????????????????????
         */
        lineDataSet.setDrawFilled(true)

        /**
         * ??????MarkView??????????????????
         * ???????????????????????????
         */
        lineDataSet.isHighlightEnabled = true
        lineDataSet.highLightColor = Color.TRANSPARENT


        /**
         * ?????????????????????
         */
        lineDataSet.lineWidth = 2.0f


        /**
         * ???????????????????????????
         */
        lineDataSet.setDrawValues(false)

        /**
         * ?????????????????????????????????
         */
        lineDataSet.setDrawCircles(false)
        return lineDataSet
    }

    /**
     * ????????????
     */
    fun clearDepthChart() {
        depth_chart?.clear()
        depth_chart?.notifyDataSetChanged()
        depth_chart?.invalidate()
    }


    private fun loopStop() {
        if (subscribe != null) {
            subscribe?.dispose()
        }
    }

    ////ybc start
    private fun collectCoin() {
        /**
         * ?????????????????????"??????"?????????
         */
        EventBus.getDefault().post(CpCollectionEvent(CpCollectionEvent.TYPE_REQUEST))
        /*if (isLogined && isOptionalSymbolServerOpen) {
            getOptionalSymbol()
        } else {
            var hasCollect = LikeDataService.getInstance().hasCollect(symbol)
            showImgCollect(hasCollect, false, false)
        }*/

        ib_collect?.setOnClickListener {
            if (serverSelfSymbols.contains("e-" +baseSymbol.lowercase() + "-" + quoteSymbol.lowercase())) {
                operationType = 2
            } else {
                operationType = 1
            }
            //addOrDeleteSymbol(operationType, symbol)
            EventBus.getDefault()
                .post(CpCollectionEvent(CpCollectionEvent.TYPE_ADD_DEL, operationType, "e-" +baseSymbol.lowercase() + "-" + quoteSymbol.lowercase()))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCpCollectionEvent2(event: CpCollectionEvent2) {
        Log.e("yubch", "bbb:" + event.type)
        if (getUserSelfDataReqType == event.type) {
            showServerSelfSymbols(event.jsonObject.optJSONObject("data"))
        } else if (addCancelUserSelfDataReqType == event.type) {
            var hasCollect = false
            if (operationType == 2) {
                serverSelfSymbols.remove("e-" +baseSymbol.lowercase() + "-" + quoteSymbol.lowercase())
            } else {
                hasCollect = true
                serverSelfSymbols.add("e-" +baseSymbol.lowercase() + "-" + quoteSymbol.lowercase())
            }
            showImgCollect(hasCollect, true, true)
        }
    }

    /*
    * ????????????????????????????????????
    */
    private fun showImgCollect(hasCollect: Boolean, isShowToast: Boolean, isAddRemove: Boolean) {
        if (hasCollect) {
            ib_collect?.setImageResource(R.drawable.quotes_optional_selected2)
            if (isShowToast) {
                CpNToastUtil.showTopToastNet(
                    mActivity,
                    true,
                    CpLanguageUtil.getString(this, "kline_tip_addCollectionSuccess")
                )
            }

        } else {
            ib_collect?.setImageResource(R.drawable.quotes_optional_default2)
            if (isShowToast) {
                CpNToastUtil.showTopToastNet(
                    mActivity,
                    true,
                    CpLanguageUtil.getString(this, "kline_tip_removeCollectionSuccess")
                )
            }

        }
    }
}
