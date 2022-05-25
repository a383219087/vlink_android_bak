package com.yjkj.chainup.new_version.activity.personalCenter

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.TextViewAddEditTextView
import com.yjkj.chainup.new_version.view.TextViewAndPwdView
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.util.StringUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_set_modify_pwd.*

/**
 * @Author lianshangljl
 * @Date 2019/4/2-11:07 AM
 * @Email buptjinlong@163.com
 * @description  设置或者修改 资金密码  或者登陆密码
 */
@Route(path = RoutePath.SetOrModifyPwdActivity)
class SetOrModifyPwdActivity : NewBaseActivity() {


    @JvmField
    @Autowired(name = ParamConstant.taskType)
    var taskType = ParamConstant.RESET_PWD

    @JvmField
    @Autowired(name = ParamConstant.taskFrom)
    var taskFrom = ParamConstant.FROM_OTC

    var realContent = ""
    var oldPwd = ""
    var newPwd = ""
    var newAgainPwd = ""

    /*companion object {
        //const val SET_PWD = "SET_PWD"
        //const val RESET_PWD = "RESET_PWD"
        //const val FROM_LOGIN = "FROM_LOGIN"
        //const val FROM_OTC = "FROM_OTC"
        //const val TASKTYPE = "TASKTYPE"
        //const val FROM = "FROM"
        */
    /**
     * @param taskType  任务类型  修改或者设置密码
     * @param from 修改登录密码或者修改资金密码
     *
     *//*
        fun enter2(context: Context, taskType: String, from: String) {
            var intent = Intent()
            intent.setClass(context, SetOrModifyPwdActivity::class.java)
            intent.putExtra(TASKTYPE, taskType)
            intent.putExtra(FROM, from)
            context.startActivity(intent)
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArouterUtil.inject(this)
        setContentView(R.layout.activity_set_modify_pwd)

        real_name_layout?.setTitle(LanguageUtil.getString(this, "safety_text_userIdentifier"))
        old_pws?.setTitle(LanguageUtil.getString(this, "safety_text_oldPassword"))
        old_pws?.setEditHint(LanguageUtil.getString(this, "register_tip_inputPassword"))

        new_pws?.setTitle(LanguageUtil.getString(this, "otcSafeAlert_text_otcPwd"))
        new_pws?.setEditHint(LanguageUtil.getString(this, "register_tip_inputPassword"))

        new_again_pws?.setTitle(LanguageUtil.getString(this, "safety_text_confrimPasswod"))
        new_again_pws?.setEditHint(LanguageUtil.getString(this, "register_tip_inputPassword"))
        cub_submit?.setBottomTextContent(LanguageUtil.getString(this, "common_action_next"))
        cub_submit?.isEnable(true)
        initView()
        setOnClick()
    }

    fun setOnClick() {
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout?.slidingShowTitle(status)
            }

        }

        cub_submit?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (newPwd != newAgainPwd) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SetOrModifyPwdActivity, "common_tip_inputsNotMatch"), isSuc = false)

                    return
                }
                if (!StringUtils.checkPass(newPwd)) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SetOrModifyPwdActivity, "common_tip_pwdNotice"), isSuc = false)
                    return
                }
                when (taskFrom) {
                    ParamConstant.FROM_LOGIN -> {
                        dialog = NewDialogUtils.showBindDialog(this@SetOrModifyPwdActivity, AppConstant.CHANGE_PWD, object : NewDialogUtils.DialogVerifiactionListener {
                            override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                                dialog?.dismiss()
                                changeLoginPwd(phone ?: "", oldPwd, newPwd, googleCode
                                        ?: "", realContent)
                            }
                        }, 1)
                    }
                    ParamConstant.FROM_OTC -> {

                        dialog = NewDialogUtils.showBindDialog(this@SetOrModifyPwdActivity, AppConstant.CHANGE_CAPITAL_PWD, object : NewDialogUtils.DialogVerifiactionListener {
                            override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                                dialog?.dismiss()
                                when (taskType) {
                                    ParamConstant.SET_PWD -> {
                                        capitalPassword4OTC(newPwd, phone ?: "", googleCode ?: "")

                                    }
                                    ParamConstant.RESET_PWD -> {
                                        capitalPasswordReset4OTC(newPwd, phone
                                                ?: "", googleCode ?: "")
                                    }
                                }
                            }
                        }, 1)


                    }
                }
            }

        }
    }

    var dialog: TDialog? = null

    fun initView() {
        val title = if (PublicInfoDataService.getInstance().getB2CSwitchOpen(null)) {
            LanguageUtil.getString(this, "safety_text_editOtcPassword_forotc")
        } else {
            LanguageUtil.getString(this, "safety_text_editOtcPassword")
        }

        when (taskFrom) {
            ParamConstant.FROM_LOGIN -> {
                if (taskType == ParamConstant.RESET_PWD) {
                    if (UserDataService.getInstance().authLevel != 1) {
                        real_name_layout?.visibility = View.GONE
                    }
                }
                title_layout?.setContentTitle(LanguageUtil.getString(this, "safety_action_changeLoginPassword"))
                new_pws?.setTitle(LanguageUtil.getString(this, "register_text_loginPwd"))

                new_again_pws?.setTitle(LanguageUtil.getString(this, "common_tip_inputLoginPwd"))
            }
            ParamConstant.FROM_OTC -> {
                real_name_layout?.visibility = View.GONE
                old_pws?.visibility = View.GONE
                when (taskType) {
                    ParamConstant.SET_PWD -> {
                        title_layout?.setContentTitle(LanguageUtil.getString(this, "safety_action_otcPassword"))
                    }
                    ParamConstant.RESET_PWD -> {
                        title_layout?.setContentTitle(title)
                    }
                }

                new_pws?.setTitle(LanguageUtil.getString(this, "otcSafeAlert_text_otcPwd"))
                new_again_pws?.setTitle(LanguageUtil.getString(this, "safety_text_confrimPasswod"))
            }
        }
        real_name_layout?.listener = object : TextViewAddEditTextView.OnTextListener {
            override fun showText(text: String): String {
                realContent = text
                setButtonEnable()
                return text
            }
        }
        old_pws?.listener = object : TextViewAndPwdView.OnTextListener {
            override fun showText(text: String): String {
                oldPwd = text
                setButtonEnable()
                return text
            }
        }
        new_pws?.listener = object : TextViewAndPwdView.OnTextListener {
            override fun showText(text: String): String {
                newPwd = text
                setButtonEnable()
                return text
            }
        }
        new_again_pws?.listener = object : TextViewAndPwdView.OnTextListener {
            override fun showText(text: String): String {
                newAgainPwd = text
                setButtonEnable()
                return text
            }
        }
    }


    fun setButtonEnable() {
        when (taskFrom) {
            ParamConstant.FROM_LOGIN -> {
//                if (UserDataService.getInstance().authLevel != 1) {
//                    if (!TextUtils.isEmpty(oldPwd) && !TextUtils.isEmpty(newPwd) && !TextUtils.isEmpty(newAgainPwd)) {
//                        cub_submit?.isEnable(true)
//                    } else {
//                        cub_submit?.isEnable(false)
//                    }
//                } else {
//                    if (!TextUtils.isEmpty(realContent) && !TextUtils.isEmpty(oldPwd) && !TextUtils.isEmpty(newPwd) && !TextUtils.isEmpty(newAgainPwd)) {
//                        cub_submit?.isEnable(true)
//                    } else {
//                        cub_submit?.isEnable(false)
//                    }
//                }
            }
            ParamConstant.FROM_OTC -> {
//                if (!TextUtils.isEmpty(newPwd) && !TextUtils.isEmpty(newAgainPwd)) {
//                    cub_submit?.isEnable(true)
//                } else {
//                    cub_submit?.isEnable(false)
//                }

            }
        }
    }


    /**
     * 修改登录密码
     */
    fun changeLoginPwd(smsAuthCode: String = "", loginPwd: String, newLoginPwd: String,
                       googleCode: String = "", identificationNumber: String? = "") {
        HttpClient.instance.changeLoginPwd(smsAuthCode = smsAuthCode, loginPwd = loginPwd, newLoginPwd = newLoginPwd, googleCode = googleCode, identificationNumber = identificationNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SetOrModifyPwdActivity, "common_tip_editSuccess"), isSuc = true)

                        /**
                         * 修改登录密码
                         */
                        val loginInfo = LoginManager.getInstance().loginInfo
                        loginInfo.loginPwd = newLoginPwd
                        LoginManager.getInstance().saveLoginInfo(loginInfo)

                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }

                })
    }

    /**
     * 设置 资金密码
     */
    fun capitalPassword4OTC(capitalPwd: String, smsAuthCode: String, googleCode: String) {
        HttpClient.instance.capitalPassword4OTC(capitalPwd, smsAuthCode, googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SetOrModifyPwdActivity, "common_tip_editSuccess"), isSuc = true)

                        var info = UserDataService.getInstance().userData
                        info.put("isCapitalPwordSet", 1)
                        UserDataService.getInstance().saveData(info)
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }

                })
    }


    /**
     * 重置资金密码
     */

    fun capitalPasswordReset4OTC(capitalPwd: String, smsAuthCode: String, googleCode: String) {
        HttpClient.instance.capitalPasswordReset4OTC(capitalPwd, smsAuthCode, googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@SetOrModifyPwdActivity, "common_tip_editSuccess"), isSuc = true)

                        var info = UserDataService.getInstance().userData
                        info.put("isCapitalPwordSet", 1)
                        UserDataService.getInstance().saveData(info)
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }

                })
    }

}
