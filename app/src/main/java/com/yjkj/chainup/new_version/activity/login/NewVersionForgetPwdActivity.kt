package com.yjkj.chainup.new_version.activity.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.alibaba.android.arouter.facade.annotation.Route
import com.jaeger.library.StatusBarUtil
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.FindPwd2verifyActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.Gt3GeeListener
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.activity_new_version_forget_pwd.*
import org.json.JSONObject
import java.util.*

/**
 * @Author lianshangljl
 * @Date 2019/3/13-3:24 PM
 * @Email buptjinlong@163.com
 * @description 忘记密码
 */
@Route(path = "/login/newversionforgetpwdactivity")
class NewVersionForgetPwdActivity : NBaseActivity(), Gt3GeeListener {
    override fun setContentView(): Int {
        return R.layout.activity_new_version_forget_pwd
    }

    var accountText = ""

    override fun initView() {
        StatusBarUtil.setColor(this, ColorUtil.getColorByMode(R.color.bg_card_color_day), 0)
        cet_view?.isFocusable = true
        cet_view?.isFocusableInTouchMode = true
        cubtn_view?.isEnable(false)
        cet_view?.setOnFocusChangeListener { v, hasFocus ->
            view_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        setOnClick()
        setTextContent()
    }

    fun setTextContent() {
        tv_title?.text = LanguageUtil.getString(this, "login_action_fogotPassword")
        cet_view?.hint = LanguageUtil.getString(this, "common_tip_inputPhoneOrMail")
        cubtn_view?.setBottomTextContent(LanguageUtil.getString(this, "common_action_next"))
    }

    var tDialog: TDialog? = null

    fun setOnClick() {
        cet_view?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                accountText = s.toString()
                if (accountText.isNotEmpty()) {
                    cubtn_view?.isEnable(true)
                } else {
                    cubtn_view?.isEnable(false)
                }
            }

        })

        iv_cancel?.setOnClickListener { finish() }

        cubtn_view?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                verificationType = PublicInfoDataService.getInstance().getVerifyType(null)
                when (verificationType) {
                    2 -> {
                        Utils.gee3test(this@NewVersionForgetPwdActivity, this@NewVersionForgetPwdActivity)
                    }
                    1 -> {
                        tDialog = NewDialogUtils.webAliyunShare(mActivity, object : NewDialogUtils.Companion.DialogWebViewAliYunSlideListener {
                            override fun webviewSlideListener(json: Map<String, String>) {
                                tDialog?.apply {
                                    dismiss()
                                    onDestroy()
                                }
                                findPwdStep1(accountText, "", "", "",json)
                            }

                        }, PublicInfoDataService.getInstance().aliYunNcUrl)
                    }
                    else -> {
                        findPwdStep1(accountText)
                    }
                }
            }

        }
    }


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }

    /**
     * 极验
     */
    var gee3test = arrayListOf<String>()


    var verificationType = 2

    /**
     * 如果有极验 此处是极验成功返回的接口
     */
    override fun onSuccess(result: ArrayList<String>) {
        gee3test = result
        Utils.setGeetestDeatroy()
        findPwdStep1(accountText, gee3test[0], gee3test[1], gee3test[2])
    }

    var token = ""

    /**
     * 账号
     */
    var account = ""

    /**
     * 找回密码
     * @param registerCode 填写手机号或者邮箱
     */
    private fun findPwdStep1(registerCode: String,
                             geetest_challenge: String = "",
                             geetest_validate: String = "",
                             geetest_seccode: String = "", mAliyun: Map<String, String>? = null) {

        showLoadingDialog()
        var isPhoneNum = StringUtils.isNumeric(registerCode)
        var mobileNumber = ""
        var email = ""
        if (isPhoneNum) {
            mobileNumber = registerCode
        } else {
            email = registerCode
        }
        addDisposable(getMainModel().findPwdStep1(
                mobileNumber,
                email,
                verificationType = verificationType,
                geetest_challenge = geetest_challenge,
                geetest_validate = geetest_validate,
                geetest_seccode = geetest_seccode,
                aliyun = mAliyun,
                consumer = object : NDisposableObserver(true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        closeLoadingDialog()
                        var json = jsonObject.optJSONObject("data")
                        token = json?.optString("token") ?: ""
//                if (StringUtils.checkEmail(registerCode)) {
//                    var bundle = Bundle()
//                    bundle.putString("send_account", accountText)
//                    bundle.putString("send_token", token)
//                    bundle.putString("send_countryCode", "")
//                    bundle.putInt("send_position", 2)
//                    bundle.putInt("send_islogin", 2)
//                    ArouterUtil.navigation("/login/newphoneverificationactivity", bundle)
//                } else if (StringUtils.isNumeric(registerCode)) {
//                    var bundle = Bundle()
//                    bundle.putString("send_account", accountText)
//                    bundle.putString("send_token", token)
//                    bundle.putString("send_countryCode", "")
//                    bundle.putInt("send_position", 1)
//                    bundle.putInt("send_islogin", 2)
//                    ArouterUtil.navigation("/login/newphoneverificationactivity", bundle)
//                }
//                finish() // 否则出现Token过期就尴尬....

                        var isCertificateNumber = json?.optString("isCertificateNumber").equals("1") //下一步是否身份证验证，0表示不需要
                        var isGoogleAuth = json?.optString("isGoogleAuth").equals("1") //下一步是否进行google验证， 0不需要

//                        var isCertificateNumber = true //下一步是否身份证验证，0表示不需要
//                        var isGoogleAuth = true //下一步是否进行google验证， 0不需要

                        var codeType = if (isPhoneNum) AppConstant.FIND_PWD_MOBILE else AppConstant.FIND_PWD_EMAIL
                        NewDialogUtils.showForgetPwdSecurityVerificationDialog(this@NewVersionForgetPwdActivity, isPhoneNum, !isPhoneNum, isGoogleAuth, isCertificateNumber, codeType, object : NewDialogUtils.DialogVerifiactionNewListener {
                            override fun returnCode(phone: String?, mail: String?, phoneCode: String?, mailCode: String?, googleCode: String?, certifcateNumber: String?) {
                                addDisposable(getMainModel().findPwdStep2(
                                        token,
                                        phoneCode.toString(),
                                        phone.toString(),
                                        mailCode.toString(),
                                        mail.toString(),
                                        certifcateNumber.toString(),
                                        googleCode.toString(),
                                        consumer = MyNDisposableObserver(NewPhoneVerificationActivity.FINDPWDSTEP2_TYPE)))

                            }
                        }, -1, LanguageUtil.getString(this@NewVersionForgetPwdActivity, "common_text_btnConfirm"), token, accountText)
                    }

                    override fun onResponseFailure(code: Int, msg: String?) {
                        super.onResponseFailure(code, msg)
                        closeLoadingDialog()
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                }))
    }

    inner class MyNDisposableObserver(type: Int) : NDisposableObserver(this, true) {
        var reqType = type

        override fun onResponseSuccess(jsonObject: JSONObject) {
            var json = jsonObject.optJSONObject("data")
            when (reqType) {
                NewPhoneVerificationActivity.FINDPWDSTEP2_TYPE -> {
                    var isCertificateNumber = json?.optString("isCertificateNumber") ?: "0"
                    var isGoogleAuth = json?.optString("isGoogleAuth") ?: "0"
                    if (isCertificateNumber == "0" && isGoogleAuth == "0") {
                        /**
                         * Directly 跳转至"重置密码"界面
                         */
                        val bundle = Bundle()
                        bundle.putString("account_num", account)
                        bundle.putInt("index_status", 1)
                        bundle.putString("index_token", token)
                        bundle.putString("index_number_code", "")
                        bundle.putString("param", "")
                        ArouterUtil.navigation("/login/newsetpasswordactivity", bundle)
                    } else {
                        /**
                         * 验证 Google , ID
                         */
                        FindPwd2verifyActivity.enter2(token, isCertificateNumber.toInt(), isGoogleAuth.toInt(), account)
                    }
                    finish()
                }
            }


        }

        override fun onResponseFailure(code: Int, msg: String?) {
            super.onResponseFailure(code, msg)

        }

    }

}


