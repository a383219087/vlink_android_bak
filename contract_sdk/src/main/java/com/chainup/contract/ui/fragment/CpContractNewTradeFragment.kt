package com.chainup.contract.ui.fragment


import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.LogUtils
import com.chainup.contract.R
import com.chainup.contract.app.CpCommonConstant
import com.chainup.contract.app.CpParamConstant
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.activity.CpContractEntrustNewActivity
import com.chainup.contract.utils.*
import com.chainup.contract.view.CpDialogUtil
import com.chainup.contract.view.CpNewDialogUtils
import com.chainup.contract.view.CpSlDialogHelper
import com.chainup.contract.ws.CpWsContractAgentManager
import com.google.android.material.appbar.AppBarLayout
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.chainup.contract.ui.activity.CpMarketDetail4Activity
import com.chainup.contract.bean.CpCreateOrderBean
import com.yjkj.chainup.manager.CpLanguageUtil
import com.yjkj.chainup.new_contract.activity.CpContractCalculateActivity
import com.yjkj.chainup.new_contract.adapter.CpContractKlineCtrlAdapter
import com.yjkj.chainup.new_contract.bean.CpKlineCtrlBean
import com.yjkj.chainup.new_version.kline.bean.CpKLineBean
import com.yjkj.chainup.new_version.kline.view.cp.MainKlineViewStatus
import com.yjkj.chainup.new_version.kline.view.vice.CpViceViewStatus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.cp_activity_market_detail4.*
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.*
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.customize_depth_chart
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.kline_tab_indicator
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rl_kline_ctrl
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.rv_kline_ctrl
import kotlinx.android.synthetic.main.cp_fragment_cl_contract_trade_new.v_kline
import kotlinx.android.synthetic.main.cp_trade_header_tools.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.concurrent.TimeUnit

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
    var openContract = 0//是否开通了合约交易 1已开通, 0未开通
    var futuresLocalLimit = 0 //1 区域限制范围内  0 不在限制范围
    var authLevel = 0 //	 0、未审核，1、通过，2、未通过  3未认证
    var subscribe: Disposable? = null
    var isContractHidden:Boolean =true
    var isContractFirst:Boolean =false
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
        main_index = CpKLineUtil.getMainIndex()
        vice_index = CpKLineUtil.getViceIndex()
        cur_time_index = CpKLineUtil.getCurTime4Index()

        curTime = CpKLineUtil.getCurTime()
        klineScale = CpKLineUtil.getKLineScale()

        themeMode = 0

    }
    var klineData: ArrayList<CpKLineBean> = arrayListOf()
    var symbol = ""
    //k线图end


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
        img_top.setOnClickListener {
            appbarlayout.setExpanded(true, true)
        }
        //去k线图页面
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
        }
        //合约上面的三个点
        iv_more.setOnClickListener {
            CpSlDialogHelper.createContractSettingNew(
                    activity,
                    iv_more,
                    mContractId,
                    "",
                    openContract
            )
        }
        //合约计算器
        ib_jisuan.setOnClickListener {
            if (mContractId > 0) {
                CpContractCalculateActivity.show(context as Activity, mContractId, "")
            }
        }
        //全部
        ll_all_entrust_order.setSafeListener {
            if (!CpClLogicContractSetting.isLogin()) {
                CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
            } else if (openContract == 0) {
                CpDialogUtil.showCreateContractDialog(this!!.activity!!, object : CpNewDialogUtils.DialogBottomListener {
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
        setTextConetnt()
    }

    private fun setTextConetnt() {
        mklineCtrlList.add(CpKlineCtrlBean("15min", CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("15min")), 1))
        mklineCtrlList.add(CpKlineCtrlBean("60min", CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("60min")), 1))
        mklineCtrlList.add(CpKlineCtrlBean("4h", CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("4h")), 1))
        mklineCtrlList.add(CpKlineCtrlBean("1day", CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("1day")), 1))

        if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("line"))) {
            mklineCtrlList.add(CpKlineCtrlBean("line", true, 2))
        } else if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("1min"))) {
            mklineCtrlList.add(CpKlineCtrlBean("1min", true, 2))
        } else if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("5min"))) {
            mklineCtrlList.add(CpKlineCtrlBean("5min", true, 2))
        } else if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("30min"))) {
            mklineCtrlList.add(CpKlineCtrlBean("30min", true, 2))
        } else if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("1week"))) {
            mklineCtrlList.add(CpKlineCtrlBean("1week", true, 2))
        } else if (CpKLineUtil.getCurTime4Index().equals(CpKLineUtil.getKLineScale().indexOf("1month"))) {
            mklineCtrlList.add(CpKlineCtrlBean("1month", true, 2))
        } else {
            mklineCtrlList.add(CpKlineCtrlBean(CpLanguageUtil.getString(activity, "cp_extra_text152"), false, 2))
        }
        mklineCtrlList.add(CpKlineCtrlBean(CpLanguageUtil.getString(activity, "cp_extra_text153"), false, 3))
        mklineCtrlList.add(CpKlineCtrlBean(CpLanguageUtil.getString(activity, "cp_extra_text154"), false, 2))
        mCpContractKlineCtrlAdapter = CpContractKlineCtrlAdapter(mklineCtrlList)
        rv_kline_ctrl.layoutManager = GridLayoutManager(activity, 7)
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
                })
            } else {
                if (position != 6) {
                    mklineCtrlList[4].time = CpLanguageUtil.getString(activity, "cp_extra_text152")
                }
            }
            if (position == 6) {
//                mklineCtrlList[4].isSelect = false
                CpDialogUtil.createMoreTargetKlinePop(activity, rl_kline_ctrl, object : CpNewDialogUtils.DialogOnSigningItemClickListener {
                    override fun clickItem(position: Int, text: String) {
                        if (text.equals("main")) {
                            when (position) {
                                0 -> {
                                    v_kline?.changeMainDrawType(MainKlineViewStatus.MA)
                                    CpKLineUtil.setMainIndex(MainKlineViewStatus.MA.status)
                                }
                                1 -> {
                                    v_kline?.changeMainDrawType(MainKlineViewStatus.BOLL)
                                    CpKLineUtil.setMainIndex(MainKlineViewStatus.BOLL.status)
                                }
                            }
                        } else {
                            when (position) {
                                0 -> {
                                    v_kline?.setChildDraw(0)
                                    CpKLineUtil.setViceIndex(CpViceViewStatus.MACD.status)
                                }
                                1 -> {
                                    v_kline?.setChildDraw(1)
                                    CpKLineUtil.setViceIndex(CpViceViewStatus.KDJ.status)
                                }
                                2 -> {
                                    v_kline?.setChildDraw(2)
                                    CpKLineUtil.setViceIndex(CpViceViewStatus.RSI.status)
                                }
                                3 -> {
                                    v_kline?.setChildDraw(3)
                                    CpKLineUtil.setViceIndex(CpViceViewStatus.WR.status)
                                }
                            }
                        }
                    }
                }, object : CpNewDialogUtils.DialogOnDismissClickListener {
                    override fun clickItem() {
                        mklineCtrlList[6].isSelect = false
                        mCpContractKlineCtrlAdapter?.notifyDataSetChanged()
                    }
                })
            }
        }

        /*rv_kline_ctrl.postDelayed(Runnable {
            LogUtils.e("positiongetCurTime-----+" + CpKLineUtil.getCurTime())
            val position = CpKLineUtil.getKLineDefaultScale().indexOf(CpKLineUtil.getCurTime())
            LogUtils.e("position-----+" + position)
            if (position != -1) {
                val childView: View = rv_kline_ctrl.getChildAt(position)
                childView?.apply {
                    val tvSale = this.findViewById<TextView>(R.id.tv_time)
                    tvSale?.let { textClickTab(it, null) }
                }
            } else {
                val childView: View = rv_kline_ctrl.getChildAt(4)
                childView?.apply {
                    val tvSale = this.findViewById<TextView>(R.id.tv_scale)
                    tvSale?.let { textClickTab(it, null) }
                }
            }
//            LogUtils.e("childView --- " + childView)
        }, 300)*/
    }

    private fun showLeftCoinWindow() {
        if (CpChainUtil.isFastClick())
            return
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
        tab_order.setViewPager(vp_order, arrayOf(getString(R.string.cp_order_text1), getString(R.string.cp_order_text2), getString(R.string.cp_order_text3)), this.activity, mFragments)
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
        addDisposable(getContractModel().getPublicInfo(
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        saveContractPublicInfo(jsonObject)
                    }
                })
        )
    }


    private fun loopStart() {
        loopStop()
        subscribe = Observable.interval(0L, CpCommonConstant.capitalRateLoopTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    getContractUserConfig()
                    getMarkertInfo()
                    getPositionAssetsList()
                    getCurrentOrderList()
                    getCurrentPlanOrderList()
                }
    }

    private fun getContractUserConfig() {
        if (!CpClLogicContractSetting.isLogin()) {
            CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_logout_event))
            tab_order.getTitleView(0).text = getString(R.string.cp_order_text1)
            tab_order.getTitleView(1).text = getString(R.string.cp_order_text2)
            tab_order.getTitleView(2).text = getString(R.string.cp_order_text3)
            v_horizontal_depth.setUserLogout()
            return
        }
        addDisposable(
                getContractModel().getUserConfig(mContractId.toString(),
                        consumer = object : CpNDisposableObserver() {
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
                                swipeLayout.isRefreshing = false
                            }

                            override fun onResponseFailure(code: Int, msg: String?) {
                                super.onResponseFailure(code, msg)
                                swipeLayout.isRefreshing = false
                            }
                        })
        )
    }

    private fun getMarkertInfo() {
        addDisposable(
                getContractModel().getMarkertInfo(mSymbol, mContractId.toString(),
                        consumer = object : CpNDisposableObserver() {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                jsonObject.optJSONObject("data").run {
                                    activity?.runOnUiThread {
                                        v_horizontal_depth.setMarkertInfo(this)
                                    }
                                }
                            }
                        })
        )
    }

    private fun getPositionAssetsList() {
        if (!CpClLogicContractSetting.isLogin()) return
        if (openContract == 0) return
        addDisposable(
                getContractModel().getPositionAssetsList(
                        consumer = object : CpNDisposableObserver(true) {
                            @SuppressLint("SetTextI18n")
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                jsonObject.optJSONObject("data")?.run {
//                                    tab_order.getTitleView(0).text = getString(R.string.cp_order_text1) + " " + this.optJSONArray("positionList").length()
                                    val msgEvent = CpMessageEvent(CpMessageEvent.sl_contract_refresh_position_list_event)
                                    msgEvent.msg_content = this
                                    CpEventBusUtil.post(msgEvent)
                                    v_horizontal_depth.setUserAssetsInfo(this)
                                }
                            }
                        })
        )
    }

    private fun getCurrentOrderList() {
        if (!CpClLogicContractSetting.isLogin()) return
        if (openContract == 0) return
        addDisposable(
                getContractModel().getCurrentOrderListAll( 0, 1,
                        consumer = object : CpNDisposableObserver(true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                jsonObject.optJSONObject("data").run {
//                                    tab_order.getTitleView(1).text = getString(R.string.cp_order_text2) + " " + this.optString("count")
                                    val msgEvent = CpMessageEvent(CpMessageEvent.sl_contract_refresh_current_entrust_list_event)
                                    msgEvent.msg_content = this
                                    CpEventBusUtil.post(msgEvent)
                                    v_horizontal_depth.setCurrentOrderJsonInfo(this)
                                }
                            }
                        })
        )
    }

    private fun getCurrentPlanOrderList() {
        if (!CpClLogicContractSetting.isLogin()) return
        if (openContract == 0) return
        addDisposable(
                getContractModel().getCurrentPlanOrderList(mContractId.toString(), 0, 1,
                        consumer = object : CpNDisposableObserver(true) {
                            @SuppressLint("SetTextI18n")
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                jsonObject.optJSONObject("data").run {
//                                    tab_order.getTitleView(2).text = getString(R.string.cp_order_text3) + " " + this.optString("count")
                                    val msgEvent = CpMessageEvent(CpMessageEvent.sl_contract_refresh_plan_entrust_list_event)
                                    msgEvent.msg_content = this
                                    CpEventBusUtil.post(msgEvent)
                                }
                            }
                        })
        )
    }

    private fun doCreateContractAccount() {

        if (authLevel!=3){
            if (authLevel==0){
                kycTips( getString(R.string.cl_kyc_4))
            }else if (authLevel==1){
                //审核通过
                if (futuresLocalLimit==1){
                    // 区域限制范围内 提示
                    kycTips( getString(R.string.cl_kyc_3))

                }else{
                    // 不在区域限制范围内
                    addDisposable(
                            getContractModel().createContract(
                                    consumer = object : CpNDisposableObserver(true) {
                                        override fun onResponseSuccess(jsonObject: JSONObject) {
                                            getContractUserConfig()
                                        }
                                    })
                    )
                }
            }else if (authLevel==2){
                //审核不通过
                if (futuresLocalLimit==1){
                    // 区域限制范围内 提示
                    kycTips( getString(R.string.cl_kyc_3))
                }else{
                    goKycTips( getString(R.string.cl_kyc_5))
                }

            }else{
                kycTips( getString(R.string.cl_kyc_7))
            }
        }else{
            goKycTips( getString(R.string.cl_kyc_2))
        }
    }

    private fun kycTips(s: String) {
        CpNewDialogUtils.showDialog(
                context!!,
               s,
                true,
                null,
                getString(R.string.cp_extra_text27),
                getString(R.string.cp_overview_text56)
        )
    }
    private fun goKycTips(s: String) {
        CpNewDialogUtils.showDialog(
                context!!,
                s.replace("\n","<br/>"),
                false,
                object : CpNewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_kyc_page)
                        )
                    }
                },
                getString(R.string.cl_kyc_1),
                getString(R.string.cl_kyc_6),
                getString(R.string.cp_overview_text56)
        )
    }

    private fun modifyMarginModel(marginModel: String) {
        addDisposable(getContractModel().modifyMarginModel(mContractId.toString(), marginModel,
                consumer = object : CpNDisposableObserver(true) {
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
            try {
                var obj: JSONObject = contractList.get(0) as JSONObject
                mContractId = obj.optInt("id")
                mSymbol = obj.optString("symbol")
                symbolPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(activity, mContractId)
                var isExitId = false;
                val id = CpClLogicContractSetting.getContractCurrentSelectedId(activity)

                //通知子页面更新合约id
                val event = CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_contract_id)
                event.msg_content = mContractId
                CpEventBusUtil.post(event)


                if (id == -1 && contractList.length() != 0) {
                    isExitId = true
                    showTabInfo(contractList[0] as JSONObject)
                } else {
                    for (i in 0..(contractList.length() - 1)) {
                        var obj = contractList.get(i) as JSONObject
                        if (id == obj.optInt("id")) {
                            isExitId = true
                            showTabInfo(obj)
                        }
                    }
                }
                if (!isExitId && contractList.length() != 0) {
                    showTabInfo(contractList[0] as JSONObject)
                }
                getContractUserConfig()
            }finally {

            }

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
//                v_horizontal_depth.mContractId
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_open_contract_event -> {
                doCreateContractAccount()
            }
            CpMessageEvent.sl_contract_switch_lever_event -> {
                modifyMarginModel(event.msg_content as String)
            }
            CpMessageEvent.sl_contract_left_coin_type -> {
                //切换币对后更新信息
                val obj = event.msg_content as JSONObject
                showTabInfo(obj)
            }
            CpMessageEvent.sl_contract_create_order_event -> {
//                ToastUtils.showShort("发起下单请求")
                LogUtils.e("我是二次弹窗--接收")
                val obj = event.msg_content as CpCreateOrderBean
                addDisposable(getContractModel().createOrder(obj,
                        consumer = object : CpNDisposableObserver(mActivity, true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                CpNToastUtil.showTopToastNet(
                                        this.mActivity,
                                        true,
                                        getString(R.string.cp_extra_text53)
                                )
                            }
                        })
                )
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
            }
            CpMessageEvent.sl_contract_receive_coupon -> {
                //领取模拟合约体验金
                receiveCoupon()
            }
            CpMessageEvent.sl_contract_modify_depth_event -> {
                //处理深度显示条数
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
        addDisposable(
                getContractModel().receiveCoupon(
                        consumer = object : CpNDisposableObserver(true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                getContractUserConfig()
                            }
                        })
        )
    }

    private fun modifyLevel(s: String) {
        addDisposable(getContractModel().modifyLevel(mContractId.toString(), s,
                consumer = object : CpNDisposableObserver(true) {
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
        currentSymbol = (obj.getString("contractType") + "_" + obj.getString("symbol")
                .replace("-", "")).toLowerCase()
        depthLevel = "0"
        tv_contract.text = CpClLogicContractSetting.getContractShowNameById(activity, mContractId)
        v_horizontal_depth.setContractJsonInfo(obj)
        var para: HashMap<String, Any> = hashMapOf("symbol" to currentSymbol, "step" to depthLevel)
        CpWsContractAgentManager.instance.sendMessage(para, this@CpContractNewTradeFragment)
        //通知子页面更新合约id
        val event = CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_contract_id)
        event.msg_content = mContractId
        CpEventBusUtil.post(event)


        loopStart()
    }






    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        isContractHidden=hidden
        if (hidden){
            loopStop()
        }else{
            isContractFirst=true
            getContractPublicInfo()
            v_horizontal_depth.setLoginContractLayout(CpClLogicContractSetting.isLogin(), openContract == 1)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isContractHidden&&isContractFirst){
            getContractPublicInfo()
            v_horizontal_depth.setLoginContractLayout(CpClLogicContractSetting.isLogin(), openContract == 1)
        }
    }


    override fun onPause() {
        super.onPause()
        loopStop()
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

    /**
     * WebSocket 发送消息
     */
    private fun sendMsg(msg: String) {

    }

    //初次 和 切换币对时触发
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
}