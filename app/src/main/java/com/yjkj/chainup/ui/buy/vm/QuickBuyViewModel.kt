package com.yjkj.chainup.ui.buy.vm


import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.BuyInfo
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import io.reactivex.functions.Consumer


class QuickBuyViewModel : BaseViewModel() {


    var money = MutableLiveData("0")

    var items: ObservableList<BuyInfo> = ObservableArrayList()

    var itemsChild: ObservableList<BuyInfo> = ObservableArrayList()

    var oneIndex = MutableLiveData(0)

    var twoIndex = MutableLiveData(0)



    /**
     * 法币选择
     */
    fun setOneCheck(){



    }

    /**
     * 币币选择
     */
    fun setTwoCheck(){




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
        ArouterUtil.navigation(RoutePath.QuickBuySureActivity, null)
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
        items.clear()
        startTask(apiService.coinList(), Consumer {
            if (it.data.isEmpty()) {
                return@Consumer
            }
            for (i in it.data.indices) {
                items.add(it.data[i])
                if (i == 0) {
                    itemsChild.clear()
                    it.data[i].coins?.forEach { it1 ->
                        itemsChild.add(BuyInfo(fiat = it1.key, logo = it1.value))
                    }

                }

            }


        })
    }

}