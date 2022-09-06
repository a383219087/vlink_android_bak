package com.yjkj.chainup.new_version.view.depth

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding2.view.RxView
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.ParamConstant.*
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.model.model.MainModel
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.ui.NewMainActivity
import com.yjkj.chainup.new_version.activity.leverage.TradeFragment
import com.yjkj.chainup.new_version.dialog.DialogUtil
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.depth_vertical_layout.view.*
import kotlinx.android.synthetic.main.trade_amount_view_new.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.view
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.math.pow

/**
 * @Author: Bertking
 * @Date：2019/3/7-5:43 PM
 * @Description: 交易量的View
 */
class TradeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val TAG = "我是交易量的View"



    //交易类型
    var transactionType = TYPE_BUY
    var isLever = false
    var isETF = false
    var price = ""

    //价格类型
    var priceType = 0

    //可用余额
    var canUseMoney: String = "0"
    var inputPrice: String = ""
    var inputQuantity: String = ""

    var priceScale = 2

    var volumeScale = 2
    var etfInfo: JSONObject? = null


    var disposable: CompositeDisposable? = null
    var mainModel: MainModel? = null

    var dialog: TDialog? = null
    var coinMapData: JSONObject? = NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol)
        set(value) {
            field = value
            synchronized(this) {
                priceScale = value?.optInt("price", 2) ?: 2
                volumeScale = value?.optInt("volume", 2) ?: 2
            }

            tv_coin_name?.text = NCoinManager.getMarketCoinName(showAnoterName(value))


            getAvailableBalance()

            /**
             * 设置 RadioButton 的选中效果
             */
            for (i in 0 until rg_trade.childCount step 2) {
                val radioButton = rg_trade?.getChildAt(i) as RadioButton
                radioButton.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                radioButton.backgroundResource = R.color.transparent
            }
            et_volume?.setText("")
            if (!StringUtil.checkStr(et_price?.text.toString())) {
                tv_convert_price?.text = "--"
            }

            tv_transaction_money?.text = "--"
        }


    fun setPrice() {

        synchronized(this) {
            priceScale = coinMapData?.optInt("price", 2) ?: 2
            volumeScale = coinMapData?.optInt("volume", 2) ?: 2
        }


        et_price?.filters = arrayOf(DecimalDigitsInputFilter(priceScale))
        if (transactionType == TYPE_BUY && priceType == ParamConstant.TYPE_MARKET) {
            et_volume?.filters = arrayOf(DecimalDigitsInputFilter(priceScale))
        } else {
            et_volume?.filters = arrayOf(DecimalDigitsInputFilter(volumeScale))
        }
    }

    private fun showAnoterName(jsonObject: JSONObject?): String {
        return NCoinManager.showAnoterName(jsonObject)
    }

    /**
     * 获取可用余额
     */

    fun getAvailableBalance() {

        if (!LoginManager.checkLogin(context, false)) {
            /**
             * 可用余额
             */
            tv_available_balance?.text = "--"
            return
        }


    }

    fun setTextContent() {
        tv_order_type?.textContent = LanguageUtil.getString(context, "contract_action_limitPrice")
        tv_transaction_text?.text = LanguageUtil.getString(context, "transaction_text_tradeSum")
        et_price?.hint = LanguageUtil.getString(context, "contract_text_price")

    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ComVerifyView, 0, 0)
            typedArray.recycle()
        }
        /**
         * 这里的必须为：True
         */
        LayoutInflater.from(context).inflate(R.layout.trade_amount_view_new, this, true)

        setTextContent()


        /**
         * 点击 买
         */
        rb_buy?.setOnClickListener {
            transactionType = TYPE_BUY
            buyOrSell(transactionType, isLever)
        }

        /**
         * 点击卖
         */
        rb_sell?.setOnClickListener {
            transactionType = TYPE_SELL
            buyOrSell(transactionType, isLever)
        }
        observeData()

        NLiveDataUtil.observeData(this.context as NewMainActivity, Observer {
            if (it == null) return@Observer
            if (MessageEvent.login_operation_type == it.msg_type) {
                operator4PriceVolume(context)
            }
        })

        tv_order_type?.view()?.let {
            RxView.clicks(it)
                .throttleFirst(500L, TimeUnit.MILLISECONDS) // 1秒内只有第一次点击有效
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { x ->
                    DialogUtil.createCVCOrderPop(
                        context,
                        priceType,
                        it,
                        object : NewDialogUtils.DialogOnSigningItemClickListener {
                            override fun clickItem(position: Int, text: String) {
                                tv_order_type?.textContent = text
                                changePriceType(position)
                            }
                        })
                }
        }
        getAvailableBalance()
        operator4Price(context)
        /**
         * 交易额
         */
        if (priceType == TYPE_MARKET) {
            ll_transaction?.visibility = View.INVISIBLE
            tv_convert_price?.visibility = View.INVISIBLE

        } else {
            ll_transaction?.visibility = View.VISIBLE
            tv_transaction_money?.visibility = View.VISIBLE
            tv_transaction_money?.text = "--"
            tv_convert_price?.visibility = View.VISIBLE
        }


        /**
         * 交易量百分比
         * TODO 代码优化
         */
        rg_trade?.setOnCheckedChangeListener { group, checkedId ->

            /**
             * 设置 RadioButton 的选中效果
             */
            for (i in 0 until rg_trade.childCount step 2) {
                val radioButton = rg_trade?.getChildAt(i) as RadioButton
                radioButton.setTextColor(ColorUtil.getCheck4ColorStateList(isBuy()))
                radioButton.background = ColorUtil.getCheck4StateListDrawable(isBuy())
            }

            if (checkedId > -1) {
                if (!LoginManager.checkLogin(context, true)) {
                    group.clearCheck()
                    return@setOnCheckedChangeListener
                }
            }

            when (checkedId) {
                R.id.rb_1st -> {
                    adjustRatio("0.25")
                }

                R.id.rb_2nd -> {
                    adjustRatio("0.50")
                }

                R.id.rb_3rd -> {
                    adjustRatio("0.75")
                }

                R.id.rb_4th -> {
                    adjustRatio("1.0")

                }
                else -> {
                    adjustRatio("0.25")
                }
            }
        }

        operator4PriceVolume(context)

        addTextListener()

        cbtn_create_order?.isEnable(true)
        cbtn_create_order?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (!LoginManager.checkLogin(context, true)) return

                var status =
                    if (TradeFragment.currentIndex == LEVER_INDEX_TAB) PublicInfoDataService.getInstance().leverTradeKycOpen else PublicInfoDataService.getInstance().exchangeTradeKycOpen

                if (status && UserDataService.getInstance().authLevel != 1) {
                    NewDialogUtils.KycSecurityDialog(context!!, context?.getString(R.string.common_kyc_trading)
                        ?: "", object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    NToastUtil.showTopToastNet(
                                        getActivity(),
                                        false,
                                        context?.getString(R.string.noun_login_pending)
                                    )
                                }

                                2, 3 -> {
                                    ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                        }
                    })
                    return
                }

                LogUtil.e(TAG, "isETF ${isETF}")
                if (isETF) {
                    if (!UserDataService.getInstance().userIsOpenEtf) {
                        tradeETF()
                        return
                    }
                }
                /**
                 * 限价交易
                 */
                if (priceType == TYPE_LIMIT) {
                    if (TextUtils.isEmpty(inputPrice)) {
                        NToastUtil.showTopToastNet(
                            getActivity(),
                            false,
                            LanguageUtil.getString(context, "contract_tip_pleaseInputPrice")
                        )
                        return
                    }

                    if (TextUtils.isEmpty(inputQuantity)) {
                        NToastUtil.showTopToastNet(
                            getActivity(),
                            false,
                            LanguageUtil.getString(context, "transfer_tip_emptyVolume")
                        )
                        return
                    }


                    val limitPriceMin = coinMapData?.optString("limitPriceMin")
                    if (BigDecimalUtils.compareTo(inputPrice, limitPriceMin) < 0) {
                        val msg =
                            LanguageUtil.getString(context, "common_tip_limitMinTransactionPrice") + BigDecimalUtils.showSNormal(
                                limitPriceMin
                            )
                        NToastUtil.showTopToastNet(getActivity(), false, msg)
                        return
                    }

                    val limitVolumeMin = coinMapData?.optString("limitVolumeMin")
                    if (BigDecimalUtils.compareTo(inputQuantity, limitVolumeMin) < 0) {
                        val msg =
                            LanguageUtil.getString(context, "common_tip_limitMaxTransactionVolume") + BigDecimalUtils.showSNormal(
                                limitVolumeMin
                            )
                        NToastUtil.showTopToastNet(getActivity(), false, msg)
                        return
                    }

                    if (transactionType == TYPE_SELL) {
                        if (BigDecimalUtils.compareTo(canUseMoney, inputQuantity) < 0) {
                            // DisplayUtil.showSnackBar(this@TradeView.rootView, LanguageUtil.getString(context,R.string.common_tip_balanceNotEnough), isSuc = false)
                            NToastUtil.showTopToastNet(
                                getActivity(),
                                false,
                                LanguageUtil.getString(context, "common_tip_balanceNotEnough")
                            )
                            return
                        }
                    } else {

                    }
                }

                /**
                 * 现价交易
                 */
                if (priceType == TYPE_MARKET) {
                    if (TextUtils.isEmpty(inputQuantity)) {
                        NToastUtil.showTopToastNet(
                            getActivity(),
                            false,
                            LanguageUtil.getString(context, "transfer_tip_emptyVolume")
                        )
                        return
                    }
                    val marketBuyMin = coinMapData?.optString("marketBuyMin")
                    val marketSellMin = coinMapData?.optString("marketSellMin")

                    /**
                     * 市价交易
                     * 在市价交易的前提下，无论买入or卖出，使用的是et_volume,So the context is inputQuantity
                     */
                    if (transactionType == TYPE_BUY) {

                        /**
                         * 最小价格
                         */
                        if (BigDecimalUtils.compareTo(inputQuantity, marketBuyMin) < 0) {
                            NToastUtil.showTopToastNet(
                                getActivity(),
                                false,
                                LanguageUtil.getString(
                                    context,
                                    "common_tip_limitMinTransactionPrice"
                                ) + BigDecimalUtils.showSNormal(marketBuyMin)
                            )
                            return
                        }

                        if (BigDecimalUtils.compareTo(canUseMoney, inputQuantity) < 0) {
                            NToastUtil.showTopToastNet(
                                getActivity(),
                                false,
                                LanguageUtil.getString(context, "common_tip_balanceNotEnough")
                            )
                            return
                        }

                    } else {
                        /**
                         * 最小交易量
                         */
                        if (BigDecimalUtils.compareTo(inputQuantity, marketSellMin) < 0) {
                            NToastUtil.showTopToastNet(
                                getActivity(),
                                false,
                                LanguageUtil.getString(
                                    context,
                                    "common_tip_limitMaxTransactionVolume"
                                ) + BigDecimalUtils.showSNormal(marketSellMin)
                            )
                            return
                        }

                        if (BigDecimalUtils.compareTo(canUseMoney, inputQuantity) < 0) {
                            NToastUtil.showTopToastNet(
                                getActivity(),
                                false,
                                LanguageUtil.getString(context, "common_tip_balanceNotEnough")
                            )
                            return
                        }

                    }
                }

                createOrder()
            }
        }
        img_transfer?.setOnClickListener {
            if (!LoginManager.checkLogin(context, true)) {
                return@setOnClickListener
            }
            if (isLever) {
                ArouterUtil.navigation(RoutePath.NewVersionTransferActivity, Bundle().apply {
                    putString(TRANSFERSTATUS, ParamConstant.LEVER_INDEX)
                    putString(TRANSFERCURRENCY, getCurrentSymbol())
                })
            } else {
                ArouterUtil.forwardTransfer(TRANSFER_BIBI, getCurrentCoin())
            }
        }
    }

    /**
     * 处理price,volume的事件&登录状态的关系
     */
    private fun operator4PriceVolume(context: Context) {
        priceScale = coinMapData?.optInt("price", 2) ?: 2
        volumeScale = coinMapData?.optInt("volume", 2) ?: 2
        setPrice()
        if (!LoginManager.isLogin(context)) {
            tv_transaction_money?.text = "--"
            et_price?.isFocusableInTouchMode = false
            et_volume?.isFocusableInTouchMode = false
        } else {
            if (et_volume?.isFocusableInTouchMode?.not() == true) {
                et_volume?.isFocusable = true
                et_volume?.isFocusableInTouchMode = true
                et_volume?.requestFocus()
                et_volume?.findFocus()
            }
            if (et_price?.isFocusableInTouchMode?.not() == true) {
                et_price?.isFocusable = true
                et_price?.isFocusableInTouchMode = true
                et_price?.requestFocus()
                et_price?.findFocus()
            }
        }

        et_price?.setOnClickListener {
            LoginManager.checkLogin(context, true)
        }
        et_volume?.setOnClickListener {
            LoginManager.checkLogin(context, true)
        }


        /**
         * 「价格」输入框的背景变换
         */
        et_price?.setOnFocusChangeListener { _, hasFocus ->
            ll_price?.setBackgroundResource(if (hasFocus) ColorUtil.getMainFocusColorType(isBuy()) else R.drawable.bg_trade_et_unfocused)
        }

        /**
         * 「交易量」输入框的背景变换
         */
        et_volume?.setOnFocusChangeListener { _, hasFocus ->
            ll_volume?.setBackgroundResource(if (hasFocus) ColorUtil.getMainFocusColorType(isBuy()) else R.drawable.bg_trade_et_unfocused)
        }

        if (priceType == TYPE_MARKET) {
            ll_transaction?.visibility = View.INVISIBLE
            tv_convert_price?.visibility = View.INVISIBLE
        } else {
            ll_transaction?.visibility = View.VISIBLE
            tv_convert_price?.visibility = View.VISIBLE
        }
    }

    /**
     * 调节可用余额的比例
     */
    private fun adjustRatio(radio: String) {
        if (TextUtils.isEmpty(canUseMoney)) return
        when (priceType) {
            /**
             * 限价
             */
            TYPE_LIMIT -> {
                val price = et_price?.text.toString()
                if (transactionType == TYPE_BUY) {
                    val consume = BigDecimalUtils.mul(canUseMoney, radio, priceScale).toString()
                    if (!TextUtils.isEmpty(price)) {
                        val volume = BigDecimalUtils.div(consume, price, volumeScale).toPlainString()
                        et_volume?.setText(volume)
                    }
                    if (TextUtils.isEmpty(inputPrice) || inputPrice == "0") {
                        tv_transaction_money?.text = "--"
                    } else {
                        tv_transaction_money?.text = "${BigDecimalUtils.showSNormal(consume) + showMarket()}"
                    }
                } else {
                    val volume = BigDecimalUtils.mul(canUseMoney, radio, volumeScale).toPlainString()
                    et_volume?.setText(volume)
                    val consume = if (TextUtils.isEmpty(price)) {
                        BigDecimalUtils.mul(volume, "0", priceScale).toString()
                    } else {
                        BigDecimalUtils.mul(volume, price, priceScale).toString()
                    }

                    tv_transaction_money?.text = "${BigDecimalUtils.showSNormal(consume) + showMarket()}"
                }
            }

            /**
             * 市价
             */
            TYPE_MARKET -> {
                if (transactionType == TYPE_BUY) {
                    val consume = BigDecimalUtils.mul(canUseMoney, radio, priceScale).toPlainString()
                    et_volume?.setText(consume)
                    tv_transaction_money?.text = "${BigDecimalUtils.showSNormal(consume) + showMarket()}"
                } else {
                    val volume = BigDecimalUtils.mul(canUseMoney, radio, volumeScale).toPlainString()
                    et_volume?.setText(volume)
                    tv_transaction_money?.text = "${BigDecimalUtils.showSNormal(volume) + showCoinName()}"

                }
            }

        }


    }

    /**
     * 价格操作(加 + 减 -)
     */
    private fun operator4Price(context: Context) {

        /**
         * 价格 （-）
         */
        tv_sub?.setOnClickListener {priceSub() }
        iv_sub?.setOnClickListener {priceSub() }
        /**
         * 价格 （+）
         */
        tv_add?.setOnClickListener {priceAdd() }
        iv_add?.setOnClickListener {priceAdd() }
    }
    private fun priceAdd() {
        if (Utils.isFastClick(300)){
            return
        }
        if (!LoginManager.checkLogin(context, true)) return
        val scale = if (transactionType == TYPE_SELL && priceType == TYPE_MARKET) {
            volumeScale
        } else {
            priceScale
        }
        val unit = (1 / 10.0.pow(scale.toDouble())).toString()
        Log.d(TAG, "=======price:加的单位量unit:$unit===")
        if (TextUtils.isEmpty(unit)) return
        if (BigDecimal(inputPrice).toFloat() > 0f) {
            inputPrice =
                BigDecimalUtils.divForDown(BigDecimalUtils.add(inputPrice, unit).toPlainString(), scale).toPlainString()
            //这边必须要这样不然字段会改变
            val inputPrice1= inputPrice
            if (inputPrice1.isNotEmpty()){
                et_price?.setText(BigDecimalUtils.subAndDot(inputPrice1))
                inputPrice=inputPrice1
                Log.d("我是入口","${inputPrice}==3")
                tv_convert_price?.text = RateManager.getCNYByCoinMap(coinMapData, inputPrice)
            }

        } else {
            Log.d("我是入口","0.0==2")
            et_price?.setText("0.0")
            inputPrice="0.0"
            tv_convert_price?.text = "0.0"
            return
        }
    }


    private  fun priceSub(){
        if (Utils.isFastClick(300)){
            return
        }
        if (!LoginManager.checkLogin(context, true)){
            return
        }
        val scale = if (transactionType == TYPE_SELL && priceType == TYPE_MARKET) {
            volumeScale
        } else {
            priceScale
        }
        val unit = (1 / 10.0.pow(scale.toDouble())).toString()
        if (TextUtils.isEmpty(unit)){
            return
        }
        if (inputPrice.isEmpty()) {
            Log.d("我是入口","==7")
            et_price?.setText("")
            tv_convert_price?.text = ""
            return
        }
        if (BigDecimal(inputPrice).toFloat() > 0f) {
            inputPrice =
                BigDecimalUtils.divForDown(BigDecimalUtils.sub(inputPrice, unit).toPlainString(), scale).toPlainString()
            //这边必须要这样不然字段会改变
            val inputPrice1= inputPrice
            if (inputPrice1.isNotEmpty()){
                et_price?.setText(BigDecimalUtils.subAndDot(inputPrice1))
                inputPrice=inputPrice1
                Log.d("我是入口","${inputPrice}==3")
                tv_convert_price?.text = RateManager.getCNYByCoinMap(coinMapData, inputPrice)
            }
        } else {
            Log.d("我是入口","0.0==5")
            et_price?.setText("0.0")
            tv_convert_price?.text = "0.0"
            return
        }

    }



    private var beforlong = 0
    private var bhlong: Int = 0
    private fun addTextListener() {

        /**
         * 价格
         */
        et_price?.filters = arrayOf(DecimalDigitsInputFilter(priceScale))
        et_price?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                Log.d(TAG, "======价格输入框的内容==afterTextChanged:${s.toString()}=======")
                bhlong = s.toString().length

                if (priceType == TYPE_MARKET || TextUtils.isEmpty(s) || s.toString() == "0.") {
                    tv_convert_price?.visibility = View.INVISIBLE
                } else {
                    Log.d(TAG, "=======可见===========")
                    tv_convert_price?.visibility = View.VISIBLE
                    tv_convert_price?.text = RateManager.getCNYByCoinMap(coinMapData, s.toString())
                }

                if (s?.startsWith(".") == true) {
                    Log.d("我是入口", "${inputPrice}==10")
                    et_price?.text?.clear()
                    Log.d(TAG, "=======1===========")
                }
                if(s.isNullOrEmpty()&&inputPrice.isNotEmpty()){
                    et_price.text=inputPrice.editable()
                }

                if (inputPrice.isEmpty()) {
                    isClear = true
                }
                if (inputPrice.startsWith(".")) {
                    inputPrice = "0"
                }

                if (transactionType == TYPE_BUY) {
                    if (priceType == TYPE_LIMIT) {
                        if (TextUtils.isEmpty(inputPrice) || TextUtils.isEmpty(inputQuantity)) {
                            tv_transaction_money?.text = "--"
                        } else {
                            //计算总金额
                            var money = BigDecimalUtils.mul(inputPrice, inputQuantity).toPlainString()
                            money = DecimalUtil.cutValueByPrecision(money, priceScale)

                            tv_transaction_money?.text = "${BigDecimalUtils.showSNormal(money) + showMarket()}"
                        }
                    } else {
                        //买入且市价交易状态下，价格未知,交易额不显示
//                        tv_transaction_money?.text = SymbolInterceptUtils.interceptData(canUseMoney, coinMapData?.optString("symbol", ""), "price") + marketName
                    }
                } else {
                    //当市价卖出状态时，此输入框输入的是商品数量（即币对前半段）
                    if (priceType == TYPE_LIMIT) {
                        if (TextUtils.isEmpty(inputPrice) || TextUtils.isEmpty(inputQuantity)) {
                            tv_transaction_money?.text = "--"
                        } else {
                            var money = BigDecimalUtils.mul(inputPrice, inputQuantity).toPlainString()
                            money = DecimalUtil.cutValueByPrecision(money, priceScale)

                            tv_transaction_money?.text = "${BigDecimalUtils.showSNormal(money) + showMarket()}"
                        }

                    }
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                beforlong = s.toString().length
                Log.d(TAG, "==========beforeTextChanged:$start  ${count}  ${after}")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        /**
         * 数量
         */
        et_volume?.filters = arrayOf(DecimalDigitsInputFilter(volumeScale))
        et_volume?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (s?.startsWith(".") == true) {
                    et_price?.text?.clear()
                }

                Log.d(TAG, "======数量输入框的内容:${s.toString()},${volumeScale}=======")

                inputQuantity = s.toString()

                /**
                 * 清除 RadioButton 的选中效果
                 */
                if (s.toString().length < volumeScale + 2) {
                    for (i in 0 until rg_trade.childCount step 2) {
                        val radioButton = rg_trade?.getChildAt(i) as RadioButton
                        radioButton.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                        radioButton.backgroundResource = R.color.transparent
                    }
                }

//                inputQuantity = if (TextUtils.isEmpty(s)) {
//                    "0"
//                } else {
//                    s.toString()
//                }

                Log.d(TAG, "==========inputQuantity:$inputQuantity")

                if (inputQuantity.startsWith(".")) {
                    inputQuantity = "0"
                }

                if (transactionType == TYPE_BUY) {
                    if (priceType == TYPE_LIMIT) {
                        //交易额
                        if (TextUtils.isEmpty(inputPrice) || TextUtils.isEmpty(inputQuantity)) {
                            tv_transaction_money?.text = "--"
                        } else {
                            var money = BigDecimalUtils.mul(inputPrice, inputQuantity).toPlainString()
                            money = DecimalUtil.cutValueByPrecision(money, priceScale)
                            tv_transaction_money?.text = "${BigDecimalUtils.showSNormal(money) + showMarket()}"
                        }
                    }
                } else {
                    if (priceType == TYPE_LIMIT) {
                        if (TextUtils.isEmpty(inputQuantity) || TextUtils.isEmpty(inputPrice)) {
                            tv_transaction_money?.text = "--"
                        } else {
                            //交易额
                            var money = BigDecimalUtils.mul(inputPrice, inputQuantity).toPlainString()
                            money = DecimalUtil.cutValueByPrecision(money, priceScale)
                            tv_transaction_money?.text = "${BigDecimalUtils.showSNormal(money) + showMarket()}"
                        }

                    }
                }
//                et_volume?.setSelection(et_volume?.text?.length ?: 0)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    /**
     * 下单
     */
    fun createOrder() {
        cbtn_create_order?.showLoading()
        val side = if (transactionType == TYPE_BUY) {
            "BUY"
        } else {
            "SELL"
        }

        val type = if (priceType == TYPE_LIMIT) {
            1
        } else {
            2
        }
        //市价情况下：买则表示交易额，卖表示总个数；限价下:买卖个数
        val volume = inputQuantity
        //限价模式下表示价格，市价无意义
        val price = inputPrice
        Log.d(TAG, "=disposable:===${disposable == null} ,${mainModel == null}======")
        (disposable ?: CompositeDisposable()).add(
            (mainModel
                ?: MainModel()).createOrder(side,
                type,
                volume,
                price,
                coinMapData?.optString("symbol", "")
                    ?: return,
                isLever = TradeFragment.currentIndex == LEVER_INDEX_TAB,
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(data: JSONObject) {
                        cbtn_create_order?.hideLoading()
                        NToastUtil.showTopToastNet(
                            getActivity(),
                            true,
                            LanguageUtil.getString(context, "contract_tip_submitSuccess")
                        )
                        val event =
                            MessageEvent(MessageEvent.CREATE_ORDER_TYPE, true, TradeFragment.currentIndex == LEVER_INDEX_TAB)
                        try {
                            val item = data.getJSONObject("data")
                            event.msg_content_data = item
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        NLiveDataUtil.postValue(event)
                        EventBusUtil.post(event)
                        //刷新委托列表
                        getAvailableBalance()
                        /**
                         * 设置 RadioButton 的选中效果
                         */
                        for (i in 0 until rg_trade.childCount step 2) {
                            val radioButton = rg_trade?.getChildAt(i) as RadioButton
                            radioButton.setTextColor(ColorUtil.getColor(R.color.normal_text_color))
                            radioButton.backgroundResource = R.color.transparent
                        }
                        rg_trade.clearCheck()
                        et_volume?.text?.clear()
                        et_volume?.invalidate()
                        et_volume?.setText("")
                    }

                    override fun onResponseFailure(code: Int, msg: String?) {
                        super.onResponseFailure(code, msg)
                        cbtn_create_order?.hideLoading()
                    }
                })!!
        )

    }


    private fun observeData() {

        NLiveDataUtil.observeData((this.context as NewMainActivity), Observer<MessageEvent> {
            if (null == it)
                return@Observer

            if (TradeFragment.currentIndex == CVC_INDEX_TAB) {
                if (it.isLever) {
                    return@Observer
                }
            } else {
                if (!it.isLever) {
                    return@Observer
                }
            }

            when (it.msg_type) {
                MessageEvent.TAB_TYPE -> {
                    coinMapData = if (it.isLever) {
                        NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol4Lever)
                    } else {
                        NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol)
                    }

                    if (it.isLever) {
                        buyOrSell(transactionType, it.isLever)
                    } else {
                        buyOrSell(transactionType, it.isLever)
                    }

                }

                MessageEvent.symbol_switch_type -> {
                    if (null != it.msg_content) {
                        val symbol = it.msg_content as String
                        if (symbol != coinMapData?.optString("symbol", "")) {
                            coinMapData = NCoinManager.getSymbolObj(symbol)
                            tv_coin_name?.text = "${showCoinName()}"
                            tv_convert_price?.text = "--"
                            tv_transaction_money?.text = "--"
                            getAvailableBalance()
                        }
                    }
                }

            }
        })
    }

    private fun showCoinName(): String? {
        return NCoinManager.getMarketShowCoinName(showAnoterName(coinMapData))
    }

    private fun showMarket(): String? {
        return NCoinManager.getMarketName(showAnoterName(coinMapData))
    }

    /**
     * 买入 & 卖出
     */
    fun buyOrSell(transferType: Int, isLever: Boolean = false) {
        this.isLever = isLever
        coinMapData = if (isLever) {
            NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol4Lever)
        } else {
            NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol)
        }
        ll_etf_item?.visibility = View.GONE
        resetPrice()
        setPrice()
        tabChangeReset()
        if (priceType == TYPE_LIMIT) {
            v_market_trade_tip?.visibility = View.GONE
            ll_price?.visibility = View.VISIBLE
            tv_convert_price?.visibility = View.VISIBLE
            tv_convert_price?.text = "--"
            ll_transaction?.visibility = View.VISIBLE
            tv_transaction_money?.text = "--"
            if (transactionType == TYPE_BUY) {
                et_volume?.hint = LanguageUtil.getString(context, "charge_text_volume")
            } else {
                et_volume?.hint = LanguageUtil.getString(context, "common_text_sellVolume")
            }
            getAvailableBalance()
        } else {
            v_market_trade_tip?.visibility = View.VISIBLE
            ll_price?.visibility = View.GONE
            tv_convert_price?.visibility = View.INVISIBLE
            ll_transaction?.visibility = View.INVISIBLE
            tv_transaction_money?.text = "--"
            if (transactionType == TYPE_BUY) {
                et_volume?.hint = LanguageUtil.getString(context, "transaction_text_tradeSum")
            } else {
                et_volume?.hint = LanguageUtil.getString(context, "common_text_sellVolume")
            }
            getAvailableBalance()
        }

        val priceClose = et_price?.text?.toString()
        if (!priceClose.isNullOrEmpty()) {
            LogUtil.d(TAG, "========buyOrSell======price_close is $priceClose")
            tv_convert_price?.text = "${RateManager.getCNYByCoinMap(coinMapData, priceClose)}"
        }
        transactionType = transferType

        /**
         * 切换方向清空数量
         */
        rg_trade?.clearCheck()
        et_volume?.text?.clear()
        changeBuyOrSell()
        if (currentItem != null) {
            changeSellOrBuyData(currentItem!!)
        }
        if (transferType == TYPE_BUY) {
            if (UserDataService.getInstance().isLogined) {
                cbtn_create_order?.textContent = LanguageUtil.getString(context, "contract_action_buy") + " " + showCoinName()
            } else {
                cbtn_create_order?.textContent = LanguageUtil.getString(context, "login_action_login")
            }
            cbtn_create_order?.normalBgColor = ColorUtil.getMainColorType()

            /**
             * 币种
             */
            tv_coin_name?.text =
                if (priceType == TYPE_LIMIT) {
                    "${showCoinName()}"
                } else {
                    "${showMarket()}"
                }

            if (priceType == TYPE_LIMIT) {
                et_volume?.hint = LanguageUtil.getString(context, "charge_text_volume")
            } else {
                et_volume?.hint = LanguageUtil.getString(context, "transaction_text_tradeSum")
            }
            /**
             * 设置 RadioButton 的选中效果
             */
            for (i in 0 until rg_trade.childCount step 2) {
                val radioButton = rg_trade?.getChildAt(i) as RadioButton
                radioButton.setTextColor(ColorUtil.getCheck4ColorStateList())
                radioButton.background = ColorUtil.getCheck4StateListDrawable()
            }

        } else {
            if (UserDataService.getInstance().isLogined) {
                cbtn_create_order?.textContent = LanguageUtil.getString(context, "contract_action_sell") + " " + showCoinName()
            } else {
                cbtn_create_order?.textContent = LanguageUtil.getString(context, "login_action_login")
            }

            cbtn_create_order?.normalBgColor = ColorUtil.getMainColorType(false)

            /**
             * 币种
             */
            tv_coin_name?.text = "${showCoinName()}"

            et_volume?.hint = LanguageUtil.getString(context, "common_text_sellVolume")

            /**
             * 设置 RadioButton 的选中效果
             */
            for (i in 0 until rg_trade.childCount step 2) {
                val radioButton = rg_trade?.getChildAt(i) as RadioButton
                radioButton.setTextColor(ColorUtil.getCheck4ColorStateList(isRise = false))
                radioButton.background = ColorUtil.getCheck4StateListDrawable(isRise = false)
            }
        }
    }

    var currentItem: JSONObject? = null
    fun changeSellOrBuyData(data: JSONObject) {
        LogUtil.d("getAvailableBalance", "getAvailableBalance==data is $data")

        val countCoinBalance = data.optString("countCoinBalance")
        val baseCoinBalance = data.optString("baseCoinBalance")
        if (transactionType == TYPE_BUY) {
            val coinName = NCoinManager.getMarketName(coinMapData?.optString("name", ""))
            var precision = NCoinManager.getCoinShowPrecision(coinName)
            if (TradeFragment.currentIndex == LEVER_INDEX_TAB) {
                precision = 8
            }
            canUseMoney = DecimalUtil.cutValueByPrecision(
                countCoinBalance
                    ?: "0", precision
            )

            NCoinManager.getMarketByName(showCoinName())
            tv_available_balance?.text = "$canUseMoney ${showMarket()}"

            tv_coin_name?.text = if (priceType == TYPE_LIMIT) "${showCoinName()}" else "${showMarket()}"
        } else {
            canUseMoney = baseCoinBalance.getTradeCoinBalance(coinMapData)
            tv_coin_name?.text = "${showCoinName()}"
            tv_available_balance?.text = "$canUseMoney ${showCoinName()}"
        }
        currentItem = data
    }

    private fun changeBuyOrSell() {
        LogUtil.d(TAG, "changeBuyOrSell== ${isLever}  ${transactionType}$")
        when (this.transactionType) {
            // 买
            TYPE_BUY -> {
                rb_buy?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_select_text_color))
                    backgroundResource = ColorUtil.getBuyOrSell(true)
                }

                rb_sell?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_normal_text_color))
                    backgroundResource = R.mipmap.coins_exchange_sell_grey
                }
            }
            // 卖
            TYPE_SELL -> {
                rb_buy?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_normal_text_color))
                    backgroundResource = R.mipmap.coins_exchange_buy_grey
                }

                rb_sell?.run {
                    typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(context!!, R.color.btn_select_text_color))
                    backgroundResource = ColorUtil.getBuyOrSell(false)
                }

            }
        }
    }




    fun editPriceIsNull(): Boolean {
        if (et_price.text.isNullOrEmpty() && !isClear) {
            return true
        }
        return false
    }


    fun initTick(tick: JSONArray, depthLevel: Int = 2,type: Int) {
        if(inputPrice.isEmpty()){
            et_price.text  = tick.getPriceTick(depthLevel).editable()
            Log.d("我是入口","${tick.getPriceTick(depthLevel)}==8")
            inputPrice= tick.getPriceTick(depthLevel)
        }


    }



    fun verticalDepth(isVertical: Boolean = false, isBuy: Boolean = true, isLever: Boolean = true) {
        rg_buy_sell.visibility = (!isVertical).visiableOrGone()
        tv_order_type.visibility = (!isVertical).visiableOrGone()
        img_transfer.visibility = (!isVertical).visiableOrGone()
        transactionType = when (isBuy) {
            true -> TYPE_BUY
            else -> TYPE_SELL
        }
        this.isLever = isLever
        buyOrSell(transactionType, isLever)

        tv_sub.visibility = (!isVertical).visiableOrGone()
        v_line.visibility = (!isVertical).visiableOrGone()
        tv_add.visibility = (!isVertical).visiableOrGone()
        v_sub_line.visibility = (!isVertical).visiableOrGone()

        layout_v_tools.visibility = isVertical.visiableOrGone()
        et_price.gravity = when (isVertical) {
            true -> Gravity.CENTER_VERTICAL
            else -> Gravity.CENTER
        }


    }

    fun changePriceType(item: Int) {
        var showCoinName = NCoinManager.getMarketShowCoinName(coinMapData?.optString("showName", ""))
        when (item) {
            0 -> {
                priceType = TYPE_LIMIT
                v_market_trade_tip?.visibility = View.GONE
                ll_price?.visibility = View.VISIBLE
                tv_convert_price?.visibility = View.VISIBLE
                ll_transaction?.visibility = View.VISIBLE
                if (transactionType == TYPE_BUY) {
                    et_volume?.hint = LanguageUtil.getString(context, "charge_text_volume")
                } else {
                    et_volume?.hint = LanguageUtil.getString(context, "common_text_sellVolume")
                }
                tv_coin_name?.text = "$showCoinName"
                getAvailableBalance()
            }
            1 -> {
                priceType = TYPE_MARKET
                v_market_trade_tip?.visibility = View.VISIBLE
                ll_price?.visibility = View.GONE
                tv_convert_price?.visibility = View.INVISIBLE
                ll_transaction?.visibility = View.INVISIBLE
                if (transactionType == TYPE_BUY) {
                    et_volume?.hint = LanguageUtil.getString(context, "transaction_text_tradeSum")
                } else {
                    et_volume?.hint = LanguageUtil.getString(context, "common_text_sellVolume")
                }
                tv_coin_name?.text = "$showCoinName"
                getAvailableBalance()
                resetPrice()
            }
        }
    }

    var isClear = false
    fun resetPrice() {
        inputPrice=""
        et_price?.text?.clear()
        isClear = false
    }




    fun changeEtfLayout(isVerticalOrGone: Boolean = false) {
        val layoutParams = ll_transaction.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = DisplayUtil.dip2px(
            when (isVerticalOrGone) {
                true -> 18
                else -> 35
            }
        )
        isETF = !isVerticalOrGone
    }

    private fun isBuy(): Boolean {
        return transactionType == TYPE_BUY
    }

    private fun tabChangeReset() {
        et_price?.clearFocus()
        et_volume?.clearFocus()
        KeyBoardUtils.closeKeyBoard(context)

    }

    fun notLoginLayout(isShow: Boolean = false) {
        cbtn_create_order?.visibility = isShow.visiableOrGone()
    }


    fun getActivity(): Activity? {
        if (context is Activity) {
            return context as Activity
        }
        return null
    }

    private fun getCurrentSymbol(): String {
        return coinMapData?.optString("symbol", "") ?: ""
    }

    private fun getCurrentCoin(): String {
        return NCoinManager.getMarketName(coinMapData?.optString("name", "") ?: "")
    }

    fun getPrecision(): Int {
        return NCoinManager.getMarketCoinShowPrecision(getCurrentSymbol())
    }

    private fun tradeETF() {
        (disposable ?: CompositeDisposable()).add(
            (mainModel
                ?: MainModel()).getETFCoin(consumer = object : NDisposableObserver(true) {
                override fun onResponseSuccess(data: JSONObject) {

                    try {
                        val item = data.getJSONObject("data")
                        val status = item.optInt("status", 0)
                        if (status == 0) {
                            val url = etfInfo?.optString("faqUrl") ?: ""
                            val domainName = etfInfo?.optString("domainName") ?: ""
                            DialogUtil.showETFStatement(
                                context
                                    ?: return, domainName, url
                            )
                        } else if (status == 1) {
                            NToastUtil.showTopToastNet(
                                getActivity(),
                                false,
                                LanguageUtil.getString(context, "etf_agreement_pendingKYC")
                            )
                        } else if (status == 2) {
                            // 跳转 kyc
                            ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                        } else if (status == 3) {
                            NToastUtil.showTopToastNet(
                                getActivity(),
                                false,
                                LanguageUtil.getString(context, "etf_agreement_countryNotSurpport")
                            )
                        } else {
                            MainModel().saveUserInfo()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onResponseFailure(code: Int, msg: String?) {
                    super.onResponseFailure(code, msg)

                }
            })!!
        )
    }

}




