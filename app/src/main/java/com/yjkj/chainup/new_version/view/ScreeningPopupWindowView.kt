package com.yjkj.chainup.new_version.view

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RelativeLayout
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.OTCPublicInfoDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.manager.Contract2PublicInfoManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.bean.CashFlowSceneBean
import com.yjkj.chainup.treaty.bean.ContractSceneList
import com.yjkj.chainup.util.KeyBoardUtils
import com.yjkj.chainup.util.LineSelectOnclickListener
import com.yjkj.chainup.wedegit.DisplayUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_asset_top_up.view.*
import kotlinx.android.synthetic.main.item_commissioned_by_history.view.*
import kotlinx.android.synthetic.main.item_contract_bill_screening.view.*
import kotlinx.android.synthetic.main.item_history_screening_4_contract.view.*
import kotlinx.android.synthetic.main.item_lever_asset_history.view.*
import kotlinx.android.synthetic.main.item_lever_transfer_history.view.*
import kotlinx.android.synthetic.main.item_mail_screening.view.*
import kotlinx.android.synthetic.main.item_order_history.view.*
import kotlinx.android.synthetic.main.item_otc_fund_pwd_layout.view.*
import kotlinx.android.synthetic.main.item_otc_trading_layout.view.*
import kotlinx.android.synthetic.main.item_otc_transfer.view.*
import kotlinx.android.synthetic.main.item_protocolpos_record_layout.view.*
import kotlinx.android.synthetic.main.item_screen_cashflow_b2c.view.*
import kotlinx.android.synthetic.main.item_screening_popup_window.view.*
import kotlinx.android.synthetic.main.item_withdraw_record.view.*
import kotlinx.android.synthetic.main.sl_item_contract_transfer_history.view.*
import org.json.JSONObject


/**
 * @Author lianshangljl
 * @Date 2019/4/9-2:45 PM
 * @Email buptjinlong@163.com
 * @description
 */
/**
 * ????????????
 */
const val COMMISSIONED = 0
/**
 * ??????????????????
 */
const val TRADING = 1
/**
 * ????????????
 */
const val TRANSFER = 2
/**
 * ????????????
 */
const val OTCFUNDFLOWINGWATER = 3
/**
 * ????????????
 */
const val OTCORDER = 4
/**
 * ????????????
 */
const val ASSETTOPUP = 5
/**
 * ?????????
 */
const val MAILSCREENING = 6

/**
 * ????????????
 */
const val WITHDRAWTYPE = 7

/**
 * ??????????????????
 */
const val HISTORYSCREENINGCONTRACT = 8
/**
 * ????????????
 */
const val CONTRACTBILL = 9

/**
 * B2C???????????????
 */
const val CASHFLOW_B2C = 10


/**
 * ?????? ????????????
 */
const val LEVER_ASSET_CASHFLOW = 11
/**
 * ?????? ????????????
 */
const val LEVER_TRANSFER_HISTORY = 12

/**
 * ?????? ????????????(??????)
 */
const val CONTRACT_TRANSFER_HISTORY = 13



/**
 * ????????????
 */
const val TRANSFER_RECORD = 14



/**
 * ??????PoS??????
 */
const val MYPOSRECORD = 14


class ScreeningPopupWindowView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    val TAG = ScreeningPopupWindowView::class.java.simpleName

    /**
     * ????????????
     */
    var tradingAmount = ""
    /**
     * ???????????? 0 ???????????? 1????????????
     */
    var tradingtypeForTrading = 0
    /**
     * ????????????????????????
     */
    var faitStatus = false
    /**
     * ????????????????????????
     */
    var paymentStatus = false
    /**
     *  ????????????????????????
     */
    var countriesStatus = false

    var beginTime: String = ""
    var endTime: String = ""
    /**
     * ?????????????????????
     */
    var fundFlowingWaterSymbol = ""
    /**
     * ?????????????????????
     */
    var fundFlowingWaterType = ""
    /**
     * ?????????????????????
     */
    var payType = ""
    /**
     * ?????????????????????
     */
    var paymentMethod = ""
    /**
     * ???????????????
     */
    var choosecountries = ""
    /**
     *  ?????????????????????
     */
    var payCoinsForTrading: ArrayList<JSONObject>? = arrayListOf()
    /**
     *  ???????????????
     */
    var hidePayCoinForTradingList: ArrayList<JSONObject>? = arrayListOf()
    /**
     *  ???????????????
     */
    var countryForTrading: ArrayList<JSONObject>? = arrayListOf()
    /**
     *  ?????????????????????
     */
    var paymentForTrading: ArrayList<JSONObject>? = arrayListOf()
    var transfer = ""
    var closingIncome = ""
    var contractFee = ""
    var blastPosition = ""
    var capitalFee = ""

    var cencelist: ArrayList<CashFlowSceneBean.Scene> = arrayListOf()

    var coinList: ArrayList<String>? = null

    var type = COMMISSIONED
    var showTransferType = false

    var commissIonedListener: CommissIonedListener? = null
    var tradingListener: TradingListener? = null
    var transferListener: TransferListener? = null
    var fundFlowingWaterListenre: OTCFundFlowingWaterListenre? = null
    var otcOrderListener: OTCOrderListener? = null
    var assetTopUpListener: AssetTopUpListener? = null
    var widthDrawListener: WithDrawListener? = null
    var mailScreeningListener: MailOrderListener? = null
    var contractBillListener: ContractBillListener? = null
    var contractScreeningListener: ContractScreeningListener? = null
    var cashFlow4B2CListener: OTCFundFlowingWaterListenre? = null
    var leverTransferListener: LeverTransferScreeningListener? = null
    var leverAssetCashFlowListener: LeverAssetCashFlowScreeningListener? = null
    var posRecordListener:PosRecordListener?=null
    var marginBottom = DisplayUtils.dip2px(context, 56f)
    var contractTimer: Int = 0

    /**
     * ????????????
     */
    var leverTransferDirection = ""
    /**
     * ????????????
     */
    var contractTransferDirection = 0

    /**
     * ????????????????????????
     */
    var leverSymbol = ""

    /**
     * ????????????????????????
     */
    var leverAssetCashFlowType = ParamConstant.THE_CURRENT_LENDING

    var commissionedSymbol = ""
    var commissionedSeries = ""
    var commissiondSelectSymbolStatus = false
    var commissiondCheckedStatus = false
    var priceType = 0
    var positionsType = 0
    var tradingType = 0
    var symbolAndUnit = ""
    /**
     * ??????
     */
    var coinSymbol = ""

    /**
     * otc all trading ??????
     */
    var payForOTC = OTCPublicInfoDataService.getInstance().paycoins
    /**
     * ??????
     */
    var payCoin = "CNY"

    var tradeType = ""
    var otcByStatusContent = arrayListOf(
        LanguageUtil.getString(context, "common_action_sendall"), LanguageUtil.getString(context, "filter_otc_waitPay"), LanguageUtil.getString(context, "filter_otc_didPay"), LanguageUtil.getString(context, "filter_otc_complete"), LanguageUtil.getString(context, "filter_otc_cancel"),
            LanguageUtil.getString(context, "otc_text_orderAppeal"), LanguageUtil.getString(context, "filter_otc_appealDone"), LanguageUtil.getString(context, "filter_otc_appealCancel"))
    var otcByStatusNum = arrayListOf("", "1", "2", "3", "4", "5", "8", "9")
    var otcByStatus = ""
    var orderSelectStatus = false
    var contractHistorySelectStatus = false
    var orderSelectCoinStatus = false

    /**
     * ?????????
     */
    var mailList: ArrayList<String> = arrayListOf()
    var mailType = ""


    /**
     * ??????????????????
     */
    interface ContractScreeningListener {
        /**
         * ??????
         * @param status ??????????????????
         * @param series ????????????
         * @param contractTime ????????????
         * @param tradingType 0 ?????? 1 ?????? 2 ??????
         * @param priceType 0 ?????? 1 ?????? 2 ??????
         * @param begin ????????????
         * @param end ????????????
         * @param positionsType ????????????
         *
         */
        fun confirmContractScreening(status: Boolean, series: String, contractTimer: Int, tradingType: Int, priceType: Int, begin: String, end: String, positionsType: Int)
    }

    /**
     * ????????????----??????
     */

    interface LeverAssetCashFlowScreeningListener {

        fun confirmLeverAssetCashFlowScreening(leverSymbol: String, leverAssetCashFlowType: Int)

        fun onclickSymbolSelect()
    }



    /**
     * ??????PoS??????
     */
    interface PosRecordListener {
        /**
         * @param symbol ??????
         * @param miningType ????????????
         * @param begin ????????????
         * @param end ????????????
         */
        fun returnPosRecord(symbol: String, miningType: Int, begin: String, end: String)
    }

    /**
     * ?????? -----??????
     */
    interface LeverTransferScreeningListener {
        fun confirmLeverTransferScreening(leverTransferDirection: String)
    }

    /**
     * ?????????
     */
    interface MailOrderListener {
        fun returnMailOrderType(position: String)
    }


    /**
     * ????????????
     */
    interface ContractBillListener {
        /**
         * ??????
         * @param transfer
         * @param closingIncome
         * @param contractFee
         * @param blastPosition
         * @param capitalFee
         * @param begin ????????????
         * @param end ????????????
         */
        fun returnContractBill(transfer: String, closingIncome: String, contractFee: String, blastPosition: String, capitalFee: String, begin: String, end: String)
    }


    /**
     * ????????????
     */
    interface OTCOrderListener {
        fun returnScreeningOrderStatus(trading: String, payCoin: String, coinSymbol: String, orderStatus: String, startTime: String, end: String)
    }

    /**
     * ????????????
     */
    interface AssetTopUpListener {
        fun returnAssetTopUpTime(startTime: String, end: String)
    }

    /**
     * ????????????
     */
    interface WithDrawListener {
        fun returnWithDrawTime(startTime: String, end: String, type: Int)
    }

    /**
     * ????????????
     */
    interface CommissIonedListener {

        /**
         * ??????
         * @param status ??????????????????
         * @param symbol ??????
         * @param symbolAndUnit ????????????
         * @param tradingType 0 ?????? 1 ?????? 2 ??????
         * @param priceType 0 ?????? 1 ?????? 2 ??????
         * @param begin ????????????
         * @param end ????????????
         */
        fun confirmCommissioned(status: Boolean, symbol: String, symbolAndUnit: String, tradingType: Int, priceType: Int, begin: String, end: String)

    }

    /**
     * ??????????????????
     */
    interface TradingListener {
        /**
         *
         * @param trading ??????????????????
         * @param amount ??????????????????
         * @param fiatType ??????????????????
         * @param paymentType ??????????????????
         * @param countries ????????????
         */
        fun returnTradingType(trading: Int, amount: String, fiatType: String, paymentType: String, countries: String)

    }

    /**
     * ????????????
     */
    interface TransferListener {

        /**
         * @param transferType ????????????
         * @param begin ????????????
         * @param end ????????????
         */
        fun returnTransfer(transferType: Int, begin: String, end: String)
    }

    /**
     * ????????????
     */
    interface OTCFundFlowingWaterListenre {

        /**
         * @param symbol ??????
         * @param waterType ????????????
         * @param begin ????????????
         * @param end ????????????
         */
        fun returnOTCFundFlowingWater(symbol: String, waterType: String, begin: String, end: String)
    }

    init {
        attrs.let {
            var typeArray = context.obtainStyledAttributes(it, R.styleable.ScreeningPopupWindowView)
            type = typeArray.getInteger(R.styleable.ScreeningPopupWindowView_screen_type, COMMISSIONED)
            showTransferType = typeArray.getBoolean(R.styleable.ScreeningPopupWindowView_screen_show_type_for_transfer, false)
            marginBottom = typeArray.getInteger(R.styleable.ScreeningPopupWindowView_screen_margin_bottom, DisplayUtils.dip2px(context, 56f))
        }
        initView(context)
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.item_screening_popup_window, this, true)
        cub_confirm.isEnable(true)
        setInitView(type)
        main_layout.setOnClickListener {
            this.visibility = View.GONE
        }
        cub_reset?.setBottomTextContent(LanguageUtil.getString(context, "filter_action_reset"))
        cub_confirm?.setBottomTextContent(LanguageUtil.getString(context, "common_text_btnConfirm"))

        /**
         * ??????
         */
        cub_reset.isEnable(true)
        cub_reset.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                when (type) {
                    COMMISSIONED -> {
                        commissionedReset()
                    }
                    TRADING -> {
                        setResetForOTCTraing()
                    }
                    TRANSFER -> {
                        resetTransfer()
                    }
                    OTCFUNDFLOWINGWATER -> {
                        resetFundFlowingWater()
                    }
                    OTCORDER -> {
                        resetOTCOrder()
                    }
                    ASSETTOPUP -> {
                        resetAssetTopUp()
                    }
                    MAILSCREENING -> {
                        resetMail()
                    }
                    WITHDRAWTYPE -> {
                        resetWithDraw()
                    }
                    HISTORYSCREENINGCONTRACT -> {
                        historyContractReset()
                    }
                    CONTRACTBILL -> {
                        resetContractBill()
                    }
                    CASHFLOW_B2C -> {
                        resetB2CCashFlow()
                    }
                    LEVER_ASSET_CASHFLOW -> {
                        resetLeverAssetCashFlow()
                    }
                    LEVER_TRANSFER_HISTORY -> {
                        resetLeverTransferHistory()
                    }
                    MYPOSRECORD -> {
                        resetPoSRecord()
                    }
                    CONTRACT_TRANSFER_HISTORY -> {
                        resetContractTransfer()
                    }
                }
                // ????????????
                KeyBoardUtils.closeKeyBoard(context)
            }
        }
        /**
         * ??????
         */

        cub_confirm.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                when (type) {
                    COMMISSIONED -> {

                        if (commissIonedListener != null) {
                            commissIonedListener?.confirmCommissioned(commissiondCheckedStatus, commissionedSymbol, symbolAndUnit, tradingType, priceType, beginTime, endTime)
                        }
                    }
                    TRADING -> {
                        if (tradingListener != null) {
                            tradingListener?.returnTradingType(tradingtypeForTrading, tradingAmount, payType, paymentMethod, choosecountries)
                        }

                    }
                    TRANSFER -> {
                        if (transferListener != null) {
                            transferListener?.returnTransfer(transferType, beginTime, endTime)
                        }
                    }
                    OTCFUNDFLOWINGWATER -> {
                        if (fundFlowingWaterListenre != null) {
                            fundFlowingWaterListenre?.returnOTCFundFlowingWater(fundFlowingWaterSymbol, fundFlowingWaterType, beginTime, endTime)
                        }
                    }
                    OTCORDER -> {
                        if (otcOrderListener != null) {
                            otcOrderListener?.returnScreeningOrderStatus(tradeType, payCoin, coinSymbol, otcByStatus, beginTime, endTime)
                        }
                    }
                    ASSETTOPUP -> {
                        if (assetTopUpListener != null) {
                            assetTopUpListener?.returnAssetTopUpTime(beginTime, endTime)
                        }
                    }
                    MAILSCREENING -> {
                        if (mailScreeningListener != null) {
                            mailScreeningListener?.returnMailOrderType(mailType)
                        }
                    }
                    WITHDRAWTYPE -> {
                        if (widthDrawListener != null) {
                            widthDrawListener?.returnWithDrawTime(beginTime, endTime, withDrawType)
                        }
                    }
                    HISTORYSCREENINGCONTRACT -> {
                        if (contractScreeningListener != null) {
                            contractScreeningListener?.confirmContractScreening(contractHistorySelectStatus, commissionedSeries, contractTimer, tradingType, priceType, beginTime, endTime, positionsType)
                        }
                    }
                    CONTRACTBILL -> {
                        if (contractBillListener != null) {
                            contractBillListener?.returnContractBill(transfer, closingIncome, contractFee, blastPosition, capitalFee, beginTime, endTime)
                        }
                    }

                    CASHFLOW_B2C -> {
                        if (cashFlow4B2CListener != null) {
                            cashFlow4B2CListener?.returnOTCFundFlowingWater(fundFlowingWaterSymbol, fundFlowingWaterType, beginTime, endTime)
                        }
                    }
                    LEVER_ASSET_CASHFLOW -> {
                        if (null != leverAssetCashFlowListener) {
                            leverAssetCashFlowListener?.confirmLeverAssetCashFlowScreening(leverSymbol, leverAssetCashFlowType)
                        }
                    }
                    MYPOSRECORD ->{
                        if (posRecordListener != null) {
                            posRecordListener?.returnPosRecord(posSymbol,miningType,beginTime,endTime)
                        }

                    }
                    LEVER_TRANSFER_HISTORY -> {
                        if (null != leverTransferListener) {
                            leverTransferListener?.confirmLeverTransferScreening(leverTransferDirection)
                        }
                    }
                    CONTRACT_TRANSFER_HISTORY -> {
                        transferListener?.returnTransfer(contractTransferDirection, beginTime, endTime)
                    }
                }
                visibility = View.GONE
                KeyBoardUtils.closeKeyBoard(context)

            }

        }
    }

    fun setInitView(status: Int) {
        type = status
        setRecordVisible()
        when (status) {
            COMMISSIONED -> {
                commissioned_layout?.visibility = View.VISIBLE
                initCommissioned()
            }
            TRADING -> {
                otc_trading_layout.visibility = View.VISIBLE
                initTrading()
            }
            TRANSFER -> {
                otc_transfer.visibility = View.VISIBLE
                initTransfer()
            }
            OTCFUNDFLOWINGWATER -> {
                otc_fund_pwd.visibility = View.VISIBLE
                initFundFlowingWater()
            }
            OTCORDER -> {
                otc_my_order.visibility = View.VISIBLE
                initMyOrder()
            }
            ASSETTOPUP -> {
                asset_top_up.visibility = View.VISIBLE
                initAssetTopUp()
            }
            MAILSCREENING -> {
                mail_screening.visibility = View.VISIBLE
            }
            WITHDRAWTYPE -> {
                withdraw_record.visibility = View.VISIBLE
                initWithDrawView()
            }
            HISTORYSCREENINGCONTRACT -> {
                history_contract_screening?.visibility = View.VISIBLE
                initContract4History()
            }
            CONTRACTBILL -> {
                contract_bill_screening.visibility = View.VISIBLE
                initContractBill()
            }

            CASHFLOW_B2C -> {
                b2c_cashflow_screening.visibility = View.VISIBLE
                initB2CCashFlow()
            }
            LEVER_ASSET_CASHFLOW -> {
                item_lever_asset_history?.visibility = View.VISIBLE
                initLeverAssetCashflow()
            }
            LEVER_TRANSFER_HISTORY -> {
                item_lever_transfer_history?.visibility = View.VISIBLE
                initLeverTransferHistory()
            }
            MYPOSRECORD -> {
                my_pos_record.visibility = View.VISIBLE
                initPosRecord()

            }
            CONTRACT_TRANSFER_HISTORY -> {
                cub_confirm?.isEnable(true)
                item_contract_transfer_history?.visibility = View.VISIBLE
                initContractTransferHistory()
            }
        }
    }

    fun setMage() {
        val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        ll_bb_layout.measure(w, h)
        val height = ll_bb_layout.measuredHeight
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowHight = wm.defaultDisplay.height
        if (height > (windowHight - marginBottom)) {
            setMarginBottomHeight(marginBottom)
        } else {
            setMarginBottomHeight((windowHight - height) / 2)
        }
    }

    fun setOTCTrading() {
        val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        ll_bb_layout.measure(w, h)
        val height = ll_bb_layout.measuredHeight
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val windowHight = wm.defaultDisplay.height
        setMarginBottomHeight(marginBottom)
    }


    fun setRecordVisible() {
        commissioned_layout?.visibility = View.GONE
        otc_trading_layout.visibility = View.GONE
        otc_transfer.visibility = View.GONE
        otc_fund_pwd.visibility = View.GONE
        otc_my_order.visibility = View.GONE
        asset_top_up.visibility = View.GONE
        mail_screening.visibility = View.GONE
        mail_screening.visibility = View.GONE
    }

    fun setMarginBottomHeight(height: Int) {
        var params: RelativeLayout.LayoutParams = ll_bb_layout.layoutParams as RelativeLayout.LayoutParams
        params.setMargins(0, 0, 0, height)
    }

    /**  ????????? ????????????    **************************************************  ************************************************** ************************************************** **/


    fun initContractBill() {
        var sceneList = Contract2PublicInfoManager.getSceneList()
        if (sceneList.isEmpty()) {
            return
        }

        sceneList.forEach {
            var childItem = it.childItem as ArrayList<ContractSceneList.ChildItem?>?
            childItem?.add(0, ContractSceneList.ChildItem("", LanguageUtil.getString(context, "common_action_sendall")))
            when (it?.item) {
                "TRANSFER" -> {
                    tv_title_for_bill_one.text = it.langTxt
                    ll_bill_title_one.setContractBillData(childItem ?: arrayListOf(), false)
                }
                "CLOSING_INCOME" -> {
                    tv_title_for_bill_two.text = it.langTxt
                    ll_bill_title_two.setContractBillData(childItem ?: arrayListOf(), false)
                }
                "CONTRACT_FEE" -> {
                    tv_title_for_bill_three.text = it.langTxt
                    ll_bill_title_three.setContractBillData(childItem ?: arrayListOf(), false)

                }
                "BLAST_POSITION" -> {
                    tv_title_for_bill_four.text = it.langTxt
                    ll_bill_title_four.setContractBillData(childItem ?: arrayListOf(), false)
                }
                "CAPITAL_FEE" -> {
                    tv_title_for_bill_five.text = it.langTxt
                    ll_bill_title_five.setContractBillData(childItem ?: arrayListOf(), false)
                }
            }
        }


        ll_bill_title_one.setLineSelectOncilckListener(object : LineSelectOnclickListener {

            override fun sendOnclickMsg() {

            }

            override fun selectMsgIndex(index: String?) {
                transfer = index ?: ""
            }

        })
        ll_bill_title_two.setLineSelectOncilckListener(object : LineSelectOnclickListener {

            override fun sendOnclickMsg() {

            }

            override fun selectMsgIndex(index: String?) {
                closingIncome = index ?: ""
            }

        })
        ll_bill_title_three.setLineSelectOncilckListener(object : LineSelectOnclickListener {

            override fun sendOnclickMsg() {

            }

            override fun selectMsgIndex(index: String?) {
                contractFee = index ?: ""
            }

        })
        ll_bill_title_four.setLineSelectOncilckListener(object : LineSelectOnclickListener {

            override fun sendOnclickMsg() {

            }

            override fun selectMsgIndex(index: String?) {
                blastPosition = index ?: ""
            }

        })
        ll_bill_title_five.setLineSelectOncilckListener(object : LineSelectOnclickListener {

            override fun sendOnclickMsg() {

            }

            override fun selectMsgIndex(index: String?) {
                capitalFee = index ?: ""
            }

        })


        sdv_bill_top_up?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                beginTime = startTime
                endTime = endTimes
            }

        }
    }

    fun resetContractBill() {
        transfer = ""
        closingIncome = ""
        contractFee = ""
        blastPosition = ""
        capitalFee = ""
        beginTime = ""
        endTime = ""
        sdv_bill_top_up.resetTime()
        ll_bill_title_one.clearLables()
        ll_bill_title_two.clearLables()
        ll_bill_title_three.clearLables()
        ll_bill_title_four.clearLables()
        ll_bill_title_five.clearLables()
    }


    /**  ????????? ????????????    **************************************************  ************************************************** ************************************************** **/


    fun initMyOrder() {
        if (null != payForOTC && payForOTC.isNotEmpty()) {
            payCoin = payForOTC[0].optString("key")
        }
        tv_title_order_commissioned_type?.text = LanguageUtil.getString(context, "filter_fold_transactionType")
        rb_order_all?.text = LanguageUtil.getString(context, "common_action_sendall")
        rb_order_buy?.text = LanguageUtil.getString(context, "otc_action_buy")
        rb_order_sell?.text = LanguageUtil.getString(context, "otc_action_sell")
        tv_order_title_trading?.text = LanguageUtil.getString(context, "filter_fold_tradeUnit")
        et_order_symbol?.hint = LanguageUtil.getString(context, "filter_input_coinsymbol")
        tv_order_title_price_type?.text = LanguageUtil.getString(context, "filter_fold_orderState")
        tv_order_title_data?.text = LanguageUtil.getString(context, "charge_text_date")


        otc_my_order?.sdv_otc_order?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                beginTime = startTime
                endTime = endTimes
            }

        }

        ll_order_trading_type_title.setStringBeanData(otcByStatusContent, true)


        if (payForOTC.isNotEmpty()) {
            ll_order_total_title.setPaycoinsBeanData(payForOTC, false)
            pet_order_select_coin.setEditText(payForOTC[0].optString("key"))
        }
        ll_order_total_title.visibility = View.GONE
        rb_order_all.setOnClickListener {
            tradeType = ""
            setSelectForOTC(0)
        }
        rb_order_buy.setOnClickListener {
            setSelectForOTC(1)
            tradeType = "buy"
        }
        rb_order_sell.setOnClickListener {
            setSelectForOTC(2)
            tradeType = "sell"
        }

        pet_order_select_coin.setEditText("CNY")
        pet_order_select_coin.onTextListener = object : PwdSettingView.OnTextListener {
            override fun showText(text: String): String {

                return text
            }


            override fun returnItem(item: Int) {

            }

            override fun onclickImage() {
                orderSelectStatus = !orderSelectStatus
                if (orderSelectStatus) {
                    ll_order_total_title.visibility = View.VISIBLE
                    pet_order_select_coin.setImageView(R.drawable.collapse)
                } else {
                    pet_order_select_coin.setImageView(R.drawable.dropdown)
                    ll_order_total_title.visibility = View.GONE
                }
            }

        }
        et_order_symbol.isFocusable = true
        et_order_symbol.isFocusableInTouchMode = true
        et_order_symbol.setOnFocusChangeListener { v, hasFocus ->
            v_order_line.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }

        et_order_symbol.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                coinSymbol = s.toString()
            }

        })

        ll_order_total_title.setLineSelectOncilckListener(object : LineSelectOnclickListener {

            override fun sendOnclickMsg() {

            }

            override fun selectMsgIndex(index: String?) {
                payCoin = index ?: OTCPublicInfoDataService.getInstance().getotcDefaultPaycoin()
                pet_order_select_coin.setEditText(payCoin)

            }

        })


        ll_order_trading_type_title.setLineSelectOncilckListener(object : LineSelectOnclickListener {

            override fun sendOnclickMsg() {

            }

            override fun selectMsgIndex(index: String?) {
                otcByStatus = otcByStatusNum[otcByStatusContent.indexOf(index)]
                tv_order_change_trading.text = otcByStatusContent[otcByStatusContent.indexOf(index)]

            }
        })
        rl_order_title_price_layout.setOnClickListener {
            orderSelectCoinStatus = !orderSelectCoinStatus

            setImageStatus(iv_order_change_fiat, orderSelectCoinStatus)
            ll_order_trading_type_title?.setStringBeanData(otcByStatusContent, !orderSelectCoinStatus)

        }
    }

    /**
     * ????????????
     */
    fun setSelectForOTC(index: Int) {
        tradingType = index
        when (index) {
            0 -> {
                rb_order_all.isLabelEnable = true
                rb_order_buy.isLabelEnable = false
                rb_order_sell.isLabelEnable = false
                rb_order_all?.setBg(true)
                rb_order_buy?.setBg(false)
                rb_order_sell?.setBg(false)
            }
            1 -> {
                rb_order_all.isLabelEnable = false
                rb_order_buy.isLabelEnable = true
                rb_order_sell.isLabelEnable = false
                rb_order_all?.setBg(false)
                rb_order_buy?.setBg(true)
                rb_order_sell?.setBg(false)
            }
            2 -> {
                rb_order_all.isLabelEnable = false
                rb_order_buy.isLabelEnable = false
                rb_order_sell.isLabelEnable = true
                rb_order_all?.setBg(false)
                rb_order_buy?.setBg(false)
                rb_order_sell?.setBg(true)
            }
        }
    }

    fun resetOTCOrder() {
        setSelectForOTC(0)
        ll_order_total_title.visibility = View.GONE
        pet_order_select_coin.setEditText("CNY")
        et_order_symbol.setText("")
        coinSymbol = ""
        tradeType = ""
        otcByStatus = ""
        tv_order_change_trading.text = ""
        orderSelectCoinStatus = false
        beginTime = ""
        endTime = ""
        otc_my_order?.sdv_otc_order?.resetTime()
        payCoin = OTCPublicInfoDataService.getInstance().getotcDefaultPaycoin() ?: ""
        ll_order_trading_type_title.clearLables()
        ll_order_trading_type_title.setStringBeanData(otcByStatusContent, true)


    }

    /**  ????????? ????????????    **************************************************  ************************************************** ************************************************** **/


    /**
     * ????????????
     */
    fun initFundFlowingWater() {
        tv_title_otc_symbol?.text = LanguageUtil.getString(context, "common_text_coinsymbol")
        cet_otc_symbol?.hint = LanguageUtil.getString(context, "charge_action_searchcoin")
        tv_title_otc_trading_type?.text = LanguageUtil.getString(context, "filter_fold_transactionType")
        tv_title_otc_data?.text = LanguageUtil.getString(context, "charge_text_date")



        cet_otc_symbol.isFocusable = true
        cet_otc_symbol.isFocusableInTouchMode = true
        cet_otc_symbol.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fundFlowingWaterSymbol = s.toString()
            }
        })
        setGloblalLayoutListener(cet_otc_symbol)

        otc_fund_pwd?.sdv_fund?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                beginTime = startTime
                endTime = endTimes
            }

        }
    }

    fun resetFundFlowingWater() {
        beginTime = ""
        endTime = ""
        otc_fund_pwd?.sdv_fund?.resetTime()
        ll_total_otc_fund_pwd_title.clearLables()
        ll_total_otc_fund_pwd_title.setSceneBeanData(cencelist, false)

        fundFlowingWaterSymbol = ""
        cet_otc_symbol.setText("")
    }


    /**
     * ????????????B2C
     */
    fun initB2CCashFlow() {
        tv_coin_title?.text = LanguageUtil.getString(context, "common_text_coinsymbol")
        tv_cashflow_type_title?.text = LanguageUtil.getString(context, "contract_text_type")
        rb_recharge_b2c?.text = LanguageUtil.getString(context, "title_recharge")
        rb_withdraw_b2c?.text = LanguageUtil.getString(context, "withdraw_b2c")
        tv_title_otc_transfer_data?.text = LanguageUtil.getString(context, "charge_text_date")
        cet_coin?.isFocusable = true
        cet_coin?.isFocusableInTouchMode = true
        rb_recharge_b2c?.setBg(true)
        fundFlowingWaterType = ParamConstant.TRANSFER_RECHARGE_RECORD
        cet_coin?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fundFlowingWaterSymbol = s.toString()
            }
        })

        rb_recharge_b2c?.setOnClickListener {
            fundFlowingWaterType = ParamConstant.TRANSFER_RECHARGE_RECORD
            cashflowLabel(0)
        }

        rb_withdraw_b2c?.setOnClickListener {
            fundFlowingWaterType = ParamConstant.TRANSFER_WITHDRAW_RECORD
            cashflowLabel(1)
        }


        b2c_cashflow_screening?.sdv_cashflow_b2c?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                beginTime = startTime
                endTime = endTimes
            }
        }
    }

    fun resetB2CCashFlow() {
        beginTime = ""
        endTime = ""
        b2c_cashflow_screening?.sdv_cashflow_b2c?.resetTime()

        fundFlowingWaterSymbol = ""
        cet_coin?.setText("")

        cashflowLabel()
    }


    fun cashflowLabel(index: Int = 0) {
        when (index) {
            0 -> {
                rb_recharge_b2c.isLabelEnable = true
                rb_withdraw_b2c.isLabelEnable = false
                rb_recharge_b2c?.setBg(true)
                rb_withdraw_b2c?.setBg(false)
            }
            1 -> {
                rb_recharge_b2c.isLabelEnable = false
                rb_withdraw_b2c.isLabelEnable = true
                rb_recharge_b2c?.setBg(false)
                rb_withdraw_b2c?.setBg(true)
            }
        }
    }


    fun initLineAdaptiveLayout(date: ArrayList<CashFlowSceneBean.Scene>) {
        if (date.size > 0) {
            fundFlowingWaterType = date[0].key.toString()
        }
        cencelist = date
        ll_total_otc_fund_pwd_title.setSceneBeanData(date, false)
        ll_total_otc_fund_pwd_title.setLineSelectOncilckListener(object : LineSelectOnclickListener {
            override fun selectMsgIndex(index: String?) {
                if (!TextUtils.isEmpty(index)) {
                    fundFlowingWaterType = index ?: ""
                }
            }

            override fun sendOnclickMsg() {

            }
        })
    }

    /**
     * ?????? ?????? ?????? ??????
     */
    fun getChooseTitle() {
        HttpClient.instance.getCashFlowScene()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<CashFlowSceneBean>() {
                    override fun onHandleSuccess(t: CashFlowSceneBean) {
                        if (t.sceneList == null) {
                            return
                        }

                    }


                })
    }

    /**  ????????? ????????????    **************************************************  ************************************************** ************************************************** **/
    fun initContractTransferHistory() {
        tv_type_contract?.text = LanguageUtil.getString(context, "contract_text_type")
        rb_all_contract_transfer_history?.text = LanguageUtil.getString(context, "common_action_sendall")
        rb_buy_contract_transfer_history?.text = LanguageUtil.getString(context, "contract_bb_transfer_to_contract")
        rb_sell_contract_transfer_history?.text = LanguageUtil.getString(context, "sl_str_contract_to_coin")
        tv_contract_data_new?.text = LanguageUtil.getString(context, "charge_text_date")

        rb_all_contract_transfer_history?.setBg(true)
        rb_all_contract_transfer_history?.setOnClickListener {
            setSelectContractTransfer(0)
        }
        rb_buy_contract_transfer_history?.setOnClickListener {
            setSelectContractTransfer(1)
        }
        rb_sell_contract_transfer_history?.setOnClickListener {
            setSelectContractTransfer(2)
        }

        contract_transfer_time_layout?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                beginTime = startTime
                endTime = endTimes
            }

        }
    }

    fun initPosRecord() {
        my_pos_record?.sdv_asset_top_up_pos?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                beginTime = startTime
                endTime = endTimes
            }

        }
        et_symbol.isFocusable = true
        et_symbol.isFocusableInTouchMode = true
        et_symbol.setOnFocusChangeListener { _, hasFocus ->
            view_line_pos.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }

        et_symbol.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                posSymbol = s.toString()
            }

        })

    }

    fun resetContractTransfer() {
        beginTime = ""
        endTime = ""
        contract_transfer_time_layout?.resetTime()
        setSelectContractTransfer(0)
    }
    fun resetPoSRecord() {
        ll_pos_record_mining.clearLables()
        ll_pos_record_mining.setStringBeanData(miningTypeList, false)
        beginTime = ""
        endTime = ""
        et_symbol.setText("")
        my_pos_record?.sdv_asset_top_up_pos?.resetTime()

    }

    fun setSelectContractTransfer(index: Int) {
        contractTransferDirection = index
        when (index) {
            0 -> {
                rb_all_contract_transfer_history?.isLabelEnable = true
                rb_buy_contract_transfer_history?.isLabelEnable = false
                rb_sell_contract_transfer_history?.isLabelEnable = false
                rb_all_contract_transfer_history?.setBg(true)
                rb_buy_contract_transfer_history?.setBg(false)
                rb_sell_contract_transfer_history?.setBg(false)
            }
            1 -> {
                rb_all_contract_transfer_history?.isLabelEnable = false
                rb_buy_contract_transfer_history?.isLabelEnable = true
                rb_sell_contract_transfer_history?.isLabelEnable = false
                rb_all_contract_transfer_history?.setBg(false)
                rb_buy_contract_transfer_history?.setBg(true)
                rb_sell_contract_transfer_history?.setBg(false)
            }
            2 -> {
                rb_all_contract_transfer_history?.isLabelEnable = false
                rb_buy_contract_transfer_history?.isLabelEnable = false
                rb_sell_contract_transfer_history?.isLabelEnable = true
                rb_all_contract_transfer_history?.setBg(false)
                rb_buy_contract_transfer_history?.setBg(false)
                rb_sell_contract_transfer_history?.setBg(true)
            }
        }
    }


    /**  ????????? ????????????    **************************************************  ************************************************** ************************************************** **/
    fun initLeverTransferHistory() {
        tv_title_lever_transfer_history_type?.text = LanguageUtil.getString(context, "contract_text_type")
        rb_all_lever_transfer_history?.text = LanguageUtil.getString(context, "common_action_sendall")
        rb_buy_lever_transfer_history?.text = LanguageUtil.getString(context, "coin_to_leverage")
        rb_sell_lever_transfer_history?.text = LanguageUtil.getString(context, "leverage_to_coin")


        rb_all_lever_transfer_history?.setBg(true)
        rb_all_lever_transfer_history?.setOnClickListener {
            setSelectLeverTransfer(0)
        }
        rb_buy_lever_transfer_history?.setOnClickListener {
            setSelectLeverTransfer(1)
        }
        rb_sell_lever_transfer_history?.setOnClickListener {
            setSelectLeverTransfer(2)
        }
    }

    fun setSelectLeverTransfer(index: Int) {
        when (index) {
            0 -> {
                leverTransferDirection = ""
                rb_all_lever_transfer_history?.isLabelEnable = true
                rb_buy_lever_transfer_history?.isLabelEnable = false
                rb_sell_lever_transfer_history?.isLabelEnable = false
                rb_all_lever_transfer_history?.setBg(true)
                rb_buy_lever_transfer_history?.setBg(false)
                rb_sell_lever_transfer_history?.setBg(false)
            }
            1 -> {
                leverTransferDirection = "1"
                rb_all_lever_transfer_history?.isLabelEnable = false
                rb_buy_lever_transfer_history?.isLabelEnable = true
                rb_sell_lever_transfer_history?.isLabelEnable = false
                rb_all_lever_transfer_history?.setBg(false)
                rb_buy_lever_transfer_history?.setBg(true)
                rb_sell_lever_transfer_history?.setBg(false)
            }
            2 -> {
                leverTransferDirection = "2"
                rb_all_lever_transfer_history?.isLabelEnable = false
                rb_buy_lever_transfer_history?.isLabelEnable = false
                rb_sell_lever_transfer_history?.isLabelEnable = true
                rb_all_lever_transfer_history?.setBg(false)
                rb_buy_lever_transfer_history?.setBg(false)
                rb_sell_lever_transfer_history?.setBg(true)
            }
        }
    }

    fun resetLeverTransferHistory() {
        setSelectLeverTransfer(0)
    }

    /**  ????????? ??????????????????    **************************************************  ************************************************** ************************************************** **/


    fun initLeverAssetCashflow() {
        tv_lever_symbol_title?.text = LanguageUtil.getString(context, "leverage_coinMap")
        tv_title_lever_history_type?.text = LanguageUtil.getString(context, "filter_fold_journalType")
        rb_lever_current_loan?.text = LanguageUtil.getString(context, "leverage_current_borrow")
        rb_lever_apply_for_loan?.text = LanguageUtil.getString(context, "leverage_history_borrow")
        rb_lever_return_the_borrowing?.text = LanguageUtil.getString(context, "transfer_text_record")
        psv_lever_symbol?.setEditText(LanguageUtil.getString(context, "common_action_sendall"))
        rb_lever_current_loan?.setBg(true)
        rb_lever_current_loan?.setOnClickListener {
            setSelectLeverAssetCashFlow(0)
        }
        rb_lever_apply_for_loan?.setOnClickListener {
            setSelectLeverAssetCashFlow(1)
        }
        rb_lever_return_the_borrowing?.setOnClickListener {
            setSelectLeverAssetCashFlow(2)
        }

        psv_lever_symbol?.onTextListener = object : PwdSettingView.OnTextListener {
            override fun showText(text: String): String {
                return text
            }

            override fun returnItem(item: Int) {

            }

            override fun onclickImage() {
                if (null != leverAssetCashFlowListener) {
                    leverAssetCashFlowListener?.onclickSymbolSelect()
                }
            }

        }
    }

    fun setLever4AssetCashFlowSymbol(symbol: String) {
        leverSymbol = symbol
        psv_lever_symbol?.setEditText(NCoinManager.getMarketName4Symbol(leverSymbol))
    }

    fun resetLeverAssetCashFlow() {
        setLever4AssetCashFlowSymbol("")
        setSelectLeverAssetCashFlow(0)
    }

    fun setSelectLeverAssetCashFlow(index: Int) {
        leverAssetCashFlowType = index
        when (index) {
            0 -> {
                rb_lever_current_loan?.isLabelEnable = true
                rb_lever_apply_for_loan?.isLabelEnable = false
                rb_lever_return_the_borrowing?.isLabelEnable = false
                rb_lever_current_loan?.setBg(true)
                rb_lever_apply_for_loan?.setBg(false)
                rb_lever_return_the_borrowing?.setBg(false)
            }
            1 -> {
                rb_lever_current_loan?.isLabelEnable = false
                rb_lever_apply_for_loan?.isLabelEnable = true
                rb_lever_return_the_borrowing?.isLabelEnable = false
                rb_lever_current_loan?.setBg(false)
                rb_lever_apply_for_loan?.setBg(true)
                rb_lever_return_the_borrowing?.setBg(false)
            }
            2 -> {
                rb_lever_current_loan?.isLabelEnable = false
                rb_lever_apply_for_loan?.isLabelEnable = false
                rb_lever_return_the_borrowing?.isLabelEnable = true
                rb_lever_current_loan?.setBg(false)
                rb_lever_apply_for_loan?.setBg(false)
                rb_lever_return_the_borrowing?.setBg(true)
            }
        }
    }

    /**  ????????? ????????????    **************************************************  ************************************************** ************************************************** **/
    /**
     * ????????????
     */

    var transferType = 0

    fun initTransfer() {

        otc_transfer?.sdv_transfer?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                beginTime = startTime
                endTime = endTimes
            }

        }
        otc_transfer?.tv_title_transfer_data?.text = LanguageUtil.getString(context, "charge_text_date")


        rb_otc_transfer_all?.setOnClickListener {
            setSelectTransfer(0)
        }
        rb_bibi_to_otc?.setOnClickListener {
            setSelectTransfer(1)
        }
        rb_otc_to_bibi?.setOnClickListener {
            setSelectTransfer(2)
        }
    }

    fun setShowTransfer(status: Boolean) {
        showTransferType = status
    }

    fun resetTransfer() {
        if (showTransferType) {
            setSelectTransfer(0)
        }
        otc_transfer?.sdv_transfer?.initDate()
        beginTime = ""
        endTime = ""
        otc_transfer?.sdv_transfer?.resetTime()
    }


    fun setSelectTransfer(index: Int) {
        transferType = index
        when (index) {
            0 -> {
                rb_otc_transfer_all?.isLabelEnable = true
                rb_bibi_to_otc?.isLabelEnable = false
                rb_otc_to_bibi?.isLabelEnable = false
                rb_otc_transfer_all?.setBg(true)
                rb_bibi_to_otc?.setBg(false)
                rb_otc_to_bibi?.setBg(false)
            }
            1 -> {
                rb_otc_transfer_all?.isLabelEnable = false
                rb_bibi_to_otc?.isLabelEnable = true
                rb_otc_to_bibi?.isLabelEnable = false
                rb_otc_transfer_all?.setBg(false)
                rb_bibi_to_otc?.setBg(true)
                rb_otc_to_bibi?.setBg(false)
            }
            2 -> {
                rb_otc_transfer_all?.isLabelEnable = false
                rb_bibi_to_otc?.isLabelEnable = false
                rb_otc_to_bibi?.isLabelEnable = true
                rb_otc_transfer_all?.setBg(false)
                rb_bibi_to_otc?.setBg(false)
                rb_otc_to_bibi?.setBg(true)
            }
        }
    }


    /**  ?????????????????? ??????    **************************************************  ************************************************** ************************************************** **/


    fun setDataForService(json: JSONObject?) {
        json ?: return
        var city = json.optJSONArray("countryNumberInfo")
        var paycoins = json.optJSONArray("paycoins")
        var payments = json.optJSONArray("payments")
        if (null != city) {
            for (item in 0 until city.length()) {
                countryForTrading?.add(city.optJSONObject(item))
            }
        }

        countryForTrading?.add(0, JSONObject("{\"key\":null,\"title\":\"${LanguageUtil.getString(context, "common_action_sendall")}\",\"icon\":null,\"account\":null,\"used\":false,\"numberCode\":\"\",\"open\":true}"))
        if (null != paycoins) {
            for (num in 0 until paycoins.length()) {
                payCoinsForTrading?.add(paycoins.optJSONObject(num))
            }
        }
        if (null != payments) {
            for (num in 0 until payments.length()) {
                paymentForTrading?.add(payments.optJSONObject(num))
            }
        }

        paymentForTrading?.add(0, JSONObject("{\"key\": \"\",\"title\": \"${LanguageUtil.getString(context, "common_action_sendall")}\",\"icon\": \"\",\"account\": null,\"used\": false,\"numberCode\": null,\"open\": true,\"hide\":true}"))
        payCoinsForTrading?.forEach {
            if (it.optBoolean("hide")) {
                hidePayCoinForTradingList?.add(it)
            }
        }

        if (hidePayCoinForTradingList?.size ?: 0 > 0) {
            ll_fait_title.visibility = View.GONE
            rl_fiat_layout.visibility = View.GONE
        } else {
            if (payCoinsForTrading!!.isNotEmpty()) {
                payType = payCoinsForTrading!![0].optString("key")
            }
            ll_fait_title.setPaycoinsBeanData(payCoinsForTrading ?: arrayListOf(), true)
        }
        Log.e("paymentForTrading", paymentForTrading.toString())

        if (countryForTrading!!.isNotEmpty()) {
            choosecountries = countryForTrading!![0].optString("numberCode")
        }
        if (paymentForTrading!!.isNotEmpty()) {
            paymentMethod = paymentForTrading!![0].optString("key")
        }


        ll_payment_title.setPaymentBeanData(paymentForTrading ?: arrayListOf(), true)
        ll_choose_countries_title.setCountryNumberInfoData(countryForTrading
                ?: arrayListOf(), true)
        et_money.isFocusable = true
        et_money.isFocusableInTouchMode = true
        et_money.setOnFocusChangeListener { v, hasFocus ->
            view_line.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        et_money.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tradingAmount = s.toString()
            }

        })

        setGloblalLayoutListener(et_money)
        rb_general_trading.setOnClickListener {
            setTransactionType(0)
        }
        rb_big_deals.setOnClickListener {
            setTransactionType(1)
        }
        rb_general_trading.setBg(true)
        rb_big_deals.setBg(false)
    }

    /**
     * ??????????????????
     */
    fun initTrading() {

        tv_title_trading_type?.text = LanguageUtil.getString(context, "filter_fold_transactionType")
        rb_general_trading?.text = LanguageUtil.getString(context, "filter_fold_normalTrade")
        rb_big_deals?.text = LanguageUtil.getString(context, "filter_fold_blockTrade")
        tv_title_target_money?.text = LanguageUtil.getString(context, "filter_input_targetPrice")
        et_money?.hint = LanguageUtil.getString(context, "filter_Input_placeholder")
        tv_fiat_type?.text = LanguageUtil.getString(context, "filter_fold_currencyType")
        tv_payment_type?.text = LanguageUtil.getString(context, "filter_fold_payMethod")
        tv_choose_countries_type?.text = LanguageUtil.getString(context, "filter_fold_country")

        /**
         * ???????????? ??????????????????
         */
        rl_fiat_layout.setOnClickListener {
            faitStatus = !faitStatus
            setImageStatus(iv_change_fiat, faitStatus)
            ll_fait_title.setPaycoinsBeanData(payCoinsForTrading ?: arrayListOf(), !faitStatus)

        }
        /**
         * ???????????? ??????????????????
         */
        rl_payment_layout?.setOnClickListener {
            paymentStatus = !paymentStatus
            setImageStatus(iv_payment_fiat, paymentStatus)
            ll_payment_title?.setPaymentBeanData(paymentForTrading
                    ?: arrayListOf(), !paymentStatus)

        }
        /**
         * ???????????? ??????????????????
         */
        rl_choose_countries_layout?.setOnClickListener {
            countriesStatus = !countriesStatus
            setImageStatus(iv_choose_countries_fiat, countriesStatus)
            ll_choose_countries_title?.setCountryNumberInfoData(countryForTrading
                    ?: arrayListOf(), !countriesStatus)

        }

        val currencyTypeTitle = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
            LanguageUtil.getString(context, "filter_fold_currencyType_forotc")
        } else {
            LanguageUtil.getString(context, "filter_fold_currencyType")
        }
        tv_fiat_type?.text = currencyTypeTitle

        /**
         * ???????????? ?????????
         */
        ll_fait_title?.setLineSelectOncilckListener(object : LineSelectOnclickListener {
            override fun selectMsgIndex(index: String?) {
                if (!TextUtils.isEmpty(index)) {
                    payType = index ?: ""
                    payCoinsForTrading?.forEach {
                        if (it.optString("key") == index) {
                            tv_coin_trading?.text = it.optString("title")
                        }
                    }
                }
            }

            override fun sendOnclickMsg() {
            }
        })
        /**
         * ???????????? ?????????
         */
        ll_payment_title?.setLineSelectOncilckListener(object : LineSelectOnclickListener {
            override fun selectMsgIndex(index: String?) {
                paymentMethod = index ?: ""
                paymentForTrading?.forEach {
                    if (it.optString("key") == index) {
                        tv_payment_trading?.text = it.optString("title")
                    }
                }

            }

            override fun sendOnclickMsg() {
            }
        })
        /**
         * ???????????? ?????????
         */
        ll_choose_countries_title?.setLineSelectOncilckListener(object : LineSelectOnclickListener {
            override fun selectMsgIndex(index: String?) {
                choosecountries = index ?: ""
                countryForTrading?.forEach {
                    if (it.optString("numberCode") == index) {
                        tv_choose_countries_trading?.text = it.optString("title")
                    }
                }
            }

            override fun sendOnclickMsg() {
            }
        })

    }

    /**
     * ????????????
     */
    fun setTransactionType(index: Int) {
        tradingtypeForTrading = index
        when (index) {
            0 -> {
                rb_general_trading?.isLabelEnable = true
                rb_big_deals?.isLabelEnable = false
                rb_general_trading?.setBg(true)
                rb_big_deals?.setBg(false)
            }
            1 -> {
                rb_general_trading?.isLabelEnable = false
                rb_big_deals?.isLabelEnable = true
                rb_general_trading?.setBg(false)
                rb_big_deals?.setBg(true)

            }
        }
    }

    fun setResetForOTCTraing() {
        countriesStatus = false
        paymentStatus = false
        faitStatus = false
        ll_fait_title?.clearLables()
        ll_payment_title?.clearLables()
        ll_choose_countries_title?.clearLables()
        ll_fait_title?.setPaycoinsBeanData(payCoinsForTrading ?: arrayListOf(), true)
        ll_payment_title?.setPaymentBeanData(paymentForTrading ?: arrayListOf(), true)
        ll_choose_countries_title?.setCountryNumberInfoData(countryForTrading
                ?: arrayListOf(), true)
        setTransactionType(0)

        tradingAmount = ""
        paymentMethod = ""
        tv_coin_trading.text = ""
        tv_payment_trading.text = ""
        tv_choose_countries_trading.text = ""
        et_money.setText("")
        if (payCoinsForTrading!!.isNotEmpty()) {
            payType = payCoinsForTrading!![0].optString("key")
        }
        if (countryForTrading!!.isNotEmpty()) {
            choosecountries = countryForTrading!![0].optString("numberCode")
        }
    }

    /**  ??????????????????????????????     ************************************************** ************************************************** **************************************************  **/

    fun setView4ContractSelect(view: View, status: Boolean) {
        if (status) {
            view.setBackgroundResource(R.drawable.open)
        } else {
            view.setBackgroundResource(R.drawable.shut_down)
        }
    }

    fun initContract4History() {
        tv_history_contract_title?.text = LanguageUtil.getString(context, "contract_text_hideCancelOrder")
        tv_title_history_contract_type?.text = LanguageUtil.getString(context, "filter_fold_contractOrderType")
        et_currency_history_contract?.hint = LanguageUtil.getString(context, "contract_tip_seiresInput")
        rb_sustainable_history_contract?.text = LanguageUtil.getString(context, "contract_text_perpetual")
        rb_week_history_contract?.text = LanguageUtil.getString(context, "contract_text_currentWeek")
        rb_once_week_history_contract?.text = LanguageUtil.getString(context, "contract_text_nextWeek")
        tv_title_direction_of_position?.text = LanguageUtil.getString(context, "contract_text_positionsDirection")
        rb_price_all_direction_of_position?.text = LanguageUtil.getString(context, "common_action_sendall")
        tv_title_price_type_history_contract?.text = LanguageUtil.getString(context, "contract_text_type")
        tv_title_data_history_contract?.text = LanguageUtil.getString(context, "charge_text_date")


        tv_title_history_contract?.text = LanguageUtil.getString(context, "filter_fold_contractType") + "/" + LanguageUtil.getString(context, "filter_fold_contractLimit")
        switch_visible_history_contract?.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                setView4ContractSelect(switch_visible_history_contract, isChecked)
                contractHistorySelectStatus = isChecked
            }
        })
        pet_select_coin_history_contract?.onTextListener = object : PwdSettingView.OnTextListener {
            override fun showText(text: String): String {
                return text
            }

            override fun returnItem(item: Int) {
            }

            override fun onclickImage() {
                if (ll_sustainable_layout.visibility == View.VISIBLE) {
                    ll_sustainable_layout.visibility = View.GONE
                } else {
                    ll_sustainable_layout.visibility = View.VISIBLE
                }
            }
        }
        et_currency_history_contract?.isFocusable = true
        et_currency_history_contract?.isFocusableInTouchMode = true
        et_currency_history_contract?.setOnFocusChangeListener { v, hasFocus ->
            v_history_contract_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }

        et_currency_history_contract.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                commissionedSeries = s.toString()
            }

        })

        pet_select_coin_history_contract.setEditText(LanguageUtil.getString(context, "contract_text_perpetual"))

        history_contract_screening?.sdv_history_history_contract?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                beginTime = startTime
                endTime = endTimes
            }

        }
        /**
         * ????????????
         */
        rb_sustainable_history_contract.setOnClickListener { setContractTime(0) }
        rb_week_history_contract.setOnClickListener { setContractTime(1) }
        rb_once_week_history_contract.setOnClickListener { setContractTime(2) }

        /**************
         * ??????"????????????" && "????????????"???????????????
         * ????????????: a.????????????  - "ALL" , "??????" , "??????"; b. ???????????? ??????
         * ???????????????: a. ????????????  - "ALL" , "???" , "???" ; b. ???????????? ?????????
         * ****************/

        /**
         * ????????????
         */
        if (Contract2PublicInfoManager.isPureHoldPosition()) {
            ll_position_direction.visibility = View.GONE
            tv_title_direction_of_position.visibility = View.GONE
            rb_buy_history_contract.text = LanguageUtil.getString(context, "contract_text_long")
            rb_sell_history_contract.text = LanguageUtil.getString(context, "contract_text_short")
        } else {
            ll_position_direction.visibility = View.VISIBLE
            tv_title_direction_of_position.visibility = View.VISIBLE
            rb_buy_history_contract.text = LanguageUtil.getString(context, "contract_text_long")
            rb_sell_history_contract.text = LanguageUtil.getString(context, "contract_text_short")
            rb_long_position_history_contract.text = LanguageUtil.getString(context, "contract_text_openAverage")
            rb_market_short_positions_history_contract.text = LanguageUtil.getString(context, "contract_action_closeContract")

        }

        rb_all_history_contract.setOnClickListener { setContractSelectTrading(0) }
        rb_buy_history_contract.setOnClickListener { setContractSelectTrading(1) }
        rb_sell_history_contract.setOnClickListener { setContractSelectTrading(2) }

        /**
         * ????????????
         */
        rb_price_all_history_contract.setOnClickListener { setContractSelectPrice(0) }
        rb_price_history_contract.setOnClickListener { setContractSelectPrice(1) }
        rb_market_price_history_contract.setOnClickListener { setContractSelectPrice(2) }
        /**
         * ????????????
         */
        rb_price_all_direction_of_position.setOnClickListener { setContractPositionsDirectionPrice(0) }
        rb_long_position_history_contract.setOnClickListener { setContractPositionsDirectionPrice(1) }
        rb_market_short_positions_history_contract.setOnClickListener { setContractPositionsDirectionPrice(2) }


    }

    /**
     * ????????????
     */
    fun setContractTime(index: Int) {
        contractTimer = index
        when (index) {
            0 -> {
                pet_select_coin_history_contract.setEditText(LanguageUtil.getString(context, "contract_text_perpetual"))

                rb_sustainable_history_contract?.isLabelEnable = true
                rb_week_history_contract?.isLabelEnable = false
                rb_once_week_history_contract?.isLabelEnable = false
                rb_sustainable_history_contract?.setBg(true)
                rb_week_history_contract?.setBg(false)
                rb_once_week_history_contract?.setBg(false)
            }
            1 -> {
                pet_select_coin_history_contract.setEditText(LanguageUtil.getString(context, "contract_text_currentWeek"))
                rb_sustainable_history_contract?.isLabelEnable = false
                rb_week_history_contract?.isLabelEnable = true
                rb_once_week_history_contract?.isLabelEnable = false
                rb_sustainable_history_contract?.setBg(false)
                rb_week_history_contract?.setBg(true)
                rb_once_week_history_contract?.setBg(false)
            }
            2 -> {
                pet_select_coin_history_contract.setEditText(LanguageUtil.getString(context, "contract_text_nextWeek"))
                rb_sustainable_history_contract?.isLabelEnable = false
                rb_week_history_contract?.isLabelEnable = false
                rb_once_week_history_contract?.isLabelEnable = true
                rb_sustainable_history_contract?.setBg(false)
                rb_week_history_contract?.setBg(false)
                rb_once_week_history_contract?.setBg(true)
            }
        }
    }


    /**
     * ????????????
     */
    fun setContractSelectTrading(index: Int) {
        tradingType = index
        when (index) {
            0 -> {
                rb_all_history_contract?.isLabelEnable = true
                rb_buy_history_contract?.isLabelEnable = false
                rb_sell_history_contract?.isLabelEnable = false
                rb_all_history_contract?.setBg(true)
                rb_buy_history_contract?.setBg(false)
                rb_sell_history_contract?.setBg(false)
            }
            1 -> {
                rb_all_history_contract?.isLabelEnable = false
                rb_buy_history_contract?.isLabelEnable = true
                rb_sell_history_contract?.isLabelEnable = false
                rb_all_history_contract?.setBg(false)
                rb_buy_history_contract?.setBg(true)
                rb_sell_history_contract?.setBg(false)
            }
            2 -> {
                rb_all_history_contract?.isLabelEnable = false
                rb_buy_history_contract?.isLabelEnable = false
                rb_sell_history_contract?.isLabelEnable = true
                rb_all_history_contract?.setBg(false)
                rb_buy_history_contract?.setBg(false)
                rb_sell_history_contract?.setBg(true)
            }
        }
    }

    /**
     * ????????????
     */
    fun setContractSelectPrice(index: Int) {
        priceType = index
        when (index) {
            0 -> {
                rb_price_all_history_contract?.isLabelEnable = true
                rb_price_history_contract?.isLabelEnable = false
                rb_market_price_history_contract?.isLabelEnable = false
                rb_price_all_history_contract?.setBg(true)
                rb_price_history_contract?.setBg(false)
                rb_market_price_history_contract?.setBg(false)

            }
            1 -> {
                rb_price_all_history_contract?.isLabelEnable = false
                rb_price_history_contract?.isLabelEnable = true
                rb_market_price_history_contract?.isLabelEnable = false
                rb_price_all_history_contract?.setBg(false)
                rb_price_history_contract?.setBg(true)
                rb_market_price_history_contract?.setBg(false)
            }
            2 -> {
                rb_price_all_history_contract?.isLabelEnable = false
                rb_price_history_contract?.isLabelEnable = false
                rb_market_price_history_contract?.isLabelEnable = true
                rb_price_all_history_contract?.setBg(false)
                rb_price_history_contract?.setBg(false)
                rb_market_price_history_contract?.setBg(true)
            }
        }
    }

    /**
     * ????????????
     */
    fun setContractPositionsDirectionPrice(index: Int) {
        positionsType = index
        when (index) {
            0 -> {
                rb_price_all_direction_of_position?.isLabelEnable = true
                rb_long_position_history_contract?.isLabelEnable = false
                rb_market_short_positions_history_contract?.isLabelEnable = false
                rb_price_all_direction_of_position?.setBg(true)
                rb_long_position_history_contract?.setBg(false)
                rb_market_short_positions_history_contract?.setBg(false)

            }
            1 -> {
                rb_price_all_direction_of_position?.isLabelEnable = false
                rb_long_position_history_contract?.isLabelEnable = true
                rb_market_short_positions_history_contract?.isLabelEnable = false
                rb_price_all_direction_of_position?.setBg(false)
                rb_long_position_history_contract?.setBg(true)
                rb_market_short_positions_history_contract?.setBg(false)
            }
            2 -> {
                rb_price_all_direction_of_position?.isLabelEnable = false
                rb_long_position_history_contract?.isLabelEnable = false
                rb_market_short_positions_history_contract?.isLabelEnable = true
                rb_price_all_direction_of_position?.setBg(false)
                rb_long_position_history_contract?.setBg(false)
                rb_market_short_positions_history_contract?.setBg(true)
            }
        }
    }


    /**
     * ????????????????????????
     */
    fun historyContractReset() {
        setContractTime(0)
        setContractSelectTrading(0)
        setContractSelectPrice(0)
        setContractPositionsDirectionPrice(0)
        commissionedSeries = ""
        symbolAndUnit = coinList?.get(0) ?: ""
        contractHistorySelectStatus = false
        beginTime = ""
        endTime = ""
        history_contract_screening?.sdv_history_history_contract?.resetTime()
        switch_visible_history_contract?.isChecked = true
        et_currency_history_contract?.setText("")
        pet_select_coin_history_contract.setEditText(context?.getString(R.string.contract_text_perpetual)
                ?: "")
        ll_sustainable_layout.visibility = View.GONE
    }

    /**  ?????????????????????     ************************************************** ************************************************** **************************************************  **/

    fun setViewSelect(view: View, status: Boolean) {
        if (status) {
            view.setBackgroundResource(R.drawable.open)
        } else {
            view.setBackgroundResource(R.drawable.shut_down)
        }
    }

    /**
     * ????????????
     */
    fun initCommissioned() {
        coinList = ArrayList(PublicInfoDataService.getInstance().getMarketSortList(null))
        tv_cancellations_title?.text = LanguageUtil.getString(context, "contract_text_hideCancelOrder")
        tv_title_trading?.text = LanguageUtil.getString(context, "filter_mix_tradeCoinPair")
        et_currency?.hint = LanguageUtil.getString(context, "filter_input_coinsymbol")
        pet_select_coin?.setHintEditText(LanguageUtil.getString(context, "filter_fold_tradeUnit"))
        tv_title_commissioned_type?.text = LanguageUtil.getString(context, "filter_fold_transactionType")
        tv_title_price_type?.text = LanguageUtil.getString(context, "filter_text_currencytype")
        rb_all?.text = LanguageUtil.getString(context, "common_action_sendall")
        rb_buy?.text = LanguageUtil.getString(context, "contract_action_buy")
        rb_sell?.text = LanguageUtil.getString(context, "contract_action_sell")

        rb_price_all?.text = LanguageUtil.getString(context, "common_action_sendall")
        rb_price?.text = LanguageUtil.getString(context, "contract_action_limitPrice")
        rb_market_price?.text = LanguageUtil.getString(context, "contract_action_marketPrice")


        cub_confirm?.isEnable(false)
        if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
            coinList?.add(0, LanguageUtil.getString(context, "common_action_sendall"))
            et_currency?.setText(LanguageUtil.getString(context, "common_text_allDay"))
            cub_confirm?.isEnable(true)
            et_currency?.isFocusableInTouchMode = false
            symbolAndUnit = ""
        } else {
            et_currency?.isFocusableInTouchMode = true
            symbolAndUnit = coinList?.get(0).toString()
            cub_confirm?.isEnable(true)
        }
        rb_all?.setBg(true)
        rb_price_all?.setBg(true)
        ll_total_title?.setStringBeanData(coinList
                ?: arrayListOf(), false)
        ll_total_title?.visibility = View.GONE

        switch_visible_cancellations?.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                setViewSelect(switch_visible_cancellations, isChecked)
                commissiondCheckedStatus = isChecked
            }
        })
        pet_select_coin?.setEditText(NCoinManager.getShowMarket(coinList?.get(0)) ?: "")

        pet_select_coin?.onTextListener = object : PwdSettingView.OnTextListener {
            override fun showText(text: String): String {

                return text
            }

            override fun returnItem(item: Int) {

            }

            override fun onclickImage() {
                commissiondSelectSymbolStatus = !commissiondSelectSymbolStatus
                if (commissiondSelectSymbolStatus) {
                    ll_total_title?.visibility = View.VISIBLE
                } else {
                    ll_total_title?.visibility = View.GONE
                }
            }

        }


        ll_total_title?.setLineSelectOncilckListener(object : LineSelectOnclickListener {
            override fun selectMsgIndex(index: String?) {
                if (index == LanguageUtil.getString(context, "common_action_sendall")) {
                    et_currency?.setText(LanguageUtil.getString(context, "common_text_allDay"))
                    symbolAndUnit = ""
                    pet_select_coin?.setEditText(LanguageUtil.getString(context, "common_action_sendall"))
                    et_currency?.isFocusableInTouchMode = false
                    commissionedSymbol = ""
                    cub_confirm?.isEnable(true)
                } else {
                    symbolAndUnit = NCoinManager.getShowMarket(index) ?: ""
                    et_currency?.isFocusableInTouchMode = true
                    cub_confirm?.isEnable(false)
                    et_currency?.setText("")
                    pet_select_coin?.setEditText(NCoinManager.getShowMarket(index) ?: "")
                }
            }

            override fun sendOnclickMsg() {

            }

        })



        commissioned_layout?.sdv_history?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                Log.d(TAG, "=========RETURNVALUE:$startTime,$endTime========")
                beginTime = startTime
                endTime = endTimes
            }

        }


        /**
         * ????????????
         * ??????
         */
        rb_all?.setOnClickListener {
            setSelectTrading(0)
        }
        /**
         * ????????????
         * ???
         */
        rb_buy?.setOnClickListener {
            setSelectTrading(1)
        }
        /**
         * ????????????
         * ???
         */
        rb_sell?.setOnClickListener {
            setSelectTrading(2)
        }

        /**
         * ????????????
         * ??????
         */
        rb_price_all?.setOnClickListener {
            setSelectPrice(0)
        }
        /**
         * ????????????
         * ??????
         */
        rb_price?.setOnClickListener {
            setSelectPrice(1)
        }
        /**
         * ????????????
         * ??????
         */
        rb_market_price?.setOnClickListener {
            setSelectPrice(2)
        }


        et_currency?.isFocusable = true
        et_currency?.isFocusableInTouchMode = true
        et_currency?.setOnFocusChangeListener { v, hasFocus ->
            v_commissioned_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        et_currency?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var temp = s.toString()
                if (temp.isNotEmpty()) {
                    if (LanguageUtil.getString(context, "common_text_allDay") != s.toString()) {
                        commissionedSymbol = s.toString()
                    }
                    cub_confirm?.isEnable(true)
                } else {
                    cub_confirm?.isEnable(false)
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        setGloblalLayoutListener(et_currency)


    }

    fun filterLeverData() {
        coinList?.clear()
        coinList?.addAll(NCoinManager.getLeverGroup())
        if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
            coinList?.add(0, LanguageUtil.getString(context, "common_action_sendall"))
        }
        ll_total_title.clearLineCoinAdapter(coinList ?: arrayListOf())
    }


    fun setCancellationsLayoutVisible(status: Boolean) {
        rl_cancellations_layout?.visibility = if (status) View.VISIBLE else View.GONE
    }


    /**
     * ????????????
     * ??????????????????????????????
     *
     */
    fun setCommissionedUnit(coins: ArrayList<String>) {
    }


    /**
     * ????????????????????????
     */
    fun commissionedReset() {
        setSelectPrice(0)
        setSelectTrading(0)
        commissionedSymbol = ""
        if (LanguageUtil.getString(context, "common_action_sendall") == coinList?.get(0)) {
            symbolAndUnit = ""
        } else {
            symbolAndUnit = coinList?.get(0) ?: ""
        }
        if (PublicInfoDataService.getInstance().getOpenOrderCollect(null)) {
            et_currency?.setText(LanguageUtil.getString(context, "common_text_allDay"))
            pet_select_coin?.setEditText(LanguageUtil.getString(context, "common_action_sendall"))
            cub_confirm?.isEnable(true)
        } else {
            et_currency?.setText("")
            pet_select_coin?.setEditText(NCoinManager.getShowMarket(symbolAndUnit))
        }
        commissiondCheckedStatus = false
        commissiondSelectSymbolStatus = false
        beginTime = ""
        endTime = ""
        commissioned_layout?.sdv_history?.resetTime()
        switch_visible_cancellations?.isChecked = false
        ll_total_title?.visibility = View.GONE
        ll_total_title?.clearLables()
    }

    /**
     * ????????????
     */
    fun setSelectTrading(index: Int) {
        tradingType = index
        when (index) {
            0 -> {
                rb_all?.isLabelEnable = true
                rb_buy?.isLabelEnable = false
                rb_sell?.isLabelEnable = false
                rb_all?.setBg(true)
                rb_buy?.setBg(false)
                rb_sell?.setBg(false)
            }
            1 -> {
                rb_all?.isLabelEnable = false
                rb_buy?.isLabelEnable = true
                rb_sell?.isLabelEnable = false
                rb_all?.setBg(false)
                rb_buy?.setBg(true)
                rb_sell?.setBg(false)
            }
            2 -> {
                rb_all?.isLabelEnable = false
                rb_buy?.isLabelEnable = false
                rb_sell?.isLabelEnable = true
                rb_all?.setBg(false)
                rb_buy?.setBg(false)
                rb_sell?.setBg(true)
            }
        }
    }

    /**
     * ????????????
     */
    fun setSelectPrice(index: Int) {
        priceType = index
        when (index) {
            0 -> {
                rb_price_all?.isLabelEnable = true
                rb_price?.isLabelEnable = false
                rb_market_price?.isLabelEnable = false
                rb_price_all?.setBg(true)
                rb_price?.setBg(false)
                rb_market_price?.setBg(false)

            }
            1 -> {
                rb_price_all?.isLabelEnable = false
                rb_price?.isLabelEnable = true
                rb_market_price?.isLabelEnable = false
                rb_price_all?.setBg(false)
                rb_price?.setBg(true)
                rb_market_price?.setBg(false)
            }
            2 -> {
                rb_price_all?.isLabelEnable = false
                rb_price?.isLabelEnable = false
                rb_market_price?.isLabelEnable = true
                rb_price_all?.setBg(false)
                rb_price?.setBg(false)
                rb_market_price?.setBg(true)
            }
        }
    }


    /**  ?????????????????????     ************************************************** ************************************************** **************************************************  **/


    fun initAssetTopUp() {
        tv_title_asset_top_up_data?.text = LanguageUtil.getString(context, "charge_text_date")

        asset_top_up?.sdv_asset_top_up?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                Log.d(TAG, "=========RETURNVALUE:$startTime,$endTime========")
                beginTime = startTime
                endTime = endTimes
            }

        }

    }

    fun resetAssetTopUp() {
        beginTime = ""
        endTime = ""
        asset_top_up?.sdv_asset_top_up?.resetTime()
        payCoin = OTCPublicInfoDataService.getInstance().getotcDefaultPaycoin()
    }

    /**  ??????????????????     ************************************************** ************************************************** **************************************************  **/

    fun initMailScreening(beans: ArrayList<String>) {
        mailList.clear()
        mailList.addAll(beans)
        if (mailList.isNotEmpty()) {
            mailType = mailList[0]
        }
        ll_mail_type_title?.setStringBeanData(mailList, false)
        ll_mail_type_title?.setLineSelectOncilckListener(object : LineSelectOnclickListener {

            override fun sendOnclickMsg() {

            }

            override fun selectMsgIndex(index: String?) {
                mailType = index ?: ""

            }

        })
    }

    fun resetMail() {
        ll_mail_type_title?.clearLables()
        ll_mail_type_title?.setStringBeanData(mailList, false)

    }


    /** ????????????     ************************************************** ************************************************** **************************************************  **/

    var withDrawType = 0

    fun initWithDrawView() {
        tv_title_DrawView_data?.text = LanguageUtil.getString(context, "charge_text_date")
        withdraw_record?.sdv_withdraw?.dateListener = object : SelectDateView.IDateValue {
            override fun returnValue(startTime: String, endTimes: String) {
                Log.d(TAG, "=========RETURNVALUE:$startTime,$endTime========")
                beginTime = startTime
                endTime = endTimes
            }
        }

        tv_title_transfer_type?.visibility = View.VISIBLE
        ll_transfer_type_layout?.visibility = View.VISIBLE
        rb_withdraw_all?.setOnClickListener {
            setSelectWithDraw(0)
        }
        rb_withdraw_doing?.setOnClickListener {
            setSelectWithDraw(1)
        }
        rb_withdraw_done?.setOnClickListener {
            setSelectWithDraw(2)
        }

    }

    fun resetWithDraw() {
        if (showTransferType) {
            setSelectWithDraw(0)
        }
        beginTime = ""
        endTime = ""
        withdraw_record?.sdv_withdraw?.resetTime()
    }


    fun setSelectWithDraw(index: Int) {
        withDrawType = index
        when (index) {
            0 -> {
                rb_withdraw_all?.isLabelEnable = true
                rb_withdraw_doing?.isLabelEnable = false
                rb_withdraw_done?.isLabelEnable = false
                rb_withdraw_all?.setBg(true)
                rb_withdraw_doing?.setBg(false)
                rb_withdraw_done?.setBg(false)
            }
            1 -> {
                rb_withdraw_all?.isLabelEnable = false
                rb_withdraw_doing?.isLabelEnable = true
                rb_withdraw_done?.isLabelEnable = false
                rb_withdraw_all?.setBg(false)
                rb_withdraw_doing?.setBg(true)
                rb_withdraw_done?.setBg(false)
            }
            2 -> {
                rb_withdraw_all?.isLabelEnable = false
                rb_withdraw_doing?.isLabelEnable = false
                rb_withdraw_done?.isLabelEnable = true
                rb_withdraw_all?.setBg(false)
                rb_withdraw_doing?.setBg(false)
                rb_withdraw_done?.setBg(true)
            }
        }
    }


    var rootBotom = Integer.MIN_VALUE

    /**
     * ????????????????????????
     * ?????????????????????
     */

    fun setGloblalLayoutListener(editText: CustomizeEditText) {
//        editText.viewTreeObserver.addOnGlobalLayoutListener {
//            var r = Rect()
//            (context as Activity).window.decorView.getWindowVisibleDisplayFrame(r)
//            if (rootBotom == Integer.MIN_VALUE) {
//                rootBotom = r.bottom
//                return@addOnGlobalLayoutListener
//            }
//            if (r.bottom < rootBotom) {
//                var height = DisplayUtils.getWidthHeight(context)[1] - r.bottom
//                setMarginBottomHeight(height)
//            } else {
//                setMarginBottomHeight(marginBottom)
//            }
//
//        }
    }


    fun setImageStatus(view: ImageView, status: Boolean) {
        if (status) {
            view.setImageResource(R.drawable.collapse)
        } else {
            view.setImageResource(R.drawable.dropdown)

        }
    }
    var miningTypeList= arrayListOf<String>()
    var posSymbol=""
    var miningType = 3
    fun initPosRecordLineLayout(data:ArrayList<String>){
        miningTypeList=data
        ll_pos_record_mining.setStringBeanData(data, false)

        ll_pos_record_mining.setLineSelectOncilckListener(object : LineSelectOnclickListener {

            override fun sendOnclickMsg() {

            }

            override fun selectMsgIndex(index: String?) {
                if(miningTypeList[0].equals(index)){
                    miningType=3
                }else if(miningTypeList[1].equals(index)){
                    miningType=1
                }

            }
        })



    }


}
