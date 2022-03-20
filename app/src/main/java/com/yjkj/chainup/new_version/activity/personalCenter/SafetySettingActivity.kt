package com.yjkj.chainup.new_version.activity.personalCenter

import android.content.Intent
import android.os.Bundle
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.util.JsonUtils
import com.google.gson.JsonObject
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.bean.UserInfoData
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_safety_setting.*

/**
 * @author bertking
 * @date 2018,5,21
 * @description 安全设置
 * TODO 代码待优化....
 */
@Route(path = RoutePath.SafetySettingActivity)
class SafetySettingActivity : NewBaseActivity() {
    lateinit var fingerprintManager: FingerprintManagerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_setting)
        context = this
        fingerprintManager = FingerprintManagerCompat.from(context)
        initOnClickListener()
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout.slidingShowTitle(status)
            }

        }
        title_layout?.setContentTitle(LanguageUtil.getString(this, "personal_text_safetycenter"))
        rl_phone?.setStatusText(LanguageUtil.getString(this, "personal_text_safeSettingOpen"))
        rl_phone?.setTitle(LanguageUtil.getString(this, "register_text_phone"))

        rl_email?.setTitle(LanguageUtil.getString(this, "register_text_mail"))
        rl_email?.setStatusText(LanguageUtil.getString(this, "personal_text_safeSettingOpen"))

        rl_google_verify?.setTitle(LanguageUtil.getString(this, "safety_text_googleAuth"))
        rl_google_verify?.setStatusText(LanguageUtil.getString(this, "close_verify"))

        rl_change_pwd?.setTitle(LanguageUtil.getString(this, "register_text_loginPwd"))
        rl_change_pwd?.setStatusText(LanguageUtil.getString(this, "filter_action_reset"))

        rl_fund_pwd?.setTitle(LanguageUtil.getString(this, "otc_text_pwd_forotc"))
        rl_fund_pwd?.setStatusText(LanguageUtil.getString(this, "filter_action_reset"))

        tv_safety_text_gesturePassword?.text = LanguageUtil.getString(this, "safety_text_gesturePassword")
        tv_login_text_fingerprint?.text = LanguageUtil.getString(this, "login_text_fingerprint")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "=====onResume()====")
//        getUserInfo()
        initView()
    }
//    fun getUserInfo(){
//        HttpClient.instance.getUserInfo()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object:NetObserver<UserInfoData>(){
//                    override fun onHandleSuccess(t: UserInfoData?) {
//
//                    }
//
//                    override fun onHandleError(msg: String?) {
//                        super.onHandleError(msg)
//                    }
//
//                })
//    }

    fun initView() {

        var status = !TextUtils.isEmpty(UserDataService.getInstance()
                .gesturePass) || !TextUtils.isEmpty(UserDataService.getInstance().gesturePwd)
        /**
         * 手势密码的状态
         */
        switch_gesture_pwd?.isChecked = status
        setViewSelect(switch_gesture_pwd, status)
        /**
         * 手机登录的情况
         */
        if (!TextUtils.isEmpty(UserDataService.getInstance().mobileNumber)) {
            if (UserDataService.getInstance().isOpenMobileCheck == 0) {
                rl_phone.setStatusText(LanguageUtil.getString(this, "personal_text_safeSettingOff"))
            } else {
                rl_phone.setStatusText(LanguageUtil.getString(this, "personal_text_safeSettingOpen"))
            }
            rl_phone.setOnClickListener {
                var bundle = Bundle()
                bundle.putInt(ParamConstant.VERIFY_TYPE, ParamConstant.MOBILE_TYPE)
                ArouterUtil.navigation(RoutePath.NewVerifyActivity, bundle)

            }
        } else {
            rl_phone.setStatusText(LanguageUtil.getString(this, "userinfo_text_mailUnbind"))
            rl_phone.setOnClickListener {
                BindMobileOrEmailActivity.enter2(this, BindMobileOrEmailActivity.MOBILE_TYPE, BindMobileOrEmailActivity.VALIDATION_BIND)
            }
        }


        /**
         * 邮箱登录的情况
         */
        if (TextUtils.isEmpty(UserDataService.getInstance().email)) {
            rl_email.setStatusText(LanguageUtil.getString(this, "userinfo_text_mailUnbind"))

            /**
             * 绑定邮箱
             */
            rl_email.setOnClickListener {
                if (!Utils.isFastClick()) {
                    BindMobileOrEmailActivity.enter2(context, BindMobileOrEmailActivity.MAIL_TYPE, BindMobileOrEmailActivity.VALIDATION_BIND)
                }

            }


        } else {
            rl_email.setStatusText(LanguageUtil.getString(this, "common_action_edit"))
            rl_email.setOnClickListener {
                if (!Utils.isFastClick()) {
                    var bundle = Bundle()
                    bundle.putInt(ParamConstant.VERIFY_TYPE, ParamConstant.MAIL_TYPE)
                    ArouterUtil.navigation(RoutePath.NewVerifyActivity, bundle)
                }
            }
        }


        /**
         * google验证
         */
        val googleStatus = UserDataService.getInstance().googleStatus


        if (googleStatus == 0) {
            rl_google_verify?.setStatusText(LanguageUtil.getString(this, "userinfo_text_mailUnbind"))

        } else {
            rl_google_verify?.setStatusText(LanguageUtil.getString(this, "personal_text_safeSettingOpen"))
        }
        rl_google_verify?.setOnClickListener {
            when (googleStatus) {
                0 -> {
                    startActivity(Intent(this, GoogleValidationActivity::class.java))
                }
                1 -> {
                    if (UserDataService.getInstance().isOpenMobileCheck == 1) {
                        var bundle = Bundle()
                        bundle.putInt(ParamConstant.VERIFY_TYPE, ParamConstant.GOOGLE_TYPE)
                        ArouterUtil.navigation(RoutePath.NewVerifyActivity, bundle)

                    } else {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "login_tip_bindPhoneFirst"), isSuc = false)
                        return@setOnClickListener
                    }
                }
            }

        }


        /**
         * 资金密码 --> 显示隐藏判断
         */
        if (PublicInfoDataService.getInstance().otcOpen(null)) {
            rl_fund_pwd.visibility = View.VISIBLE
        } else {
            rl_fund_pwd.visibility = View.GONE
        }


        /**
         * 资金密码
         *
         */
        val pwdTitle = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
            LanguageUtil.getString(this, "otc_text_pwd_forotc")
        } else {
            LanguageUtil.getString(this, "otc_text_pwd")
        }

        rl_fund_pwd.setTitle(pwdTitle)

        if (UserDataService.getInstance().isCapitalPwordSet == 0) {

            rl_fund_pwd.setStatusText(LanguageUtil.getString(this, "personal_text_setting"))

        } else {
            rl_fund_pwd.setStatusText(LanguageUtil.getString(this, "common_action_edit"))

        }

        rl_fund_pwd.setOnClickListener {
            if (UserDataService.getInstance().isOpenMobileCheck == 0 && UserDataService.getInstance().googleStatus == 0) {
                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "unbind_verify_warn"), isSuc = false)
                return@setOnClickListener
            }
            if (!JsonUtils.getCertification(this@SafetySettingActivity)) {
                return@setOnClickListener
            }

            val isFirstSet = UserDataService.getInstance().isCapitalPwordSet == 0
            if (PublicInfoDataService.getInstance().getSafeWithdraw(null)?.optString("is_open", "") == "1" && !isFirstSet) {
                NewDialogUtils.showNormalDialog(this@SafetySettingActivity, String.format(LanguageUtil.getString(this, "login_tip_safeSettingChange"),
                        PublicInfoDataService.getInstance().getSafeWithdraw(PublicInfoDataService.getInstance().getData(null))?.optString("hour")), object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().isOpenMobileCheck == 0 && UserDataService.getInstance().googleStatus == 0) {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SafetySettingActivity, "unbind_verify_warn"), isSuc = false)

                            return
                        }
                        if (isFirstSet) {
                            ArouterUtil.forwardModifyPwdPage(ParamConstant.SET_PWD, ParamConstant.FROM_OTC)
                        } else {
                            ArouterUtil.forwardModifyPwdPage(ParamConstant.RESET_PWD, ParamConstant.FROM_OTC)
                        }
                    }
                })
            } else {
                if (isFirstSet) {
                    ArouterUtil.forwardModifyPwdPage(ParamConstant.SET_PWD, ParamConstant.FROM_OTC)
                } else {
                    ArouterUtil.forwardModifyPwdPage(ParamConstant.RESET_PWD, ParamConstant.FROM_OTC)
                }
            }
        }
    }


    private fun initOnClickListener() {
        /**
         * 修改密码
         */
        rl_change_pwd.setOnClickListener {
            if (UserDataService.getInstance().isOpenMobileCheck != 1 && UserDataService.getInstance().googleStatus != 1) {
                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_text_pleaseBindGoogleFirst"), isSuc = false)
                return@setOnClickListener
            }
            if (PublicInfoDataService.getInstance().getSafeWithdraw(null)?.optString("is_open", "") == "1") {
                NewDialogUtils.showNormalDialog(this@SafetySettingActivity, String.format(LanguageUtil.getString(this, "login_tip_safeSettingChange"), PublicInfoDataService.getInstance().getSafeWithdraw(PublicInfoDataService.getInstance().getData(null))?.optString("hour", "")), object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().isOpenMobileCheck == 0 && UserDataService.getInstance().googleStatus == 0) {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SafetySettingActivity, "unbind_verify_warn"), isSuc = false)

                            return
                        }
                        ArouterUtil.forwardModifyPwdPage(ParamConstant.RESET_PWD, ParamConstant.FROM_LOGIN)
                    }
                })
            } else {
                ArouterUtil.forwardModifyPwdPage(ParamConstant.RESET_PWD, ParamConstant.FROM_LOGIN)
            }

        }


        /**
         * 手势密码
         */
        switch_gesture_pwd?.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                Log.d(TAG, "====hhhh=======" + isChecked)
                if (!buttonView!!.isPressed) return
                if (LoginManager.getInstance().fingerprint == 1) {
                    switch_gesture_pwd?.isChecked = false
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SafetySettingActivity, "common_tip_activeLimit"), isSuc = false)
                    return
                }
                switch_gesture_pwd?.isChecked = isChecked
                setViewSelect(switch_gesture_pwd, isChecked)
                if (isChecked) {
                    showGestureVerifyDialog(true)
                } else {
                    if (TextUtils.isEmpty(UserDataService.getInstance().gesturePass)) {
                        return
                    }
                    showGestureVerifyDialog(false)
                }
            }
        })


        /**
         * 指纹识别
         */
        if (!fingerprintManager.isHardwareDetected) {
            rl_fingerprint.visibility = View.GONE
        } else {
            rl_fingerprint.visibility = View.VISIBLE
            /**
             * 是否录制了指纹
             */
            if (fingerprintManager.hasEnrolledFingerprints()) {
                /**
                 * 根据打开状态去判断
                 */
                switch_fingerprint_pwd.isChecked = LoginManager.getInstance().fingerprint == 1
                setViewSelect(switch_fingerprint_pwd, LoginManager.getInstance().fingerprint == 1)

            } else {

                switch_fingerprint_pwd.isChecked = false
                setViewSelect(switch_fingerprint_pwd, false)


            }
        }

        switch_fingerprint_pwd.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                /**
                 * 判断是否支持指纹
                 */
                if (fingerprintManager.isHardwareDetected) {
                    /**
                     * 判断是否输入指纹
                     */
                    if (!fingerprintManager.hasEnrolledFingerprints()) {
                        switch_fingerprint_pwd.isChecked = false
                        setViewSelect(switch_fingerprint_pwd, false)

                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SafetySettingActivity, "no_fingerprints_were_entered"), isSuc = false)

                        return
                    }
                }


                if (!TextUtils.isEmpty(UserDataService.getInstance().gesturePass)) {
                    switch_fingerprint_pwd.isChecked = false
                    setViewSelect(switch_fingerprint_pwd, false)
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SafetySettingActivity, "only_set_one_login_way"), isSuc = false)

                    return
                }
                if (!buttonView!!.isPressed) return
                if (isChecked) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SafetySettingActivity, "common_tip_addSuccess"), isSuc = true)
                    val fingerprint = 1
                    LoginManager.getInstance().saveFingerprint(fingerprint)
                    switch_fingerprint_pwd.isChecked = LoginManager.getInstance().fingerprint == fingerprint
                    setViewSelect(switch_fingerprint_pwd, LoginManager.getInstance().fingerprint == fingerprint)
                } else {
                    LoginManager.getInstance().saveFingerprint(0)
                    setViewSelect(switch_fingerprint_pwd, LoginManager.getInstance().fingerprint == 1)
                }
            }

        })

    }

    var dialog: TDialog? = null
    /**
     * 设置手势密码
     * isopen true开启手势密码 false关闭手势密码
     */
    private fun showGestureVerifyDialog(isOpen: Boolean) {
        dialog = NewDialogUtils.showCertificationSecondDialog(this, AppConstant.GESTURE_PWD, object : NewDialogUtils.DialogSecondListener, NewDialogUtils.DialogCertificationSecondListener {
            override fun cancelBtn() {
                switch_gesture_pwd?.isChecked = !isOpen
                setViewSelect(switch_gesture_pwd, !isOpen)
            }

            override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {
                dialog?.dismiss()
                /**
                 * 请求服务器开启手势密码
                 */
                if (isOpen) {
                    showProgressDialog()
                    HttpClient.instance.openHandPwd(pwd ?: "", phone ?: "", googleCode ?: "")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : NetObserver<JsonObject>() {
                                override fun onHandleSuccess(t: JsonObject?) {
                                    cancelProgressDialog()
                                    Log.d(TAG, "=======手势密码:=====" + t.toString())

                                    var bundle = Bundle()
                                    bundle.putInt("SET_TYPE", 0)
                                    bundle.putString("SET_TOKEN", t?.get("token")?.asString
                                            ?: "")
                                    bundle.putBoolean("SET_STATUS", false)
                                    bundle.putBoolean("SET_LOGINANDSET", false)
                                    ArouterUtil.navigation("/login/gesturespasswordactivity", bundle)

                                    switch_gesture_pwd?.isChecked = true
                                    setViewSelect(switch_gesture_pwd, true)

                                }

                                override fun onHandleError(code: Int, msg: String?) {
                                    super.onHandleError(code, msg)
                                    cancelProgressDialog()
                                    switch_gesture_pwd?.isChecked = false
                                    setViewSelect(switch_gesture_pwd, false)
                                    DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                                    Log.d(TAG, "======开启手势密码：=====error==" + msg)

                                }
                            })
                } else {
                    /**
                     * 关闭手势密码
                     */
                    showProgressDialog()
                    HttpClient.instance.closeHandPwd(pwd ?: "", phone ?: "", googleCode ?: "")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : NetObserver<Any>() {
                                override fun onHandleSuccess(t: Any?) {
                                    cancelProgressDialog()
                                    switch_gesture_pwd?.isChecked = false
                                    setViewSelect(switch_gesture_pwd, false)
                                    UserDataService.getInstance().saveGesturePass("")
                                    var userInfoData = UserDataService.getInstance().userData
                                    userInfoData.put("gesturePwd", "")
                                    UserDataService.getInstance().saveData(userInfoData)
                                }

                                override fun onHandleError(code: Int, msg: String?) {
                                    super.onHandleError(code, msg)
                                    cancelProgressDialog()
                                    switch_gesture_pwd?.isChecked = true
                                    setViewSelect(switch_gesture_pwd, true)
                                    DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                                }
                            })
                }
            }

        },confirmTitle = LanguageUtil.getString(this,"common_text_btnConfirm"))

    }

    fun setViewSelect(view: View, status: Boolean) {
        if (status) {
            view.setBackgroundResource(R.drawable.open)
        } else {
            view.setBackgroundResource(R.drawable.shut_down)
        }
    }

}
