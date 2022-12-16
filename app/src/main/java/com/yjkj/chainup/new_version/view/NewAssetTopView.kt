package com.yjkj.chainup.new_version.view

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.SPUtils
import com.timmy.tdialog.listener.OnBindViewListener
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.AssetScreenBean
import com.yjkj.chainup.contract.utils.ContractUtils
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.activity.CashFlow4Activity
import com.yjkj.chainup.new_version.activity.asset.NewVersionContractBillActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.ui.asset.NewVersionAssetOptimizeDetailFragment
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.accet_header_view.view.*
import kotlinx.android.synthetic.main.fragment_new_version_my_asset.*
import kotlinx.android.synthetic.main.sl_activity_contract_entrust_detail.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-08-22-14:41
 * @Email buptjinlong@163.com
 * @description
 */
class NewAssetTopView @JvmOverloads constructor(context: Activity, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    /**
     * bibi 是币币
     * bibao 是币宝
     * fabi 是otc
     * b2c
     */
    private var param_index: String = ""

    private var assetsTitle = LanguageUtil.getString(context, "assets_crypto_asset_value")

    /**
     * 隐藏小额资产
     */
    private var isLittleAssetsShow = false

    var assetScreen = AssetScreenBean("", "")

    init {
        initView(context)
    }

    var listener: selecetTransferListener? = null

    interface selecetTransferListener {
        fun selectTransfer(param_index: String)
        fun leverageFilter(temp: String)
        fun fiatFilter(temp: String)
        fun bibiFilter(temp: String)
        fun b2cFilter(temp: String)
        fun selectWithdrawal(temp: String)
        fun selectRedEnvelope(temp: String)
        fun isShowAssets()

        fun clickAssetsPieChart()
    }

    fun initView(context: Activity) {
        LayoutInflater.from(context).inflate(R.layout.accet_header_view, this, true)
        setRefreshViewData()
        setSelectClick(context)
        tv_contract_text_orderMargin?.text = LanguageUtil.getString(context, "contract_text_orderMargin")
        tv_noun_order_paymentTerm?.text = LanguageUtil.getString(context, "noun_order_paymentTerm")
        tv_assets_action_transfer?.text = LanguageUtil.getString(context, "assets_action_transfer")
        tv_assets_action_journalaccount?.text = LanguageUtil.getString(context, "assets_action_journalaccount")
        tv_withdraw_text_available?.text = LanguageUtil.getString(context, "withdraw_text_available")
        tv_contract_text_positionMargin?.text = LanguageUtil.getString(context, "contract_text_positionMargin")
        tv_contract_text_orderMargin?.text = LanguageUtil.getString(context, "contract_text_orderMargin")
        fragment_my_asset_order_hide?.text = LanguageUtil.getString(context, "assets_action_privacy")
        et_search?.hint = LanguageUtil.getString(context, "assets_action_search")
    }

    fun setAssetOrderHide(status: Boolean) {
        isLittleAssetsShow = status
        fragment_my_asset_order_hide?.isChecked = isLittleAssetsShow
        et_search?.setText("")
    }

    //收益分析
    fun accountStats(rate: String, usdt: String) {
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

    fun setRefreshViewData() {
        isLittleAssetsShow = UserDataService.getInstance().assetState
        fragment_my_asset_order_hide?.isChecked = isLittleAssetsShow
        et_search?.setText("")
        val isShowAssets = UserDataService.getInstance().isShowAssets
        Utils.showAssetsSwitch(isShowAssets, iv_hide_asset)
    }

    fun clearEdittext() {
        et_search?.setText("")
    }

    fun setEdittext(str: String) {
        et_search?.setText(str)
    }

    fun setSelectClick(context: Activity) {
        /**
         *  今日盈亏
         */
        tv_today_pl.setOnClickListener {
            SlDialogHelper.showIncomeDialog(context)
        }
        /**
         * 显示或者隐藏资产
         */
        iv_hide_asset.setOnClickListener {
            val isShowAssets = UserDataService.getInstance().isShowAssets
            UserDataService.getInstance().setShowAssetStatus(!isShowAssets)
            Utils.showAssetsSwitch(!isShowAssets, iv_hide_asset)
            if (null != listener) {
                listener?.isShowAssets()
            }
        }
        /**
         * 查看安全建议
         */
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
         * 是否隐藏小额资产
         */
        fragment_my_asset_order_hide?.setOnClickListener {
            var message = MessageEvent(MessageEvent.refresh_trans_type)
            NLiveDataUtil.postValue(message)

        }
        /**
         *  充币
         */
        ll_top_up_layout?.setOnClickListener {
            if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                ToastUtils.showToast(context.getString(R.string.important_hint1))
                return@setOnClickListener
            }
            if (Utils.isFastClick()) return@setOnClickListener
            if (param_index == ParamConstant.BIBI_INDEX) {
                if (PublicInfoDataService.getInstance().depositeKycOpen && UserDataService.getInstance().authLevel != 1) {
                    NewDialogUtils.KycSecurityDialog(context,
                        context.getString(R.string.common_kyc_chargeAndwithdraw),
                        object : NewDialogUtils.DialogBottomListener {
                            override fun sendConfirm() {
                                when (UserDataService.getInstance().authLevel) {
                                    0 -> {
                                        NToastUtil.showTopToastNet(context, false, context.getString(R.string.noun_login_pending))
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
            } else {
                if (realNameCertification()) {
                    ArouterUtil.navigation(RoutePath.SelectCoinActivity, Bundle().apply {
                        putInt(ParamConstant.OPTION_TYPE, ParamConstant.RECHARGE)
                        putString(ParamConstant.ASSET_ACCOUNT_TYPE, ParamConstant.B2C_ACCOUNT)
                        putBoolean(ParamConstant.COIN_FROM, true)
                    })
                }

            }

        }
        /**
         *  提币
         */
        ll_otc_layout?.setOnClickListener {
            if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                ToastUtils.showToast(context.getString(R.string.important_hint1))
                return@setOnClickListener
            }
            if (Utils.isFastClick()) return@setOnClickListener
            if (param_index == ParamConstant.BIBI_INDEX) {
                if (null != listener) {
                    listener?.selectWithdrawal(param_index)
                }
            } else {
                if (realNameCertification()) {
                    ArouterUtil.navigation(RoutePath.SelectCoinActivity, Bundle().apply {
                        putInt(ParamConstant.OPTION_TYPE, ParamConstant.WITHDRAW)
                        putString(ParamConstant.ASSET_ACCOUNT_TYPE, ParamConstant.B2C_ACCOUNT)
                        putBoolean(ParamConstant.COIN_FROM, true)
                    })
                }

            }
        }


        /**
         *  收款方式
         */
        ll_payment_methods_layout?.setOnClickListener {
            if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                ToastUtils.showToast(context.getString(R.string.important_hint1))
                return@setOnClickListener
            }
            if (Utils.isFastClick()) return@setOnClickListener
            ArouterUtil.greenChannel(RoutePath.PaymentMethodActivity, null)
        }
        /**
         *  划转(币币)
         */
        ll_transfer_layout?.setOnClickListener {
            if (Utils.isFastClick()) return@setOnClickListener
            if (null != listener) {
                listener?.selectTransfer(param_index)
            }
        }
        /**
         *  划转(合约)
         */
        ll_transfer_layout1.setOnClickListener {
            if (Utils.isFastClick()) return@setOnClickListener
            if (null != listener) {
                listener?.selectTransfer(param_index)
            }
        }
        /**
         *  划转(法币)
         */
        ll_transfer_layout3.setOnClickListener {
            if (Utils.isFastClick()) return@setOnClickListener
            if (null != listener) {
                listener?.selectTransfer(param_index)
            }
        }
        /**
         *  资金流水
         */
        ll_funds_layout?.setOnClickListener {
            if (Utils.isFastClick()) return@setOnClickListener
            when (param_index) {
                ParamConstant.BIBI_INDEX -> {
                    CashFlow4Activity.enter2(context, ParamConstant.TYPE_DEPOSIT)
                }
                ParamConstant.FABI_INDEX -> {
                    CashFlow4Activity.enter2(context, ParamConstant.TYPE_OTC_TRANSFER)
                }
                ParamConstant.B2C_INDEX -> {
                    ArouterUtil.navigation(RoutePath.B2CCashFlowActivity, null)
                }
                ParamConstant.LEVER_INDEX -> {
                    ArouterUtil.navigation(RoutePath.LeverDrawRecordActivity, null)
                }
            }
        }
        /**
         *  合约账单
         */
        ll_transfer_layout2?.setOnClickListener {
            if (Utils.isFastClick()) return@setOnClickListener
            NewVersionContractBillActivity.enter2(context)
        }

        /**
         * 饼状图
         */
        img_assets_pie_chart?.setOnClickListener {
            if (Utils.isFastClick()) return@setOnClickListener
            if (null != listener) {
                listener?.clickAssetsPieChart()
            }
        }

        /**
         * 监听搜索编辑框
         */

        et_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 如果adapter不为空的话就根据编辑框中的内容来过滤数据
                et_search?.isFocusable = true
                et_search?.isFocusableInTouchMode = true
                    if (null != listener) {
                        when (param_index) {
                            ParamConstant.BIBI_INDEX -> {
                                listener?.bibiFilter(s.toString())
                            }
                            ParamConstant.FABI_INDEX -> {
                                listener?.fiatFilter(s.toString())
                            }

                        }
                    }

            }
        })


        img_small_assets_tip.setOnClickListener {
            NewDialogUtils.showDialog(context!!,
                LanguageUtil.getString(context, "assets_less_than_0.0001BTC"),
                true,
                object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {

                    }
                },
                "",
                LanguageUtil.getString(context, "alert_common_i_understand"),
                "")
        }


    }

    /**
     * 设置页面
     */
    fun initNorMalView(index: String?) {
        param_index = index ?: ""
        assetScreen.index4Asset = param_index
        ll_bibi.visibility = View.GONE
        ll_heyue.visibility = View.GONE
        ll_fabi.visibility = View.GONE
        when (param_index) {
            ParamConstant.BIBI_INDEX -> {
                ll_bibi.visibility = View.VISIBLE
                ll_payment_methods_layout?.visibility = View.GONE
                img_assets_pie_chart?.visibility = View.VISIBLE

                assetsTitle = LanguageUtil.getString(context, "assets_crypto_asset_value")
            }
            ParamConstant.FABI_INDEX -> {
                ll_income.visibility = View.GONE
                ll_fabi.visibility = View.VISIBLE
                ll_payment_methods_layout?.visibility = View.VISIBLE
                ll_otc_layout?.visibility = View.GONE
                ll_top_up_layout?.visibility = View.GONE

                assetsTitle = LanguageUtil.getString(context, "assets_fiat_account_value")
            }
            ParamConstant.CONTRACT_INDEX -> {
                ll_heyue_balance.visibility = View.VISIBLE
                ll_heyue.visibility = View.VISIBLE
                ll_payment_methods_layout?.visibility = View.GONE
                if (PublicInfoDataService.getInstance().contractCouponOpen(null)) View.VISIBLE else View.GONE
                v_top_line?.visibility = View.VISIBLE
                ll_otc_layout?.visibility = View.GONE
                ll_top_up_layout?.visibility = View.GONE
                ll_funds_layout?.visibility = View.GONE
                rl_search_layout?.visibility = View.GONE

                assetsTitle = LanguageUtil.getString(context, "assets_contract_value")
            }
            ParamConstant.B2C_INDEX -> {
                ll_payment_methods_layout?.visibility = View.GONE
                v_top_line?.visibility = View.GONE // 划转
                ll_transfer_layout?.visibility = View.GONE
                ll_otc_layout?.visibility = View.VISIBLE
                ll_top_up_layout?.visibility = View.VISIBLE
                ll_funds_layout?.visibility = View.VISIBLE
                rl_search_layout?.visibility = View.VISIBLE

                assetsTitle = LanguageUtil.getString(context, "assets_fiat_account_value")
            }
            ParamConstant.LEVER_INDEX -> {
                ll_top_up_layout?.visibility = View.GONE
                ll_otc_layout?.visibility = View.GONE

                assetsTitle = LanguageUtil.getString(context, "assets_margin_account_value")
            }
        }
    }

    fun setRefreshAdapter() {
        if (param_index == "contract") {
            tv_assets_title.setText(assetsTitle + "(BTC)")
            val totalBalanceSymbol = "BTC"
            val totalBalance = ContractUtils.calculateTotalBalance(totalBalanceSymbol)
            val assets_legal_currency_balance = RateManager.getCNYByCoinName(totalBalanceSymbol, totalBalance.toString())

            var isShowAssets = UserDataService.getInstance().isShowAssets
            Utils.assetsHideShow(isShowAssets, tv_assets_btc_balance, totalBalance.toString())
            Utils.assetsHideShow(isShowAssets, tv_assets_legal_currency_balance, assets_legal_currency_balance)
        }
    }


    /**
     * 合约总资产
     */
    fun setContractHeadData(jsonObject: JSONObject) {
        val assets_legal_currency_balance =
            RateManager.getCNYByCoinName(jsonObject?.optString("totalBalanceSymbol"), jsonObject?.optString("futuresTotalBalance"))
        val assets_btc_balance =
            BigDecimalUtils.showSNormal(BigDecimalUtils.divForDown(jsonObject?.optString("futuresTotalBalance"), 8).toPlainString(), 8)
        tv_assets_title.setText(LanguageUtil.getString(context, "assets_contract_value") + "(BTC)")
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_assets_btc_balance, assets_btc_balance)
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_assets_legal_currency_balance, assets_legal_currency_balance)
    }

    /**
     * 合约保证金余额
     */
    fun setContractHeadData1(jsonObject: JSONObject) { //                        {"symbol":"USDT","totalAmount":"0","canUseAmount":0.0,"isolateMargin":"0","lockAmount":"0",
        //                            "unRealizedAmount":"0","realizedAmount":"0","totalMargin":"0","totalMarginRate":"0"}
        //                        钱包余额 用 totalAmount
        //                       lockAmount 保证金余额
        //                        unRealizedAmount 为实现盈亏

        val bibi1 = BigDecimalUtils.showSNormal(BigDecimalUtils.divForDown(jsonObject?.optString("lockAmount"), 2).toPlainString(), 2)
        val fabi1 = RateManager.getCNYByCoinName("USDT", jsonObject?.optString("lockAmount"))
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_bibi_1, bibi1)
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_fabi_1, fabi1)
        val bibi2 = BigDecimalUtils.showSNormal(BigDecimalUtils.divForDown(jsonObject?.optString("totalAmount"), 2).toPlainString(), 2)
        val fabi2 = RateManager.getCNYByCoinName("USDT", jsonObject?.optString("totalAmount"))
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_bibi_2, bibi2)
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_fabi_2, fabi2)
        val bibi3 = BigDecimalUtils.showSNormal(BigDecimalUtils.divForDown(jsonObject?.optString("unRealizedAmount"), 2).toPlainString(), 2)
        val fabi3 = RateManager.getCNYByCoinName("USDT", jsonObject?.optString("unRealizedAmount"))
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_bibi_3, bibi3)
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_fabi_3, fabi3)


    }

    fun setHeadData(jsonObject: JSONObject) {
        val assets_legal_currency_balance =
            RateManager.getCNYByCoinName(jsonObject?.optString("totalBalanceSymbol"), jsonObject?.optString("totalBalance"))
        val assets_btc_balance = BigDecimalUtils.showSNormal(BigDecimalUtils.divForDown(jsonObject?.optString("totalBalance"), 8).toPlainString(), 8)
        tv_assets_title.setText(assetsTitle + "(BTC)")
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_assets_btc_balance, assets_btc_balance)
        Utils.assetsHideShow(UserDataService.getInstance().isShowAssets, tv_assets_legal_currency_balance, assets_legal_currency_balance)
    }

    var symbol4Contract: JSONObject = JSONObject()

    /**
     * 合约 账户余额
     */
    fun initAdapterView(list: JSONArray) {
        if (list.length() <= 0) {
            return
        }
        ll_contract_content_layout.visibility = View.VISIBLE
        /**
         * 可用余额
         */
        var symbol = list.optJSONObject(0)
        symbol4Contract = symbol

        setRefreshAdapter()

    }

    fun realNameCertification(): Boolean {
        if (UserDataService.getInstance().authLevel != 1) {
            NewDialogUtils.OTCTradingOnlyPermissionsDialog(context, object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    when (UserDataService.getInstance().authLevel) {
                        0 -> {
                            ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                        }

                        2, 3 -> {
                            ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                        }
                    }
                }

            }, context.getString(R.string.otc_please_cert))
            return false
        }
        return true
    }

    fun skipCoinMap4Lever() {
        ArouterUtil.navigation(RoutePath.CoinMapActivity, Bundle().apply {
            putBoolean(ParamConstant.SEARCH_COIN_MAP_FOR_LEVER, true)
            putBoolean(ParamConstant.SEARCH_COIN_MAP_FOR_LEVER_UNREFRESH, true)
        })
    }

    fun getItemToastView(): View {
        return img_assets_pie_chart
    }

}