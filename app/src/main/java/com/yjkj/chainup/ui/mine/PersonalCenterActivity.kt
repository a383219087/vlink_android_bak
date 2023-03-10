package com.yjkj.chainup.ui.mine

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.SPUtils
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.HttpHelper
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.net.api.ApiService
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.BlackListActivity
import com.yjkj.chainup.new_version.activity.personalCenter.*
import com.yjkj.chainup.new_version.bean.ReadMessageCountBean
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_personal_center.*
import kotlinx.android.synthetic.main.item_personal_center_title.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/3/27-11:18 AM
 * @Email buptjinlong@163.com
 * @description  个人中心页面
 */

@Route(path = RoutePath.PersonalCenterActivity)
class PersonalCenterActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_personal_center
    }


    override fun initView() {
        setOnClick()
        if (!TextUtils.isEmpty(PublicInfoDataService.getInstance().getOnlineService(null))) {
            aiv_service?.visibility = View.VISIBLE
        }
        var fontFamily2 = SystemV2Utils.getFontFamily2();
        tv1.typeface = fontFamily2
        tv2.typeface = fontFamily2
        tv3.typeface = fontFamily2
        tv4.typeface = fontFamily2
        tv5.typeface = fontFamily2
        tv6.typeface = fontFamily2
        tv7.typeface = fontFamily2
        var fontFamily3 = SystemV2Utils.getFontFamily3();
        tv_share_data1.typeface = fontFamily3
        tv_share_data2.typeface = fontFamily3
        tv_share_data3.typeface = fontFamily3
    }

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
        aiv_level_rate?.setTitle(LanguageUtil.getString(this, "transfer_text_gradeRate"))
        aiv_service?.setTitle(LanguageUtil.getString(this, "personal_text_onlineservice"))
        aiv_help_center?.setTitle(LanguageUtil.getString(this, "personal_text_helpcenter"))
        aiv_safe_enter?.setTitle(LanguageUtil.getString(this, "personal_text_safetycenter"))
        aiv_setting?.setTitle(LanguageUtil.getString(this, "personal_text_setting"))
        aiv_mine_black_list?.setTitle(LanguageUtil.getString(this, "personal_text_blacklist"))
        aiv_about_us?.setTitle(LanguageUtil.getString(this, "personal_text_aboutus"))
    }


    override fun onResume() {
        super.onResume()
        getDealerInfo()
        if (UserDataService.getInstance().isLogined) {
            ChainUpApp().changeNetwork(
                SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)
            )
        } else {
            ChainUpApp().changeNetwork(false)
        }

    }

    fun initView(t: JSONObject?) {
        if (t == null) return

        val member = t.optJSONObject("member")
        if (null == member) {
            aiv_level_rate?.setStatusText("")
            ll_user_level?.visibility = View.GONE
        } else {
            /*会员类型名称*/
            val roleName = member?.optString("roleName")
            /* 会员等级 */
            val level = member?.optString("level")

            aiv_level_rate?.setStatusText("$roleName Lv.$level")
            ll_user_level?.visibility = View.VISIBLE

            tv_user_level?.text = "Lv.$level"
        }
        title_layout?.setContentTitle(LanguageUtil.getString(this, "userinfo_text_data"))

        title_layout?.setUserName("UID:${t.optString("id")}")
        title_layout?.setAccountContent(t.optString("userAccount"))
        /*title_layout?.setContentTitle(getString(R.string.title_personal_center));
        title_layout?.slidingShowTitle(false)*/
        when (t.optInt("authLevel")) {
            /**
             * 认证状态 0、未审核，1、通过，2、未通过  3未认证
             */
            0 -> {
                aiv_announcement.setStatusText(
                    LanguageUtil.getString(
                        this,
                        "personal_text_verified"
                    )
                )
            }
            1 -> {
                aiv_announcement.setShowArrow(false)
                aiv_announcement.setStatusText(
                    LanguageUtil.getString(
                        this,
                        "personal_text_verified"
                    )
                )
            }
            2 -> {
                aiv_announcement.setStatusText(
                    LanguageUtil.getString(
                        this,
                        "personal_text_unverified"
                    )
                )
            }
            3 -> {
                aiv_announcement.setStatusText(
                    LanguageUtil.getString(
                        this,
                        "personal_text_unverified"
                    )
                )
            }
        }

    }

    fun setOnClick() {
        if (!UserDataService.getInstance().isLogined) {
            title_layout?.setNoLogin()
            ll_share.visibility = View.GONE
        }

        if (PublicInfoDataService.getInstance().isUserRoleLevel(null)) {
            aiv_level_rate?.visibility = View.VISIBLE
        } else {
            aiv_level_rate?.visibility = View.GONE
        }


        /**
         * 点击个人资料
         */
        title_layout?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {
                when (UserDataService.getInstance()?.authLevel) {
                    0 -> {
                        ArouterUtil.greenChannel(
                            RoutePath.RealNameCertificaionSuccessActivity,
                            null
                        )
                    }
                    2, 3 -> {
                        ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                    }
                }
            }

            override fun onclickName() {
                if (LoginManager.checkLogin(this@PersonalCenterActivity, true)) {
                    ArouterUtil.greenChannel(RoutePath.PersonalInfoActivity, null)
                }
            }

            override fun onclickRightIcon() {

            }

            override fun onclickHead() {
                if (LoginManager.checkLogin(this@PersonalCenterActivity, true)) {
                    ArouterUtil.greenChannel(RoutePath.PersonalInfoActivity, null)
                }
            }

        }

        /**
         * 等级费率
         */
        aiv_level_rate?.setOnClickListener {
            if (!LoginManager.checkLogin(this, true)) {
                return@setOnClickListener
            }
            // 展示H5界面
            ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, Bundle().apply {
                putInt(ParamConstant.web_type, WebTypeEnum.ROLE_INDEX.value)
            })
        }

        /**
         * 安全中心
         */
        aiv_safe_enter?.setOnClickListener {
            if (!LoginManager.checkLogin(this, true)) {
                return@setOnClickListener
            }

            startActivity(Intent(this, SafetySettingActivity::class.java))
        }
        /**
         * 合伙人
         */
        ll_share?.setOnClickListener {
            if (!LoginManager.checkLogin(this, true)) {
                return@setOnClickListener
            }
            if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                ToastUtils.showToast(this.getString(R.string.important_hint1))
                return@setOnClickListener
            }
            ArouterUtil.navigation(RoutePath.PartnerActivity, null)
        }


        /**
         * 设置
         */
        aiv_setting?.setOnClickListener {
            NewSettingActivity.enter2(this)
        }


        /**
         * 用户信息
         */
        aiv_mail?.setOnClickListener {
            if (LoginManager.checkLogin(this@PersonalCenterActivity, true)) {
                ArouterUtil.greenChannel(RoutePath.PersonalInfoActivity, null)
            }

        }
        /**
         * 帮助中心
         */
        aiv_help_center?.setOnClickListener {
            startActivity(Intent(this, HelpCenterActivity::class.java))

        }
        /**
         * 关于我们
         */
        aiv_about_us?.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        /**
         * 屏蔽名单
         */
        aiv_mine_black_list?.setOnClickListener {
            if (!LoginManager.checkLogin(this, true)) {
                return@setOnClickListener
            }
            BlackListActivity.enter2(this)
        }

        /**
         * 人工服务
         */
        aiv_service?.setOnClickListener {
            //ItemDetailActivity.enter2(this@PersonalCenterActivity, PublicInfoDataService.getInstance().getOnlineService(null), "在线客服", true, true)
            val intent = Intent()
            intent.setClass(this, UdeskWebViewActivity::class.java)
            intent.putExtra(
                ParamConstant.URL_4_SERVICE,
                PublicInfoDataService.getInstance().getOnlineService(null)
            )
            startActivity(intent)
        }

        /**
         * 身份验证
         */
        aiv_announcement?.setOnClickListener {
            if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                ToastUtils.showToast(this.getString(R.string.important_hint1))
                return@setOnClickListener
            }
            when (UserDataService.getInstance()?.authLevel) {
                0 -> {
                    ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
                }
                2, 3 -> {
                    ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                }
            }
        }

    }

    /**
     * 获取用户信息
     */

    private fun getDealerInfo() {
        if (UserDataService.getInstance().isLogined) {
            addDisposable(getMainModel().getUserInfo(object : NDisposableObserver() {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    val json = jsonObject.optJSONObject("data")
                    initView(json)
                    UserDataService.getInstance().saveData(json)
                    getMessageCount()
                }

            }))
        } else {
            ChainUpApp().changeNetwork(false)
            ll_share.visibility = View.GONE
            title_layout?.setNoLogin()
        }

    }

    private fun getMessageCount() {
//        HttpClient.instance.getReadMessageCount()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : NetObserver<ReadMessageCountBean>() {
//                override fun onHandleSuccess(t: ReadMessageCountBean?) {
//                    t ?: return
//                    if (StringUtils.isNumeric(t.noReadMsgCount)) {
//                        if (t.noReadMsgCount.toInt() > 0) {
//                            aiv_mail?.showMailRed(true)
//                        } else {
//                            aiv_mail?.showMailRed(false)
//
//
//                        }
//                    }
//                }
//
//            })

        HttpClient.instance.getInviteStatus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : NetObserver<String>() {
                override fun onHandleSuccess(t: String?) {
                    if (t == "1" || t == "0") {
                        ll_share.visibility = View.VISIBLE
                    } else {
                        ll_share.visibility = View.GONE
                    }

                }

            })

        startTask(
            HttpHelper.instance.getBaseUrlService(ApiService::class.java).myBonus(),
            Consumer {
                tv_share_data1.text = it.data.userCount.toString()
                tv_share_data2.text = it.data.txCount.toString()
                tv_share_data3.text = DecimalUtil.cutValueByPrecision(it.data.amount.toString(), 2)


            }) {

        }
    }


}