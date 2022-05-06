package com.yjkj.chainup.new_version.activity.personalCenter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import com.alibaba.android.arouter.facade.annotation.Route
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_verify_mobile_mail_google.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/3/31-6:23 PM
 * @Email buptjinlong@163.com
 * @description  验证页面
 */
@Route(path = RoutePath.NewVerifyActivity)
class NewVerifyActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_verify_mobile_mail_google
    }

    var type = 0

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }


    override fun initView() {
        getData()
        getDealerInfo()
        setOnClick()
        title_layout?.setContentTitle(LanguageUtil.getString(this,"safety_text_phoneAuth"))
        aiv_account?.setTitle(LanguageUtil.getString(this,"filter_action_reset"))
    }

    fun setViewSelect(view: View, status: Boolean) {
        if (status) {
            view.setBackgroundResource(R.drawable.open)
        } else {
            view.setBackgroundResource(R.drawable.shut_down)
        }
        (view as Switch).isChecked = status
    }

    fun setOnClick() {
        /**
         * 开启或者关闭验证
         */
        switch_gesture_pwd?.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

                if (type == ParamConstant.MOBILE_TYPE && UserDataService.getInstance().googleStatus != 1 && !isChecked) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewVerifyActivity,"personal_tip_pleaseOpenGoogleFirst"), isSuc = false)
                    switch_gesture_pwd?.isChecked = !isChecked
                    return
                }

                Log.d(TAG, "====hhhh=======" + isChecked)
                if (!buttonView!!.isPressed) return

                if (isChecked) {
                    when (type) {
                        ParamConstant.GOOGLE_TYPE -> {
                            startActivity(Intent(this@NewVerifyActivity, GoogleValidationActivity::class.java))
                            finish()
                        }
                        ParamConstant.MOBILE_TYPE -> {
                            openMobileVerify()
                            setViewSelect(switch_gesture_pwd ?: return, isChecked)
                        }
                    }

                } else {
                    when (type) {
                        ParamConstant.GOOGLE_TYPE -> {
                            setViewSelect(switch_gesture_pwd ?: return, !isChecked)
                            dialog = NewDialogUtils.showBindDialog(this@NewVerifyActivity, AppConstant.CLOASE_GOOGLE_VERIFY, object : NewDialogUtils.DialogVerifiactionListener {
                                override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                                    dialog?.dismiss()
                                    unbindGoogleVerify(phone ?: "", googleCode ?: "")
                                }

                            }, 1)
                        }
                        ParamConstant.MOBILE_TYPE -> {
                            dialog = NewDialogUtils.showBindPhoneCodeDialog(this@NewVerifyActivity, AppConstant.CLOSE_MOBILE_VERIFY, object : NewDialogUtils.DialogVerifiactionListener {
                                override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                                    dialog?.dismiss()
                                    unbindMobileVerify(phone ?: "", googleCode ?: "")
                                }

                            })
                        }

                    }
                }
            }
        })
    }

    var dialog: TDialog? = null

    /**
     * 根据不同状态
     */
    fun initVerifyView(t: JSONObject) {
        Log.e("bean", "json$t")
        when (type) {
            ParamConstant.GOOGLE_TYPE -> {
                rl_phone_layout?.visibility = View.VISIBLE
                title_layout?.setContentTitle(LanguageUtil.getString(this,"safety_text_googleAuth"))
                tv_title_name?.text = LanguageUtil.getString(this,"common_action_activeGoogle")
                aiv_account?.visibility = View.GONE
                var bean = t.optInt("googleStatus")
                Log.e("bean", "bean$type")
                switch_gesture_pwd?.isChecked = t.optInt("googleStatus") != 0
                setViewSelect(switch_gesture_pwd ?: return, t.optInt("googleStatus") != 0)
            }
            ParamConstant.MOBILE_TYPE -> {
                rl_phone_layout?.visibility = View.VISIBLE
                tv_title_name?.text = LanguageUtil.getString(this,"safety_text_phoneAuth")
                title_layout?.setContentTitle(LanguageUtil.getString(this,"safety_text_phoneAuth"))
                aiv_account?.setTitle(t.optString("mobileNumber"))
                aiv_account?.setStatusText(LanguageUtil.getString(this,"common_action_edit"))
                aiv_account?.setOnClickListener {
                    if (!Utils.isFastClick()){
                        BindMobileOrEmailActivity.enter2(this, BindMobileOrEmailActivity.MOBILE_TYPE, BindMobileOrEmailActivity.VALIDATION_CHANGE)
                        finish()
                    }
                }
                switch_gesture_pwd?.isChecked = t.optInt("isOpenMobileCheck") != 0
                setViewSelect(switch_gesture_pwd ?: return, t.optInt("isOpenMobileCheck") != 0)
            }
            ParamConstant.MAIL_TYPE -> {
                rl_phone_layout?.visibility = View.GONE
                title_layout?.setContentTitle(LanguageUtil.getString(this,"safety_text_mailAuth"))
                tv_title_name?.text = LanguageUtil.getString(this,"safety_text_mailAuth")
                aiv_account?.setTitle(t.optString("email"))
                aiv_account?.setStatusText(LanguageUtil.getString(this,"common_action_edit"))
                aiv_account?.setOnClickListener {
                    if (!Utils.isFastClick()){
                        BindMobileOrEmailActivity.enter2(this, BindMobileOrEmailActivity.MAIL_TYPE, BindMobileOrEmailActivity.VALIDATION_CHANGE)
                        finish()
                    }

                }
            }
        }
    }

    fun getData() {
        if (intent != null) {
            type = intent.getIntExtra(ParamConstant.VERIFY_TYPE, ParamConstant.GOOGLE_TYPE)
        }
    }


    /**
     * 开启手机验证
     */
    fun openMobileVerify() {
        HttpClient.instance.openMobileVerify()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {

                        switch_gesture_pwd?.isChecked = true
                        setViewSelect(switch_gesture_pwd ?: return, true)

                        var json = UserDataService.getInstance().userData
                        json.put("isOpenMobileCheck", 1)
                        UserDataService.getInstance().saveData(json)

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.d(TAG, "===unbindMobileVerify:=err=" + msg)
                        switch_gesture_pwd?.isChecked = false
                        setViewSelect(switch_gesture_pwd ?: return, false)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }
                })
    }

    /**
     * 解除手机验证
     * @param smsValidCode 验证码
     * @param googleCode Google验证码
     */
    fun unbindMobileVerify(smsValidCode: String, googleCode: String) {
        HttpClient.instance.unbindMobileVerify(smsValidCode, googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewVerifyActivity,"toast_close_verify_suc"), isSuc = true)

                        val userInfoData = UserDataService.getInstance().userData
                        userInfoData.put("isOpenMobileCheck", 0)
                        UserDataService.getInstance().saveData(userInfoData)



                        switch_gesture_pwd?.isChecked = false
                        setViewSelect(switch_gesture_pwd ?: return, false)

                        finish()

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.d(TAG, "===unbindMobileVerify:=err=" + msg)
                        switch_gesture_pwd?.isChecked = true
                        setViewSelect(switch_gesture_pwd ?: return, true)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }
                })
    }

    var userInfoData: JSONObject = JSONObject()
    /**
     * 获取用户信息
     */
    fun getDealerInfo() {
        showLoadingDialog()

        addDisposable(getMainModel().getUserInfo(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                closeLoadingDialog()

                userInfoData = jsonObject.optJSONObject("data")
                UserDataService.getInstance().saveData(userInfoData)
                initVerifyView(userInfoData)
            }

        }))
    }

    /**
     * 解除Google绑定
     * @param smsValidCode 验证码
     * @param googleCode Google验证码
     */
    fun unbindGoogleVerify(smsValidCode: String, googleCode: String) {
        HttpClient.instance.unbindGoogleVerify(smsValidCode, googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@NewVerifyActivity,"toast_close_verify_suc"), isSuc = true)
                        setViewSelect(switch_gesture_pwd ?: return, false)

                        var json = UserDataService.getInstance().userData
                        json.put("googleStatus", 0)
                        UserDataService.getInstance().saveData(json)

                        finish()

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                        Log.d(TAG, "===unbindGoogleVerify:=err=" + msg)
                    }

                })
    }

}