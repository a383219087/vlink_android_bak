package com.yjkj.chainup.new_version.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.PwdSetView
import com.yjkj.chainup.util.NToastUtil
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_findpwd2verify.*


/**
 * @author Bertking
 * @date 2018-11-19
 * @description 找回密码的step 2 -- 身份认证
 */
@Route(path = "/login/findpwdverifyactivity")
class FindPwd2verifyActivity : NewBaseActivity() {
    var haveID = 0
    var haveGoogle = 0

    var token = ""

    var id_num = ""
    var google_code = ""
    var account_content = ""


    companion object {

        const val TOKEN = "token"
        const val HAVE_ID = "id"
        const val HAVE_GOOGLE = "google"
        const val ACCOUNT_CONTENT = "account_content"
        const val PATH_KEY = "/login/findpwdverifyactivity"

        fun enter2(token: String, haveID: Int, haveGoogle: Int, account: String = "") {
            var bundle = Bundle()
            bundle.putString(TOKEN, token)
            bundle.putInt(HAVE_ID, haveID)
            bundle.putInt(HAVE_GOOGLE, haveGoogle)
            bundle.putString(ACCOUNT_CONTENT, account)
            ArouterUtil.navigation(PATH_KEY, bundle)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findpwd2verify)
        context = this

        token = intent?.getStringExtra(TOKEN) ?: ""
        account_content = intent?.getStringExtra(ACCOUNT_CONTENT) ?: ""
        haveID = intent?.getIntExtra(HAVE_ID, 0) ?: 0
        haveGoogle = intent?.getIntExtra(HAVE_GOOGLE, 0) ?: 0


        psv_id?.visibility = if (haveID == 0) View.GONE else View.VISIBLE
        psv_google?.visibility = if (haveGoogle == 0) View.GONE else View.VISIBLE

        btn_next?.isEnable(false)
        /**
         * 密码
         */
        psv_id?.onTextListener = object : PwdSetView.OnTextListener {
            override fun showText(text: String): String {
                id_num = text
                if (haveGoogle == 1) {
                    if (TextUtils.isEmpty(google_code) || TextUtils.isEmpty(text)) {
                        btn_next?.isEnable(false)
                    } else {
                        btn_next?.isEnable(true)

                        /**
                         * 点击下一步
                         */
                        toNext()
                    }
                } else {
                    if (TextUtils.isEmpty(text)) {
                        btn_next?.isEnable(false)
                    } else {
                        btn_next?.isEnable(true)

                        /**
                         * 点击下一步
                         */
                        toNext()
                    }
                }


                return text
            }

        }

        /**
         * 谷歌
         */
        psv_google.onTextListener = object : PwdSetView.OnTextListener {
            override fun showText(text: String): String {
                google_code = text
                if (haveID == 0) {
                    if (TextUtils.isEmpty(text)) {
                        btn_next?.isEnable(false)
                    } else {
                        btn_next?.isEnable(true)
                    }
                } else {
                    if (TextUtils.isEmpty(id_num) || TextUtils.isEmpty(text)) {
                        btn_next?.isEnable(false)
                    } else {
                        btn_next?.isEnable(true)
                    }
                }

                return text
            }
        }
        /**
         * 点击下一步
         */
        toNext()
        ib_back?.setOnClickListener { finish() }

    }

    private fun toNext() {
        btn_next?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                findPwdStep3(token, psv_id?.text ?: "", psv_google?.text ?: "")
            }
        }
    }


    /**
     * 找回密码 Step 3
     */
    private fun findPwdStep3(token: String, certifcateNumber: String = "", googleCode: String = "") {
        showProgressDialog(LanguageUtil.getString(this,"common_text_refreshing"))
        HttpClient.instance.findPwdStep3(token = token, certifcateNumber = certifcateNumber, googleCode = googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(json: Any?) {
                        cancelProgressDialog()
                        /**
                         * 跳转至"重置密码"界面
                         */
                        val bundle = Bundle()
                        bundle.putString("account_num", account_content)
                        bundle.putInt("index_status", 1)
                        bundle.putString("index_token", token)
                        bundle.putString("index_number_code", "")
                        bundle.putString("param", "")
                        ArouterUtil.navigation("/login/newsetpasswordactivity", bundle)
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        NToastUtil.showTopToastNet(this@FindPwd2verifyActivity,false, msg)
                    }
                })
    }
}
