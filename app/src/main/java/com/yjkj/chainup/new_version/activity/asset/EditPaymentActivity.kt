package com.yjkj.chainup.new_version.activity.asset

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.yjkj.chainup.util.JsonUtils
import com.google.gson.JsonObject
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.ShowImageActivity
import com.yjkj.chainup.new_version.activity.personalCenter.PersonalInfoActivity
import com.yjkj.chainup.new_version.activity.personalCenter.RealNameCertificaionSuccessActivity
import com.yjkj.chainup.new_version.activity.personalCenter.RealNameCertificationActivity
import com.yjkj.chainup.new_version.activity.personalCenter.SafetySettingActivity
import com.yjkj.chainup.new_version.bean.ImageTokenBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.OnSaveSuccessListener
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.new_version.view.UploadHelper
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_edit_payment.*
import org.json.JSONObject

const val IDCARD = 1 // 身份证
const val PASSPORT = 2 // 护照

/**
 * 作为标记照片位置
 */
const val FIRST_INDEX = 0 // 第一张照片
const val SECOND_INDEX = 1 // 第二张照片
const val THIRD_INDEX = 2// 第三张照片

const val CREDENTIALS_TYPE = 1//证件
const val PHOTO_TYPE = 2//图片选择

/**
 * @date 2018-10-13
 * @author Bertking
 * @description  编辑支付页面
 */
class EditPaymentActivity : NewBaseActivity() {


    var payment = ALIPAY
    var payTitle = ""

    var operate = ADD

    var paymentBean: JSONObject? = null


    /**
     * 拍照的工具类
     */
    var imageTool: ImageTools? = null
    /**
     * 上传图片 oss  获取token
     */
    var imageTokenBean: ImageTokenBean = ImageTokenBean()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_payment)

        imageTool = ImageTools(this)
        context = this
        payment = intent?.getStringExtra(PAYMENT) ?: ""
        payTitle = intent?.getStringExtra(PAYTITLE) ?: ""
        operate = intent.getIntExtra(OPERATION, ADD)
        if (intent?.getStringExtra(BEAN) != null) {
            paymentBean = JSONObject(intent?.getStringExtra(BEAN) ?: "")
        }
        if (paymentBean != null) {
            payTitle = paymentBean?.optString("title") ?: payTitle
            LocalAddress = paymentBean?.optString("qrcodeImg") ?: ""
            payment = paymentBean?.optString("payment") ?: ""
            firstImgPath = paymentBean?.optString("qrcodeImg") ?: ""

        }
        v_title.setContentTitle(payTitle)
        tv_account_title.text = payTitle + LanguageUtil.getString(this, "noun_account_accountName")
        rl_pay_qrcode.visibility = View.GONE
        initViews()
        setClickListener()
        btn_finished?.setBottomTextContent(LanguageUtil.getString(this, "kyc_action_submit"))
    }

    fun setClickListener() {
        iv_cancel.setOnClickListener {
            firstImgPath = ""
            LocalAddress = ""
            iv_upload_pay_qrcode.setImageResource(R.drawable.assets_addingpaymentmethod)
            iv_cancel.visibility = View.GONE
        }
    }


    companion object {
        val PAYMENT = "payment"
        val PAYTITLE = "payTitle"

        const val OPERATION = "operation"

        /**
         * 支付宝
         */
        val ALIPAY = "otc.payment.alipay"
        /**
         * 微信支付
         */
        val WECHATPAY = "otc.payment.wxpay"

        /**
         * 银行卡支付
         */
        val BANKPAY = "otc.payment.domestic.bank.transfer"

        val PAYPAL = "otc.payment.paypal"// 贝宝
        val WESTUNIO = "otc.payment.western.union"//西联汇款
        val SWIFT = "otc.payment.swift" //swift
        val PAYNOW = "otc.payment.paynow"//paynow
        val PAYTM = "otc.payment.paytm" //paytm
        val QIWI = "otc.payment.qiwi"//qiwi
        val INTERACT = "otc.payment.interac"//interac
        val IMPS = "otc.payment.imps"//imps
        val UPI = "otc.payment.upi"//upi
        /**
         * 编辑
         */
        const val EDIT = 1

        /**
         * 添加
         */
        const val ADD = 2

        /**
         * 查看
         */
        const val PREVIEW = 3

        const val BEAN = "BEAN"


        /**
         * 用于添加
         */
        fun enter2(context: Context, payment: String, payTitle: String) {
            var intent = Intent(context, EditPaymentActivity::class.java)
            intent.putExtra(PAYMENT, payment)
            intent.putExtra(PAYTITLE, payTitle)
            context.startActivity(intent)
        }

        /**
         * 用于编辑
         */
        fun enter2(context: Context, operate: Int, paymentBean: String) {
            var intent = Intent(context, EditPaymentActivity::class.java)
            intent.putExtra(OPERATION, operate)
            intent.putExtra(BEAN, paymentBean)
            context.startActivity(intent)
        }
    }


//    getString(R.string.name_dif_realname)

    fun initViews() {


        when (operate) {
            EDIT -> {
                firstImgPath = paymentBean?.optString("qrcodeImg") ?: ""
                btn_finished.isEnable(true)
                editPayment()
            }
            ADD -> {
                btn_finished.isEnable(false)
                addPayment()
            }
            PREVIEW -> {
                firstImgPath = paymentBean?.optString("qrcodeImg") ?: ""
                previewPayment()
            }

        }

        iv_upload_pay_qrcode.setOnClickListener {
            if (firstImgPath.isNotEmpty()) {

                ShowImageActivity.enter2(this, LocalAddress ?: "", false)
            } else {
                showBottomDialog()
            }

        }

        btn_finished.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (submitShow()) {
                    showVerifyDialog()
                }
            }

        }
    }


    fun submitShow(): Boolean {
        var isSuccess = true
        when (payment) {
            WECHATPAY -> {
                if (TextUtils.isEmpty(et_username.text.toString())) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "personal_tip_inputNickname"))
                    isSuccess = false
                    return isSuccess
                }
                if (TextUtils.isEmpty(et_pay_account.text.toString())) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "otc_wx_a_input"))
                    isSuccess = false
                    return isSuccess
                }
                if (TextUtils.isEmpty(firstImgPath)) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "otc_upload_wx_qr"))
                    isSuccess = false
                    return isSuccess
                }
            }
            ALIPAY -> {
                if (TextUtils.isEmpty(et_username.text.toString())) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "personal_tip_inputNickname"))
                    isSuccess = false
                    return isSuccess
                }
                if (TextUtils.isEmpty(et_pay_account.text.toString())) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "otc_alipay_a"))
                    isSuccess = false
                    return isSuccess
                }
                if (TextUtils.isEmpty(firstImgPath)) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "otc_alipay_qr"))
                    isSuccess = false
                    return isSuccess
                }
            }

            BANKPAY -> {
                if (TextUtils.isEmpty(et_username.text.toString())) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "personal_tip_inputNickname"))
                    isSuccess = false
                    return isSuccess
                }
                if (TextUtils.isEmpty(et_pay_account.text.toString())) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "otc_bank_name_input"))
                    isSuccess = false
                    return isSuccess
                }
                if (TextUtils.isEmpty(et_bank4deposit.text.toString())) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "otc_bank_name_lit_input"))
                    isSuccess = false
                    return isSuccess
                }
                if (TextUtils.isEmpty(et_bank4number.text.toString())) {
                    ToastUtils.showToast(LanguageUtil.getString(this, "otc_bank_num"))
                    isSuccess = false
                    return isSuccess
                }
            }
        }

        return isSuccess
    }

    var tDialog: TDialog? = null

    /**
     * 显示二次验证的Dialog
     *
     * 13-数字货币提现
     */
    private fun showVerifyDialog() {
        tDialog = NewDialogUtils.showSecondDialog(this, 28, object : NewDialogUtils.DialogSecondListener {
            override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {
                if (operate == ADD) {
                    // TODO 显示 对话框
                    /**
                     * 新加 支付方式
                     */

                    when (payment) {
                        WECHATPAY -> {
                            addPayment4OTC(payment, et_username.text.toString()
                                    , et_pay_account.text.toString(), firstImgPath, ""
                                    , et_bank4deposit.text.toString(), ""
                                    , "", phone ?: "", googleCode ?: "")
                        }
                        ALIPAY -> {
                            addPayment4OTC(payment, et_username.text.toString()
                                    , et_pay_account.text.toString(), firstImgPath, ""
                                    , et_bank4deposit.text.toString(), ""
                                    , "", phone ?: "", googleCode ?: "")
                        }

                        BANKPAY -> {
                            addPayment4OTC(payment, et_username.text.toString()
                                    , et_bank4number.text.toString(), firstImgPath, et_pay_account.text.toString()
                                    , et_bank4deposit.text.toString(), ""
                                    , "", phone ?: "", googleCode ?: "")
                        }
                        else -> {
                            addPayment4OTC(payment, et_username.text.toString(), et_pay_account.text.toString(),
                                    "", "", "", "", "", phone
                                    ?: "", googleCode ?: "")
                        }

                    }


                } else {
                    /**
                     * 编辑支付方式
                     *  id: String,
                     *payment: String,
                     *userName: String,
                     *account: String,
                     *qrcodeImg: String,
                     *bankName: String,
                     *bankOfDeposit: String,
                     *ifscCode: String,
                     *remittanceInformation: String,
                     */

                    when (payment) {
                        WECHATPAY -> {
                            updatePayment4OTC(paymentBean?.optString("id")
                                    ?: "", paymentBean?.optString("payment")!!, et_username.text.toString()
                                    , et_pay_account.text.toString()
                                    , firstImgPath
                                    , ""
                                    , ""
                                    , paymentBean?.optString("ifscCode")!!, "", phone
                                    ?: "", googleCode ?: "")
                        }
                        ALIPAY -> {
                            updatePayment4OTC(paymentBean?.optString("id")
                                    ?: "", paymentBean?.optString("payment")!!, et_username.text.toString()
                                    , et_pay_account.text.toString()
                                    , firstImgPath
                                    , ""
                                    , ""
                                    , paymentBean?.optString("ifscCode")!!, "", phone
                                    ?: "", googleCode ?: "")
                        }

                        BANKPAY -> {
                            updatePayment4OTC(paymentBean?.optString("id").toString(), paymentBean?.optString("payment")!!, et_username.text.toString()
                                    , et_bank4number.text.toString()
                                    , firstImgPath
                                    , et_pay_account.text.toString()
                                    , et_bank4deposit.text.toString()
                                    , paymentBean?.optString("ifscCode")!!, "", phone
                                    ?: "", googleCode ?: "")
                        }
                    }

                }

                tDialog?.dismiss()
            }

        }, loginPwdShow = false)

    }

    var imageDialog: TDialog? = null
    /**
     * 照片选择 或者  证件选择
     * titleText 标题
     * fristText 第一个选项
     * secondTextUtils 第二个选项
     */
    fun showBottomDialog() {
        imageDialog = NewDialogUtils.showBottomListDialog(this, arrayListOf(LanguageUtil.getString(this, "noun_camera_takeAlbum"), LanguageUtil.getString(this, "noun_camera_takephoto")), 0, object : NewDialogUtils.DialogOnclickListener {
            override fun clickItem(data: ArrayList<String>, item: Int) {
                when (item) {
                    0 -> {
                        if (isFinishing && isDestroyed) return
                        imageTool?.openGallery("")
                    }
                    1 -> {
                        if (isFinishing && isDestroyed) return
                        openCamera()
                    }
                }
                imageDialog?.dismiss()
            }
        })
    }

    /**
     * 获取相机权限
     */
    private fun openCamera() {
        val rxPermissions: RxPermissions = RxPermissions(this)
        rxPermissions.request(android.Manifest.permission.CAMERA)
                .subscribe({ granted ->
                    if (granted) {
                        imageTool?.openCamera("")
                    } else {
                        ToastUtils.showToast(LanguageUtil.getString(this, "warn_camera_permission"))
                    }

                })
    }

    /**
     * 查看支付方式
     */
    fun previewPayment() {
        btn_finished.visibility = View.GONE
        et_pay_account.isFocusable = false
        et_pay_account.isFocusableInTouchMode = false
        when (payment) {
            ALIPAY -> {
                v_title.setContentTitle(LanguageUtil.getString(this, "payMethod_text_alipay"))
                tv_name_title.text = LanguageUtil.getString(this, "otc_text_payee")
                et_username.setText(UserDataService.getInstance()?.realName)
                tv_account_title.text = LanguageUtil.getString(this, "alipay_text_account")
                et_pay_account.setText(paymentBean?.optString("account"))
                GlideUtils.loadPaymentImage(context, paymentBean?.optString("qrcodeImg"), iv_upload_pay_qrcode)
                activity_edit_payment_title.text = LanguageUtil.getString(this, "alipay_text_qrcode")
                ll_bank4deposit.visibility = View.GONE
                rl_pay_qrcode.visibility = View.VISIBLE
                ll_bank4number.visibility = View.GONE
            }

            WECHATPAY -> {
                tv_name_title.text = LanguageUtil.getString(this, "otc_text_payee")
                et_username.setText(UserDataService.getInstance()?.realName)
                v_title.setContentTitle(LanguageUtil.getString(this, "pyaMethod_text_wxpay"))
                tv_account_title.text = LanguageUtil.getString(this, "otc_text_wxID")
                et_pay_account.setText(paymentBean?.optString("account"))
                GlideUtils.loadPaymentImage(context, paymentBean?.optString("qrcodeImg"), iv_upload_pay_qrcode)
                activity_edit_payment_title.text = LanguageUtil.getString(this, "wxpay_text_qrcode")
                rl_pay_qrcode.visibility = View.VISIBLE
                ll_bank4deposit.visibility = View.GONE
                ll_bank4number.visibility = View.GONE

            }

            BANKPAY -> {
                v_title.setContentTitle(LanguageUtil.getString(this, "otc_text_bindBankCard"))
                tv_name_title.text = LanguageUtil.getString(this, "otc_text_bindBankCard")
                et_username.setText(UserDataService.getInstance()?.realName)

                tv_account_title.text = LanguageUtil.getString(this, "otc_text_bankName")
                /**
                 * 验证实名认证的名字 & 填写的是否一致
                 */
                et_username.setText(UserDataService.getInstance().realName)
                et_username.text?.length?.let { et_username.setSelection(it) }
                et_username.isFocusable = false
                et_username.isFocusableInTouchMode = false


                et_pay_account.setText(paymentBean?.optString("bankName"))
                et_bank4deposit.setText(paymentBean?.optString("bankOfDeposit"))
                ll_bank4deposit.visibility = View.VISIBLE
                ll_bank4number.visibility = View.VISIBLE
                rl_pay_qrcode.visibility = View.GONE
                et_bank4number.setText(paymentBean?.optString("account"))
            }
            else -> {
                v_title.setContentTitle(payTitle)
                et_pay_account.setText(paymentBean?.optString("account"))
                et_username.setText(UserDataService.getInstance()?.realName)
            }

        }

    }


    /**
     * 编辑支付方式
     */
    private fun editPayment() {
//        tv_payment.text = paymentBean?.title
        var data = UserDataService.getInstance().userData
        var otcCompanyInfo = data?.optJSONObject("otcCompanyInfo")


        if (UserDataService.getInstance()?.authLevel == 1) {
            et_username.setText(UserDataService.getInstance()?.realName)
        } else {
            et_username.setText(paymentBean?.optString("userName"))
        }
        v_title.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {

            }

            override fun onclickName() {

            }
        }
        when (paymentBean?.optString("payment")) {
            "otc.payment.wxpay" -> {
                tv_name_title.text = LanguageUtil.getString(this, "otc_text_wxID")
                et_username.setText(UserDataService.getInstance()?.realName)
                v_title.setContentTitle(LanguageUtil.getString(this, "otc_text_wxID"))
                activity_edit_payment_title.text = LanguageUtil.getString(this, "wxpay_text_qrcode")
                tv_account_title.text = LanguageUtil.getString(this, "otc_text_wxID")
                ll_bank4deposit.visibility = View.GONE
                rl_pay_qrcode.visibility = View.VISIBLE
                ll_bank4number.visibility = View.GONE
                et_pay_account.setText(paymentBean?.optString("account"))
                GlideUtils.loadPaymentImage(context, paymentBean?.optString("qrcodeImg"), iv_upload_pay_qrcode)
                iv_cancel.visibility = View.VISIBLE

            }

            "otc.payment.alipay" -> {
                tv_name_title.text = LanguageUtil.getString(this, "new_otc_real_name")
                et_username.setText(UserDataService.getInstance()?.realName)
                v_title.setContentTitle(LanguageUtil.getString(this, "alipay_text_account"))
                tv_account_title.text = LanguageUtil.getString(this, "alipay_text_account")
                activity_edit_payment_title.text = LanguageUtil.getString(this, "alipay_text_qrcode")
                ll_bank4deposit.visibility = View.GONE
                rl_pay_qrcode.visibility = View.VISIBLE
                ll_bank4number.visibility = View.GONE
                et_pay_account.setText(paymentBean?.optString("account"))
                GlideUtils.loadPaymentImage(context, paymentBean?.optString("qrcodeImg"), iv_upload_pay_qrcode)
                iv_cancel.visibility = View.VISIBLE
            }

            "otc.payment.domestic.bank.transfer" -> {
                tv_name_title.text = LanguageUtil.getString(this, "kyc_text_name")
                et_username.setText(UserDataService.getInstance()?.realName)
                tv_account_title.text = LanguageUtil.getString(this, "otc_text_bankName")
                v_title.setContentTitle(LanguageUtil.getString(this, "otc_text_bindBankCard"))
                ll_bank4deposit.visibility = View.VISIBLE
                rl_pay_qrcode.visibility = View.GONE
                ll_bank4number.visibility = View.VISIBLE
                /**
                 * 验证实名认证的名字 & 填写的是否一致
                 */

                et_username.isFocusable = false
                et_username.isFocusableInTouchMode = false

                et_pay_account.setText(paymentBean?.optString("bankName"))
                et_bank4deposit.setText(paymentBean?.optString("bankOfDeposit"))

                et_bank4number.setText(paymentBean?.optString("account"))

            }
            else -> {
                v_title.setContentTitle(payTitle)
                et_username.setText(UserDataService.getInstance()?.realName)
            }
        }
        var status = otcCompanyInfo?.optString("status") ?: ""
        if (status == "0") {
            et_username.isFocusable = true
            et_username.isFocusableInTouchMode = true
        } else {
            var userCompanyInfo = data?.optJSONObject("userCompanyInfo")

            var status = userCompanyInfo?.optString("status") ?: ""
            if ("1" == status || "3" == status) {
                et_username.isFocusable = true
                et_username.isFocusableInTouchMode = true
            } else {
                et_username.isFocusable = false
                et_username.isFocusableInTouchMode = false
            }
        }

    }


    /**
     * 添加支付方式
     */
    private fun addPayment() {
        var data = UserDataService.getInstance().userData
        var otcCompanyInfo = data?.optJSONObject("otcCompanyInfo")
        when (payment) {
            ALIPAY -> {
                v_title.setContentTitle(LanguageUtil.getString(this, "alipay_text_bind"))
                tv_name_title.text = LanguageUtil.getString(this, "otc_text_payee")
                et_username.setText(UserDataService.getInstance()?.realName)
                tv_account_title.text = LanguageUtil.getString(this, "alipay_text_account")
                et_pay_account.hint = LanguageUtil.getString(this, "alipay_text_account")
                activity_edit_payment_title.text = LanguageUtil.getString(this, "alipay_text_qrcode")
                ll_bank4deposit.visibility = View.GONE
                rl_pay_qrcode.visibility = View.VISIBLE
                ll_bank4number.visibility = View.GONE
            }

            WECHATPAY -> {
                tv_name_title.text = LanguageUtil.getString(this, "otc_text_payee")
                et_username.setText(UserDataService.getInstance()?.realName)
                v_title.setContentTitle(LanguageUtil.getString(this, "wxpay_text_bind"))
                tv_account_title.text = LanguageUtil.getString(this, "otc_text_wxID")
                et_pay_account.hint = LanguageUtil.getString(this, "otc_wx_a_input")
                activity_edit_payment_title.text = LanguageUtil.getString(this, "wxpay_text_qrcode")
                rl_pay_qrcode.visibility = View.VISIBLE
                ll_bank4deposit.visibility = View.GONE
                ll_bank4number.visibility = View.GONE

            }

            BANKPAY -> {
                tv_name_title.text = LanguageUtil.getString(this, "otc_text_payee")
                et_username.setText(UserDataService.getInstance()?.realName)
                v_title.setContentTitle(LanguageUtil.getString(this, "otc_text_bindBankCard"))
                tv_account_title.text = LanguageUtil.getString(this, "otc_text_bankName")
                /**
                 * 验证实名认证的名字 & 填写的是否一致
                 */
                et_username.setText(UserDataService.getInstance().realName)
                et_username.text?.length?.let { et_username.setSelection(it) }
                et_username.isFocusable = false
                et_username.isFocusableInTouchMode = false


                ll_bank4deposit.visibility = View.VISIBLE
                ll_bank4number.visibility = View.VISIBLE
                rl_pay_qrcode.visibility = View.GONE
            }
            else -> {
                v_title.setContentTitle(payTitle)
                et_username.setText(UserDataService.getInstance()?.realName)
            }

        }
        et_pay_account.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    btn_finished.isEnable(true)
                }
            }

        })
        var status = otcCompanyInfo?.optString("status") ?: ""
        if (status == "0") {
            et_username.isFocusable = true
            et_username.isFocusableInTouchMode = true
        } else {
            var userCompanyInfo = data?.optJSONObject("userCompanyInfo")

            var status = userCompanyInfo?.optString("status") ?: ""
            if ("1" == status || "3" == status) {
                et_username.isFocusable = true
                et_username.isFocusableInTouchMode = true
            } else {
                et_username.isFocusable = false
                et_username.isFocusableInTouchMode = false
            }
        }


    }


    /**
     * 添加支付方式
     */
    private fun addPayment4OTC(payment: String,
                               userName: String,
                               account: String,
                               qrcodeImg: String,
                               bankName: String,
                               bankOfDeposit: String,
                               ifscCode: String,
                               remittanceInformation: String,
                               smsAuthCode: String,
                               googleCode: String) {
        if (TextUtils.isEmpty(userName)) {
            return
        }
        showProgressDialog()
        HttpClient.instance
                .addPayment4OTC(payment = payment,
                        userName = userName,
                        account = account,
                        qrcodeImg = qrcodeImg,
                        bankName = bankName,
                        bankOfDeposit = bankOfDeposit,
                        ifscCode = ifscCode,
                        remittanceInformation = remittanceInformation,
                        smsAuthCode = smsAuthCode,
                        googleCode = googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        cancelProgressDialog()
                        NToastUtil.showTopToastNet(this@EditPaymentActivity,true, LanguageUtil.getString(this@EditPaymentActivity, "common_tip_addSuccess"))
                        finish()
                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        NToastUtil.showTopToastNet(this@EditPaymentActivity,false, msg)

                    }
                })
    }


    /**
     * 更新支付方式
     */
    private fun updatePayment4OTC(
            id: String,
            payment: String,
            userName: String,
            account: String,
            qrcodeImg: String,
            bankName: String,
            bankOfDeposit: String,
            ifscCode: String,
            remittanceInformation: String,
            smsAuthCode: String,
            googleCode: String) {
        showProgressDialog()
        HttpClient.instance
                .updatePayment4OTC(
                        id = id,
                        payment = payment,
                        userName = userName,
                        account = account,
                        qrcodeImg = qrcodeImg,
                        bankName = bankName,
                        bankOfDeposit = bankOfDeposit,
                        ifscCode = ifscCode,
                        remittanceInformation = remittanceInformation,
                        smsAuthCode = smsAuthCode,
                        googleCode = googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        cancelProgressDialog()
                        finish()
                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        NToastUtil.showTopToastNet(this@EditPaymentActivity,false, msg)
                    }
                })
    }

    var LocalAddress: String? = ""
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageTool?.onAcitvityResult(requestCode, resultCode, data) { bitmap, path ->
            var options = RequestOptions().placeholder(R.drawable.ic_sample).error(R.drawable.ic_sample)
            GlideUtils.load(this, path, iv_upload_pay_qrcode, options)
            val bitmap2Base64 = imageTool?.bitmap2Base64(bitmap)

            uploadImg(bitmap2Base64, FIRST_INDEX)

            LocalAddress = path
            if (PublicInfoDataService.getInstance().getUploadImgType(null) == "1") {
                Utils.saveBitmap(LocalAddress, object : OnSaveSuccessListener {
                    override fun onSuccess(path: String) {
                        if (LocalAddress != null) {
                            loadingImage(LocalAddress)
                        }
                    }
                })
            } else {
                var options = RequestOptions().placeholder(R.drawable.assets_addingpaymentmethod)
                        .error(R.drawable.assets_addingpaymentmethod)
                GlideUtils.load(this, path, iv_upload_pay_qrcode, options)
                val bitmap2Base64 = imageTool?.bitmap2Base64(bitmap)
                uploadImg(bitmap2Base64, FIRST_INDEX)
            }
        }
    }

    var firstImgPath = ""
    /**
     * 上传照片
     */
    fun uploadImg(imageBase: String?, index: Int) {
        showProgressDialog(LanguageUtil.getString(this, "pic_uploading"))
        HttpClient.instance.uploadImg(imgBase64 = imageBase ?: "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(t: JsonObject?) {
                        if (t == null) return
                        var json = JSONObject(t.toString())
                        cancelProgressDialog()

                        val baseImgUrl = json.getString("base_image_url")
                        val fileName = json.getString("filename")

                        if (!TextUtils.isEmpty(fileName)) {
                            NToastUtil.showTopToastNet(this@EditPaymentActivity,true, LanguageUtil.getString(this@EditPaymentActivity, "toast_upload_pic_suc"))
                        } else {
                            NToastUtil.showTopToastNet(this@EditPaymentActivity,false, LanguageUtil.getString(this@EditPaymentActivity, "toast_upload_pic_failed"))
                        }


                        firstImgPath = baseImgUrl + fileName
                        if (TextUtils.isEmpty(fileName)) {
                            iv_upload_pay_qrcode.setImageResource(R.drawable.ic_sample)
                        }
                        iv_cancel.visibility = View.VISIBLE
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        NToastUtil.showTopToastNet(this@EditPaymentActivity,false, LanguageUtil.getString(this@EditPaymentActivity, "toast_upload_pic_failed"))
                    }
                })
    }


    var isRefresh = false
    /**
     * 新接口 获取token 图片
     */
    fun getImageToken(operate_type: String? = "2", path: String? = "") {
        showProgressDialog()
        HttpClient.instance.getImageToken(operate_type ?: "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ImageTokenBean>() {
                    override fun onHandleSuccess(t: ImageTokenBean?) {
                        cancelProgressDialog()
                        t ?: return
                        imageTokenBean = t
                        if (isRefresh) {
                            isRefresh = false
                            loadingImage(path)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        ToastUtils.showToast(msg)
                    }

                })

    }

    /**
     * 上传图片oss 方式
     */
    fun loadingImage(path: String?) {
        if (!StringUtil.checkStr(path))
            return
        showProgressDialog()
        var uploadHelper = UploadHelper.uploadImage(path, imageTokenBean.AccessKeyId, imageTokenBean.AccessKeySecret, imageTokenBean.bucketName,
                imageTokenBean.ossUrl, imageTokenBean.SecurityToken, imageTokenBean.catalog)
        cancelProgressDialog()
        if (TextUtils.isEmpty(uploadHelper)) {
            isRefresh = true
            getImageToken(operate_type = "2", path = path)
            return
        }
        var options = RequestOptions().placeholder(R.drawable.ic_sample)
                .error(R.drawable.ic_sample)

        cancelProgressDialog()
        iv_cancel.visibility = View.VISIBLE
        firstImgPath = path ?: ""
        firstImgPath = uploadHelper.substring(uploadHelper.indexOf(imageTokenBean.catalog))
        GlideUtils.load(this@EditPaymentActivity, path, iv_upload_pay_qrcode, options)
        iv_cancel.visibility = View.VISIBLE
    }


    /**
     * 删除支付方式
     */
    private fun removePayment4OTC(position: String) {

        HttpClient.instance
                .removePayment4OTC(id = position, smsAuthCode = "", googleCode = "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@EditPaymentActivity, "otc_tip_paymentDeactiveSuccess"), isSuc = true)
                        finish()
                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)

                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }

}
