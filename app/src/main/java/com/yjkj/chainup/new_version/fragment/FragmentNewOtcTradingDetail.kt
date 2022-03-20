package com.yjkj.chainup.new_version.fragment

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.yjkj.chainup.util.JsonUtils
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.bean.OTCInitInfoBean
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.OTCPublicInfoDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.activity.otcTrading.NewVersionOTCBuyActivity
import com.yjkj.chainup.new_version.activity.otcTrading.NewVersionOTCSellActivity
import com.yjkj.chainup.new_version.adapter.NewOTCTradingAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.new_version.view.OTCAdvertisingListener
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.LineAdapter4FundsLayout
import com.yjkj.chainup.wedegit.recycler.LinearTryLayoutManager
import kotlinx.android.synthetic.main.fragment_new_otc_trading_detail.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/4/3-12:20 PM
 * @Email buptjinlong@163.com
 * @description 法币交易 列表页
 */
const val NUMBERCODE = "isBlockTrade"
const val PAYCOIN = "payCoin"
const val PAYMENT = "payment"
const val TRADING_TYPE_BUY = "BUY"
const val TRADING_TYPE_SELL = "SELL"

class FragmentNewOtcTradingDetail : NBaseFragment(), OTCAdvertisingListener {

    override fun setContentView(): Int {
        return R.layout.fragment_new_otc_trading_detail
    }

    override fun loadData() {
        arguments?.let {
            marketName = it.getString(MARKET_NAME) ?: ""
            numberCode = it.getString(NUMBERCODE) ?: ""
            payCoin = it.getString(PAYCOIN) ?: ""
            payment = it.getString(PAYMENT) ?: ""
            curIndex = it.getInt(CUR_INDEX)
            curType = it.getInt(CUR_TYPE)
        }

        coinTitles = PublicInfoDataService.getInstance().coinArray

        getData(false)
    }


    /**
     *  所有的法币类型
     */
    var payCoinsForTrading: ArrayList<JSONObject>? = arrayListOf()

    /**
     *  初始化需要显示的烈性
     */
    var showPayCoinForTradingList: ArrayList<JSONObject>? = arrayListOf()

    var hidePayCoinForTradingList: ArrayList<JSONObject>? = arrayListOf()

    var showHide = false


    override fun onResume() {
        super.onResume()
        getUserPayment4OTC()
    }

    override fun initView() {

        setOnclick()
        val currencyTypeTitle = if (isAdded) {
            if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
                LanguageUtil.getString(context, "filter_fold_currencyType_forotc")
            } else {
                LanguageUtil.getString(context, "filter_fold_currencyType")
            }
        } else {
            ""
        }
        val tvOrderPriceType = headerView?.findViewById<TextView>(R.id.tv_order_title_price_type)
        val tvChangeTrading = headerView?.findViewById<TextView>(R.id.tv_order_change_trading)
        val faitTitle = headerView?.findViewById<LineAdapter4FundsLayout>(R.id.ll_fait_title)
        val rlPriceLayout = headerView?.findViewById<RelativeLayout>(R.id.rl_order_title_price_layout)

        tvOrderPriceType?.text = currencyTypeTitle

        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))
        var list = OTCPublicInfoDataService.getInstance().payments
        if (null != list) {
            payCoinsForTrading?.addAll(list)
        }

        if (payCoinsForTrading?.isEmpty() ?: return) {
            return
        }
        payCoinsForTrading?.forEach {
            if (it.optBoolean("hide")) {
                showPayCoinForTradingList?.add(it)
            } else {
                hidePayCoinForTradingList?.add(it)
            }
        }
        OTCInitInfoBean.Paycoins("1", LanguageUtil.getString(context, "common_action_showMore"), "", false, true, false)

        payCoinsForTrading?.clear()
        payCoinsForTrading?.addAll(hidePayCoinForTradingList ?: arrayListOf())
        payCoinsForTrading?.addAll(showPayCoinForTradingList ?: arrayListOf())
        if (showPayCoinForTradingList?.size ?: 0 > 0) {
            showHide = true
            if (hidePayCoinForTradingList?.size ?: 0 < payCoinsForTrading?.size ?: 0) {
                hidePayCoinForTradingList?.add(JSONObject("{\"key\":\"1\",\"title\":${LanguageUtil.getString(context, "common_action_showMore")},\"icon\":\"\",\"account\":null,\"used\":false,\"numberCode\":null,\"open\":false,\"hide\":true}"))
                payCoinsForTrading?.add(JSONObject("{\"key\":\"1\",\"title\":${LanguageUtil.getString(context, "common_action_hideMore")},\"icon\":\"\",\"account\":null,\"used\":false,\"numberCode\":null,\"open\":false,\"hide\":true}"))
            }
            payCoinsForTrading?.forEach {
                if (it.optString("key") == payCoin) {
                    tvChangeTrading?.text = it.optString("title")
                }
            }
            faitTitle?.visibility = View.VISIBLE
            rlPriceLayout?.visibility = View.VISIBLE
        } else {
            faitTitle?.visibility = View.GONE
            rlPriceLayout?.visibility = View.GONE
        }

        if (showHide) {
            faitTitle?.setPaycoinsBeanData(hidePayCoinForTradingList
                    ?: arrayListOf(), false, true)
        }

        setFaitChickListener(faitTitle, tvChangeTrading)

    }


    override fun setOTCClick(bean: JSONObject) {
        if (LoginManager.checkLogin(activity, true)) {
            var advertId = bean?.optString("advertId")
            var payCoin = bean?.optString("payCoin")
            getValidateAdvert(advertId, if (curType == 0) "buy" else "sell", payCoin, bean)
        }


    }


    private var marketName = ""
    private var payCoin: String = "CNY"
    private var payment: String = ""
    private var numberCode: String = ""
    private var isBlockTrade: String = "0"
    private var curIndex: Int = 0
    private var curType: Int = 0
    var adapter: NewOTCTradingAdapter? = null
    var list: ArrayList<JSONObject> = arrayListOf()

    var headerView: View? = null
    val pageSize = 20
    var page = 1
    var isScrollStatus = true
    var isShowMorelist = true

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: Int, param3: Int, numbercode: String, payCoin: String, payment: String) =
                FragmentNewOtcTradingDetail().apply {
                    arguments = Bundle().apply {
                        putString(MARKET_NAME, param1)
                        putString(NUMBERCODE, numbercode)
                        putString(PAYCOIN, payCoin)
                        putString(PAYMENT, payment)
                        putInt(CUR_TYPE, param3)
                        putInt(CUR_INDEX, param2)
                    }
                }
    }

    fun setFaitChickListener(faitTitle: LineAdapter4FundsLayout?, tvChangeTrading: TextView?) {
        /**
         * 法币类型 返回值
         */
        faitTitle?.setLineSelectOncilckListener(object : LineSelectOnclickListener {
            override fun selectMsgIndex(index: String?) {
                page = 1
                if (showHide) {
                    if (isShowMorelist) {
                        if (index == "1") {
                            isShowMorelist = !isShowMorelist
                            faitTitle.setPaycoinsBeanData(payCoinsForTrading
                                    ?: arrayListOf(), false, true)
                        } else {
                            payCoin = index ?: "CNY"
                            hidePayCoinForTradingList?.forEach {
                                if (it.optString("key") == index) {
                                    tvChangeTrading?.text = it.optString("title")
                                }
                            }

                        }
                    } else {
                        if (index == "1") {
                            isShowMorelist = !isShowMorelist
                            faitTitle.setPaycoinsBeanData(hidePayCoinForTradingList
                                    ?: arrayListOf(), false, true)
                        } else {
                            payCoin = index ?: "CNY"
                            payCoinsForTrading?.forEach {
                                if (it.optString("key") == index) {
                                    tvChangeTrading?.text = it.optString("title")
                                }
                            }
                        }
                    }
                } else {
                    payCoin = index ?: "CNY"
                    payCoinsForTrading?.forEach {
                        if (it.optString("key") == index) {
                            tvChangeTrading?.text = it.optString("title")
                        }
                    }
                }
                getData(false)
            }

            override fun sendOnclickMsg() {
            }
        })
    }

    fun getData(status: Boolean) {
        if (!status) page = 1
        getMainSearch(if (curType == 0) TRADING_TYPE_BUY else TRADING_TYPE_SELL, marketName, isBlockTrade, payCoin, payment, "", numberCode, pageSize, page, status)
    }

    fun refreshData(datas: ArrayList<JSONObject>) {
        list.addAll(datas)
        adapter?.setCoinName(marketName)
        adapter?.notifyDataSetChanged()
    }

    /**
     * 先选的币对
     */
    private var coinTitles: ArrayList<String> = arrayListOf()

    /**
     * showName
     */
    private var coinShowNameTitles: ArrayList<String> = arrayListOf()

    /**
     * 选择的币对
     */
    var selectItem = 0

    var coinTitleDialog: TDialog? = null

    /**
     * * @param trading 返回交易类型
     * @param amount 返回目标金额
     * @param fiatType 返回法币类型
     * @param paymentType 返回支付方式
     * @param countries 返回国家
     */

    fun refreshForOTHHome(status: String, trading: String = "", amount: String = "", fiatType: String = "", paymentType: String = "", countries: String = "") {
        isFristGetData = true
        payment = paymentType
        payCoin = fiatType
        isBlockTrade = trading
        numberCode = countries
        getMainSearch(status, marketName, isBlockTrade, payCoin, payment, "", numberCode, pageSize, 1, false, amount)
    }

    fun setOnclick() {
        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            adapter?.loadMoreModule?.isEnableLoadMore = false
            getData(false)
        }

        adapter = NewOTCTradingAdapter(list, marketName, this)
        adapter?.loadMoreModule?.isEnableLoadMore = false
        adapter?.headerWithEmptyEnable = true
        recycler_view_buy?.layoutManager = LinearTryLayoutManager(context)
        headerView = LayoutInflater.from(mActivity).inflate(R.layout.header_new_otc_trading_detail, recycler_view_buy, false)
        adapter?.addHeaderView(headerView!!)
        adapter?.loadMoreModule?.setOnLoadMoreListener {
            LogUtil.e("LogUtils", "loadMore()")
            getData(true)
        }
        recycler_view_buy?.isNestedScrollingEnabled = false
        recycler_view_buy?.adapter = adapter
        val tvChooseSymbol = headerView?.findViewById<TextView>(R.id.tv_choose_symbol)
        val llChooseSymbolLayout = headerView?.findViewById<LinearLayout>(R.id.ll_choose_symbol_layout)

        tvChooseSymbol?.text = NCoinManager.getShowMarket(marketName)
        llChooseSymbolLayout?.setOnClickListener {
            coinTitles.clear()
            coinTitles.addAll(NCoinManager.getMarkets4OTC())
            coinShowNameTitles.clear()
            coinShowNameTitles.addAll(NCoinManager.getMarketsShowName4OTC())
            for (num in 0 until coinTitles.size) {
                if (marketName == coinTitles[num]) {
                    selectItem = num
                }
            }
            coinTitleDialog = NewDialogUtils.showBottomListDialog(context!!, coinShowNameTitles, selectItem, object : NewDialogUtils.DialogOnclickListener {
                override fun clickItem(data: ArrayList<String>, item: Int) {
                    selectItem = item
                    marketName = coinTitles[selectItem]
                    tvChooseSymbol?.text = NCoinManager.getShowMarket(marketName)
                    getData(false)
                    coinTitleDialog?.dismiss()
                }
            })
        }

//        val tvReferencePrice = headerView?.findViewById<TextView>(R.id.tv_reference_price)
//        val ivReferencePrice = headerView?.findViewById<ImageView>(R.id.iv_reference_price)
//
//        tvReferencePrice?.text = LanguageUtil.getString(context, "otc_text_rp")
//        tvReferencePrice?.setOnClickListener {
//            NewDialogUtils.showSingleDialog(context!!, LanguageUtil.getString(context, "alert_content_otcRPdesc"), object : NewDialogUtils.DialogBottomListener {
//                override fun sendConfirm() {
//
//                }
//            }, "", LanguageUtil.getString(context, "alert_common_iknow"))
//
//        }
//        ivReferencePrice?.setOnClickListener {
//            NewDialogUtils.showSingleDialog(context!!, LanguageUtil.getString(context, "alert_content_otcRPdesc"), object : NewDialogUtils.DialogBottomListener {
//                override fun sendConfirm() {
//
//                }
//            }, "", LanguageUtil.getString(context, "alert_common_iknow"))
//        }


    }


    fun getAdverListData(data: JSONArray?): ArrayList<JSONObject> {
        if (data == null || data.length() <= 0) {
            return arrayListOf()
        }
        var adverList: ArrayList<JSONObject> = arrayListOf()
        for (num in 0 until data.length()) {
            adverList.add(data.optJSONObject(num))

        }
        return adverList
    }


    var isFristGetData = true

    /**
     * 获取广告
     *
     * @param side 交易类型（出售 OR 求购
     * @param symbol 交易币种
     * @param isBlockTrade 是否是大宗交易，默认0
     * @param payCoin 支付币种
     * @param payment 支付方式
     * @param sort    排序方式
     * @param numberCode 国家数字编码
     * @param pageSize    页大小
     * @param price    页大小
     * @param page 页号
     */
    fun getMainSearch(side: String, symbol: String, isBlockTrade: String, payCoin: String, payment: String,
                      sort: String, numberCode: String, pageSize: Int, page: Int, refresh: Boolean, price: String = "") {
//        getConsiderPrice()
        if (isFristGetData) {
            showLoadingDialog()
            isFristGetData = false
        }
        addDisposable(getOTCModel().getmainSearch4OTC(side, symbol, isBlockTrade, payCoin, payment, sort, numberCode, pageSize, page, price, object : NDisposableObserver() {
            override fun onResponseSuccess(data: JSONObject) {
                closeLoadingDialog()

                var json = data.optJSONObject("data")
                var advertList = getAdverListData(json?.optJSONArray("advertList"))
                initData(refresh, advertList)

            }


            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                closeLoadingDialog()
                swipe_refresh?.isRefreshing = false
                list.clear()
                adapter?.notifyDataSetChanged()
                NToastUtil.showTopToastNet(this@FragmentNewOtcTradingDetail.mActivity,false, msg)
            }


        }))
    }


    fun initData(isMoreRef: Boolean, list: ArrayList<JSONObject>) {
        if (activity?.isFinishing ?: return || activity?.isDestroyed ?: return || !isAdded) {
            return
        }
        adapter?.setCoinName(marketName)
        if (list.isNotEmpty()) {
            if (!isMoreRef) {
                adapter?.setList(list)
            } else {
                adapter?.addData(list)
            }
            val isMore = list.size == pageSize
            if (isMore) page++
            adapter?.apply {
                adapter?.loadMoreModule?.isEnableLoadMore = true
                if (!isMore) {
                    adapter?.loadMoreModule?.loadMoreEnd(!isMoreRef)
                } else {
                    adapter?.loadMoreModule?.loadMoreComplete()
                }
            }

        } else {
            if (!isMoreRef) adapter?.setList(null)
            if (!isMoreRef) adapter?.setEmptyView(EmptyForAdapterView(context ?: return))
            adapter?.loadMoreModule?.loadMoreEnd(isMoreRef)
        }
        swipe_refresh?.isRefreshing = false


    }

//
//    /**
//     * 获取参考价
//     */
//    fun getConsiderPrice() {
//        addDisposable(getOTCModel().considerPrice(marketName, payCoin, object : NDisposableObserver() {
//            override fun onResponseSuccess(data: JSONObject) {
//                var json = data.optJSONObject("data")
//                var paySymbol = json?.optString("paySymbol")
//                var referencePrice = json?.optString("referencePrice")
//
//                var precision = RateManager.getFiat4Coin(paySymbol ?: "$")
//                var showReferencePrice = BigDecimalUtils.divForDown(referencePrice, precision).toPlainString()
//
//                val tvReferencePrice = headerView?.findViewById<TextView>(R.id.tv_reference_price)
//                tvReferencePrice?.apply {
//                    text = LanguageUtil.getString(context, "otc_text_rp") + "：$paySymbol$showReferencePrice"
//                }
//            }
//        }))
//
//
//    }

    /**
     * 购买出售前验证（app4.0）
     */
    fun getValidateAdvert(id: String?, advertType: String?, coin: String?, item: JSONObject?) {
        if (!LoginManager.checkLogin(activity, true)) {
            return
        }
        if (advertType == "sell") {
            if (JsonUtils.getCertification(activity)) {
                LogUtil.d(TAG, "getValidateAdvert==beans is $beans")
                LogUtil.d(TAG, "getValidateAdvert==item is $item")

                if (UserDataService.getInstance().isCapitalPwordSet != 1 || beans.size == 0) {
                    NewDialogUtils.OTCTradingSecurityDialog(context!!, object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            if (UserDataService.getInstance().isCapitalPwordSet != 1) {
                                ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            } else {
                                ArouterUtil.greenChannel(RoutePath.PaymentMethodActivity, null)
                            }
                        }
                    }, beans.size != 0)
                    return
                } else {
                    if (showFiatPaymentDialog(item)) {
                        return
                    }
                }
            } else {
                return
            }
        }
        addDisposable(getOTCModel().getValidateAdvert(id, advertType, MyNDisposableObserver(id, advertType)))
    }

    private fun showFiatPaymentDialog(item: JSONObject?): Boolean {
        if (beans.size <= 0)
            return false
        var payments = item?.optJSONArray("payments")

        if (null != payments && payments.length() > 0) {
            var match = false
            for (i in 0 until payments.length()) {
                var key = payments.optJSONObject(i)?.optString("key") ?: ""
                for (j in 0 until beans.size) {
                    var payment = beans[j].optString("payment") ?: ""
                    if (StringUtil.checkStr(payment) && payment == key) {
                        var isOpen = beans[j]?.optInt("isOpen") ?: 0
                        if (1 == isOpen) {
                            match = true
                        }
                    }
                }
            }
            if (!match) {
                NewDialogUtils.activationPaymentMethodDialog(activity, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        ArouterUtil.greenChannel(RoutePath.PaymentMethodActivity, null)
                    }
                }, payments)
                return true
            }
        }
        return false
    }

    inner class MyNDisposableObserver(var id: String?, var advertType: String?) : NDisposableObserver(activity, false) {
        override fun onResponseSuccess(jsonObject: JSONObject) {
            var nId = 0
            if (StringUtil.isNumeric(id))
                nId = id!!.toInt()

            if (advertType == "buy") {
                NewVersionOTCBuyActivity.enter2(context!!, nId)
            } else {
                NewVersionOTCSellActivity.enter2(context!!, nId)
            }
        }


        override fun onResponseFailure(code: Int, msg: String?) {
            if (advertType == "buy") {
                if (code == 2074 || code == 2055) {
                    NewDialogUtils.OTCTradingNickeSecurityDialog(context!!, object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            if (code == 2074) {
                                ArouterUtil.greenChannel(RoutePath.PersonalInfoActivity, null)
                            } else if (code == 2055) {
                                when (UserDataService.getInstance().authLevel) {
                                    0 -> {
                                        ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
                                    }

                                    2, 3 -> {
                                        ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                    }
                                }
                            } else {
                                ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            }
                        }
                    })
                    return
                } else if (code == 2079) {
                    NewDialogUtils.showNormalDialog(context!!, msg
                            ?: "", object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {

                        }
                    }, "", LanguageUtil.getString(context, "alert_common_iknow"))
                    return
                } else if (code == 2069) {
                    NewDialogUtils.showSingle2Dialog(context!!, msg
                            ?: "", object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            if (LoginManager.checkLogin(context, true)) {
                                ArouterUtil.greenChannel(RoutePath.NewOTCOrdersActivity, null)
                            }

                        }
                    }, "", LanguageUtil.getString(context, "alert_action_toDealWith"))
                    return
                } else if (code != -1) {
                    NToastUtil.showTopToastNet(this.mActivity,false, msg)
                }
            } else {
                if (code == 2001 || code == 2056) {
                    NewDialogUtils.OTCTradingSecurityDialog(context!!, object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            if (code == 2001) {
                                ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            } else {
                                ArouterUtil.greenChannel(RoutePath.PaymentMethodActivity, null)
                            }

                        }
                    }, beans.size != 0)
                    return
                } else if (code == 2078) {
                    NewDialogUtils.showNormalDialog(context!!, msg
                            ?: "", object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, marketName)
                        }
                    }, "", LanguageUtil.getString(context, "alert_action_toTransfer"), LanguageUtil.getString(context, "common_text_btnCancel"))
                    return
                } else if (code == 2069) {
                    NewDialogUtils.showSingle2Dialog(context!!, msg
                            ?: "", object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            if (LoginManager.checkLogin(context, true)) {
                                ArouterUtil.greenChannel(RoutePath.NewOTCOrdersActivity, null)
                            }

                        }
                    }, "", LanguageUtil.getString(context, "alert_action_toDealWith"))
                    return
                } else if (code != -1) {
                    NToastUtil.showTopToastNet(this.mActivity,false, msg)
                }
            }
        }
    }


    var beans: ArrayList<JSONObject> = arrayListOf()


    /**
     * 获取支付方式
     */
    private fun getUserPayment4OTC() {
        if (UserDataService.getInstance().isLogined) {
            addDisposable(getOTCModel().getUserPayment4OTC(consumer = object : NDisposableObserver() {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    if (null != jsonObject) {
                        beans.clear()
                        var json = jsonObject.optJSONArray("data")
                        for (num in 0 until json.length()) {
                            beans.add(json.optJSONObject(num))
                        }
                    }
                }

            }))
        }
    }

    /**
     * 获取 eventBus 从我的资产页面过来跳转 对应币对
     */
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (event.msg_type == MessageEvent.fait_trading) {
            if (null != event.msg_content) {
                var json = event.msg_content as JSONObject
                marketName = json.optString("coin")
                val tvChooseSymbol = headerView?.findViewById<TextView>(R.id.tv_choose_symbol)
                tvChooseSymbol?.apply {
                    text = NCoinManager.getShowMarket(marketName)
                }
                selectItem = coinTitles.indexOf(marketName)
                getMainSearch(if (curType == 0) TRADING_TYPE_BUY else TRADING_TYPE_SELL, marketName, isBlockTrade, payCoin, payment, "", numberCode, pageSize, page, false)
            }
        }

    }


}