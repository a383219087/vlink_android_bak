package com.yjkj.chainup.ui.mine.invite.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.InviteBean
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class MyFriendsViewModel : BaseViewModel() {


    val itemBinding = ItemBinding.of<InviteBean>(BR.item, R.layout.item_invite_rank)
    val items: ObservableList<InviteBean> = ObservableArrayList()


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
        startTask(apiService.myNextAgentUsers(map), Consumer {
            if (page == 1) {
                items.clear()
                isRefreshing.value = !isRefreshing.value!!
            } else {
                isLoadMore.value = !isLoadMore.value!!
            }
            if (it.data.data.isNullOrEmpty()) {
                return@Consumer
            }
            for (i in 0 until it.data.data.size) {
                val bean = it.data.data[i]
                if (page==1){
                    bean.index = i
                }else{
                    bean.index = 10*page+i
                }

                items.add(bean)
            }

        })

    }

}