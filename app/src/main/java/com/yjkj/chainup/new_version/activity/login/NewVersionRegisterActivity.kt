package com.yjkj.chainup.new_version.activity.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.jaeger.library.StatusBarUtil
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.CountryInfo
import com.yjkj.chainup.bean.TitleBean
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.ActivityManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.SelectAreaActivity
import com.yjkj.chainup.new_version.dialog.DialogUtil
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.new_version.view.PwdSettingView
import com.yjkj.chainup.util.*
import kotlinx.android.synthetic.main.activity_new_version_register.*
import kotlinx.android.synthetic.main.activity_new_version_register.pws_view
import kotlinx.android.synthetic.main.activity_new_version_register.tv_cancel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

/**
 * @Author lianshangljl
 * @Date 2019/3/11-3:33 PM
 * @Email buptjinlong@163.com
 * @description  ????????????
 */
@Route(path = "/login/newversionregisteractivity")
class NewVersionRegisterActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_new_version_register
    }


    var isEmailRegister = false
    var accountContent = ""
    var pwdTextContent = ""
    var country = "86"
    private lateinit var mTitleList: ArrayList<TitleBean>
    private lateinit var mBuffAdapter: BuffAdapter

    /**
     * ??????
     */
    var gee3test = arrayListOf<String>()


    var verificationType = 0


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ActivityManager.pushAct2Stack(this)
        var getMessage = PublicInfoDataService.getInstance().getUserRegType(null)
        setTextContent()
        initView()
        setOnClick()
        mTitleList = ArrayList()
        if (!TextUtils.isEmpty(getMessage)) {
            val json = JSONObject(getMessage)
            if (json.length() > 0) {
                val current = json.optJSONArray(JsonUtils.getLanguage())
                if (current != null && current.length() > 0) {
                    userRegTypeSetView(current[0] == 2)
                    if (current.length()==2){
                        mTitleList.add(TitleBean(LanguageUtil.getString(this, "register_action_phone"), current[0] == 1))
                        mTitleList.add(TitleBean(LanguageUtil.getString(this, "register_action_mail"), current[0] == 2))
                    }else{
                        if (current[0] == 1){
                            mTitleList.add(TitleBean(LanguageUtil.getString(this, "register_action_phone"), current[0] == 1))
                        }
                        if (current[0] == 2){
                            mTitleList.add(TitleBean(LanguageUtil.getString(this, "register_action_mail"), current[0] == 2))
                        }
                    }

                }
            }
        }
        mBuffAdapter = BuffAdapter(R.layout.item_register_tab, mTitleList)
        rv_register_title.apply {
            layoutManager = LinearLayoutManager(this@NewVersionRegisterActivity, RecyclerView.HORIZONTAL, false)
            adapter = mBuffAdapter
        }
        mBuffAdapter.setOnItemClickListener { adapter, view, position ->
            if (selectRegisterNum == position) {
                return@setOnItemClickListener
            }
            selectRegisterNum = position
            for (index in mTitleList.indices) {
                mTitleList[index].isSelect = (index == position)
            }

            mBuffAdapter.selectIndex = position
            userRegTypeSetView(!isEmailRegister)
            mBuffAdapter.notifyDataSetChanged()
        }
        if (isEmailRegister) {
            mBuffAdapter.selectIndex = 1
            selectRegisterNum = 1
        } else {
            mBuffAdapter.selectIndex = 0
            selectRegisterNum = 0
        }
    }

    override fun onResume() {
        super.onResume()
        ChainUpApp().changeNetwork(false)
    }
    var selectRegisterNum = 0

    fun userRegTypeSetView(status: Boolean) {
        isEmailRegister = status
        if (isEmailRegister) {
            //pws_view?.visibility = View.GONE
            cet_view?.hint = LanguageUtil.getString(this, "safety_tip_inputMail")
            cet_view?.inputType = InputType.TYPE_CLASS_TEXT
            cet_view?.setMaxLeng(110)
        } else {
            //pws_view?.visibility = View.VISIBLE
            cet_view?.hint = LanguageUtil.getString(this, "userinfo_tip_inputPhone")
            cet_view?.inputType = InputType.TYPE_CLASS_NUMBER
            cet_view?.setMaxLeng(11)
        }
        cet_view?.setText("")

    }

    fun setTextContent() {
        cet_view?.hint = LanguageUtil.getString(this, "userinfo_tip_inputPhone")
        cub_view?.setBottomTextContent(LanguageUtil.getString(this, "common_action_next"))
    }

    var tDialog: TDialog? = null

    @SuppressLint("NewApi")
    fun setOnClick() {
        tv_cancel?.setOnClickListener {
            finish()
        }

        cet_view?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                accountContent = s.toString()
                flushNextView()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        et_email?.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                accountContent = s.toString()
                flushNextView()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        pws_pwd?.setListener(object : PwdSettingView.OnTextListener {
            override fun onclickImage() {

            }

            override fun returnItem(item: Int) {

            }

            override fun showText(text: String): String {
                pwdTextContent = text
                flushNextView()
                return text
            }
        })

        pws_view.setOnClickListener {
            startActivity(Intent(this@NewVersionRegisterActivity, SelectAreaActivity::class.java))
        }

        tv_email.setOnClickListener{
            tv_email.setBackgroundResource(R.mipmap.bg_register_email_selected)
            tv_email.setTextColor(resources.getColor(R.color.white))
            tv_tel.setBackgroundResource(R.mipmap.bg_register_tel_normal)
            tv_tel.setTextColor(resources.getColor(R.color.normal_text_color))
            tv_title_email.visibility = View.VISIBLE
            et_email.visibility = View.VISIBLE
            tv_title_tel.visibility = View.GONE
//            cet_view.visibility = View.GONE
            layout_tel.visibility = View.GONE
            flushNextView()
        }

        tv_tel.setOnClickListener {
            tv_email.setBackgroundResource(R.mipmap.bg_register_email_normal)
            tv_email.setTextColor(resources.getColor(R.color.normal_text_color))
            tv_tel.setBackgroundResource(R.mipmap.bg_register_tel_selected)
            tv_tel.setTextColor(resources.getColor(R.color.white))
            tv_title_email.visibility = View.GONE
            et_email.visibility = View.GONE
            tv_title_tel.visibility = View.VISIBLE
//            cet_view.visibility = View.VISIBLE
            layout_tel.visibility = View.VISIBLE
            flushNextView()
        }

        /**
         * ???????????????
         */

        cub_view?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (et_email.visibility == View.VISIBLE) {
                    if (!StringUtils.checkEmail(accountContent)) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "toast_email_error"), isSuc = false)
                        return
                    }
                } else {
                    if (StringUtil.isNumericAndroidLenght(accountContent)) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "userinfo_tip_inputPhone"), isSuc = false)
                        return
                    }
                }
                reg4Step1(country, accountContent)
            }
        }

        layout_radio.setOnClickListener{
            (!iv_radio.isSelected).also { iv_radio.isSelected = it }
            flushNextView()
        }

        // TODO: test
//        et_email.setText("yubachang@163.com")
//        pws_pwd.text = "1"
//        et_inviteCode.setText("1")
        iv_radio.isSelected = true
    }

    private fun flushNextView() {
        if(et_email.visibility == View.VISIBLE) {
            if(et_email.text.isNotEmpty() && iv_radio.isSelected) {
                cub_view?.isEnable(true)
            } else {
                cub_view?.isEnable(false)
            }
        } else if(cet_view.visibility == View.VISIBLE) {
            if (accountContent.isNotEmpty() && (accountContent.length >= 5)&& iv_radio.isSelected) {
                cub_view?.isEnable(true)
            } else {
                cub_view?.isEnable(false)
            }
        }
    }

    override fun initView() {
        StatusBarUtil.setColor(this, ColorUtil.getColorByMode(R.color.bg_card_color_day), 0)
        cet_view?.inputType = InputType.TYPE_CLASS_PHONE
//        pws_view?.setvalidationStatus(false)
        cet_view?.isFocusable = true
        cet_view?.isFocusableInTouchMode = true
        cub_view?.isEnable(false)
        getAreaData()
        cet_view?.setOnFocusChangeListener { v, hasFocus ->
            cet_view_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        DialogUtil.showRegisterStatement(this)
    }

    private fun handleData(data: JsonObject) {
        var allCountry = arrayListOf<CountryInfo>()
        var limtCountry = PublicInfoDataService.getInstance().getLimitCountryList(null)
        if (data.get("countryList").isJsonArray) {
            val countryData = JsonUtils.jsonToList(data.get("countryList").toString(), CountryInfo::class.java)
            Log.d(TAG, "-----?????????----" + countryData.size)
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
            setPwsView(allCountry[0])
            var defaultCode = PublicInfoDataService.getInstance().getDefaultCountryCodeReal(null);
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
        runOnUiThread {
            if (Locale.getDefault().language.contentEquals("zh")) {
                tv_area_code.setText(it.cnName + " " + it.dialingCode)
            } else {
                tv_area_code.setText(it.enName + " " + it.dialingCode)
            }
            country = it.dialingCode
        }

    }


    private fun getAreaData() {
        val stream: InputStream = assets.open("area.json")
        val size = stream.available()
        val byteArray = ByteArray(size)
        stream.read(byteArray)
        stream.close()
        val json: String = String(byteArray, Charset.defaultCharset())
        val jsonObject = JsonParser().parse(json).asJsonObject
        handleData(jsonObject)
        Log.d(TAG, "------" + json)

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent4Area(area: CountryInfo) {
        country = area.dialingCode
        if (Locale.getDefault().language.contentEquals("zh")) {
            tv_area_code.setText(area.cnName + " " + area.dialingCode)
        } else {
            tv_area_code.setText(area.enName + " " + area.dialingCode)
        }

    }


    /**
     * ??????Step 1
     */
    private fun reg4Step1(country: String,
                          mobile: String,
                          geetest_challenge: String = "",
                          geetest_validate: String = "",
                          geetest_seccode: String = "", mJson: Map<String, String>? = null) {
        addDisposable(getMainModel().reg4Step1(country = country,
                mobile = mobile,
                verificationType = verificationType,
                geetest_challenge = geetest_challenge,
                geetest_validate = geetest_validate,
                geetest_seccode = geetest_seccode, json = mJson, consumer = object : NDisposableObserver(mActivity, false) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                if (et_email.visibility == View.VISIBLE) {
                    var bundle = Bundle()
                    bundle.putString("send_account", accountContent)
                    bundle.putString("send_token", "")
                    bundle.putString("send_countryCode", country)
                    bundle.putInt("send_position", NewPhoneVerificationActivity.EMAIL_VERIFY)
                    bundle.putInt("send_islogin", 1)
                    ArouterUtil.greenChannel("/login/newphoneverificationactivity", bundle)
                } else {
                    var bundle = Bundle()
                    bundle.putString("send_account", accountContent)
                    bundle.putString("send_token", "")
                    bundle.putString("send_countryCode", country)
                    bundle.putInt("send_position", NewPhoneVerificationActivity.MOBiLE_VERIFY)
                    bundle.putInt("send_islogin", 1)
                    ArouterUtil.greenChannel("/login/newphoneverificationactivity", bundle)
                }
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                if (code == 10023 || code == 10013) {
                    NewDialogUtils.showDialog(this@NewVersionRegisterActivity, LanguageUtil.getString(this@NewVersionRegisterActivity, "account_has_benn_registered_tip"), false, object : NewDialogUtils.DialogBottomListener {
                        override fun sendConfirm() {
                            ArouterUtil.navigation("/login/NewVersionLoginActivity", null)
                            finish()
                        }
                    }, getLineText("common_text_tip"), getLineText("login_action_login"), getLineText("cancel"))
                } else {
                    NToastUtil.showTopToastNet(mActivity, false, msg)
                }
            }
        }))
    }

    class BuffAdapter(layoutResId: Int, data: MutableList<TitleBean>) :
            BaseQuickAdapter<TitleBean, BaseViewHolder>(layoutResId, data) {
        var selectIndex = 0

        @SuppressLint("NewApi")
        override fun convert(helper: BaseViewHolder, item: TitleBean) {
            if (selectIndex == helper.adapterPosition) {
                helper.setTextColor(R.id.title, ContextCompat.getColor(context, R.color.text_color))
            } else {
                helper.setTextColor(R.id.title, ContextCompat.getColor(context, R.color.normal_text_color))
            }
            helper.setText(R.id.title, item.titleName)
            helper.getView<TextView>(R.id.title).setTextSize(if (item.isSelect) 28f else 16f)
        }
    }
}