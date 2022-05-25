package com.yjkj.chainup.new_version.activity.asset

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import com.google.zxing.integration.android.IntentIntegrator
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.view.ManyChainSelectListener
import com.yjkj.chainup.new_version.activity.ScanningActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_version_add_address.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/5/17-3:58 PM
 * @Email buptjinlong@163.com
 * @description 增加提币地址
 */
class NewVersionAddAddressActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_version_add_address

    var symbol = ""
    var showSymbol = ""
    var isShowTag: Boolean = false

    var tokenBase = ""
    var tagBean = "0"
    var trustStype = false
    var addressContent = ""
    var addressTag = ""
    var addressNote = ""

    companion object {
        const val SYMBOL = "SYMBOL"
        const val SHOWSYMBOL = "SHOWSYMBOL"
        fun enter2(context: Context, symbol: String, showSymbol: String) {
            var intent = Intent()
            intent.setClass(context, NewVersionAddAddressActivity::class.java)
            intent.putExtra(SYMBOL, symbol)
            intent.putExtra(SHOWSYMBOL, showSymbol)
            context.startActivity(intent)
        }
    }

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
        getData()

        collapsing_toolbar?.title = String.format(LanguageUtil.getString(this, "add_address_title"), showSymbol)
        tv_address_title?.text = LanguageUtil.getString(this, "withdraw_text_address")
        et_address?.hint = LanguageUtil.getString(this, "address_tip_inputorcopy")
        et_address_tag?.hint = LanguageUtil.getString(this, "withdraw_tip_tagEmpty")
        tv_note_title?.text = LanguageUtil.getString(this, "address_text_remark")
        et_address_note?.hint = LanguageUtil.getString(this, "withdraw_text_remark")
        tv_trust_title?.text = LanguageUtil.getString(this, "withdraw_action_trustAddress")
        tv_trust_content?.text = LanguageUtil.getString(this, "withdraw_tip_trustDesc")
        cbtn_confirm?.setBottomTextContent(LanguageUtil.getString(this, "common_text_btnConfirm"))


        initManyChain()
        var userinfo = LoginManager.getInstance().loginInfo
        emailStatus = !TextUtils.isEmpty(userinfo.email)
        initView()
        initClickListener()
    }

    fun getData() {
        intent ?: return
        symbol = intent.getStringExtra(SYMBOL) ?: ""
        showSymbol = intent.getStringExtra(SHOWSYMBOL) ?: ""
        var list = PublicInfoDataService.getInstance().getFollowCoinsByMainCoinName(showSymbol)
        if (null == list || list.size == 0) {
            coinBean = NCoinManager.getCoinObj(symbol)
        } else {
            list.forEach {
                if (it?.optString("name", "") == symbol) {
                    coinBean = it
                }
            }
        }

        getCost(symbol)
    }

    var coinBean: JSONObject = JSONObject()

    /**
     * 设置多链
     */
    private fun initManyChain() {
        mcv_layout?.listener = object : ManyChainSelectListener {
            override fun selectCoin(coinName: JSONObject) {
                symbol = coinName.optString("name", "")
                coinBean = coinName
                setTag()
            }
        }
        mcv_layout?.setManyChainView(showSymbol, symbol)
    }

    fun setTag() {
        tokenBase = coinBean?.optString("tokenBase", "") ?: ""
        tagBean = coinBean?.optString("tagType", "0") ?: "0"
        if (tagBean == "0") {
            et_address_tag?.visibility = View.GONE
            tv_tag_title?.visibility = View.GONE

        } else {
            et_address_tag?.visibility = View.VISIBLE
            tv_tag_title?.visibility = View.VISIBLE

        }
        isShowTag = tagBean == "2"
    }

    override fun initView() {
        cbtn_confirm?.isEnable(false)
        setTag()
        /**
         * 地址
         */
        et_address?.isFocusable = true
        et_address?.isFocusableInTouchMode = true
        et_address?.setOnFocusChangeListener { v, hasFocus ->
            view_address_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        /**
         * tag
         */
        et_address_tag?.isFocusable = true
        et_address_tag?.isFocusableInTouchMode = true

        et_address_tag?.setOnFocusChangeListener { v, hasFocus ->
            view_address_tag_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        /**
         * 备注
         */
        et_address_note?.isFocusable = true
        et_address_note?.isFocusableInTouchMode = true

        et_address_note?.setOnFocusChangeListener { v, hasFocus ->
            view_address_note_line?.setBackgroundResource(if (hasFocus) R.color.main_blue else R.color.new_edit_line_color)
        }
        switch_trust_adr?.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                trustStype = isChecked
                if (isChecked) {
                    switch_trust_adr?.setBackgroundResource(R.drawable.open)
                } else {
                    switch_trust_adr?.setBackgroundResource(R.drawable.shut_down)
                }
            }

        })
        /**
         * 备注
         */
        et_address_note?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addressNote = s.toString()
                if (isShowTag) {
                    if (addressTag.isNotEmpty() && addressContent.isNotEmpty() && addressNote.isNotEmpty()) {
                        cbtn_confirm?.isEnable(true)
                    } else {
                        cbtn_confirm?.isEnable(false)
                    }
                } else {
                    if (addressContent.isNotEmpty() && addressNote.isNotEmpty()) {
                        cbtn_confirm?.isEnable(true)
                    } else {
                        cbtn_confirm?.isEnable(false)
                    }
                }
            }

        })
        /**
         * tag
         */
        et_address_tag?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addressTag = s.toString()
                if (isShowTag) {
                    if (addressTag.isNotEmpty() && addressContent.isNotEmpty() && addressNote.isNotEmpty()) {
                        cbtn_confirm?.isEnable(true)
                    } else {
                        cbtn_confirm?.isEnable(false)
                    }
                } else {
                    if (addressContent.isNotEmpty() && addressNote.isNotEmpty()) {
                        cbtn_confirm?.isEnable(true)
                    } else {
                        cbtn_confirm?.isEnable(false)
                    }
                }
            }

        })
        /**
         * 地址
         */
        et_address?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addressContent = s.toString()
                if (isShowTag) {
                    if (addressTag.isNotEmpty() && addressContent.isNotEmpty() && addressNote.isNotEmpty()) {
                        cbtn_confirm?.isEnable(true)
                    } else {
                        cbtn_confirm?.isEnable(false)
                    }
                } else {
                    if (addressContent.isNotEmpty() && addressNote.isNotEmpty()) {
                        cbtn_confirm?.isEnable(true)
                    } else {
                        cbtn_confirm?.isEnable(false)
                    }
                }
            }

        })

    }


    private fun initClickListener() {
        /**
         * 跳转至扫描界面
         */
        iv_scanner?.setOnClickListener {
            val intentIntegrator = IntentIntegrator(this)
            intentIntegrator.captureActivity = ScanningActivity::class.java
            intentIntegrator.setPrompt(LanguageUtil.getString(mActivity, "scan_tip_aimToScan"))
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.initiateScan()
        }


        /**
         * 确定按钮
         */
        cbtn_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                var address = et_address?.text.toString().trim()
                var addressName = et_address_note?.text.toString().trim()

                val tag = et_address_tag?.text.toString().trim()

                if (isShowTag) {
                    if (tagBean == "1") {
                        if (!TextUtils.isEmpty(tag)) {
                            address += "_$tag"
                        }
                    } else if (tagBean == "2") {
                        if (TextUtils.isEmpty(tag)) {
                            NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "toast_no_tag"))
                            return
                        } else {
                            address += "_$tag"
                        }
                    }
                } else {
                    if (tag.isNotEmpty()) {
                        address += "_$tag"
                    }
                }


                if (TextUtils.isEmpty(address)) {
                    NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "toast_no_withdraw_address"))
                    return
                }

                if (TextUtils.isEmpty(addressName)) {
                    NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "withdraw_text_remark"))

                    return
                }

                /**
                 * 若无Google或者Mobile，则不验证...
                 */
                if (UserDataService.getInstance().isOpenMobileCheck == 0 && UserDataService.getInstance().googleStatus == 0 && !emailStatus) {
                    addWithDrawAddress(address = address, label = addressName)
                } else {
                    showVerifyDialog(address, addressName)

                }
            }
        }
    }

    var tDialog: TDialog? = null
    var emailStatus = false

    /**
     * 显示二次验证的Dialog
     *
     * 11-添加数字货币地址
     *
     * {"code":"10033","msg":"请先进行实名认证","data":null}
     */
    private fun showVerifyDialog(address: String, label: String) {
        if (trustStype) {
            tDialog = NewDialogUtils.showSecurityDialog(this@NewVersionAddAddressActivity, -1, AppConstant.ADD_WITHDRAW_ADDRESS, object : NewDialogUtils.DialogVerifiactionListener {
                override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                    addWithDrawAddress(address = address, phone = phone
                            ?: "", label = label, googleCode = googleCode
                            ?: "", emailValidCode = mail ?: "")



                    tDialog?.dismiss()
                }

            }, 13)
        } else {
            tDialog = NewDialogUtils.showSecondDialog(this@NewVersionAddAddressActivity, AppConstant.ADD_WITHDRAW_ADDRESS, object : NewDialogUtils.DialogSecondListener {
                override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {

                    addWithDrawAddress(address = address, phone = phone
                            ?: "", label = label, googleCode = googleCode ?: "")

                    tDialog?.dismiss()
                }
            }, loginPwdShow = false)

        }

    }

    /**
     * 添加地址
     */
    fun addWithDrawAddress(address: String = "", phone: String = "", label: String = "", googleCode: String = "", emailValidCode: String = "") {
        HttpClient.instance.addWithdrawAddress(
                symbol = symbol,
                address = address,
                smsCode = phone,
                label = label,
                googleCode = googleCode,
                trustType = if (trustStype) "1" else "0",
                emailValidCode = emailValidCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        NToastUtil.showTopToastNet(mActivity, true, LanguageUtil.getString(mActivity, "add_address_suc"))
                        finish()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(mActivity, false, msg)

                    }
                })
    }


    /**
     * 扫码结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result != null) {
                if (TextUtils.isEmpty(result.contents)) {
                    NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "toast_scan_content_empty"))
                } else {
                    et_address.setText(result.contents)
                }
            } else {
                NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(mActivity, "toast_scan_content_empty"))
            }
        }
    }

    /**
     * 根据币种查询手续费和提现地址
     */
    private fun getCost(symbol: String = "") {
        addDisposable(getMainModel().getCost(symbol, object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data")
                if (null == data || data.length() == 0) return
                mcv_layout?.content = data?.optString("mainChainNameTip", "")
            }
        }))
    }

}
