package com.yjkj.chainup.new_version.activity.otcTrading

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import com.alibaba.android.arouter.facade.annotation.Route
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.service.OTCPublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.NewReleaseAdvertisiongAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.*
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.activity_release_advertising.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-10-17-11:49
 * @Email buptjinlong@163.com
 * @description 发布广告
 */
@Route(path = "/main/otc/NewReleaseAdvertisingActivity")
class NewReleaseAdvertisingActivity : NBaseActivity() {


    var payCoin = ""
    var coinSymbol = ""

    var advertisingType = "BUY"
    var number = "0"
    var pricing = "2"
    var custom_price = "0"
    var custom_actual_price = "0"

    var premium_percentage = "0"
    var allAmount = "0"
    var maxLimit = "0"
    var minLimit = "0"
    var paymentTime = "5"
    var transaction_number = "0"
    var failureTime = "30"
    var autoReply = ""
    var advertisingMessage = ""

    var payCoinRates = 0
    var failureTimeSelect = 3
    var pricingSelect = 0
    var premiumDirectionSelect = 0

    var coinDialog: TDialog? = null
    var coinTitleDialog: TDialog? = null
    var pricingDialog: TDialog? = null
    var premiumDirectionDialog: TDialog? = null
    var failureTimeDialog: TDialog? = null


    var payCoinList = arrayListOf<JSONObject>()
    var payCoinNameList = arrayListOf<String>()
    var failureTimeList = arrayListOf<String>("2", "4", "7", "30")
    var payCoinSelectItem = -1
    var paymentsList = arrayListOf<JSONObject>()
    var payCoinName = ""

    /**
     * 先选的币对
     */
    private var coinTitles: ArrayList<String> = arrayListOf()
    private var coinTitleNames: ArrayList<String> = arrayListOf()
    var coinTitlesSelect = 0

    private var adapter: NewReleaseAdvertisiongAdapter? = null


    override fun setContentView() = R.layout.activity_release_advertising


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        getData()
        initView()
        getConsiderPrice()
        getAccountBalance4OTC()
        getUserPayment4OTC()
        initAdapter()
        setTextContent()
    }

    fun setTextContent() {
        v_title?.setContentTitle(getStringContent("otc_publish_advertise"))
        tv_changecoin?.text = getStringContent("b2c_text_changecoin")
        tv_advertising_type?.text = getStringContent("otc_advertise_type")
        rb_buy?.text = getStringContent("otc_action_buy")
        rb_sell?.text = getStringContent("otc_action_sell")
        net_coin_type?.setToptitleContent("otc_coin_type")
        ntt_advertising_id?.setToptitleContent("charge_text_volume")
        ntt_advertising_id?.setEditTextHintContent("transfer_tip_emptyVolume")
        tv_otc_account?.text = getStringContent("otc_out_assetsAvalaible")
        ntt_market_reference_price?.setToptitleContent("otc_market_ReferencPrice")
        ntt_pricing?.setToptitleContent("otc_setPrice_method")
        net_custom_price?.setToptitleContent("otc_userSet_singlePrice")
        ntt_premium_direction?.setToptitleContent("otc_outPrice_direction")
        net_custom_price?.setEditTextHintContent("otc_userSet_singlePrice")

        net_premium_percentage?.setToptitleContent("otc_outPrice_percent")
        net_premium_percentage?.setEditTextHintContent("otc_outPrice_percent")
        net_minimum_limit?.setToptitleContent("otc_min_amount")
        net_minimum_limit?.setEditTextHintContent("otc_writeMin_amount")

        net_max_limit?.setToptitleContent("otc_max_amount")
        net_max_limit?.setEditTextHintContent("otc_writeMax_amount")

        net_limit_for_payment?.setToptitleContent("otc_payMoney_time")
        net_limit_for_payment?.setEditTextHintContent("otc_payMoney_timeLimit")

        net_failure_time?.setToptitleContent("otc_text_validTime")

        net_minimum_number_of_transactions?.setToptitleContent("otc_other_minTransactionTimes")
        net_minimum_number_of_transactions?.setEditTextHintContent("otc_writeOther_minTransactionTimes")

        net_auto_reply?.setToptitleContent("otc_text_autoBack")
        net_auto_reply?.setEditTextHintContent("filter_Input_placeholder")

        net_advertising_message?.setToptitleContent("otc_text_advertiseLeaveWords")
        net_advertising_message?.setEditTextHintContent("filter_Input_placeholder")

        cub_confirm?.setContent(getStringContent("otc_publish_advertise"))

        ntt_price?.setToptitleContent("otc_text_price")
        ntt_all_amount?.setToptitleContent("redpacket_send_total")

        tv_complaint_title?.text = getStringContent("otc_getMoney_Method")
        tv_no_payments?.text = getStringContent("otc_noSetGetMoney_Method")


    }

    fun getStringContent(contentId: String): String {
        return LanguageUtil.getString(this, contentId)
    }

    override fun loadData() {
        super.loadData()

    }

    fun initAdapter() {
        adapter = NewReleaseAdvertisiongAdapter(arrayListOf(), object : NewReleaseListener {
            override fun addOrRemovePaymethodListener(checkbox: CheckBox, isChecked: Boolean, payment: JSONObject) {
                addOrRemovePaymethod(checkbox, isChecked, payment)
            }
        })
        rg_complaint_layout?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rg_complaint_layout?.layoutManager = GridLayoutManager(this, 2)
        rg_complaint_layout?.adapter = adapter
    }


    fun getData() {
        payCoin = OTCPublicInfoDataService.getInstance().getotcDefaultPaycoin()
        payCoinRates = RateManager.getRatesByPayCoin(payCoin).toInt()
        custom_price = intent?.getStringExtra("custom_price") ?: "0"
        custom_actual_price = custom_price
        coinTitles = NCoinManager.getMarkets4OTC()
        var paycoins = OTCPublicInfoDataService.getInstance().paycoins
        if (null != paycoins) {
            payCoinList.clear()
            payCoinList.addAll(paycoins)
        }

        payCoinNameList.clear()
        for (temp in 0 until payCoinList.size) {
            payCoinNameList.add(payCoinList[temp].optString("title"))
            if (payCoinList[temp].optString("key") == payCoin) {
                payCoinSelectItem = temp
                payCoinName = payCoinList[temp].optString("title")
                net_coin_type?.setBottomContent(payCoinList[temp].optString("title"))
            }
        }

        if (TextUtils.isEmpty(OTCPublicInfoDataService.getInstance().defaultCoin)) {
            coinSymbol = coinTitles[0]
            coinTitlesSelect = 0
        } else {
            coinSymbol = OTCPublicInfoDataService.getInstance().defaultCoin

            for (temp in 0 until coinTitles.size) {
                coinTitleNames.add(NCoinManager.getShowMarket(coinTitles[temp]))
                if (coinTitles[temp] == coinSymbol) {
                    coinTitlesSelect = temp
                }
            }
        }
        tv_coin_symbol.text = NCoinManager.getShowMarket(coinSymbol)

        v_title.finishListener = object : PersonalCenterView.MyPorfileFinishListener {
            override fun onclickFinish() {
                NewDialogUtils.showNormalDialog(this@NewReleaseAdvertisingActivity, getString("otc_confirm_giveupEdit"), object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        finish()
                    }

                }, "", getString("common_text_btnConfirm"), getString("redpacket_payment_cancel"))
            }

        }
    }

    override fun initView() {
        advertisingType = "SELL"
        if (advertisingType == "BUY") {
            rb_buy?.isChecked = true
            rb_sell?.isChecked = false
            tv_otc_account?.visibility = View.GONE
        } else {
            rb_buy?.isChecked = false
            rb_sell?.isChecked = true
            tv_otc_account?.visibility = View.VISIBLE
        }
        /**
         * 选择币对
         */
        rl_select_coin_layout?.setOnClickListener {
            coinTitleDialog = NewDialogUtils.showBottomListDialog(this@NewReleaseAdvertisingActivity, coinTitleNames, coinTitlesSelect, object : NewDialogUtils.DialogOnclickListener {
                override fun clickItem(data: ArrayList<String>, item: Int) {
                    coinTitlesSelect = item
                    if (coinSymbol != coinTitles[item]) {
                        coinSymbol = coinTitles[item]
                        advertisingType = OTCPublicInfoDataService.getInstance().getDafaultCoin()
                        rb_buy?.isChecked = false
                        rb_sell?.isChecked = true
                        tv_complaint_title?.text = getString("otc_getMoney_Method")
                        payCoin = OTCPublicInfoDataService.getInstance().getotcDefaultPaycoin()
                        payCoinRates = RateManager.getRatesByPayCoin(payCoin)
                        refreshView()
                        custom_price = "0"
                        custom_actual_price = "0"

                        getConsiderPrice()
                        getAccountBalance4OTC()
                        for (temp in 0 until payCoinList.size) {
                            if (payCoinList[temp].optString("key") == payCoin) {
                                payCoinSelectItem = temp
                                payCoinName = payCoinList[temp].optString("title")
                                net_coin_type?.setBottomContent(payCoinList[temp].optString("title"))
                                break
                            }
                        }
                    }

                    coinTitleDialog?.dismiss()
                }
            })
        }


        cub_confirm?.isEnable(true)
        /**
         * 总金额
         */
        allAmount = BigDecimalUtils.divForDown("0", payCoinRates).toPlainString()
        ntt_all_amount?.setBottomContent("$allAmount $payCoin")
        ntt_price?.setBottomContent(BigDecimalUtils.divForDown("0", payCoinRates).toPlainString() + " $payCoin")
        /**
         * 广告类型  side
         */
        rg_buy_or_sell_layout?.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_buy -> {
                    if (advertisingType != "BUY") {
                        refreshView()
                    }
                    advertisingType = "BUY"
                    setBuyPayment()
                    tv_complaint_title?.text = getString("otc_payMoney_Method")
                    tv_otc_account?.visibility = View.GONE
                    ntt_advertising_id.normalErrorEdittext(false)
                    tv_otc_account?.setTextColor(ContextCompat.getColor(this@NewReleaseAdvertisingActivity, R.color.normal_text_color))

                }
                R.id.rb_sell -> {
                    if (advertisingType != "SELL") {
                        refreshView()
                    }
                    advertisingType = "SELL"
                    setSellPayment()
                    tv_complaint_title?.text = getString("otc_getMoney_Method")
                    tv_otc_account?.visibility = View.VISIBLE
                    if (number.isNotEmpty()) {
                        if (BigDecimalUtils.compareTo(coinNormal, number) == -1) {
                            ntt_advertising_id.normalErrorEdittext(true)
                            tv_otc_account?.setTextColor(ContextCompat.getColor(this@NewReleaseAdvertisingActivity, R.color.red))
                        } else {
                            ntt_advertising_id.normalErrorEdittext(false)
                            tv_otc_account?.setTextColor(ContextCompat.getColor(this@NewReleaseAdvertisingActivity, R.color.normal_text_color))
                        }
                    } else {
                        ntt_advertising_id.normalErrorEdittext(false)
                        tv_otc_account?.setTextColor(ContextCompat.getColor(this@NewReleaseAdvertisingActivity, R.color.normal_text_color))
                    }
                }
            }
        }

        /**
         * 货币类型
         */

        net_coin_type?.listener = object : NewOTCEditTextView.OTCEditTextListener {
            override fun edittextListener() {
                coinDialog = NewDialogUtils.showBottomListDialog(this@NewReleaseAdvertisingActivity, payCoinNameList, payCoinSelectItem, object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        payCoinSelectItem = item
                        if (payCoin != payCoinList[item].optString("key")) {
                            payCoin = payCoinList.get(item).optString("key")
                            payCoinRates = RateManager.getRatesByPayCoin(payCoin)
                            payCoinName = payCoinList.get(item).optString("title")
                            net_coin_type?.setBottomContent(payCoinName)
                            advertisingType = OTCPublicInfoDataService.getInstance().getDafaultCoin()
                            rb_buy?.isChecked = true
                            rb_sell?.isChecked = false
                            refreshView()
                            custom_price = "0"
                            custom_actual_price = "0"
                            getConsiderPrice()
                            getAccountBalance4OTC()
                        }

                        coinDialog?.dismiss()
                    }
                })
            }
        }

        /**
         * 数量
         */
        ntt_advertising_id?.setRightContent(NCoinManager.getShowMarket(coinSymbol))
        ntt_advertising_id?.setEdittextFilter(RateManager.getFiat4Coin(coinSymbol).toInt())
        ntt_advertising_id?.listener = object : NewOTCTextViewEdittextView.OTCEdittextChangeListener {
            override fun returnEdittextContent(content: String) {
                number = content
                if (number.isNotEmpty()) {
                    ntt_advertising_id?.setErrorEdittext("", false)
                }
                allAmount = BigDecimalUtils.divForDown(BigDecimalUtils.mul(number, custom_actual_price).toPlainString(), payCoinRates).toPlainString()
                ntt_all_amount?.setBottomContent("$allAmount $payCoin")
                if (maxLimit != "0") {
                    setmaxLimit(net_max_limit)
                }
                if (minLimit != "0") {
                    setminLimit(net_minimum_limit)
                }
                if (advertisingType == "SELL") {
                    if (number.isNotEmpty()) {
                        if (BigDecimalUtils.compareTo(coinNormal, number) == -1) {
                            ntt_advertising_id.normalErrorEdittext(true)
                            tv_otc_account?.setTextColor(ContextCompat.getColor(this@NewReleaseAdvertisingActivity, R.color.red))
                        } else {
                            ntt_advertising_id.normalErrorEdittext(false)
                            tv_otc_account?.setTextColor(ContextCompat.getColor(this@NewReleaseAdvertisingActivity, R.color.normal_text_color))
                        }
                    } else {
                        ntt_advertising_id.normalErrorEdittext(false)
                        tv_otc_account?.setTextColor(ContextCompat.getColor(this@NewReleaseAdvertisingActivity, R.color.normal_text_color))
                    }
                }


            }
        }

        /**
         * 市场参考价
         */
        ntt_market_reference_price?.setBottomContent(BigDecimalUtils.divForDown(custom_price, payCoinRates).toPlainString() + " $payCoin")
        ntt_market_reference_price?.listener = object : NewOTCTextView.OTCTextViewClickListener {
            override fun onclickTopImage() {
                NewDialogUtils.showSingleDialog(this@NewReleaseAdvertisingActivity, getString("otc_market_ReferencPriceDetail"), object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {

                    }
                }, cancelTitle = "ok")
            }

            override fun onclickBottomImage() {

            }
        }

        /**
         * 定价方式
         */
        ntt_pricing?.setBottomContent(getString("otc_market_price"))
        ntt_pricing?.listener = object : NewOTCEditTextView.OTCEditTextListener {
            override fun edittextListener() {
                pricingDialog = NewDialogUtils.showBottomListDialog(this@NewReleaseAdvertisingActivity, arrayListOf(getString("otc_market_price"), getString("otc_custom_price")), pricingSelect, object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        pricingSelect = item
                        when (item) {
                            0 -> {
                                net_custom_price?.visibility = View.GONE
                                ntt_premium_direction?.visibility = View.VISIBLE
                                net_premium_percentage?.visibility = View.VISIBLE
                                ntt_price?.visibility = View.VISIBLE
                            }
                            1 -> {
                                pricing = "0"
                                net_custom_price?.visibility = View.VISIBLE
                                ntt_premium_direction?.visibility = View.GONE
                                net_premium_percentage?.visibility = View.GONE
                                ntt_price?.visibility = View.GONE
                                premium_percentage = "0"
                                net_premium_percentage?.setEditTextContent("0")
                            }
                        }
                        custom_actual_price = custom_price
                        allAmount = BigDecimalUtils.divForDown(BigDecimalUtils.mul(number, custom_actual_price).toPlainString(), payCoinRates).toPlainString()
                        ntt_all_amount?.setBottomContent("$allAmount $payCoin")
                        ntt_pricing?.setBottomContent(data[item])
                        pricingDialog?.dismiss()
                    }

                })
            }
        }
        /**
         * 自定义价格
         */
        net_custom_price?.setEdittextFilter(payCoinRates)
        net_custom_price?.setRightContent(payCoin)
        net_custom_price?.listener = object : NewOTCTextViewEdittextView.OTCEdittextChangeListener {
            override fun returnEdittextContent(content: String) {
                custom_price = content
                custom_actual_price = content
                allAmount = BigDecimalUtils.divForDown(BigDecimalUtils.mul(number, custom_actual_price).toPlainString(), payCoinRates).toPlainString()
                ntt_all_amount?.setBottomContent("$allAmount $payCoin")
            }
        }

        /**
         * 溢价方向
         */
        ntt_premium_direction?.setBottomContent(getString("otc_aboveReference_price"))
        ntt_premium_direction?.listener = object : NewOTCEditTextView.OTCEditTextListener {
            override fun edittextListener() {
                premiumDirectionDialog = NewDialogUtils.showBottomListDialog(this@NewReleaseAdvertisingActivity, arrayListOf(getString("otc_aboveReference_price"), getString("otc_belowReference_price")), premiumDirectionSelect, object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        premiumDirectionSelect = item
                        when (item) {
                            0 -> {
                                pricing = "2"
                            }
                            1 -> {
                                pricing = "3"
                            }
                        }
                        ntt_premium_direction?.setBottomContent(data[item])
                        changePrice()
                        premiumDirectionDialog?.dismiss()
                    }
                })
            }
        }
        /**
         * 溢价百分比
         */
        net_premium_percentage?.setEditTextContent("0.00")
        net_premium_percentage?.setEdittextFilter(2)
        net_premium_percentage?.listener = object : NewOTCTextViewEdittextView.OTCEdittextChangeListener {
            override fun returnEdittextContent(content: String) {
                premium_percentage = content



                changePrice()
                allAmount = BigDecimalUtils.divForDown(BigDecimalUtils.mul(number, custom_actual_price).toPlainString(), payCoinRates).toPlainString()
                ntt_all_amount?.setBottomContent("$allAmount $payCoin")

                if (BigDecimalUtils.compareTo(premium_percentage, "50") == 1) {
                    net_premium_percentage?.setErrorEdittext(getString("otc_outPrice_percentMax"), true)
                    return
                } else {
                    net_premium_percentage?.setErrorEdittext("", false)
                }
                if (premium_percentage.isNotEmpty()) {
                    net_premium_percentage?.setErrorEdittext("", false)
                }
            }
        }

        /**
         * 最小限制
         */
        net_minimum_limit?.setRightContent(payCoin)
        net_minimum_limit?.setEdittextFilter(payCoinRates)
        net_minimum_limit?.listener = object : NewOTCTextViewEdittextView.OTCEdittextChangeListener {
            override fun returnEdittextContent(content: String) {
                minLimit = content
                setminLimit(net_minimum_limit)
                if (maxLimit != "0") {
                    setmaxLimit(net_max_limit)
                }
            }
        }

        /**
         * 最大限制
         */
        net_max_limit?.setRightContent(payCoin)
        net_max_limit?.setEdittextFilter(payCoinRates)
        net_max_limit?.listener = object : NewOTCTextViewEdittextView.OTCEdittextChangeListener {
            override fun returnEdittextContent(content: String) {
                maxLimit = content
                setmaxLimit(net_max_limit)
                if (minLimit != "0") {
                    setminLimit(net_minimum_limit)
                }

            }
        }
        /**
         * 付款时限
         */
        net_limit_for_payment?.setEditTextContent("5")
        net_limit_for_payment?.setRightContent(getString("otc_payMoney_timeMinute"))
        net_limit_for_payment?.listener = object : NewOTCTextViewEdittextView.OTCEdittextChangeListener {
            override fun returnEdittextContent(content: String) {
                paymentTime = content
                if (BigDecimalUtils.compareTo(paymentTime, "5") == -1 || BigDecimalUtils.compareTo(paymentTime, "60") == 1) {
                    net_limit_for_payment?.setErrorEdittext(getString("otc_payMoney_timeLimit"), true)
                    return
                }

                if (paymentTime.isNotEmpty()) {
                    net_limit_for_payment?.setErrorEdittext("", false)
                }
            }
        }
        /**
         * 对方最少交易次数
         */
        net_minimum_number_of_transactions?.setEditTextContent("0")
        net_minimum_number_of_transactions?.setRightContent(getString("otc_other_times"))
        net_minimum_number_of_transactions?.listener = object : NewOTCTextViewEdittextView.OTCEdittextChangeListener {
            override fun returnEdittextContent(content: String) {
                transaction_number = content
                if (transaction_number.isNotEmpty()) {
                    net_minimum_number_of_transactions?.setErrorEdittext("", false)
                }
            }
        }


        /**
         * 失效时间
         */
        net_failure_time?.setBottomContent("30${getString("otc_advertiseValid_day")}")
        net_failure_time?.listener = object : NewOTCEditTextView.OTCEditTextListener {
            override fun edittextListener() {
                failureTimeDialog = NewDialogUtils.showBottomListDialog(this@NewReleaseAdvertisingActivity, arrayListOf("2${getString("otc_advertiseValid_day")}", "4${getString("otc_advertiseValid_day")}", "7${getString("otc_advertiseValid_day")}", "30${getString("otc_advertiseValid_day")}"), failureTimeSelect, object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        net_failure_time?.setBottomContent(data[item])

                        failureTimeSelect = item
                        failureTime = failureTimeList[item]
                        failureTimeDialog?.dismiss()
                    }
                })
            }

        }

        /**
         * 自动回复
         */
        net_auto_reply?.listener = object : NewOTCTextViewEdittextView.OTCEdittextChangeListener {
            override fun returnEdittextContent(content: String) {
                autoReply = content

            }
        }
        /**
         * 广告留言
         */
        net_advertising_message?.listener = object : NewOTCTextViewEdittextView.OTCEdittextChangeListener {
            override fun returnEdittextContent(content: String) {
                advertisingMessage = content

            }
        }

        cub_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (BigDecimalUtils.compareTo(number, "0") == 0 || TextUtils.isEmpty(number)) {
                    DisplayUtil.showSnackBar(window?.decorView, getString("otc_mustWrite_tex"), isSuc = false)
                    ntt_advertising_id?.setErrorEdittext(getString("otc_MustWrite"), true)
                    return
                }
                if (pricingSelect != 0 && BigDecimalUtils.compareTo(custom_price, "0") == 0) {
                    DisplayUtil.showSnackBar(window?.decorView, getString("otc_mustWrite_tex"), isSuc = false)
                    net_custom_price?.setErrorEdittext(getString("otc_MustWrite"), true)
                    return
                }
                if (BigDecimalUtils.compareTo(minLimit, "0") == 0 || TextUtils.isEmpty(minLimit) || !minlimitStatus) {
                    if (BigDecimalUtils.compareTo(minLimit, "0") == 0 || TextUtils.isEmpty(minLimit)) {
                        DisplayUtil.showSnackBar(window?.decorView, getString("otc_mustWrite_tex"), isSuc = false)
                        net_minimum_limit?.setErrorEdittext(getString("otc_MustWrite"), true)
                    }
                    return
                }
                if (BigDecimalUtils.compareTo(maxLimit, "0") == 0 || TextUtils.isEmpty(maxLimit) || !maxlimitStatus) {
                    if (BigDecimalUtils.compareTo(maxLimit, "0") == 0 || TextUtils.isEmpty(maxLimit)) {
                        DisplayUtil.showSnackBar(window?.decorView, getString("otc_mustWrite_tex"), isSuc = false)
                        net_max_limit?.setErrorEdittext(getString("otc_MustWrite"), true)
                    }
                    return
                }
                if (premium_percentage.isEmpty()) {
                    DisplayUtil.showSnackBar(window?.decorView, getString("otc_mustWrite_tex"), isSuc = false)
                    net_premium_percentage?.setErrorEdittext(getString("otc_MustWrite"), true)
                    return
                }
                if (paymentTime.isEmpty()) {
                    DisplayUtil.showSnackBar(window?.decorView, getString("otc_mustWrite_tex"), isSuc = false)
                    net_limit_for_payment?.setErrorEdittext(getString("otc_MustWrite"), true)
                    return
                }
                if (transaction_number.isEmpty()) {
                    DisplayUtil.showSnackBar(window?.decorView, getString("otc_mustWrite_tex"), isSuc = false)
                    net_minimum_number_of_transactions?.setErrorEdittext(getString("otc_MustWrite"), true)
                    return
                }
                if (paymentsList.size == 0) {
                    if (advertisingType == "BUY") {
                        DisplayUtil.showSnackBar(window?.decorView, getString("otc_choose_payMoneyMethod"), isSuc = false)
                    } else {
                        DisplayUtil.showSnackBar(window?.decorView, getString("otc_choose_getMoneyMethod"), isSuc = false)
                    }

                    return
                }

                setWantedSave()

            }

        }


    }

    fun changePrice() {
        var temp = BigDecimalUtils.div(premium_percentage, "100").toPlainString()
        if (pricing == "2") {
            custom_actual_price = BigDecimalUtils.divForDown(BigDecimalUtils.mul(BigDecimalUtils.add("1", temp).toPlainString(), custom_price).toPlainString(), payCoinRates).toPlainString()
            ntt_price?.setBottomContent("$custom_actual_price $payCoin")
        } else if (pricing == "3") {
            custom_actual_price = BigDecimalUtils.divForDown(BigDecimalUtils.mul(BigDecimalUtils.sub("1", temp).toPlainString(), custom_price).toPlainString(), payCoinRates).toPlainString()
            ntt_price?.setBottomContent(custom_actual_price + " $payCoin")
        }
    }

    var coinNormal = "0"

    /**
     * 获取账户信息  法币
     */
    private fun getAccountBalance4OTC() {

        addDisposable(getMainModel().otc_account_list(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var t: JSONObject? = jsonObject.optJSONObject("data") ?: return
                var allCoinMap = t!!.optJSONArray("allCoinMap")
                if (null == allCoinMap) return
                for (num in 0 until allCoinMap.length()) {
                    var it = allCoinMap.optJSONObject(num)
                    if (it.optString("coinSymbol") == coinSymbol) {

                        var normal = BigDecimalUtils.divForDown(it.optString("normal"), RateManager.getFiat4Coin(it?.optString("coinSymbol").toString()).toInt()).toPlainString()
                        var rate = NCoinManager.getFeeOtc4Advertising(coinSymbol)
                        coinNormal = BigDecimalUtils.sub(normal, rate.toString()).toPlainString()

                        tv_otc_account?.text = "${getString("otc_out_assetsAvalaible")} $coinNormal${NCoinManager.getShowMarket(it?.optString("coinSymbol"))}"
                        return
                    }
                }


            }
        }))

    }

    /**
     * 发布广告
     */
    private fun setWantedSave() {

        addDisposable(getOTCModel().setWantedSave(coinSymbol, advertisingType, payCoin, number,
                custom_actual_price, premium_percentage, pricing, minLimit,
                maxLimit, paymentTime, transaction_number, failureTime, getPayments(), advertisingMessage, autoReply, object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                ToastUtils.showToast(getString("otc_advertisePublish_success"))
                finish()
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(mActivity,false,msg)
            }

        }))
    }

    private fun getPayments(): ArrayList<JSONObject> {
        var payments = ArrayList<JSONObject>()
        paymentsList.forEach {
            var key = it?.optString("key")
            if (!StringUtil.checkStr(key)) {
                key = it?.optString("payment")
            }
            var jsonObject = JSONObject()
            jsonObject.put("payment", key)
            payments.add(jsonObject)

        }
        return payments
    }

    /**
     * 获取参考价
     */
    fun getConsiderPrice() {

        addDisposable(getOTCModel().considerPrice(coinSymbol, payCoin, object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data")
                var referencePrice = data?.optString("referencePrice")
                var precision = RateManager.getFiat4Coin(payCoin)
                custom_price = BigDecimalUtils.divForDown(referencePrice, precision).toPlainString()
                custom_actual_price = custom_price
                ntt_market_reference_price?.setBottomContent("$custom_actual_price${payCoin}")
                ntt_price?.setBottomContent("$custom_actual_price${payCoin}")
            }
        }))

    }

    var beans = arrayListOf<JSONObject>()
    /**
     * 获取支付方式
     */
    private var myPayments: ArrayList<JSONObject>? = null

    private fun getUserPayment4OTC() {
        if (UserDataService.getInstance().isLogined) {
            addDisposable(getOTCModel().getUserPayment4OTC(consumer = object : NDisposableObserver() {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    if (null != jsonObject) {
                        myPayments = JSONUtil.arrayToList(jsonObject.optJSONArray("data"))

                        if (advertisingType == "BUY") {
                            setBuyPayment()
                        } else {
                            if (null == myPayments || myPayments!!.isEmpty()) {
                                tv_no_payments?.visibility = View.VISIBLE
                                rg_complaint_layout?.visibility = View.GONE
                            } else {
                                setSellPayment()
                            }
                        }

                    }
                }

            }))
        }
    }


    fun setSellPayment() {
        beans?.clear()
        if (null != myPayments) {
            beans?.addAll(myPayments!!)
        }
        showExchiefPayMethod(true)
        LogUtil.d(TAG, "setSellPayment==beans is $beans")
    }

    fun setBuyPayment() {
        var payments = OTCPublicInfoDataService.getInstance().payments
        beans.clear()
        if (null != payments) {
            beans.addAll(payments)
        }
        showExchiefPayMethod(false)
        LogUtil.d(TAG, "setSellPayment==beans is $beans")
    }

    /*
     * 显示非洲定制化需求 支付方式
     */
    private var hasAddBank = false

    private fun showExchiefPayMethod(isSell: Boolean) {
        paymentsList.clear()
        hasAddBank = false
        var paymentList: ArrayList<JSONObject> = arrayListOf()
        if (isSell) {
            beans?.forEach {
                if (isSell) {
                    if (1 == it.optInt("isOpen")) {
                        paymentList.add(it)
                    }

                } else {
                    paymentList.add(it)
                }
            }
        } else {
            paymentList.addAll(beans)
        }
        adapter?.setList(paymentList)

    }


    private fun addOrRemovePaymethod(checkbox: CheckBox, isChecked: Boolean, payment: JSONObject) {

        if (isChecked) {
            if (paymentsList.size >= 3) {
                checkbox.isChecked = false
                NToastUtil.showTopToastNet(mActivity,false, getString(R.string.otc_getMoney_Method))
                return
            }
            if (!paymentsList.contains(payment)) {
                paymentsList.add(payment)
            }
        } else {
            if (paymentsList.contains(payment)) {
                paymentsList.remove(payment)
            }
        }
    }

    var minlimitStatus: Boolean = false
    var maxlimitStatus: Boolean = false
    fun setminLimit(view: NewOTCTextViewEdittextView) {
        if (minLimit == "0" || TextUtils.isEmpty(minLimit)) return
        var munAmount = BigDecimalUtils.compareTo(minLimit, allAmount)
        if (munAmount == 1) {
            minlimitStatus = false
            view?.setErrorEdittext(getString("otc_min_smallTotal"), true)
            return
        } else {
            minlimitStatus = true
            view?.setErrorEdittext("", false)
        }
        var numMax = BigDecimalUtils.compareTo(minLimit, maxLimit)
        if (numMax == 1 || numMax == 0) {
            minlimitStatus = false
            view?.setErrorEdittext(getString("otc_min_smallMaxLimit"), true)
        } else {
            minlimitStatus = true
            view?.setErrorEdittext("", false)
        }


    }

    fun setmaxLimit(view: NewOTCTextViewEdittextView) {
        if (maxLimit == "0" || TextUtils.isEmpty(maxLimit)) return
        var munAmount = BigDecimalUtils.compareTo(maxLimit, allAmount)
        if (munAmount == 1) {
            maxlimitStatus = false
            view?.setErrorEdittext(getString("otc_maxLimit_smallTotal"), true)
            return
        } else {
            maxlimitStatus = true
            view?.setErrorEdittext("", false)
        }
        var numMax = BigDecimalUtils.compareTo(maxLimit, minLimit)
        if (numMax == -1 || numMax == 0) {
            maxlimitStatus = false
            view?.setErrorEdittext(getString("otc_MaxLimit_bigMinLimit"), true)
        } else {
            maxlimitStatus = true
            view?.setErrorEdittext("", false)
        }
    }

    fun refreshView() {
        number = "0"
        pricing = "2"
        premium_percentage = "0"
        allAmount = "0"
        maxLimit = "0"
        minLimit = "0"
        paymentTime = "5"
        transaction_number = "0"
        failureTime = "30"
        failureTimeSelect = 3
        pricingSelect = 0
        premiumDirectionSelect = 0
        tv_coin_symbol?.text = NCoinManager.getShowMarket(coinSymbol)
        allAmount = BigDecimalUtils.divForDown("0", payCoinRates).toPlainString()
        ntt_advertising_id?.setEditTextContent("")
        ntt_advertising_id?.setRightContent(NCoinManager.getShowMarket(coinSymbol))
        ntt_advertising_id?.setEdittextFilter(RateManager.getFiat4Coin(coinSymbol))
        ntt_pricing?.setBottomContent(getString("otc_market_price"))
        ntt_premium_direction?.setBottomContent(getString("otc_aboveReference_price"))
        net_premium_percentage?.setEditTextContent("0")
        ntt_all_amount?.setBottomContent("$allAmount $payCoin")
        net_minimum_limit?.setEditTextContent("")
        net_max_limit?.setEditTextContent("")
        net_minimum_number_of_transactions?.setEditTextContent("0")
        net_limit_for_payment?.setEditTextContent("5")
        net_failure_time?.setBottomContent("30${getString("otc_advertiseValid_day")}")
        paymentsList?.clear()
        net_advertising_message?.setEditTextContent("")
        autoReply = ""
        advertisingMessage = ""
        net_auto_reply?.setEditTextContent("")

    }

    fun getString(content: String): String {
        return LanguageUtil.getString(this@NewReleaseAdvertisingActivity, content)
    }


}