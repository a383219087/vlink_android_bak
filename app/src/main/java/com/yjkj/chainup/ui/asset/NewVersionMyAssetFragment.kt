package com.yjkj.chainup.ui.asset

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.SPUtils
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractAccount
import com.contract.sdk.impl.ContractAccountListener
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.bean.AssetScreenBean
import com.yjkj.chainup.contract.utils.ContractUtils
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.accet_header_view.view.*
import kotlinx.android.synthetic.main.fragment_new_version_my_asset.*
import org.json.JSONObject

private const val ARG_PARAM1 = "param1"

/**
 * @Author lianshangljl
 * @Date 2019/5/14-7:49 PM
 * @Email buptjinlong@163.com
 * @description 我的资产
 *
 * NOTE:币币、法币（B2C）、场外（OTC）、合约、杠杆
 */
open class NewVersionMyAssetFragment : NBaseFragment() {

    override fun setContentView() = R.layout.fragment_new_version_my_asset

    val assetlist = ArrayList<JSONObject>()
    var tabTitles = arrayListOf<String>()


    val showTitles = arrayListOf<String>()


    private var bibiObject: JSONObject? = null
    private var fabiObject: JSONObject? = null
    private var heyueObject: JSONObject? = null


    private var totalBalance: String? = null
    private var legalCurrency: String? = null

    var contractAssetFragment: ClContractAssetFragment? = null


    /**
     * 场外
     */
    var otcOpen = true

    /**
     * 合约
     */
    var contractOpen = true
    var chooseIndex = 0

    /**
     * b2c
     */
    var b2cOpen = true


    var openContract = 0


    /*
     *  是否已经登录
     */
    var isLogined = false

    /**
     * 是否第一次接受合约数据
     */
    var isFristContract = true

    var contractTotal: Double = 0.0

    val ARG_INDEX = "param_index"

    private var isFromAssetsActivity = false

    companion object {
        @JvmStatic
        fun newInstance(openContract: Int) =
            NewVersionMyAssetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, openContract)
                }
            }
    }


    open fun setFromAssetsActivity(isFromAssetsActivity: Boolean) {
        this.isFromAssetsActivity = isFromAssetsActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        arguments?.let {
            openContract = it.getInt(ARG_PARAM1)
        }
        setAssetViewVisible()
        getAccountBalance()
        getTotalAssets()
        if (openContract == 1) {
            getTotalAccountBalance()
        }

        ContractUserDataAgent.registerContractAccountWsListener(this, object : ContractAccountListener() {
            /**
             * 合约账户ws有更新，
             */
            override fun onWsContractAccount(contractAccount: ContractAccount?) {
                if (PublicInfoDataService.getInstance().contractOpen(null)) {
                    if (isFristContract) {
                        contractTotal = ContractUtils.calculateTotalBalance("BTC")
                        updateAsset(true)
                        isFristContract = false
                    }

                    updateContractAccount()
                }
            }

        })
        NLiveDataUtil.observeData(this) {
            if (MessageEvent.hide_safety_advice == it?.msg_type) {
                rl_safety_advice.visibility =
                    if (PreferenceManager.getBoolean(mActivity, "isShowSafetyAdviceDialog", true)) View.VISIBLE else View.GONE
            }
        }


    }

    // 合约
    private fun getTotalAccountBalance() {
        if (!UserDataService.getInstance().isLogined) return
        addDisposable(
            getMainModel().contractTotalAccountBalanceV2(
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        heyueObject = jsonObject
                        refresh()

                    }
                })
        )
    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            isLogined = UserDataService.getInstance().isLogined
            if (isLogined) {
                setAssetViewVisible()
                getAccountBalance()
                getTotalAssets()
                if (openContract == 1) {
                    getTotalAccountBalance()
                }
            }
        }
    }




    var versionAssetStatus = false
    fun activityRefresh(status: Boolean) {
        versionAssetStatus = status
        val message = MessageEvent(MessageEvent.into_my_asset_activity, versionAssetStatus)
        NLiveDataUtil.postValue(message)
    }


    fun refresh4Homepage() {
        if (UserDataService.getInstance().isLogined) {
            getAccountBalance()
            getTotalAssets()
        }
    }

    var isShowAssets = true
    private fun setAssetViewVisible() {
        isShowAssets = UserDataService.getInstance().isShowAssets
        Utils.showAssetsSwitch(isShowAssets, iv_hide_asset)
    }

    @SuppressLint("SuspiciousIndentation")
    fun setSelectClick() {
        /**
         * 点击隐藏或者显示资金
         */
        iv_hide_asset.setOnClickListener {
            isShowAssets = !isShowAssets
            UserDataService.getInstance().setShowAssetStatus(isShowAssets)
            setAssetViewVisible()
            refresh()
        }

        /**
         *  总资产信息
         */
        tv_assets_title.setOnClickListener {
            SlDialogHelper.showAllAssetDialog(context!!)
        }
        /**
         *  今日盈亏
         */
        tv_today_pl.setOnClickListener {
            SlDialogHelper.showIncomeDialog(context!!)
        }

        rl_safety_advice.setOnClickListener {
            SlDialogHelper.showSimpleSafetyAdviceDialog(context!!, OnBindViewListener { viewHolder ->
                viewHolder?.let {
                    it.getView<TextView>(R.id.tv_cancel_btn).onLineText("common_text_btnCancel")
                    it.setImageResource(R.id.iv_logo, R.drawable.sl_create_contract)
                    it.setText(R.id.tv_text, LanguageUtil.getString(context, "assets_security_advice_tips"))
                    it.setText(R.id.tv_confirm_btn, LanguageUtil.getString(context, "alert_common_i_understand"))
                }

            }, object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    var messageEvent = MessageEvent(MessageEvent.hide_safety_advice)
                    NLiveDataUtil.postValue(messageEvent)
                }
            })
        }

        /**
         *  充币
         */
        ll_top_up_layout?.setOnClickListener {
            if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                NToastUtil.showTopToastNet(mActivity, false, context!!.getString(R.string.important_hint1))
                return@setOnClickListener
            }
            if (Utils.isFastClick()) return@setOnClickListener
            if (PublicInfoDataService.getInstance().depositeKycOpen && UserDataService.getInstance().authLevel != 1) {
                NewDialogUtils.KycSecurityDialog(
                    mActivity!!,
                    context!!.getString(R.string.common_kyc_chargeAndwithdraw),
                    object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    NToastUtil.showTopToastNet(mActivity, false, context?.getString(R.string.noun_login_pending))
                                }

                                2, 3 -> {
                                    ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                        }
                    })
                return@setOnClickListener
            }
            ArouterUtil.navigation(RoutePath.SelectCoinActivity, Bundle().apply {
                putInt(ParamConstant.OPTION_TYPE, ParamConstant.RECHARGE)
                putBoolean(ParamConstant.COIN_FROM, true)
            })


        }
        /**
         *  提币
         */
        ll_otc_layout?.setOnClickListener {
            if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                ToastUtils.showToast(context?.getString(R.string.important_hint1))
                return@setOnClickListener
            }
            if (Utils.isFastClick()) return@setOnClickListener

            bibiObject?.optJSONObject("data")?.run {
                val json = this.optJSONObject("allCoinMap")
                val keys: Iterator<String> = json?.keys() as Iterator<String>
                var balancelist = arrayListOf<JSONObject>()
                while (keys.hasNext()) {
                    val coinMap = json.optJSONObject(keys.next())
                    balancelist.add(coinMap ?: JSONObject())

                }
                balancelist = DecimalUtil.sortByMultiOptions(balancelist, option2 = "coinName")
                balancelist.forEach { it ->
                    if (it.optInt("withdrawOpen") == 1) {
                        if (phoneCertification()) return@setOnClickListener
                        if (PublicInfoDataService.getInstance().withdrawKycOpen && UserDataService.getInstance().authLevel != 1) {
                            NewDialogUtils.KycSecurityDialog(context!!, context?.getString(R.string.common_kyc_chargeAndwithdraw)
                                ?: "", object : NewDialogUtils.DialogBottomListener {
                                override fun sendConfirm() {
                                    when (UserDataService.getInstance().authLevel) {
                                        0 -> {
                                            NToastUtil.showTopToastNet(
                                                mActivity,
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
                            return@setOnClickListener
                        }

                        ArouterUtil.navigation(RoutePath.WithdrawSelectCoinActivity, Bundle().apply {
                            putInt(ParamConstant.OPTION_TYPE, ParamConstant.WITHDRAW)
                            putBoolean(ParamConstant.COIN_FROM, true)
                        })
                        return@setOnClickListener
                    }
                }
                NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(context, "withdraw_tip_notavailable"))

            }


        }
        /**
         *  划转
         */
        ll_transfer_layout?.setOnClickListener {
            if (Utils.isFastClick()) return@setOnClickListener
            if (PublicInfoDataService.getInstance().otcOpen(null)) {
                var list = DataManager.getCoinsFromDB(true)
                if (list.size == 0) {
                    NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(context, "otc_not_open_transfer"))
                    return@setOnClickListener
                }
                list.sortBy { it.sort }
                ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, list[0].name)

            } else if (PublicInfoDataService.getInstance().contractOpen(null)) {
                ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_CONTRACT, "USDT")
                return@setOnClickListener
            }
        }
    }


    fun phoneCertification(type: Int = 2): Boolean {
        if (PublicInfoDataService.getInstance().isEnforceGoogleAuth(null)) {
            if (UserDataService.getInstance().googleStatus != 1) {
                NewDialogUtils.OTCTradingMustPermissionsDialog(context!!, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().googleStatus != 1) {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            return
                        }

                        if (UserDataService.getInstance().nickName.isEmpty()) {
                            //认证状态 0、审核中，1、通过，2、未通过  3未认证
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                            return
                        }

                        if (UserDataService.getInstance().authLevel != 1) {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                            return
                        }

                    }
                }, type = type, title = LanguageUtil.getString(context, "withdraw_tip_bindGoogleFirst"))
                return true
            }
        } else {
            if (UserDataService.getInstance().isOpenMobileCheck != 1 && UserDataService.getInstance().googleStatus != 1) {
                NewDialogUtils.OTCTradingPermissionsDialog(context!!, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().googleStatus != 1) {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            return
                        }

                        if (UserDataService.getInstance().nickName.isEmpty()) {
                            //认证状态 0、审核中，1、通过，2、未通过  3未认证
                            //.enter2(context!!)
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                            return
                        }
                        if (UserDataService.getInstance().authLevel != 1) {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                            return
                        }

                    }

                }, type = 2, title = LanguageUtil.getString(context, "otcSafeAlert_action_bindphoneOrGoogle"))
                return true
            }
        }

        return false
    }

    fun refresh() {
        if (null != totalBalance) {
            Utils.assetsHideShowJrLongData(
                UserDataService.getInstance().isShowAssets,
                tv_assets_btc_balance,
                totalBalance,
                legalCurrency
            )
        }
        if (fabiObject != null) {
            fabiObject!!.optJSONObject("data")?.run {
                val assets_legal_currency_balance =
                    RateManager.getCNYByCoinName(this.optString("totalBalanceSymbol"), this.optString("totalBalance"))
                val assets_btc_balance =
                    BigDecimalUtils.showSNormal(BigDecimalUtils.divForDown(this.optString("totalBalance"), 8).toPlainString(), 8)
                Utils.assetsHideShow(
                    UserDataService.getInstance().isShowAssets,
                    tv_assets_action_bibi3,
                    assets_btc_balance + "(BTC)"
                )
                Utils.assetsHideShow(
                    UserDataService.getInstance().isShowAssets,
                    tv_assets_action_fabi3,
                    assets_legal_currency_balance
                )
            }
        }
        if (bibiObject != null) {
            bibiObject!!.optJSONObject("data")?.run {
                val assets_legal_currency_balance =
                    RateManager.getCNYByCoinName(this.optString("totalBalanceSymbol"), this.optString("totalBalance"))
                val assets_btc_balance =
                    BigDecimalUtils.showSNormal(BigDecimalUtils.divForDown(this.optString("totalBalance"), 8).toPlainString(), 8)
                Utils.assetsHideShow(
                    UserDataService.getInstance().isShowAssets,
                    tv_assets_action_bibi1,
                    assets_btc_balance + "(BTC)"
                )
                Utils.assetsHideShow(
                    UserDataService.getInstance().isShowAssets,
                    tv_assets_action_fabi1,
                    assets_legal_currency_balance
                )
            }
        }
        if (heyueObject != null) {
            ll_heyue.visibility = View.VISIBLE
            heyueObject!!.optJSONObject("data")?.run {
                val assets_legal_currency_balance =
                    RateManager.getCNYByCoinName(this.optString("totalBalanceSymbol"), this.optString("futuresTotalBalance"))
                val assets_btc_balance = BigDecimalUtils.showSNormal(
                    BigDecimalUtils.divForDown(this.optString("futuresTotalBalance"), 8).toPlainString(), 8
                )
                Utils.assetsHideShow(
                    UserDataService.getInstance().isShowAssets,
                    tv_assets_action_bibi2,
                    assets_btc_balance + "(BTC)"
                )
                Utils.assetsHideShow(
                    UserDataService.getInstance().isShowAssets,
                    tv_assets_action_fabi2,
                    assets_legal_currency_balance
                )
            }
        } else {
            ll_heyue.visibility = View.GONE

        }


    }

    @SuppressLint("SuspiciousIndentation")
    override fun initView() {
        setSelectClick()
        val jsonObject = JSONObject()
        jsonObject.put("title", LanguageUtil.getString(context, "otc_bibi_account"))
        jsonObject.put("totalBalanceSymbol", "BTC")
        jsonObject.put("totalBalance", "0")
        jsonObject.put("balanceType", ParamConstant.BIBI_INDEX)
        assetlist.add(jsonObject)
        val otcText = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
            LanguageUtil.getString(context, "assets_text_otc_forotc")
        } else {
            LanguageUtil.getString(context, "assets_text_otc")
        }



        if (b2cOpen) {
            val jsonObject = JSONObject()
            jsonObject.put("title", LanguageUtil.getString(context, "assets_text_otc"))
            jsonObject.put("totalBalanceSymbol", "BTC")
            jsonObject.put("totalBalance", "0")
            jsonObject.put("balanceType", ParamConstant.B2C_INDEX)

            assetlist.add(jsonObject)
        }

        if (otcOpen) {
            val jsonObject = JSONObject()
            jsonObject.put("title", otcText)
            jsonObject.put("totalBalanceSymbol", "BTC")
            jsonObject.put("totalBalance", "0")
            jsonObject.put("balanceType", ParamConstant.FABI_INDEX)
            assetlist.add(jsonObject)
        }
        if (contractOpen) {
            val jsonObject = JSONObject()
            jsonObject.put("title", LanguageUtil.getString(context, "assets_text_contract"))
            jsonObject.put("totalBalanceSymbol", "USDT")
            jsonObject.put("totalBalance", "0")
            jsonObject.put("balanceType", ParamConstant.CONTRACT_INDEX)
            assetlist.add(jsonObject)
        }
        isLogined = UserDataService.getInstance().isLogined
        if (isLogined) {
            setAssetViewVisible()
            getAccountBalance()
            getTotalAssets()

        }

    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.assetsTab_type == event.msg_type) {
            val msg_content = event.msg_content
            if (null != msg_content && msg_content is Bundle) {
                val vpPos = msg_content.getInt(ParamConstant.assetTabType)
                setViewPagePosition(vpPos)
            }
        }
        if (MessageEvent.refresh_local_coin_trans_type == event.msg_type) {
            val msg_content = event.msg_content
            if (null != msg_content && msg_content is Bundle) {
                val content = msg_content.getString(ARG_INDEX)
                when (content) {
                    //币币
                    ParamConstant.BIBI_INDEX -> {
                        getAccountBalance()
                        getTotalAssets()
                    }
                    //法币
                    ParamConstant.FABI_INDEX -> {
                        getAccountBalance4OTC()
                    }

                }
            }else if (msg_content=="bibi,fabi"){
                getAccountBalance()
                getTotalAssets()
                getAccountBalance4OTC()
            }
        }
    }


    var accountBean: JSONObject = JSONObject()

    /**
     * 获取账户信息  法币
     */
    private fun getAccountBalance4OTC() {
        addDisposable(getMainModel().otc_account_list(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var t = jsonObject.optJSONObject("data")
                fabiObject = jsonObject
                refresh()
                if (b2cOpen) {
                    if (assetlist.size > 3) {
                        assetlist[3].put("totalBalance", t.optString("totalBalance") ?: "")
                        assetlist[3].put(
                            "totalBalanceSymbol", t.optString("totalBalanceSymbol")
                                ?: ""
                        )
                    }
                } else if (b2cOpen) {
                    assetlist[2].put("totalBalance", t.optString("totalBalance") ?: "")
                    assetlist[2].put(
                        "totalBalanceSymbol", t.optString("totalBalanceSymbol")
                            ?: ""
                    )
                } else {
                    assetlist[1].put("totalBalance", t.optString("totalBalance") ?: "")
                    assetlist[1].put(
                        "totalBalanceSymbol", t.optString("totalBalanceSymbol")
                            ?: ""
                    )
                }


                if (contractOpen) {
                    getContractAccount()
                }
                refresh()

                var message = MessageEvent(MessageEvent.refresh_local_trans_type)
                message.msg_content = t
                NLiveDataUtil.postValue(message)
            }

            override fun onError(e: Throwable) {
                super.onError(e)
            }
        }))

    }

    var isFristRequest = true

    /**
     * 获取账户信息 bibi
     */
    private fun getAccountBalance() {
        var loadingActivity = activity
        if (!isFristRequest) {
            loadingActivity = null
        }
        addDisposable(getMainModel().accountBalance(object : NDisposableObserver(loadingActivity) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                closeLoadingDialog()
                isFristRequest = false

                var json = jsonObject.optJSONObject("data")
                bibiObject = jsonObject
                refresh()

                accountBean = json
                assetlist[0].put("totalBalance", json.optString("totalBalance") ?: "")
                assetlist[0].put(
                    "totalBalanceSymbol", json.optString("totalBalanceSymbol")
                        ?: ""
                )
                when {

                    otcOpen -> {
                        getAccountBalance4OTC()
                    }
                    openContract==1 -> {
                        getContractAccount()
                    }
                }


                refresh()

                var message = MessageEvent(MessageEvent.refresh_local_coin_trans_type)
                message.msg_content = json
                NLiveDataUtil.postValue(message)

            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                isFristRequest = false
            }


        }))
    }

    private fun getContractAccount() {
        try {
            ContractUserDataAgent.getContractAccounts(true)
        } catch (e: Exception) {
        }
    }

    var titleStatus = false
    var viewpagePosotion = 0

    /**
     * 更新账户信息  合约
     */
    @SuppressLint("SuspiciousIndentation")
    private fun updateContractAccount() {
        val totalBalanceSymbol = "BTC"
        val totalBalance = ContractUtils.calculateTotalBalance(totalBalanceSymbol)

        if (b2cOpen && otcOpen) {
            if (assetlist.size > 4) {
                assetlist[4].put("totalBalance", totalBalance)
                assetlist[4].put("totalBalanceSymbol", totalBalanceSymbol)
            }
        } else if ((b2cOpen && otcOpen) || (otcOpen) || (b2cOpen)) {
            if (assetlist.size > 3) {
                assetlist[3].put("totalBalance", totalBalance)
                assetlist[3].put("totalBalanceSymbol", totalBalanceSymbol)
            }
        } else if ((!b2cOpen && otcOpen) || (b2cOpen && !otcOpen) || (!b2cOpen && !b2cOpen)) {
            assetlist[2].put("totalBalance", totalBalance)
            assetlist[2].put("totalBalanceSymbol", totalBalanceSymbol)
        } else {
            assetlist[1].put("totalBalance", totalBalance)
            assetlist[1].put("totalBalanceSymbol", totalBalanceSymbol)
        }


        //刷新header
        refresh()
        //通知列表刷新
        contractAssetFragment?.setRefreshAdapter()

    }



    /**
     * 获取总资产
     */
    private fun getTotalAssets() {
        addDisposable(
            getMainModel().getTotalAsset(
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val data = jsonObject.optJSONObject("data")
                        totalBalance = if (null == totalBalance) {
                            data.optString("totalBalance")
                        } else {
                            BigDecimalUtil.add(data.optString("totalBalance"), contractTotal.toString(), 8).toPlainString()
                        }
//                        总资产折合计算： 币币总资产+ 法币总资产 +合约总资产估值 +杠杆净资产（总资产 - 借贷资产）
                        bibiSHouyi()
                        updateAsset(false)
                    }
                })
        )
    }

    /**
     * 币币收益
     */
    private fun bibiSHouyi() {
        if (!UserDataService.getInstance().isLogined) return
//        {
//                   "nowBalance": 8410.16,
//                   "yesterdayBalance": 8920.74,
//                   "rate": -5.72,
//                   "yesterdayAccountAmount": 8920.74,
//                   "usdt": -510.57,
//                   "deposit": 0,
//                   "sumToFutures": 0,
//                   "sumFromFutures": 0,
//                   "withdraw": 0
//               }
        addDisposable(
            getMainModel().accountStats(
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            val usdt = opt("usdt").toString().toDouble()
                            val balance = opt("yesterdayBalance").toString().toDouble()
                            if (openContract == 1) {
                                addDisposable(
                                    getMainModel().accountStatsCon(
                                        consumer = object : NDisposableObserver(true) {
                                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                                jsonObject.optJSONObject("data").run {
                                                    val usdt1 = opt("usdt").toString().toDouble()
                                                    val balance1 = opt("yesterdayBalance").toString().toDouble()
                                                    val rate = BigDecimalUtil.getFixedPointNum3(
                                                        ((usdt + usdt1) * 100 / (balance + balance1)).toString(),
                                                        2
                                                    );
                                                    val rateU = BigDecimalUtil.getFixedPointNum3(((usdt + usdt1)).toString(), 2);
                                                    accountStats(rate, rateU)
                                                }
                                            }
                                        })
                                )

                            } else {
                                val rate = BigDecimalUtil.getFixedPointNum3(((usdt) * 100 / (balance)).toString(), 2);
                                val rateU = BigDecimalUtil.getFixedPointNum3(((usdt)).toString(), 2);
                                accountStats(rate, rateU)
                            }


                        }
                    }
                })
        )
    }

    //收益分析
    private fun accountStats(rate: String, usdt: String) {
        tv_rate.text = "$rate%"
        tv_usdt.text = usdt
        tv_cny.text = RateManager.getCNYByCoinName("USDT", usdt)
        if (rate.contains("-")) {
            tv_rate.setTextColor(resources.getColor(R.color.main_red))
            tv_usdt.setTextColor(resources.getColor(R.color.main_red))
        } else {
            tv_rate.setTextColor(resources.getColor(R.color.main_green))
            tv_usdt.setTextColor(resources.getColor(R.color.main_green))
        }
    }


    fun updateAsset(status: Boolean) {
        totalBalance = if (status) {
            BigDecimalUtil.add(totalBalance, contractTotal.toString(), 8).toPlainString()
        } else {
            BigDecimalUtil.add(totalBalance, "0", 8).toPlainString()
        }
        legalCurrency = RateManager.getCNYByCoinName("BTC", totalBalance)
        Utils.assetsHideShowJrLongData(
            UserDataService.getInstance().isShowAssets,
            tv_assets_btc_balance,
            totalBalance,
            legalCurrency
        )

    }


    fun hideTitle(status: Boolean) {
        titleStatus = status
    }

    fun setViewPagePosition(position: Int) {
        chooseIndex = position
        viewpagePosotion = position
    }


}

private const val ARG_PARAM2 = "param2"

