package com.yjkj.chainup.new_version.activity.personalCenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.PwdSettingView
import com.yjkj.chainup.new_version.view.TextViewAddEditTextView
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import kotlinx.android.synthetic.main.activity_real_name_certification.*
import java.util.*

/**
 * @Author lianshangljl
 * @Date 2019-08-01-14:30
 * @Email buptjinlong@163.com
 * @description 新版本 实名制认证选择国家
 */

class RealNameCertificationChooseCountriesActivity : NewBaseActivity() {

    /**
     * 这里需要传 国家号 + 电话区号
     */
    var areaInfo: String = ""
    var areaCode: String = ""

    /**
     * 国家
     */
    var areaCountry: String = ""
    /**
     * 证件号
     */
    var certNum = ""
    /**
     * 名字
     */
    var realName = ""
    /**
     * 名
     */
    var surname = ""
    /**
     *  姓
     */
    var fristName = ""

    /**
     * 证件类型
     * 默认：身份证
     */
    var credentials_type: Int = RealNameCertificaionDownloadImgActivity.IDCARD

    companion object {
        fun enter(context: Context, areaNum: String, areaCountry: String, areaCode: String) {
            var intent = Intent()
            intent.setClass(context, RealNameCertificationChooseCountriesActivity::class.java)
            intent.putExtra(ParamConstant.AREA_NUMBER, areaNum)
            intent.putExtra(ParamConstant.AREA_COUNTRY, areaCountry)
            intent.putExtra(ParamConstant.AREA_CODE, areaCode)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_name_certification)

        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout?.slidingShowTitle(status)
            }
        }
        cub_next?.isEnable(false)
        getData()
        initView()
        setOnclick()
        title_layout?.setContentTitle(LanguageUtil.getString(this,"otcSafeAlert_action_identify"))
        tv_countries_title?.text = LanguageUtil.getString(this,"personal_text_country")
        tv_certificate_type_title?.text = LanguageUtil.getString(this,"kyc_text_certificateType")
        tv_prompt_title?.text = LanguageUtil.getString(this,"common_text_tip")
        tv_prompt?.text = LanguageUtil.getString(this,"common_tip_safetyIdentityAuth")

        tet_firstname?.setTitle(LanguageUtil.getString(this,"kyc_text_givenName"))
        tet_firstname?.setEditText(LanguageUtil.getString(this,"kyc_text_givenName"))

        tet_surname?.setTitle(LanguageUtil.getString(this,"kyc_text_familyName"))
        tet_surname?.setEditText(LanguageUtil.getString(this,"kyc_text_familyName"))

        tet_name?.setTitle(LanguageUtil.getString(this,"kyc_text_name"))
        tet_name?.setEditText(LanguageUtil.getString(this,"common_tip_inputRealName"))

        tet_id_number?.setTitle(LanguageUtil.getString(this,"kyc_text_certificateNumber"))
        tet_id_number?.setEditText(LanguageUtil.getString(this,"personal_tip_inputIdnumber"))
        cub_next?.setBottomTextContent(LanguageUtil.getString(this,"common_action_next"))
    }

    fun getData() {
        areaInfo = intent?.getStringExtra(ParamConstant.AREA_NUMBER) ?: ""
        areaCode = intent?.getStringExtra(ParamConstant.AREA_CODE) ?: ""
        areaCountry = intent?.getStringExtra(ParamConstant.AREA_COUNTRY) ?: ""
        pws_certificate_type_view?.setEditText(LanguageUtil.getString(this,"kyc_text_passport"))
        cet_view?.setText(areaCountry)
        cet_view?.isFocusable = false
        cet_view?.isFocusableInTouchMode = false
        if (areaInfo == "+86") {
            tet_firstname.visibility = View.GONE
            tet_surname.visibility = View.GONE
            tv_certificate_type_title.visibility = View.GONE
            pws_certificate_type_view.visibility = View.GONE
            tet_id_number.setTitle(LanguageUtil.getString(this,"kyc_text_idnumber"))
            credentials_type = RealNameCertificaionDownloadImgActivity.IDCARD
        } else {
            tet_name.visibility = View.GONE
            credentials_type = RealNameCertificaionDownloadImgActivity.PASSPORT
        }
    }


    var certificateDialog: TDialog? = null

    var certificateItem = 0
    fun initView() {

        /**
         * 证件类型
         */
        pws_certificate_type_view?.onTextListener = object : PwdSettingView.OnTextListener {
            override fun showText(text: String): String {

                return text
            }

            override fun returnItem(item: Int) {

            }

            override fun onclickImage() {
                certificateDialog = NewDialogUtils.showBottomListDialog(this@RealNameCertificationChooseCountriesActivity, arrayListOf(
                    LanguageUtil.getString(this@RealNameCertificationChooseCountriesActivity,"kyc_text_passport"), LanguageUtil.getString(this@RealNameCertificationChooseCountriesActivity,"kyc_text_drivingLicense"), LanguageUtil.getString(this@RealNameCertificationChooseCountriesActivity,"kyc_text_otherLegal")), certificateItem, object : NewDialogUtils.DialogOnclickListener {
                    override fun clickItem(data: ArrayList<String>, item: Int) {
                        certificateItem = item
                        when (item) {
                            0 -> {
                                credentials_type = RealNameCertificaionDownloadImgActivity.PASSPORT
                            }
                            1 -> {
                                credentials_type = RealNameCertificaionDownloadImgActivity.DRIVERLICENSE
                            }
                            2 -> {
                                credentials_type = RealNameCertificaionDownloadImgActivity.ORHERID
                            }
                        }
                        pws_certificate_type_view?.setEditText(data[item])
                        certificateDialog?.dismiss()
                    }
                })
            }

        }
        tet_name?.listener = object : TextViewAddEditTextView.OnTextListener {
            override fun showText(text: String): String {
                realName = text
                if (areaInfo == "+86") {
                    if (realName.isNotEmpty() && certNum.isNotEmpty()) {
                        cub_next?.isEnable(true)
                    } else {
                        cub_next?.isEnable(false)
                    }
                }

                return text
            }

        }
        tet_id_number?.listener = object : TextViewAddEditTextView.OnTextListener {
            override fun showText(text: String): String {
                certNum = text
                if (areaInfo == "+86") {
                    if (realName.isNotEmpty() && certNum.isNotEmpty()) {
                        cub_next?.isEnable(true)
                    } else {
                        cub_next?.isEnable(false)
                    }
                } else {
                    if (certNum.isNotEmpty() && fristName.isNotEmpty() && surname.isNotEmpty()) {
                        cub_next?.isEnable(true)
                    } else {
                        cub_next?.isEnable(false)
                    }
                }
                return text

            }
        }
        tet_firstname?.listener = object : TextViewAddEditTextView.OnTextListener {
            override fun showText(text: String): String {
                fristName = text
                if (certNum.isNotEmpty() && fristName.isNotEmpty() && surname.isNotEmpty()) {
                    cub_next?.isEnable(true)
                } else {
                    cub_next?.isEnable(false)
                }
                return text
            }

        }
        tet_surname?.listener = object : TextViewAddEditTextView.OnTextListener {
            override fun showText(text: String): String {
                surname = text
                if (certNum.isNotEmpty() && fristName.isNotEmpty() && surname.isNotEmpty()) {
                    cub_next?.isEnable(true)
                } else {
                    cub_next?.isEnable(false)
                }
                return text
            }

        }

    }


    fun setOnclick() {
        cub_next?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (areaInfo == "+86") {
                    RealNameCertificaionDownloadImgActivity.enter2(this@RealNameCertificationChooseCountriesActivity, areaInfo, certNum, realName, credentials_type, areaCode)
                } else {
                    RealNameCertificaionDownloadImgActivity.enter2(this@RealNameCertificationChooseCountriesActivity, areaInfo, certNum, surname, fristName, credentials_type, areaCode)
                }
                finish()
            }
        }

    }


}