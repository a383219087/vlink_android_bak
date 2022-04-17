package com.yjkj.chainup.new_version.activity.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.db.constant.RoutePath
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class FirstViewModel : BaseViewModel() {

    var index = MutableLiveData(0)

    interface OnItemListener {
        fun onClick(item: CommissionBean)

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: CommissionBean) {
            ARouter.getInstance().build(RoutePath.TradersActivity)
                .withSerializable("bean",item)
                .navigation()

        }
    }

    val itemBinding =
        ItemBinding.of<CommissionBean>(BR.item, R.layout.item_documentary_first).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<CommissionBean> = ObservableArrayList()

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
    fun appTraders(){
        ARouter.getInstance().build(RoutePath.ApplyTradersActivity).navigation()
    }



    fun setIndex(i :Int){
        index.value=i
        page = 1
        getList(1)
    }

    fun getList(page :Int) {
        val map = HashMap<String, Any>()
        map["page"] = page.toString()
        map["pageSize"] ="20"
        map["orderBy"] =when(index.value){
            1->"profitUSDT"
            2->"totalUSDT"
            3->"orderCount"
            4->"followerCount"
            5->"profitRatio"
            6->"totalRatio"
            7->"winRatio"
            else->"all"

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
            items.addAll(it.data)

        }, Consumer {

        });

    }





}