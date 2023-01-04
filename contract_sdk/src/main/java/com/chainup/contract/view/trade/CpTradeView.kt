package com.chainup.contract.view.trade

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.chainup.contract.R
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.app.CpMyApp.Companion.instance
import com.chainup.contract.app.CpParamConstant
import com.chainup.contract.bean.CpCreateOrderBean
import com.chainup.contract.bean.CpCurrentOrderBean
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.listener.CpDoListener
import com.chainup.contract.utils.*
import com.chainup.contract.view.CpCommonlyUsedButton
import com.chainup.contract.view.CpDialogUtil
import com.chainup.contract.view.CpNewDialogUtils
import com.jakewharton.rxbinding2.view.RxView
import com.timmy.tdialog.TDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.cp_item_commonly_used_button.view.*
import kotlinx.android.synthetic.main.cp_trade_amount_view_new.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.view
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


@SuppressLint("CustomViewStyleable")
class CpTradeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    val TAG = CpTradeView::class.java.simpleName
    private val buyOrSellHelper = CpContractBuyOrSellHelper()

    //交易类型
    var transactionType = CpParamConstant.TYPE_BUY
    var isLever = false
    var isPercentPlaceOrder = false
    var dialog: TDialog? = null
    var isRivalPriceModel = false
    var isMarketPriceModel = false

    var mUserConfigInfoJson: JSONObject? = null
    var mUserAssetsInfoJson: JSONObject? = null
    var mContractJson: JSONObject? = null

    var mContractId = 0
    var contractSide = ""
    var percent = "0.0"
    var canOpenBuy = "0"
    var canOpenSell = "0"
    var positionType = ""
    var maxOpenLimit = "0"
    var positionValue = "0"
    var entrustedValue = "0"
    var price = "0"
    var triggerPrice = "0"
    var multiplier = "0"
    var canUseAmount = "0"
    var canCloseVolumeBuy = "0"
    var canCloseVolumeSell = "0"
    var level = 20
    var marginModel = ""
    var marginRate = "0"
    var marginCoin = ""
    var marginCoinPrecision = 0
    var multiplierPrecision = 0
    var symbolPricePrecision = 9
    var priceType = 1
    var base = ""
    var quote = ""

    var mContractUint = 0

    var buyMaxPrice = ""
    var askMaxPrice = ""
    var lastPrice = ""

    var buyMaxPriceList = arrayListOf<JSONArray>()
    var sellMaxPriceList = arrayListOf<JSONArray>()

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ComVerifyView, 0, 0)
            typedArray.recycle()
        }
        LayoutInflater.from(context).inflate(R.layout.cp_trade_amount_view_new, this, true)
        tv_rival_price_type.setContent(context.getString(R.string.cp_overview_text38))

        et_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_price?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
        }

        et_volume?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (isPercentPlaceOrder) {
                    et_volume.setText("")
                    isPercentPlaceOrder = !isPercentPlaceOrder
                }
                rg_trade.clearCheck()
            }
        }

        et_stop_profit_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_stop_profit_price?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
        }

        et_stop_loss_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_stop_loss_price?.setBackgroundResource(if (hasFocus) R.drawable.cp_bg_trade_et_focused else R.drawable.cp_bg_trade_et_unfocused)
        }

        et_trigger_price.numberFilter(symbolPricePrecision, otherFilter = object : CpDoListener {
            override fun doThing(obj: Any?): Boolean {
                updateAvailableVol()
                return true
            }
        })

        et_price.numberFilter(symbolPricePrecision, otherFilter = object : CpDoListener {
            override fun doThing(obj: Any?): Boolean {
                updateAvailableVol()
                return true
            }
        })
        et_volume.numberFilter(if (mContractUint == 0) 0 else multiplierPrecision, otherFilter = object : CpDoListener {
            override fun doThing(obj: Any?): Boolean {
                updateAvailableVol()
                return true
            }
        })

        rb_buy.setSafeListener {
            transactionType = CpParamConstant.TYPE_BUY
            changeBuyOrSellUI()
            clearUIFocus()
            buyOrSellHelper.isOpen = true
            val mCpMessageEvent = CpMessageEvent(CpMessageEvent.sl_contract_modify_depth_event)
            mCpMessageEvent.msg_content = buyOrSellHelper
            CpEventBusUtil.post(mCpMessageEvent)
        }
        rb_sell.setSafeListener {
            transactionType = CpParamConstant.TYPE_SELL
            changeBuyOrSellUI()
            clearUIFocus()
            buyOrSellHelper.isOpen = false
            val mCpMessageEvent = CpMessageEvent(CpMessageEvent.sl_contract_modify_depth_event)
            mCpMessageEvent.msg_content = buyOrSellHelper
            CpEventBusUtil.post(mCpMessageEvent)
        }


        btn_login_contract.setSafeListener {
            if (!CpClLogicContractSetting.isLogin()) {
                CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_login_page))
            }
        }

        btn_open_contract.setSafeListener {
            CpDialogUtil.showCreateContractDialog(context, object : CpNewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_open_contract_event))
                }
            })
        }


        changePriceType(1)
        priceType = 1 //选择订单类型
        tv_order_type?.view()?.let {
            RxView.clicks(it).throttleFirst(500L, TimeUnit.MILLISECONDS) // 1秒内只有第一次点击有效
                .observeOn(AndroidSchedulers.mainThread()).subscribe { x ->
                    clearUIFocus()
                    tv_order_type?.stratAnim()
                    CpDialogUtil.createCVCOrderPop(context,
                        buyOrSellHelper.orderType,
                        it,
                        object : CpNewDialogUtils.DialogOnSigningItemClickListener {
                            override fun clickItem(position: Int, text: String) {
                                tv_order_type?.textContent = text
                                priceType = position
                                changePriceType(position)
                            }
                        },
                        object : CpNewDialogUtils.DialogOnDismissClickListener {
                            override fun clickItem() {
                                tv_order_type?.stopAnim()
                            }
                        })
                }
        } //选择对手价档位
        tv_rival_price_type?.view()?.let {
            RxView.clicks(it).throttleFirst(500L, TimeUnit.MILLISECONDS) // 1秒内只有第一次点击有效
                .observeOn(AndroidSchedulers.mainThread()).subscribe { x ->
                    clearUIFocus()
                    tv_rival_price_type?.stratAnim()
                    CpDialogUtil.createRivalPricePop(context, buyOrSellHelper, it, object : CpNewDialogUtils.DialogOnSigningItemClickListener {
                        override fun clickItem(position: Int, text: String) {
                            tv_rival_price_type?.textContent = text
                        }
                    }, object : CpNewDialogUtils.DialogOnDismissClickListener {
                        override fun clickItem() {
                            tv_rival_price_type?.stopAnim()
                        }
                    })
                }
        }
        img_buy_transfer.setOnClickListener {
            CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_transfer))
        }
        img_sell_transfer.setOnClickListener {
            CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_transfer))
        } //点击条件单下的市价单
        tv_price_hint.setOnClickListener {
            isMarketPriceModel = !isMarketPriceModel
            if (CpClLogicContractSetting.getExecution(CpMyApp.instance()) == 1) {
                CpClLogicContractSetting.setExecution(CpMyApp.instance(), 0)
                et_price.requestFocus()
            } else {
                CpClLogicContractSetting.setExecution(CpMyApp.instance(), 1)
                et_price.clearFocus()
            }
            updataMarketPriceUI()
            updateAvailableVol()
            clearUIFocus()
        } //点击对手价
        tv_rival_price_hint.setOnClickListener {
            isRivalPriceModel = !isRivalPriceModel
            updataRivalPriceUI()
            clearUIFocus()
        } //只减仓
        ll_only_reduce_positions.setSafeListener {
            cb_only_reduce_positions.isChecked = !cb_only_reduce_positions.isChecked
            clearUIFocus()
        } //只减仓选择监听
        cb_only_reduce_positions.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                cb_stop_loss.isChecked = false
            }
            transactionType = if (!isChecked) CpParamConstant.TYPE_BUY else CpParamConstant.TYPE_SELL
            changeBuyOrSellUI()
            buyOrSellHelper.isOpen = !isChecked
            val mCpMessageEvent = CpMessageEvent(CpMessageEvent.sl_contract_modify_depth_event)
            mCpMessageEvent.msg_content = buyOrSellHelper
            CpEventBusUtil.post(mCpMessageEvent)
        } //输入数量的单位切换
        tv_volume_unit.setOnClickListener {
            CpClLogicContractSetting.setIsUSDT(context, !CpClLogicContractSetting.getIsUSDT(context))
            tv_volume_unit.text = if (CpClLogicContractSetting.getIsUSDT(context)) quote else base
            updateAvailableVol()
        }

        //止盈止损
        ll_stop_loss.setSafeListener {
            cb_stop_loss.isChecked = !cb_stop_loss.isChecked
            clearUIFocus()
        } //止盈止损选择监听
        cb_stop_loss.setOnCheckedChangeListener { _, isChecked ->
            ll_stop_profit_loss_price.visibility = if (isChecked) View.VISIBLE else View.GONE
            et_stop_profit_price.setText("")
            et_stop_loss_price.setText("")
            buyOrSellHelper.isOto = isChecked
            val mCpMessageEvent = CpMessageEvent(CpMessageEvent.sl_contract_modify_depth_event)
            mCpMessageEvent.msg_content = buyOrSellHelper
            CpEventBusUtil.post(mCpMessageEvent)
        }

        for (buff in 0 until rg_trade?.childCount!!) {
            rg_trade.getChildAt(buff).setOnClickListener {
                isPercentPlaceOrder = true
                et_volume.clearFocus()
                when (it.id) {
                    R.id.rb_1st -> {
                        et_volume.setText(rb_1st.text.toString())
                        adjustRatio((0.25 * level).toString())
                    }
                    R.id.rb_2nd -> {
                        et_volume.setText(rb_2nd.text.toString())
                        adjustRatio((0.5 * level).toString())
                    }
                    R.id.rb_3rd -> {
                        et_volume.setText(rb_3rd.text.toString())
                        adjustRatio((0.75 * level).toString())
                    }
                    R.id.rb_4th -> {
                        et_volume.setText(rb_4th.text.toString())
                        adjustRatio((1 * level).toString())
                    }
                }
            }
        }

        btn_buy.isEnable(true)
        btn_buy.setBgColor(CpColorUtil.getMainColorType(true))
        btn_buy.listener = object : CpCommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {

                if (et_volume.text.isNullOrEmpty()) {
                    CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_trade_text3))
                    return
                } //                val canBuy1 = if (isPercentPlaceOrder || priceType > 3) {
                //                    true
                //                } else { //输入框单位是币币的时候约等于多少张要大于1
                //                    CpBigDecimalUtils.stringToNum1(tv_equivalent.text.toString()).toDouble() > CpBigDecimalUtils.showSNormal(multiplier).toDouble()
                //
                //                }
                //
                //
                //                if (!canBuy1) {
                //                    CpNToastUtil.showTopToastNet(getActivity(),
                //                        false,
                //                        context.getString(R.string.cp_trade_text2) + context.getString(R.string.cp_trade_text4) + context.getString(R.string.cp_trade_text1))
                //                    return
                //                }
                var isOpen = false;
                isOpen = transactionType == CpParamConstant.TYPE_BUY
                if (isOpen && mUserConfigInfoJson?.optInt("forceKycOpen") == 1) {
                    when {
                        mUserConfigInfoJson?.optInt("authLevel") == 3 -> {
                            goKycTips(context.getString(R.string.cl_kyc_8));
                            return
                        }
                        mUserConfigInfoJson?.optInt("authLevel") == 2 -> {
                            goKycTips(context.getString(R.string.cl_kyc_8));
                            return
                        }
                        mUserConfigInfoJson?.optInt("authLevel") == 0 -> {
                            kycTips(context.getString(R.string.cl_kyc_9));
                            return
                        }
                    }
                }
                doBuyOrSell("BUY")
            }
        }
        btn_sell.isEnable(true)
        btn_sell.setBgColor(CpColorUtil.getMainColorType(false))
        btn_sell.listener = object : CpCommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (et_volume.text.isNullOrEmpty()) {
                    CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_trade_text3))
                    return
                }

                //                val canBuy1 = if (isPercentPlaceOrder || priceType > 3) {
                //                    true
                //                } else { //输入框单位是币币的时候要大于合约面值
                //                    CpBigDecimalUtils.stringToNum1(tv_equivalent.text.toString()).toDouble() > CpBigDecimalUtils.showSNormal(multiplier).toDouble()
                //                }
                //                if (!canBuy1) {
                //                    CpNToastUtil.showTopToastNet(getActivity(),
                //                        false,
                //                        context.getString(R.string.cp_trade_text2) + context.getString(R.string.cp_trade_text4) + context.getString(R.string.cp_trade_text1))
                //                    return
                //                }

                val isOpen = transactionType == CpParamConstant.TYPE_BUY
                if (isOpen && mUserConfigInfoJson?.optInt("forceKycOpen") == 1) {
                    when {
                        mUserConfigInfoJson?.optInt("authLevel") == 3 -> {
                            goKycTips(context.getString(R.string.cl_kyc_8));
                            return
                        }
                        mUserConfigInfoJson?.optInt("authLevel") == 2 -> {
                            goKycTips(context.getString(R.string.cl_kyc_8));
                            return
                        }
                        mUserConfigInfoJson?.optInt("authLevel") == 0 -> {
                            kycTips(context.getString(R.string.cl_kyc_9));
                            return
                        }
                    }
                }
                doBuyOrSell("SELL")
            }
        }
    }

    private fun clearUIFocus() {
        clearFocus()
        CpSoftKeyboardUtil.hideSoftKeyboard(getActivity())
    }

    //点击盘口切换对手价模式
    private fun updateRivalPriceUI() {
        isRivalPriceModel = false
        updataRivalPriceUI()
    }

    fun updatePrice(tickPrice: String) {
        et_price?.setText(tickPrice)
        if (buyOrSellHelper.orderType == 1) {
            updateRivalPriceUI()
        }
    }

    fun initTick(tickPrice: String) {
        et_price.setText(tickPrice)
    }

    fun setTickLastPrice(buyMaxPrice: String, sellMaxPrice: String, lastPrice: String) {
        if (!TextUtils.isEmpty(buyMaxPrice)) {
            this.buyMaxPrice = buyMaxPrice
        }
        if (!TextUtils.isEmpty(sellMaxPrice)) {
            this.askMaxPrice = sellMaxPrice
        }
        if (!TextUtils.isEmpty(lastPrice)) {
            this.lastPrice = lastPrice
        }
        updateAvailableVol()
    }

    fun setTickPrice(buyMaxPriceList: ArrayList<JSONArray>?, sellMaxPriceList: ArrayList<JSONArray>?) {
        buyMaxPriceList?.let {
            this.buyMaxPriceList = buyMaxPriceList
        }
        sellMaxPriceList?.let {
            this.sellMaxPriceList = sellMaxPriceList
        }
    }

    fun editPriceIsNull(): Boolean {
        if (et_price.text.isNullOrEmpty() && !et_price.isFocused) {
            return true
        }
        return false
    }

    private fun doBuyOrSell(side: String) {
        var isOpen = false;
        var isConditionOrder = false
        var orderType = 1;
        var dialogTitle = ""
        var volume = if (CpClLogicContractSetting.getIsUSDT(context)) {
            et_volume.text.toString()

        } else {
            CpBigDecimalUtils.mulStr(et_volume.text.toString(), price, multiplierPrecision)
        }
        Log.d("我是输入的金额6", volume.toString())
        isOpen = transactionType == CpParamConstant.TYPE_BUY
        val isStopLoss = cb_stop_loss.isChecked
        val stopProfitPrice = et_stop_profit_price.text.toString().trim()
        val stopLossPrice = et_stop_loss_price.text.toString().trim()
        var price = et_price.text.toString()
        val triggerPrice = et_trigger_price.text.toString() //触发价格

        if (isOpen && isStopLoss) {
            if (TextUtils.isEmpty(stopProfitPrice) && TextUtils.isEmpty(stopLossPrice)) {
                CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_overview_text41))
                return
            }
        }
        var buyPositionAmount = volume
        var sellPositionAmount = volume
        if (isPercentPlaceOrder) {
            if (side == "BUY") {
                volume = CpBigDecimalUtils.mulStr(canOpenBuy, percent, multiplierPrecision)
                buyPositionAmount = volume
                Log.d("我是输入的金额4", volume.toString())
            } else {
                volume = CpBigDecimalUtils.mulStr(canOpenSell, percent, multiplierPrecision)
                sellPositionAmount = volume
                Log.d("我是输入的金额5", volume.toString())
            }

        }
        volume = if (side == "BUY") {
            buyPositionAmount
        } else {
            sellPositionAmount
        }
        Log.d("我是输入的金额3", volume.toString())
        if (mContractUint == 0) {
            volume = if (isOpen) {
                CpBigDecimalUtils.showSNormal(volume, 0)
            } else {
                CpBigDecimalUtils.showSNormalUp(volume, 0)
            }
            Log.d("我是输入的金额2", volume.toString())
        }

        when (buyOrSellHelper.orderType) { //限价单
            1 -> {
                orderType = 1
                if (isRivalPriceModel) {
                    sellMaxPriceList.sortBy {
                        it.optDouble(0)
                    }
                    price = if (side == "BUY") {
                        if (sellMaxPriceList.size > buyOrSellHelper.rivalPricePosition) {
                            sellMaxPriceList[buyOrSellHelper.rivalPricePosition].optDouble(0).toString()
                        } else {
                            CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_overview_text50))
                            return
                        }
                    } else {
                        if (buyMaxPriceList.size > buyOrSellHelper.rivalPricePosition) {
                            buyMaxPriceList[buyOrSellHelper.rivalPricePosition].optDouble(0).toString()
                        } else {
                            CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_overview_text50))
                            return
                        }
                    }
                } else {
                    if (TextUtils.isEmpty(price)) {
                        CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_tip_text1))
                        return
                    }
                }
            } //市价单
            2 -> {
                orderType = 2
                price = CpBigDecimalUtils.median(buyMaxPrice, askMaxPrice, lastPrice)
                isMarketPriceModel = true
                if (isOpen && isPercentPlaceOrder) {
                    val buff = CpBigDecimalUtils.mulStr(canUseAmount, percent, symbolPricePrecision)
                    volume = CpBigDecimalUtils.mulStr(buff, "1", symbolPricePrecision)
                    Log.d("我是输入的金额1", volume.toString())
                }
            }
            3 -> {
                isConditionOrder = true
                if (TextUtils.isEmpty(triggerPrice)) {
                    CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_tip_text2))
                    return
                }
                if (isMarketPriceModel) {
                    orderType = 2 //市价单
                    price = CpBigDecimalUtils.median(buyMaxPrice, askMaxPrice, lastPrice)
                    isMarketPriceModel = true
                } else {
                    orderType = 1
                    if (TextUtils.isEmpty(price)) {
                        CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_tip_text1))
                        return
                    }
                } //                if (isOpen && isPercentPlaceOrder && isMarketPriceModel) {
                //
                //                    val buff = CpBigDecimalUtils.mulStr(canUseAmount, percent, symbolPricePrecision)
                //                    volume = CpBigDecimalUtils.mulStr(buff, "1", symbolPricePrecision)
                //                }
            }
            4 -> {
                orderType = 5
                if (TextUtils.isEmpty(price)) {
                    CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_tip_text1))
                    return
                }
            }
            5 -> {
                orderType = 4
                if (TextUtils.isEmpty(price)) {
                    CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_tip_text1))
                    return
                }
            }
            6 -> {
                orderType = 3
                if (TextUtils.isEmpty(price)) {
                    CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_tip_text1))
                    return
                }
            }
        }
        if (TextUtils.isEmpty(volume)) {
            CpNToastUtil.showTopToastNet(getActivity(), false, context.getString(R.string.cp_extra_text34))
            return
        } //下单限制判断
        val coinResultVo = mContractJson?.optString("coinResultVo")?.let { JSONObject(it) }
        val minOrderVolume = coinResultVo!!.optString("minOrderVolume") //最小下单量
        val minOrderMoney = coinResultVo.optString("minOrderMoney") //最小下单金额
        val maxMarketVolume = coinResultVo.optString("maxMarketVolume") //市价单最大下单数量
        val maxMarketMoney = coinResultVo.optString("maxMarketMoney") //市价最大下单金额
        val maxLimitVolume = coinResultVo.optString("maxLimitVolume") //限价单最大下单数量




        when (buyOrSellHelper.orderType) {
            1, 4, 5, 6 -> { //最小下单量  < x <限价单最大下单数量
                if (CpBigDecimalUtils.orderNumMinCheck(volume, minOrderVolume, multiplier)) {
                    CpNToastUtil.showTopToastNet(getActivity(),
                        false,
                        context.getString(R.string.cp_tip_text5) + minOrderVolume + context.getString(R.string.cp_overview_text9))
                    return
                }
                if (CpBigDecimalUtils.orderNumMaxCheck(volume, maxLimitVolume, multiplier)) {
                    CpNToastUtil.showTopToastNet(getActivity(),
                        false,
                        context.getString(R.string.cp_tip_text6) + maxLimitVolume + context.getString(R.string.cp_overview_text9))
                    return
                }
            }
            2 -> { //                if (isOpen) { //最小下单金额  < x < 市价最大下单金额
                if (CpBigDecimalUtils.orderMoneyMinCheck(volume, minOrderMoney, multiplier)) {
                    CpNToastUtil.showTopToastNet(getActivity(),
                        false,
                        context.getString(R.string.cp_tip_text7) + minOrderMoney + mContractJson?.optString("quote"))
                    return
                }
                if (CpBigDecimalUtils.orderMoneyMaxCheck(volume, maxMarketMoney, multiplier)) {
                    CpNToastUtil.showTopToastNet(getActivity(),
                        false,
                        context.getString(R.string.cp_tip_text8) + maxMarketMoney + mContractJson?.optString("quote"))
                    return
                } //                } else { //最小下单量  < x <市价单最大下单数量
                //                    if (CpBigDecimalUtils.orderNumMinCheck(volume, minOrderVolume, multiplier)) {
                //                        CpNToastUtil.showTopToastNet(getActivity(),
                //                            false,
                //                            context.getString(R.string.cp_tip_text5) + minOrderVolume + context.getString(R.string.cp_overview_text9))
                //                        return
                //                    }
                //                    if (CpBigDecimalUtils.orderNumMaxCheck(volume, maxMarketVolume, multiplier)) {
                //                        CpNToastUtil.showTopToastNet(getActivity(),
                //                            false,
                //                            context.getString(R.string.cp_tip_text6) + maxMarketVolume + context.getString(R.string.cp_overview_text9))
                //                        return
                //                    }
                //                }
            }
            3 -> {
                if (isMarketPriceModel && isOpen) { //最小下单金额  < x < 市价最大下单金额
                    if (CpBigDecimalUtils.orderMoneyMinCheck(volume, minOrderMoney, multiplier)) {
                        CpNToastUtil.showTopToastNet(getActivity(),
                            false,
                            context.getString(R.string.cp_tip_text7) + minOrderMoney + mContractJson?.optString("quote"))
                        return
                    }
                    if (CpBigDecimalUtils.orderMoneyMaxCheck(volume, maxMarketMoney, multiplier)) {
                        CpNToastUtil.showTopToastNet(getActivity(),
                            false,
                            context.getString(R.string.cp_tip_text8) + maxMarketMoney + mContractJson?.optString("quote"))
                        return
                    }
                } else { //最小下单量  < x <限价单最大下单数量
                    if (CpBigDecimalUtils.orderNumMinCheck(volume, minOrderVolume, multiplier)) {
                        CpNToastUtil.showTopToastNet(getActivity(),
                            false,
                            context.getString(R.string.cp_tip_text5) + minOrderVolume + context.getString(R.string.cp_overview_text9))
                        return
                    }
                    if (CpBigDecimalUtils.orderNumMaxCheck(volume, maxLimitVolume, multiplier)) {
                        CpNToastUtil.showTopToastNet(getActivity(),
                            false,
                            context.getString(R.string.cp_tip_text6) + maxLimitVolume + context.getString(R.string.cp_overview_text9))
                        return
                    }
                }
            }
        }
        var expireTime = CpClLogicContractSetting.getStrategyEffectTimeStr(context)
        var volumeData= et_volume.text.toString()
        if ( isPercentPlaceOrder) {
            val buff = CpBigDecimalUtils.mulStr(canUseAmount, percent, symbolPricePrecision)
            volumeData = CpBigDecimalUtils.mulStr(buff, "1", symbolPricePrecision)
        }
        val mCpCreateOrderBean = CpCreateOrderBean(mContractId,
            positionType,
            if (isOpen) "OPEN" else "CLOSE",
            side,
            orderType,
            level,
            if (isMarketPriceModel) "0" else price,
            CpBigDecimalUtils.getOrderNum1(isOpen,
                volumeData,
                multiplier,
                buyOrSellHelper.orderType,
                price,
                if (priceType == 2) symbolPricePrecision else multiplierPrecision,isPercentPlaceOrder),
            isConditionOrder,
            triggerPrice,
            expireTime,
            isStopLoss,
            stopProfitPrice,
            stopLossPrice)
        val titleColor = if (side == "BUY") {
            CpColorUtil.getMainColorType(true)
        } else {
            CpColorUtil.getMainColorType(false)
        }
        if (isOpen && side == "BUY") {
            dialogTitle = context.getString(R.string.cp_overview_text13) //买入开多
        } else if (isOpen && side == "SELL") {
            dialogTitle = context.getString(R.string.cp_overview_text14) //卖出开空
        } else if (!isOpen && side == "BUY") {
            dialogTitle = context.getString(R.string.cp_extra_text4) //买入平空
        } else if (!isOpen && side == "SELL") {
            dialogTitle = context.getString(R.string.cp_extra_text5) //卖出平多
        }
        val contractName = CpClLogicContractSetting.getContractShowNameById(context, mContractId)
        val contractSide = CpClLogicContractSetting.getContractSideById(context, mContractId)

        var showPrice = ""
        var showTriggerPrice = ""

        showPrice = if (isMarketPriceModel) {
            context.getString(R.string.cp_overview_text53)
        } else {
            if (isRivalPriceModel) {
                when (buyOrSellHelper.rivalPricePosition) {
                    0 -> {
                        context.getString(R.string.cp_overview_text38)
                    }
                    4 -> {
                        context.getString(R.string.cp_overview_text39)
                    }
                    else -> {
                        context.getString(R.string.cp_overview_text40)
                    }
                }
            } else {
                "$price $quote"
            }
        }
        showTriggerPrice = "$triggerPrice $quote"
        val showTag = marginModel + level.toString() + "X"
        val tradeConfirm = CpPreferenceManager.getInstance(CpMyApp.instance()).getSharedBoolean(CpPreferenceManager.PREF_TRADE_CONFIRM, true) //二次弹窗确认
        if (tradeConfirm) {
            CpDialogUtil.showCreateOrderDialog(context,
                titleColor,
                dialogTitle,
                contractName,
                showPrice,
                showTriggerPrice,
                tv_buy_cost.text.toString(),
                tv_equivalent.text.toString(),
                buyOrSellHelper.orderType,
                stopProfitPrice,
                stopLossPrice,
                quote.toString(),
                showTag,
                object : CpNewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        LogUtils.e("我是二次弹窗--发起")
                        val event = CpMessageEvent(CpMessageEvent.sl_contract_create_order_event)
                        event.msg_content = mCpCreateOrderBean
                        CpEventBusUtil.post(event)
                    }
                })
        } else {
            val event = CpMessageEvent(CpMessageEvent.sl_contract_create_order_event)
            event.msg_content = mCpCreateOrderBean
            CpEventBusUtil.post(event)

        }
    }


    fun setCurrentOrderListInfo(jsonList: ArrayList<CpCurrentOrderBean>) {
        var entrustedValueBuff = BigDecimal.ZERO
        for (buff in jsonList) {
            var mOrderBalance = buff.orderBalance
            if (TextUtils.isEmpty(mOrderBalance)) {
                mOrderBalance = "0"
            }
            entrustedValueBuff = BigDecimal(mOrderBalance).add(entrustedValueBuff)
        }
        entrustedValue = entrustedValueBuff.setScale(multiplierPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()
    }


    fun setContractJsonInfo(json: JSONObject) {
        mContractJson = json
        mContractJson?.let {
            contractSide = it.optString("contractSide")
            marginRate = it.optString("marginRate")
            marginCoin = it.optString("marginCoin")
            multiplier = it.optString("multiplier")
            mContractId = it.getInt("id")
            val multiplierCoin = it.optString("multiplierCoin")
            base = multiplierCoin //base=BTC quote=USDT
            quote = mContractJson?.optString("quote").toString()
            multiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(context, mContractId)
            marginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionById(context, mContractId)
            symbolPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(context, mContractId)

            val equivalentUnit = if (CpClLogicContractSetting.getIsUSDT(context)) base else quote
            tv_volume_unit.text = if (CpClLogicContractSetting.getIsUSDT(context)) quote else base
            tv_coin_name.text = marginCoin
            tv_equivalent.text = "≈ 0 $equivalentUnit"
            et_price.numberFilter(symbolPricePrecision, otherFilter = object : CpDoListener {
                override fun doThing(obj: Any?): Boolean {
                    updateAvailableVol()
                    return true
                }
            })
            var mPrecision = multiplierPrecision
            if (isMarketPriceModel && transactionType == CpParamConstant.TYPE_BUY) {
                mPrecision = symbolPricePrecision
            }
            et_volume.numberFilter(mPrecision, otherFilter = object : CpDoListener {
                override fun doThing(obj: Any?): Boolean {
                    updateAvailableVol()
                    return true
                }
            })
            this.mUserAssetsInfoJson?.let { it1 -> setUserAssetsInfo(it1) }
        }

    }


    fun setUserConfigInfo(json: JSONObject) {
        btn_sell.setBgColor(CpColorUtil.getMainColorType(false))
        btn_buy.setBgColor(CpColorUtil.getMainColorType(true))
        mUserConfigInfoJson = json
        mUserConfigInfoJson?.let {
            level = it.optInt("nowLevel")
            marginModel =
                if (it.optInt("marginModel") == 1) context.getString(R.string.cp_contract_setting_text1) else context.getString(R.string.cp_contract_setting_text2)
            positionType = it.optString("positionModel")
            buyOrSellHelper.isOneWayPosition = (positionType != "2")
            val coUnit = it.optInt("coUnit") //合约单位 1标的货币, 2张
            CpClLogicContractSetting.setContractUint(context, if (coUnit == 1) 1 else 0)
            if (!it.isNull("leverOriginCeiling")) {
                val leverOriginCeilingObj = it.optJSONObject("leverOriginCeiling")
                val iteratorKeys = leverOriginCeilingObj?.keys()
                val leverOriginCeilingArr = ArrayList<Int>()
                if (iteratorKeys != null) {
                    while (iteratorKeys.hasNext()) {
                        val key = iteratorKeys.next().toInt()
                        leverOriginCeilingArr.add(key)
                    }
                }
                leverOriginCeilingArr.sort()
                for (buff in leverOriginCeilingArr) {
                    if (level <= buff) {
                        maxOpenLimit = leverOriginCeilingObj?.optString(buff.toString()) ?: "0"
                        break
                    }
                }
            }
        }
        if (positionType == "1") {
            transactionType = if (!cb_only_reduce_positions.isChecked) CpParamConstant.TYPE_BUY else CpParamConstant.TYPE_SELL
            changeBuyOrSellUI()
            if (isFrist) {
                val mCpMessageEvent = CpMessageEvent(CpMessageEvent.sl_contract_modify_depth_event)
                mCpMessageEvent.msg_content = buyOrSellHelper
                CpEventBusUtil.post(mCpMessageEvent)
            }
            isFrist = false
        }
        mContractUint = CpClLogicContractSetting.getContractUint(context)
        mContractJson?.let { setContractJsonInfo(it) }
    }
    var isFrist = true

    fun setUserAssetsInfo(json: JSONObject) {
        canCloseVolumeSell = "0"
        canCloseVolumeBuy = "0"
        positionValue = "0"
        canUseAmount = "0"
        mUserAssetsInfoJson = json
        mUserAssetsInfoJson?.apply {
            canUseAmount = optString("canUseAmount")
            if (!isNull("positionList")) {
                val mOrderListJson = optJSONArray("positionList")
                var positionValueBuff = BigDecimal.ZERO
                if (mOrderListJson != null) {
                    for (i in 0 until mOrderListJson.length()) {
                        val obj = mOrderListJson.getJSONObject(i)
                        if (obj.getInt("contractId") == mContractId) {
                            var canCloseVolume = obj.getString("canCloseVolume")
                            var orderSid = obj.getString("orderSide")
                            if (orderSid.equals("BUY")) {
                                canCloseVolumeSell = canCloseVolume
                            } else if (orderSid.equals("SELL")) {
                                canCloseVolumeBuy = canCloseVolume
                            }
                            positionValueBuff = BigDecimal(obj.optString("positionBalance")).add(positionValueBuff)
                        }
                    }
                }
                positionValue = positionValueBuff.setScale(multiplierPrecision, BigDecimal.ROUND_HALF_DOWN).toPlainString()
            }
            if (!isNull("accountList")) {
                val mOrderListJson = optJSONArray("accountList")
                if (mOrderListJson != null) {
                    for (i in 0 until mOrderListJson.length()) {
                        val obj = mOrderListJson.getJSONObject(i)
                        if (mContractJson?.optString("marginCoin").toString() == obj.optString("symbol")) {
                            canUseAmount = obj.getString("canUseAmount")
                            canUseAmount = CpBigDecimalUtils.scaleStr(canUseAmount, 3)
                        }
                    }
                }
            }
            updateAvailableVol()
        }

    }

    private fun adjustRatio(radio: String) {
        percent = radio //计算可开多成本
        updateAvailableVol()
    }

    @SuppressLint("SetTextI18n")
    private fun updateAvailableVol() {
        var isOpen = false;
        isOpen = transactionType == CpParamConstant.TYPE_BUY
        if (positionType == "1") {
            isOpen = !cb_only_reduce_positions.isChecked
        }
        tv_equivalent.visibility = if (isOpen) View.VISIBLE else View.INVISIBLE

        et_volume.hint = context.getString(R.string.cp_overview_text8)

        price = et_price.text.toString()
        triggerPrice = et_trigger_price.text.toString()
        val triggerPrice = et_trigger_price.text.toString()
        if (TextUtils.isEmpty(price)) {
            price = "0"
        }
        var buyPrice = price
        var sellPrice = price

        if (isRivalPriceModel) {
            sellMaxPriceList.sortBy {
                it.optDouble(0)
            }
            if (sellMaxPriceList.size > buyOrSellHelper.rivalPricePosition) {
                buyPrice = sellMaxPriceList[buyOrSellHelper.rivalPricePosition].optDouble(0).toString()
            }
            if (buyMaxPriceList.size > buyOrSellHelper.rivalPricePosition) {
                sellPrice = buyMaxPriceList[buyOrSellHelper.rivalPricePosition].optDouble(0).toString()
            }
        }

        var positionAmount = et_volume.text.toString()
        var buyPositionAmount = positionAmount
        var sellPositionAmount = positionAmount
        if (TextUtils.isEmpty(positionAmount)) {
            positionAmount = "0"
            tv_buy_cost.text = "0.00"
            tv_sell_cost.text = "0.00"
        }
        if (isPercentPlaceOrder) {
            positionAmount = if (CpClLogicContractSetting.getIsUSDT(context)) {
                CpBigDecimalUtils.mulStr(canOpenBuy, percent, multiplierPrecision)
            } else {
                CpBigDecimalUtils.divStr(CpBigDecimalUtils.mulStr(canOpenBuy, percent, multiplierPrecision), price, multiplierPrecision)
            }
            buyPositionAmount = positionAmount
            sellPositionAmount = positionAmount
        }
        when (buyOrSellHelper.orderType) {
            1, 4, 5, 6 -> {

            }
            2 -> {
                if (isOpen) {
                    price = CpBigDecimalUtils.median(buyMaxPrice, askMaxPrice, lastPrice)
                    buyPrice = price
                    sellPrice = price
                }
//                if (isOpen && isPercentPlaceOrder) {
//                    val buff = CpBigDecimalUtils.mulStr(canUseAmount,
//                        percent,
//                        multiplierPrecision) //                    positionAmount = CpBigDecimalUtils.mulStr(buff, level.toString(), multiplierPrecision)
//                    positionAmount = CpBigDecimalUtils.mulStr(buff, "1", multiplierPrecision)
//                    buyPositionAmount = positionAmount
//                    sellPositionAmount = positionAmount
//                }
            }
            3 -> {
                if (isOpen) {
                    if (isMarketPriceModel) {
                        price = triggerPrice
                    }
                    buyPrice = price
                    sellPrice = price
                }
//                if (isOpen && isPercentPlaceOrder && isMarketPriceModel) {
//                    val buff = CpBigDecimalUtils.mulStr(canUseAmount,
//                        percent,
//                        multiplierPrecision) //                    positionAmount = CpBigDecimalUtils.mulStr(buff, level.toString(), multiplierPrecision)
//                    positionAmount = CpBigDecimalUtils.mulStr(buff, "1", multiplierPrecision)
//                    buyPositionAmount = positionAmount
//                    sellPositionAmount = positionAmount
//                }
            }
        }
        tv_equivalent.text = "≈" + CpBigDecimalUtils.canPositionMarketStr(contractSide == "1",
            multiplier,
            positionAmount,
            price,
            multiplierPrecision,
            mContractJson?.optString("multiplierCoin"))

        //
        //        if (isOpen && buyOrSellHelper.orderType == 2) {
        //
        //            tv_equivalent.text = "≈" + CpBigDecimalUtils.canPositionMarketStr(
        //                contractSide == "1",
        //                multiplier,
        //                positionAmount,
        //                price,
        //                multiplierPrecision,
        //                unit
        //            )
        //
        //
        //        }
        //        if (isOpen && buyOrSellHelper.orderType == 3 && isMarketPriceModel) {
        //            tv_equivalent.text = "≈" + CpBigDecimalUtils.canPositionMarketStr(
        //                contractSide == "1",
        //                multiplier,
        //                positionAmount,
        //                price,
        //                multiplierPrecision,
        //                unit
        //            )
        //
        //        }

        //        //通过保证金计算的可开数
        //        val buybuff1 = CpBigDecimalUtils.canBuyStr(
        //            isOpen,
        //            buyOrSellHelper.orderType == 2,
        //            contractSide == "1",
        //            buyPrice,
        //            multiplier,
        //            canUseAmount,
        //            canCloseVolumeBuy,
        //            level.toString(),
        //            marginRate,
        //            multiplierPrecision,
        //            base
        //        )
        //        //通过保证金计算的可开数
        //        val sellbuff1 = CpBigDecimalUtils.canBuyStr(
        //            isOpen,
        //            buyOrSellHelper.orderType == 2,
        //            contractSide == "1",
        //            sellPrice,
        //            multiplier,
        //            canUseAmount,
        //            canCloseVolumeSell,
        //            level.toString(),
        //            marginRate,
        //            multiplierPrecision,
        //            base
        //        )

        //        //通过风险限额计算的可开数
        //        val buybuff2 = CpBigDecimalUtils.canOpenStr(
        //            contractSide == "1",
        //            buyOrSellHelper.orderType == 2,
        //            buyPrice,
        //            maxOpenLimit,
        //            positionValue,
        //            entrustedValue,
        //            multiplier,
        //            marginRate,
        //            multiplierPrecision,
        //            base
        //        )
        //        //通过风险限额计算的可开数
        //        val sellbuff2 = CpBigDecimalUtils.canOpenStr(
        //            contractSide == "1",
        //            buyOrSellHelper.orderType == 2,
        //            sellPrice,
        //            maxOpenLimit,
        //            positionValue,
        //            entrustedValue,
        //            multiplier,
        //            marginRate,
        //            multiplierPrecision,
        //            base
        //        )

        //计算预估成本价
        val buyCostbuff1 =
            CpBigDecimalUtils.canCostStr(buyPrice, buyPositionAmount, multiplier, level.toString(), marginRate, marginCoinPrecision, marginCoin)

        //计算预估成本价
        val sellCostbuff1 =
            CpBigDecimalUtils.canCostStr(sellPrice, sellPositionAmount, multiplier, level.toString(), marginRate, marginCoinPrecision, marginCoin)

        tv_buy_cost.text = buyCostbuff1
        tv_sell_cost.text = sellCostbuff1

        //        canOpenBuy = CpBigDecimalUtils.min(buybuff1.split(" ")[0], buybuff2.split(" ")[0])
        //        canOpenSell = CpBigDecimalUtils.min(sellbuff1.split(" ")[0], sellbuff2.split(" ")[0])
        canOpenBuy = canUseAmount
        canOpenSell = canUseAmount //        if (!isOpen) {
        //            canOpenBuy = buybuff1.split(" ")[0]
        //            canOpenSell = sellbuff1.split(" ")[0]
        //        }
        tv_long_value.text = "$canOpenBuy $quote"
        tv_short_value.text = "$canOpenSell $quote"


        val llLongTitle = ll_long_title.layoutParams as LayoutParams
        val llShortTitle = ll_short_title.layoutParams as LayoutParams

        buyOrSellHelper.isOpen = isOpen
        buyOrSellHelper.isOto = cb_stop_loss.isChecked //根据订单类型来改变间距
        if (positionType == "2") { //双向持仓
            when (buyOrSellHelper.orderType) {
                1, 4, 5, 6 -> {
                    if (isOpen) {
                        if (cb_stop_loss.isChecked) {
                            llLongTitle.topMargin = SizeUtils.dp2px(12f)
                            llShortTitle.topMargin = SizeUtils.dp2px(24f)
                        } else {
                            llLongTitle.topMargin = SizeUtils.dp2px(12f)
                            llShortTitle.topMargin = SizeUtils.dp2px(16f)
                        }
                    } else {
                        llLongTitle.topMargin = SizeUtils.dp2px(0f)
                        llShortTitle.topMargin = SizeUtils.dp2px(14f)
                    }
                }
                2 -> {
                    if (isOpen) {
                        if (cb_stop_loss.isChecked) {
                            llLongTitle.topMargin = SizeUtils.dp2px(12f)
                            llShortTitle.topMargin = SizeUtils.dp2px(24f)
                        } else {
                            llLongTitle.topMargin = SizeUtils.dp2px(12f)
                            llShortTitle.topMargin = SizeUtils.dp2px(16f)
                        }
                    } else {
                        llLongTitle.topMargin = SizeUtils.dp2px(0f)
                        llShortTitle.topMargin = SizeUtils.dp2px(14f)
                    }
                }
                3 -> {
                    if (isOpen) {
                        llLongTitle.topMargin = SizeUtils.dp2px(10f)
                        llShortTitle.topMargin = SizeUtils.dp2px(24f)
                    } else {
                        llLongTitle.topMargin = SizeUtils.dp2px(6f)
                        llShortTitle.topMargin = SizeUtils.dp2px(14f)
                    }
                }
            }
        } else { //单向持仓
            when (buyOrSellHelper.orderType) {
                1, 4, 5, 6 -> {
                    if (isOpen) {
                        if (cb_stop_loss.isChecked) {
                            llLongTitle.topMargin = SizeUtils.dp2px(16f)
                            llShortTitle.topMargin = SizeUtils.dp2px(10f)
                        } else {
                            llLongTitle.topMargin = SizeUtils.dp2px(10f)
                            llShortTitle.topMargin = SizeUtils.dp2px(10f)
                        }
                    } else {
                        llLongTitle.topMargin = SizeUtils.dp2px(29f)
                        llShortTitle.topMargin = SizeUtils.dp2px(30f)
                    }
                }
                2 -> {
                    if (isOpen) {
                        if (cb_stop_loss.isChecked) {
                            llLongTitle.topMargin = SizeUtils.dp2px(16f)
                            llShortTitle.topMargin = SizeUtils.dp2px(10f)
                        } else {
                            llLongTitle.topMargin = SizeUtils.dp2px(10f)
                            llShortTitle.topMargin = SizeUtils.dp2px(10f)
                        }
                    } else {
                        llLongTitle.topMargin = SizeUtils.dp2px(30f)
                        llShortTitle.topMargin = SizeUtils.dp2px(28f)
                    }
                }
                3 -> {
                    if (isOpen) {
                        llLongTitle.topMargin = SizeUtils.dp2px(6f)
                        llShortTitle.topMargin = SizeUtils.dp2px(19f)
                    } else {
                        llLongTitle.topMargin = SizeUtils.dp2px(0f)
                        llShortTitle.topMargin = SizeUtils.dp2px(12f)
                    }
                }
            }
        }
    }

    private fun updataMarketPriceUI() {
        tv_price_hint?.setBackgroundResource(if (!isMarketPriceModel) R.drawable.cp_bg_trade_et_unfocused else R.drawable.cp_bg_trade_et_focused)
        tv_price_hint?.setTextColor(if (!isMarketPriceModel) this.resources.getColor(R.color.normal_text_color) else this.resources.getColor(R.color.main_blue))
        ll_price.visibility = if (!isMarketPriceModel) View.VISIBLE else View.GONE
        tv_order_tips_layout_plan.visibility = if (isMarketPriceModel) View.VISIBLE else View.GONE
    }


    private fun updataRivalPriceUI() {
        ll_price.visibility = if (!isRivalPriceModel) View.VISIBLE else View.GONE
        tv_rival_price_type.visibility = if (isRivalPriceModel) View.VISIBLE else View.GONE
        tv_rival_price_hint?.setBackgroundResource(if (!isRivalPriceModel) R.drawable.cp_bg_trade_et_unfocused else R.drawable.cp_bg_trade_et_focused)
        tv_rival_price_hint?.setTextColor(if (!isRivalPriceModel) this.resources.getColor(R.color.normal_text_color) else this.resources.getColor(R.color.main_blue))
    }

    private fun changeBuyOrSellUI() {
        when (this.transactionType) { // 买
            CpParamConstant.TYPE_BUY -> {
                rb_buy?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_select_text_color))
                    backgroundResource = R.drawable.coins_exchange_buy_blue
                }

                rb_sell?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_normal_text_color))
                    backgroundResource = R.drawable.coins_exchange_sell_grey
                }

                ll_buy_cost.visibility = View.VISIBLE
                ll_sell_cost.visibility = View.VISIBLE
                if (buyOrSellHelper.orderType != 3) {
                    ll_stop_loss.visibility = View.VISIBLE
                } else {
                    ll_stop_loss.visibility = View.INVISIBLE
                    cb_stop_loss.isChecked = false
                }

                btn_sell.textContent = "<font> ${context.getString(R.string.cp_overview_text14)} </font>"
                btn_buy.textContent = "<font> ${context.getString(R.string.cp_overview_text13)} </font>"
            } // 卖
            CpParamConstant.TYPE_SELL -> {
                rb_buy?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_normal_text_color))
                    backgroundResource = R.drawable.coins_exchange_buy_grey
                }

                rb_sell?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_select_text_color))
                    backgroundResource = R.drawable.coins_exchange_sell_blue
                }

                ll_buy_cost.visibility = View.GONE
                ll_sell_cost.visibility = View.GONE
                ll_stop_loss.visibility = View.INVISIBLE
                cb_stop_loss.isChecked = false
                btn_sell.textContent = "<font> ${context.getString(R.string.cp_extra_text5)} </font>"
                btn_buy.textContent = "<font> ${context.getString(R.string.cp_extra_text4)} </font>"
            }
        }
        updateAvailableVol()
    }


    @SuppressLint("SetTextI18n")
    fun changePriceType(item: Int) {
        CpClLogicContractSetting.setExecution(CpMyApp.instance(), 0)
        et_price.setText("")
        et_volume.setText("")
        tv_buy_cost.text = "0.00"
        tv_sell_cost.text = "0.00"
        et_trigger_price.setText("")
        buyOrSellHelper.orderType = item
        tv_order_tips_layout.visibility = View.GONE
        ll_trigger_price.visibility = View.GONE
        tv_price_hint.visibility = View.GONE
        tv_rival_price_hint.visibility = View.GONE
        tv_order_tips_layout_plan.visibility = View.GONE
        ll_all_price.visibility = View.VISIBLE
        isRivalPriceModel = false
        isMarketPriceModel = false
        isPercentPlaceOrder = false
        rg_trade.clearCheck()
        updataMarketPriceUI()
        changeBuyOrSellUI()
        updataRivalPriceUI()
        val mCpMessageEvent = CpMessageEvent(CpMessageEvent.sl_contract_modify_depth_event)
        mCpMessageEvent.msg_content = buyOrSellHelper
        CpEventBusUtil.post(mCpMessageEvent)
        when (item) {
            1 -> { //限价单
                ll_price.visibility = View.VISIBLE
                tv_rival_price_hint.visibility = View.VISIBLE
            }
            2 -> { //市价单
                ll_price.visibility = View.GONE
                ll_all_price.visibility = View.GONE
                tv_order_tips_layout.visibility = View.VISIBLE
                CpClLogicContractSetting.setExecution(CpMyApp.instance(), 1)
            }
            3 -> { //条件单
                ll_price.visibility = View.VISIBLE
                ll_trigger_price.visibility = View.VISIBLE
                tv_price_hint.visibility = View.VISIBLE
            }
            4 -> { //PostOnly
                ll_price.visibility = View.VISIBLE
            }
            5 -> { //IOC
                ll_price.visibility = View.VISIBLE
            }
            6 -> { //FOK
                ll_price.visibility = View.VISIBLE
            }
        }


    }

    fun getActivity(): Activity? {
        if (context is Activity) {
            return context as Activity
        }
        return null
    }


    private fun kycTips(s: String) {
        CpNewDialogUtils.showDialog(context!!,
            s,
            true,
            null,
            context.getString(R.string.cp_extra_text27),
            context.getString(R.string.cp_overview_text56))
    }

    private fun goKycTips(s: String) {
        CpNewDialogUtils.showDialog(context!!, s.replace("\n", "<br/>"), false, object : CpNewDialogUtils.DialogBottomListener {
            override fun sendConfirm() {
                CpEventBusUtil.post(CpMessageEvent(CpMessageEvent.sl_contract_go_kyc_page))
            }
        }, context.getString(R.string.cl_kyc_1), context.getString(R.string.cl_kyc_6), context.getString(R.string.cp_overview_text56))
    }

}




