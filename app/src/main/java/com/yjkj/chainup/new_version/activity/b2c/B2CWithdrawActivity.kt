package com.yjkj.chainup.new_version.activity.b2c

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.CoinActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.activity_b2_cwithdraw.*
import kotlinx.android.synthetic.main.activity_b2_cwithdraw.btn_confirm
import kotlinx.android.synthetic.main.activity_b2_cwithdraw.collapsing_toolbar
import kotlinx.android.synthetic.main.activity_b2_cwithdraw.et_amount
import kotlinx.android.synthetic.main.activity_b2_cwithdraw.toolbar
import kotlinx.android.synthetic.main.activity_b2_cwithdraw.tv_available_balance
import kotlinx.android.synthetic.main.activity_b2_cwithdraw.tv_choose_symbol
import kotlinx.android.synthetic.main.activity_b2_cwithdraw.tv_note
import kotlinx.android.synthetic.main.activity_b2_cwithdraw.tv_recharge_record
import org.json.JSONObject

/**
 * @description:提现(B2C)
 * @author Bertking
 * @date 2019-10-23 AM
 */
@Route(path = RoutePath.B2CWithdrawActivity)
class B2CWithdrawActivity : NBaseActivity() {

    /* 提现金额 */
    var withdrawAmount = ""
    /*提现账户id*/
    var withdrawId = ""

    var dialog: TDialog? = null


    var symbol: String = PublicInfoDataService.getInstance().coinInfo4B2c

    var precision: Int = 2

    var availableBalance: String = "0"

    var fee = ""
    var feeType = "0"
    // 单笔最小提现金额
    var withdrawMin = "0"
    // 单笔最大提现金额
    var withdrawMax = "0"
    // 今日可提现
    var canWithdrawBalance = "0"


    override fun setContentView() = R.layout.activity_b2_cwithdraw

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()

        if (!TextUtils.isEmpty(withdrawAmount) && (!TextUtils.isEmpty(withdrawId))) {
            btn_confirm?.isEnable(true)
        } else {
            btn_confirm?.isEnable(false)
        }

        Log.d(TAG, "账户信息ID::::$withdrawId")
        if (StringUtil.checkStr(withdrawId)) {
            fiatGetBank()
        } else {
            initWithdrawAccount()
        }

        getB2CAccount(symbol)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                /** 币种*/
                321 -> {
                    symbol = data?.getStringExtra(CoinActivity.SELECTED_COIN) ?: ""
                    PublicInfoDataService.getInstance().saveCoinInfo4B2C(symbol)
                    getB2CAccount(symbol)
                }

                123 -> {
                    withdrawId = data?.getStringExtra(ParamConstant.WITHDRAW_ID) ?: ""
                    Log.d(TAG, "账户信息ID::::$withdrawId")
                    if (StringUtil.checkStr(withdrawId)) {
                        fiatGetBank()
                    } else {
                        initWithdrawAccount()
                    }
                }

            }
        }
    }


    override fun initView() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }

        collapsing_toolbar?.run {
            setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            expandedTitleGravity = Gravity.BOTTOM
        }


        /* 提现记录 */
        tv_recharge_record?.setOnClickListener {
            ArouterUtil.navigation(RoutePath.B2CRecordsActivity, Bundle().apply { putInt(ParamConstant.OPTION_TYPE, ParamConstant.WITHDRAW) })
        }

        /*选择币种*/
        tv_choose_symbol?.setOnClickListener {
            ArouterUtil.navigation4Result(RoutePath.SelectCoinActivity, Bundle().apply {
                putInt(ParamConstant.OPTION_TYPE, ParamConstant.WITHDRAW)
                putBoolean(ParamConstant.COIN_FROM, false)
            }, mActivity, 321)
        }

        // 选择提现账户
        rl_into_withdraw_layout?.setOnClickListener {
            ArouterUtil.navigation4Result(RoutePath.B2CWithdrawAccountListActivity, Bundle().apply { putString(ParamConstant.symbol, symbol) }, mActivity, 123)
        }

        // 全部金额
        btn_all?.setOnClickListener {
            et_amount?.setText(availableBalance)
            et_amount?.setSelection(et_amount?.text?.length ?: 0)
        }


        et_amount?.isFocusable = true
        et_amount?.isFocusableInTouchMode = true

        et_amount?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                withdrawAmount = s.toString()
                // 0:固定值  1：百分比

                if (StringUtil.checkStr(fee)) {
                    val realAmount = if (feeType == "1") {
                        val precent = (100f - fee.toFloat()).div(100f).toString()
                        BigDecimalUtils.mul(withdrawAmount, precent).toPlainString()
                    } else {
                        BigDecimalUtils.sub(withdrawAmount, fee).toPlainString()
                    }
                    tv_real_amount?.text = DecimalUtil.cutValueByPrecision(realAmount, precision) + symbol

                    if (feeType == "1") {
                        val feePrecent = fee.toFloat().div(100f).toString()
                        Log.d(TAG, "=======Precent:$feePrecent=====")
                        et_fee?.setText(BigDecimalUtils.mul(withdrawAmount, feePrecent).toPlainString())
                    }

                }

                if (!TextUtils.isEmpty(withdrawAmount) && (!TextUtils.isEmpty(withdrawId))) {
                    btn_confirm?.isEnable(true)
                } else {
                    btn_confirm?.isEnable(false)
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        /**
         * 确认
         */
        btn_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {

                if (BigDecimalUtils.compareTo(withdrawAmount, availableBalance) > 0) {
                    NToastUtil.showTopToastNet(mActivity,false, LanguageUtil.getString(this@B2CWithdrawActivity,"common_tip_balanceNotEnough"))
                    return
                }

                if (BigDecimalUtils.compareTo(withdrawAmount, withdrawMin) < 0) {
                    NToastUtil.showTopToastNet(mActivity,false, LanguageUtil.getString(this@B2CWithdrawActivity,"b2c_text_singleWithdrawMin").format(DecimalUtil.cutValueByPrecision(withdrawMin, precision)))
                    return
                }

                if (BigDecimalUtils.compareTo(withdrawAmount, canWithdrawBalance) > 0) {
                    NToastUtil.showTopToastNet(mActivity,false, LanguageUtil.getString(this@B2CWithdrawActivity,"b2c_text_singleWithdrawMin").format(DecimalUtil.cutValueByPrecision(canWithdrawBalance, precision)))
                    return
                }

                if (BigDecimalUtils.compareTo(withdrawAmount, withdrawMax) > 0) {
                    NToastUtil.showTopToastNet(mActivity,false, LanguageUtil.getString(this@B2CWithdrawActivity,"b2c_text_singleWithdrawMax").format(DecimalUtil.cutValueByPrecision(withdrawMax, precision)))
                    return
                }

                dialog = NewDialogUtils.showAccountBindDialog(mActivity, UserDataService.getInstance().mobileNumber, 1, 32, object : NewDialogUtils.DialogVerifiactionListener {
                    override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                        dialog?.dismiss()
                        Log.d(TAG, "=======phone验证码:$phone==")
                        withdraw(phone ?: return, googleCode ?: return)
                    }
                })
            }
        }
    }

    /**
     * 提现
     */
    fun withdraw(smsAuthCode: String = "", googleCode: String = "") {
        addDisposable(getMainModel().fiatWithdraw(symbol = symbol,
                userWithdrawBankId = withdrawId,
                amount = withdrawAmount,
                smsAuthCode = smsAuthCode,
                googleCode = googleCode,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        NToastUtil.showTopToastNet(mActivity,true, LanguageUtil.getString(this@B2CWithdrawActivity,"b2c_text_withdrawSuccess"))
                        et_amount?.setText("")
                        finish()
                    }
                }))
    }

    private fun getB2CAccount(symbol: String) {
        addDisposable(getMainModel().fiatBalance(symbol = symbol,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {

                        val data = jsonObject.optJSONObject("data")
                        val withdrawTip = data?.optString("withdrawTip", "")
                        tv_note?.text = withdrawTip
                        tv_notes_title?.visibility = if (TextUtils.isEmpty(withdrawTip)) View.INVISIBLE else View.VISIBLE

                        val jsonArray = data?.optJSONArray("allCoinMap")
                        if (jsonArray?.length() != 0) {
                            jsonArray?.optJSONObject(0)?.run {
                                tv_symbol?.text = optString("symbol", "")
                                /* 充值金额 */
                                precision = optInt("showPrecision", 2)
                                et_amount?.filters = arrayOf(DecimalDigitsInputFilter(precision))

                                availableBalance = optString("normalBalance", "0")
                                tv_available_balance?.text = LanguageUtil.getString(this@B2CWithdrawActivity,"withdraw_text_available") + " $availableBalance"

                                tv_fee_symbol?.text = symbol
                                // 单笔最小
                                withdrawMin = optString("withdrawMin", "")
                                tv_min_amount?.text = LanguageUtil.getString(this@B2CWithdrawActivity,"b2c_text_singleWithdrawMin").format(DecimalUtil.cutValueByPrecision(withdrawMin, precision)) + " $symbol"
                                // 单笔最大
                                withdrawMax = optString("withdrawMax", "")
                                tv_max_amount?.text = LanguageUtil.getString(this@B2CWithdrawActivity,"b2c_text_singleWithdrawMax").format(DecimalUtil.cutValueByPrecision(withdrawMax, precision)) + " $symbol"
                                // 今日
                                canWithdrawBalance = optString("canWithdrawBalance", "")
                                tv_amount_day?.text = LanguageUtil.getString(this@B2CWithdrawActivity,"b2c_text_todaywithdraw").format(DecimalUtil.cutValueByPrecision(canWithdrawBalance, precision)) + " $symbol"

                            }
                        }


                    }
                }))
    }


    /**
     * 查询提现银行
     */
    private fun fiatGetBank() {
        addDisposable(getMainModel().fiatGetBank(id = withdrawId,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val data = jsonObject.optJSONObject("data")
                        if (data == null) {
                            initWithdrawAccount()
                        } else {
                            data.run {
                                val cardNo = optString("cardNo", "")
                                val bankName = optString("bankName", "")
                                withdrawId = optString("id")
                                // 账号展示
                                cet_withdraw_adr?.text = bankName + "_**" + cardNo.takeLast(3)
                                fee = optString("fee")

                                // 0:固定值  1：百分比
                                /**
                                 * 手续费
                                 */
                                feeType = optString("feeType")
                                if (feeType != "1") {
                                    et_fee?.setText(DecimalUtil.cutValueByPrecision(fee, precision))
                                }

                            }
                        }


                    }
                }))

    }

    private fun initWithdrawAccount() {
        cet_withdraw_adr?.text = ""
        et_fee?.setText("--")
    }
}
