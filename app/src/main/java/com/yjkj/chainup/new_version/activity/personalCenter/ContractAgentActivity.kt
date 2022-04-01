package com.yjkj.chainup.new_version.activity.personalCenter

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.request.RequestOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.AgentBean
import com.yjkj.chainup.bean.AgentInfoBean
import com.yjkj.chainup.bean.AgentUserBean
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.freestaking.ITEM_ID
import com.yjkj.chainup.freestaking.PROJECT_TYPE
import com.yjkj.chainup.freestaking.bean.MyPosRecordBean
import com.yjkj.chainup.freestaking.bean.NotificationRefreshBean
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_contract_agent.*
import kotlinx.android.synthetic.main.item_contract_agent_content.*
import kotlinx.android.synthetic.main.item_invitation_layout.*
import kotlinx.android.synthetic.main.item_invitation_registration_rewards.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.text.DecimalFormat

/**
 * @Author lianshangljl
 * @Date 2020-05-04-20:53
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.ContractAgentActivity1)
class ContractAgentActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_contract_agent

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
        setOnClick()
        setContent()
        initRedPacketView()
    }

    /*
   * 初始化红包view
   */
    private fun initRedPacketView() {
        val isRedPacketOpen = PublicInfoDataService.getInstance().isRedPacketOpen(null)
        showRedPacket(isRedPacketOpen)
    }

    private fun showRedPacket(isVisibile: Boolean) {
        if (isVisibile) {
            rl_red_envelope_entranc_layout?.visibility = View.VISIBLE
        } else {
            rl_red_envelope_entranc_layout?.visibility = View.GONE
        }
    }


    /**
     * 处理多语言问题
     */
    fun setContent() {
//        tv_right_title?.text = LanguageUtil.getString(this, "coAgent_text_explain")
        tv_setps1_content?.text = LanguageUtil.getString(this, "send_invitation")
        tv_setps2_content?.text = LanguageUtil.getString(this, "invitee_complete_registration_transaction")
        tv_setps3_content?.text = LanguageUtil.getString(this, "receive_corresponding_ratio_commission")


        tv_invitaion_link?.text = LanguageUtil.getString(this, "invitation_Link")
        tv_my_qr_code?.text = LanguageUtil.getString(this, "my_invitation_code")
        btn_generate_poster?.text = LanguageUtil.getString(this, "generate_invitation_poster")
        btn_face_to_face?.text = LanguageUtil.getString(this, "face_to_face_invitation")
        tv_registration_title?.text = LanguageUtil.getString(this, "rewards_invitation_register")

//        tv_spot_agent_rule_description?.text = LanguageUtil.getString(this, "rules_and_regulations")

        tv_invitation_registration_rule_description?.text = LanguageUtil.getString(this, "rules_and_regulations")

        tv_contract_agent_rule_description?.text = LanguageUtil.getString(this, "rules_and_regulations")

        tv_inviter?.text = LanguageUtil.getString(this, "number_friends")
        tv_commission_ratio?.text = LanguageUtil.getString(this, "rewards_amount") + "(USDT)"

//        tv_registration_title_agent?.text = LanguageUtil.getString(this, "spot_trading_broker")
//        tv_inviter_agent?.text = LanguageUtil.getString(this, "number_people_invited")
//        tv_commission_ratio_agent?.text = LanguageUtil.getString(this, "ratio_commission")
//        tv_total_commission?.text = LanguageUtil.getString(this, "cumulative_rewards_amount") + "(USDT)"




        tl_agent_return_layout?.setTitleContent(LanguageUtil.getString(this, "coAgent_text_return"))
        tl_agent_childReturn_layout?.setTitleContent(LanguageUtil.getString(this, "coAgent_text_childReturn"))
        tl_agent_childTotal_layout?.setTitleContent(LanguageUtil.getString(this, "coAgent_text_childTotal"))
        tl_agent_childTotalUSDT_layout?.setTitleContent(LanguageUtil.getString(this, "coAgent_text_childTotalUSDT"))
        tl_agent_yesterdayReturn_layout?.setTitleContent(LanguageUtil.getString(this, "coAgent_text_yesterdayReturn"))
        tl_agent_byesterdayReturn_layout?.setTitleContent(LanguageUtil.getString(this, "coAgent_text_byesterdayReturn"))
        tl_agent_child_layout?.setTitleContent(LanguageUtil.getString(this, "coAgent_text_level2return"))


    }

    var dialog: TDialog? = null

    fun setOnClick() {
        /**
         * 返回
         */
        iv_back?.setOnClickListener { finish() }
//        /**
//         * 跳转到说明
//         * TODO 需要确定URL是否正确
//         */
//        tv_right_title?.setOnClickListener {
//
//            var bundle = Bundle()
//
//            bundle.putString(ParamConstant.head_title, "")
//            bundle.putString(ParamConstant.web_url, PublicInfoDataService.getInstance().getAgentUrl(null))
//
//            ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
//        }


        /**
         * 点击红包 跳转
         */
        rl_red_envelope_entrance?.setOnClickListener {
            if (!LoginManager.checkLogin(this, true)) {
                return@setOnClickListener
            }

            var isEnforceGoogleAuth = PublicInfoDataService.getInstance().isEnforceGoogleAuth(null)

            var authLevel = UserDataService.getInstance().authLevel
            var googleStatus = UserDataService.getInstance().googleStatus
            var isOpenMobileCheck = UserDataService.getInstance().isOpenMobileCheck

            if (isEnforceGoogleAuth) {
                if (authLevel != 1 || googleStatus != 1) {
                    NewDialogUtils.redPackageCondition(this ?: return@setOnClickListener)
                    return@setOnClickListener
                }
            } else {
                if (authLevel != 1 || (googleStatus != 1 && isOpenMobileCheck != 1)) {
                    NewDialogUtils.redPackageCondition(this ?: return@setOnClickListener)
                    return@setOnClickListener
                }
            }
            ArouterUtil.navigation(RoutePath.CreateRedPackageActivity, null)
        }

        item_invitation_registration_rewards.visibility=View.GONE

        item_contract_agent_content.visibility = View.GONE
        /**
         * 点击关闭红包
         */
        iv_close_red_envelope?.setOnClickListener {
            showRedPacket(false)
        }

        /**
         * 面对面邀请
         */
        btn_face_to_face?.setOnClickListener {
            NewDialogUtils.showFaceToFace(this, faceToFaceImg)
        }

        /**
         * 邀请注册页面
         */
        item_invitation_registration_rewards?.setOnClickListener {
            ArouterUtil.navigation(RoutePath.InvitationRewardActivity, null)
        }
        /**
         * 点击奖励金
         */
        ll_commission_ratio_reward_layout?.setOnClickListener {
            ArouterUtil.navigation(RoutePath.InvitationRewardActivity, Bundle().apply {
                putString(ParamConstant.TYPE, ParamConstant.INVITE_REWARDS)
            })
        }

        /**
         * 生成邀请海报
         */
        btn_generate_poster?.setOnClickListener {
            var list: ArrayList<String> = arrayListOf()
            if (TextUtils.isEmpty(posterOneImg)) {
                list.add("url1")
            } else {
                list.add(posterOneImg)
            }
            if (TextUtils.isEmpty(posterTwoImg)) {
                list.add("url2")
            } else {
                list.add(posterTwoImg)
            }
            dialog = NewDialogUtils.showInvitationPosters(this, list, object : NewDialogUtils.DialogSharePostersListener {
                override fun saveIamgePosters(imageUrl: String, shareView: ImageView) {
                    createShareView(shareView)
                    dialog?.dismiss()
                }

                override fun saveIamgePostersNew(imageUrl: String) {

                }
            })
        }

        /**
         * 现货经纪人
         */
//        ll_spot_agent_rule_description_layout?.setOnClickListener {
//            goWebview(exchangeBrokerRuleUrl)
//        }
        /**
         * 邀请注册
         */
        ll_invitation_registration_rule_description_layout?.setOnClickListener {
            goWebview(invitationRuleUrl)
        }
        /**
         * 合约经纪人
         */
        ll_contract_agent_rule_description?.setOnClickListener {
            goWebview(coBrokerRuleUrl)
        }

        /**
         * 邀请人数
         */
//        ll_inviter_layout?.setOnClickListener {
//            if (!agent_data_query_url.contains("http")) {
//                agent_data_query_url = "http://$agent_data_query_url"
//            }
//            goWebview(agent_data_query_url)
//
//        }

        /**
         * 累计佣金
         */
//        ll_commission_layout?.setOnClickListener {
//            if (!agent_account_query_url.contains("http")) {
//                agent_account_query_url = "http://$agent_account_query_url"
//            }
//            goWebview(agent_account_query_url)
//        }

    }

    fun createShareView(shareView: ImageView) {
        var bitmap = (shareView.drawable as BitmapDrawable).bitmap
        sv_view.setShareView(bitmap)
        saveImage(sv_view)
    }


    fun goWebview(httpUrl: String) {
        if (!StringUtil.checkStr(httpUrl)) {
            return
        }
        var bundle = Bundle()
        bundle.putString(ParamConstant.head_title, "")
        bundle.putString(ParamConstant.web_url, httpUrl)
        ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
    }

    override fun initView() {
        super.initView()

        /**
         * 现货经纪人是否显示
         */
        if (PublicInfoDataService.getInstance().getAgentUserOpen(null) && UserDataService.getInstance().agentStatus != 0) {
            getAccountBalanceV4()
            view_contract_line_3?.visibility = View.VISIBLE
            item_spot_agent?.visibility = View.VISIBLE
        } else {
            item_spot_agent?.visibility = View.GONE
            view_contract_line_3?.visibility = View.GONE
        }
        initInvitView()
        getPageConfig()
        if (SystemUtils.isZh()) {
            rl_red_envelope_entrance.setImageResource(R.drawable.redenvelope)
        } else {
            rl_red_envelope_entrance.setImageResource(R.drawable.redenvelope_english)
        }
//        mDataList= ArrayList();
//        mBuffAdapter = BuffAdapter(R.layout.item_my_invitation_list, mDataList)
//        rv_invitation_reward_detail.apply {
//            layoutManager = LinearLayoutManager(this@ContractAgentActivity)
//            adapter = mBuffAdapter
//        }

        HttpClient.instance.getInviteConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<AgentBean>() {
                    override fun onHandleSuccess(t: AgentBean?) {
                        if (!TextUtils.isEmpty(t?.bannerUrl)) {
                            GlideUtils.loadImage(this@ContractAgentActivity,t?.bannerUrl,iv_bg_image)
                        }

                        tv_spot_agent_rule_description.visibility=if (TextUtils.isEmpty(t?.coAgentDesc)) View.GONE else View.VISIBLE
                        tv_spot_agent_rule_description.setOnClickListener {
                            if (!TextUtils.isEmpty(t?.coAgentDesc)) {
                                var bundle = Bundle()
                                bundle.putString(ParamConstant.head_title, "")
                                bundle.putString(ParamConstant.web_url, t?.coAgentDesc)
                                ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
                            }
                        }
                        posterOneImg=t?.billOneUrl.toString()
                        posterTwoImg=t?.billTwoUrl.toString()
                        faceToFaceImg=t?.faceToFaceUrl.toString()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, false)
                    }

                })

         HttpClient.instance.getAgentInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<AgentInfoBean>() {
                    override fun onHandleSuccess(t: AgentInfoBean?) {
                        tv_direct_commission.setText(DecimalFormat("0.00%").format(t?.scaleReturn))
                        tv_secondary_commission.setText(DecimalFormat("0.00%").format(t?.scaleSecond))
                        tv_subbroker_commission.setText(DecimalFormat("0.00%").format(t?.scaleSub))
                        tv_customers_number.setText(t?.countAgent)
                        tv_accumulated_commission.setText(t?.amountTotal)
                        tv_yesterday_commission.setText(t?.amountYesterday)
                        tv_previousday_commission.setText(t?.amountBYesterday)
                        tv_registration_title_agent.setText(t?.roleName)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, false)
                    }

                })

        HttpClient.instance.getAgentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<AgentUserBean>() {
                    override fun onHandleSuccess(t: AgentUserBean?) {
                        if (TextUtils.isEmpty(t?.status)){
                            ll_contract_agent.visibility=View.GONE
                        }else{
                            ll_contract_agent.visibility=View.VISIBLE
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, false)
                    }

                })
    }

    fun initInvitView() {
        tv_content_1?.text = UserDataService.getInstance().inviteUrl
        tv_content_2?.text = UserDataService.getInstance().inviteCode
        /**
         * 复制邀请链接
         */
        ll_copy_url_layout?.setOnClickListener {
            Utils.copyString(UserDataService.getInstance().inviteUrl)
            NToastUtil.showTopToastNet(this@ContractAgentActivity,true, LanguageUtil.getString(mActivity, "common_tip_copySuccess"))
        }
        /**
         * 复制邀请码
         */
        ll_copy_code_layout?.setOnClickListener {
            Utils.copyString(UserDataService.getInstance().inviteCode)
            NToastUtil.showTopToastNet(this@ContractAgentActivity,true, LanguageUtil.getString(mActivity, "common_tip_copySuccess"))
        }
    }

    fun initViewData() {
        if (UserDataService.getInstance().isLogined) {
            item_contract_agent_content?.visibility = View.GONE
            getNoTokenPublic()
        } else {
            item_contract_agent_content?.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
//        initViewData()
    }

    var coAgentStatus = "0"
    /**
     * 判断是否是合约经纪人
     */
    fun getNoTokenPublic() {
        showLoadingDialog()
        getMainModel().getNoTokenPublic(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                closeLoadingDialog()
                var data = jsonObject.optJSONObject("data") ?: return
                coAgentStatus = data?.optString("coAgentStatus")
                if (coAgentStatus != null && coAgentStatus == "1") {
                    getAgentIndex()
                }
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                closeLoadingDialog()
                NToastUtil.showTopToastNet(this@ContractAgentActivity,false, msg)
                item_contract_agent_content.visibility = View.GONE
            }
        })

    }

    fun getAgentIndex() {
        /**
         * TODO
         * 需要确定接口路径是否正确
         */
        getMainModel().getAgentIndex(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data") ?: return
                item_contract_agent_content?.visibility = View.VISIBLE
                var role_name = data?.optString("role_name", "") ?: ""

                tv_agent_title?.text = role_name

                /**
                 * 下级情况
                 */
                var childInfo = data?.optJSONObject("child_info")
                /**
                 * 客户数量
                 */
                tl_agent_childTotal_layout?.setContentText(childInfo?.optString("count_total", "")
                        ?: "")

                /**
                 * 返佣情况
                 */
                var bonusInfo = data?.optJSONObject("bonus_info")
                /**
                 * 昨日佣金折合
                 */
                val yesterday = bonusInfo?.optString("amount_yesterday", "")
                        ?: ""
                tl_agent_yesterdayReturn_layout?.setContentText(yesterday.totalNumToDigDown())
                /**
                 * 前日佣金折合
                 */
                val byesterday = bonusInfo?.optString("amount_b_yesterday", "")
                        ?: ""
                tl_agent_byesterdayReturn_layout?.setContentText(byesterday.totalNumToDigDown())

                /**
                 * 累计佣金折合
                 */
                val total = bonusInfo?.optString("amount_total", "")
                        ?: ""
                tl_agent_childTotalUSDT_layout?.setContentText(total.totalNumToDigDown())

                /**
                 * 分成比例
                 */
                var scaleInfo = data?.optJSONObject("scale_info")
                /**
                 * 直推返佣
                 */
                var result = scaleInfo?.optString("scale_return", "")
                        ?: ""
                var scaleSecond = scaleInfo?.optString("scale_second", "")
                        ?: ""
                if (scaleSecond.isNotEmpty() && scaleSecond != "0") {
                    tl_agent_child_layout.setContentText(scaleSecond.numToScalePer())
                    tl_agent_child_layout.visibility = View.VISIBLE
                } else {
                    tl_agent_child_layout.visibility = View.INVISIBLE
                }
                tl_agent_return_layout?.setContentText(result.numToScalePer())
                /**
                 * 子经纪人分佣
                 */
                var sub = scaleInfo?.optString("scale_sub", "")
                        ?: "0"
                if (sub.isNotEmpty() && sub != "0") {
                    tl_agent_childReturn_layout?.setContentText(sub.numToScalePer())
                    tl_agent_childReturn_layout.visibility = View.VISIBLE
                } else {
                    tl_agent_childReturn_layout.visibility = View.INVISIBLE
                }

            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                item_contract_agent_content?.visibility = View.GONE
                NToastUtil.showTopToastNet(this@ContractAgentActivity,false, msg)
            }

        })
    }

    var agent_data_query_url = ""

    var agent_account_query_url = ""

    /**
     * 获取现货经纪人
     */
    fun getAccountBalanceV4() {
        addDisposable(getMainModel().getAgentDataQuery("USDT", object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data") ?: return

                var userCount = data?.optString("userCount", "0") ?: "0"//邀请总人数

                var oneLevelScale = data?.optString("oneLevelScale", "0") ?: "0"//一级返佣比例

                var allBonusCoin = data?.optString("allBonusCoin", "USDT") ?: "USDT"//返佣币种

                var allBonusAmount = data?.optString("allBonusAmount", "0") ?: "0"//累计返佣

                agent_data_query_url = data?.optString("agent_data_query_url", "")
                        ?: ""//返佣记录url
                agent_account_query_url = data?.optString("agent_account_query_url", "")
                        ?: ""//持仓统计url

//                tv_inviter_content?.text = userCount
//
//                tv_commission_ratio_content?.text = "${BigDecimalUtils.divForDown(oneLevelScale, 2).toPlainString()}%"
//
//                tv_total_commission?.text = LanguageUtil.getString(this@ContractAgentActivity, "total_commission") + "(${allBonusCoin})"
//
//                tv_total_commission_content?.text = "${BigDecimalUtils.divForDown(allBonusAmount, NCoinManager.getCoinShowPrecision("USDT")).toPlainString()}"
            }
        }))
    }

    var inviteQECode = ""


    /**
     *  合约经纪人规则说明链接
     */
    var coBrokerRuleUrl = ""
    /**
     * 现货经济人规则说明链接
     */
    var exchangeBrokerRuleUrl = ""
    /**
     * 面对面分享底图地址
     */
    var faceToFaceImg = ""
    /**
     * 分享首页banner底图地址
     */
    var headerIndexImg = ""
    /**
     * 邀请注册奖励规则说明
     */
    var invitationRuleUrl = ""
    /**
     * 海报2图片地址
     */
    var posterOneImg = ""
    /**
     * 海报2图片地址
     */
    var posterTwoImg = ""

    /**
     * 经纪人展示页面图片链接接口
     */
    fun getPageConfig() {
        addDisposable(getMainModel().getPageConfig(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data")
                if (data == null) return
                tv_inviter_number_content?.text = data?.optString("invitationUserCount", "0") ?: "0"
                var invitationRewardUsdtSum = data?.optString("invitationRewardUsdtSum", "0")
                        ?: "0"
                tv_commission_ratio_reward?.text = BigDecimalUtils.divForDown(invitationRewardUsdtSum, NCoinManager.getCoinShowPrecision("USDT")).toPlainString()

                val pageConfig = data?.optJSONObject("pageConfig") ?: return
                coBrokerRuleUrl = pageConfig.optString("coBrokerRuleUrl")
                exchangeBrokerRuleUrl = pageConfig.optString("exchangeBrokerRuleUrl")
                faceToFaceImg = pageConfig.optString("faceToFaceImg")
                headerIndexImg = pageConfig.optString("headerIndexImg")
                invitationRuleUrl = pageConfig.optString("invitationRuleUrl")
                posterOneImg = pageConfig.optString("posterOneImg")
                posterTwoImg = pageConfig.optString("posterTwoImg")

                if (TextUtils.isEmpty(coBrokerRuleUrl)) {
                    ll_contract_agent_rule_description?.visibility = View.GONE
                } else {
                    ll_contract_agent_rule_description?.visibility = View.VISIBLE
                }

                if (TextUtils.isEmpty(exchangeBrokerRuleUrl)) {
                    ll_spot_agent_rule_description_layout?.visibility = View.GONE

                } else {
                    ll_spot_agent_rule_description_layout?.visibility = View.VISIBLE
                }
                if (TextUtils.isEmpty(invitationRuleUrl)) {
                    ll_invitation_registration_rule_description_layout?.visibility = View.GONE

                } else {
                    ll_invitation_registration_rule_description_layout?.visibility = View.VISIBLE
                }

                if (SystemUtils.isZh()) {
                    var options = RequestOptions().placeholder(R.drawable.banner_cn).error(R.drawable.banner_cn)

                    GlideUtils.load(this@ContractAgentActivity, headerIndexImg, iv_bg_image, options)
                } else {
                    var options = RequestOptions().placeholder(R.drawable.banner_en).error(R.drawable.banner_en)

                    GlideUtils.load(this@ContractAgentActivity, headerIndexImg, iv_bg_image, options)
                }

            }
        }))
    }


    fun saveImage(view: View) {
        val rxPermissions = RxPermissions(this)
        var bitmap: Bitmap? = null
        /**
         * 获取读写权限
         */
        rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted) {
                        bitmap = ScreenShotUtil.createViewBitmap(view, ContextCompat.getColor(this, R.color.white))
                        if (bitmap != null) {
                            val saveImageToGallery = ImageTools.saveImageToGallery4ContractAgent(this, bitmap)
                            if (saveImageToGallery) {
                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_saveImgSuccess"), true)
                            } else {
                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_saveImgFail"), false)
                            }
                        } else {
                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_saveImgFail"), false)
                        }
                    } else {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "common_tip_saveImgFail"), false)
                    }
                }
    }

}