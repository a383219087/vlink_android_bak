package com.yjkj.chainup.new_version.activity.login

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.jaeger.library.StatusBarUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.RegStep2Bean
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.ActivityManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.PwdSettingView
import com.yjkj.chainup.util.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_new_version_set_pwd.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * @Author lianshangljl
 * @Date 2019/3/13-3:23 PM
 * @Email buptjinlong@163.com
 * @description 设置密码 or 重置密码
 */
@Route(path = "/login/newsetpasswordactivity")
class NewSetPasswordActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_new_version_set_pwd
    }

    lateinit var fingerprintManager: FingerprintManagerCompat

    var bean: RegStep2Bean? = null

    var account = ""
    var pwdContent = ""
    var pwdAgainContent = ""
    var index = 0
    var token = ""
    var numberCode = ""

    /**
     * 安全验证 默认是 google
     *  Google 0
     *  手机   1
     *  邮箱   2
     */
    var securityVerificationType = 0

    companion object {
        /**
         * @param account 账号
         * @param INDEX_STATUS 设置密码 or 重置密码  0 设置密码 1 重置密码
         */
        private const val ACCOUNT_NUM = "account_num"
        private const val INDEX_STATUS = "index_status"
        private const val INDEX_TOKEN = "index_token"
        private const val INDEX_NUMBER_CODE = "index_number_code"

        private const val PARAM = "param"

    }


    fun getData() {
        if (intent != null) {
            account = intent.getStringExtra(ACCOUNT_NUM) ?: ""
            token = intent.getStringExtra(INDEX_TOKEN) ?: ""
            numberCode = intent.getStringExtra(INDEX_NUMBER_CODE) ?: ""
            index = intent.getIntExtra(INDEX_STATUS, 0)
        }
    }

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }


    override fun initView() {
        fingerprintManager = FingerprintManagerCompat.from(this)
        StatusBarUtil.setColor(this, ColorUtil.getColorByMode(R.color.bg_card_color_day), 0)
        getData()
        tv_info?.text = LanguageUtil.getString(this, "register_tip_agreement")
        tv_terms_service?.text = LanguageUtil.getString(this, "register_action_agreement")

        cet_pwd_view?.isFocusable = true
        cet_pwd_view?.isFocusableInTouchMode = true
        cet_pwd_view?.setHintEditText(LanguageUtil.getString(this, "password_input_rule_tips"))
        cet_pwd_again_view?.isFocusable = true
        cet_pwd_again_view?.isFocusableInTouchMode = true

        cet_pwd_invite_code_view?.isFocusable = true
        cet_pwd_invite_code_view?.isFocusableInTouchMode = true
        cet_pwd_invite_code_view?.setOnFocusChangeListener { v, hasFocus ->
            v_line_pwd?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }

        cet_pwd_again_view?.setHintEditText(LanguageUtil.getString(this, "register_tip_repeatPassword"))
        cet_pwd_invite_code_view?.hint = LanguageUtil.getString(this, "invite_code_hint")
        cubtn_view?.isEnable(false)


        /**
         * 配置是
         */
        when (index) {
            0 -> {
                tv_title?.text = LanguageUtil.getString(mActivity, "register_action_setPassword")
                cet_pwd_invite_code_view?.visibility = View.VISIBLE
                cubtn_view?.setContent(LanguageUtil.getString(mActivity, "register_action_register"))
                bean = intent?.getParcelableExtra(PARAM)
                Log.d(TAG, "======配置项:${bean?.toString()}=======")
                if (bean?.invitationCodeRequired == 0) {
                    cet_pwd_invite_code_view?.hint = LanguageUtil.getString(mActivity, "invite_code_hint")
                } else {
                    cet_pwd_invite_code_view?.hint = LanguageUtil.getString(mActivity, "register_text_inviteCode")
                }
                cet_pwd_again_view.visibility = View.GONE
                /**
                 * 点击 服务条款
                 */
                tv_terms_service?.setOnClickListener {
                    var bundle = Bundle()
                    bundle.putString(ParamConstant.head_title, LanguageUtil.getString(this, "register_action_agreement"))
                    bundle.putString(ParamConstant.web_url, bean?.url ?: "")

                    ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
                }

            }
            1 -> {
                tv_title?.text = LanguageUtil.getString(mActivity, "login_action_resetPassword")
                tv_reset_password?.text = LanguageUtil.getString(mActivity, "password_reset_tips")
                tv_reset_password?.visibility = View.VISIBLE
                tv_info?.visibility = View.GONE
                cet_pwd_invite_code_view?.visibility = View.GONE
                v_line_pwd?.visibility = View.GONE
                tv_terms_service?.visibility = View.GONE
                cubtn_view?.setContent(LanguageUtil.getString(mActivity, "common_text_btnConfirm"))
            }
        }
        setOnclick()
        removeToken()
    }

    fun setOnclick() {

        iv_cancel?.setOnClickListener { finish() }

        /**
         * 监听第一个密码edittext
         */
        cet_pwd_view?.onTextListener = object : PwdSettingView.OnTextListener {
            override fun onclickImage() {

            }

            override fun returnItem(item: Int) {

            }

            override fun showText(text: String): String {
                pwdContent = text
                if (pwdContent.isNotEmpty() && index == 0) {
                    cubtn_view?.isEnable(true)
                } else {
                    if (pwdContent.isNotEmpty() && pwdAgainContent.isNotEmpty()) {
                        cubtn_view?.isEnable(true)
                    } else {
                        cubtn_view?.isEnable(false)
                    }
                }
                return text
            }

        }


        /**
         * 监听第二个密码edittext
         */
        cet_pwd_again_view?.onTextListener = object : PwdSettingView.OnTextListener {
            override fun onclickImage() {

            }

            override fun returnItem(item: Int) {

            }

            override fun showText(text: String): String {
                pwdAgainContent = text
                if (pwdContent.isNotEmpty() && pwdAgainContent.isNotEmpty()) {
                    cubtn_view?.isEnable(true)
                } else {
                    cubtn_view?.isEnable(false)
                }
                return text
            }

        }


        /**
         *  点击确认登录
         */
        cubtn_view?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                // 0 设置密码 1 重置密码
                // 设置密码的流程无需增加双密码校验
                // 重置密码的流程需要增加双密码校验
                if (index == 0) {
                    if (!StringUtils.checkPass(pwdContent)) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "login_tip_passwordRequire"), isSuc = false)
                        return
                    }
                } else {
                    /**
                     * 判断密码是否相同
                     */
                    if (pwdContent == pwdAgainContent) {
                        if (!StringUtils.checkPass(pwdAgainContent)) {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "login_tip_passwordRequire"), isSuc = false)
                            return
                        }
                    } else {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "login_tip_passwordNotMatch"), isSuc = false)
                        return
                    }
                }
                when (index) {
                    /**
                     * 注册
                     */
                    0 -> {
                        /**
                         * 服务器返回 字段 如果是1 邀请码必填
                         * 0 邀请码选填
                         */
                        if (bean?.invitationCodeRequired == 1) {
                            if (TextUtils.isEmpty(cet_pwd_invite_code_view.text?.trim())) {
                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "common_tip_inputInviteCode"), false)
                                return
                            } else {
                                reg4Step3(account, pwdContent, cet_pwd_invite_code_view.text.toString())
                            }
                        } else {
                            reg4Step3(account, pwdContent, cet_pwd_invite_code_view.text.toString())
                        }
                    }
                    /**
                     * 忘记密码
                     */
                    1 -> {
                        findPwdStep4(token, pwdContent)
                    }
                }
            }
        }


    }

    /**
     * 找回密码 Step 4 实际上的第三步  新版本第三步删除了，只保留了第一步、第二步、第三步
     */
    private fun findPwdStep4(token: String, loginPword: String = "") {
        addDisposable(getMainModel().findPwdStep4(token, loginPword, object : NDisposableObserver(this, true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                closeLoadingDialog()
                ArouterUtil.navigation("/login/NewVersionLoginActivity", null)
                var userInfo = LoginManager.getInstance().loginInfo
                userInfo.loginPwd = loginPword
                LoginManager.getInstance().saveLoginInfo(userInfo)
//                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "重置已完成，将为您自动跳转..."), isSuc = true)
                Toast.makeText(this@NewSetPasswordActivity, LanguageUtil.getString(this@NewSetPasswordActivity, LanguageUtil.getString(this@NewSetPasswordActivity,"set_password_completed_jumping")), Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(mActivity,false, LanguageUtil.getString(this@NewSetPasswordActivity, "account_action_token_expire_tip"))
            }

        }))

    }


    /**
     * 注册Step 3?
     *
     * @param registerCode 注意这里填的是"手机或者邮箱验证码"
     */
    private fun reg4Step3(registerCode: String, loginPwd: String, invitedCode: String = "") {
        addDisposable(getMainModel().reg4Step3(registerCode = registerCode, loginPword = loginPwd, newPassword = loginPwd, invitedCode = invitedCode, consumer = object : NDisposableObserver(this, true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                closeLoadingDialog()

                var json = jsonObject.optJSONObject("data") ?: return
                var token = json.optString("token") ?: ""
                UserDataService.getInstance().saveGesturePass("")
                UserDataService.getInstance().clearToken()
                UserDataService.getInstance().clearLoginState()
                LoginManager.getInstance().saveFingerprint(0)


                UserDataService.getInstance().saveToken(token)
                HttpClient.instance.setToken(token)
                getMainModel().saveUserInfo()
                addDisposable(getMainModel().getUserInfo(MyNDisposableObserver()))
                var quicktoken = json?.optString("quicktoken") ?: ""
                UserDataService.getInstance().saveQuickToken(quicktoken)


                /**
                 * 保存信息
                 */
//                        val loginInfo = LoginManager.getInstance().loginInfo
//                        loginInfo.loginPwd = loginPwd
//                        LoginManager.getInstance().saveLoginInfo(loginInfo)

                //5.0版本新增quicktoken 用于注册完毕后直接登录
//                val quicktoken = jsonObject?.optString("quicktoken") ?: ""
//                UserDataService.getInstance().saveQuickToken(quicktoken)

//                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "common_tip_registerSuccess"), isSuc = true)

                ActivityManager.popAllActFromStack()
                finish()
                KeyBoardUtils.closeKeyBoard(this@NewSetPasswordActivity)
            }

        }))

    }

    inner class MyNDisposableObserver() : NDisposableObserver(this, true) {
        override fun onResponseSuccess(jsonObject: JSONObject) {
            getUserInfo(jsonObject)
        }

    }

    /**
     * 获取用户信息
     */
    private fun getUserInfo(data: JSONObject?) {
        if (data == null) return

        var gesturePwd = data.optString("gesturePwd") ?: ""

        UserDataService.getInstance().saveData(data)

        var focusView = this.currentFocus

        /**
         * 判断 是否设置过手势密码
         */
        if (!TextUtils.isEmpty(gesturePwd) && TextUtils.isEmpty(UserDataService.getInstance().gesturePass)) {
            UserDataService.getInstance().saveGesturePass(gesturePwd)
            SoftKeyboardUtil.hideSoftKeyboard(focusView)
            finish()
            return
        } else if (!TextUtils.isEmpty(gesturePwd) || !TextUtils.isEmpty(UserDataService.getInstance().gesturePass)) {
            SoftKeyboardUtil.hideSoftKeyboard(focusView)
            finish()
            return
        }


        /**
         * 判断是否支持指纹
         */
        if (fingerprintManager.isHardwareDetected) {
            /**
             * 判断是否输入指纹
             */
            if (fingerprintManager.hasEnrolledFingerprints()) {
                if (LoginManager.getInstance().fingerprint == 1) {
                    SoftKeyboardUtil.hideSoftKeyboard(focusView)
                    finish()
                    return
                }

                enter2GUdeGesture(2)
            } else {
                quickLogin()
            }
        } else {
            quickLogin()
        }
    }

    private fun enter2GUdeGesture(type: Int, handPwd: String = "") {
        SoftKeyboardUtil.hideSoftKeyboard(mActivity?.currentFocus)

        var bundle = Bundle()
        bundle.putInt("guidegesturetype", type)
        bundle.putString("guidegesturehandpwd", handPwd)
        ArouterUtil.navigation("/login/guidegesturepwdactivity", bundle)

        finish()
    }

    fun quickLogin() {
        var bundle = Bundle()
        bundle.putInt("SET_TYPE", 0)
        bundle.putBoolean("SET_STATUS", false)
        bundle.putBoolean("SET_LOGINANDSET", true)
        bundle.putString("SET_TOKEN", "")
        ArouterUtil.navigation("/login/gesturespasswordactivity", bundle)
    }

    var disposable: Disposable? = null

    /**
     * 5分钟后删除token并销毁页面
     */
    fun removeToken() {
        Observable.interval(5, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long> {
                    override fun onNext(aLong: Long) {
                        Log.d("====onNext=====", "=====count:===along:$aLong")
                        if (disposable != null && !disposable?.isDisposed!!) {
                            disposable?.dispose()
                        }
                        finish()
                        ActivityManager.popAllActFromStack()
                        ArouterUtil.greenChannel("/login/newversionregisteractivity", null)
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "new_register_time_out"), isSuc = false)
                    }

                    override fun onSubscribe(d: Disposable) {
                        Log.d("=========", "====onSubscribe====")
                        disposable = d
                    }


                    override fun onError(e: Throwable) {
                        Log.d("========", "===onError")

                    }

                    override fun onComplete() {
                        Log.d("========", "===onComplete")

                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }


    fun cancel() {
        if (disposable != null && !disposable?.isDisposed()!!) {
            disposable?.dispose()
        }
    }


}