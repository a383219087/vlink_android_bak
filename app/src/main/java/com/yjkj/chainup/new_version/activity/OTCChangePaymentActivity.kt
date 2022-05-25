package com.yjkj.chainup.new_version.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.service.OTCPublicInfoDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.asset.EditPaymentActivity
import com.yjkj.chainup.new_version.adapter.OTCChangeCityAdapter
import com.yjkj.chainup.new_version.adapter.OTCChangePayCoinAdapter
import com.yjkj.chainup.new_version.adapter.OTCChangePaymentAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.activity_change_payment.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2018/10/13-下午3:56
 * @Email buptjinlong@163.com
 * @description 选择支付方式页面 or 选择语言
 */
const val CHOOSE_PAYMENT = "choose_payment"
const val CHOOSE_PAYMENT_LIST = "choose_payment_list"
const val CHOOSE_PAYMENT_KEY = "choose_payment_key"
const val SYMBOL_OPEN = "symbol_open"
const val CHOOSE_ITEM = "choose_item"
const val OTC_CHANGE_PAYMENT = 9528

class OTCChangePaymentActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_change_payment
    }


    private var chooseType = 0
    private var symbolOpen = 0
    var paymentList = arrayListOf<String>()

    companion object {
        /**
         *choosePayment 0 支付方式 1 支付类型 2 选择国家 3 添加支付方式
         */
        fun newIntent(context: Context, chooseType: Int, symbolOpen: Int) {
            var intent = Intent(context, OTCChangePaymentActivity::class.java)
            intent.putExtra(CHOOSE_PAYMENT, chooseType)
            intent.putExtra(SYMBOL_OPEN, symbolOpen)
            (context as Activity).startActivityForResult(intent, OTC_CHANGE_PAYMENT)
        }

        fun newIntent(context: Context, paymentList: ArrayList<String> = arrayListOf()) {
            var intent = Intent(context, OTCChangePaymentActivity::class.java)
            intent.putExtra(CHOOSE_PAYMENT, 3)
            intent.putExtra(SYMBOL_OPEN, 0)
            intent.putExtra(CHOOSE_PAYMENT_LIST, paymentList)
            context.startActivity(intent)
        }
    }


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }


    override fun initView() {
        getData()
        setSelcetOnclick()
        activity_change_payment_list?.layoutManager = LinearLayoutManager(this)
        when (chooseType) {
            0 -> {
                activity_change_title?.setContentTitle(LanguageUtil.getString(this, "otc_choose_pay"))

                addDisposable(getOTCModel().getOTCPublicInfo(object : NDisposableObserver(this) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        OTCPublicInfoDataService.getInstance().saveData(jsonObject.optJSONObject("data"))
                        var data = OTCPublicInfoDataService.getInstance().payments
                        if (data != null) {

                            data.add(0, JSONObject("{\"key\":\"all\",\"title\":\"${LanguageUtil.getString(this@OTCChangePaymentActivity, "common_action_sendall")}\",\"icon\":\"\",\"account\":null,\"used\":false,\"numberCode\":null,\"open\":true,\"hide\":true}"))
                            for (i in 0 until data.size) {
                                data[i].putOpt("open", false)
                            }
                            data[symbolOpen].putOpt("open", true)
                            var adapter = OTCChangePaymentAdapter(data)
                            adapter.setEmptyView(EmptyForAdapterView(this@OTCChangePaymentActivity))
                            activity_change_payment_list.adapter = adapter
                            adapter.setOnItemClickListener { adapter, view, position ->
                                val intent = Intent()
                                intent.putExtra(CHOOSE_PAYMENT, data[position].optString("title"))
                                intent.putExtra(CHOOSE_PAYMENT_KEY, data[position].optString("key"))
                                intent.putExtra(SYMBOL_OPEN, position)
                                intent.putExtra(CHOOSE_ITEM, chooseType)
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                        }

                    }
                }))

            }
            1 -> {
                activity_change_title?.setContentTitle(LanguageUtil.getString(this, "otc_choose_fabi"))
                var data = OTCPublicInfoDataService.getInstance().payments
                for (i in 0 until data.size) {
                    data[i].putOpt("open", false)
                }
                data[symbolOpen].putOpt("open", true)
                var adapter = OTCChangePayCoinAdapter(data)
                adapter.setEmptyView(EmptyForAdapterView(this))
                activity_change_payment_list?.adapter = adapter
                adapter.setOnItemClickListener { adapter, view, position ->
                    val intent = Intent()
                    intent.putExtra(CHOOSE_PAYMENT, data[position].optString("title"))
                    intent.putExtra(CHOOSE_PAYMENT_KEY, data[position].optString("key"))
                    intent.putExtra(SYMBOL_OPEN, position)
                    intent.putExtra(CHOOSE_ITEM, chooseType)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            2 -> {
                activity_change_title?.setContentTitle(LanguageUtil.getString(this, "otc_choose_contry"))

                var data = OTCPublicInfoDataService.getInstance().countryNumberInfos
                var jsonObject = JSONObject()
                jsonObject.put("key", "all")
                jsonObject.put("title", LanguageUtil.getString(this, "common_action_sendall"))
                jsonObject.put("icon", "")
                jsonObject.put("open", true)
                jsonObject.put("hide", false)
                jsonObject.put("numberCode", "")
                jsonObject.put("used", "")
                data.add(0, jsonObject)
                for (i in 0 until data.size) {
                    data[i].put("open", false)
                }
                data[symbolOpen].put("open", true)
                var adapter = OTCChangeCityAdapter(data = data)
                adapter.setEmptyView(EmptyForAdapterView(this))
                activity_change_payment_list?.adapter = adapter
                adapter.setOnItemClickListener { _, view, position ->
                    val intent = Intent()
                    intent.putExtra(CHOOSE_PAYMENT, data[position].optString("title"))
                    intent.putExtra(CHOOSE_PAYMENT_KEY, data[position].optString("numberCode"))
                    intent.putExtra(SYMBOL_OPEN, position)
                    intent.putExtra(CHOOSE_ITEM, chooseType)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            3 -> {
                activity_change_title?.setContentTitle(LanguageUtil.getString(this, "noun_order_paymentTerm"))

                addDisposable(getOTCModel().getOTCPublicInfo(object : NDisposableObserver(this) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        OTCPublicInfoDataService.getInstance().saveData(jsonObject.optJSONObject("data"))
                        var data = OTCPublicInfoDataService.getInstance().payments
                        if (data != null) {
                            var adapter = OTCChangePaymentAdapter(data)
                            adapter.setEmptyView(EmptyForAdapterView(this@OTCChangePaymentActivity))
                            activity_change_payment_list?.adapter = adapter
                            adapter.setOnItemClickListener { adapter, view, position ->
                                if (paymentList.isNotEmpty()) {
                                    paymentList.forEach {
                                        if (it == data[position].optString("key")) {
                                            NToastUtil.showTopToastNet(mActivity,false, LanguageUtil.getString(this@OTCChangePaymentActivity, "otc_tip_paymentLimitDeactiveError"))
                                            return@setOnItemClickListener
                                        }
                                    }
                                }
                                EditPaymentActivity.enter2(this@OTCChangePaymentActivity, data[position].optString("key"), payTitle = data[position].optString("title"))
                                finish()
                            }
                        }
                    }
                }))


            }
        }
    }


    fun getData() {
        if (intent != null) {
            chooseType = intent.getIntExtra(CHOOSE_PAYMENT, 0)
            symbolOpen = intent.getIntExtra(SYMBOL_OPEN, 0)
            paymentList = intent.getStringArrayListExtra(CHOOSE_PAYMENT_LIST) ?: arrayListOf()
        }
    }

    fun setSelcetOnclick() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OTC_CHANGE_PAYMENT &&
                resultCode == RESULT_OK) {

        }

    }

}