package com.yjkj.chainup.new_contract

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractTicker
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.listener.SLDoListener
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_contract.activity.ClMarketDetail4Activity
import com.yjkj.chainup.new_contract.activity.ClSelectLeverageActivity
import com.yjkj.chainup.new_contract.activity.ClSelectPositionActivity
import com.yjkj.chainup.new_contract.fragment.ClContractCoinSearchDialog
import com.yjkj.chainup.new_contract.fragment.ClContractHoldFragment
import com.yjkj.chainup.new_contract.fragment.ClContractTradeFragment
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.VerticalTextview4ChainUp
import com.yjkj.chainup.ws.WsContractAgentManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_cl_contract.*
import kotlinx.android.synthetic.main.fragment_new_version_inter_homepage.vtc_advertising
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * 合约
 */
class ClContractFragment : NBaseFragment(), SwipeRefreshLayout.OnRefreshListener, SLDoListener, WsContractAgentManager.WsResultCallback {

    override fun setContentView() = R.layout.fragment_cl_contract


    //tab类型  0 开仓  1 平仓  2 持仓  3 持仓
    private var currentIndex = 0

    private var bufferIndex = "-1"

    private var isFristShow = true
    private lateinit var tradeFragment: ClContractTradeFragment
    private lateinit var holdFragment: ClContractHoldFragment
    private var currentFragment = Fragment()
    private val handler = Handler()
    private var isFirstInputLastPrice: Boolean = true

    //左边币种选择弹窗
    private var contractAccountDialog: TDialog? = null

    //合约id
    private var mContractId = 0
    private lateinit var contractList: JSONArray
    var mContract: Contract? = null
    var updateInputPrice: Boolean = true

    private var pushJumpToType = -1

    private var timeDiff = 0L
    private var currentTimeMillis = 0L
    private var currentSymbol = ""
    private var depthLevel = "0"
    private var reqSymbol = ""
    private var multiplier = ""
    private var quote = ""
    private var base = ""
    private var contractType = ""

    private var lastFundRate = "0"
    private var currentFundRate = "0"

    private lateinit var symbolList: ArrayList<String>

    private var timeSlot = 0
    private var capitalFrequency = 0
    private var positionModel = 1 //1 单向持仓  非1 双向持仓
    private var startLong = 0L
    private var symbolPricePrecision = 2 //价格精度
    private var openContract = 0//是否开通了合约交易 1已开通, 0未开通
    private var lastTick: JSONObject? = null
    private var currentSymbolBuff = ""
    private var indexPrice = "0"
    private var couponTag = 1 //体验金标识 ： 0：未领取 1：已领取
    var subscribe: Disposable? = null
    var timeMillisSubscribe: Disposable? = null
    private var capitalRateList = ArrayList<String>()

    companion object {
        var contractLiveData4DepthData = MutableLiveData<MessageEvent>()
        var contractId = -1
    }

    override fun initView() {
        initAutoStringView()
        tradeFragment = ClContractTradeFragment()
        holdFragment = ClContractHoldFragment()
        tradeFragment.refreshListener = this
        holdFragment.refreshListener = this
        showFragment()
        initRollCapitalRate()
        initListener()

//        refresh_layout.setColorSchemeResources(R.color.colorAccent)
//        refresh_layout.setOnRefreshListener(this)
    }

    private fun initRollCapitalRate() {
        vtc_advertising?.setRight(true)
        vtc_advertising?.setText(12f, 0, ContextCompat.getColor(context!!, R.color.text_color),true)
        vtc_advertising?.setTextStillTime(8000)//设置停留时长间隔
        vtc_advertising?.setAnimTime(800)//设置进入和退出的时间间隔
        vtc_advertising?.setOnItemClickListener(object : VerticalTextview4ChainUp.OnItemClickListener {
            override fun onItemClick(pos: Int) {
            }
        })
        vtc_advertising?.startAutoScroll()
    }


    /**
     * 文本动态初始化
     */
    private fun initAutoStringView() {
        rb_open_position.onLineText("contract_text_openAverage")
        rb_close_position.onLineText("sl_str_close")
        rb_hold_position.onLineText("sl_str_position")
    }

    override fun loadData() {
        super.loadData()
        WsContractAgentManager.instance.addWsCallback(this)


        getDealerInfo()
    }

    private fun loadContractPublicInfo() {
        symbolList = ArrayList()
        addDisposable(getContractModel().getPublicInfo(
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            contractList = optJSONArray("contractList")
                            var marginCoinList = optJSONArray("marginCoinList")
                            currentTimeMillis = optString("currentTimeMillis").toLong()
                            getCurrentTimeMillis()
                            if (contractList.length() == 0) {
                                return
                            }
                            LogicContractSetting.setContractJsonListStr(context, contractList.toString())
                            LogicContractSetting.setContractMarginCoinListStr(context, marginCoinList.toString())
                            val arrays = arrayOfNulls<String>(contractList.length())
                            var msgEvent = MessageEvent(MessageEvent.sl_contract_first_show_info_event)

                            for (i in 0..(contractList.length() - 1)) {
                                var obj: JSONObject = contractList.get(i) as JSONObject
                                var currentSymbolBuff = (obj.getString("contractType") + "_" + obj.getString("symbol").replace("-", "")).toLowerCase()
                                arrays.set(i, currentSymbolBuff)
                                symbolList.add(currentSymbolBuff)
                                if (LogicContractSetting.getContractCurrentSelectedId(context) == obj.getInt("id")) {
                                    msgEvent.msg_content = obj
                                    currentSymbol = (obj.getString("contractType") + "_" + obj.getString("symbol").replace("-", "")).toLowerCase()
                                }
                            }
                            if (msgEvent.msg_content == null) {
                                msgEvent.msg_content = getContractJsonListSortOne(contractList)
                            }

                            var obj: JSONObject? = getContractJsonListSortOne(contractList)
                            obj?.apply {
                                if (TextUtils.isEmpty(currentSymbol)) {
                                    currentSymbol = (getString("contractType") + "_" + getString("symbol").replace("-", "")).toLowerCase()
                                }
                                WsContractAgentManager.instance.sendMessage(hashMapOf("symbol" to currentSymbol, "step" to depthLevel), this@ClContractFragment)
                            }
                            EventBusUtil.post(msgEvent)
                            showTabInfo(msgEvent.msg_content as JSONObject)
                        }
                    }
                }))

    }

    private fun loadContractUserConfig() {
        if (!UserDataService.getInstance().isLogined) {
            rb_hold_position.onLineText("sl_str_position")
            val msgEvent = MessageEvent(MessageEvent.sl_contract_login_status_event)
            msgEvent.msg_content = this
            EventBusUtil.post(msgEvent)
            return
        }
        if (contractId == -1) return
        addDisposable(getContractModel().getUserConfig(contractId.toString(),
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            var nowLevel = optString("nowLevel")
                            var marginModel = optInt("marginModel")
                            positionModel = optInt("positionModel")
                            couponTag = optInt("couponTag")
                            //1单向, 2双向
                            LogicContractSetting.setPositionModel(mActivity, positionModel)
                            //  1已开通, 0未开通
                            openContract = optInt("openContract")
                            tv_position.setText(if (marginModel == 1) getLineText("sl_str_full_position") else getLineText("sl_str_gradually_position"))
                            tv_lever.setText(nowLevel + "X")
                            var coUnit = optInt("coUnit")//合约单位 1标的货币, 2张
                            LogicContractSetting.setContractUint(context, if (coUnit == 1) 1 else 0)
//                            LogicContractSetting.setContractIsOpen(context, openContract)
                            val msgEvent = MessageEvent(MessageEvent.sl_contract_user_config_event)
                            msgEvent.msg_content = this
                            EventBusUtil.post(msgEvent)
                            settingPositionModel()
                            receiveCoupon()
                        }
                    }
                }))
    }


    private fun receiveCoupon() {
        if (!UserDataService.getInstance().isLogined) return
        if (openContract == 0) return
        if (!contractType.equals("S")) return
        if (couponTag == 1) return
        addDisposable(getContractModel().receiveCoupon(
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        loadContractUserConfig()
                    }
                }))
    }

    /**
     * 设置持仓模式
     */
    private fun settingPositionModel() {
        //currentIndex
        //UserDataService.getInstance().isLogined && openContract == 0
        /**
         * 此处需要先获取当前点击的位置，通过当前点击的位置以及当前是否登录和是否开通合约来展示
         */
        //双向持仓
        //1、当前点击在开仓tab
        //2、当前点击在平仓tab
        //3、当前点击在持仓tab
        //单向持仓
        //1、当前点击在提交委托tab
        //2、当前点击在持仓tab
        if (positionModel == 1) {
            //单向持仓
            rb_submit_entrust.visibility = View.VISIBLE
            rb_open_position.visibility = View.GONE
            rb_close_position.visibility = View.GONE
            if (UserDataService.getInstance().isLogined && openContract == 0) {
                rg_buy_sell.check(R.id.rb_submit_entrust)
            } else {
                if (currentIndex == 3 || currentIndex == 0) {
                    rg_buy_sell.check(R.id.rb_submit_entrust)
                } else {
                    rg_buy_sell.check(R.id.rb_hold_position)
                }
            }
        } else {
            //双向持仓
            rb_submit_entrust.visibility = View.GONE
            rb_open_position.visibility = View.VISIBLE
            rb_close_position.visibility = View.VISIBLE
            if (UserDataService.getInstance().isLogined && openContract == 0) {
                rg_buy_sell.check(R.id.rb_open_position)
            } else {
                if (currentIndex == 0) {
                    rg_buy_sell.check(R.id.rb_open_position)
                } else if (currentIndex == 1) {
                    rg_buy_sell.check(R.id.rb_close_position)
                } else if (currentIndex == 2) {
                    rg_buy_sell.check(R.id.rb_hold_position)
                } else {
                    rg_buy_sell.check(R.id.rb_open_position)
                }
            }

        }
    }

    /**
     * 获取资金费率
     */
    private fun getCapitalRate() {
        val c: Calendar = GregorianCalendar()
        capitalRateList.clear()
        addDisposable(getContractModel().getMarkertInfo(reqSymbol, contractId.toString(),
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            currentFundRate = optString("currentFundRate")
                            lastFundRate = optString("nextFundRate")
                            var timeMillisBuff = startLong
                            for (index in 0..timeSlot) {
                                c.timeInMillis = timeMillisBuff
                                if (currentTimeMillis < timeMillisBuff) {
                                    timeDiff = timeMillisBuff - currentTimeMillis
                                    capitalRateList.add(DateUtils.getHourMinNew(timeMillisBuff) + " " + getLineText("cl_funding_rate_str") + " " + if (currentFundRate.equals("null")) "--" else NumberUtil.getDecimal(5).format(MathHelper.round(MathHelper.mul(currentFundRate, "100"), 5)).toString() + "%")
                                    break
                                }
                                c.add(Calendar.HOUR, capitalFrequency)
                                LogUtil.e("timeSlot", DateUtils.getHourMin(c.timeInMillis))
                                timeMillisBuff = c.timeInMillis
                            }
                            c.timeInMillis = timeMillisBuff
                            c.add(Calendar.HOUR, capitalFrequency)
                            capitalRateList.add(DateUtils.getHourMinNew(c.timeInMillis) + " " + getLineText("cl_estimated_rate_str") + " " + if (lastFundRate.equals("null")) "--" else NumberUtil.getDecimal(5).format(MathHelper.round(MathHelper.mul(lastFundRate, "100"), 5)).toString() + "%")
                            vtc_advertising?.setTextList(capitalRateList)

                        }
                    }
                }))

    }

    private fun getMarkertInfo() {
        loopStop()
        subscribe = Observable.interval(0L, CommonConstant.capitalRateLoopTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (this::contractList.isInitialized && contractList.length() != 0) {
                        addDisposable(getContractModel().getMarkertInfo(reqSymbol, contractId.toString(),
                                consumer = object : NDisposableObserver() {
                                    override fun onResponseSuccess(jsonObject: JSONObject) {
                                        jsonObject.optJSONObject("data").run {
                                            tv_capital_rate?.apply {
                                                var tagPrice = optString("tagPrice")
                                                var fundRate = optString("currentFundRate")
                                                indexPrice = optString("indexPrice")
                                                currentFundRate = fundRate
                                                lastFundRate = optString("lastFundRate")
                                                LogUtil.e("标记价格", tagPrice)
                                                LogUtil.e("资金费率", fundRate)
                                                LogUtil.e("指数价格", indexPrice)
//                                                val rate = MathHelper.mul(fundRate, "100")
//                                                tv_capital_rate.text = NumberUtil.getDecimal(5).format(MathHelper.round(rate, 5)).toString() + "%"
                                                var obj = JSONObject()
                                                obj.put("tagPrice", BigDecimalUtils.scaleStr(tagPrice, symbolPricePrecision))
                                                obj.put("indexPrice", BigDecimalUtils.scaleStr(indexPrice, symbolPricePrecision))
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
    }

    private fun getCurrentTimeMillis() {
        timeMillisSubscribe = Observable.interval(0L, CommonConstant.currentTimeMillisLoopTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    currentTimeMillis = currentTimeMillis + 1000L
                    timeDiff = timeDiff - 1000L
                    LogUtil.e("currentTimeMillis", DateUtils.getYearMonthDayHourMinSecondMS(currentTimeMillis))
                    var showCountDownTime = DateUtils.formatLongToTimeStr(timeDiff)
                    showCountDownTime = if (showCountDownTime.equals("00:00:00") || timeDiff <= 0) "00:00:00" else showCountDownTime
                    tv_time?.setText(showCountDownTime)
                    capitalRateList.clear()
                    val msgEvent = MessageEvent(MessageEvent.sl_contract_rate_countdown_event)
                    msgEvent.msg_content = showCountDownTime
                    EventBusUtil.post(msgEvent)
                    val c: Calendar = GregorianCalendar()
                    var timeMillisBuff = startLong
                    if (showCountDownTime.equals("00:00:00")) {
                        for (index in 0..timeSlot) {
                            c.timeInMillis = timeMillisBuff
                            if (currentTimeMillis < timeMillisBuff) {
                                timeDiff = timeMillisBuff - currentTimeMillis
                                capitalRateList.add(DateUtils.getHourMinNew(timeMillisBuff) + " " + getLineText("cl_funding_rate_str") + " " + if (currentFundRate.equals("null")) "--" else NumberUtil.getDecimal(5).format(MathHelper.round(MathHelper.mul(currentFundRate, "100"), 5)).toString() + "%")
                                break
                            }
                            c.add(Calendar.HOUR, capitalFrequency)
                            LogUtil.e("timeSlot", DateUtils.getYearMonthDayHourMinSecondMS(c.timeInMillis))
                            timeMillisBuff = c.timeInMillis
                        }
                        c.timeInMillis = timeMillisBuff
                        c.add(Calendar.HOUR, capitalFrequency)
                        capitalRateList.add(DateUtils.getHourMinNew(c.timeInMillis) + " " + getLineText("cl_estimated_rate_str") + " " + if (lastFundRate.equals("null")) "--" else NumberUtil.getDecimal(5).format(MathHelper.round(MathHelper.mul(lastFundRate, "100"), 5)).toString() + "%")

                    }
                }
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
//            ContractPublicDataAgent.subscribeDepthWs(mContractId)
//            createContractAccount()
//            loadContractUserConfig()
        }
    }


    var hasShowCreateContractDialog = false


    /**
     * 开通合约账户弹窗
     */
    private fun realCreateContractAccount() {
        hasShowCreateContractDialog = true
        contractAccountDialog = SlDialogHelper.showCreateContractAccountDialog(activity!!, OnBindViewListener { viewHolder ->
            viewHolder?.let {
                it.getView<TextView>(R.id.tv_title).onLineText("sl_str_risk_disclosure")
                it.getView<TextView>(R.id.tv_content).text = getLineText("cl_str_risk_disclosure_notice", true)
                it.getView<TextView>(R.id.tv_confirm_btn).onLineText("sl_str_open_contract_account_btn")
                it.getView<TextView>(R.id.tv_content).movementMethod = ScrollingMovementMethod.getInstance()
                it.getView<TextView>(R.id.tv_confirm_btn).setOnClickListener {
                    contractAccountDialog?.dismiss()
                    doCreateContractAccount()
                }
            }
        })

    }

    private fun doCreateContractAccount() {
        addDisposable(getContractModel().createContract(
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        ToastUtils.showToast(context, getLineText("sl_str_account_created_successfully"))
                        loadContractUserConfig()
                    }
                }))
    }

    /**
     * 获取用户信息
     */
    private fun getDealerInfo() {
        if (UserDataService.getInstance().isLogined) {
            addDisposable(getMainModel().getUserInfo(object : NDisposableObserver() {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    val json = jsonObject.optJSONObject("data")
                    UserDataService.getInstance().saveData(json)
                }
            }))
        }
    }

    /**
     * 更新头部View
     */
    private fun updateHeaderView(ticker: ContractTicker) {
        if (tv_contract == null) {
            return
        }
        mContract?.let {
            //合约名称
            tv_contract.text = it.getDisplayName(mActivity)
            val decimalFormat = DecimalFormat("###################.###########", DecimalFormatSymbols(Locale.ENGLISH))
            val dfPrice = NumberUtil.getDecimal(it.price_index - 1)
            val riseFallRate: Double = MathHelper.round(ticker.change_rate.toDouble() * 100, 2)
            val sRate = if (riseFallRate >= 0) "+" + NumberUtil.getDecimal(2).format(riseFallRate) + "%" else NumberUtil.getDecimal(2).format(riseFallRate) + "%"
            val color = ColorUtil.getMainColorType(riseFallRate >= 0)
            //最新成交价
            tv_last_price.text = dfPrice.format(MathHelper.round(ticker.last_px))
            tv_last_price.textColor = color
            //百分比
            tv_rate.text = sRate
            tv_rate.textColor = color
            tv_rate.backgroundResource = ColorUtil.getContractRateDrawable(riseFallRate >= 0)
            // LogUtil.d("lb","ticker.last_px:"+ticker.last_px)
        }

    }


    private fun initListener() {
        positionModel = LogicContractSetting.getPositionModel(mActivity)

//        settingPositionModel()
        /**
         * 切换开仓 平仓 持仓
         */
        rg_buy_sell?.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_open_position -> {
                    currentIndex = 0
                    tradeFragment.doSwitchTab(currentIndex)
                    showFragment()
                }
                R.id.rb_close_position -> {
                    currentIndex = 1
                    tradeFragment.doSwitchTab(currentIndex)
                    showFragment()
                }
                R.id.rb_hold_position -> {
                    if (LoginManager.checkLogin(mContext, true)) {
                        if (openContract == 0) {
                            EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                            if (positionModel == 1) {
                                rg_buy_sell?.check(R.id.rb_submit_entrust)
                            } else {
                                rg_buy_sell?.check(R.id.rb_open_position)
                            }
                        } else {
                            currentIndex = 2
                            showFragment()
                            EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_refresh_position_list_event))
                        }
                    } else {
                        if (positionModel == 1) {
                            rg_buy_sell?.check(R.id.rb_submit_entrust)
                        } else {
                            rg_buy_sell?.check(R.id.rb_open_position)
                        }
                    }
                }
                R.id.rb_submit_entrust -> {
                    currentIndex = 3
                    tradeFragment.doSwitchTab(currentIndex)
                    showFragment()
                }
            }
        }
        /**
         * 侧边栏
         */
        tv_contract.setOnClickListener {
            showLeftCoinWindow()
        }
        iv_more.setOnClickListener {
            SlDialogHelper.createContractSettingNew(activity, iv_more, contractId, indexPrice, openContract)
        }
        /**
         * K线
         */
        ib_kline.setOnClickListener {
            if (!Utils.isFastClick()) {
                val mIntent = Intent(mActivity!!, ClMarketDetail4Activity::class.java)
                mIntent.putExtra(ParamConstant.symbol, currentSymbolBuff)
                mIntent.putExtra("contractId", contractId)
                mIntent.putExtra("baseSymbol", base)
                mIntent.putExtra("quoteSymbol", quote)
                mIntent.putExtra("pricePrecision", symbolPricePrecision)
                mIntent.putExtra(ParamConstant.TYPE, ParamConstant.BIBI_INDEX)
                startActivity(mIntent)
            }
        }
        ll_position.setOnClickListener {
            if (LoginManager.checkLogin(context, true)) {
                if (openContract == 0) {
                    EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                    return@setOnClickListener
                }
                ClSelectPositionActivity.show(mActivity!!, contractId, 0, "0", 0)
            }
        }
        ll_lever.setOnClickListener {
            if (LoginManager.checkLogin(context, true)) {
                if (openContract == 0) {
                    EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                    return@setOnClickListener
                }
                ClSelectLeverageActivity.show(mActivity!!, contractId, multiplier, indexPrice)
            }
        }
    }


    /*
     * 处理线程跟发消息线程一致
     * 子类重载
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: MessageEvent) {
        when (event.msg_type) {
            MessageEvent.sl_contract_receive_coupon -> {
                //领取模拟合约体验金
                receiveCoupon()
            }
            MessageEvent.sl_contract_modify_margin_event -> {
                //保证金模式切换消息
                loadContractUserConfig()
            }
            MessageEvent.sl_contract_modify_leverage_event -> {
                //杠杆切换消息
                loadContractUserConfig()
            }
            MessageEvent.sl_contract_change_position_model_event -> {
                //持仓模式切换
                positionModel = LogicContractSetting.getPositionModel(mActivity)
                LogUtil.e(TAG, "positionModel:" + positionModel)
                if (positionModel == 1) {
                    bufferIndex == "3"  //展示提交委托
                } else {
                    bufferIndex == "0" // 展示开仓
                }
            }
            MessageEvent.sl_contract_create_account_event -> {
                //开通合约弹窗消息
                realCreateContractAccount()
            }
            MessageEvent.sl_contract_left_coin_type -> {
                //切换币对后更新信息
                val obj = event.msg_content as JSONObject
                currentSymbol = (obj.getString("contractType") + "_" + obj.getString("symbol").replace("-", "")).toLowerCase()
                depthLevel = "0"
                showTabInfo(obj)
                LogUtil.e(TAG, "onVisibleChanged==sl_contract_left_coin_type() ")
                WsContractAgentManager.instance.sendMessage(hashMapOf("symbol" to currentSymbol, "step" to depthLevel), this@ClContractFragment)
            }
            MessageEvent.sl_contract_position_num_event -> {
                //持仓数量改变
                val obj = event.msg_content as Int
                if (obj > 0) {
                    rb_hold_position.text = Html.fromHtml("<font> ${getLineText("sl_str_position")} <small>[$obj]</small> </font>")
                } else {
                    rb_hold_position.text = getLineText("sl_str_position")
                }
            }
            MessageEvent.sl_contract_depth_level_event -> {
                depthLevel = event.msg_content as String
                WsContractAgentManager.instance.sendMessage(hashMapOf("symbol" to currentSymbol, "step" to depthLevel), this@ClContractFragment)
            }

        }
    }

    private fun showTabInfo(obj: JSONObject) {
        isFirstInputLastPrice = true
        contractType = obj.getString("contractType")
        reqSymbol = obj.getString("symbol")
        multiplier = obj.getString("multiplier")
        base = obj.getString("base")
        quote = obj.getString("quote")

        contractId = obj.getInt("id")
        var capitalStartTime = obj.getInt("capitalStartTime")
        capitalFrequency = obj.getInt("capitalFrequency")

        tv_capital_rate_hour.setText(capitalFrequency.toString() + "H资金费率")

        //计算最后结束的时间
        timeSlot = 24 / capitalFrequency
        val c: Calendar = GregorianCalendar()
        val currentDate = DateUtils.getYearMonthDayHourMinSecond(currentTimeMillis)
        val startDate = currentDate.split(" ")[0] + " " + String.format("%02d", capitalStartTime) + ":00:00"
        startLong = SimpleDateFormat(DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND).parse(startDate).time
        var timeMillisBuff = startLong
        for (index in 0..timeSlot) {
            c.timeInMillis = timeMillisBuff
            if (currentTimeMillis < timeMillisBuff) {
                timeDiff = timeMillisBuff - currentTimeMillis
                break
            }
            c.add(Calendar.HOUR, capitalFrequency)
            LogUtil.e("timeSlot", DateUtils.getHourMin(c.timeInMillis))
            timeMillisBuff = c.timeInMillis
        }

        var coinResultVo = JSONObject(obj.get("coinResultVo").toString())
        symbolPricePrecision = coinResultVo.optInt("symbolPricePrecision")
        loadContractUserConfig()
        tv_contract.text = LogicContractSetting.getContractShowNameById(context, contractId)
        currentSymbolBuff = (obj.getString("contractType") + "_" + obj.getString("symbol").replace("-", "")).toLowerCase()
        getCapitalRate()
    }


    private fun showLeftCoinWindow() {
        if (Utils.isFastClick())
            return
        if (this::contractList.isInitialized) {
            var mContractCoinSearchDialog = ClContractCoinSearchDialog()
            var bundle = Bundle()
            bundle.putString("contractList", contractList.toString())
            mContractCoinSearchDialog.arguments = bundle
            mContractCoinSearchDialog.showDialog(childFragmentManager, "SlContractFragment")
        }
    }

    private fun showFragment() {
        val transaction = childFragmentManager.beginTransaction()
        if (currentIndex == 2) {
            if (!holdFragment.isAdded) {
                transaction.hide(currentFragment).add(R.id.fragment_container, holdFragment, "2")
            } else {
                transaction.hide(currentFragment).show(holdFragment)
            }
            currentFragment = holdFragment
        } else {
            if (currentFragment is ClContractTradeFragment) {
                return
            }
            if (!tradeFragment.isAdded) {
                transaction.hide(currentFragment).add(R.id.fragment_container, tradeFragment, "1")
            } else {
                transaction.hide(currentFragment).show(tradeFragment)
            }
            currentFragment = tradeFragment
        }
        transaction.commitNow()
    }

    override fun onRefresh() {
        when (currentIndex) {
            0, 1 -> {
                tradeFragment.onRefresh()
            }
        }
//        handler.postDelayed({
//            refresh_layout.isRefreshing = false
//        }, 250)
    }

    override fun doThing(obj: Any?): Boolean {
//        if (obj is Boolean) {
//            refresh_layout.isEnabled = obj
//        }
        return true
    }

    override fun onWsMessage(json: String) {
        handleData(json)
    }

    fun handleData(data: String) {
//        Log.d(TAG, "==111==24H行情:$data")
        try {
            val jsonObj = JSONObject(data)
            val channel = jsonObj.optString("channel")
            if (!jsonObj.isNull("tick")) {

                val tick = jsonObj.optJSONObject("tick")
                /**
                 * 24H行情
                 */
                if (!StringUtil.checkStr(currentSymbol)) {
                    return
                }
                if (!tick.isNull("rose")) {
                    for (i in 0..(contractList.length() - 1)) {
                        val obj: JSONObject = contractList.get(i) as JSONObject
                        val symbol = obj.getString("symbol")
                        val contractType = obj.getString("contractType")
                        val symbolbuff = symbol.replace("-", "")
                        if (channel.split("_")[2].equals(symbolbuff.toLowerCase()) && channel.split("_")[1].equals(contractType.toLowerCase())) {
                            obj.remove("rose")
                            obj.put("rose", tick.optString("rose"))
                        }
                    }
                }
                if (channel == WsLinkUtils.tickerFor24HLink(currentSymbol, isChannel = true)) {
                    if (tick == null) {
                        return
                    } else {
                        if (lastTick != null) {
                            var lastTime = lastTick?.optLong("ts") ?: 0L
                            var time = jsonObj.optLong("ts")
                            if (time < lastTime) {
                                return
                            }
                        }
                    }
                    lastTick = jsonObj
                    val rose = tick.optString("rose")
                    val close = tick.optString("close")
                    val pricePrecision = symbolPricePrecision
                    activity?.runOnUiThread {
                        tv_last_price?.run {
                            textColor = ColorUtil.getMainColorType(isRise = RateManager.getRoseTrend(rose) >= 0)
                            text = DecimalUtil.cutValueByPrecision(close, pricePrecision)
                        }
                        setFirstLastPric(DecimalUtil.cutValueByPrecision(close, pricePrecision))
//                        val marketName = "usdt"
//                        tv_rate?.text = "${RateManager.getCNYByCoinName(marketName, close)}"

                        val riseFallRate: Double = MathHelper.round(rose.toDouble() * 100, 2)
                        val sRate = if (riseFallRate >= 0) "+" + NumberUtil.getDecimal(2).format(riseFallRate) + "%" else NumberUtil.getDecimal(2).format(riseFallRate) + "%"
                        val color = ColorUtil.getMainColorType(riseFallRate >= 0)
                        tv_rate?.apply {
                            text = sRate
                            textColor = color
                            backgroundResource = ColorUtil.getContractRateDrawable(riseFallRate >= 0)
                        }
                    }
                    val msgEvent = MessageEvent(MessageEvent.sl_contract_cancel_last_price_event)
                    msgEvent.msg_content = close
                    EventBusUtil.post(msgEvent)
                }
                /**
                 * 深度
                 */
                LogUtil.e(TAG, "depth_level:" + depthLevel)
                if (channel == WsLinkUtils.getDepthLink(currentSymbol, isSub = true, step = depthLevel).channel) {
//                    LogUtil.d(TAG, "=======深度：$data")
                    val msgEvent = MessageEvent(MessageEvent.DEPTH_CONTRACT_DATA_TYPE)
                    msgEvent.msg_content = jsonObj
                    EventBusUtil.post(msgEvent)
                }
            }
            if (channel.equals("review")) {
                if (!jsonObj.isNull("data")) {
                    val jsonData = jsonObj.optJSONObject("data")
                    for (buff in symbolList) {
                        try {
                            LogUtil.e(TAG, buff + ":" + jsonData.optJSONObject(buff).optString("close"))
                            for (i in 0..(contractList.length() - 1)) {
                                val obj: JSONObject = contractList.get(i) as JSONObject
                                val symbol = obj.getString("symbol")
                                val contractType = obj.getString("contractType")
                                val symbolbuff = symbol.replace("-", "")
                                if (buff.split("_")[1].equals(symbolbuff.toLowerCase()) && buff.split("_")[0].equals(contractType.toLowerCase())) {
                                    obj.remove("rose")
                                    obj.put("rose", jsonData.optJSONObject(buff).optString("rose"))
                                }
                            }
                        } catch (e: Exception) {
                            LogUtil.e(TAG, e.message.toString())
                        }

                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setFirstLastPric(lastPrice: String) {
        if (isFirstInputLastPrice) {
            isFirstInputLastPrice = false
            val msgEvent = MessageEvent(MessageEvent.sl_contract_first_input_last_price_event)
            msgEvent.msg_content = lastPrice
            EventBusUtil.post(msgEvent)
        }
    }

    fun getContractJsonListSortOne(mJSONArray: JSONArray): JSONObject? {
        val mJSONList: MutableList<JSONObject?> = java.util.ArrayList()
        for (i in 0 until mJSONArray.length()) {
            var mJSONObject: JSONObject? = null
            try {
                mJSONObject = mJSONArray[i] as JSONObject
                mJSONList.add(mJSONObject)
            } catch (e: JSONException) {
                e.printStackTrace()
                return null
            }
        }
        mJSONList.sortBy { it?.optInt("sort") }
        if (mJSONList.size != 0) {
            return mJSONList[0]
        } else {
            return null
        }
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            if (TAG.equals("ClContractFragment")) {
                LogUtil.e(TAG, "合约展示fragmentVisibile")
//                tradeFragment.doSwitchTab(currentIndex)
                loadContractUserConfig()
                getMarkertInfo()
                if (!UserDataService.getInstance().isLogined) {
                    tv_position.setText(getString(R.string.cl_currentsymbol_marginmodel1))
                    tv_lever.setText("20X")
                }
            }
        } else {
            subscribe?.dispose()
        }
    }

    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
        LogUtil.e(TAG, "合约展示onVisibleChanged")
        LogUtil.e(TAG, "onVisibleChanged==ClContractFragment() ${isVisible} ")
        val mMessageEvent = MessageEvent(MessageEvent.sl_contract_page_hide_event)
        mMessageEvent.msg_content = isVisible
        EventBusUtil.post(mMessageEvent)
        if (isVisible) {
            loadContractPublicInfo()
            showOpenContracDialog()
            getMarkertInfo()
        } else {
            if (currentSymbol.isNotEmpty()) {
                WsContractAgentManager.instance.unbind(this, true)
            }
            subscribe?.dispose()
        }
        if (!UserDataService.getInstance().isLogined) {
            rb_hold_position.onLineText("sl_str_position")
            EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_logout_event))
        }
    }

    private fun showOpenContracDialog() {
        if (!UserDataService.getInstance().isLogined) return
        addDisposable(getContractModel().getUserConfig("0",
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            //  1已开通, 0未开通
                            openContract = optInt("openContract")
                            if (openContract == 0) {
                                EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_logout_event))
                            }
                            if (openContract == 0 && !hasShowCreateContractDialog) {
                                realCreateContractAccount()
                            }
                        }
                    }
                }))
    }

    private fun loopStop() {
        if (subscribe != null) {
            subscribe?.dispose()
        }
    }
}