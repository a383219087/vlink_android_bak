package com.yjkj.chainup.ui.documentary.vm


import android.app.Activity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.TraderTransactionBean
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.DateUtil
import com.yjkj.chainup.util.DecimalUtil
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class SingleMoneyViewModel : BaseViewModel() {
    var context = MutableLiveData<Activity>()

    //0是收益明细1是收益统计
    var index = MutableLiveData(0)
    fun setIndex(i: Int) {
        index.value = i
        getList()
    }


    var cnyString = MutableLiveData<String>()


    var usdtString = MutableLiveData<String>()

    var todayString = MutableLiveData<String>("0.0")
    var yesterdayString = MutableLiveData<String>("0.0")

    // 显示资产
    var showMoney = MutableLiveData(UserDataService.getInstance().isShowAssets)


    fun setShowMoney() {
        showMoney.value = !showMoney.value!!
        UserDataService.getInstance().setShowAssetStatus(showMoney.value!!)
    }

    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })

    interface OnItemListener {
        fun onClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
        }


    }

    class Item {
        var index = MutableLiveData(0)
        var side = MutableLiveData("")
        var bean = MutableLiveData<TraderTransactionBean>()
    }


    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_single_money).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()

    var onRefreshCommand1 = BindingCommand<Any>(BindingAction {
        EventBusUtil.post(MessageEvent(MessageEvent.refresh_MyInviteCodesActivity))
    })

    fun getList() {
        items.clear()
        val map = HashMap<String, Any>()
        map["uid"] = UserDataService.getInstance().userInfo4UserId
        if (index.value == 0) {
            startTask(contractApiService.traderTransaction(map), Consumer {
                if (it.data.records.isNullOrEmpty()) {
                    return@Consumer
                }
                for (i in 0 until it.data.records!!.size) {
                    val item = Item()
                    item.bean.value = it.data.records!![i]
                    item.index.value = index.value
                    item.side.value=if (it.data.records!![i].side=="BUY"){
                            context.value?.getString(R.string.contract_text_long)
                    }else{
                        context.value?.getString(R.string.contract_text_short)
                    }
                    items.add(item)
                }

            })
        } else {
            startTask(contractApiService.traderTransactionDay(map), Consumer {
                if (it.data.isNullOrEmpty()) {
                    return@Consumer
                }
                for (i in 0 until it.data!!.size) {
                    val item = Item()
                    item.bean.value = it.data!![i]
                    item.index.value = index.value
                    items.add(item)
                }

            })

        }

    }


    fun getData1() {
        startTask(contractApiService.traderTotalProfit(), Consumer {
            usdtString.value = DecimalUtil.cutValueByPrecision( it.data,2)
            cnyString.value =RateManager.getHomeCNYByCoinName("USDT", it.data, isOnlyResult = true)
        })
        val map = HashMap<String, Any>()
        map["uid"] = UserDataService.getInstance().userInfo4UserId
        startTask(contractApiService.traderTransactionDay(map), Consumer {
            if (it.data.isNullOrEmpty()) {
                return@Consumer
            }
            for (i in 0 until it.data!!.size) {

                if (it.data[i].date == DateUtil.longToString("yyyy-MM-dd", System.currentTimeMillis())) {
                    todayString.value = DecimalUtil.cutValueByPrecision(it.data[i].amount,2)
                }
                if (it.data[i].date == DateUtil.longToString("yyyy-MM-dd", System.currentTimeMillis() - 24 * 3600 * 1000)) {
                    yesterdayString.value =DecimalUtil.cutValueByPrecision(it.data[i].amount,2)

                }
            }

        })

    }
}