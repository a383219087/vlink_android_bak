package com.yjkj.chainup.new_version.activity.asset

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.CoinActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.TRANSFER_RECORD
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.activity_directly_withdraw.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020-07-03-15:53
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.DirectlyWithdrawActivity)
class DirectlyWithdrawActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_directly_withdraw

    @JvmField
    @Autowired(name = ParamConstant.JSON_BEAN)
    var bean = ""

    var showSymbol = ""
    var coinPrecision = 2
    var jsonBean: JSONObject? = null

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
        collapsing_toolbar?.title = LanguageUtil.getString(this, "assets_action_internalTransfer")
        setContentText()
        initView()
        onSetClick()
        getCost()
    }

    fun setContentText() {
        tv_amount?.text = LanguageUtil.getString(this, "transfer_transferAlert_contentA")
        tv_amount_day?.text = LanguageUtil.getString(this, "transfer_transferAlert_contentB")
        tv_fee_section?.text = LanguageUtil.getString(this, "transfer_transferAlert_contentC")
        tv_instructions_title?.text = LanguageUtil.getString(this, "transfer_tip_notice")
        tv_withdraw_adr_title?.text = LanguageUtil.getString(this, "internalTransfer_text_address")
        tv_number_title?.text = LanguageUtil.getString(this, "charge_text_volume")
        tv_available_balance?.text = LanguageUtil.getString(this, "sl_str_available_balance")
        tv_recharge_record?.text = LanguageUtil.getString(this, "internalTransfer_action_History")
        tv_fee_title?.text = LanguageUtil.getString(this, "withdraw_text_fee")
        tv_withdraw_text_moneyWithoutFee?.text = LanguageUtil.getString(this, "withdraw_text_moneyWithoutFee")
        cubtn_confirm?.setContent(LanguageUtil.getString(this, "common_text_btnConfirm"))
        cet_withdraw_adr?.hint = LanguageUtil.getString(this, "filter_Input_placeholder") + LanguageUtil.getString(this, "internalTransfer_text_address")
    }

    var normal_balance = ""

    override fun initView() {
        super.initView()
        jsonBean = JSONObject(bean)
        showSymbol = jsonBean?.optString("coinName", "") ?: ""
        coinPrecision = NCoinManager.getCoinShowPrecision(showSymbol)
        normal_balance = jsonBean?.optString("normal_balance") ?: "0"
        tv_available_balance?.text = "${LanguageUtil.getString(this, "withdraw_text_available")}${BigDecimalUtils.divForDown(jsonBean?.optString("normal_balance"), coinPrecision).toPlainString()}"

        /**
         * 对方账户地址
         */
        cet_withdraw_adr?.isFocusable = true
        cet_withdraw_adr?.isFocusableInTouchMode = true
        cet_withdraw_adr?.setOnFocusChangeListener { v, hasFocus ->
            view_withdraw_adr_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        cet_withdraw_adr?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        /**
         * 转账数量
         */
        et_amount?.isFocusable = true
        et_amount?.isFocusableInTouchMode = true
        et_amount?.setOnFocusChangeListener { v, hasFocus ->
            view_amount_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        et_amount?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                realAmount(innerTransferFee, s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }


    private fun realAmount(fee: String, amount: String) {
        var value = "0"
        if (!TextUtils.isEmpty(amount)) {
            value = amount
        }

        if (value.startsWith(".")) {
            value = "0"
        }

        if (value.endsWith(".")) {
            value = value.substring(0, value.length - 1)
        }
        /**
         * 实际到账数量
         */
        var result = BigDecimalUtils.sub(value, fee).toString()
        /**
         * 为避免其出现科学计数法
         */
        if (amount.isEmpty()) {
            tv_real_amount?.text = "0" + NCoinManager.getShowMarket(showSymbol)
        } else {
            tv_real_amount?.setTextColor(ColorUtil.getColor(R.color.main_font_color))
            tv_real_amount?.text = BigDecimalUtils.divForDown(result, coinPrecision).toPlainString() + NCoinManager.getShowMarket(showSymbol)
        }

    }


    var innerTransferFee = "0"
    var maxAmount = ""
    var minAmount = ""
    var todayAmount = ""

    /**
     * 加载数据
     */
    fun setFeeView(json: JSONObject) {
        tv_fee_symbol?.text = NCoinManager.getShowMarket(showSymbol)
        tv_coin_name?.text = NCoinManager.getShowMarket(showSymbol)
        tv_symbol_name?.text = NCoinManager.getShowMarket(showSymbol)
        maxAmount = json.optString("withdraw_max")
        minAmount = json.optString("withdraw_min")
        todayAmount = json.optString("withdraw_max_day")
        tv_amount?.text = "${LanguageUtil.getString(this, "transfer_transferAlert_contentA")}${BigDecimalUtils.showNormal(json.optString("withdraw_min"))}$showSymbol"
        tv_amount_day?.text = "${LanguageUtil.getString(this, "transfer_transferAlert_contentB")}${BigDecimalUtils.showNormal(json.optString("withdraw_max"))}$showSymbol"
        tv_fee_section?.text = "${LanguageUtil.getString(this, "transfer_transferAlert_contentC")}${BigDecimalUtils.showNormal(json.optString("withdraw_max_day"))}$showSymbol"
        innerTransferFee = json.optString("innerTransferFee")
        et_fee?.setText(BigDecimalUtils.showNormal(innerTransferFee))
        et_amount.setHint(getString(R.string.withdraw_text_minimumVolume) +" "+minAmount)
    }

    fun onSetClick() {
        /**
         * 提币
         */
        cubtn_confirm?.isEnable(true)
        cubtn_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {

                val address = cet_withdraw_adr?.text.toString()

                if (TextUtils.isEmpty(address)) {
                    NToastUtil.showTopToast(false, LanguageUtil.getString(this@DirectlyWithdrawActivity, "common_tip_targetAccount"))
                    return
                }

                val amount = et_amount?.text.toString()

                if (BigDecimalUtils.compareTo(BigDecimalUtils.divForDown(jsonBean?.optString("normal_balance"), coinPrecision).toPlainString(), amount) < 0) {
                    NToastUtil.showTopToast(false, "${LanguageUtil.getString(this@DirectlyWithdrawActivity, "common_tip_balanceNotEnough")}")
                    return
                }

                if (TextUtils.isEmpty(amount)) {
                    NToastUtil.showTopToast(false, LanguageUtil.getString(this@DirectlyWithdrawActivity, "transfer_tip_emptyVolume"))
                    return
                }
                if (BigDecimalUtils.compareTo(maxAmount, amount) < 0) {
                    NToastUtil.showTopToast(false, "${LanguageUtil.getString(this@DirectlyWithdrawActivity, "internalTransfer_tip_maxValueError")}$maxAmount")
                    return
                }
                if (BigDecimalUtils.compareTo(minAmount, amount) > 0) {
                    NToastUtil.showTopToast(false, "${LanguageUtil.getString(this@DirectlyWithdrawActivity, "internalTransfer_tip_minValueError")}$minAmount")
                    return
                }

                if (BigDecimalUtils.compareTo(amount, todayAmount) > 0) {
                    NToastUtil.showTopToast(false, "${LanguageUtil.getString(this@DirectlyWithdrawActivity, "common_tip_todayRemaining")}$todayAmount")
                    return
                }

                sendTransfer(address)
            }
        }
        /**
         * 点击切换币种
         */
        rl_symbol_name?.setOnClickListener {
            ArouterUtil.navigation4Result(RoutePath.SelectCoinActivity, Bundle().apply {
                putInt(ParamConstant.OPTION_TYPE, ParamConstant.INNEROPEN)
                putBoolean(ParamConstant.COIN_FROM, false)
            }, this, 321)
        }

        /**
         * 转账记录
         */
        tv_recharge_record?.setOnClickListener {
            WithDrawRecordActivity.enter2(this, showSymbol, ParamConstant.DIRECTLY_WITHDRAW_TYPE, TRANSFER_RECORD)
        }
        /**
         * 全部
         */
        btn_all_amount?.setOnClickListener {
            et_amount?.setText(normal_balance)
        }

    }


    /**
     * 扫码结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            cet_withdraw_adr?.setText("")
            et_amount?.setText("")
            et_fee?.setText("")
            when (requestCode) {
                /**
                 * 币种
                 */
                321 -> {
                    showSymbol = data?.getStringExtra(CoinActivity.SELECTED_COIN) ?: ""
                    coinPrecision = NCoinManager.getCoinShowPrecision(showSymbol)
                    getCost()
                }

            }
        }
    }

    var tDialog: TDialog? = null
    /**
     * 确认转账接口
     */
    fun sendTransfer(id: String) {
        addDisposable(getMainModel().innerTransferUserAuth(id, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                tDialog = NewDialogUtils.showSecondDialog(this@DirectlyWithdrawActivity, AppConstant.CONFIRM_TRANSFER_IPHONE, object : NewDialogUtils.DialogSecondListener {
                    override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {
                        val amount = BigDecimalUtils.sub(et_amount?.text.toString(), innerTransferFee).toString()
                        val fee = et_fee?.text.toString()
                        innerTransferUserAuth(id, amount, fee, showSymbol, phone ?: "", googleCode
                                ?: "")

                        tDialog?.dismiss()
                    }
                }, loginPwdShow = false, cointype4Email = AppConstant.CONFIRM_TRANSFER_EMAIL)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToast(false, msg)
            }
        }))

    }

    /**
     * 内部转账用户认证
     */
    fun innerTransferUserAuth(id: String, amount: String, fee: String, symbol: String, smsAuthCode: String, googleCode: String) {
        addDisposable(getMainModel().innerTransferDoWithdraw(id, amount, fee, symbol, smsAuthCode, googleCode, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                NToastUtil.showTopToast(true, LanguageUtil.getString(this@DirectlyWithdrawActivity, "internalTransfer_tip_success"))
                finish()
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToast(false, msg)
            }
        }))

    }


    /**
     * 根据币种查询手续费和提现地址
     */
    private fun getCost() {
        addDisposable(getMainModel().getCost(showSymbol, object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data")
                if (null == data || data.length() == 0) return
                setFeeView(data)
            }
        }))

    }

}