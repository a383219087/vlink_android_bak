package com.yjkj.chainup.ui.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.ui.documentary.ApplyTradersDialog
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class FirstViewModel : BaseViewModel() {

    var context = MutableLiveData<FragmentActivity>()

    var index = MutableLiveData(0)

    //    申请进度 -1: 未申请; 0: 申请中，1 : 已是交易员, 2: 拒绝

    var status = MutableLiveData<Int>()

    // 显示更多
    var showMore = MutableLiveData(false)

    // 显示资产
    var showMoney = MutableLiveData(UserDataService.getInstance().isShowAssets)


    fun setShowMoney() {
        showMoney.value=!showMoney.value!!
        UserDataService.getInstance().setShowAssetStatus(showMoney.value!!)
    }

    fun setShow(clear: Boolean) {
        if (clear) {
            showMore.value = false
        } else {
            showMore.value = !showMore.value!!
        }

    }


    interface OnItemListener {
        fun onClick(item: Item)

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: Item) {

            ARouter.getInstance().build(RoutePath.TradersActivity)
                .withSerializable("bean", item.bean.value)
                .withInt("status", item.status.value!!)
                .navigation()


        }
    }

    class Item {
        var status = MutableLiveData(-1)
        var bean = MutableLiveData<CommissionBean>()


    }

    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_documentary_first).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()

    var isRefreshing = MutableLiveData(false)
    var isLoadMore = MutableLiveData(false)

    private var page = 1
    var onRefreshCommand = BindingCommand<Any>(BindingAction {
        page = 1
        getList(1)
    })
    var onLoadMoreCommand = BindingCommand<Any>(BindingAction {
        page++
        getList(page)
    })

    //成为交易员
    fun appTraders() {
        ApplyTradersDialog().showDialog(context.value?.supportFragmentManager,"")
    }

    //发起带单
    fun toLaunchSingle() {

        EventBusUtil.post(MessageEvent(MessageEvent.DocumentaryActivity_close))
    }

    //我的跟随者
    fun toMyFollow() {

        EventBusUtil.post(MessageEvent(MessageEvent.DocumentaryActivity_index))
    }

    fun setIndex(i: Int) {
        index.value = i
        page = 1
        getList(1)
    }

    fun getList(page: Int) {
        val map = HashMap<String, Any>()
        map["page"] = page.toString()
        map["pageSize"] = "20"
        map["orderBy"] = when (index.value) {
            1 -> "profitUSDT"
            2 -> "totalUSDT"
            3 -> "orderCount"
            4 -> "followerCount"
            5 -> "profitRatio"
            6 -> "totalRatio"
            7 -> "winRatio"
            else -> "all"

        }
        startTask(apiService.traderUserList(map), Consumer {
            if (page == 1) {
                items.clear()
                isRefreshing.value = !isRefreshing.value!!
            } else {
                isLoadMore.value = !isLoadMore.value!!
            }
            if (it.data.isNullOrEmpty()) {
                return@Consumer
            }
            for (i in it.data.indices) {
                val item = Item()
                item.bean.value = it.data[i]
                item.status.value = status.value
                items.add(item)
            }

        })

    }


}