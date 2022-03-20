package com.yjkj.chainup.new_version.activity.asset

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.util.JsonUtils
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.activity.OTCChangePaymentActivity
import com.yjkj.chainup.new_version.adapter.PaymentAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.util.DisplayUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_payment_method.*
import org.json.JSONObject

/**
 * @date 2018-10-13
 * @author Bertking
 * @description 收款方式
 */
@Route(path = RoutePath.PaymentMethodActivity)
class PaymentMethodActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_payment_method
    }

    var list = arrayListOf<JSONObject>()
    var adapter = PaymentAdapter(list)

    var paymentList = arrayListOf<String>()


    override fun onResume() {
        super.onResume()
        if (UserDataService.getInstance().isLogined) {
            getUserPayment4OTC()
        }

    }

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
        v_title?.setContentTitle(LanguageUtil.getString(this, "noun_order_paymentTerm"))
        v_title?.setRightTitle(LanguageUtil.getString(this, "payMethod_action_addnew"))
    }

    override fun initView() {
        /**
         * 添加或者编辑支付方式
         */
        v_title.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                if (JsonUtils.getCertification(mActivity)) {
                    OTCChangePaymentActivity.newIntent(mActivity, paymentList)
                }

            }

            override fun onclickName() {

            }

        }
        rv_payment.layoutManager = LinearLayoutManager(this)
        rv_payment.adapter = adapter
        adapter.setEmptyView(EmptyForAdapterView(this))

        /**
         * 长按删除
         */
        adapter.setOnItemLongClickListener { adapter, view, position ->
            if (paymentBeanList.size == 1) {
                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "otc_tip_paymentLimitError"), isSuc = false)
                return@setOnItemLongClickListener true
            }

            if (paymentActivation.size == 1 && paymentBeanList[position].optInt("isOpen") == 1) {
                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this, "otc_tip_paymentLimitActiveError"), isSuc = false)
                return@setOnItemLongClickListener true
            }

            NewDialogUtils.showNormalDialog(this@PaymentMethodActivity, LanguageUtil.getString(this, "payMethod_tip_reconfirmUnbinding"), object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    removePayment4OTC(position, "", "")
                }
            }, cancelTitle = LanguageUtil.getString(this, "payMethod_text_unbinding"))

            true
        }
        adapter.paymentLoadLister = object : PaymentAdapter.PaymentLoadLister {
            override fun onLoading() {
                showLoadingDialog()
            }

            override fun onClose() {
                closeLoadingDialog()
            }
        }
    }


    var deletDialog: TDialog? = null
    /**
     * 显示二次验证的Dialog
     *
     * 13-数字货币提现
     */
    private fun showVerifyDialogForDel(id: String) {

        deletDialog = NewDialogUtils.showSecondDialog(this, AppConstant.SET_CAPITAL_PWD, object : NewDialogUtils.DialogSecondListener {
            override fun returnCode(phone: String?, mail: String?, googleCode: String?, pwd: String?) {

            }

        })
    }

    /**
     * 删除支付方式
     */
    private fun removePayment4OTC(position: Int, smsAuthCode: String, googleCode: String) {

        var id = paymentBeanList[position].optString("id")
        HttpClient.instance
                .removePayment4OTC(id = id, smsAuthCode = smsAuthCode, googleCode = googleCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {

                        paymentBeanList.removeAt(position)
                        paymentList.clear()
                        paymentActivation.clear()
                        paymentBeanList.forEach {
                            paymentList.add(it.optString("payment"))
                            if (it.optInt("isOpen") == 1) {
                                paymentActivation.add(it.optInt("id"))
                            }
                        }
                        adapter.setBans(paymentBeanList)
                        adapter.setList(paymentBeanList)
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(mActivity, "otc_tip_paymentDeactiveSuccess"), isSuc = true)
                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)

                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }

    /**
     * 支付方式
     */
    var paymentBeanList: ArrayList<JSONObject> = arrayListOf()
    var paymentActivation: ArrayList<Int> = arrayListOf()

    /**
     * 获取支付方式
     */
    private fun getUserPayment4OTC() {

        addDisposable(getOTCModel().getUserPayment4OTC(consumer = object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                paymentBeanList.clear()
                var json = jsonObject.optJSONArray("data")
                for (num in 0 until json.length()) {
                    paymentBeanList.add(json.optJSONObject(num))
                }
                paymentList.clear()
                paymentBeanList.forEach {
                    if (null != it.optString("payment")) {
                        paymentList.add(it.optString("payment"))
                    }
                    if (it.optInt("isOpen") == 1) {
                        paymentActivation.add(it.optInt("id"))
                    }
                }
                adapter.setBans(paymentBeanList)
                adapter.setList(paymentBeanList)
            }

        }))
    }


}
