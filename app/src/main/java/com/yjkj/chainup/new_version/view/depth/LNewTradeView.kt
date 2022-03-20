package com.yjkj.chainup.new_version.view.depth

import androidx.lifecycle.Observer
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.RxView
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.ParamConstant.*
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.model.model.MainModel
import com.yjkj.chainup.new_version.activity.NewMainActivity
import com.yjkj.chainup.new_version.activity.leverage.NLeverFragment
import com.yjkj.chainup.new_version.activity.leverage.TradeFragment
import com.yjkj.chainup.new_version.dialog.DialogUtil
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.fragment.NCVCTradeFragment
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.trade_amount_view_new.view.*
import kotlinx.android.synthetic.main.trade_amount_view_new_l.view.*
import kotlinx.android.synthetic.main.trade_amount_view_new_l.view.tv_order_type
import org.jetbrains.anko.view
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * @Author: Bertking
 * @Date：2019/3/7-5:43 PM
 * @Description: 交易量的View
 */
class LNewTradeView @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null,
                                              defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    val TAG = LNewTradeView::class.java.simpleName


    private val delayTime = 100L

    //交易类型
    var transactionType = ParamConstant.TYPE_BUY
    var isLever = false

    //价格类型
    var priceType = 0


    var priceScale = 2
    var volumeScale = 2
    var disposable: CompositeDisposable? = null
    var mainModel: MainModel? = null

    var dialog: TDialog? = null
    var coinMapData: JSONObject? = NCoinManager.getSymbolObj(PublicInfoDataService.getInstance().currentSymbol)
        set(value) {
            Log.d(TAG, "=======调用SET=====value is $value")
            field = value
            synchronized(this) {
                priceScale = value?.optInt("price", 2) ?: 2
                volumeScale = value?.optInt("volume", 2) ?: 2
            }

        }


    fun setPrice() {

        synchronized(this) {
            priceScale = coinMapData?.optInt("price", 2) ?: 2
            volumeScale = coinMapData?.optInt("volume", 2) ?: 2
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
            //
            return
        }


    }

    fun setTextContent() {
        tv_order_type?.textContent = LanguageUtil.getString(context, "contract_action_limitPrice")

    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ComVerifyView, 0, 0)
            typedArray.recycle()
        }


        LogUtil.d(TAG, "TradeView==init==priceScale is $priceScale,volumeScale is $volumeScale")
        /**
         * 这里的必须为：True
         */
        LayoutInflater.from(context).inflate(R.layout.trade_amount_view_new_l, this, true)

        setTextContent()

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
                    .subscribe({ x ->
                        DialogUtil.createCVCOrderPop(context, priceType, it, object : NewDialogUtils.DialogOnSigningItemClickListener {
                            override fun clickItem(position: Int, text: String) {
                                tv_order_type?.textContent = text
                                priceType = position
                                trade_amount_view_buy?.changePriceType(position)
                                trade_amount_view_sell?.changePriceType(position)
                                dialog?.dismiss()
                            }
                        })
                    })
        }
        getAvailableBalance()
        operator4PriceVolume(context)

        addTextListener()
        loginStatusView()
        cbtn_l_create_order_login?.isEnable(true)
        cbtn_l_create_order_login?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (!LoginManager.checkLogin(context, true)) return
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

    }


    private fun addTextListener() {

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
                        buyOrSell(0, it.isLever)
                    } else {
                        buyOrSell(0, it.isLever)
                    }

                }

                MessageEvent.symbol_switch_type -> {
                    if (null != it.msg_content) {
                        val symbol = it.msg_content as String
                        if (symbol != coinMapData?.optString("symbol", "")) {
                            coinMapData = NCoinManager.getSymbolObj(symbol)
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
        trade_amount_view_buy?.verticalDepth(true, true, isLever)
        trade_amount_view_sell?.verticalDepth(true, false, isLever)
    }


    private fun showBalanceData() {

    }

    fun editPriceIsNull(): Boolean {
        return false
    }

    fun initTick(tick: JSONArray, isBuy: Boolean = true, depthLevel: Int = 2) {
        LogUtil.e(TAG,"initTick  isBuy ${isBuy}")
        if (!isBuy) {
            if (isFirstSetValue(!isBuy)) {
                trade_amount_view_buy?.initTick(tick, depthLevel)
            }
        } else {
            if (isFirstSetValue(!isBuy)) {
                trade_amount_view_sell?.initTick(tick, depthLevel)
            }
        }

    }


    /**
     * 限价的情况下，价格默认为：收盘价
     */
    fun isFirstSetValue(isBuy: Boolean = true): Boolean {
        if (isBuy) {
            return trade_amount_view_buy.editPriceIsNull()
        } else {
            return trade_amount_view_sell.editPriceIsNull()
        }
    }

    fun resetPrice() {
        trade_amount_view_buy?.resetPrice()
        trade_amount_view_sell?.resetPrice()
    }

    fun changeSellOrBuyData(data: JSONObject?) {
        if (data != null) {
            trade_amount_view_buy?.changeSellOrBuyData(data)
            trade_amount_view_sell?.changeSellOrBuyData(data)
        }
    }

    fun loginStatusView() {
        val isLogin = UserDataService.getInstance().isLogined
        if (isLogin) {
            cbtn_l_create_order_login?.visibility = View.GONE
        } else {
            cbtn_l_create_order_login?.visibility = View.VISIBLE
        }
        trade_amount_view_buy?.notLoginLayout(isLogin)
        trade_amount_view_sell?.notLoginLayout(isLogin)
    }

    fun changeETFInfo(data: JSONObject?){
        trade_amount_view_buy?.etfInfo = data
        trade_amount_view_sell?.etfInfo = data
    }

}




