package com.yjkj.chainup.new_contract.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.common.sdk.impl.IResponse
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.common.sdk.utlis.TimeFormatUtils
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractFundingRate
import com.contract.sdk.data.DepthData
import com.contract.sdk.impl.ContractDepthListener
import com.google.gson.Gson
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.contract.activity.SlContractDetailActivity
import com.yjkj.chainup.contract.adapter.BuySellContractAdapter
import com.yjkj.chainup.contract.data.bean.TabInfo
import com.yjkj.chainup.contract.extension.showBaseName
import com.yjkj.chainup.contract.extension.showQuoteName
import com.yjkj.chainup.contract.helper.SLContractBuyOrSellHelper
import com.yjkj.chainup.contract.listener.SLDoListener
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.*
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_contract.activity.ClContractEntrustActivity
import com.yjkj.chainup.new_contract.adapter.ClContractPriceEntrustNewAdapter
import com.yjkj.chainup.new_contract.bean.ClCurrentOrderBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.UISegmentedView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.cl_include_contract_trade_left_layout.*
import kotlinx.android.synthetic.main.cl_item_transaction_detail.view.*
import kotlinx.android.synthetic.main.fragment_cl_contract_trade.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColor
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 * 合约交易
 */
class ClContractTradeFragment : NBaseFragment(), BuySellContractAdapter.OnBuySellContractClickedListener, LogicContractSetting.IContractSettingListener, SLDoListener {
    override fun setContentView(): Int {
        return R.layout.fragment_cl_contract_trade
    }

    /**
     * 限价模式下，判断是否是高级委托
     */
    private var isAdvancedLimit = false
    var orderTypeList = ArrayList<TabInfo>()
    var orderTypeDialog: TDialog? = null

    //盘口精度
    var depthList = ArrayList<TabInfo>()
    var depthDialog: TDialog? = null
    var depthType: TabInfo? = null

    //订单成交方式 1:普通,2:FOK,3:IOC
    var currAdvancedLimit = 1
    var advancedLimitTypeList = ArrayList<TabInfo>()

    var mContract: Contract? = null
    var mContractJson: JSONObject? = null
    private var mContractId = -1

    //盘面
    var diskTypeList = ArrayList<TabInfo>()
    var currDiskType: TabInfo? = null
    var sidkDialog: TDialog? = null
    var tapeDialog: TDialog? = null
    var tapeLevel = 0

    //资金费率
    var feeDialog: TDialog? = null
    var llFeeWarpLayout: LinearLayout? = null

    var inflater: LayoutInflater? = null

    // 0 限价委托 1 计划委托
    private var tabEntrustIndex = 0

    //合约深度

    private var tagPrice = "0.00"
    private var indexPrice = "0.00"

    private var canCloseVolumeBuy = "0"
    private var canCloseVolumeSell = "0"

    private var buyMaxPrice = 0.0
    private var askMaxPrice = 0.0
    private var lastPrice = "0.00"

    private var coUnit = 0
    private var multiplier = "0"
    private var multiplierPrecision = 0

    private var tabIndex = 0
    private var openContract = 0//是否开通了合约交易 1已开通, 0未开通

    private lateinit var userConfigObj: JSONObject
    private lateinit var userAccountObj: JSONObject
    private lateinit var positionListObj: JSONArray
    var transactionData: JSONObject? = null
    var depth_level = 2

    /**
     * 卖盘的item
     */
    private var sellViewList = mutableListOf<View>()

    /**
     * 买盘的item
     */
    private var buyViewList = mutableListOf<View>()

    /**
     * 买卖数据辅助类
     */
    private val buyOrSellHelper = SLContractBuyOrSellHelper()

    /**
     * 刷新监听
     */
    var refreshListener: SLDoListener? = null


    var percent: String? = "0.0"
    var canBuy: String = "0.0 "
    var canSell: String = "0.0 "
    var canOpen: String = "0.0 "
    var canOpenBuff: String = "0.0 "
    var etPositionStr: String = ""
    var isPercentPlaceOrder = false

    private var mContractPriceEntrustAdapter: ClContractPriceEntrustNewAdapter? = null
    private lateinit var mCurrentOrderList: ArrayList<ClCurrentOrderBean>
    private lateinit var mCurrentOrderListBuffer: ArrayList<ClCurrentOrderBean>


    override fun initView() {
        initAutoStringView()
        LogicContractSetting.getInstance().registListener(this)
        inflater = LayoutInflater.from(context)
        doSwitchTab(0)
        updateLeverUI()
        updateBtnUI()
        changeDiskTypeUi()
        initListener()
        updateOrderType(false)
        initDetailView()
        tv_change_depth?.text = "0.01"

        mCurrentOrderList = ArrayList()

        mCurrentOrderList = ArrayList()
        mCurrentOrderListBuffer = ArrayList()
        rv_hold_contract.layoutManager = LinearLayoutManager(context)
        mContractPriceEntrustAdapter = this!!.context?.let { ClContractPriceEntrustNewAdapter(it, mCurrentOrderList) }
        rv_hold_contract.adapter = mContractPriceEntrustAdapter
        mContractPriceEntrustAdapter?.setEmptyView(EmptyForAdapterView(context ?: return))

        mContractPriceEntrustAdapter?.addChildClickViewIds(R.id.tv_cancel_common, R.id.tv_cancel_plan, R.id.tv_order_type_common)
        mContractPriceEntrustAdapter?.setOnItemChildClickListener { adapter, view, position ->
            cancelOrder((adapter.data[position] as ClCurrentOrderBean).id)
        }
    }

    private fun cancelOrder(orderId: String) {
        NewDialogUtils.showDialog(context!!, getLineText("sl_str_cancel_order_tips"), false, object : NewDialogUtils.DialogBottomListener {
            override fun sendConfirm() {
                addDisposable(getContractModel().orderCancel(mContractId.toString(), orderId, tabEntrustIndex == 1,
                        consumer = object : NDisposableObserver() {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                if (tabEntrustIndex == 0) {
                                    getCurrentOrderList()
                                } else {
                                    getCurrentPlanOrderList()
                                }
                            }
                        }))
            }
        }, getLineText("common_text_tip"), getLineText("common_text_btnConfirm"), getLineText("common_text_btnCancel"))
    }

    /**
     * 文本动态初始化
     */
    private fun initAutoStringView() {
        tv_order_type.onLineText("sl_str_limit_entrust")
        tv_lever_title.onLineText("contract_action_lever")
        tab_latest_price.onLineText("sl_str_latest_price_simple")
        tab_fair_price.onLineText("sl_str_fair_price_simple")
        tab_index_price.onLineText("sl_str_index_price_simple")
        et_position.hint = getLineText("sl_str_amount")
//        tv_balance_title.onLineText("sl_str_avbl")
        tab_market_price.onLineText("sl_str_market_order")
        tab_buy1.onLineText("sl_str_buy1_price")
        tab_sell1.onLineText("sl_str_sell1_price")
        tv_price_hint.onLineText("contract_action_marketPrice")
        tv_price_title.onLineText("contract_text_price")
//        tv_index_price_title.onLineText("cl_currentsymbol_title2")
        tv_fair_price.onLineText("sl_str_fair_price")
        iv_funds_rate.onLineText("sl_str_funds_rate")
//        rb_limit_entrust.text = getLineText("sl_str_limit_entrust")
//        rb_plan_entrust.text = getLineText("sl_str_plan_entrust")
        ll_all_entrust_order.onLineText("common_action_sendall")
        et_price.hint = getLineText("contract_text_price")
        tv_long_title2.onLineText("sl_str_position")
        tv_short_title2.onLineText("sl_str_position")
        tv_market_price_hint.onLineText("sl_str_market_price")
        tv_sell_cost_label.onLineText("contract_text_mybeCost")
        tv_buy_cost_label.onLineText("contract_text_mybeCost")
        tv_amount.text = getLineText("sl_str_amount") + "(" + getLineText("contract_text_volumeUnit") + ")"
        coUnit = LogicContractSetting.getContractUint(ContractSDKAgent.context)
    }


    override fun loadData() {
        super.loadData()
        //订单类型
        orderTypeList.add(TabInfo(getLineText("cl_limit_order_str"), 1))
        orderTypeList.add(TabInfo(getLineText("cl_market_order_str"), 2))
        orderTypeList.add(TabInfo(getLineText("cl_stop_order_str"), 3))
        orderTypeList.add(TabInfo("Post Only", 4))
        orderTypeList.add(TabInfo("IOC", 5))
        orderTypeList.add(TabInfo("FOK", 6))
//        orderTypeList.add(TabInfo(getLineText("sl_str_plan_entrust"), CONTRACT_ORDER_PLAN))
//        orderTypeList.add(TabInfo(getLineText("sl_str_limit_advanced_entrust"), CONTRACT_ORDER_ADVANCED_LIMIT))

        //高级委托设置类型
        advancedLimitTypeList.add(TabInfo(getLineText("sl_str_item_post_only"), 1))
        advancedLimitTypeList.add(TabInfo(getLineText("sl_str_item_fok"), 2))
        advancedLimitTypeList.add(TabInfo(getLineText("sl_str_item_ioc"), 3))

        ///杠杆
        initLeverageData()

        //盘面
        diskTypeList.add(TabInfo(getLineText("sl_str_default"), AppConstant.DEFAULT_TAPE))
        diskTypeList.add(TabInfo(getLineText("sl_str_ask"), AppConstant.SELL_TAPE))
        diskTypeList.add(TabInfo(getLineText("sl_str_bid"), AppConstant.BUY_TAPE))
        currDiskType = diskTypeList[0]

        //注册深度监听
        ContractPublicDataAgent.registerDepthWsListener(this, getDepthSubCount(), deepListener)

        mCurrentOrderList = ArrayList()
    }

    private fun getDepthSubCount(): Int {
        currDiskType?.let {
            return if (it.index == AppConstant.DEFAULT_TAPE) {
                5
            } else {
                10
            }
        }
        return 5
    }

    private val deepListener = object : ContractDepthListener() {

        override fun onWsContractDepth(contractId: Int, buyList: java.util.ArrayList<DepthData>, sellList: java.util.ArrayList<DepthData>) {
            if (contractId == mContract?.instrument_id) {
                if (isHidden || !isVisible) {
                    return
                }
                if (buyList.isNotEmpty()) {
                    updateDepth(true, buyList)
                }
                if (sellList.isNotEmpty()) {
                    updateDepth(false, sellList)
                }
            }
        }

    }
    private val updateAvailableVolListener = object : SLDoListener {
        override fun doThing(obj: Any?): Boolean {
            updateAvailableVol()
            return true
        }
    }

    private val updatePriceListener = object : SLDoListener {
        override fun doThing(obj: Any?): Boolean {
            var price: String = et_price.text.toString()
            if (price.isEmpty()) {
                price = "0"
            }
            updateAvailableVol()
            return true
        }
    }

    private val updatePositionListener = object : SLDoListener {
        override fun doThing(obj: Any?): Boolean {
            var position = et_position.text.toString()
            var price = et_price.text.toString()
            if (position.isEmpty()) {
                position = "0"
            }
            updateAvailableVol()
            return true
        }
    }

    private fun initLeverageData() {
        mContract?.let {
            GlobalLeverageUtils.bindContract(it)
            GlobalLeverageUtils.updateLeverageListener = this
        }
    }

    private fun initListener() {
        et_trigger_price.numberFilter(3, otherFilter = updateAvailableVolListener)
        et_price.numberFilter(3, otherFilter = updatePriceListener)
        et_position.numberFilter(3, otherFilter = updatePositionListener)
        //切换限价个计划列表tab
        rg_tab_layout.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_limit_entrust -> {
                    tabEntrustIndex = 0
                    getCurrentOrderList()
                }
                R.id.rb_plan_entrust -> {
                    tabEntrustIndex = 1
                    getCurrentPlanOrderList()
                }
            }

        }
        //跳转全部委托
        ll_all_entrust_order.setOnClickListener {
            if (LoginManager.checkLogin(activity, true)) {
                if (openContract == 0) {
                    EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                    return@setOnClickListener
                }
                ClContractEntrustActivity.show(mActivity!!, mContractId, tabEntrustIndex)
            }
        }
        //标记价格弹窗
        tv_flag_price_title.setOnClickListener {
            NewDialogUtils.showDialog(context!!, getLineText("cl_mark_price_tip_str", true), true, null, getLineText("contract_price_tag"), getLineText("alert_common_i_understand"))
        }
        //指数价格弹窗
        tv_index_price_title.setOnClickListener {
            NewDialogUtils.showDialog(context!!, getLineText("cl_index_price_tip_str", true), true, null, getLineText("contract_text_indexPrice"), getLineText("alert_common_i_understand"))
        }
        //合理价格弹窗
        tv_fair_price.setOnClickListener {
            NewDialogUtils.showDialog(context!!, getLineText("sl_str_fair_price_intro", true), true, null, getLineText("sl_str_fair_price"), getLineText("alert_common_i_understand"))
        }
        //资金费率
        iv_funds_rate.setOnClickListener {
            showFundsRateDialog()
        }
        ///限价、计划,高级委托切换
        tv_order_type.setOnClickListener {
            showOrderTypeDialog()
        }
        ///订单类型切换
        rl_seletc_contract_order_type.setOnClickListener {
            showContractOrderTypeDialog()
        }
        ///改变盘面
        ib_disk_layout.setOnClickListener {
            showSelectDiskDialog()
        }

        /**
         * 改变盘口的样式
         */
        ib_tape?.setOnClickListener {
            tapeDialog = NewDialogUtils.showBottomListDialog(context!!, arrayListOf(LanguageUtil.getString(context, "contract_text_defaultMarket"), LanguageUtil.getString(context, "contract_text_buyMarket"), LanguageUtil.getString(context, "contract_text_sellMarket")), tapeLevel, object : NewDialogUtils.DialogOnclickListener {
                override fun clickItem(data: ArrayList<String>, item: Int) {
                    tapeDialog?.dismiss()
                    tapeLevel = item
                    changeTape(item)
                }
            })
        }

        //市场价
        tab_market_price.setOnClickListener {
            buyOrSellHelper.priceType = if (buyOrSellHelper.priceType == CONTRACT_ORDER_MARKET) {
                CONTRACT_ORDER_LIMIT
            } else {
                CONTRACT_ORDER_MARKET
            }
            updateOrderType(true)
        }
        //买一价
        tab_buy1.setOnClickListener {
            buyOrSellHelper.priceType = if (buyOrSellHelper.priceType == CONTRACT_ORDER_BID_PRICE) {
                CONTRACT_ORDER_LIMIT
            } else {
                CONTRACT_ORDER_BID_PRICE
            }
            updateOrderType(true)
        }
        //卖一价
        tab_sell1.setOnClickListener {
            buyOrSellHelper.priceType = if (buyOrSellHelper.priceType == CONTRACT_ORDER_ASK_PRICE) {
                CONTRACT_ORDER_LIMIT
            } else {
                CONTRACT_ORDER_ASK_PRICE
            }
            updateOrderType(true)
        }
        //最新价
        tab_latest_price.setOnClickListener {
            LogicContractSetting.setTriggerPriceType(mActivity, 1)
            onContractSettingChange()
        }
        //合理价
        tab_fair_price.setOnClickListener {
            LogicContractSetting.setTriggerPriceType(mActivity, 2)
            onContractSettingChange()
        }
        //指数价
        tab_index_price.setOnClickListener {
            LogicContractSetting.setTriggerPriceType(mActivity, 4)
            onContractSettingChange()
        }
        //计划-市价
        tv_price_hint.setOnClickListener {
            //0限价 1市价
            if (LogicContractSetting.getExecution(ContractSDKAgent.context) == 1) {
                LogicContractSetting.setExecution(ContractSDKAgent.context, 0)
                tv_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_unfocused)
                et_price.requestFocus()
            } else {
                LogicContractSetting.setExecution(ContractSDKAgent.context, 1)
                tv_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_focused)
                et_price.clearFocus()
            }
            updateAvailableVol()

//            buyOrSellHelper.priceType = CONTRACT_ORDER_PLAN
//            updateOrderType(true)
        }
        //计划委托规则弹窗
        iv_plan_rule.setOnClickListener {
            NewDialogUtils.showDialog(context!!, getLineText("sl_str_plan_entrust_intro", true), true, null, getLineText("sl_str_plan_entrust"), getLineText("alert_common_i_understand"))
        }
        //买入
        stv_buy.isEnable(true)
        stv_buy.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (LoginManager.checkLogin(activity, true)) {
                    if (openContract == 0) {
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                        return
                    }
                    buyOrSellHelper.isBuy = true
                    doBuyOrSell("BUY")
                }
            }
        }
        //卖出
        stv_sell.isEnable(true)
        stv_sell.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (LoginManager.checkLogin(activity, true)) {
                    if (openContract == 0) {
                        EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                        return
                    }
                    buyOrSellHelper.isBuy = false
                    doBuyOrSell("SELL")
                }
            }

        }

        //焦点变化改变输入框背景
        et_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_price?.setBackgroundResource(if (hasFocus) R.drawable.bg_trade_et_focused else R.drawable.bg_trade_et_unfocused)
            if (hasFocus) {
                LogicContractSetting.setExecution(ContractSDKAgent.context, 0)
                tv_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_unfocused)
            }
            updateAvailableVol()
        }
        et_position?.setOnFocusChangeListener { _, hasFocus ->
            ll_position?.setBackgroundResource(if (hasFocus) R.drawable.bg_trade_et_focused else R.drawable.bg_trade_et_unfocused)
        }
        et_trigger_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_trigger_price?.setBackgroundResource(if (hasFocus) R.drawable.bg_trade_et_focused else R.drawable.bg_trade_et_unfocused)
        }

        img_transfer.setOnClickListener {
            if (LoginManager.checkLogin(context, true)) {
                if (openContract == 0) {
                    EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_create_account_event))
                    return@setOnClickListener
                }
                mContractJson?.let {
                    ArouterUtil.navigation(RoutePath.NewVersionTransferActivity, Bundle().apply {
                        putString(ParamConstant.TRANSFERSTATUS, ParamConstant.TRANSFER_CONTRACT)
                        putString(ParamConstant.TRANSFERSYMBOL, it.optString("marginCoin"))
                    })
                }
            }
        }
        rl_seletc_precision.setOnClickListener {
            showPositionPrecisionDialog()
        }

        cb_noly_reduce_position.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {//是 只减仓
                buyOrSellHelper.tradeType = 1
                tv_long_title.onLineText("sl_str_sell_empty")
                tv_short_title.onLineText("sl_str_sell_max")
                ll_buy_cost.visibility = View.VISIBLE
                ll_sell_cost.visibility = View.VISIBLE
            } else {//否 只减仓
                buyOrSellHelper.tradeType = 0
                tv_long_title.onLineText("sl_str_buy_open_up_to")
                tv_short_title.onLineText("sl_str_sell_open_up_to")
                ll_buy_cost.visibility = View.VISIBLE
                ll_sell_cost.visibility = View.VISIBLE
            }
            tv_long_value.text = "--"
            tv_short_value.text = "--"
            tv_long_value2.text = "--"
            tv_short_value2.text = "--"
            tv_buy_cost.text = " --"
            tv_sell_cost.text = " --"

            resetInputPosition()
            updateBtnUI()
            //可开可平数量
            updateAvailableVolUI()
        }
        tv_percent.setOnClickListener {
            resetInputPosition()
        }

        rg_trade.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                when (checkedId) {
                    R.id.rb_1st -> {
                        percent = "0.10"
                        adjustRatio("0.10")
                        tv_percent.setText(rb_1st.text.toString())
                    }

                    R.id.rb_2nd -> {
                        percent = "0.20"
                        adjustRatio("0.20")
                        tv_percent.setText(rb_2nd.text.toString())
                    }

                    R.id.rb_3rd -> {
                        percent = "0.50"
                        adjustRatio("0.50")
                        tv_percent.setText(rb_3rd.text.toString())
                    }

                    R.id.rb_4th -> {
                        percent = "1.0"
                        adjustRatio("1.0")
                        tv_percent.setText(rb_4th.text.toString())
                    }
                    else -> {
                        percent = "0.0"
                        tv_percent.visibility = View.GONE
                        et_position.visibility = View.VISIBLE
                        et_position.setText("")
                    }
                }
            }
        })
        sv_trade.setOnSelectionChangedListener(object : UISegmentedView.OnSelectionChangedListener {
            override fun onSelectionChanged(position: Int, value: String?) {
            }

            override fun onSelectionChanged(position: Int) {
                LogUtil.e(TAG, "position:" + position)
                when (position) {
                    0 -> {
                        percent = "0.10"
                        adjustRatio("0.10")
                        tv_percent.setText(rb_1st.text.toString())
                    }
                    1 -> {
                        percent = "0.20"
                        adjustRatio("0.20")
                        tv_percent.setText(rb_2nd.text.toString())
                    }
                    2 -> {
                        percent = "0.50"
                        adjustRatio("0.50")
                        tv_percent.setText(rb_3rd.text.toString())
                    }
                    3 -> {
                        percent = "1.0"
                        adjustRatio("1.0")
                        tv_percent.setText(rb_4th.text.toString())
                    }
                    else -> {
                        percent = "0.0"
                        tv_percent.visibility = View.GONE
                        et_position.visibility = View.VISIBLE
                        tv_volume_value.visibility = View.VISIBLE
                        isPercentPlaceOrder = false
                        et_position.setText("")
                    }
                }
            }
        })

    }

    private fun resetInputPosition() {
        tv_percent.visibility = View.GONE
        et_position.visibility = View.VISIBLE
        tv_volume_value.visibility = View.VISIBLE
        et_position.setText("")
        isPercentPlaceOrder = false
        rg_trade.clearCheck()
        sv_trade.clear()
    }

    private fun adjustRatio(proportion: String) {
        tv_percent.visibility = View.VISIBLE
        et_position.visibility = View.GONE
        tv_volume_value.visibility = View.INVISIBLE
        isPercentPlaceOrder = true
        var isOpen = false
        when (buyOrSellHelper.tradeType) {
            0 -> {
                isOpen = true
            }
            1 -> {
                isOpen = false
            }
            else -> isOpen = false
        }
        if (buyOrSellHelper.tradeType == 3) {
            if (cb_noly_reduce_position.isChecked) {
                isOpen = false
            } else {
                isOpen = true
            }
        }
        var canUseAmount = "0"
        if (this::userAccountObj.isInitialized) {
            userAccountObj?.let {
                canUseAmount = it.optString("canUseAmount")
            }
        }
        var level = "20"
        if (this::userConfigObj.isInitialized) {
            userConfigObj?.let {
                level = it.optString("nowLevel")
                if (TextUtils.isEmpty(level)) {
                    level = "20";
                }
            }
        }
        var price = ""
        var etPriceStr = et_price.text.toString()
        var etPositionStr = et_position.text.toString()
        var etTriggerPriceStr = et_trigger_price.text.toString()
        when (buyOrSellHelper.priceType) {
            1, 4, 5, 6 -> {//限价单
                price = etPriceStr
            }
            2 -> {//市价单
                price = BigDecimalUtils.median(buyMaxPrice.toString(), askMaxPrice.toString(), lastPrice)  //使用本交易所最新价格
            }
            3 -> {
                //0限价 1市价
                if (LogicContractSetting.getExecution(ContractSDKAgent.context) == 1) {
                    //条件市价单
                    price = etTriggerPriceStr
                } else {//条件限价单
                    price = etPriceStr
                }
            }
        }
        var multiplierPrecision = if (BigDecimalUtils.subZeroAndDot(multiplier).indexOf(".") > 0) BigDecimalUtils.subZeroAndDot(multiplier).split(".")[1].length else 0//合约面值精度
        mContractJson?.let {
            var marginRate = it.optString("marginRate")
            var contractSide = it?.optString("contractSide")
            val canBuy = BigDecimalUtils.canBuyStr(isOpen, buyOrSellHelper.priceType == 2, contractSide.equals("1"), price, multiplier, canUseAmount, canCloseVolumeBuy, level, marginRate, multiplierPrecision, "").trim()
            et_position.setText(BigDecimalUtils.mulStr(canBuy, proportion, multiplierPrecision))
        }
    }

    /**
     * 更新杠杆UI
     */
    private fun updateLeverUI() {
        tv_lever?.text = if (GlobalLeverageUtils.currentPositionType == 1) {
            getLineText("sl_str_gradually_position") + GlobalLeverageUtils.currLeverage + "X"
        } else {
            getLineText("sl_str_full_position") + GlobalLeverageUtils.currLeverage + "X"
        }
    }

    /**
     * 展示资金费率提示框
     */
    private fun showFundsRateDialog() {
        if (mContract == null) {
            return
        }
        if (Utils.isFastClick())
            return
        feeDialog = SlDialogHelper.showFundsRateDialog(context!!, OnBindViewListener { viewHolder ->
            viewHolder?.let {
                it.getView<TextView>(R.id.tv_title).onLineText("sl_str_funds_rate")
                it.getView<TextView>(R.id.tv_content).onLineText("sl_str_funds_rate_intro")
                it.getView<TextView>(R.id.tv_more).onLineText("sl_str_learn_more")
                it.getView<TextView>(R.id.tv_confirm_btn).onLineText("alert_common_i_understand")

                llFeeWarpLayout = it.getView(R.id.ll_fee_warp_layout)
                it.setOnClickListener(R.id.tv_more) {
                    SlContractDetailActivity.show(mActivity!!, mContract?.instrument_id!!, 2)
                    feeDialog?.dismiss()
                }
            }
        })
        ContractPublicDataAgent.loadFundingRate(mContract!!.instrument_id, object : IResponse<MutableList<ContractFundingRate>>() {
            override fun onSuccess(data: MutableList<ContractFundingRate>) {
                if (data != null && data.isNotEmpty()) {
                    for (i in 0 until Math.min(data.size, 4)) {
                        val item = data[i]
                        val itemView = inflater?.inflate(R.layout.sl_item_funding_rate_dlg, llFeeWarpLayout, false)
                        llFeeWarpLayout?.addView(itemView)
                        val tvTimeValue = itemView?.findViewById<TextView>(R.id.tv_time_value)
                        val tvFundingRateValue = itemView?.findViewById<TextView>(R.id.tv_funding_rate_value)
                        val rate: Double = MathHelper.mul(item.rate, "100")
                        tvFundingRateValue?.text = NumberUtil.getDecimal(4).format(rate).toString() + "%"

                        tvTimeValue?.text = TimeFormatUtils.timeStampToDate((item.timestamp + mContract!!.settlement_interval) * 1000, "yyyy-MM-dd  HH:mm:ss")
                    }
                }
            }

            override fun onFail(code: String, msg: String) {
                ToastUtils.showToast(ContractSDKAgent.context, msg)
            }
        })
    }

    /**
     * 展示 默认/卖盘/买盘
     */
    private fun showSelectDiskDialog() {
        sidkDialog = NewDialogUtils.showNewBottomListDialog(context!!, diskTypeList, currDiskType!!.index, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                if (currDiskType != diskTypeList[index]) {
                    currDiskType = diskTypeList[index]
                    changeDiskTypeUi()
                }
                sidkDialog?.dismiss()
                sidkDialog = null
            }
        })
    }

    /**
     * 盘口精度选择
     */
    private fun showPositionPrecisionDialog() {
        if (depthList.size == 0) return
        depthDialog = NewDialogUtils.showNewBottomListDialog(context!!, depthList, depthType!!.index, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                if (depthType != depthList[index]) {
                    depthType = depthList[index]
                    depth_level = depthType?.extras?.toInt()!!
                    tv_change_depth.setText(depthType?.name)
                    val mMessageEvent = MessageEvent(MessageEvent.sl_contract_depth_level_event)
                    mMessageEvent.msg_content = index.toString()
                    EventBusUtil.post(mMessageEvent)

                }
                depthDialog?.dismiss()
                depthDialog = null
            }
        })
    }

    private fun showOrderTypeDialog() {
        orderTypeDialog = NewDialogUtils.showNewBottomListDialog(context!!, orderTypeList, if (isAdvancedLimit) CONTRACT_ORDER_ADVANCED_LIMIT else buyOrSellHelper.priceType, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(item: Int) {
                tv_order_type?.text = orderTypeList[item].name
                orderTypeDialog?.dismiss()
                if (orderTypeList[item].index == CONTRACT_ORDER_ADVANCED_LIMIT) {//如果选择高级委托,priceType 按照普通限价类型走逻辑
                    isAdvancedLimit = true
                    buyOrSellHelper.priceType = CONTRACT_ORDER_LIMIT
                    //高级委托，市价单置灰
                    tab_market_price.isEnabled = false
                } else {
                    tab_market_price.isEnabled = true
                    isAdvancedLimit = false
                    buyOrSellHelper.priceType = orderTypeList[item].index
                }

                updateOrderType(true)
            }
        })
    }

    private fun showContractOrderTypeDialog() {
        orderTypeDialog = NewDialogUtils.showNewBottomListDialog(context!!, orderTypeList, buyOrSellHelper.priceType, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(item: Int) {
                tv_contract_order_type?.text = orderTypeList[item].name
                orderTypeDialog?.dismiss()
                buyOrSellHelper.priceType = orderTypeList[item].index
                updateOrderType(true)
            }
        })
    }

    /**
     * 更新盘面UI
     */
    fun changeDiskTypeUi(isInit: Boolean = false) {
        currDiskType?.index?.let {
            ColorUtil.setTapeIcon(ib_disk_layout, it)
            //切换了盘面，需处理数据
            if (!isInit) {
                deepListener.count = getDepthSubCount()
//                ll_sell_Layout.updateDeepType(it)
//                ll_buy_layout.updateDeepType(it)
            }
        }
    }

    /**
     * 更新订单类型
     */
    fun updateOrderType(updatePrice: Boolean) {

        iv_plan_rule.visibility = View.GONE
        tv_coin_name.visibility = View.VISIBLE
        tv_price_hint.visibility = View.GONE
        ll_trigger_price.visibility = View.GONE
        ll_order_advanced_setting.visibility = View.INVISIBLE
        tv_market_price_hint.visibility = View.GONE
        rg_trigger_type.visibility = View.GONE
        et_price.visibility = View.VISIBLE
        tv_order_tips_layout.visibility = View.GONE
        when (buyOrSellHelper.priceType) {
            1 -> {//限价单
                ll_price.visibility = View.VISIBLE
            }
            2 -> {//市价单
                //双向持仓
                tv_order_tips_layout.visibility = View.VISIBLE
                tv_order_tips_layout.setText(getLineText("cl_Trading_at_the_current_best_price_str"))
                ll_price.visibility = View.GONE
            }

            3 -> {//条件单
                //双向持仓
                tv_price_hint.visibility = View.VISIBLE
                ll_price.visibility = View.VISIBLE
                tv_trigger_coin_name.setText("USDT")
                ll_trigger_price.visibility = View.VISIBLE
                tv_order_tips_layout.visibility = View.GONE
                rg_order_type.visibility = View.GONE

                if (LogicContractSetting.getExecution(ContractSDKAgent.context) == 1) {
                    LogicContractSetting.setExecution(ContractSDKAgent.context, 0)
                    tv_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_unfocused)
                    et_price.requestFocus()
                } else {
                    LogicContractSetting.setExecution(ContractSDKAgent.context, 1)
                    tv_price_hint?.setBackgroundResource(R.drawable.bg_trade_et_focused)
                    et_price.clearFocus()
                }
            }

            4 -> {//PostOnly
                ll_price.visibility = View.VISIBLE
            }

            5 -> {//IOC
                ll_price.visibility = View.VISIBLE
            }

            5 -> {//FOK
                ll_price.visibility = View.VISIBLE
            }
        }
        updateAvailableVolUI()
    }

    private fun updateAdvancedLimitUiStatus() {
        if (isAdvancedLimit) {
            ll_order_advanced_setting.visibility = View.VISIBLE
        } else {
            ll_order_advanced_setting.visibility = View.INVISIBLE
        }
    }

    /**
     * 切换开仓和平仓
     */
    fun doSwitchTab(index: Int = 0) {
        tabIndex = index
        updateBtnUI()
        resetInputPosition()
        if (buyOrSellHelper.tradeType != index) {
            buyOrSellHelper.tradeType = index
            updateBtnUI()
            //重置杠杆颜色
            resetLeverTextColor()
            //可开可平数量
            updateAvailableVolUI()
        }

    }

    /**
     * /重置可开可平UI
     */
    private fun updateAvailableVolUI() {
        resetInputPosition()
        // * 0 开仓
        //     * 1 平仓
        if (buyOrSellHelper.tradeType == 1) {
            tv_long_title.onLineText("sl_str_sell_empty")
            tv_short_title.onLineText("sl_str_sell_max")
            ll_buy_cost.visibility = View.GONE
            ll_sell_cost.visibility = View.GONE
//            initDetailView(5)
        } else {
            tv_long_title.onLineText("sl_str_buy_open_up_to")
            tv_short_title.onLineText("sl_str_sell_open_up_to")
            ll_buy_cost.visibility = View.VISIBLE
            ll_sell_cost.visibility = View.VISIBLE
//            initDetailView(6)
        }
        tv_long_value.text = "--"
        tv_short_value.text = "--"
        tv_long_value2.text = "--"
        tv_short_value2.text = "--"
        tv_buy_cost.text = " --"
        tv_sell_cost.text = " --"
        et_trigger_price.setText("")
        et_price.setText("")
        et_position.setText("")
        updateAvailableVol()
    }

    var quoteUint = ""
    var baseUint = ""

    /**
     * 更新可开可平数量
     */
    fun updateAvailableVol() {
        if (et_price == null) {
            return
        }
        var etPriceStr = et_price.text.toString()
        etPositionStr = et_position.text.toString()

        LogUtil.e(TAG, "canOpenBuff:" + canOpenBuff)
        if (isPercentPlaceOrder) {
            etPositionStr = BigDecimalUtils.mulStr(canOpenBuff, percent, multiplierPrecision)
        }
        LogUtil.e(TAG, "etPositionStr:" + etPositionStr)
        var etTriggerPriceStr = et_trigger_price.text.toString()
        var canUseAmount = "0";
        var level = "20";
        if (this::userAccountObj.isInitialized) {
            userAccountObj?.let {
                canUseAmount = it.optString("canUseAmount")
            }
        }

        if (this::userConfigObj.isInitialized) {
            userConfigObj?.let {
                level = it.optString("nowLevel")
                if (TextUtils.isEmpty(level)) {
                    level = "20";
                }
            }
        }
        val mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(context, mContractId)
        mContractJson?.let {
            var marginRate = it.optString("marginRate")
            var marginCoin = it.optString("marginCoin")
            var multiplier = it?.optString("multiplier")
            var base = if (LogicContractSetting.getContractUint(ChainUpApp.appContext) == 0) getString(R.string.sl_str_contracts_unit) else it?.optString("multiplierCoin")
            var isOpen = false;
            var price = "";
            var contractSide = it?.optString("contractSide")
            quoteUint = it?.optString("quote")
            baseUint = it?.optString("base")
            LogUtil.e(TAG, buyOrSellHelper.tradeType.toString())
            when (buyOrSellHelper.tradeType) {
                0 -> {
                    isOpen = true
                }
                1 -> {
                    isOpen = false
                }
                else -> isOpen = false
            }
            if (buyOrSellHelper.tradeType == 3) {
                if (cb_noly_reduce_position.isChecked) {
                    isOpen = false
                } else {
                    isOpen = true
                }
            }
            when (buyOrSellHelper.priceType) {
                1, 4, 5, 6 -> {//限价单
                    price = etPriceStr
                }
                2 -> {//市价单
                    if (isOpen) {
                        price = BigDecimalUtils.median(buyMaxPrice.toString(), askMaxPrice.toString(), lastPrice)  //使用本交易所最新价格
                        if (isPercentPlaceOrder) {
                            if (LogicContractSetting.getContractUint(context) == 0) {
                                if (contractSide.equals("1")) {
                                    val buff1 = BigDecimalUtils.mulStr(etPositionStr, price, mPricePrecision)
                                    etPositionStr = BigDecimalUtils.mulStr(multiplier, buff1, mPricePrecision)
                                } else {
                                    val buff1 = BigDecimalUtils.mulStr(etPositionStr, multiplier, mPricePrecision)
                                    etPositionStr = BigDecimalUtils.divStr(buff1, price, mPricePrecision)
                                }
                            } else {
                                if (contractSide.equals("1")) {
                                    etPositionStr = BigDecimalUtils.mulStr(etPositionStr, price, mPricePrecision)
                                } else {
                                    etPositionStr = BigDecimalUtils.divStr(etPositionStr, price, mPricePrecision)
                                }
                            }
                        }
                    }

                }
                3 -> {
                    //0限价 1市价
                    if (LogicContractSetting.getExecution(ContractSDKAgent.context) == 1) {
                        //条件市价单
                        price = etTriggerPriceStr
                        if (isOpen) {
                            if (isPercentPlaceOrder) {
                                if (LogicContractSetting.getContractUint(context) == 0) {
                                    if (contractSide.equals("1")) {
                                        val buff1 = BigDecimalUtils.mulStr(etPositionStr, price, mPricePrecision)
                                        etPositionStr = BigDecimalUtils.mulStr(multiplier, buff1, mPricePrecision)
                                    } else {
                                        val buff1 = BigDecimalUtils.mulStr(etPositionStr, multiplier, mPricePrecision)
                                        etPositionStr = BigDecimalUtils.divStr(buff1, price, mPricePrecision)
                                    }
                                } else {
                                    if (contractSide.equals("1")) {
                                        etPositionStr = BigDecimalUtils.mulStr(etPositionStr, price, mPricePrecision)
                                    } else {
                                        etPositionStr = BigDecimalUtils.divStr(etPositionStr, price, mPricePrecision)
                                    }
                                }
                            }
                        }
                    } else {//条件限价单
                        price = etPriceStr
                    }
                }
            }
            var coinResultVo = JSONObject(it.optString("coinResultVo"))
            var marginCoinPrecision = coinResultVo.optInt("marginCoinPrecision")//保证金币种显示精度
            var multiplierPrecision = if (BigDecimalUtils.subZeroAndDot(multiplier).indexOf(".") > 0) BigDecimalUtils.subZeroAndDot(multiplier).split(".")[1].length else 0//合约面值精度

            var positionHintText = getString(R.string.cl_reminder_text5)
            var positionUintText = base
            if (buyOrSellHelper.priceType == 2 && isOpen) {
                positionHintText = getLineText("cl_open_value_str")
                positionUintText = if (contractSide.equals("1")) quoteUint else baseUint
                tv_volume_value.setText("≈" + BigDecimalUtils.canPositionMarketStr(contractSide.equals("1"), marginRate, multiplier, etPositionStr, price, multiplierPrecision, it?.optString("multiplierCoin")));
            } else if (buyOrSellHelper.priceType == 3 && LogicContractSetting.getExecution(ContractSDKAgent.context) == 1 && isOpen) {
                positionHintText = getLineText("cl_open_value_str")
                positionUintText = if (contractSide.equals("1")) quoteUint else baseUint
                tv_volume_value.setText("≈" + BigDecimalUtils.canPositionMarketStr(contractSide.equals("1"), marginRate, multiplier, etPositionStr, price, multiplierPrecision, it?.optString("multiplierCoin")));
            } else {
                tv_volume_value.setText("≈" + BigDecimalUtils.canPositionStr(etPositionStr, multiplier, multiplierPrecision, it.optString("multiplierCoin")));
            }
            et_position.setHint(positionHintText)
            tv_volume_unit.setText(positionUintText)

            if (UserDataService.getInstance().isLogined) {
                if (!isOpen) {
                    stv_buy.isEnable(BigDecimalUtils.compareTo(canCloseVolumeBuy, "0") == 1)
                    stv_sell.isEnable(BigDecimalUtils.compareTo(canCloseVolumeSell, "0") == 1)
                } else {
                    stv_buy.isEnable(true)
                    stv_sell.isEnable(true)
                }
            } else {
                stv_buy.isEnable(true)
                stv_sell.isEnable(true)
            }

            tv_long_value.setText(BigDecimalUtils.canBuyStr(isOpen, buyOrSellHelper.priceType == 2, contractSide.equals("1"), price, multiplier, canUseAmount, canCloseVolumeBuy, level, marginRate, multiplierPrecision, base));
            tv_short_value.setText(BigDecimalUtils.canBuyStr(isOpen, buyOrSellHelper.priceType == 2, contractSide.equals("1"), price, multiplier, canUseAmount, canCloseVolumeSell, level, marginRate, multiplierPrecision, base));

            tv_buy_cost.setText(BigDecimalUtils.canCostStr(isOpen, contractSide.equals("1"), buyOrSellHelper.priceType, etPriceStr, etPositionStr, multiplier, level, marginRate, marginCoinPrecision, marginCoin))
            tv_sell_cost.setText(BigDecimalUtils.canCostStr(isOpen, contractSide.equals("1"), buyOrSellHelper.priceType, etPriceStr, etPositionStr, multiplier, level, marginRate, marginCoinPrecision, marginCoin))

            //计算持仓价值
            var positionValueBuy = "0"
            var positionValueSell = "0"
            var positionValue = "0"
            var positionValueBuff = BigDecimal.ZERO
            var positionValueBuyBuff = BigDecimal.ZERO
            var positionValueSellBuff = BigDecimal.ZERO
            if (this::positionListObj.isInitialized) {
                positionListObj?.let {
                    for (i in 0..(it.length() - 1)) {
                        val mJSONObject = it.getJSONObject(i)
                        if (mJSONObject.getInt("contractId") == mContractId) {
                            if (mJSONObject.getString("orderSide").equals("SELL")) {
                                positionValueSellBuff = BigDecimal(mJSONObject.optString("positionBalance")).add(positionValueSellBuff)
                            } else {
                                positionValueBuyBuff = BigDecimal(mJSONObject.optString("positionBalance")).add(positionValueBuyBuff)
                            }
                            positionValueBuff = BigDecimal(mJSONObject.optString("positionBalance")).add(positionValueBuff)
                        }
                    }
                    positionValueBuy = positionValueBuyBuff.setScale(multiplierPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()
                    positionValueSell = positionValueSellBuff.setScale(multiplierPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()
                    positionValue = positionValueBuff.setScale(multiplierPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()
                }
            }

            //计算委托价值
            var entrustedValueBuy = "0"
            var entrustedValueSell = "0"
            var entrustedValue = "0"
            var entrustedValueSellBuff = BigDecimal.ZERO
            var entrustedValueBuyBuff = BigDecimal.ZERO
            var entrustedValueBuff = BigDecimal.ZERO
            for (buff in mCurrentOrderListBuffer) {
//                if (buff.side.equals("SELL")) {
//                    entrustedValueSellBuff = BigDecimal(buff.orderBalance).add(entrustedValueSellBuff)
//                } else {
//                    entrustedValueBuyBuff = BigDecimal(buff.orderBalance).add(entrustedValueBuyBuff)
//                }
                var mOrderBalance = buff.orderBalance
                if (TextUtils.isEmpty(mOrderBalance)) {
                    mOrderBalance = "0"
                }
                entrustedValueBuff = BigDecimal(mOrderBalance).add(entrustedValueBuff)
            }
            entrustedValueBuy = entrustedValueBuyBuff.setScale(multiplierPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()
            entrustedValueSell = entrustedValueSellBuff.setScale(multiplierPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()
            entrustedValue = entrustedValueBuff.setScale(multiplierPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()

            //计算最大可开的额度
            var maxOpenLimit = "0"
            try {
                if (this::userConfigObj.isInitialized) {
                    userConfigObj?.let {
                        val leverOriginCeilingObj = it.optJSONObject("leverOriginCeiling")
                        val iteratorKeys = leverOriginCeilingObj.keys()
                        var leverOriginCeilingArr = ArrayList<Int>()
                        while (iteratorKeys.hasNext()) {
                            val key = iteratorKeys.next().toInt()
                            leverOriginCeilingArr.add(key)
                        }
                        leverOriginCeilingArr.sort()
                        for (buff in leverOriginCeilingArr) {
                            LogUtil.e(TAG, "level:" + level + "    " + "key:" + buff)
                            if (level.toInt() <= buff.toInt()) {
                                maxOpenLimit = leverOriginCeilingObj.optString(buff.toString())
                                break
                            }
                        }
                    }
                }
            } catch (e: Exception) {

            }
            LogUtil.e(TAG, "maxOpenLimit:" + maxOpenLimit)
            LogUtil.e(TAG, "positionValue:" + positionValue)
            LogUtil.e(TAG, "entrustedValue:" + entrustedValue)
            LogUtil.e(TAG, "entrustedValue:" + entrustedValueBuy)
            if (isOpen) {
                val buff1 = BigDecimalUtils.canBuyStr(isOpen, buyOrSellHelper.priceType == 2, contractSide.equals("1"), price, multiplier, canUseAmount, canCloseVolumeBuy, level, marginRate, multiplierPrecision, base)
                //Buy
                canBuy = BigDecimalUtils.canOpenStr(contractSide.equals("1"), buyOrSellHelper.priceType == 2, price, maxOpenLimit, positionValueBuy, entrustedValueBuy, multiplier, marginRate, multiplierPrecision, base)
                //Sell
                canSell = BigDecimalUtils.canOpenStr(contractSide.equals("1"), buyOrSellHelper.priceType == 2, price, maxOpenLimit, positionValueSell, entrustedValueSell, multiplier, marginRate, multiplierPrecision, base)

                canOpen = BigDecimalUtils.canOpenStr(contractSide.equals("1"), buyOrSellHelper.priceType == 2, price, maxOpenLimit, positionValue, entrustedValue, multiplier, marginRate, multiplierPrecision, base)
                LogUtil.e(TAG, "buff1:" + buff1)
                LogUtil.e(TAG, "buff2:" + canBuy)
                LogUtil.e(TAG, "buff3:" + canSell)
                LogUtil.e(TAG, "buff4:" + canOpen)

                canOpenBuff = BigDecimalUtils.min(buff1.split(" ")[0], canOpen.split(" ")[0])
                tv_long_value.setText(canOpenBuff + " " + base)
                tv_short_value.setText(canOpenBuff + " " + base)
            } else {
                canBuy = BigDecimalUtils.canBuyStr(isOpen, buyOrSellHelper.priceType == 2, contractSide.equals("1"), price, multiplier, canUseAmount, canCloseVolumeBuy, level, marginRate, multiplierPrecision, base);
                canSell = BigDecimalUtils.canBuyStr(isOpen, buyOrSellHelper.priceType == 2, contractSide.equals("1"), price, multiplier, canUseAmount, canCloseVolumeSell, level, marginRate, multiplierPrecision, base);
                LogUtil.e(TAG, "canBuy:" + canSell)
                LogUtil.e(TAG, "canSell:" + canOpen)
            }

        }

    }

    /**
     * 平仓 杠杆颜色置灰
     */
    private fun resetLeverTextColor() {
        if (buyOrSellHelper.tradeType == 1) {//平仓
            val color = resources.getColor(R.color.normal_text_color)
            tv_lever_title.textColor = color
            tv_lever.textColor = color
        } else {
            val color = resources.getColor(R.color.text_color)
            tv_lever_title.textColor = color
            tv_lever.textColor = color
        }
    }

    /**
     * 更新买入卖出按钮UI
     */
    fun updateBtnUI() {
        if (UserDataService.getInstance().isLogined) {
            if (openContract != 0) {
                if (buyOrSellHelper.tradeType == 0) {
                    //开仓
                    stv_sell.textContent = "<font> ${getLineText("sl_str_sell_open")} </font>"
                    stv_buy.textContent = "<font> ${getLineText("sl_str_buy_open")} </font>"
                } else if (buyOrSellHelper.tradeType == 1) {
                    //平仓
                    stv_sell.textContent = "<font> ${getLineText("sl_str_sell_close")} </font>"
                    stv_buy.textContent = "<font> ${getLineText("sl_str_buy_close")} </font>"
                } else {
                    //提交委托
                    stv_sell.textContent = "<font> ${getLineText("cl_calculator_text20")} </font>"
                    stv_buy.textContent = "<font> ${getLineText("cl_calculator_text19")}</font>"
                }
                ll_noly_reduce_position.visibility = if (tabIndex == 3) View.VISIBLE else View.GONE

                cb_noly_reduce_position.isChecked = (buyOrSellHelper.tradeType == 1)
            } else {
                val textLogin = getString(R.string.cl_tradeform_text53)
                stv_sell.textContent = textLogin
                stv_buy.textContent = textLogin
            }
        } else {
            val textLogin = getLineText("sl_str_login_register")
            stv_sell.textContent = textLogin
            stv_buy.textContent = textLogin

            tv_long_value.text = "--"
            tv_short_value.text = "--"
            tv_long_value2.text = "--"
            tv_short_value2.text = "--"
            tv_aavl_value.text = "--"
            //退出登录时。如果当前在仓位页面，则调整出去
            if (tabEntrustIndex == 1) {
                tabEntrustIndex = 0
//                showFragment()
            }
        }
    }

    companion object {
        /**
         * 限价
         */
        const val CONTRACT_ORDER_LIMIT = 0

        /**
         * 市价
         */
        const val CONTRACT_ORDER_MARKET = 1

        /**
         * 计划
         */
        const val CONTRACT_ORDER_PLAN = 2

        /**
         * 买一价
         */
        const val CONTRACT_ORDER_BID_PRICE = 3

        /**
         * 卖一价
         */
        const val CONTRACT_ORDER_ASK_PRICE = 4

        /**
         * 限价(高级委托)
         */
        const val CONTRACT_ORDER_ADVANCED_LIMIT = 5
    }

    override fun onBuySellContractVolClick(depthData: DepthData?, showVol: String?, flag: Int) {
        if (buyOrSellHelper.priceType == CONTRACT_ORDER_PLAN) {
            return
        }
        mContract?.let {
            if (depthData != null) {
                val dfVol = NumberUtil.getDecimal(it.vol_index)
                et_position.setText(dfVol.format(depthData.vol.toString()))
            }

        }
    }

    override fun onBuySellContractClick(depthData: DepthData?, showVol: String?, flag: Int) {
        if (buyOrSellHelper.priceType == CONTRACT_ORDER_PLAN) {
            mContract?.let {
                if (depthData != null && depthData.price > "0") {
                    val dfPrice = NumberUtil.getDecimal(it.price_index - 1)
                    val price = MathHelper.round(depthData.price)
                    et_trigger_price.setText(dfPrice.format(price))
                }
            }
        } else {
            mContract?.let {
                if (depthData != null && depthData.price > "0") {
                    val dfPrice = NumberUtil.getDecimal(it.price_index - 1)
                    val price = MathHelper.round(depthData.price)
                    et_price.setText(dfPrice.format(price))
//                    et_price.setText(BigDecimalUtils.showSNormal(dfPrice.format(price),LogicContractSetting.getContractSymbolPricePrecisionById(context, mContractId)))
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(messageEvent: MessageEvent) {
        when (messageEvent.msg_type) {
            MessageEvent.sl_contract_change_tagPrice_event -> {
                val obj = messageEvent.msg_content as JSONObject
                tv_flag_price.setText(obj.optString("tagPrice"))
                tv_index_price.setText(obj.optString("indexPrice"))
            }
            MessageEvent.sl_contract_cancel_last_price_event -> {
                lastPrice = messageEvent.msg_content as String
            }
            MessageEvent.sl_contract_change_unit_event -> {
                coUnit = LogicContractSetting.getContractUint(mContext)
                tv_contract_text_price.setText(getString(R.string.cl_reminder_text2) + "(" + mContractJson?.optString("quote") + ")")
                tv_charge_text_volume.setText(getString(R.string.charge_text_volume) + if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 0) "(" + getString(R.string.sl_str_contracts_unit) + ")" else "(" + mContractJson?.optString("multiplierCoin") + ")")
                if (tabEntrustIndex == 0) {
                    getCurrentOrderList()
                } else {
                    getCurrentPlanOrderList()
                }
            }
            MessageEvent.sl_contract_change_coin_list_type -> {
                mContractJson = messageEvent.msg_content as JSONObject
                tv_coin_name.setText(mContractJson?.optString("quote"))
                tv_trigger_coin_name.setText(mContractJson?.optString("quote"))
                tv_volume_unit.setText(if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 0) getString(R.string.sl_str_contracts_unit) else mContractJson?.optString("base"))
                mContractId = mContractJson?.getInt("id")!!
                if (tabEntrustIndex == 0) {
                    getCurrentOrderList()
                } else {
                    getCurrentPlanOrderList()
                }
                getPositionAssetsList()
                updateAvailableVol()
            }
            MessageEvent.sl_contract_left_coin_type -> {
                mContractJson = messageEvent.msg_content as JSONObject
                showContractInfo(mContractJson!!)
            }
            MessageEvent.sl_contract_first_show_info_event -> {
                mContractJson = messageEvent.msg_content as JSONObject
                showContractInfo(mContractJson!!)
            }
            MessageEvent.sl_contract_user_config_event -> {
                userConfigObj = messageEvent.msg_content as JSONObject
                openContract = userConfigObj.optInt("openContract")
                coUnit = LogicContractSetting.getContractUint(ContractSDKAgent.context)
                getPositionAssetsList()
                getCurrentOrderList()
                updateBtnUI()
            }
            MessageEvent.sl_contract_login_status_event -> {
                updateBtnUI()
            }
            MessageEvent.sl_contract_first_input_last_price_event -> {
                var result = messageEvent.msg_content as String
                result = BigDecimalUtils.showSNormal(result, LogicContractSetting.getContractSymbolPricePrecisionById(context, mContractId))
                et_price.setText(result)
            }
            MessageEvent.DEPTH_CONTRACT_DATA_TYPE -> {
                refreshDepthView(messageEvent.msg_content as JSONObject)
            }
            MessageEvent.sl_contract_page_hide_event -> {
                val isStartpLoop = messageEvent.msg_content as Boolean
                if (isStartpLoop) {
                    loopStart()
                } else {
                    loopStop()
                }
            }
            MessageEvent.sl_contract_select_leverage_event -> {
                if (messageEvent.msg_content != null && messageEvent.msg_content is HashMap<*, *>) {
                    val map = messageEvent.msg_content as HashMap<String, Int>
                    GlobalLeverageUtils.currLeverage = map["leverage"]!!
                    GlobalLeverageUtils.currentPositionType = map["leverageType"]!!
                    mContract?.let {
                        if (GlobalLeverageUtils.isOpenGlobalLeverage) {
                            GlobalLeverageUtils.uploadLeverage(it.instrument_id, GlobalLeverageUtils.currLeverage, GlobalLeverageUtils.currentPositionType)
                        } else {
                            GlobalLeverageUtils.saveLeverage()
                        }
                    }
                    updateLeverUI()
                    updateAvailableVol()
                }
            }
            MessageEvent.sl_contract_logout_event -> {
                mCurrentOrderList.clear()
                mCurrentOrderListBuffer.clear()
                mContractPriceEntrustAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun showContractInfo(mContractJson: JSONObject) {
        tv_contract_text_price.setText(getString(R.string.cl_reminder_text2) + "(" + mContractJson?.optString("quote") + ")")
        tv_charge_text_volume.setText(getString(R.string.charge_text_volume) + if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 0) "(" + getString(R.string.sl_str_contracts_unit) + ")" else "(" + mContractJson?.optString("base") + ")")
        tv_coin_name.setText(mContractJson?.optString("quote"))
        tv_trigger_coin_name.setText(mContractJson?.optString("quote"))
        tv_volume_unit.setText(if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 0) getString(R.string.sl_str_contracts_unit) else mContractJson?.optString("base"))
        mContractId = mContractJson?.getInt("id")!!
        multiplier = LogicContractSetting.getContractMultiplierById(context, mContractId)
        multiplierPrecision = LogicContractSetting.getContractMultiplierPrecisionById(context, mContractId)
        if (tabEntrustIndex == 0) {
            getCurrentOrderList()
        } else {
            getCurrentPlanOrderList()
        }
        resetInputPosition()
        getPositionAssetsList()
        updateAvailableVol()
        //更新盘口精度
        var coinResultVo = mContractJson?.getJSONObject("coinResultVo")
        var mDepthJsonArray = coinResultVo?.optJSONArray("depth")
        depthList.clear()
        for (i in 0 until mDepthJsonArray?.length()!!) {
            val obj = mDepthJsonArray[i] as String
            if (obj.toInt() == 0) {
                depthList.add(TabInfo("1", i, obj))
            } else {
                var buff = StringBuffer("0")
                buff.append(".")
                for (x in 0 until obj.toInt() - 1) {
                    buff.append("0")
                }
                buff.append("1")
                depthList.add(TabInfo(buff.toString(), i, obj))
            }
        }
        depth_level = depthList[0].extras?.toInt()!!
        LogUtil.e(TAG, "depth_level:" + depth_level)
        depthType = depthList[0]
        tv_change_depth.setText(depthType?.name)

        //设置输入精度控制
        et_trigger_price.numberFilter(LogicContractSetting.getContractSymbolPricePrecisionById(context, mContractId), otherFilter = updateAvailableVolListener)
        et_price.numberFilter(LogicContractSetting.getContractSymbolPricePrecisionById(context, mContractId), otherFilter = updatePriceListener)
        et_position.numberFilter(if (LogicContractSetting.getContractUint(context) == 0) 0 else LogicContractSetting.getContractMultiplierPrecisionById(context, mContractId), otherFilter = updatePositionListener)

    }

    private fun getCurrentOrderList() {
        if (mContractId == -1) return
        if (!UserDataService.getInstance().isLogined) return
        if (openContract == 0) return
        if (tabEntrustIndex == 1) return

        addDisposable(getContractModel().getCurrentOrderList(mContractId.toString(), 0, 1,
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<ClCurrentOrderBean>()
                        mCurrentOrderListBuffer.clear()
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("orderList")) {
                                val mOrderListJson = optJSONArray("orderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<ClCurrentOrderBean>(obj, ClCurrentOrderBean::class.java)
                                    mClCurrentOrderBean.isPlan = false
                                    mListBuffer.add(mClCurrentOrderBean)
                                    mCurrentOrderListBuffer.add(mClCurrentOrderBean)
                                }
                            }
                        }
                        mContractPriceEntrustAdapter?.setList(mListBuffer)
                    }
                }))
    }

    private fun getCurrentPlanOrderList() {
        if (!UserDataService.getInstance().isLogined) return
        if (openContract == 0) return
        if (tabEntrustIndex == 0) return
        addDisposable(getContractModel().getCurrentPlanOrderList(mContractId.toString(), 0, 1,
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<ClCurrentOrderBean>()
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("trigOrderList")) {
                                val mOrderListJson = optJSONArray("trigOrderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<ClCurrentOrderBean>(obj, ClCurrentOrderBean::class.java)
                                    mClCurrentOrderBean.isPlan = true
                                    mListBuffer.add(mClCurrentOrderBean)
                                }
                            }
                        }
                        mContractPriceEntrustAdapter?.setList(mListBuffer)
                    }
                }))
    }

    private fun getPositionAssetsList() {
        if (!UserDataService.getInstance().isLogined) return
        LogUtil.e(TAG, "获取资产--1")
        if (openContract == 0) return
        LogUtil.e(TAG, "获取资产--2")
        addDisposable(getContractModel().getPositionAssetsList(
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        var mPositionNum = 0
                        canCloseVolumeSell = "0"
                        canCloseVolumeBuy = "0"
                        jsonObject.optJSONObject("data")?.run {
                            if (!isNull("positionList")) {
                                val mPositionListJson = optJSONArray("positionList")
                                mPositionNum = mPositionListJson.length()
                                val msgEvent = MessageEvent(MessageEvent.sl_contract_position_num_event)
                                msgEvent.msg_content = mPositionNum
                                EventBusUtil.post(msgEvent)
                                for (i in 0..(mPositionListJson.length() - 1)) {
                                    val mJSONObject = mPositionListJson.getJSONObject(i)
                                    if (mJSONObject.getInt("contractId") == mContractId) {
                                        var canCloseVolume = mJSONObject.getString("canCloseVolume")
                                        var orderSid = mJSONObject.getString("orderSide")
                                        if (orderSid.equals("BUY")) {
                                            canCloseVolumeSell = canCloseVolume
                                        } else if (orderSid.equals("SELL")) {
                                            canCloseVolumeBuy = canCloseVolume
                                        }
                                    }
                                }
                                positionListObj = mPositionListJson

                                val msgEvent2 = MessageEvent(MessageEvent.sl_contract_change_position_list_event)
                                msgEvent2.msg_content = mPositionListJson
                                EventBusUtil.post(msgEvent2)
                            }

                            if (!isNull("accountList")) {
                                val mOrderListJson = optJSONArray("accountList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    val obj = mOrderListJson.getJSONObject(i)
                                    var canUseAmount = obj.getString("canUseAmount")
                                    canUseAmount = BigDecimalUtils.scaleStr(canUseAmount, 3)
                                    if (mContractJson?.optString("marginCoin").toString().equals(obj.optString("symbol"))) {
                                        tv_aavl_value?.setText(canUseAmount + mContractJson?.optString("marginCoin").toString())
                                        userAccountObj = mOrderListJson.getJSONObject(i)
                                    }
                                }
                                updateAvailableVol()
                            }
                        }
//                        mContractPriceEntrustAdapter?.setList(mCurrentOrderList)
                    }
                }))
    }

    private lateinit var vol: String

    private fun doBuyOrSell(side: String) {
        if (mContractJson == null) {
            return
        }
        var contractSide = mContractJson?.optString("contractSide")
        var isOpen = false;
        when (buyOrSellHelper.tradeType) {
            0 -> {
                isOpen = true
            }
            1 -> {
                isOpen = false
            }
            3 -> {
                if (cb_noly_reduce_position.isChecked) {
                    isOpen = false
                } else {
                    isOpen = true
                }
            }
            else -> isOpen = false
        }

        val mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(context, mContractId)
        var dialogTitle = ""
        var orderType = 1
        var isConditionOrder = false;
        var price = et_price.text.toString()//下单价格(市价单传0)
        var triggerPrice = et_trigger_price.text.toString()//触发价格
        var volume = "0"
        var canUseAmount = "0"
        var level = "20";
        if (this::userAccountObj.isInitialized) {
            userAccountObj?.let {
                canUseAmount = it.optString("canUseAmount")
            }
        }
        if (this::userConfigObj.isInitialized) {
            userConfigObj?.let {
                level = it.optString("nowLevel")
                if (TextUtils.isEmpty(level)) {
                    level = "20";
                }
            }
        }
//        if (isPercentPlaceOrder) {
//            if (isOpen){
//                LogUtil.e(TAG, "canOpen：" + canOpenBuff)
//                volume = BigDecimalUtils.mulStr(canOpenBuff, percent, multiplierPrecision)
//            }else{
//                if (side.equals("BUY")) {
//                    LogUtil.e(TAG, "canBuy：" + canBuy)
//                    volume = BigDecimalUtils.mulStr(canBuy.split(" ")[0], percent, multiplierPrecision)
//                } else {
//                    LogUtil.e(TAG, "canSell：" + canSell)
//                    volume = BigDecimalUtils.mulStr(canSell.split(" ")[0], percent, multiplierPrecision)
//                }
//            }
//        } else {
//            volume = et_position.text.toString()//下单数量(开仓市价开仓市价单单：金额)
        volume = etPositionStr//下单数量(开仓市价开仓市价单单：金额)
//        }
        if (isPercentPlaceOrder) {
            if (!isOpen) {
                if (side.equals("BUY")) {
                    LogUtil.e(TAG, "canBuy：" + canBuy)
                    volume = BigDecimalUtils.mulStr(canBuy.split(" ")[0], percent, multiplierPrecision)
                } else {
                    LogUtil.e(TAG, "canSell：" + canSell)
                    volume = BigDecimalUtils.mulStr(canSell.split(" ")[0], percent, multiplierPrecision)
                }
            }
        }
        LogUtil.e(TAG, "开仓数量：" + volume)
        when (buyOrSellHelper.priceType) {
            1 -> {
                orderType = 1 //限价单
                if (TextUtils.isEmpty(price)) {
                    NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_please_enter_limited_price_str"))
                    return
                }
                dialogTitle = getLineText("contract_action_limitPrice")
            }
            2 -> {
                orderType = 2 //市价单
                price = "0"
                dialogTitle = getLineText("contract_action_marketPrice")
                if (isOpen) {
//                    if (isPercentPlaceOrder){
//                        if (LogicContractSetting.getContractUint(context) == 0) {
//                            if (contractSide.equals("1")){
//                                val buff1=BigDecimalUtils.mulStr(volume,price,mPricePrecision)
//                                volume=BigDecimalUtils.mulStr(multiplier,buff1,mPricePrecision)
//                            }else{
//                                val buff1=BigDecimalUtils.mulStr(volume,multiplier,mPricePrecision)
//                                volume=BigDecimalUtils.divStr(buff1,price,mPricePrecision)
//                            }
//                        }else{
//                            if (contractSide.equals("1")){
//                                volume=BigDecimalUtils.mulStr(volume,price,mPricePrecision)
//                            }else{
//                                volume=BigDecimalUtils.divStr(volume,price,mPricePrecision)
//                            }
//                        }
//                    }
                }
            }
            3 -> {
                if (TextUtils.isEmpty(triggerPrice)) {
                    NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_please_enter_trigger_price_str"))
                    return
                }
                isConditionOrder = true
                if (LogicContractSetting.getExecution(ContractSDKAgent.context) == 1) {
                    orderType = 2 //市价单
                    price = "0"
                    dialogTitle = getLineText("cl_conditional_market_price_str")
//                    if (isOpen){
//                        if (isPercentPlaceOrder){
//                            if (LogicContractSetting.getContractUint(context) == 0) {
//                                if (contractSide.equals("1")){
//                                    val buff1=BigDecimalUtils.mulStr(volume,price,mPricePrecision)
//                                    volume=BigDecimalUtils.mulStr(multiplier,buff1,mPricePrecision)
//                                }else{
//                                    val buff1=BigDecimalUtils.mulStr(volume,multiplier,mPricePrecision)
//                                    volume=BigDecimalUtils.divStr(buff1,price,mPricePrecision)
//                                }
//                            }else{
//                                if (contractSide.equals("1")){
//                                    volume=BigDecimalUtils.mulStr(volume,price,mPricePrecision)
//                                }else{
//                                    volume=BigDecimalUtils.divStr(volume,price,mPricePrecision)
//                                }
//                            }
//                        }
//                    }
                } else {
                    orderType = 1 // 限价单
                    if (TextUtils.isEmpty(price)) {
                        NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_please_enter_limited_price_str"))
                        return
                    }
                    dialogTitle = getLineText("cl_conditional_limit_price_str")
                }
            }
            4 -> {
                orderType = 5 //PostOnly
                if (TextUtils.isEmpty(price)) {
                    NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_please_enter_limited_price_str"))
                    return
                }
                dialogTitle = getLineText("contract_action_limitPrice")
            }
            5 -> {
                orderType = 3//IOC
                if (TextUtils.isEmpty(price)) {
                    NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_please_enter_limited_price_str"))
                    return
                }
                dialogTitle = getLineText("contract_action_limitPrice")
            }
            6 -> {
                orderType = 4//FOK
                if (TextUtils.isEmpty(price)) {
                    NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_please_enter_limited_price_str"))
                    return
                }
                dialogTitle = getLineText("contract_action_limitPrice")
            }
        }
        if (TextUtils.isEmpty(volume)) {
            NToastUtil.showTopToastNet(this.mActivity, false, getLineText("transfer_tip_emptyVolume"))
            return
        }

        var multiplier = mContractJson?.optString("multiplier")
        var coinResultVo = JSONObject(mContractJson?.optString("coinResultVo"))

        var minOrderVolume = coinResultVo.optString("minOrderVolume")//最小下单量
        var minOrderMoney = coinResultVo.optString("minOrderMoney")//最小下单金额

        var maxMarketVolume = coinResultVo.optString("maxMarketVolume")//市价单最大下单数量
        var maxMarketMoney = coinResultVo.optString("maxMarketMoney")//市价最大下单金额

        var maxLimitVolume = coinResultVo.optString("maxLimitVolume")//限价单最大下单数量
        LogUtil.e(TAG, "开仓数量：" + volume)
        when (buyOrSellHelper.priceType) {
            1, 4, 5, 6 -> {
                //最小下单量  < x <限价单最大下单数量
                if (BigDecimalUtils.orderNumMinCheck(volume, minOrderVolume, multiplier)) {
                    NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_minimum_order_quantity_str") + minOrderVolume + getLineText("contract_text_volumeUnit"))
                    return
                }
                if (BigDecimalUtils.orderNumMaxCheck(volume, maxLimitVolume, multiplier)) {
                    NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_exceeding_the_maximum_single_order_quantity_str") + maxLimitVolume + getLineText("contract_text_volumeUnit"))
                    return
                }
            }
            2 -> {
                if (isOpen) {
                    //最小下单金额  < x < 市价最大下单金额
                    if (BigDecimalUtils.orderMoneyMinCheck(volume, minOrderMoney, multiplier)) {
                        NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_minimum_order_amount_str") + minOrderMoney + mContractJson?.optString("quote"))
                        return
                    }
                    if (BigDecimalUtils.orderMoneyMaxCheck(volume, maxMarketMoney, multiplier)) {
                        NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_Exceeding_the_maximum_single_order_amount_str") + maxMarketMoney + mContractJson?.optString("quote"))
                        return
                    }
                } else {
                    //最小下单量  < x <市价单最大下单数量
                    if (BigDecimalUtils.orderNumMinCheck(volume, minOrderVolume, multiplier)) {
                        NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_minimum_order_quantity_str") + minOrderVolume + getLineText("contract_text_volumeUnit"))
                        return
                    }
                    if (BigDecimalUtils.orderNumMaxCheck(volume, maxMarketVolume, multiplier)) {
                        NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_exceeding_the_maximum_single_order_quantity_str") + maxMarketVolume + getLineText("contract_text_volumeUnit"))
                        return
                    }
                }
            }
            3 -> {
                if (LogicContractSetting.getExecution(ContractSDKAgent.context) == 1 && isOpen) {
                    //最小下单金额  < x < 市价最大下单金额
                    if (BigDecimalUtils.orderMoneyMinCheck(volume, minOrderMoney, multiplier)) {
                        NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_minimum_order_amount_str") + minOrderMoney + mContractJson?.optString("quote"))
                        return
                    }
                    if (BigDecimalUtils.orderMoneyMaxCheck(volume, maxMarketMoney, multiplier)) {
                        NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_Exceeding_the_maximum_single_order_amount_str") + maxMarketMoney + mContractJson?.optString("quote"))
                        return
                    }
                } else {
                    //最小下单量  < x <限价单最大下单数量
                    if (BigDecimalUtils.orderNumMinCheck(volume, minOrderVolume, multiplier)) {
                        NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_minimum_order_quantity_str") + minOrderVolume + getLineText("contract_text_volumeUnit"))
                        return
                    }
                    if (BigDecimalUtils.orderNumMaxCheck(volume, maxLimitVolume, multiplier)) {
                        NToastUtil.showTopToastNet(this.mActivity, false, getLineText("cl_exceeding_the_maximum_single_order_quantity_str") + maxLimitVolume + getLineText("contract_text_volumeUnit"))
                        return
                    }
                }
            }
        }

        var contractId = mContractId
        var positionType = userConfigObj.optString("positionModel")//持仓类型(1 全仓，2 逐仓)
        var open = if (buyOrSellHelper.tradeType == 0) "OPEN" else "CLOSE"//开平仓方向(OPEN 开仓，CLOSE 平仓)
        if (buyOrSellHelper.tradeType == 3) {
            if (cb_noly_reduce_position.isChecked) {
                open = "CLOSE"
            } else {
                open = "OPEN"
            }
        }
        var side = side//买卖方向（BUY 买入，SELL 卖出）
        var type = orderType//订单类型(1 limit， 2 market，3 IOC，4 FOK，5 POST_ONLY)
        var leverageLevel = userConfigObj.optInt("nowLevel")//杠杆倍数

        var expireTime = LogicContractSetting.getStrategyEffectTimeStr(mActivity)

        val titleColor = if (buyOrSellHelper.isBuy) {
            resources.getColor(R.color.main_green)
        } else {
            resources.getColor(R.color.main_red)
        }
        dialogTitle = if (buyOrSellHelper.isBuy) {
            dialogTitle + getString(R.string.contract_action_buy)
        } else {
            dialogTitle + getString(R.string.contract_action_sell)
        }
        val tradeConfirm = PreferenceManager.getInstance(ContractSDKAgent.context).getSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, true)
        if (!tradeConfirm) {
            addDisposable(getContractModel().createOrder(contractId, positionType, open, side, type, leverageLevel, price, BigDecimalUtils.getOrderNum(volume, multiplier, buyOrSellHelper.priceType), isConditionOrder, triggerPrice, expireTime,
                    consumer = object : NDisposableObserver(mActivity, true) {
                        override fun onResponseSuccess(jsonObject: JSONObject) {
                            //刷新当前委托的订单
                            NToastUtil.showTopToastNet(this.mActivity, true, getString(R.string.toast_trade_success))
                            updateAvailableVolUI()
                            getPositionAssetsList()
                            Observable.timer(500, TimeUnit.MILLISECONDS)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe {
                                        getCurrentOrderList()
                                        getCurrentPlanOrderList()
                                    }
                        }
                    }))
            return
        }
        var mAmoutValue = ""
        if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 0) {
            volume = BigDecimalUtils.showSNormal(volume, 0)
        }
        if (buyOrSellHelper.priceType == 2 && isOpen) {
            mAmoutValue = volume + " " + if (mContractJson?.optString("contractSide").equals("1")) quoteUint else baseUint
        } else if (buyOrSellHelper.priceType == 3 && LogicContractSetting.getExecution(ContractSDKAgent.context) == 1 && isOpen) {
            mAmoutValue = volume + " " + if (mContractJson?.optString("contractSide").equals("1")) quoteUint else baseUint
        } else {
            mAmoutValue = volume + " " + if (LogicContractSetting.getContractUint(ContractSDKAgent.context) == 0) getString(R.string.sl_str_contracts_unit) else mContractJson?.optString("multiplierCoin")
        }
        SlDialogHelper.showOrderCreateConfirmDialog(mActivity!!,
                titleColor,
                dialogTitle,
                LogicContractSetting.getContractShowNameById(context, mContractJson?.optInt("id")!!),
                (if (price.equals("0")) getString(R.string.cl_tradeform_mprice) else price + " " + mContractJson?.optString("quote")),
                triggerPrice + " " + mContractJson?.optString("quote"),
                tv_buy_cost.text.toString(),
                mAmoutValue,
                buyOrSellHelper.priceType,
                object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        addDisposable(getContractModel().createOrder(contractId, positionType, open, side, type, leverageLevel, price, BigDecimalUtils.getOrderNum(volume, multiplier, buyOrSellHelper.priceType), isConditionOrder, triggerPrice, expireTime,
                                consumer = object : NDisposableObserver(mActivity, true) {
                                    override fun onResponseSuccess(jsonObject: JSONObject) {
                                        //刷新当前委托的订单
                                        NToastUtil.showTopToastNet(this.mActivity, true, getString(R.string.toast_trade_success))
                                        updateAvailableVolUI()
                                        getPositionAssetsList()
                                        Observable.timer(500, TimeUnit.MILLISECONDS)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread()).subscribe {
                                                    getCurrentOrderList()
                                                    getCurrentPlanOrderList()
                                                }
                                    }
                                }))
                    }
                })
    }

    fun updateDepth(isBuy: Boolean, list: List<DepthData>?) {
        if (isBuy) {
//            ll_buy_layout?.initData(list)
        } else {
//            ll_sell_Layout?.initData(list)
        }
    }

    override fun onContractSettingChange() {
        et_position.numberFilterContract(mContract, otherFilter = updatePositionListener)
        updateOrderType(true)
        setVolUnit()
    }

    private fun setVolUnit() {
        val unit: Int = LogicContractSetting.getContractUint(ContractSDKAgent.context)
        tv_volume_unit.text = if (unit == 0) getLineText("sl_str_contracts_unit") else mContract?.showBaseName()
        tv_coin_name.text = mContract?.showQuoteName()
        tv_trigger_coin_name.text = mContract?.showQuoteName()
        val etPosition = et_position.text.toString()
        if (etPosition.isNotEmpty()) {
            et_position.setText("")
            et_position.setText(etPosition)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogicContractSetting.getInstance().unregistListener(this)
    }

    override fun doThing(obj: Any?): Boolean {
        if (obj == "updateLeverage") {
            updateLeverUI()
        }
        return true
    }

    var loopSubscribe: Disposable? = null

    private fun loopStart() {
        loopStop()
        LogUtil.e(TAG, "开始执行轮训--1")
        loopSubscribe = Observable.interval(0L, CommonConstant.coinLoopTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    LogUtil.e(TAG, "开始执行轮训--2")
                    getPositionAssetsList()
                    getCurrentOrderList()
                }
    }

    private fun loopStop() {
        if (loopSubscribe != null) {
            loopSubscribe?.dispose()
        }
    }

    /**
     * 刷新
     */
    fun onRefresh() {
        if (ContractSDKAgent.isLogin) {
            ContractUserDataAgent.getContractAccounts(true)
            mContract?.let {
                ContractUserDataAgent.getCoinPositions(it.instrument_id, true)
                if (tabEntrustIndex == 0) {
                    ContractUserDataAgent.getContractOrder(it.instrument_id, true)
                } else {
                    ContractUserDataAgent.getContractPlanOrder(it.instrument_id, true)
                }
                ContractPublicDataAgent.loadDepthFromNet(it.instrument_id, getDepthSubCount())
            }
        } else {
            mContract?.let {
                ContractPublicDataAgent.loadDepthFromNet(it.instrument_id, getDepthSubCount())
            }
        }
    }

    /**
     * 买卖盘
     *
     * 初始化交易详情记录view
     */
    fun initDetailView(items: Int = 6) {
        sellViewList.clear()
        buyViewList.clear()

        if (ll_buy_price?.childCount ?: 0 > 0) {
            (ll_buy_price as LinearLayout).removeAllViews()
        }

        if (ll_sell_price?.childCount ?: 0 > 0) {
            (ll_sell_price as LinearLayout).removeAllViews()
        }

        for (i in 0 until items) {
            /**
             * 卖盘
             */
            val sell_layout: View = context?.layoutInflater!!.inflate(R.layout.cl_item_transaction_detail, null)

            sell_layout.tv_price_item?.textColor = ColorUtil.getMainColorType(isRise = false)
            NLiveDataUtil.observeForeverData {
                if (null != it && MessageEvent.color_rise_fall_type == it.msg_type) {
                    sell_layout.tv_price_item?.textColor = ColorUtil.getMainColorType(isRise = false)
                }
            }

            sell_layout.setOnClickListener {
                val result = sell_layout.tv_price_item?.text.toString()
                Log.d(TAG, "========resultSell:$result=======")
                if (buyOrSellHelper.priceType == 1 || buyOrSellHelper.priceType == 4 || buyOrSellHelper.priceType == 5 || buyOrSellHelper.priceType == 6) {
                    et_price.setText(if (result.equals("--")) "" else BigDecimalUtils.showSNormal(result, LogicContractSetting.getContractSymbolPricePrecisionById(context, mContractId)))
                }
                if (buyOrSellHelper.priceType == 3) {
                    et_trigger_price.setText(if (result.equals("--")) "" else result)
                }
            }
            sellViewList.add(sell_layout)

            /**
             * 买盘
             */
            val buy_layout: View = context?.layoutInflater!!.inflate(R.layout.cl_item_transaction_detail, null)

            buy_layout.tv_price_item?.textColor = ColorUtil.getMainColorType()
            NLiveDataUtil.observeForeverData {
                if (null != it && MessageEvent.color_rise_fall_type == it.msg_type) {
                    buy_layout.tv_price_item?.textColor = ColorUtil.getMainColorType()
                }
            }

            buy_layout.setOnClickListener {
                val result = buy_layout.tv_price_item?.text.toString()
                Log.d(TAG, "========resultBuy:$result=======")
                if (buyOrSellHelper.priceType == 1 || buyOrSellHelper.priceType == 4 || buyOrSellHelper.priceType == 5 || buyOrSellHelper.priceType == 6) {
                    et_price.setText(if (result.equals("--")) "" else BigDecimalUtils.showSNormal(result, LogicContractSetting.getContractSymbolPricePrecisionById(context, mContractId)))
                }
                if (buyOrSellHelper.priceType == 3) {
                    et_trigger_price.setText(if (result.equals("--")) "" else result)
                }
            }
            buyViewList.add(buy_layout)
        }


        buyViewList.forEach {
            ll_buy_price?.addView(it)
        }

        sellViewList.forEach {
            ll_sell_price?.addView(it)
        }

    }

    fun changeTape(item: Int, needData: Boolean = true) {
        when (item) {
            AppConstant.DEFAULT_TAPE -> {
                ll_buy_price?.visibility = View.VISIBLE
                ll_sell_price?.visibility = View.VISIBLE
                v_tape_line?.visibility = View.VISIBLE
                ll_sell_buy_line?.visibility = View.VISIBLE
                ColorUtil.setTapeIcon(ib_tape, AppConstant.DEFAULT_TAPE)
                initDetailView()
            }

            AppConstant.BUY_TAPE -> {
                ll_buy_price?.visibility = View.VISIBLE
                ll_sell_price?.visibility = View.GONE
                v_tape_line?.visibility = View.GONE
                ll_sell_buy_line?.visibility = View.GONE
                ColorUtil.setTapeIcon(ib_tape, AppConstant.BUY_TAPE)
                initDetailView(12)
            }

            AppConstant.SELL_TAPE -> {
                ll_buy_price?.visibility = View.GONE
                v_tape_line?.visibility = View.GONE
                ll_sell_price?.visibility = View.VISIBLE
                ll_sell_buy_line?.visibility = View.GONE
                ColorUtil.setTapeIcon(ib_tape, AppConstant.SELL_TAPE)
                initDetailView(12)
            }
        }
        if (needData) {
            refreshDepthView(transactionData)
        }
    }


    fun refreshDepthView(data: JSONObject?) {
        data?.run {
            transactionData = this
            val tick = this.optJSONObject("tick")

            /**
             * 卖盘交易量最大的
             */
            val askList = arrayListOf<JSONArray>()
            val asks = tick.optJSONArray("asks")
            for (i in 0 until asks.length()) {
                askList.add(asks.optJSONArray(i))
            }

            val askMaxVolJson = askList.maxBy {
                it.optDouble(1)
            }
            val askMaxVol = askMaxVolJson?.optDouble(1) ?: 1.0
            askMaxPrice = askMaxVolJson?.optDouble(0) ?: 0.0
            Log.d(TAG, "========askMAX:$askMaxVol=======")
            Log.d(TAG, "========askMaxPrice:$askMaxPrice=======")

            /**
             * 买盘交易量最大的
             */
            val buyList = arrayListOf<JSONArray>()
            val buys = tick.optJSONArray("buys")
            for (i in 0 until buys.length()) {
                buyList.add(buys.optJSONArray(i))
            }

            /**
             * 买盘交易量最大的
             */
            val buyMaxVolJson = buyList.maxBy {
                it.optDouble(1)
            }
            val buyMaxVol = buyMaxVolJson?.optDouble(1) ?: 1.0
            buyMaxPrice = buyMaxVolJson?.optDouble(0) ?: 0.0
            Log.d(TAG, "========buyMAX:$buyMaxVol=======")
            Log.d(TAG, "========buyMaxPrice:$buyMaxPrice=======")

            val maxVol = Math.max(askMaxVol, buyMaxVol)

            Log.d(TAG, "========maxVol:$maxVol=========")

            sellTape(askList, maxVol)
            buyTape(buyList, maxVol)
        }


    }

    /**
     * 卖盘
     */
    private fun sellTape(list: ArrayList<JSONArray>, maxVol: Double) {
        list.sortByDescending {
            it.optDouble(0)
        }

        for (i in 0 until sellViewList.size) {
            /**
             * 卖盘
             */
            if (list.size > sellViewList.size) {
                val subList = list.subList(list.size - sellViewList.size, list.size)
                if (subList.isNotEmpty()) {
                    /*****深度背景色START****/
                    sellViewList[i].fl_bg_item.backgroundColor = ColorUtil.getMinorColorType(isRise = false)
                    val layoutParams = sellViewList[i].fl_bg_item.layoutParams
                    val curVolume = subList[i].optDouble(1)
                    val width = (curVolume / maxVol) * ll_all.measuredWidth * 0.4
                    Log.d(TAG, "=======sell==curVolume is $curVolume,maxVolume is $maxVol,showBgwidth is $width，itemWidth is ${ll_all.measuredWidth * 0.4}")
                    layoutParams.width = width.toInt()
                    sellViewList[i].fl_bg_item.layoutParams = layoutParams
                    /*****深度背景色END****/
                    sellViewList[i].tv_price_item.text = SymbolInterceptUtils.interceptData(
                            subList[i].optString(0).replace("\"", "").trim(),
                            depth_level,
                            "price")
                    var amount = subList[i].optString(1)
                    //0 张 1 币
                    amount = if (coUnit == 0) amount else BigDecimalUtils.mulStr(amount, multiplier, multiplierPrecision)
                    sellViewList[i].tv_quantity_item.text = BigDecimalUtils.showDepthContractVolume(amount)
                }
            } else {
                Log.d(TAG, "======VVV=======")
                val temp = sellViewList.size - list.size
                sellViewList[i].tv_price_item.text = "--"
                sellViewList[i].tv_quantity_item.text = "--"
                sellViewList[i].ll_item.backgroundColor = ColorUtil.getColor(R.color.transparent)
                if (i >= temp) {
                    /*****深度背景色START****/
                    sellViewList[i].fl_bg_item.backgroundColor = ColorUtil.getMinorColorType(isRise = false)
                    val layoutParams = sellViewList[i].fl_bg_item.layoutParams
                    val width = (list[i - temp].optDouble(1) / maxVol) * ll_all.measuredWidth * 0.4
                    layoutParams.width = width.toInt()
                    sellViewList[i].fl_bg_item.layoutParams = layoutParams
                    /*****深度背景色END****/
                    sellViewList[i].tv_price_item.text = SymbolInterceptUtils.interceptData(
                            list[i - temp].optString(0).replace("\"", "").trim(),
                            depth_level,
                            "price")
                    var amount = list[i - temp].optString(1)
                    amount = if (coUnit == 0) amount else BigDecimalUtils.mulStr(amount, multiplier, multiplierPrecision)
                    sellViewList[i].tv_quantity_item.text = BigDecimalUtils.showDepthContractVolume(amount)

                } else {
                    sellViewList[i].run {
                        tv_price_item.text = "--"
                        tv_quantity_item.text = "--"
                        fl_bg_item.setBackgroundResource(R.color.transparent)
                    }

                }
            }


        }
    }

    /**
     * 买盘
     */
    private fun buyTape(list: ArrayList<JSONArray>, maxVol: Double) {

        /**
         * 买盘取最大
         */
        list.sortByDescending {
            it.optDouble(0)
        }

        for (i in 0 until buyViewList.size) {
            /**
             * 买盘
             */
            if (list.size > i) {
                /*****深度背景色START****/
                buyViewList[i].fl_bg_item.backgroundColor = ColorUtil.getMinorColorType()
                val layoutParams = buyViewList[i].fl_bg_item.layoutParams
                val width = (list[i].optDouble(1) / maxVol) * ll_all.measuredWidth * 0.4
                layoutParams.width = width.toInt()
                buyViewList[i].fl_bg_item.layoutParams = layoutParams

                /*****深度背景色END****/
                buyViewList[i].tv_price_item.text =
                        SymbolInterceptUtils.interceptData(
                                list[i].optString(0).replace("\"", "").trim(),
                                depth_level,
                                "price")
                var amount = list[i].optString(1).trim()
                amount = if (coUnit == 0) amount else BigDecimalUtils.mulStr(amount, multiplier, multiplierPrecision)
                LogUtil.e(TAG, "amount:" + amount)
                buyViewList[i].tv_quantity_item.text = BigDecimalUtils.showDepthContractVolume(amount)
            } else {
                buyViewList[i].run {
                    tv_price_item.text = "--"
                    tv_quantity_item.text = "--"
                    fl_bg_item.setBackgroundResource(R.color.transparent)
                }

            }
        }
    }

    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
        if (isVisible) {
            loopStart()
        } else {
            loopStop()
        }
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (!isVisibleToUser) {
            loopStop()
        }
    }

}