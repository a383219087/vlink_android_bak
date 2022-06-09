package com.yjkj.chainup.ui.buy.vm


import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.BuyInfo
import com.yjkj.chainup.bean.PaymentMethod
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.util.DecimalUtil
import io.reactivex.functions.Consumer
import java.util.*


class PayViewModel : BaseViewModel() {


    var check = MutableLiveData(false)


    fun setCheckk() {
        check.value = !check.value!!
    }


    var money = MutableLiveData("0")


    var checkInfo = MutableLiveData<BuyInfo>()
    var checkTwoInfo = MutableLiveData<BuyInfo>()


    var rate = MutableLiveData("0")


    var bean = MutableLiveData<PaymentMethod>()

    var price = MutableLiveData("1")


    fun getData() {
        if (bean.value?.price != null) {
            price.value = DecimalUtil.cutValueByPrecision(bean.value?.price.toString(), 2)
            rate.value =
                "≈${
                    DecimalUtil.cutValueByPrecision(
                        (money.value?.toDouble()?.div(bean.value!!.price!!.toDouble())).toString(),
                        2
                    )
                }${checkTwoInfo.value?.fiat}"
        }


    }


    fun crateOrder() {


        if (!check.value!!) {
            return
        }

        val map = TreeMap<String, String>()
        map["symbol"] = checkTwoInfo.value!!.fiat

        startTask(apiService.getChargeAddress(toRequestBody(DataHandler.encryptParams(map))), Consumer {
            val t: JSONObject? = it.data as JSONObject?
            val jsonObject = org.json.JSONObject(t.toString())
            val rechargeAddress = jsonObject.optString("addressStr")
            val split = rechargeAddress.split("_")


            val map1 = TreeMap<String, String>()
            map1["paymentPlatform"] = "xanpool"
            map1["fiatCurrency"] = checkInfo.value!!.fiat
            map1["digitalCurrency"] = checkTwoInfo.value!!.fiat
            map1["fiatAmount"] = money.value.toString()
            map1["walletAddress"] = split[0]
            startTask(apiService.createOrder(toRequestBody(DataHandler.encryptParams(map))), Consumer { it1 ->
                val bundle = Bundle()
                bundle.putString(ParamConstant.URL_4_SERVICE, it1.data.checkoutUrl)
                ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)
            })

        })


    }


}