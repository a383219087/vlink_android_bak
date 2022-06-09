package com.yjkj.chainup.ui.buy.vm


import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.BuyInfo
import com.yjkj.chainup.bean.PaymentMethod
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.net.DataHandler
import com.yjkj.chainup.ui.buy.CoinsDialog
import com.yjkj.chainup.util.DecimalUtil
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.functions.Consumer
import java.util.*


class QuickBuyViewModel : BaseViewModel() {

    var activity = MutableLiveData<FragmentActivity>()

    var money = MutableLiveData("0")


    var checkInfo = MutableLiveData<BuyInfo>()
    var checkTwoInfo = MutableLiveData<BuyInfo>()


    var bean = MutableLiveData<PaymentMethod>()
    var rate = MutableLiveData<String>()


    /**
     * 法币选择
     */
    fun setOneCheck() {
        CoinsDialog().apply {
            val bundle = Bundle()
            bundle.putString("type", "")
            this.arguments = bundle
            this.setOnSuccessClickListener(object : CoinsDialog.OnSuccessClickListener {
                override fun OnSuccess(info: BuyInfo) {
                    checkInfo.value = info
                    val itemsChild: ObservableList<BuyInfo> = ObservableArrayList()
                    info.coins?.forEach { it1 ->
                        val info1 = BuyInfo()
                        info1.fiat = it1.key
                        if (it1.value != null) {
                            info1.logo = it1.value!!
                        }
                        itemsChild.add(info1)
                    }
                    checkTwoInfo.value = itemsChild[0]
                    getPlatformAndPayTypes()
                }

            })
        }.showDialog(activity.value?.supportFragmentManager, "")


    }

    /**
     * 币币选择
     */
    fun setTwoCheck() {
        CoinsDialog().apply {
            val bundle = Bundle()
            bundle.putString("type", checkInfo.value?.fiat)
            this.arguments = bundle
            this.setOnSuccessClickListener(object : CoinsDialog.OnSuccessClickListener {
                override fun OnSuccess(info: BuyInfo) {
                    checkTwoInfo.value = info
                    getPlatformAndPayTypes()
                }

            })
        }.showDialog(activity.value?.supportFragmentManager, "")


    }


    /**
     * g购买记录
     */
    fun onclickRightIcon() {
        ArouterUtil.greenChannel(RoutePath.NewOTCOrdersActivity, null)
    }


    /**
     * 去卖币
     */
    fun sell() {
        val bundle = Bundle()
        bundle.putInt("tag", 1)
        ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, bundle)
    }

    /**
     * g购买
     */
    fun buy() {
        if (money.value?.toDouble() == 0.0) {
            ToastUtils.showToast("请输入金额")
            return
        }
        if (money.value?.toDouble()!! < bean.value?.minAmount?.toDouble()!!) {
            ToastUtils.showToast("金额不低于${bean.value?.minAmount}")
            return

        }
        if (money.value?.toDouble()!! > bean.value?.maxAmount?.toDouble()!!) {
            ToastUtils.showToast("金额不超过${bean.value?.maxAmount}")
            return

        }

        val bundle = Bundle()
        bundle.putSerializable("one", checkInfo.value)
        bundle.putSerializable("two", checkTwoInfo.value)
        bundle.putSerializable("bean", bean.value)
        bundle.putString("money", money.value)
        ArouterUtil.navigation(RoutePath.QuickBuySureActivity, bundle)
    }


    fun setInput(input: String) {
        if (input == "." && money.value!!.contains(".")) {
            return
        }
        if (money.value?.toDouble() == 0.0) {
            money.value = input
        } else {
            money.value = money.value + input
        }
    }

    fun delString() {
        if (money.value?.length == 0) {
            return
        }
        money.value = money.value!!.substring(0, money.value!!.length - 1)
        if (money.value?.isEmpty()!!) {
            money.value = "0"
        }
    }

    /**
     * 获取币种信息
     */
    fun getData() {
        startTask(apiService.coinList(), Consumer {
            if (it.data.isEmpty()) {
                return@Consumer
            }
            for (i in it.data.indices) {
                if (i == 0) {
                    checkInfo.value = it.data[i]
                    val itemsChild: ObservableList<BuyInfo> = ObservableArrayList()
                    it.data[i].coins?.forEach { it1 ->
                        val info1 = BuyInfo()
                        info1.fiat = it1.key
                        if (it1.value != null) {
                            info1.logo = it1.value!!
                        }

                        itemsChild.add(info1)
                    }
                    checkTwoInfo.value = itemsChild[0]
                }
            }
            getPlatformAndPayTypes()
        })
    }

    /**
     * 获取支持商户及其信息
     */
    fun getPlatformAndPayTypes() {
        val map = TreeMap<String, String>()
        map["fiatCurrency"] = checkInfo.value!!.fiat
        map["digitalCurrency"] = checkTwoInfo.value!!.fiat
        startTask(apiService.getPlatformAndPayTypes(toRequestBody(DataHandler.encryptParams(map))), Consumer {
            if (it.data.isNullOrEmpty() || it.data[0].paymentMethods.isNullOrEmpty()) {
                return@Consumer
            }
            bean.value = it.data[0].paymentMethods!![0]

            if (bean.value?.price == null) {
                rate.value = "未获取到汇率"
            } else {
                rate.value = "1 ${checkTwoInfo.value!!.fiat} ≈ ${
                    DecimalUtil.cutValueByPrecision(
                        (bean.value?.price).toString(),
                        2
                    )
                } ${checkInfo.value!!.fiat}"
            }


        })
    }

}