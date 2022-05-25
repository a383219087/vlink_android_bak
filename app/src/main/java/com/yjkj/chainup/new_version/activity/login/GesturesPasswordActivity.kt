package com.yjkj.chainup.new_version.activity.login

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.jaeger.library.StatusBarUtil
import com.wangnan.library.listener.OnGestureLockListener
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.view.GesturesPasswordTool
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.activity_gestures_password.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/3/6-3:01 PM
 * @Email buptjinlong@163.com
 * @description 手势密码
 */
@Route(path = "/login/gesturespasswordactivity")
class GesturesPasswordActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_gestures_password
    }

    var setType = 0
    var token = ""
    var status = false
    var loginAndSet = false
    var account = ""
    var pwd = ""
    var setGesturesFrist = true
    var pwdForGestures = ""


    companion object {
        const val SET_TYPE = "SET_TYPE"
        const val SET_TOKEN = "SET_TOKEN"
        const val SET_STATUS = "SET_STATUS"
        const val SET_LOGINANDSET = "SET_LOGINANDSET"
    }


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        setTextContext()
        initView()
        loadData()
    }

    fun setTextContext() {
        tv_gestures_title?.text = LanguageUtil.getString(this, "safety_text_gesturePassword")
        tv_gestures_title_second?.text = LanguageUtil.getString(this, "safety_action_setGesturePassword")
        text_next?.text = LanguageUtil.getString(this, "safety_action_faceIdNextTime")
    }

    fun getData() {
        if (intent != null) {
            setType = intent.getIntExtra(SET_TYPE, 0)
            token = intent.getStringExtra(SET_TOKEN) ?: ""
            status = intent.getBooleanExtra(SET_STATUS, false)
            loginAndSet = intent.getBooleanExtra(SET_LOGINANDSET, false)
        }
    }

    fun setOnClick() {
        text_next?.setOnClickListener {
            when (setType) {
                1 -> {
                    ArouterUtil.navigation("/login/NewVersionLoginActivity", null)
                }
            }
            finish()
        }
        tv_cancel?.setOnClickListener { finish() }
    }

    override fun loadData() {

    }

    override fun initView() {
        StatusBarUtil.setColor(this, ColorUtil.getColorByMode(R.color.bg_card_color_day), 0)
        getData()
        var userinfo = UserDataService.getInstance().userData
        var mobileNumber = userinfo.optString("mobileNumber") ?: ""
        var email = userinfo.optString("email") ?: ""

        if (setType == 1) {
            tv_gestures_title_second?.visibility = View.GONE
            if (mobileNumber.isNotEmpty()) {
                tv_gestures_title?.text = mobileNumber
            } else {
                tv_gestures_title?.text = email
            }

            tv_gestures_title_second?.visibility = View.INVISIBLE
            text_next?.text = LanguageUtil.getString(mActivity, "login_action_otherAccount")
        } else {
            iv_head?.visibility = View.GONE
        }

        if (status) {
            text_next?.text = LanguageUtil.getString(this, "login_action_otherAccount")
        } else {
            text_next?.text = LanguageUtil.getString(this, "safety_action_faceIdNextTime")
        }

        geetest_view?.setPainter(GesturesPasswordTool())
        geetest_view?.setGestureLockListener(object : OnGestureLockListener {
            /**
             * 图案解锁完成
             *
             * @param result 解锁结果（数字字符串）
             */
            override fun onComplete(result: String?) {
                if (result?.length ?: 0 < 5) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "common_tip_gestureLimitPoint"), false)
                    geetest_view?.showErrorStatus(400)
                    return
                }

                var pwdResult = ""
                result?.forEach {
                    var int = BigDecimalUtils.add(it.toString(), "1").toString()
                    pwdResult += int
                }

                /**
                 * 请求服务器开启手势密码
                 */
                if (status) {
                    handPwdLogin(pwdResult.trim { it <= ' ' })
                } else {
                    if (setGesturesFrist) {
                        setGesturesFrist = false
                        pwdForGestures = pwdResult.trim { it <= ' ' }
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "safety_action_confrimGesturePassword"), true)
                        tv_gestures_title_second?.text = LanguageUtil.getString(mActivity, "safety_action_confrimGesturePassword")
                        geetest_view?.clearView()
                    } else {
                        if (pwdForGestures == pwdResult.trim { it <= ' ' }) {
                            if (loginAndSet) {
                                addDisposable(getMainModel().newOpenHand(UserDataService.getInstance().quickToken, pwdResult.trim { it <= ' ' }, object : NDisposableObserver(mActivity, true) {
                                    override fun onResponseSuccess(jsonObject: JSONObject) {
                                        UserDataService.getInstance().saveGesturePass(pwdResult.trim(predicate = { it <= ' ' }))

                                        var json = UserDataService.getInstance().userData
                                        json.put("gesturePwd", pwdResult.trim { it <= ' ' })
                                        UserDataService.getInstance().saveData(json)
                                        this@GesturesPasswordActivity.finish()

                                    }

                                    override fun onResponseFailure(code: Int, msg: String?) {
                                        super.onResponseFailure(code, msg)
                                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                                        geetest_view?.showErrorStatus(400)
                                    }
                                }))
                            } else {
                                addDisposable(getMainModel().setHandPwd(token, pwdResult.trim { it <= ' ' }, object : NDisposableObserver() {
                                    override fun onResponseSuccess(jsonObject: JSONObject) {
                                        UserDataService.getInstance().saveGesturePass(pwdResult.trim { it <= ' ' })

                                        var json = UserDataService.getInstance().userData
                                        json.put("gesturePwd", pwdResult.trim { it <= ' ' })
                                        UserDataService.getInstance().saveData(json)

                                        this@GesturesPasswordActivity.finish()
                                    }

                                    override fun onResponseFailure(code: Int, msg: String?) {
                                        super.onResponseFailure(code, msg)
                                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                                        geetest_view?.showErrorStatus(400)
                                    }
                                }))
                            }


                        } else {
                            setGesturesFrist = true
                            pwdForGestures = ""
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "login_tip_gestureNotMatch"), isSuc = false)
                            tv_gestures_title_second?.text = LanguageUtil.getString(mActivity, "safety_action_setGesturePassword")
                            geetest_view?.showErrorStatus(400)
                        }
                    }
                }
            }

            /**
             * 监听视图解锁开始（手指按下）
             */
            override fun onStarted() {

            }

            /**
             * 图案解锁内容改变
             *
             * @param progress 解锁进度（数字字符串）
             */
            override fun onProgress(progress: String?) {

            }

        })
        setOnClick()
    }

    /**
     * 手势密码登录
     */
    private fun handPwdLogin(handPwd: String) {
        if (TextUtils.isEmpty(handPwd)) {
            return
        }
        var quickToken = UserDataService.getInstance().quickToken

        addDisposable(getMainModel().newHandLogin(quickToken, handPwd, object : NDisposableObserver(this, true) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                val token = json?.optString("token")
                UserDataService.getInstance().saveToken(token)
                HttpClient.instance.setToken(token)
                getMainModel().saveUserInfo()
                ArouterUtil.refreshWebview()
                finish()
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(mActivity, false, msg)
                if (code == ParamConstant.QUICK_LOGIN_FAILURE) {
                    UserDataService.getInstance().saveQuickToken("")
                    ArouterUtil.navigation("/login/NewVersionLoginActivity", null)
                    finish()
                }
                finish()
                geetest_view?.showErrorStatus(400)
            }

        }))

    }

}