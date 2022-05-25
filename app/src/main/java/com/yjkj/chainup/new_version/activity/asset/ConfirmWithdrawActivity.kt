package com.yjkj.chainup.new_version.activity.asset

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.bean.AuthBean
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.ClickUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_confirm_withdraw.*

/**
 * @Author lianshangljl
 * @Date 2019/5/16-9:55 PM
 * @Email buptjinlong@163.com
 * @description 确认提币页面
 */
class ConfirmWithdrawActivity : NewBaseActivity() {


    var addressStatus = false
    var addrTag = ""
    var addrContent = ""
    var addressId = ""
    var showSymbol = ""
    var symbol = ""
    var amount = ""
    var actualaMount = ""
    var fee = ""

    companion object {
        const val ADDRESSID = "addressId"
        const val ADDRTAG = "addrTag"
        const val ADDRCONTENT = "addrContent"
        const val SHOWSYMBOL = "showSymbol"
        const val SYMBOL = "Symbol"
        const val AMOUNT = "amount"
        const val FEE = "fee"
        const val ADDRESSSTATUS = "addressStatus"
        const val ACTUALAMOUNT = "actualaMount"
        fun enter(context: Context, addressId: String?,
                  addrTag: String?, addrContent: String?, showSymbol: String?,
                  amount: String?, fee: String?, addressStatus: Boolean, symbol: String?, actualaMount: String?) {
            var intent = Intent()
            intent.setClass(context, ConfirmWithdrawActivity::class.java)
            intent.putExtra(ADDRTAG, addrTag)
            intent.putExtra(ADDRCONTENT, addrContent)
            intent.putExtra(SHOWSYMBOL, showSymbol)
            intent.putExtra(SYMBOL, symbol)
            intent.putExtra(ADDRESSID, addressId)
            intent.putExtra(AMOUNT, amount)
            intent.putExtra(FEE, fee)
            intent.putExtra(ADDRESSSTATUS, addressStatus)
            intent.putExtra(ACTUALAMOUNT, actualaMount)
            context.startActivity(intent)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_withdraw)
        title_layout?.setContentTitle(LanguageUtil.getString(this, "withdraw_action_confirm"))
        tv_symbol_title?.text = LanguageUtil.getString(this, "common_text_coinsymbol")
        tv_amount_title?.text = LanguageUtil.getString(this, "charge_text_volume")
        tv_fee_title?.text = LanguageUtil.getString(this, "withdraw_text_fee")
        tv_trust_title?.text = LanguageUtil.getString(this, "withdraw_action_trustAddress")
        tv_trust_content?.text = LanguageUtil.getString(this, "withdraw_tip_trustDesc")
        cbtn_confirm?.setBottomTextContent(LanguageUtil.getString(this, "common_text_btnConfirm"))
        getData()
        initView()
        setOnClick()

    }

    fun getData() {
        intent ?: return
        addressStatus = intent.getBooleanExtra(ADDRESSSTATUS, false)
        addrContent = intent.getStringExtra(ADDRCONTENT) ?: ""
        addressId = intent.getStringExtra(ADDRESSID) ?: ""
        addrTag = intent.getStringExtra(ADDRTAG) ?: ""
        showSymbol = intent.getStringExtra(SHOWSYMBOL) ?: ""
        symbol = intent.getStringExtra(SYMBOL) ?: ""
        amount = intent.getStringExtra(AMOUNT) ?: ""
        fee = intent.getStringExtra(FEE) ?: ""
        actualaMount = intent.getStringExtra(ACTUALAMOUNT) ?: ""

    }

    fun initView() {
        tv_adr_label?.text = ""
        if (addrTag.isEmpty()) {
            tv_adr_tag?.visibility = View.GONE
        } else {
            tv_adr_tag?.text = addrTag
        }
        tv_adr_content?.text = addrContent
        tv_symbol_content?.text = NCoinManager.getShowMarket(showSymbol)
        tv_amount_content?.text = amount
        tv_fee_content?.text = fee
        if (addressStatus) {
            switch_trust_adr?.visibility = View.GONE
            tv_trust_title?.text = LanguageUtil.getString(context, "withdraw_text_trustAddress")
            tv_trust_content?.text = LanguageUtil.getString(context, "withdraw_tip_trustDesc")
        } else {
            tv_trust_content?.text = LanguageUtil.getString(context, "withdraw_tip_trustDesc")
        }
        cbtn_confirm?.isEnable(true)
    }

    var tDialog: TDialog? = null


    fun setOnClick() {

        switch_trust_adr?.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                addressStatus = isChecked
                if (isChecked) {
                    switch_trust_adr?.setBackgroundResource(R.drawable.open)
                } else {
                    switch_trust_adr?.setBackgroundResource(R.drawable.shut_down)
                }
            }
        })

        cbtn_confirm?.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                if (ClickUtil.isFastDoubleClick()) {
                    return
                }
                if (switch_trust_adr?.visibility == View.VISIBLE) {
                    if (addressStatus) {
                        tDialog = NewDialogUtils.showSecurityDialog(this@ConfirmWithdrawActivity, -1, AppConstant.CRYPTO_WITHDRAW, object : NewDialogUtils.DialogVerifiactionListener {
                            override fun returnCode(phone: String?, mail: String?, googleCode: String?) {
                                submitWithDraw(phone ?: "", googleCode
                                        ?: "", if (addressStatus) "1" else "0", mail ?: "")

                                tDialog?.dismiss()
                            }

                        }, 17, LanguageUtil.getString(this@ConfirmWithdrawActivity, "common_text_btnConfirm"))
                    } else {
                        tDialog = NewDialogUtils.showSecondDialog(this@ConfirmWithdrawActivity, AppConstant.CRYPTO_WITHDRAW, object : NewDialogUtils.DialogSecondListener {
                            override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {
                                submitWithDraw(phone ?: "", googleCode
                                        ?: "", if (addressStatus) "1" else "0")


                                tDialog?.dismiss()
                            }
                        }, loginPwdShow = false, confirmTitle = LanguageUtil.getString(this@ConfirmWithdrawActivity, "common_text_btnConfirm"))

                    }
                } else {

                    submitWithDraw()
                }
            }
        }
    }


    /**
     * 确认提现
     */
    fun submitWithDraw(first: String = "", second: String = "", trustType: String = "", emailValidCode: String = "") {
        showProgressDialog()
        cbtn_confirm?.isEnable(false)
        HttpClient.instance.doWithdraw(addressId = addressId,
                fee = fee,
                smsCode = first,
                googleCode = second,
                amount = actualaMount,
                symbol = symbol,
                address = addrContent,
                trustType = trustType,
                emailValidCode = emailValidCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<AuthBean>() {
                    override fun onHandleSuccess(t: AuthBean?) {
                        cancelProgressDialog()
                        cbtn_confirm?.isEnable(true)
                        if (t == null) {
                            NToastUtil.showTopToastNet(this@ConfirmWithdrawActivity, true, getString(R.string.toast_withdraw_suc))
                            finish()
                            return
                        }
                        //是否需要实名认证
                        if (t?.isOpenUserCheck == true) {
                            //是否开启face++TODO 这里暂时不需要处理，因为还没有确定face++如何返回值。等后期优化
                            ArouterUtil.greenChannel(RoutePath.IdentityAuthenticationActivity, Bundle().apply {
                                putString(ParamConstant.WITHDRAW_ID, t?.withdrawId ?: "")
                            })
                        } else {
                            NToastUtil.showTopToastNet(this@ConfirmWithdrawActivity, true, getString(R.string.toast_withdraw_suc))
                        }

                        finish()

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        cbtn_confirm?.isEnable(true)
                        NToastUtil.showTopToastNet(this@ConfirmWithdrawActivity, false, msg)
                    }
                })
    }

}