package com.yjkj.chainup.new_version.activity.login

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.jaeger.library.StatusBarUtil
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.util.ColorUtil
import kotlinx.android.synthetic.main.activity_guide_gesture_pwd.*

/**
 * @date 2018-11-13
 * @author Bertking
 * @description 手势引导页
 */
@Route(path = "/login/guidegesturepwdactivity")
class GuideGesturePwdActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_guide_gesture_pwd
    }

    var type = 1
    var pwd = ""
    var newhandPwd = ""

    companion object {
        const val GUIDEGESTURETYPE = "guidegesturetype"
        const val GUIDEGESTUREHANDPWD = "guidegesturehandpwd"
    }

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
        loadData()
    }

    override fun loadData() {
    }

    override fun initView() {
        StatusBarUtil.setColor(this, ColorUtil.getColorByMode(R.color.bg_card_color_day),0)
        getData()
        initClicker()
        setTextConetnt()
    }
    fun setTextConetnt(){
        tv_guide_tips?.text = LanguageUtil.getString(this,"login_tip_fingerprint")
        btn_now_set?.text = LanguageUtil.getString(this,"safety_action_activeFaceId")
        btn_cancel?.text = LanguageUtil.getString(this,"safety_action_faceIdNextTime")
    }


    fun getData() {
        if (intent != null) {
            type = intent.getIntExtra(GUIDEGESTURETYPE, 1)
            newhandPwd = intent.getStringExtra(GUIDEGESTUREHANDPWD)?:""
        }
    }

    fun initClicker() {
        when (type) {
            1 -> {
                /**
                 * 手势密码
                 */
                tv_guide_title?.text =  LanguageUtil.getString(mActivity,"set_gesture_pwd_title")
                iv_guide?.setImageResource(R.drawable.ic_guide_gesture)
                btn_now_set?.setOnClickListener {
                }

            }

            2 -> {
                /**
                 * 指纹密码
                 */
                tv_guide_title?.text =  LanguageUtil.getString(mActivity,"login_text_fingerprint")
                iv_guide?.setImageResource(R.drawable.login_fingerprintlogin)

                btn_now_set?.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putInt("type", TouchIDFaceIDActivity.FINGERPRINT)
                    bundle.putBoolean("is_first_login", true)
                    ArouterUtil.navigation("/login/touchidfaceidactivity", bundle)
                    finish()
                }
            }

            3 -> {
                /**
                 * 人脸识别
                 */
                tv_guide_title?.text =  LanguageUtil.getString(mActivity,"faceID_login")
                iv_guide?.setImageResource(R.drawable.ic_guide_face)

                btn_now_set?.setOnClickListener {
                    // TODO
                }
            }
        }


        btn_cancel.setOnClickListener {
            finish()
        }
    }

}
