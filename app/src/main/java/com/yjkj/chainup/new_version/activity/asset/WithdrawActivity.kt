package com.yjkj.chainup.new_version.activity.asset

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.zxing.integration.android.IntentIntegrator
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.AuthBean
import com.yjkj.chainup.bean.address.AddressBean
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.SymbolManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.activity.CoinActivity
import com.yjkj.chainup.new_version.activity.ScanningActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.*
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_withdraw.*
import kotlinx.android.synthetic.main.activity_withdraw.tv_fee_title
import kotlinx.android.synthetic.main.activity_withdraw.tv_number_title
import org.json.JSONArray
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/5/16-10:11 AM
 * @Email buptjinlong@163.com
 * @description 提币
 */
@Route(path = RoutePath.WithdrawActivity)
class WithdrawActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_withdraw


    lateinit var bean: JSONObject

    /**
     * 币对的精度
     */
    var coinPrecision = 0

    var addressId: Int = 0
    var fee: String = ""
    var amount: String = ""
    var actualaMount: String = ""
    var showSymbol: String = ""
    var symbol: String = ""


    var feeMax = ""
    var feeMin = ""
    var withdrawMin = ""
    var withdrawMan = ""

    var tokenBase = ""
    var address: String? = ""
    var addressTag = ""
    var addressStatus = false

    /**
     * 是否有地址
     */
    var newAddress = true

    var isShowTag = true
    var eFeeStatus = true


    var tagBean = 0

    /**
     * 最小提币数
     */
    var amountvalue = ""

    /**
     * 手续费
     */
    var feevalue = ""

    companion object {
        const val WITHDRAW = "WITHDRAW"
        fun enter2(context: Context, bean: String?) {
            var intent = Intent()
            intent.setClass(context, WithdrawActivity::class.java)
            intent.putExtra(WITHDRAW, bean)
            context.startActivity(intent)

        }
    }

    fun getData() {
        intent ?: return
        var jsonString = intent.getStringExtra(WITHDRAW) ?: return
        bean = JSONObject(jsonString)
    }

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        getData()
        initView()
        initClickListener()
    }

    override fun initView() {

        title_layout?.setContentTitle(LanguageUtil.getString(this, "assets_action_withdraw"))
        title_layout?.setRightTitle(LanguageUtil.getString(this, "withdraw_action_withdrawHistory"))

        tv_choose_symbol?.text = LanguageUtil.getString(this, "b2c_text_changecoin")
        tv_withdraw_adr_title?.text = LanguageUtil.getString(this, "withdraw_text_address")
        cet_withdraw_adr?.hint = LanguageUtil.getString(this, "withdraw_tip_addressEmpty")
        tv_adr_note_title?.text = LanguageUtil.getString(this, "charge_text_tagAddress")
        cet_withdraw_adr_note?.hint = LanguageUtil.getString(this, "withdraw_tip_tagEmpty")
        tv_number_title?.text = LanguageUtil.getString(this, "charge_text_volume")
        btn_all_amount?.text = LanguageUtil.getString(this, "common_action_sendall")
        et_amount?.hint = LanguageUtil.getString(this, "withdraw_tip_withdrawMinValueError")
        tv_fee_title?.text = LanguageUtil.getString(this, "withdraw_text_fee")
        tv_instructions_title?.text = LanguageUtil.getString(this, "withdraw_tip_notice")
        tv_withdraw_text_moneyWithoutFee?.text = LanguageUtil.getString(this, "withdraw_text_moneyWithoutFee")
        cubtn_confirm?.setBottomTextContent(LanguageUtil.getString(this, "common_text_btnConfirm"))

        if (null != bean?.has("coinName")) {
            showSymbol = bean?.optString("coinName").toString()
            symbol = showSymbol
        }
        getCost(symbol)
        initManyChain()
        setView()

        title_layout?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                WithDrawRecordActivity.enter2(this@WithdrawActivity, showSymbol, ParamConstant.TRANSFER_WITHDRAW_RECORD, WITHDRAWTYPE)
            }

            override fun onclickName() {

            }

        }
        /**
         * 最小提币数
         */
        et_amount?.isFocusable = true
        et_amount?.isFocusableInTouchMode = true
        et_amount?.setOnFocusChangeListener { v, hasFocus ->
            view_amount_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_line_color)
        }
        et_amount?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                amountvalue = s.toString()

                realAmount(et_fee.text.toString(), s.toString())


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })


        /**
         * 新地址
         */
        cet_withdraw_adr?.isFocusable = true
        cet_withdraw_adr?.isFocusableInTouchMode = true
        cet_withdraw_adr?.setOnFocusChangeListener { v, hasFocus ->
            view_withdraw_adr_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_line_color)
        }
        cet_withdraw_adr?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var add = s.toString()
                if (add != address) {
                    addressId = 0
                    addressStatus = false
                }
                address = s.toString()

            }
        })
        /**
         * 新地址标签
         */
        cet_withdraw_adr_note?.isFocusable = true
        cet_withdraw_adr_note?.isFocusableInTouchMode = true
        cet_withdraw_adr_note?.setOnFocusChangeListener { v, hasFocus ->
            view_withdraw_adr_note_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_line_color)
        }
        cet_withdraw_adr_note?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addressTag = s.toString()
            }

        })
    }

    private fun realAmount(fee: String, amount: String) {
        var value = "0"
        if (TextUtils.isEmpty(amount)) {

        } else {
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

    /**
     * 设置地址相关
     */
    private fun setAdressView(jsonObject: JSONObject, status: Boolean) {
        /**
         * 设置地址
         */

        var withdrawAddressMaps: JSONArray
        if (status) {
            withdrawAddressMaps = JSONArray(jsonObject?.optString("withdrawAddressMap"))
        } else {
            withdrawAddressMaps = JSONArray(jsonObject?.optString("userWithdrawAddrList"))
        }

        if (null != withdrawAddressMaps && withdrawAddressMaps.length() > 0) {
            var bean = withdrawAddressMaps.optJSONObject(0)

            addressId = bean?.optInt("id") ?: 0


            addressStatus = bean?.optInt("trustType") == 1
            val split = bean?.optString("address")?.split("_")

            if (null != split) {
                if (split.size > 1) {
                    addressTag = split[1]
                    cet_withdraw_adr?.setText(split[0])
                    ll_tag_layout?.visibility = View.VISIBLE
                    cet_withdraw_adr_note?.setText(addressTag)
                    address = split[0]
                } else {
                    address = bean?.optString("address")
                    ll_tag_layout?.visibility = View.GONE
                    cet_withdraw_adr?.setText(bean.optString("address"))
                }
            }

            newAddress = false
        } else {
            address = ""
            addressTag = ""
            addressStatus = false
            newAddress = true

            val coinBean = DataManager.getCoinByName(symbol)
            tokenBase = coinBean?.tokenBase ?: ""
            tagBean = coinBean?.tagType ?: 0
            if (tagBean == 0) {
                ll_tag_layout?.visibility = View.GONE
            } else {
                ll_tag_layout?.visibility = View.VISIBLE
            }
        }
    }


    var chainJson: JSONObject? = null


    /**
     * 设置多链
     */
    private fun initManyChain() {
        mcv_layout?.listener = object : ManyChainSelectListener {
            override fun selectCoin(coinName: JSONObject) {
                chainJson = coinName
                symbol = coinName.optString("name", "")
                coinPrecision = chainJson?.optInt("showPrecision", 8) ?: 8
                et_amount?.setText("")
                addressTag = ""
                addressId = 0
                getCost(symbol)
            }
        }
        mcv_layout?.setManyChainView(showSymbol)
    }


    /**
     * 选择多链后
     * 设置对应的最大手续费最小手续费等
     */
    private fun setFeeView(jsonObject: JSONObject) {
        /**
         * 手续费
         * 为避免其出现科学计数法
         */
        cubtn_confirm.isEnable(true)
        feeMin = jsonObject.optString("feeMin")
        feeMax = jsonObject.optString("feeMax")
        withdrawMin = jsonObject.optString("withdraw_min")
        withdrawMan = jsonObject.optString("withdraw_max")
        /**
         * 可用余额
         */
        var normalBalance = bean?.optString("normal_balance") ?: ""
        var normalBalanceN = BigDecimalUtils.divForDown(normalBalance, coinPrecision).toPlainString()
        var marketName = NCoinManager.getShowMarket(showSymbol)
        tv_available_balance?.text = LanguageUtil.getString(mActivity, "available_balance") + "$normalBalanceN $marketName"
        et_fee?.filters = arrayOf(DecimalDigitsInputFilter(coinPrecision))

        feevalue = jsonObject.optString("defaultFee")
        realAmount(feevalue, et_amount.text.toString())
        et_fee?.setText(BigDecimalUtils.divForDown(jsonObject.optString("defaultFee"), coinPrecision).toPlainString())

        /**
         * 单次提币最小值
         */
        tv_amount?.text = LanguageUtil.getString(mActivity, "charge_chargeAlert_contentA") + " " + BigDecimalUtils.divForDown(withdrawMin, coinPrecision).toPlainString()


        /**
         * 单次提币限额为
         */
        tv_amount_day?.text = LanguageUtil.getString(mActivity, "charge_chargeAlert_contentB") + " " + BigDecimalUtils.divForDown(withdrawMan, coinPrecision).toPlainString()


        /**
         * 手续费范围
         */
        tv_amount_range?.text = LanguageUtil.getString(mActivity, "charge_chargeAlert_contentC") + " " + BigDecimalUtils.divForDown(feeMin, coinPrecision).toPlainString()+"-"+BigDecimalUtils.divForDown(feeMax, coinPrecision).toPlainString()

        /**
         * 设置最小提币数
         */
        et_amount?.hint = String.format(LanguageUtil.getString(mActivity, "withdraw_tip_withdrawMinValueError"), BigDecimalUtils.divForDown(withdrawMin, coinPrecision).toPlainString())
        et_amount?.filters = arrayOf(DecimalDigitsInputFilter(coinPrecision))
        setAdressView(jsonObject, false)
    }


    /**
     * 设置初始view
     */
    private fun setView() {

        cubtn_confirm?.isEnable(true)
        val coinBean = DataManager.getCoinByName(showSymbol)
        tokenBase = coinBean?.tokenBase ?: ""
        tagBean = coinBean?.tagType ?: 0
        if (tagBean == 0) {
            ll_tag_layout?.visibility = View.GONE
        } else {
            ll_tag_layout?.visibility = View.VISIBLE
        }
        tv_coin_name?.text = NCoinManager.getShowMarket(showSymbol)
        isShowTag = tagBean == 2

        if (null != chainJson && chainJson?.length() ?: 0 > 0) {
            coinPrecision = chainJson?.optInt("showPrecision", 8) ?: 8
        } else {
            coinPrecision = NCoinManager.getCoinShowPrecision(symbol)
        }

        tv_symbol_name?.text = NCoinManager.getShowMarket(showSymbol)
        /**
         * 可用余额
         */
        var normalBalance = bean?.optString("normal_balance") ?: ""
        var normalBalanceN = BigDecimalUtils.divForDown(normalBalance, coinPrecision).toPlainString()
        var marketName = NCoinManager.getShowMarket(showSymbol)
        tv_available_balance?.text = LanguageUtil.getString(mActivity, "withdraw_text_available") + "$normalBalanceN $marketName"


        /**
         * 手续费 币种
         */
        tv_fee_symbol?.text = NCoinManager.getShowMarket(showSymbol)


        /**
         * 实际到账数量
         */

        tv_real_amount?.text = "0" + NCoinManager.getShowMarket(showSymbol)
    }


    private fun setNewAdr() {
        cet_withdraw_adr?.setText("")
        cet_withdraw_adr_note?.setText("")
        et_amount?.setText("")
        addressTag = ""
        addressId = 0
        mcv_layout?.clearLables()
        et_fee?.setText(BigDecimalUtils.divForDown(feeMin, coinPrecision).toPlainString())
        newAddress = true
        addressStatus = false
    }


    private fun initClickListener() {

        /**
         * 全部可用余额
         */
        btn_all_amount?.setOnClickListener {
            et_amount?.setText(BigDecimalUtils.divForDown(bean.optString("normal_balance"), coinPrecision).toPlainString())

            /**
             * 实际到账数量
             */
            var result = BigDecimalUtils.sub(bean.optString("normal_balance"), et_fee.text.toString()).toString()
            if (result.toDouble() < 0) {
                tv_real_amount?.setTextColor(ColorUtil.getColor(R.color.red))
                tv_real_amount?.text = LanguageUtil.getString(mActivity, "common_tip_balanceNotEnough")

            } else {
                tv_real_amount?.setTextColor(ColorUtil.getColor(R.color.main_font_color))
                tv_real_amount?.text = BigDecimalUtils.divForDown(result, coinPrecision).toPlainString() + NCoinManager.getShowMarket(showSymbol)
            }
        }

        /**
         * 跳转至扫描界面
         */
        iv_sweep_the_yard?.setOnClickListener {
//            val intentIntegrator = IntentIntegrator(this)
//            intentIntegrator.captureActivity = ScanningActivity::class.java
//            intentIntegrator.setPrompt(LanguageUtil.getString(mActivity, "scan_tip_aimToScan"))
//            intentIntegrator.setBeepEnabled(true)
//            intentIntegrator.initiateScan()
            val intent = Intent(this, CaptureActivity::class.java)
            startActivityForResult(intent, 0x1111)

        }
        /**
         * 提币地址 地址列表
         */
        iv_into_withdraw_list?.setOnClickListener {
            var note = cet_withdraw_adr_note?.text.toString()
            if (note.isNotEmpty()) {
                WithdrawAddressActivity.enter4Result(this, symbol, showSymbol, cet_withdraw_adr?.text.toString() + "_$note")
            } else {
                WithdrawAddressActivity.enter4Result(this, symbol, showSymbol, cet_withdraw_adr?.text.toString())
            }
        }

        /**
         * 选择币对
         */
        rl_symbol_name?.setOnClickListener {
//            ArouterUtil.navigation4Result(RoutePath.SelectCoinActivity, Bundle().apply {
//                putInt(ParamConstant.OPTION_TYPE, ParamConstant.WITHDRAW)
//                putBoolean(ParamConstant.COIN_FROM, false)
//            }, this, 321)

            ArouterUtil.navigation4Result(RoutePath.WithdrawSelectCoinActivity, Bundle().apply {
                putInt(ParamConstant.OPTION_TYPE, ParamConstant.WITHDRAW)
                putBoolean(ParamConstant.COIN_FROM, false)
            }, this, 321)
        }

        /**
         * 确认
         */
        cubtn_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (UserDataService.getInstance().googleStatus != 1 && UserDataService.getInstance().isOpenMobileCheck != 1) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "unbind_verify_warn"), isSuc = false)
                    return
                }
                fee = et_fee?.text.toString()
                var minAmount = et_amount?.text.toString().trim()
                amount = BigDecimalUtils.divForDown(minAmount, coinPrecision).toPlainString()
                actualaMount = BigDecimalUtils.divForDown(BigDecimalUtils.sub(minAmount, et_fee.text.toString()).toString(), coinPrecision).toPlainString()

                if (BigDecimalUtils.compareToDraw(minAmount, withdrawMin) == -1) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "subtitle_withdraw_min") + "$withdrawMin", isSuc = false)
                    return
                }

                if (!TextUtils.isEmpty(bean.optString("normal_balance")) && StringUtil.isNumeric(bean.optString("normal_balance"))) {
                    if (minAmount.toDouble() > bean.optDouble("normal_balance")) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "toast_withdraw_too_max"), isSuc = false)
                        return
                    }
                }
                if (newAddress) {
                    addressTag = cet_withdraw_adr_note?.text.toString()
                }
                if (tagBean == 2 && addressTag.isEmpty()) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "toast_no_tag"), isSuc = false)
                    return
                }

                /**
                 * 手续费
                 */
                if (TextUtils.isEmpty(fee)) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "toast_no_fee"), isSuc = false)
                    return
                }

                if (newAddress) {
                    address = cet_withdraw_adr?.text.toString()
                }
                if (TextUtils.isEmpty(address)) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "toast_no_withdraw_address"), isSuc = false)
                    return
                }

                /**
                 * 可用资产不足
                 */
                val normalBal = bean.optString("normal_balance")

                if (BigDecimalUtils.sub(normalBal, amount).toDouble() < 0) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "common_tip_balanceNotEnough"), isSuc = false)
                    return
                }
                if (addressTag.isNotEmpty()) {
                    address += "_$addressTag"
                }

                addWithdrawAddrValidate(symbol, address ?: "")

            }
        }


    }

    var trustDialog: TDialog? = null
    var untrustDialog: TDialog? = null

    /**
     * 需求
     */
    fun addWithdrawAddrValidate(symbol: String, address: String) {
        addDisposable(getMainModel().addWithdrawAddrValidate(symbol, address, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                /**
                 * 是否信任地址
                 * true 是
                 */
                if (addressStatus) {
                    NewDialogUtils.showNewDoubleDialog(this@WithdrawActivity, LanguageUtil.getString(this@WithdrawActivity, "withdraw_confirm_tips1"), object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            submitWithDraw()
                        }

                    }, LanguageUtil.getString(this@WithdrawActivity, "common_text_tip"), LanguageUtil.getString(this@WithdrawActivity, "common_text_btnCancel"), LanguageUtil.getString(this@WithdrawActivity, "common_text_btnConfirm"))
                } else {
                    NewDialogUtils.showNewDoubleDialog(this@WithdrawActivity, LanguageUtil.getString(this@WithdrawActivity, "withdraw_confirm_tips2"), object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            /**
                             * 非信任地址 二次验证
                             */
                            untrustDialog = NewDialogUtils.showSecondDialog(this@WithdrawActivity, AppConstant.CRYPTO_WITHDRAW, object : NewDialogUtils.DialogSecondListener {
                                override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {
                                    submitWithDraw(phone ?: "", googleCode
                                            ?: "", mail ?: "")
                                    untrustDialog?.dismiss()
                                }
                            }, loginPwdShow = false, confirmTitle = LanguageUtil.getString(this@WithdrawActivity, "common_text_btnConfirm"))

                        }
                    }, LanguageUtil.getString(this@WithdrawActivity, "login_success_action_alert_title"), LanguageUtil.getString(this@WithdrawActivity, "common_text_btnCancel"), LanguageUtil.getString(this@WithdrawActivity, "alert_common_i_understand"))
                }
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showToast(msg, false)
            }
        }))
    }

    /**
     * 扫码结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            cet_withdraw_adr?.setText("")
            cet_withdraw_adr_note?.setText("")
            et_amount?.setText("")
            addressStatus = false
            when (requestCode) {
                /**
                 * 币种
                 */
                321 -> {
                    showSymbol = data?.getStringExtra(CoinActivity.SELECTED_COIN) ?: ""
                    bean = SymbolManager.instance.getFundCoinByName(showSymbol)
                    setNewAdr()
                    initView()
                }

                /**
                 * 充值地址
                 */
                WithdrawAddressActivity.REQUEST_CODE_ADDRESS -> {
                    ll_tag_layout?.visibility = View.VISIBLE
                    val addressbean = data?.getParcelableExtra<AddressBean.Address>(WithdrawAddressActivity.OBJECT_ADDRESS)
                    if (addressbean == null) {
                        setNewAdr()
                    } else {
                        val addr = addressbean?.address
                        if (!TextUtils.isEmpty(addr)) {
                            val split = addr?.split("_")
                            if (split!!.size > 1) {
                                ll_tag_layout?.visibility = View.VISIBLE
                                cet_withdraw_adr?.setText(split[0])
                                cet_withdraw_adr_note?.setText(split[1])
                                address = split[0]
                                addressTag = split[1]
                            } else {
                                ll_tag_layout?.visibility = View.GONE
                                cet_withdraw_adr?.setText(addressbean.address)
                                address = addressbean.address
                                addressTag = ""
                            }
                        }
                        newAddress = false
                        addressStatus = addressbean?.trustType == 1
                        if (addressbean != null) {
                            addressId = addressbean.id
                        }
                    }

                }

                0x1111 -> {
                    data?.let { intent ->
                        setNewAdr()
                        intent.getStringExtra(CaptureActivity.SCAN_RESULT)?.apply {
                            var re = this.split(":")
                            if (re.size == 2) {
                                var address = re[1]
                                cet_withdraw_adr?.setText(address)
                            } else {
                                var addressSaoma = this
                                if (addressSaoma.contains("_") && ll_tag_layout.visibility == View.VISIBLE) {
                                    val split = addressSaoma.split("_")
                                    cet_withdraw_adr?.setText(split[0])
                                    cet_withdraw_adr_note?.setText(split[1])
                                } else {
                                    cet_withdraw_adr?.setText(this)
                                }

                            }
                        }
                    }
                }
            }

        }
//        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
//        if (result != null) {
//            if (TextUtils.isEmpty(result.contents)) {
//                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "toast_scan_content_empty"), isSuc = false)
//            } else {
//
//                var addressSaoma = result.contents
//                if (addressSaoma.contains("_") && ll_tag_layout.visibility == View.VISIBLE) {
//                    val split = addressSaoma.split("_")
//                    cet_withdraw_adr?.setText(split[0])
//                    cet_withdraw_adr_note?.setText(split[1])
//                } else {
//                    cet_withdraw_adr?.setText(result.contents)
//                }
//            }
//        }
    }


    /**
     * 根据币种查询手续费和提现地址
     */
    private fun getCost(symbol: String = "") {
        addDisposable(getMainModel().getCost(symbol, object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data")
                if (null == data || data.length() == 0) return
                mcv_layout?.content = data?.optString("mainChainNameTip", "")
                setFeeView(data)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                LogUtil.e("LogUtils", "getCost error")
                tv_amount?.text = LanguageUtil.getString(mActivity, "charge_chargeAlert_contentA") + " --"
                tv_amount_day?.text = LanguageUtil.getString(mActivity, "charge_chargeAlert_contentB") + " --"

                et_fee?.setText("--")
                et_amount?.hint = LanguageUtil.getString(mActivity, "withdraw_tip_withdrawMinValueError").format(" --")
                cubtn_confirm.isEnable(false)
            }
        }))

    }

    /**
     * 确认提现
     */
    fun submitWithDraw(first: String = "", second: String = "", emailValidCode: String = "") {
        cubtn_confirm?.isEnable(false)
        HttpClient.instance.doWithdraw(addressId = if (addressId == 0) "" else addressId.toString(),
                fee = fee,
                smsCode = first,
                googleCode = second,
                amount = actualaMount,
                symbol = symbol,
                address = address ?: "",
                trustType = if (addressStatus) "1" else "0",
                emailValidCode = emailValidCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<AuthBean>() {
                    override fun onHandleSuccess(t: AuthBean?) {

                        cubtn_confirm?.isEnable(true)
                        if (t == null) {
                            NToastUtil.showTopToastNet(this@WithdrawActivity, true, getString(R.string.toast_withdraw_suc))
                            NewDialogUtils.showNewsingleDialog2(this@WithdrawActivity!!, getString(R.string.toast_withdraw_suc), object : NewDialogUtils.DialogBottomListener {
                                override fun sendConfirm() {
                                    finish()
                                }
                            }, cancelTitle = LanguageUtil.getString(this@WithdrawActivity, "alert_common_i_understand"))
                            return
                        }
                        if (t.isOpenUserCheck == true) {
                            //是否需要实名认证
                            if (t.isUserCheckFace()) {
                                //face++
                                ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, Bundle().apply {
                                    putString(ParamConstant.head_title, "")
                                    putString(ParamConstant.web_url, t.faceUrl())
                                })
                            } else {
                                //用户手动提交实名
                                ArouterUtil.greenChannel(RoutePath.IdentityAuthenticationActivity, Bundle().apply {
                                    putString(ParamConstant.WITHDRAW_ID, t.withdrawId ?: "")
                                })
                            }
                            finish()
                        } else {
                            NToastUtil.showTopToastNet(this@WithdrawActivity, true, getString(R.string.toast_withdraw_suc))
                            NewDialogUtils.showNewsingleDialog2(this@WithdrawActivity!!, getString(R.string.toast_withdraw_suc), object : NewDialogUtils.DialogBottomListener {
                                override fun sendConfirm() {
                                    finish()
                                }
                            }, cancelTitle = LanguageUtil.getString(this@WithdrawActivity, "alert_common_i_understand"))
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cubtn_confirm?.isEnable(true)
                        NToastUtil.showTopToastNet(this@WithdrawActivity, false, msg)
                    }
                })
    }
}