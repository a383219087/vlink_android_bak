package com.yjkj.chainup.new_version.activity.personalCenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.yjkj.chainup.util.JsonUtils
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.bean.CountryInfo
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.SelectAreaActivity
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.PwdSettingView
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.StringUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_bind_mobile_or_email.*
import kotlinx.android.synthetic.main.activity_bind_mobile_or_email.cub_submit
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * @Author lianshangljl
 * @Date 2019/4/1-10:54 AM
 * @Email buptjinlong@163.com
 * @description  绑定手机号 or 修改手机号
 */
class BindMobileOrEmailActivity : NewBaseActivity() {

    var bindType = 0
    var validationType = "VALIDATION_BIND"

    /**
     * 账号
     */
    var accountNumber = ""
    var country = ""
    var areaCode = ""


    companion object {
        /**
         * 手机号
         */
        const val MOBILE_TYPE = 0

        /**
         * 邮箱
         */
        const val MAIL_TYPE = 1
        const val VERIFY_TYPE = "VERIFY_TYPE"
        const val BIND_OR_CHANGE = "BIND_OR_CHANGE"
        const val VALIDATION_BIND = "VALIDATION_BIND"
        const val VALIDATION_CHANGE = "VALIDATION_CHANGE"

        /**
         *  绑定账号或者修改
         * @param type 端 0 手机 1 邮箱
         * @param validationType 手机或者邮箱
         */
        fun enter2(context: Context, type: Int, validationType: String) {
            var intent = Intent()
            intent.setClass(context, BindMobileOrEmailActivity::class.java)
            intent.putExtra(VERIFY_TYPE, type)
            intent.putExtra(BIND_OR_CHANGE, validationType)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind_mobile_or_email)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        getData()

        var getMessage = PublicInfoDataService.getInstance().getUserRegType(null)
        try {
            var json = JSONObject(getMessage)
            if (json.length() > 0) {
                var current = json.optJSONArray(JsonUtils.getLanguage())
                if (current != null && current.length() == 1 && current.get(0).toString() == "2" && UserDataService.getInstance().googleStatus != 1 && bindType == MOBILE_TYPE) {
                    NewDialogUtils.showNormalDialog(this, getString(R.string.common_text_bindPhonePrompt), object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            startActivity(Intent(this@BindMobileOrEmailActivity, GoogleValidationActivity::class.java))
                            finish()
                        }
                    }, "", getString(R.string.otcSafeAlert_action_bindGoogle), getString(R.string.common_text_stillBindPhone))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        initView()
        setOnclick()
        cub_submit?.setBottomTextContent(LanguageUtil.getString(this, "common_action_next"))
    }

    fun setOnclick() {
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout.slidingShowTitle(status)
            }

        }

        cub_submit?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                accountNumber = cet_view?.text.toString()
                var type = 0
                when (validationType) {
                    VALIDATION_BIND -> {
                        when (bindType) {
                            MOBILE_TYPE -> {
                                type = AppConstant.BIND_MOBILE
                            }
                            MAIL_TYPE -> {
                                type = AppConstant.BIND_EMAIL

                            }
                        }
                    }
                    VALIDATION_CHANGE -> {
                        when (bindType) {
                            MOBILE_TYPE -> {
                                type = AppConstant.CHANGE_MOBILE
                            }
                            MAIL_TYPE -> {
                                type = AppConstant.CHANGE_EMAIL
                            }
                        }
                    }
                }

                when (bindType) {
                    MOBILE_TYPE -> {

                        if (!StringUtils.isNumeric(accountNumber) || accountNumber.length < 5) {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@BindMobileOrEmailActivity, "userinfo_tip_inputPhone"), isSuc = false)
                            return
                        } else {
                            if (validationType == VALIDATION_BIND) {
                                dialog = NewDialogUtils.showBindPhoneDialog(this@BindMobileOrEmailActivity, type, object : NewDialogUtils.DialogVerifiactionListener {
                                    override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                                        dialog?.dismiss()
                                        setBind(phone, mail, googleCode)
                                    }
                                }, accountNumber, country)
                            } else {
                                dialog = NewDialogUtils.showChangePhoneDialog(this@BindMobileOrEmailActivity, type, object : NewDialogUtils.DialogVerifiactionListener {
                                    override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                                        dialog?.dismiss()
                                        setBind(phone, mail, googleCode)
                                    }
                                }, accountNumber, country)
                            }

                        }
                    }
                    MAIL_TYPE -> {
                        if (!StringUtils.checkEmail(accountNumber) || accountNumber.length < 6) {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@BindMobileOrEmailActivity, "safety_tip_inputMail"), isSuc = false)
                            return
                        } else {
                            if (validationType == VALIDATION_BIND) {
                                dialog = NewDialogUtils.showAccountBindDialog(this@BindMobileOrEmailActivity, accountNumber, -1, type, object : NewDialogUtils.DialogVerifiactionListener {
                                    override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                                        dialog?.dismiss()
                                        setBind(phone, mail, googleCode)
                                    }
                                })
                            } else {
                                dialog = NewDialogUtils.showBindEmailDialog(this@BindMobileOrEmailActivity, type, object : NewDialogUtils.DialogReturnChangeEmail {
                                    override fun returnCode(phone: String?, oldEmail: String?, newEmail: String?, googleCode: String?) {
                                        dialog?.dismiss()
                                        setBind(phone, oldEmail, googleCode, newEmail ?: "")
                                    }

                                }, accountNumber)
                            }

                        }
                    }
                }
            }

        }

        pws_view.onTextListener = object : PwdSettingView.OnTextListener {
            override fun returnItem(item: Int) {

            }

            override fun onclickImage() {
                startActivity(Intent(this@BindMobileOrEmailActivity, SelectAreaActivity::class.java))
            }

            override fun showText(text: String): String {

                return text
            }
        }
    }

    fun setBind(phone: String?, mail: String?, googleCode: String?, newEmail: String = "") {
        when (validationType) {
            VALIDATION_BIND -> {
                when (bindType) {
                    MOBILE_TYPE -> {
                        bindMobile(country, accountNumber, phone ?: "", googleCode
                                ?: "")
                    }
                    MAIL_TYPE -> {
                        bindEmail(accountNumber, mail ?: "", phone ?: "", googleCode
                                ?: "")
                    }
                }
            }
            VALIDATION_CHANGE -> {
                when (bindType) {
                    MOBILE_TYPE -> {
                        changeMobile(phone
                                ?: "", mail
                                ?: "", country, accountNumber, googleCode
                                ?: "")
                    }
                    MAIL_TYPE -> {
                        changeEmail(mail ?: "", accountNumber, newEmail
                                , phone ?: "", googleCode
                                ?: "")
                    }
                }
            }
        }
    }


    var dialog: TDialog? = null
    fun getData() {
        if (intent != null) {
            bindType = intent.getIntExtra(VERIFY_TYPE, MOBILE_TYPE)
            validationType = intent.getStringExtra(BIND_OR_CHANGE) ?: "VALIDATION_BIND"
        }
    }

    fun initView() {
        handleData()
        cet_view.isFocusable = true
        cet_view.isFocusableInTouchMode = true
        cub_submit.isEnable(false)
        cet_view.setOnFocusChangeListener { v, hasFocus ->
            cet_view_line.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        when (validationType) {
            VALIDATION_BIND -> {
                when (bindType) {
                    MOBILE_TYPE -> {
                        pws_view.visibility = View.VISIBLE
                        title_layout.setContentTitle(LanguageUtil.getString(this, "otcSafeAlert_action_bindphone"))
                        tv_account_title.text = LanguageUtil.getString(this, "personal_text_phoneNumber")
                        cet_view.hint = LanguageUtil.getString(this, "personal_text_phoneNumber")
                    }
                    MAIL_TYPE -> {
                        pws_view.visibility = View.GONE
                        title_layout.setContentTitle(LanguageUtil.getString(this, "safety_text_bindMail"))
                        cet_view.hint = LanguageUtil.getString(this, "noun_account_accountName")
                        tv_account_title.text = LanguageUtil.getString(this, "noun_account_accountName")
                    }
                }
            }
            VALIDATION_CHANGE -> {
                when (bindType) {
                    MOBILE_TYPE -> {
                        pws_view.visibility = View.VISIBLE
                        title_layout.setContentTitle(LanguageUtil.getString(this, "safety_action_editPhone"))
                        tv_account_title.text = LanguageUtil.getString(this, "personal_text_phoneNumber")
                        cet_view.hint = LanguageUtil.getString(this, "personal_text_phoneNumber")
                    }
                    MAIL_TYPE -> {
                        pws_view.visibility = View.GONE
                        title_layout.setContentTitle(LanguageUtil.getString(this, "safety_action_editMail"))
                        tv_account_title.text = LanguageUtil.getString(this, "noun_account_accountName")
                        cet_view.hint = LanguageUtil.getString(this, "noun_account_accountName")
                    }
                }
            }
        }

        /**
         * 监听
         */
        cet_view.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                accountNumber = s.toString()
                if (TextUtils.isEmpty(accountNumber)) {
                    cub_submit.isEnable(false)
                } else {
                    cub_submit.isEnable(true)
                }
            }
        })
    }

    private fun handleData() {
        var data = JsonUtils.getAreaData(this)
        var allCountry = arrayListOf<CountryInfo>()
        var limtCountry = PublicInfoDataService.getInstance().getLimitCountryList(null)
        if (data.get("countryList").isJsonArray) {
            val countryData = JsonUtils.jsonToList(data.get("countryList").toString(), CountryInfo::class.java)
            Log.d(TAG, "-----正常的----" + countryData.size)
            countryData.forEach {
                if (!TextUtils.isEmpty(it.dialingCode)) {
                    allCountry.add(it)
                }
            }
            for (bean in limtCountry) {
                for (country in allCountry) {
                    if (country.numberCode == bean) {
                        allCountry.remove(country)
                        break
                    }
                }
            }

            var defaultCode = PublicInfoDataService.getInstance().getDefaultCountryCodeReal(null);
            if (TextUtils.isEmpty(defaultCode)) {
                selectCountry(allCountry)
            } else {
                allCountry.forEach {
                    if (it.numberCode == defaultCode) {
                        setPwsView(it)
                        return
                    }
                }
                selectCountry(allCountry)
            }
        }
    }

    fun selectCountry(allCountry: ArrayList<CountryInfo>) {
        allCountry.forEach {
            if (it.dialingCode == PublicInfoDataService.getInstance().getDefaultCountryCode(null)) {
                setPwsView(it)
                return@forEach
            }
        }
    }

    fun setPwsView(it: CountryInfo) {
        if (Locale.getDefault().language.contentEquals("zh")) {
            pws_view?.setEditText(it.cnName + " " + it.dialingCode)
        } else {
            pws_view?.setEditText(it.enName + " " + it.dialingCode)
        }
        country = it.dialingCode
        areaCode = it.numberCode
    }

    /**
     * 绑定邮箱
     */
    private fun bindEmail(email: String, emailCode: String, smsCode: String, googleCode: String) {
        HttpClient.instance.bindEmail(email, emailCode, smsCode, googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@BindMobileOrEmailActivity, "toast_bind_email_suc"), isSuc = true)

                        val userInfoData = UserDataService.getInstance().userData
                        userInfoData.put("email", email)
                        UserDataService.getInstance().saveData(userInfoData)
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)


                    }
                })
    }


    /**
     * 绑定手机
     */
    private fun bindMobile(country: String, mobile: String, smsCode: String, googleCode: String) {
        HttpClient.instance.bindMobile(country, mobile, smsCode, googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@BindMobileOrEmailActivity, "toast_bind_mobile_suc"), isSuc = true)

                        val userInfoData = UserDataService.getInstance().userData
                        userInfoData.put("isOpenMobileCheck", 1)
                        userInfoData.put("mobileNumber", mobile)
                        UserDataService.getInstance().saveData(userInfoData)
                        finish()

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.d(TAG, "===bindMobile:=err=" + msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }

                })
    }


    /**
     * 修改手机号
     */
    fun changeMobile(newSmsCode: String, originalSmsCode: String, country: String, newMobile: String = "", googleCode: String = "") {
        HttpClient.instance.changeMobile(newSmsCode = newSmsCode, originalSmsCode = originalSmsCode, country = country, newMobile = newMobile, googleCode = googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@BindMobileOrEmailActivity, "common_tip_editSuccess"), isSuc = true)


                        val userInfoData = UserDataService.getInstance().userData
                        userInfoData.put("mobileNumber", StringUtils.hideMoible(newMobile))
                        UserDataService.getInstance().saveData(userInfoData)

                        /**
                         * 保存登录信息
                         */
                        val loginInfo = LoginManager.getInstance().loginInfo
                        loginInfo.account = newMobile
                        LoginManager.getInstance().saveLoginInfo(loginInfo)
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.d(TAG, "===unbindMobileVerify:=err=" + msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }
                })
    }


    /**
     * 修改邮箱
     *
     * @oldEmailCode 原邮箱的验证码
     */
    fun changeEmail(oldEmailCode: String, newEmail: String, newEmailCode: String, smsCode: String = "", googleCode: String = "") {
        HttpClient.instance.changeEmail(oldEmailCode = oldEmailCode, newEmail = newEmail, newEmailCode = newEmailCode, smsCode = smsCode, googleCode = googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@BindMobileOrEmailActivity, "common_tip_editSuccess"), isSuc = true)

                        val userInfoData = UserDataService.getInstance().userData
                        userInfoData.put("email", newEmail)
                        UserDataService.getInstance().saveData(userInfoData)

                        /**
                         * 保存登录信息
                         */
                        val loginInfo = LoginManager.getInstance().loginInfo
                        loginInfo.account = newEmail
                        LoginManager.getInstance().saveLoginInfo(loginInfo)
                        finish()

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.d(TAG, "===unbindMobileVerify:=err=" + msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }
                })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent4Area(area: CountryInfo) {
        country = area.dialingCode
        var countryConten = ""
        val language = Locale.getDefault().language
        if (language.equals("zh")) {
            countryConten = area.cnName + " " + area.dialingCode
        } else {
            countryConten = area.enName + " " + area.dialingCode
        }
        pws_view.setEditText(countryConten)
        Log.d(TAG, "==========area:====" + area.toString())
    }

}