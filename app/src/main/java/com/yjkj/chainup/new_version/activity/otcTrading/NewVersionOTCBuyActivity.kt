package com.yjkj.chainup.new_version.activity.otcTrading

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.google.gson.JsonObject
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.service.OTCPublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_setting.view.*
import kotlinx.android.synthetic.main.activity_new_version_otc_buy.*
import kotlinx.android.synthetic.main.item_otc_buy_or_sell_detail.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * @Author lianshangljl
 * @Date 2019/4/16-11:09 AM
 * @Email buptjinlong@163.com
 * @description 购买页面
 */
class NewVersionOTCBuyActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_new_version_otc_buy
    }


    var advertID = -1
    private var mdDisposable: Disposable? = null

    /**
     * 数量时的价格
     */
    var amountPrice = "0"

    companion object {
        val ADVERTID = "advertId"

        fun enter2(context: Context, advertID: Int) {
            var intent = Intent(context, NewVersionOTCBuyActivity::class.java)
            intent.putExtra(ADVERTID, advertID)
            context.startActivity(intent)
        }
    }

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        getData()
        setOnClick()
        setTextContent()
        rb_price_buy?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, ColorUtil.getOTCBuyOrSellDrawable())
        rb_amount_buy?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

        cet_total_money?.hint = getStringContent("otc_tip_inputWishPrice")
        cet_total_money?.isFocusable = true
        cet_total_money?.isFocusableInTouchMode = true
        cub_confirm_for_buy?.isEnable(false)
        cet_total_money?.setOnFocusChangeListener { v, hasFocus ->
            v_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        getADDetail4OTC()
    }
    fun setTextContent() {
        btn_cancel?.setContent(getStringContent("common_text_btnCancel"))
        cub_confirm_for_buy?.setContent(getStringContent("otc_action_placeOrder"))
        rb_price_buy?.text = getStringContent("otc_action_buyByPrice")
        rb_amount_buy?.text = getStringContent("otc_action_buyByVolume")
        tv_price?.text = getStringContent("contract_text_price")
        tv_total_money?.text = getStringContent("otc_text_orderTotal")
        tv_limit?.text = getStringContent("otc_text_priceLimit")
        tv_fiat_balance?.text = getStringContent("otc_asset_availableBalance")
        tv_anti_money_laundering?.text = getStringContent("otc_tip_withdrawLimitTime")
        tv_trading_title?.text = getStringContent("otc_tip_tradeHintTitle")
        tv_trading_content?.text = getStringContent("otc_tip_tradeHintContent")
        cub_confirm_for_sell?.setBottomTextContent(LanguageUtil.getString(this,"otc_action_placeOrder"))
    }

    fun getStringContent(contentId: String): String {
        return LanguageUtil.getString(this, contentId)
    }

    var priceOrtotal = true
    var amount = ""

    fun setOnClick() {
        /**
         * 切换 价格购买 or 数量购买
         */
        rg_buy_sell?.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.rb_price_buy -> {
                    rb_price_buy?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, ColorUtil.getOTCBuyOrSellDrawable())
                    rb_amount_buy?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    tv_total_money?.text = LanguageUtil.getString(this,"otc_text_orderTotal") + addetailBean?.optString("payCoin")
                    cet_total_money?.hint = LanguageUtil.getString(this,"otc_tip_inputWishPrice")
                    cet_total_money?.setText("")
                    tv_market_price?.text = "≈0 " + NCoinManager.getShowMarket(addetailBean?.optString("coin"))
                    v_line?.setBackgroundResource(R.color.main_blue)
                    priceOrtotal = true
                }

                R.id.rb_amount_buy -> {
                    rb_amount_buy?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, ColorUtil.getOTCBuyOrSellDrawable())
                    rb_price_buy?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    tv_total_money?.text =LanguageUtil.getString(this,"charge_text_volume")  + "(${NCoinManager.getShowMarket(addetailBean?.optString("coin"))})"
                    cet_total_money?.hint = LanguageUtil.getString(this,"otc_tip_inputWishVolume")
                    v_line?.setBackgroundResource(R.color.main_blue)
                    cet_total_money?.setText("")
                    tv_market_price?.text = "≈0 " + addetailBean?.optString("payCoin")
                    priceOrtotal = false
                }
            }
        }
    }


    fun getData() {
        if (intent != null) {
            advertID = intent.getIntExtra(ADVERTID, -1)
        }
        EdittextUtil.setEditTextEditable(cet_price,false)

        ll_trading_layout?.visibility = View.VISIBLE
        cub_confirm_for_sell?.visibility = View.GONE
        if ("1" == OTCPublicInfoDataService.getInstance().getwindControlSwitch()) {
            tv_anti_money_laundering?.visibility = View.VISIBLE
        }
    }

    var addetailBean: JSONObject? = null
    fun initView(bean: JSONObject?) {
        if(null==bean)
            return

        btn_cancel.isEnable(true)
        addetailBean = bean
        title_layout?.setContentTitle(LanguageUtil.getString(this,"otc_action_buy") + NCoinManager.getShowMarket(bean.optString("coin")))
        tv_fiat_balance?.visibility = View.GONE
        tv_price?.text = LanguageUtil.getString(this,"contract_text_price") + bean.optString("payCoin")
        tv_total_money?.text = LanguageUtil.getString(this,"otc_text_orderTotal") + bean.optString("payCoin")
        /**
         * 最大的币种的量
         */
        val maxSymbol = BigDecimalUtils.div(bean.optString("maxTrade").toString(), bean.optString("price").toString(), NCoinManager.getCoinShowPrecision(bean.optString("coin")))
        /**
         * 设置用户名
         */
        user_info_view?.setUserNick(bean?.optString("otcNickName"))
        /**
         * 设置交易数量
         */
        user_info_view?.setTransactionNumber(bean.optString("completeOrders").toString())
        /**
         * 设置信用度
         */
        user_info_view?.setCreditContent("${BigDecimalUtils.divForDown((bean.optDouble("creditGrade") * 100).toString(), 0)}%")
        /**
         * 累计成交
         */
        user_info_view?.setCumulativeClinch(BigDecimalUtils.intercept(bean.optString("turnover").toString(), NCoinManager.getCoinShowPrecision(bean?.optString("coin"))).toString())

        var jsonArray = bean.optJSONArray("payments")
        if (null != jsonArray) {
            var paymentList = arrayListOf<JSONObject>()
            for (num in 0 until jsonArray.length()) {
                paymentList.add(jsonArray.optJSONObject(num))
            }
            user_info_view?.initPayments(paymentList)
        }


        /**
         * 设置价格
         */
        var price = bean.optString("price")
        var payCoin = bean.optString("payCoin")
        var precision = RateManager.getFiat4Coin(payCoin)
        var priceN = BigDecimalUtils.divForDown(price,precision)

        LogUtil.d(TAG,"initView==price is $price,payCoin is $payCoin")
        cet_price?.setText("$priceN $payCoin")
        /**
         * 限额
         */
        var minTrade = bean.optString("minTrade")
        var maxTrade = bean.optString("maxTrade")
        var minTradeN = BigDecimalUtils.divForDown(minTrade,precision)
        var maxTradeN = BigDecimalUtils.divForDown(maxTrade,precision)

        tv_limit?.text = LanguageUtil.getString(this,"otc_text_priceLimit") + "$minTradeN$payCoin - $maxTrade$payCoin"
        cancelBtnState()
        cet_total_money?.filters = arrayOf(DecimalDigitsInputFilter(precision))
        cet_total_money?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                amount = s.toString()

                if (priceOrtotal) {
                    calculateForPrice(amount, bean)
                } else {
                    calculateForAmount(amount, bean)
                }
                if (amount.isNotEmpty()) {
                    detectionPrice(bean)
                }else{
                    cub_confirm_for_buy?.isEnable(false)
                }

            }

        })

        /**
         * 点击全部
         *  @param priceOrtotal true 价格购买 数量购买
         */
        tv_all_buy?.setOnClickListener {
            amount = cet_total_money?.text.toString()
            if (priceOrtotal) {
                cet_total_money?.setText(bean.optString("maxTrade").toString())
                calculateForPrice(cet_total_money?.text.toString(), bean)
            } else {
                cet_total_money?.setText(maxSymbol.toString())
                calculateForAmount(cet_total_money?.text.toString(), bean)
            }

        }

        /**
         * 点击下单
         */
        cub_confirm_for_buy?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                buyOrderEnd4OTC(advertID = advertID.toString(),
                        volume = if (priceOrtotal) amount4Symbol else amount,
                        price = bean.optString("price").toString(),
                        totalPrice = if (priceOrtotal) amount else amount4Symbol,
                        type = if (priceOrtotal) "price" else "volume"
                )
            }

        }

    }

    var WhetherThrough = true

    var amount4Symbol = ""

    /**
     * 检测是否超出
     */
    fun detectionPrice(bean: JSONObject) {
        val redColor = ColorUtil.getColor(this,R.color.red)
        val textColor = ColorUtil.getColor(this,R.color.text_color)
        if (priceOrtotal) {
            var max = BigDecimalUtils.compareTo(amount, bean.optString("maxTrade").toString())
            if (max == 1) {
                WhetherThrough = false
                cet_total_money?.setTextColor(redColor)
                v_line?.setBackgroundResource(R.color.red)
                cub_confirm_for_buy?.isEnable(false)
                return
            } else {
                cet_total_money?.setTextColor(textColor)
                v_line?.setBackgroundResource(R.color.main_blue)
                cub_confirm_for_buy?.isEnable(true)
                WhetherThrough = true
            }

            var min = BigDecimalUtils.compareTo(bean.optString("minTrade").toString(), amount)
            if (min == 1) {
                cet_total_money?.setTextColor(redColor)
                v_line?.setBackgroundResource(R.color.red)
                cub_confirm_for_buy?.isEnable(false)
                WhetherThrough = false
            } else {
                cet_total_money?.setTextColor(textColor)
                v_line?.setBackgroundResource(R.color.main_blue)
                cub_confirm_for_buy?.isEnable(true)
                WhetherThrough = true
            }
        } else {
            var max = BigDecimalUtils.compareTo(amountPrice, bean.optString("maxTrade").toString())
            if (max == 1) {
                WhetherThrough = false
                cet_total_money?.setTextColor(redColor)
                v_line?.setBackgroundResource(R.color.red)
                cub_confirm_for_buy?.isEnable(false)
                return
            } else {
                WhetherThrough = true
                cet_total_money?.setTextColor(textColor)
                v_line?.setBackgroundResource(R.color.main_blue)
                cub_confirm_for_buy?.isEnable(true)
            }

            var min = BigDecimalUtils.compareTo(bean.optString("minTrade").toString(), amountPrice)
            if (min == 1) {
                WhetherThrough = false
                cet_total_money?.setTextColor(redColor)
                v_line?.setBackgroundResource(R.color.red)
                cub_confirm_for_buy?.isEnable(false)
                return
            } else {
                WhetherThrough = true
                cet_total_money?.setTextColor(textColor)
                v_line?.setBackgroundResource(R.color.main_blue)
                cub_confirm_for_buy?.isEnable(true)
                return
            }
        }
    }


    /**
     * 根据价格计算
     */
    fun calculateForPrice(temp: String, bean: JSONObject) {
        tv_market_price.text = BigDecimalUtils.div(temp, bean.optString("price").toString(), NCoinManager.getCoinShowPrecision(bean.optString("coin"))).toString() + NCoinManager.getShowMarket(bean.optString("coin"))
        amount4Symbol = BigDecimalUtils.div(temp, bean.optString("price").toString(), NCoinManager.getCoinShowPrecision(bean.optString("coin"))).toPlainString()
    }

    /**
     * 根据个数计算
     */
    fun calculateForAmount(temp: String, bean: JSONObject) {
        amountPrice = BigDecimalUtils.mul(temp, bean.optString("price").toString(), NCoinManager.getCoinShowPrecision(bean.optString("payCoin"))).toString()
        tv_market_price.text = "≈ " + amountPrice + bean.optString("payCoin")
        amount4Symbol = amountPrice
    }


    var countTotalTime = 60
    /**
     * 处理取消按钮
     */
    private fun cancelBtnState() {
        mdDisposable = Flowable.intervalRange(0, 60, 0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(object : Consumer<Long> {
                    override fun accept(t: Long?) {
                        btn_cancel.setContent("${countTotalTime - t!!}s ${LanguageUtil.getString(this@NewVersionOTCBuyActivity,"oct_action_autoCancelDesc")}")

                    }
                })


                .doOnComplete(object : Action {
                    override fun run() {
                        //倒计时完毕置为可点击状态
                        btn_cancel.setContent("60s${LanguageUtil.getString(this@NewVersionOTCBuyActivity,"oct_action_autoCancelDesc")}")
                        finish()
                    }
                })
                .subscribe()

        /**
         * 取消
         */
        btn_cancel.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                finish()
            }

        }
    }

    /**
     * 获取广告详情
     */
    private fun getADDetail4OTC() {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        addDisposable(getOTCModel().getADDetail4OTC(advertID.toString(), object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                initView(json)
            }

        }))

    }

    /**
     * 购买下单
     */
    private fun buyOrderEnd4OTC(advertID: String, volume: String, price: String, totalPrice: String, type: String) {
        showLoadingDialog()
        HttpClient.instance
                .buyOrderEnd4OTC(advertId = advertID, volume = volume, price = price, totalPrice = totalPrice, type = type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(t: JsonObject?) {
                        closeLoadingDialog()
                        if (t?.has("sequence") == true) {
                            /**
                             * 下单成功，跳转至 订单详情页面
                             */
                            var orderId = t.get("sequence").asString
                            NewVersionBuyOrderActivity.enter2(this@NewVersionOTCBuyActivity, orderId)
                            finish()
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        closeLoadingDialog()
                        if (code == 2062 || code == 2007 || code == 2039) {
                            NewDialogUtils.showSingleDialog(this@NewVersionOTCBuyActivity, msg
                                    ?: "", object : NewDialogUtils.DialogBottomListener {
                                override fun sendConfirm() {

                                }
                            }, "", LanguageUtil.getString(this@NewVersionOTCBuyActivity,"alert_common_iknow"))
                        } else if (code != -1) {
                            DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                        }

                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mdDisposable != null) {
            mdDisposable?.dispose()
        }
    }

}