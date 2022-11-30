package com.yjkj.chainup.ui.mine.partner.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.MyNextInvite
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.util.DateUtil
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class PartnerCommissionViewModel : BaseViewModel() {

    val itemBinding = ItemBinding.of<MyNextInvite>(BR.item, R.layout.item_invite_partner)
    val items: ObservableList<MyNextInvite> = ObservableArrayList()



    var time = MutableLiveData(DateUtil.timestampToStringNextDay())


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

    fun getList(page :Int) {
        val map = HashMap<String, Any>()
        map["pages"] = page.toString()
        map["pageSize"] ="20"
        startTask(apiService.bonusList(map), Consumer {
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

        })

    }

}