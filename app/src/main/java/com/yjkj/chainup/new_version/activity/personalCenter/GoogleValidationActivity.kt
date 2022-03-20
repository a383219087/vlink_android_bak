package com.yjkj.chainup.new_version.activity.personalCenter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.google.gson.JsonObject
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.view.ComVerifyView
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.PwdSettingView
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.SystemUtils
import com.yjkj.chainup.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_google_validation.*
import org.json.JSONObject


/**
 * @Author lianshangljl
 * @Date 2019/4/24-9:06 AM
 * @Email buptjinlong@163.com
 * @description 绑定谷歌验证
 */
class GoogleValidationActivity : NewBaseActivity() {
    var googleKey: String = ""

    var pwdLogin = ""
    var googlePwd = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_validation)
        setOnclick()
        getGoogleKey()
        title_layout?.setContentTitle(LanguageUtil.getString(this,"safety_action_googleBind"))
        cub_next?.setBottomTextContent(LanguageUtil.getString(this,"common_action_next"))
        cub_download?.setBottomTextContent(LanguageUtil.getString(this,"safety_action_downloadgoogle"))
        tv_one_title?.text = LanguageUtil.getString(this,"register_text_googleAuth")
        tv_two_title?.text = LanguageUtil.getString(this,"safety_explain_googleauthStepOne")
        tv_three_title?.text = LanguageUtil.getString(this,"safety_explain_googleauthStepTwo")
        tv_login_pwd_title?.text = LanguageUtil.getString(this,"register_text_loginPwd")
        tv_google_title?.text = LanguageUtil.getString(this,"safety_text_googleAuth")
        tv_copy?.text = LanguageUtil.getString(this,"common_action_copy")
        pwd_login_pwd?.setHintEditText(LanguageUtil.getString(this,"register_text_loginPwd"))
    }

    fun setOnclick() {
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout?.slidingShowTitle(status)
            }

        }


        tv_copy?.setOnClickListener {
            Utils.copyString(googleKey)
            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this,"common_tip_copySuccess"), isSuc = true)

        }

        /**
         * 点击下载
         */
        cub_download?.isEnable(true)
        cub_download?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                    val uri = Uri.parse("https://saas-oss.oss-cn-hongkong.aliyuncs.com/upload/20201130171506680.apk")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
            }
        }

        pwd_login_pwd?.onTextListener = object : PwdSettingView.OnTextListener {
            override fun showText(text: String): String {
                pwdLogin = text
                if (googlePwd.isNotEmpty() && pwdLogin.isNotEmpty()) {
                    cub_next?.isEnable(true)
                } else {
                    cub_next?.isEnable(false)
                }
                return text
            }

            override fun returnItem(item: Int) {

            }

            override fun onclickImage() {

            }
        }
        cet_view?.onTextListener = object : ComVerifyView.OnTextListener {
            override fun showText(text: String): String {
                googlePwd = text
                if (googlePwd.isNotEmpty() && pwdLogin.isNotEmpty()) {
                    cub_next?.isEnable(true)
                } else {
                    cub_next?.isEnable(false)
                }
                return text
            }

        }


        cub_next?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                val googleVerifyCode = cet_view.getCodeNum()
                val loginPwd = pwdLogin

                if (TextUtils.isEmpty(googleVerifyCode)) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@GoogleValidationActivity,"toast_no_google_code"), isSuc = false)

                    return
                }

                if (TextUtils.isEmpty(loginPwd)) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@GoogleValidationActivity,"hint_input_login_pass"), isSuc = false)

                    return
                }
                bindGoogleVerify(googleKey, loginPwd, googleVerifyCode)
            }
        }

    }

    fun initView() {

    }

    /**
     * 获取GoogleKey
     */
    private fun getGoogleKey() {
        HttpClient.instance.getGoogleKey()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(t: JsonObject?) {
                        Log.d(TAG, "=====GoogleKey======" + t.toString())
                        val jsonObject = JSONObject(t.toString())
                        if (!jsonObject.has("googleImg")) return
                        val googleImgString = jsonObject.get("googleImg").toString()
                        if (TextUtils.isEmpty(googleImgString) || !googleImgString.contains(",")) return

                        val split = googleImgString.split(",")
                        val googleImg = split[1]
                        Log.d(TAG, "=====GoogleImg:====" + googleImg)



                        googleKey = jsonObject.get("googleKey").toString()
                        tv_google_code?.text = googleKey

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }

                })
    }

    /**
     * 开启Google认证
     *  {"code":"0","msg":"成功","data":null}
     */
    private fun bindGoogleVerify(googleKey: String, loginPwd: String, googleCode: String) {
        HttpClient.instance.bindGoogleVerify(googleKey, loginPwd, googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@GoogleValidationActivity,"common_tip_bindSuccess"), isSuc = true)

                        var json = UserDataService.getInstance().userData
                        json.put("googleStatus", 1)
                        UserDataService.getInstance().saveData(json)
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.d(TAG, "===err:==" + msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                    }
                })
    }

}