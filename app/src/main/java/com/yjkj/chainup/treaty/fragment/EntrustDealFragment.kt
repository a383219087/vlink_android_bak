package com.yjkj.chainup.treaty.fragment

import androidx.lifecycle.MutableLiveData
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yjkj.chainup.util.JsonUtils
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.bean.EntrustBean
import com.yjkj.chainup.bean.QuotesData
import com.yjkj.chainup.bean.TransactionData
import com.yjkj.chainup.manager.Contract2PublicInfoManager
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.util.DecimalDigitsInputFilter
import com.yjkj.chainup.new_version.dialog.DialogUtil
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.treaty.*
import com.yjkj.chainup.treaty.adapter.ContractCurrentEntrustOrderAdapter
import com.yjkj.chainup.treaty.bean.*
import com.yjkj.chainup.treaty.dialog.ContractDialog
import com.yjkj.chainup.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_entrust_deal.*
import kotlinx.android.synthetic.main.item_active_entrust_dealt.view.*
import kotlinx.android.synthetic.main.item_depth_contract.view.*
import kotlinx.android.synthetic.main.layout_entrust_orders_treaty.*
import kotlinx.android.synthetic.main.layout_trade_treaty.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

/**
 * @author Bertking
 * @description 合约的"委托交易"
 * @date 2019-1-10
 *
 * TODO 成本，可用余额and so on 目前的精度是固定的，可能后期需要修改为动态的
 */
class EntrustDealFragment : Fragment(), DialogUtil.ConfirmListener {
    val TAG = EntrustDealFragment::class.java.simpleName

    var tDialog: TDialog? = null
    private var contractId = 0
    private var currentLevel = ""

    private var currentSymbol = ""

    lateinit var currentContract: ContractBean

    private var marketType = LIMIT_TRADE
    /**
     * 买卖方向
     */
    private var side = BUY_SIDE

    private lateinit var mSocketClient: WebSocketClient

    private val red = ContextCompat.getColor(ChainUpApp.appContext, R.color.red)
    private val green = ContextCompat.getColor(ChainUpApp.appContext, R.color.green)
    private val mainFontColor = ContextCompat.getColor(ChainUpApp.appContext, R.color.main_font_color)

    /**
     * 标记
     */
    private var isEditMode = false


    /**
     * 卖盘的item
     */
    private var sellViewList = mutableListOf<View>()
    /**
     * 买盘的item
     */
    private var buyViewList = mutableListOf<View>()

    /**
     * 活动委托的adapter
     */
    var adapter = ContractCurrentEntrustOrderAdapter(arrayListOf())


    var initOrderPrice = ""


    private var chooseLevelViewList = lazy {
        arrayListOf(tv_lever, iv_lever, stv_select_level)
    }


    override fun click(pos: Int) {
        tDialog?.dismissAllowingStateLoss()
        changeLevel(pos)
    }


    // TODO: Rename and change types of parameters
    private val CONTRACT_ID = "contract_id"

    private var disposables = CompositeDisposable()

    /**
     * TODO 后期优化
     */
    private var disposables4OrderList = CompositeDisposable()


    companion object {
        @JvmStatic
        fun newInstance(contractId: Int = 0) =
                EntrustDealFragment().apply {
                    arguments = Bundle().apply {
                        putInt(CONTRACT_ID, contractId)
                    }
                }

        var liveData: MutableLiveData<String> = MutableLiveData()

        /**
         * 限价交易
         */
        const val LIMIT_TRADE = 1
        /**
         * 市价交易
         */
        const val MARKET_TRADE = 2

        /**
         * 买入/做多
         */
        const val SELL_SIDE = "SELL"
        /**
         * 卖出/做空
         */
        const val BUY_SIDE = "BUY"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contractId = it.getInt(CONTRACT_ID, 0)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
//        contractId = arguments?.getInt(ChangeTreatyActivity.CONTRACT)!!
        var mainView = inflater.inflate(R.layout.fragment_entrust_deal, container, false)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        return mainView
    }

    override fun onResume() {
        super.onResume()
        contractId = Contract2PublicInfoManager.currentContractId()
        currentContract = Contract2PublicInfoManager.currentContract() ?: return
        isEditMode = false

        loopPriceRiskPosition()

        if (!LoginManager.checkLogin(context, false)) {
            et_price?.isFocusableInTouchMode = false
            et_position?.isFocusableInTouchMode = false
            currentLevel = currentContract.maxLeverageLevel.toString()
            stv_select_level?.text = currentLevel + "x"
            tv_lever?.text = StringUtils.getString(R.string.title_lever) + currentLevel + "x"
        } else {
            getInitOrderInfo()
            if (et_position?.isFocusableInTouchMode?.not() == true) {
                et_position?.isFocusable = true
                et_position?.isFocusableInTouchMode = true
                et_position?.requestFocus()
                et_position?.findFocus()
            }
            if (et_price?.isFocusableInTouchMode?.not() == true) {
                et_price?.isFocusable = true
                et_price?.isFocusableInTouchMode = true
                et_price?.requestFocus()
                et_price?.findFocus()
            }
        }

        et_price?.filters = arrayOf(DecimalDigitsInputFilter(currentContract.pricePrecision
                ?: 4))
        loopOrderList4Contract()

        tv_limit_contract_name?.text = currentContract.quoteSymbol
        tv_market_contract_name?.text = currentContract.quoteSymbol

        val contractType = when (currentContract.contractType) {
            1 -> StringUtils.getString(R.string.week_of_contract)
            2 -> StringUtils.getString(R.string.In_the_contract)
            else -> {
                StringUtils.getString(R.string.contract_sustainable)
            }
        }

        tv_choose_contract?.text = currentContract.baseSymbol + "*" + contractType + "(" + currentContract.maxLeverageLevel + "x" + ")"


        Log.d(TAG, "========Level:${currentLevel}=======")

        Log.d(TAG, "========合约ID:$contractId====")
        if (::mSocketClient.isInitialized) {
            Log.d(TAG, "=======Socket init=========")
            subCurrentContractMsg()
        } else {
            Log.d(TAG, "=======Socket not init=========")
            initSocket()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initCostAndBalance()

        rv_active_orders?.setHasFixedSize(true)
        rv_active_orders?.layoutManager = LinearLayoutManager(context)
        rv_active_orders?.adapter = adapter
        adapter.setEmptyView(EmptyForAdapterView(context ?: return))
        adapter.setOnItemChildClickListener { adapter, view, position ->
            view.tv_order_status?.setOnClickListener {
                //                ToastUtils.showToast("TEST取消订单")
                if (adapter?.data?.isNotEmpty() == true) {
                    try {
                        var item = adapter.data.get(position) as ActiveOrderListBean.Order?
                        when (item?.status) {
                            0, 1, 3 -> {
                                cancelOrder(item.orderId.toString(), item.contractId.toString(), position)
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        /**
         * 历史委托
         */
        iv_entrust_record?.setOnClickListener {
            if (!LoginManager.checkLogin(activity, true)) return@setOnClickListener
//            ContractHistoryEntrustActivity.enter2(context!!)
        }

        et_price.setOnFocusChangeListener { v, hasFocus ->
            val resource = if (hasFocus) R.drawable.new_item_bg_focus else R.drawable.new_item_bg_unfocus
            ll_contract_price?.setBackgroundResource(resource)
        }

        et_position.setOnFocusChangeListener { v, hasFocus ->
            val resource = if (hasFocus) R.drawable.new_item_bg_focus else R.drawable.new_item_bg_unfocus
            ll_position.setBackgroundResource(resource)
        }


        initDepthView()

        onTipsClick()

        et_position?.setText("1")
        et_position?.setSelection(1)

        rg_trade_type?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                /**
                 * 限价
                 */
                R.id.rb_limit -> {
                    marketType = LIMIT_TRADE
                    ll_contract_price?.visibility = View.VISIBLE
                    tv_market_trade_tip?.visibility = View.GONE
                    var volume = et_position?.text.toString()
                    Log.d(TAG, "====volume1======" + volume)
                    getInitOrderInfo(volume, currentLevel)
                }
                /**
                 * 市价
                 */
                R.id.rb_market -> {
                    marketType = MARKET_TRADE
                    ll_contract_price?.visibility = View.GONE
                    tv_market_trade_tip?.visibility = View.VISIBLE
                    var volume = et_position?.text.toString()
                    Log.d(TAG, "====volume2======" + volume)
                    getInitOrderInfo(volume, currentLevel)
                }
            }
        }


        /**
         * 选择杠杆
         */
        chooseLevelViewList.value.forEach {
            it.setOnClickListener {
                if (!LoginManager.checkLogin(activity, true)) return@setOnClickListener
                tDialog = DialogUtil.showSelectLevelDialog(context!!, contractId, currentLevel, this)
                tDialog?.show()
            }
        }


        /**
         * 切换合约
         */
        tv_choose_contract?.setOnClickListener {
        }

        /**
         * 开多
         */
        tv_open_more?.setOnClickListener {
            tv_open_more?.textColor = green
            tv_open_empty?.textColor = mainFontColor
            tv_open_empty?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            tv_open_more?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.bg_buy_line)
            tv_place_order?.solid = green
            tv_place_order?.text = StringUtils.getString(R.string.contract_action_buy)
            side = BUY_SIDE
        }

        /**
         * 开空
         */
        tv_open_empty?.setOnClickListener {
            tv_open_empty?.textColor = red
            tv_open_more?.textColor = mainFontColor
            tv_open_more?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            tv_open_empty?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.bg_sell_line)
            tv_place_order?.solid = red
            side = SELL_SIDE
            tv_place_order?.text = StringUtils.getString(R.string.contract_action_sell)
        }

        /**
         * 下单
         * 如果是"市价单",「价格」使用 init_take_order接口返回的price字段
         */
        tv_place_order?.setOnClickListener {
            if (!LoginManager.checkLogin(activity, true)) return@setOnClickListener
            if (marketType == LIMIT_TRADE) {
                if (TextUtils.isEmpty(et_position?.text)) {
                    toast(StringUtils.getString(R.string.contract_tip_pleaseInputPosition))
                    return@setOnClickListener
                }

                if (TextUtils.isEmpty(et_price?.text)) {
                    toast(StringUtils.getString(R.string.contract_tip_pleaseInputPrice))
                    return@setOnClickListener
                }

                takeOrder(et_position?.text.toString(), et_price?.text.toString())
            } else {
                if (TextUtils.isEmpty(et_position.text)) {
                    toast(StringUtils.getString(R.string.contract_tip_pleaseInputPosition))
                    return@setOnClickListener
                }
                takeOrder(et_position?.text.toString(), initOrderPrice)
            }
        }


        et_price?.setOnClickListener {
            LoginManager.checkLogin(activity, true)
        }
        et_position?.setOnClickListener {
            LoginManager.checkLogin(activity, true)
        }


        /**
         * 监听仓位输入框
         */

        et_position?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (TextUtils.isEmpty(s)) {
                    initCostAndBalance()
                } else {
                    getInitOrderInfo(s.toString(), currentLevel)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })


        /**
         * 监听限价输入框
         */
        et_price?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                getInitOrderInfo(et_position?.text.toString(), currentLevel)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isEditMode = true
            }

        })


    }

    private fun initCostAndBalance() {
        tv_entrust_value?.text = StringUtils.getString(R.string.contract_text_entrustValue) + ": --"
        tv_can_use_balance?.text = StringUtils.getString(R.string.subtitle_available_balance) + ": --"
        tv_cost?.text = StringUtils.getString(R.string.cost) + ": --"
    }


    /**
     * 每5s调用一次接口
     */
    private fun loopPriceRiskPosition() {
        disposables.add(Observable.interval(0, 5, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()))
    }

    private fun getObserver(): DisposableObserver<Long> {
        return object : DisposableObserver<Long>() {
            override fun onComplete() {
            }

            override fun onNext(t: Long) {
                loopTagPrice()
                loopRiskFactor()
                loopPosition4Contract()
            }

            override fun onError(e: Throwable) {
            }
        }

    }


    /**
     * 轮询标记价格接口
     */
    fun loopTagPrice() {
        HttpClient.instance
                .getTagPrice4Contract(contractId = contractId.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<TagPriceBean>() {
                    override fun onHandleSuccess(bean: TagPriceBean?) {
                        tv_tag_price?.text = bean?.indexPrice.toString() + "/" + bean?.tagPrice

                        /**
                         * 价格默认：标记价格
                         */
                        if (!isEditMode) {
                            et_price?.setText(bean?.tagPrice.toString())
                            et_price?.setSelection(bean?.tagPrice.toString().length)
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.e(TAG, "=====code:$code,msg = $msg==")
                    }
                })
    }

    /**
     * 轮询风险系数接口
     */
    fun loopRiskFactor() {
        if (!LoginManager.checkLogin(activity, false)) {
            initCostAndBalance()
            return
        }
        HttpClient.instance
                .getRiskLiquidationRate(contractId = contractId.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<LiquidationRateBean>() {
                    override fun onHandleSuccess(bean: LiquidationRateBean?) {
                        Log.d(TAG, "===========" + bean.toString())
                        csrv_risk?.liveData?.postValue(bean?.liquidationRate?.toFloat())
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        Log.d(TAG, "====loopRiskFactor:code = $code,msg = $msg====")
                    }
                })
    }

    /**
     * 轮询用户持仓信息
     */
    private fun loopPosition4Contract() {
        if (!LoginManager.checkLogin(activity, false)) {
            tv_contract_id?.setBottom("--")
            tv_realised_rate?.setBottom("--")
            tv_open_position_price?.setBottom("--")
            tv_liquidation_price?.setBottom("--")
            return
        }

        HttpClient.instance
                .getPosition4Contract(contractId = contractId.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<UserPositionBean>() {
                    override fun onHandleSuccess(bean: UserPositionBean?) {
                        Log.d(TAG, "===========" + bean.toString())

                        val positions = bean?.positions


                        if (positions?.isNotEmpty() == true) {
                            val position = positions[0]

                            /**
                             * 合约价格精度（价格精度根据这个截取）
                             */
                            val pricePrecision = position?.pricePrecision ?: 2

                            /**
                             * 价值根据这个截取
                             */
                            val valuePrecision = position?.valuePrecision ?: 4

                            Log.d(TAG, "=========this===" + position.toString())
                            tv_contract_id?.setBottom(position?.volume.toString())
                            tv_realised_rate?.setBottom(position?.unrealisedRateMarket.toString() + "%")

                            /**
                             * 开仓价格
                             */
                            val avgPriceByPrecision = Contract2PublicInfoManager.cutValueByPrecision(position?.avgPrice.toString(), pricePrecision)
                            tv_open_position_price?.setBottom(avgPriceByPrecision)
                            /**
                             * 强平价格
                             */
                            val liquidationPriceByPrecision = Contract2PublicInfoManager.cutValueByPrecision(position?.liquidationPrice.toString(), pricePrecision)
                            tv_liquidation_price?.setBottom(liquidationPriceByPrecision)
                        } else {
                            tv_contract_id?.setBottom("--")
                            tv_realised_rate?.setBottom("--")
                            tv_open_position_price?.setBottom("--")
                            tv_liquidation_price?.setBottom("--")
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
//                        ToastUtils.showToast(msg)
                        Log.d(TAG, "====loopPosition4Contract:code = $code,msg = $msg====")
                        tv_contract_id?.setBottom("--")
                        tv_realised_rate?.setBottom("--")
                        tv_open_position_price?.setBottom("--")
                        tv_liquidation_price?.setBottom("--")

                    }
                })
    }


    /**
     * 修改杠杆
     */
    private fun changeLevel(position: Int) {
        if (!LoginManager.checkLogin(activity, false)) return
        HttpClient.instance
                .changeLevel4Contract(contractId = contractId.toString(), newLevel = Contract2PublicInfoManager.getLevelsByContractId(contractId)[position])
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(bean: Any?) {
                        currentLevel = Contract2PublicInfoManager.getLevelsByContractId(contractId)[position]
                        stv_select_level?.text = currentLevel + "x"
                        tv_lever?.text = StringUtils.getString(R.string.title_lever) + currentLevel + "x"
                        Log.d(TAG, "==当前杠杆==$currentLevel")

                        Log.d(TAG, "=======Level====" + bean.toString())
                        getInitOrderInfo(et_position?.text.toString(), currentLevel)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        ToastUtils.showToast(msg)
                        Log.e(TAG, "====changeLevel:code = $code,msg = $msg====")
                    }
                })
    }

    /**
     * 订单初始化信息
     */
    fun getInitOrderInfo(volume: String = et_position?.text.toString(), lever: String = "") {

        var price = if (marketType == LIMIT_TRADE) {
            et_price?.text.toString()
        } else {
            ""
        }
        HttpClient.instance
                .getInitTakeOrderInfo4Contract(contractId = contractId.toString()
                        , volume = volume,
                        price = price,
                        level = lever,
                        orderType = marketType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<InitTakeOrderBean>() {
                    override fun onHandleSuccess(bean: InitTakeOrderBean?) {
                        Log.d(TAG, "=====订单初始化======" + bean.toString())

                        initOrderPrice = bean?.price ?: ""

                        currentLevel = bean?.level.toString()
                        stv_select_level?.text = currentLevel + "x"
                        tv_lever?.text = StringUtils.getString(R.string.title_lever) + currentLevel + "x"
                        val contractType = when (currentContract.contractType) {
                            1 -> StringUtils.getString(R.string.week_of_contract)
                            2 -> StringUtils.getString(R.string.In_the_contract)
                            else -> {
                                StringUtils.getString(R.string.contract_sustainable)
                            }
                        }

                        tv_choose_contract?.text = currentContract.baseSymbol + "*" + contractType + "(" + currentLevel + "x" + ")"

                        /**
                         * 委托价值
                         */
                        val orderPriceValueByPrecision = Contract2PublicInfoManager.cutValueByPrecision(bean?.orderPriceValue.toString(), 4)
                        tv_entrust_value?.text = StringUtils.getString(R.string.contract_text_entrustValue) + ": $orderPriceValueByPrecision"

                        /**
                         * 可用余额
                         */
                        val canUseBalanceByPrecision = Contract2PublicInfoManager.cutValueByPrecision(bean?.canUseBalance.toString(), 4)
                        tv_can_use_balance?.text =
                                StringUtils.getString(R.string.withdraw_text_available) + ": $canUseBalanceByPrecision"

                        if (side == SELL_SIDE) {
                            val sellOrderCostByPrecision = Contract2PublicInfoManager.cutValueByPrecision(bean?.sellOrderCost.toString(), 4)
                            tv_cost?.text = StringUtils.getString(R.string.cost) + ": $sellOrderCostByPrecision"
                        } else {
                            val bugOrderCostByPrecision = Contract2PublicInfoManager.cutValueByPrecision(bean?.buyOrderCost.toString(), 4)
                            tv_cost?.text = StringUtils.getString(R.string.cost) + ": $bugOrderCostByPrecision"
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        Log.d(TAG, "====getInitOrderInfo:code = $code,msg = $msg====")
                    }
                })
    }

    /**
     *  下单
     */
    private fun takeOrder(volume: String = "1", price: String) {
        HttpClient.instance
                .takeOrder4Contract(contractId = contractId.toString()
                        , volume = volume,
                        price = price,
                        orderType = marketType,
                        side = side,
                        level = currentLevel
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(bean: Any?) {
                        Log.d(TAG, "===========" + bean.toString())
                        et_price?.setText("")
                        ToastUtils.showToast(context?.getString(R.string.contract_tip_submitSuccess))
                        getOrderList4Contract()
                        getInitOrderInfo(currentLevel)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        ToastUtils.showToast(msg)
                        Log.d(TAG, "====takeOrder:code = $code,msg = $msg====")
                    }
                })
    }

    /**
     * 活动委托列表
     */
    private fun getOrderList4Contract() {
        if (!LoginManager.checkLogin(activity, false)) return
//        HttpClient.instance
//                .getOrderList4Contract()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : NetObserver<ActiveOrderListBean>() {
//                    override fun onHandleSuccess(bean: ActiveOrderListBean?) {
//                        Log.d(TAG, "===========" + bean.toString())
//                        if (bean?.orderList?.isNotEmpty() == true) {
//                            adapter.replaceData(bean.orderList)
//                            loopOrder()
//                        } else {
//                            adapter.replaceData(arrayListOf())
//                            disposables4OrderList.clear()
//                            disposables4OrderList.dispose()
//                        }
//                    }
//
//                    override fun onHandleError(code: Int, msg: String?) {
//                        disposables4OrderList.clear()
//                        disposables4OrderList.dispose()
//                        Log.d(TAG, "====getOrderList4Contract:code = $code,msg = $msg====")
//                    }
//                })
    }

    /**
     * 活动委托列表
     */
    private fun loopOrderList4Contract() {
        if (!LoginManager.checkLogin(activity, false)) return
//        HttpClient.instance
//                .getOrderList4Contract()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : NetObserver<ActiveOrderListBean>() {
//                    override fun onHandleSuccess(bean: ActiveOrderListBean?) {
//                        Log.d(TAG, "===========" + bean.toString())
//                        if (bean?.orderList?.isNotEmpty() == true) {
//                            adapter.replaceData(bean.orderList)
//                        } else {
//                            Log.d(TAG, "======订单为空=====")
//                            adapter.replaceData(arrayListOf())
//                            disposables4OrderList.clear()
//                            disposables4OrderList.dispose()
//                        }
//                    }
//
//                    override fun onHandleError(code: Int, msg: String?) {
//                        disposables4OrderList.clear()
//                        disposables4OrderList.dispose()
//                        Log.d(TAG, "====takeOrder:code = $code,msg = $msg====")
//                    }
//                })
    }


    /**
     * 每5s调用一次接口
     */
    private fun loopOrder() {
        disposables.add(Observable.interval(0, 5, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getOrderObserver()))
    }

    private fun getOrderObserver(): DisposableObserver<Long> {
        return object : DisposableObserver<Long>() {
            override fun onComplete() {
            }

            override fun onNext(t: Long) {
                loopOrderList4Contract()
            }

            override fun onError(e: Throwable) {
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        disposables4OrderList.clear()
    }


    private fun initSocket() {
        Log.d("======", "==initSocket===")
        mSocketClient = object : WebSocketClient(URI(ApiConstants.SOCKET_CONTRACT_ADDRESS)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.i(TAG, "onOpen")
                subCurrentContractMsg()
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.i(TAG, "onClose$reason")
            }


            override fun onMessage(bytes: ByteBuffer?) {
                super.onMessage(bytes)
                if (bytes == null) return
                val data = GZIPUtils.uncompressToString(bytes.array())
                if (!data.isNullOrBlank()) {
                    if (data.contains("ping")) {
                        val replace = data.replace("ping", "pong")
                        Log.d(TAG, "=====lllllllllllll=======$replace")
                        mSocketClient.send(replace)
                    } else {
                        handleData(data)
                    }
                }
            }

            override fun onMessage(message: String?) {
                Log.i(TAG, "onMessage")
            }

            override fun onError(ex: Exception?) {
                Log.i(TAG, "onError" + ex?.printStackTrace())
            }

        }
        mSocketClient.connect()
    }


    /**
     * 订阅当前合约的24H行情&深度
     */
    fun subCurrentContractMsg() {
        Log.d(TAG, "======11==")
        val contract = Contract2PublicInfoManager.currentContract(lastSymbol = currentSymbol)
                ?: return
        Log.d(TAG, "======112==")

        val lastSymbol = contract.lastSymbol
        Log.d(TAG, "======112==" + lastSymbol)

        if (!::mSocketClient.isInitialized || !mSocketClient.isOpen) {
            initSocket()
        }
        currentSymbol = Contract2PublicInfoManager.currentContract()?.symbol ?: ""

        Log.d(TAG, "======last = $lastSymbol,current = $currentSymbol============")
        if (currentSymbol == lastSymbol) {

            return
        } else {
            if (mSocketClient.isOpen) {
                Log.d(TAG, "====Socket:=====" + mSocketClient.isOpen)
                if (!contract.lastSymbol.isNullOrBlank()) {
                    mSocketClient.send(WsLinkUtils.tickerFor24HLink(contract.lastSymbol?.toLowerCase()!!, false))
                    mSocketClient.send(WsLinkUtils.getDepthLink(contract.lastSymbol?.toLowerCase()!!, false).json)
                    clearDepthView()
                }
                mSocketClient.send(WsLinkUtils.tickerFor24HLink(currentSymbol.toLowerCase()))
                mSocketClient.send(WsLinkUtils.getDepthLink(currentSymbol.toLowerCase()).json)
            } else {
                initSocket()
            }

        }
    }

    fun handleData(data: String) {
        Log.d(TAG, "======合约WS返回数据====" + data)
        doAsync {
            val json = JSONObject(data)
            val tickJson = json.optJSONObject("tick") ?: return@doAsync
            if (tickJson.has("buys")) {
                Log.d(TAG, "=====深度=====$data")
                /**
                 * 深度
                 */
                var transactionData = JsonUtils.jsonToBean(data, TransactionData::class.java)
                /**
                 *卖盘取最小
                 */
                transactionData.tick?.asks?.sortByDescending { it.get(0).asDouble }
                /**
                 * 买盘取最大
                 */
                transactionData.tick?.buys?.sortByDescending { it.get(0).asDouble }
                Log.d(TAG, "========111=====" + transactionData.tick?.asks)

                uiThread {
                    refreshDepthView(transactionData)
                }
            } else {
                Log.d(TAG, "=====24H行情=====$data")
                /**
                 * 24H
                 */
                val tick = JsonUtils.jsonToBean(tickJson.toString(), QuotesData.Tick::class.java)
                if (TextUtils.isEmpty(tick.vol)) return@doAsync
                uiThread {
                    tv_new_price?.text = "--"
                    render24H(tick)
                }
            }


        }
    }

    var lastClosePrice = ""

    /**
     * 渲染24H行情数据
     */
    private fun render24H(tick: QuotesData.Tick) {
        if (lastClosePrice == "") {
            tv_price?.setTextColor(mainFontColor)
            tv_new_price?.setTextColor(mainFontColor)
            tv_price?.text = Contract2PublicInfoManager.cutValueByPrecision(tick.close)
            tv_new_price?.text = Contract2PublicInfoManager.cutValueByPrecision(tick.close)

            iv_price_trend?.setImageResource(R.drawable.ic_price_descend)
            iv_price_trend?.visibility = View.INVISIBLE
            lastClosePrice = tick.close
        } else {
            when (BigDecimalUtils.compareTo(tick.close, lastClosePrice)) {
                0 -> {
                    tv_price?.setTextColor(mainFontColor)
                    tv_new_price?.setTextColor(mainFontColor)
                    tv_price?.text = Contract2PublicInfoManager.cutValueByPrecision(tick.close)
                    tv_new_price?.text = Contract2PublicInfoManager.cutValueByPrecision(tick.close)
                    iv_price_trend?.setImageResource(R.drawable.ic_price_descend)
                    iv_price_trend?.visibility = View.INVISIBLE

                }

                1 -> {
                    tv_price?.setTextColor(green)
                    tv_new_price?.setTextColor(green)
                    tv_price?.text = Contract2PublicInfoManager.cutValueByPrecision(tick.close)
                    tv_new_price?.text = Contract2PublicInfoManager.cutValueByPrecision(tick.close)

                    iv_price_trend?.setImageResource(R.drawable.ic_price_ascend)
                    iv_price_trend?.visibility = View.VISIBLE
                }

                -1 -> {
                    tv_price?.setTextColor(red)
                    tv_new_price?.setTextColor(red)
                    tv_price?.text = Contract2PublicInfoManager.cutValueByPrecision(tick.close)
                    tv_new_price?.text = Contract2PublicInfoManager.cutValueByPrecision(tick.close)

                    iv_price_trend?.setImageResource(R.drawable.ic_price_descend)
                    iv_price_trend?.visibility = View.VISIBLE
                }
            }
            lastClosePrice = tick.close

        }

    }

    /**
     * 买卖盘
     *
     * 初始化交易详情记录view
     */
    private fun initDepthView() {
        for (i in 0 until 5) {
            /**
             * 卖盘
             */
            val view: View = layoutInflater.inflate(R.layout.item_depth_contract, null)

            view.tv_price?.setTextColor(red)
            view.tv_position?.setTextColor(red)
            view.tv_amount?.setTextColor(red)
            /**
             * 点击 买卖盘，将价格显示在 "买入价"中
             */
            view.setOnClickListener {
                buyOrSellItemClick(view)
            }
            ll_sell_price?.addView(view)
            sellViewList.add(view)

            /***********/


            /**
             * 买盘
             */
            val view1: View = layoutInflater.inflate(R.layout.item_depth_contract, null)

            view1.tv_price?.setTextColor(green)
            view1.tv_position?.setTextColor(green)
            view1.tv_amount?.setTextColor(green)
            view1.setOnClickListener {
                buyOrSellItemClick(view1)
            }
            ll_buy_price?.addView(view1)
            buyViewList.add(view1)
        }
    }


    /**
     * 处理 "买卖盘"的点击事件
     */
    private fun buyOrSellItemClick(view1: View) {
        val result = view1.tv_price?.text.toString()
        if (result != "--" && marketType == LIMIT_TRADE) {
            et_price?.setText(result)
            et_price?.setSelection(result.length)
        }
    }


    /**
     * 更新买卖盘的数据
     */
    private fun refreshDepthView(transactionData: TransactionData) {
        val tick: TransactionData.Tick = transactionData.tick ?: return
        /**
         * 卖盘交易量最大的
         */
        val askMaxVolJson = tick.asks.maxBy { it.get(1).asDouble }
        val askMaxVol = askMaxVolJson?.get(1)?.asDouble

        /**
         * 买盘交易量最大的
         */
        val buyMaxVolJson = tick.buys.maxBy { it.get(1).asDouble }
        val buyMaxVol = buyMaxVolJson?.get(1)?.asDouble

        for (i in 0 until sellViewList.size) {
            /**
             * 卖盘
             */
            if (tick.asks.size > sellViewList.size) {
                /**
                 * 移除大值
                 */
                val subList = tick.asks.subList(tick.asks.size - sellViewList.size, tick.asks.size)

                /*****深度背景色START****/
                sellViewList[0].ll_item.post {
                    val measuredWidth = sellViewList[0].ll_item.measuredWidth
                    sellViewList[i].fl_bg_item.setBackgroundResource(R.color.entrust_sell_color)
                    val layoutParams = sellViewList[i].fl_bg_item.layoutParams
                    val width = (subList[i].get(1).asDouble / askMaxVol!!) * measuredWidth
                    layoutParams.width = width.toInt()
                    Log.d(TAG, "====1111=width:${width.toInt()}=======")
                    sellViewList[i].fl_bg_item.layoutParams = layoutParams
                }

                /*****深度背景色END****/

                sellViewList[i].tv_price.text =
                        Contract2PublicInfoManager.cutValueByPrecision(subList[i].get(0).asString)



                sellViewList[i].tv_position.text = BigDecimalUtils.formatNumber(subList[i].get(1).toString())


                /**
                 * 总量
                 */
                val sumByDouble = ArrayList(subList.asReversed().subList(0, i + 1)).sumByDouble {
                    it[1].asDouble
                }

                sellViewList[sellViewList.lastIndex - i].tv_amount?.text = sumByDouble.toInt().toString()


            } else {

                val temp = sellViewList.size - tick.asks.size
                sellViewList[i].tv_price?.text = "--"
                sellViewList[i].tv_position?.text = "--"
                if (i >= temp) {
                    /*****深度背景色START****/
                    sellViewList[0].ll_item.post {
                        val measuredWidth = sellViewList[0].ll_item.measuredWidth
                        sellViewList[i].fl_bg_item.setBackgroundResource(R.color.entrust_sell_color)
                        val layoutParams = sellViewList[i].fl_bg_item.layoutParams
                        val width = (tick.asks[i - temp].get(1).asDouble / askMaxVol!!) * measuredWidth
                        Log.d(TAG, "=========${tick.asks[i - temp].get(1).asDouble}=,${askMaxVol}==,${measuredWidth}=======")
                        layoutParams.width = width.toInt()
                        Log.d(TAG, "====1111=width:${width.toInt()}=======")
                        sellViewList[i].fl_bg_item.layoutParams = layoutParams
                    }

                    /*****深度背景色END****/

                    /**
                     * 价格
                     */
                    sellViewList[i].tv_price?.text = Contract2PublicInfoManager.cutValueByPrecision(tick.asks[i - temp].get(0).asString)


                    /**
                     * 仓位
                     */
                    sellViewList[i].tv_position?.text = BigDecimalUtils.formatNumber(tick.asks[i - temp].get(1).asString)


                    /**
                     * 总量
                     */
                    val sumByDouble = ArrayList(tick.asks.reversed().subList(0, i - temp + 1)).sumByDouble {
                        it[1].asDouble
                    }
                    sellViewList[4 - i + temp].tv_amount?.text = sumByDouble.toInt().toString()

                } else {
                    clearDepthView(SELL_SIDE)
                }
            }

            /**
             * 买盘
             */
            if (tick.buys.size > i) {
                /*****深度背景色START****/
                buyViewList[i].ll_item.post {
                    var llWidth = buyViewList[0].ll_item.measuredWidth

                    buyViewList[i].fl_bg_item.setBackgroundResource(R.color.entrust_buy_color)
                    val layoutParams = buyViewList[i].fl_bg_item.layoutParams
                    val width = (tick.buys[i].get(1).asDouble / buyMaxVol!!) * llWidth
                    Log.d(TAG, "=====buy====${tick.buys[i].get(1).asDouble}=,${buyMaxVol}==,${llWidth}=======")
                    layoutParams.width = width.toInt()
                    Log.d(TAG, "==buy==1111=width:${width.toInt()}=======")
                    buyViewList[i].fl_bg_item.layoutParams = layoutParams
                }


                /*****深度背景色END****/

                Log.d(TAG, "=========买盘$i=====")
                val price4DepthBuy = tick.buys[i].get(0).asString

                Log.d(TAG, "=========buy===" + price4DepthBuy + ", " + (buyViewList[i].tv_price == null))

                buyViewList[i].tv_price?.text = Contract2PublicInfoManager.cutValueByPrecision(price4DepthBuy)

                buyViewList[i].tv_position?.text = BigDecimalUtils.formatNumber(tick.buys[i].get(1).asString)

                /**
                 * 总量
                 */
                val sumByDouble = ArrayList(tick.buys.subList(0, i + 1)).sumByDouble {
                    it[1].asDouble
                }

                buyViewList[i].tv_amount?.text = sumByDouble.toInt().toString()

            } else {
                buyViewList[i].tv_price.text = "--"
                buyViewList[i].tv_position.text = "--"
                buyViewList[i].tv_amount.text = "--"
                buyViewList[i].fl_bg_item.setBackgroundResource(R.color.transparent)
            }
        }
    }


    private fun onTipsClick() {
        /**
         * 风险评估 Tips
         */
        csrv_risk?.setOnClickListener {
            ContractDialog.showDialog4Risk(context!!)
        }

        /**
         * 指示价格
         */
        tv_tag_price?.setOnClickListener {
            ContractDialog.showDialog4ThePrice(context!!)
        }


        /**
         * 成本
         */
        tv_cost?.setOnClickListener {
            ContractDialog.showDialog4TheCostOf(context!!)
        }

        /**
         * 强平价格
         */
        tv_liquidation_price?.onTipsListener = object : ComTitleValueView.OnTipsListener {
            override fun onClick() {
                ContractDialog.showDialog4FlatPricer(context!!)
            }

        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EntrustBean) {
        if (event.position == 0) {
            //开多
            tv_open_more?.textColor = green
            tv_open_empty?.textColor = mainFontColor
            tv_open_empty?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            tv_open_more?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.bg_buy_line)
            tv_place_order?.solid = green
            tv_place_order?.text = StringUtils.getString(R.string.contract_action_buy)
            side = BUY_SIDE
        } else {
            //开空
            tv_open_empty?.textColor = red
            tv_open_more?.textColor = mainFontColor
            tv_open_more?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            tv_open_empty?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.bg_sell_line)
            tv_place_order?.solid = red
            side = SELL_SIDE
            tv_place_order?.text = StringUtils.getString(R.string.contract_action_sell)
        }

    }

    /**
     * 取消订单
     */
    private fun cancelOrder(orderId: String, contractId: String, pos: Int) {
        HttpClient.instance
                .cancelOrder4Contract(
                        contractId = contractId,
                        orderId = orderId
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(bean: Any?) {
                        getInitOrderInfo(currentLevel)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                    }
                })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            getInitOrderInfo()
        }
    }

    /**
     * 重置买卖盘的数据
     */
    fun clearDepthView(side: String = "") {
        when (side) {
            SELL_SIDE -> {
                sellViewList.forEach {
                    clearDepthItem(it)
                }
            }

            BUY_SIDE -> {
                buyViewList.forEach {
                    clearDepthItem(it)
                }
            }

            else -> {
                sellViewList.forEach {
                    clearDepthItem(it)
                }
                buyViewList.forEach {
                    clearDepthItem(it)
                }
            }
        }


    }

    private fun clearDepthItem(it: View) {
        it.tv_position?.text = "--"
        it.tv_amount?.text = "--"
        it.tv_price?.text = "--"
        it.fl_bg_item?.setBackgroundResource(R.color.transparent)
    }


}
