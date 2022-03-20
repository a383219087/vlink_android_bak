package com.yjkj.chainup.new_version.activity.asset

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import kotlinx.android.synthetic.main.activity_authentication_results.*

/**
 * @Author lianshangljl
 * @Date 2020-03-18-15:15
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.IdentityAuthenticationResultActivity)
class IdentityAuthenticationResultActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_authentication_results

    /**
     * 是否认证成功
     */
    @JvmField
    @Autowired(name = ParamConstant.AUTHENTICATION_STATUS)
    var symbol = true
    var timeOut = 3

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        tv_authentication_content?.text = LanguageUtil.getString(this,"common_text_verifyAuthFailMsg")
        tv_out_time?.text = LanguageUtil.getString(this,"common_text_verifyAuthTimerMsg").format("$timeOut")
        initView()
    }

    override fun initView() {
        var authenticationStatus = LanguageUtil.getString(this,"common_text_verifySuccessTitle")
        var imageId = R.drawable.personal_successfulhints
        if (symbol) {
            imageId = R.drawable.personal_successfulhints
            authenticationStatus = LanguageUtil.getString(this,"common_text_verifySuccessTitle")
            tv_authentication_content?.visibility = View.GONE
        } else {
            imageId = R.drawable.personal_failure
            authenticationStatus = LanguageUtil.getString(this,"common_text_verifyAuthFailTitle")
            tv_authentication_content?.visibility = View.VISIBLE
        }
        iv_authentication?.setBackgroundResource(imageId)
        tv_authentication_status?.text = authenticationStatus
        iv_back?.setOnClickListener {
            GoBack()
        }
        startTimer()
    }


    var countDownTimer: CountDownTimer? = null
    fun startTimer() {
        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(3000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    tv_out_time?.text = LanguageUtil.getString(this@IdentityAuthenticationResultActivity,"common_text_verifyAuthTimerMsg").format("${--timeOut}")
                }

                override fun onFinish() {
                    GoBack()
                }

            }
        }
        countDownTimer?.start()
    }

    fun GoBack() {
        if (countDownTimer != null) {
            countDownTimer?.cancel()
            countDownTimer = null
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer != null) {
            countDownTimer?.cancel()
            countDownTimer = null
        }
    }


}