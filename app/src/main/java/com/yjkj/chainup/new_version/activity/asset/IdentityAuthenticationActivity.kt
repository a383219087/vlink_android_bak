package com.yjkj.chainup.new_version.activity.asset

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.TextViewAddEditTextView
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.activity_identity_authentication_layout.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020-03-18-14:21
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.IdentityAuthenticationActivity)
class IdentityAuthenticationActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_identity_authentication_layout


    /**
     * 是否认证成功
     */
    @JvmField
    @Autowired(name = ParamConstant.WITHDRAW_ID)
    var withdrawId = ""

    var idNumber = ""
    var realName = ""
    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
        ArouterUtil.inject(this)
        getIdentityAuthInfo()
        collapsing_toolbar?.title = LanguageUtil.getString(this,"common_text_identify")
        initView()
        setTextContent()
    }

    override fun initView() {
        btn_confirm?.isEnable(false)
        tet_name_authentication?.listener = object : TextViewAddEditTextView.OnTextListener {
            override fun showText(text: String): String {
                realName = text
                setSubmitStyle()
                return text
            }
        }
        tet_id_number_authentication?.listener = object : TextViewAddEditTextView.OnTextListener {
            override fun showText(text: String): String {
                idNumber = text
                setSubmitStyle()
                return text
            }
        }
        btn_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {

                if (TextUtils.isEmpty(realName)) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_realnameVerifyPlaceholder"), isSuc = false)
                    return
                }
                if (TextUtils.isEmpty(idNumber)) {
                    DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_realidVerifyPlaceholder"), isSuc = false)
                    return
                }
                submitAuthInfoCheck()
            }
        }
    }

    fun setSubmitStyle() {
        if (!TextUtils.isEmpty(realName) && !TextUtils.isEmpty(idNumber)) {
            btn_confirm?.isEnable(true)
        } else {
            btn_confirm?.isEnable(false)
        }
    }


    fun setTextContent() {
        tv_top_authentication_title?.text = LanguageUtil.getString(this,"common_text_verifyInfoTitle")
        tet_name_authentication?.setTitle(LanguageUtil.getString(this,"common_text_realnameVerifyTitle"))
        tet_id_number_authentication?.setTitle(LanguageUtil.getString(this,"common_text_realidVerifyTitle"))
        btn_confirm?.setContent(LanguageUtil.getString(this,"kyc_action_submit"))
        tet_name_authentication?.setEditText(LanguageUtil.getString(this,"common_text_realnameVerifyPlaceholder"))
        tet_id_number_authentication?.setEditText(LanguageUtil.getString(this,"common_text_realidVerifyPlaceholder"))
        tet_name_authentication?.setEdittextMaxLength(50)
        tet_id_number_authentication?.setEdittextMaxLength(35)
    }

    fun submitAuthInfoCheck() {
        addDisposable(getMainModel().submitAuthInfoCheck(idNumber, realName, withdrawId, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data") ?: return
                var reqNum = data.optString("reqNum", "1") ?: "1"
                var result = data.optString("result", "") ?: ""
                when (result) {
                    ParamConstant.AUTH_SUCCESS -> {
                        ArouterUtil.greenChannel(RoutePath.IdentityAuthenticationResultActivity, Bundle().apply {
                            putBoolean(ParamConstant.AUTHENTICATION_STATUS, true)
                        })
                        finish()
                    }
                    ParamConstant.DEFAULT_NAME_ERROR -> {
                        NToastUtil.showTopToastNet(mActivity,false, LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_verifyAuthNameError"))
                        tv_prompt?.text = LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_verifyAuthNameError") +","+ LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_verifyAuthTryTimeDesc").format(BigDecimalUtils.sub("3", reqNum).toPlainString())
                        tv_prompt?.visibility = View.VISIBLE
                    }
                    ParamConstant.DEFAULT_ID_NUMBER_ERROR -> {
                        NToastUtil.showTopToastNet(mActivity,false, LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_verifyAuthIdNumberError"))
                        tv_prompt?.text = LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_verifyAuthIdNumberError") +","+ LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_verifyAuthTryTimeDesc").format(BigDecimalUtils.sub("3", reqNum).toPlainString())
                        tv_prompt?.visibility = View.VISIBLE
                    }
                    ParamConstant.NUMBER_OUT_ERROR -> {
                        ArouterUtil.greenChannel(RoutePath.IdentityAuthenticationResultActivity, Bundle().apply {
                            putBoolean(ParamConstant.AUTHENTICATION_STATUS, false)
                        })
                        finish()
                    }
                }


            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
            }

        }))
    }

    fun getIdentityAuthInfo() {
        addDisposable(getMainModel().getIdentityAuthInfo(object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data") ?: return
                tet_name_authentication?.setTitle(
                    LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_realnameVerifyTitle").format(data.optString("userName", "")
                        ?: ""))
                tet_id_number_authentication?.setTitle(
                    LanguageUtil.getString(this@IdentityAuthenticationActivity,"common_text_realidVerifyTitle").format(data?.optString("idNumber", "")
                        ?: ""))

            }
        }))
    }


}