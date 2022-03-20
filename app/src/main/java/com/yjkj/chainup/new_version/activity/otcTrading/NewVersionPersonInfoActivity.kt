package com.yjkj.chainup.new_version.activity.otcTrading

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.util.JsonUtils
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.PersonAdsBean
import com.yjkj.chainup.bean.UserInfo4OTC
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.NewOTCPersonAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.new_version.view.NewOTCAdsListener
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_person_info.*
import kotlinx.android.synthetic.main.item_person_info.*
import org.json.JSONObject


const val PERSON_BUY = "buy"
const val PERSON_SELL = "sell"

/**
 * @Author lianshangljl
 * @Date 2019/4/23-10:32 AM
 * @Email buptjinlong@163.com
 * @description
 */
class NewVersionPersonInfoActivity : NBaseActivity(), NewOTCAdsListener {
    override fun setContentView(): Int {
        return R.layout.activity_new_person_info
    }


    override fun setOTCClick(item: PersonAdsBean.AdList) {
        getValidateAdvert(item.advertId, adType, item.coin, item.payments.size == 0)
    }

    var uid = ""

    companion object {
        const val UID: String = "UID"
        fun enter(context: Context, uid: String) {
            var intent = Intent(context, NewVersionPersonInfoActivity::class.java)
            intent.putExtra(UID, uid)
            context.startActivity(intent)
        }
    }

    fun setTextContent() {
        title_layout?.setContentTitle(getStringContent("otc_text_merchantHomePage"))
        title_layout?.setRightTitle(getStringContent("otc_action_addBlackList"))
        tv_transaction_number_title?.text = getStringContent("otc_text_merchantTradeNumber")
        tv_complain_num_title?.text = getStringContent("otc_text_merchantAppealNumber")
        tv_suc_complain_num_title?.text = getStringContent("otc_text_merchantAppealWin")
        tv_otc_xinyong_title?.text = getStringContent("otc_xinyong")
        tv_merchantPhoneAuth?.text = getStringContent("otc_text_merchantPhoneAuth")
        tv_identify?.text = getStringContent("common_text_identify")
        rb_buy?.text = getStringContent("otc_action_merchantBuy")
        rb_sell?.text = getStringContent("otc_action_merchantSell")

    }

    fun getStringContent(contentId: String): String {
        return LanguageUtil.getString(this, contentId)
    }

    var pageSize: String = "20"
    var page: Int = 1
    var adType = PERSON_SELL
    var adapter: NewOTCPersonAdapter? = null

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        rb_buy?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, ColorUtil.getOTCBuyOrSellDrawable())
        rb_sell?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        setTextContent()
        getData()
        getPersonInfo()
        getPersonAds(adType)
        setOnClick()
    }

    override fun onResume() {
        super.onResume()
        getUserPayment4OTC()
    }

    fun getData() {
        intent ?: return
        uid = intent.getStringExtra(UID)?:""
    }

    var beanInfo: UserInfo4OTC? = null
    fun initPersonInfoView(bean: UserInfo4OTC) {
        beanInfo = bean
        if (bean.otcNickName.isNotEmpty()) {
            iv_header_view?.text = bean.otcNickName.substring(0, 1)
        }
        tv_user_name?.text = bean.otcNickName
        /**
         * 交易次数
         */
        tv_transaction_number_content?.text = bean.completeOrders.toString()
        /**
         * 总胜诉量
         */
        tv_complain_num_content?.text = bean.complainNum.toString()
        /**
         * 胜诉量
         */
        tv_suc_complain_num_content?.text = bean.sucComplainNum.toString()
        /**
         * 信用
         */
        tv_otc_xinyong_content?.text = BigDecimalUtils.divForDown(BigDecimalUtils.mul((1 - bean.trustScore).toString(), "100").toString(), 0).toString() + "%"

        /**
         * 是否开启手机验证的
         */
        if (bean.mobileAuthStatus == 1) {
            iv_phone_status?.setImageResource(R.drawable.fiat_complete)
        } else {
            iv_phone_status?.setImageResource(R.drawable.delete)
        }

        /**
         * 身份认证
         */
        if (bean.authLevel == 1) {
            iv_identity_certificate_status?.setImageResource(R.drawable.fiat_complete)
        } else {
            iv_identity_certificate_status?.setImageResource(R.drawable.delete)
        }

        /**
         *  判断此页面访问情况（如下）：
        0：未登录用户查看他人的主页和登录用户查看自己的主页；
        1：登录用户查看他人的主页，并且当前显示用户在登录用户黑名单中；
        2：登录用户查看他人的主页，并且当前显示用户不在登录用户黑名单中
         */
        when (beanInfo?.identity) {
            0 -> {
                title_layout?.setRightTitle("")
            }
            1 -> {
                title_layout?.setRightTitle(getStringContent("common_action_removeBlackList"))
            }
            2 -> {
                title_layout?.setRightTitle(getStringContent("otc_action_addBlackList"))
            }

        }
    }

    /**
     * 获取个人信息
     */
    fun getPersonInfo() {
        HttpClient.instance.getPerson4otc(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<UserInfo4OTC>() {
                    override fun onHandleSuccess(t: UserInfo4OTC?) {
                        t ?: return
                        initPersonInfoView(t)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }


    fun setOnClick() {
        /**
         * 切换 价格购买 or 数量购买
         */
        rg_buy_sell?.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.rb_buy -> {
                    rb_buy?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, ColorUtil.getOTCBuyOrSellDrawable())
                    rb_sell?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    isfrister = true
                    adType = PERSON_SELL
                    getPersonAds(adType)
                }

                R.id.rb_sell -> {
                    rb_sell?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, ColorUtil.getOTCBuyOrSellDrawable())
                    rb_buy?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    isfrister = true
                    adType = PERSON_BUY
                    getPersonAds(adType)
                }
            }
        }
        title_layout.listener = object : PersonalCenterView.MyProfileListener {
            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                if (!LoginManager.checkLogin(this@NewVersionPersonInfoActivity, true)) {
                    return
                }
                /**
                 * 0 判断此页面访问情况（如下）：
                0：未登录用户查看他人的主页和登录用户查看自己的主页；
                1：登录用户查看他人的主页，并且当前显示用户在登录用户黑名单中；
                2：登录用户查看他人的主页，并且当前显示用户不在登录用户黑名单中
                 */
                when (beanInfo?.identity) {
                    1 -> {
                        NewDialogUtils.showNormalDialog(this@NewVersionPersonInfoActivity, getStringContent("common_tip_removeBlackList"), object : NewDialogUtils.DialogBottomListener {
                            override fun sendConfirm() {
                                removeRelationFromBlack(uid)
                            }

                        }, "", getStringContent("common_text_btnConfirm"), getStringContent("common_action_thinkAgain"))

                    }
                    2 -> {
                        NewDialogUtils.showNormalDialog(this@NewVersionPersonInfoActivity, getStringContent("common_tip_addToBlackList"), object : NewDialogUtils.DialogBottomListener {
                            override fun sendConfirm() {
                                userContacts4OTC(uid)
                            }

                        }, "", getStringContent("common_text_btnConfirm"), getStringContent("common_action_thinkAgain"))

                    }
                }

            }

            override fun onclickName() {

            }

            override fun onRealNameCertificat() {

            }

        }
    }

    var adList: ArrayList<PersonAdsBean.AdList> = arrayListOf()
    var payments: ArrayList<PersonAdsBean.Payments> = arrayListOf()

    fun initAdapter(bean: PersonAdsBean) {
        if (recycler_view == null) return
        adList = bean.adList
        adapter = NewOTCPersonAdapter(adList, this)
        recycler_view?.layoutManager = LinearLayoutManager(this)
        adapter?.setEmptyView(EmptyForAdapterView(this))
        recycler_view?.adapter = adapter
    }

    fun refreshView(bean: PersonAdsBean) {
        adapter?.setList(bean.adList)
    }

    var isfrister = true
    /**
     * 获取广告信息
     */

    fun getPersonAds(adType: String) {
        HttpClient.instance.getPersonAds(uid, pageSize, page.toString(), adType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<PersonAdsBean>() {
                    override fun onHandleSuccess(t: PersonAdsBean?) {
                        t ?: return
                        if (isfrister) {
                            isfrister = false
                            initAdapter(t)
                        } else {
                            refreshView(t)
                        }
                    }
                })
    }


    /**
     * 移除黑名单
     */
    fun removeRelationFromBlack(userId: String) {
        HttpClient.instance.removeRelationFromBlack(friendId = userId.toInt())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        //TODO 移除该Item
                        title_layout?.setRightTitle(getStringContent("otc_action_addBlackList"))
                        beanInfo?.identity = 0
                        DisplayUtil.showSnackBar(window?.decorView, getStringContent("otc_tip_didRemoveBlackList"), isSuc = true)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }

    /**
     *  加入黑名单
     */
    fun userContacts4OTC(otherUid: String) {
        HttpClient.instance.userContacts4OTC(otherUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        title_layout?.setRightTitle(getStringContent("common_action_removeBlackList"))
                        beanInfo?.identity = 1
                        DisplayUtil.showSnackBar(window?.decorView, getStringContent("common_tip_didinBlacklist"), isSuc = true)

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }


    /**
     * 购买出售前验证（app4.0）
     */
    fun getValidateAdvert(id: String, advertType: String, coin: String, status: Boolean) {

        if (!LoginManager.checkLogin(this, true)) {
            return
        }
        var type = if (advertType == "sell") "buy" else "sell"
        if (type == "sell") {
            if (JsonUtils.getCertification(this)) {
                if (UserDataService.getInstance().isCapitalPwordSet != 1 || beans.size == 0) {
                    NewDialogUtils.OTCTradingSecurityDialog(this, object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            if (UserDataService.getInstance().isCapitalPwordSet != 1) {
                                ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                            } else {
                                ArouterUtil.greenChannel(RoutePath.PaymentMethodActivity, null)
                            }

                        }
                    }, beans.size != 0)
                    return
                }
            }
        }

        addDisposable(getOTCModel().getValidateAdvert(id, type, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                if (advertType == "buy") {
                    NewVersionOTCSellActivity.enter2(this@NewVersionPersonInfoActivity, if (id.isNotEmpty()) id.toInt() else 0)
                } else {
                    NewVersionOTCBuyActivity.enter2(this@NewVersionPersonInfoActivity, if (id.isNotEmpty()) id.toInt() else 0)
                }
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                TradeCertification(type, code, msg, coin)
            }
        }))
    }

    private fun TradeCertification(advertType: String, code: Int, msg: String?, coin: String?) {
        if (advertType == "buy") {
            if (code == 2074 || code == 2055) {
                NewDialogUtils.OTCTradingNickeSecurityDialog(this@NewVersionPersonInfoActivity, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (code == 2074) {
                            //PersonalInfoActivity.enter2(context!!)
                            ArouterUtil.greenChannel(RoutePath.PersonalInfoActivity, null)
                        } else if (code == 2055) {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    //RealNameCertificaionSuccessActivity.enter(context!!)
                                    ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                        } else {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                        }
                    }
                })
                return
            } else if (code == 2079) {
                NewDialogUtils.showNormalDialog(this@NewVersionPersonInfoActivity, msg
                        ?: "", object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {

                    }
                }, "", getStringContent("alert_common_iknow"))
                return
            } else if (code == 2069) {
                NewDialogUtils.showSingle2Dialog(this@NewVersionPersonInfoActivity, msg
                        ?: "", object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (LoginManager.checkLogin(this@NewVersionPersonInfoActivity, true)) {
                            ArouterUtil.greenChannel(RoutePath.NewOTCOrdersActivity, null)
                        }

                    }
                }, "", getStringContent("alert_action_toDealWith"))
                return
            } else if (code != -1) {
                NToastUtil.showTopToastNet(mActivity,false, msg)
            }

        } else {
            if (code == 2001 || code == 2056) {
                NewDialogUtils.OTCTradingSecurityDialog(this@NewVersionPersonInfoActivity, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (code == 2001) {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                        } else {
                            ArouterUtil.greenChannel(RoutePath.PaymentMethodActivity, null)
                        }

                    }
                }, beans.size != 0)
                return
            } else if (code == 2078) {
                NewDialogUtils.showNormalDialog(this@NewVersionPersonInfoActivity, msg
                        ?: "", object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_BIBI, coin)
                    }
                }, "", getStringContent("alert_action_toTransfer"), getStringContent("common_text_btnCancel"))
                true
            } else if (code == 2069) {
                NewDialogUtils.showSingle2Dialog(this@NewVersionPersonInfoActivity, msg
                        ?: "", object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (LoginManager.checkLogin(this@NewVersionPersonInfoActivity, true)) {
                            ArouterUtil.greenChannel(RoutePath.NewOTCOrdersActivity, null)
                        }

                    }
                }, "", getStringContent("alert_action_toDealWith"))
                return
            } else if (code != -1) {
                NToastUtil.showTopToastNet(mActivity,false, msg)
            }
        }
    }


    var beans: ArrayList<JSONObject> = arrayListOf()
    /**
     * 获取支付方式
     */
    private fun getUserPayment4OTC() {
        if (UserDataService.getInstance().isLogined) {
            addDisposable(getOTCModel().getUserPayment4OTC(consumer = object : NDisposableObserver() {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    beans.clear()
                    val json = jsonObject.optJSONArray("data")
                    if (json?.length() ?: 0 > 0) {
                        for (num in 0 until json.length()) {
                            beans.add(json.optJSONObject(num))
                        }
                    }

                }

            }))
        }
    }
}