package com.yjkj.chainup.ui.mine.partner.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.InviteBean
import com.yjkj.chainup.bean.RebateBean
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class PartnerMyFriendsViewModel : BaseViewModel() {


    class Item {
        var show = MutableLiveData(false)
        var load = MutableLiveData(false)
        var bean = MutableLiveData<InviteBean>()
        var bean1 = MutableLiveData<RebateBean>()

    }

    interface OnItemListener {
        fun onClickOpen(index: Int)
        fun onClickClose(index: Int)

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClickOpen(index: Int) {
            if (items[index].load.value == true){
                items[index].show.value = true
              return
            }
            val map = HashMap<String, Any>()
            map["uid"] = items[index].bean.value?.uid.toString()
            startTask(apiService.stats(map), Consumer {
                items[index].bean1.value = it.data
                items[index].show.value = true
                items[index].load.value = true

            })

        }

        override fun onClickClose(index: Int) {
            items[index].show.value = false
        }
    }

    val itemBinding = ItemBinding.of<Item>(BR.item, R.layout.item_invite_rank_partner)
        .bindExtra(BR.onItemListener, onItemListener)
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


    fun getList(page: Int) {
        val map = HashMap<String, Any>()
        map["pages"] = page.toString()
        map["pageSize"] = "20"
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
                val item = Item()
                val bean = it.data.data[i]

                if (page == 1) {
                    bean.index = i
                } else {
                    bean.index = 10 * (page - 1) + i
                }
                item.bean.value = bean
                item.show.value = false
                items.add(item)
            }

        })

    }

}