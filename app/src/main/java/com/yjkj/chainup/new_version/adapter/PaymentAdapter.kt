package com.yjkj.chainup.new_version.adapter

import android.app.Activity
import android.widget.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.asset.EditPaymentActivity
import com.yjkj.chainup.new_version.activity.asset.EditPaymentActivity.Companion.BANKPAY
import com.yjkj.chainup.util.GlideUtils
import com.yjkj.chainup.util.NToastUtil
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

/**
 * Created by Bertking on 2018/10/13.
 */
open class PaymentAdapter(data: ArrayList<JSONObject>) :
        BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.item_payment_otc, data) {

    var datas: ArrayList<JSONObject> = arrayListOf()
    var activationDatas: ArrayList<JSONObject> = arrayListOf()
    var paymentLoadLister: PaymentLoadLister? = null
    fun setBans(bean: ArrayList<JSONObject>) {
        datas = bean
        activationDatas.clear()
        datas.forEach {
            if (it.optInt("isOpen") == 1) {
                activationDatas.add(it)
            }
        }
    }

    override fun convert(helper: BaseViewHolder, item: JSONObject) {

        if (item?.optString("payment") == BANKPAY) {
            helper?.setText(R.id.tv_payment_name, item.optString("bankName"))
        } else {
            helper?.setText(R.id.tv_payment_name, item?.optString("title"))
        }

        helper?.setText(R.id.tv_editor_content, LanguageUtil.getString(context,"b2c_text_edit"))
        helper?.setText(R.id.tv_account_content, item?.optString("account"))
        val checkBox = helper?.getView<CheckBox>(R.id.cb_layer)
        checkBox?.isChecked = item?.optInt("isOpen") == 1
        checkBox?.text = LanguageUtil.getString(context, when (checkBox?.isChecked) {
            true -> "payMethod_text_active"
            else -> "payMethod_text_inactive"
        })

        helper?.setText(R.id.tv_real_name, item?.optString("userName"))

        GlideUtils.loadImage(context, item?.optString("icon"), helper?.getView<ImageView>(R.id.tv_payment_icon))

        helper?.getView<CheckBox>(R.id.cb_layer)?.setOnClickListener {
            val check = it as CheckBox
            if (!check.isChecked && activationDatas.size == 1) {
                ToastUtils.showToast(LanguageUtil.getString(context, "otc_tip_paymentDidExist"))
                if (recyclerView.isComputingLayout) {
                    recyclerView?.post {
                        helper.getView<CheckBox>(R.id.cb_layer).isChecked = true
                        notifyItemChanged(helper.adapterPosition)
                    }
                } else {
                    helper.getView<CheckBox>(R.id.cb_layer).isChecked = true
                    notifyItemChanged(helper.adapterPosition)
                }
            } else {
                operatePayment4OTC(item?.optString("id")
                        ?: "0", if (item?.optInt("isOpen") == 1) "0" else "1", helper.adapterPosition)
            }
        }

        helper?.getView<RelativeLayout>(R.id.rl_layout)?.setOnClickListener {
            EditPaymentActivity.enter2(context, EditPaymentActivity.PREVIEW, paymentBean = datas[helper.adapterPosition].toString())
        }

        /**
         * 编辑
         */
        helper?.getView<TextView>(R.id.tv_editor_content)?.setOnClickListener {
            EditPaymentActivity.enter2(context, EditPaymentActivity.EDIT, datas[helper.adapterPosition].toString())
        }


    }


    /**
     * 支付方式的开关设置
     * @param isOpen 1/0
     */
    private fun operatePayment4OTC(id: String, isOpen: String, position: Int) {
        if (paymentLoadLister != null) {
            paymentLoadLister?.onLoading()
        }
        HttpClient.instance
                .operatePayment4OTC(id = id, isOpen = isOpen)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        datas[position].put("isOpen", isOpen.toInt())
                        isCheck(isOpen, position)
                        notifyItemChanged(position)
                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(context as Activity,false, msg)
                        datas[position].put("isOpen", if (isOpen == "1") 0 else 1)
                        isCheck(isOpen, position)
                        notifyItemChanged(position)
                    }
                })
    }

    private fun isCheck(isOpen: String, position: Int) {
        val isChecked = isOpen == "1"
        if (!isChecked) {
            activationDatas.remove(datas[position])
        } else {
            activationDatas.add(datas[position])
        }
        if (paymentLoadLister != null) {
            paymentLoadLister?.onClose()
        }
    }

    interface PaymentLoadLister {
        fun onLoading()
        fun onClose()
    }
}
