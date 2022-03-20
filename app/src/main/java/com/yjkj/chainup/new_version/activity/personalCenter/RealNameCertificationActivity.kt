package com.yjkj.chainup.new_version.activity.personalCenter

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.util.JsonUtils
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.CountryInfo
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.SelectAreaActivity
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.activity.WebviewActivity
import com.yjkj.chainup.new_version.bean.AccountCertificationBean
import com.yjkj.chainup.new_version.bean.KYCBean
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.PwdSettingView
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.StringUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_realname_certification_choose_countries.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * @Author lianshangljl
 * @Date 2019/4/24-9:30 AM
 * @Email buptjinlong@163.com
 * @description 实名制认证
 */
@Route(path = RoutePath.RealNameCertificationActivity)
class RealNameCertificationActivity : NewBaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realname_certification_choose_countries)
        cub_next?.isEnable(true)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout?.slidingShowTitle(status)
            }
        }
        initView()
        setOnclick()
        title_layout?.setContentTitle(LanguageUtil.getString(this, "otcSafeAlert_action_identify"))
        tv_countries_title?.text = LanguageUtil.getString(this, "personal_text_country")
        cub_next?.setBottomTextContent(LanguageUtil.getString(this, "common_action_next"))
    }


    private fun handleData() {
        var data = JsonUtils.getAreaData(this)
        var allCountry = arrayListOf<CountryInfo>()
        var limtCountry = PublicInfoDataService.getInstance().getLimitCountryList(null)

        if (data.get("countryList").isJsonArray) {
            val countryData = JsonUtils.jsonToList(data.get("countryList").toString(), CountryInfo::class.java)
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
            areaCode = UserDataService.getInstance().countryCode

            if (TextUtils.isEmpty(UserDataService.getInstance().countryCode)) {
                selectRealNameCountry(allCountry)
            } else {
                allCountry.forEach {
                    if (it.dialingCode == areaCode) {
                        setPwsView(it)
                        return
                    }
                }
                selectRealNameCountry(allCountry)
            }
        }
    }

    fun selectRealNameCountry(allCountry: ArrayList<CountryInfo>) {
        var defaultCode = PublicInfoDataService.getInstance().getDefaultCountryCodeReal(null)
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
            areaCountry = it.cnName
            pws_view?.setEditText(areaCountry)
        } else {
            areaCountry = it.enName
            pws_view?.setEditText(areaCountry)
        }
        areaInfo = it.dialingCode
        areaCode = it.numberCode
    }


    fun initView() {
        handleData()

        /**
         * 国家
         */
        pws_view?.onTextListener = object : PwdSettingView.OnTextListener {
            override fun showText(text: String): String {
                return text
            }

            override fun returnItem(item: Int) {

            }

            override fun onclickImage() {
                startActivity(Intent(this@RealNameCertificationActivity, SelectAreaActivity::class.java))
            }

        }
    }

    fun setOnclick() {
        cub_next?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (PublicInfoDataService.getInstance().isInterfaceSwitchOpen(null)) {
                    accountCertification()
                } else {
                    getKYCConfig()
                }
            }
        }
    }

    /**
     * 这里需要传 国家号 + 国家码
     */
    var areaInfo: String = "+86"
    var areaCode: String = "156"
    var areaCountry: String = "中国"
    var areaBean: CountryInfo? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent4Area(area: CountryInfo) {
        areaBean = area
        areaInfo = area.dialingCode
        areaCode = area.numberCode
        if (Locale.getDefault().language.contentEquals("zh")) {
            areaCountry = area.cnName
        } else {
            areaCountry = area.enName
        }

        Log.d(TAG, "==========area:====" + area.toString())
        if (Locale.getDefault().language.contentEquals("zh")) {
//            tv_select_area.text = area.dialingCode + " ${area.cnName}"
            pws_view?.setEditText("${area.cnName}")
        } else {
//            tv_select_area.text = area.dialingCode + " ${area.enName}"
            pws_view?.setEditText("${area.enName}")

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    /**
     * 获取实名制认证token
     */
    fun accountCertification() {
        showProgressDialog()
        HttpClient.instance.AccountCertification()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<AccountCertificationBean>() {
                    override fun onHandleSuccess(certificationBean: AccountCertificationBean?) {
                        cancelProgressDialog()
                        certificationBean ?: return
                        if (areaInfo == "+86" && certificationBean?.openAuto == "1" && !certificationBean?.toKenUrl?.isEmpty() && certificationBean?.limitFlag != "1") {
                            var bundle = Bundle()
                            bundle.putString(ParamConstant.head_title, "")
                            bundle.putString(ParamConstant.web_url, certificationBean.toKenUrl)
                            ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
                        } else {
                            getKYCConfig()
                        }
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                        getKYCConfig()
                    }
                })
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent4(area: String) {

    }

    /**
     * 获取kyc配置
     */
    fun getKYCConfig() {
        showProgressDialog()
        HttpClient.instance.getKYCConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<KYCBean>() {
                    override fun onHandleSuccess(kycBean: KYCBean?) {
                        cancelProgressDialog()
                        kycBean ?: return
                        LogUtil.d(TAG, "=====kyc:$kycBean ======")
                        if (kycBean?.openSingPass == "1") {
                            LogUtil.d(TAG, "=====intoRealNameCertification:1========")
                            // 开启SingPass认证
                            val bundle = Bundle()
                            bundle.putString(ParamConstant.head_title, "")
                            bundle.putString(ParamConstant.NUMBER_CODE, areaCode)
                            bundle.putString(ParamConstant.DIALING_CODE, areaInfo.replace("+", ""))
                            bundle.putString(ParamConstant.COUNTRY_NAME, areaCountry)
                            bundle.putString(ParamConstant.web_url, kycBean?.h5_singpass_url)
                            bundle.putInt(ParamConstant.web_type, WebTypeEnum.SING_PASS.value)
                            ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
                        } else {
                            LogUtil.d(TAG, "=====intoRealNameCertification:2========")
                            if (kycBean?.verfyTemplet == "1") {
                                //精简的
                                RealNameCertificationChooseCountriesActivity.enter(this@RealNameCertificationActivity, areaInfo, areaCountry, areaCode)

                            } else {
                                //完整的
                                val bundle = Bundle()
                                bundle.putString(ParamConstant.head_title, "")
                                bundle.putString(ParamConstant.NUMBER_CODE, areaCode)
                                bundle.putString(ParamConstant.DIALING_CODE, areaInfo.replace("+", ""))
                                bundle.putString(ParamConstant.COUNTRY_NAME, areaCountry)
                                bundle.putString(ParamConstant.web_url, kycBean?.h5_templet2_url)
                                bundle.putInt(ParamConstant.web_type, WebTypeEnum.SING_PASS.value)
                                ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
                            }
                        }
                        finish()

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                        RealNameCertificationChooseCountriesActivity.enter(this@RealNameCertificationActivity, areaInfo, areaCountry, areaCode)
                        finish()
                    }
                })
    }

}