package com.yjkj.chainup.ui.documentary.vm


import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.bean.CommissionBean1
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.ui.documentary.CreateTradersDialog
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class MyTradersModel : BaseViewModel() {




    var isMe = MutableLiveData(true)


    var uid = MutableLiveData("")

    var activity = MutableLiveData<FragmentActivity>()


    interface OnItemListener {
        fun onClick(item:Item)

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item:Item) {
            if (!item.isMe.value!!){
                return
            }
            CreateTradersDialog().apply {
                val bundle = Bundle()
                bundle.putString("uid", item.bean.value?.uid.toString())
                bundle.putSerializable("bean", item.bean.value)
                bundle.putInt("type", 2)
                this.arguments = bundle
            }
                .showDialog(activity.value?.supportFragmentManager, "")
        }
    }


    class Item {
        var type = MutableLiveData(1)
        var isMe = MutableLiveData(true)
        var bean = MutableLiveData<CommissionBean>()
        var bean1 = MutableLiveData<CommissionBean1>()
    }


    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_my_traders).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()

    fun getList() {
        items.clear()
        if (!isMe.value!!) {
            val map = HashMap<String, Any>()
            if ( uid.value.isNullOrEmpty()) {
                map["traderUid"] = UserDataService.getInstance().userInfo4UserId
            } else {
                map["traderUid"] = uid.value.toString()
            }
            startTask(apiService.follower(map), Consumer {
                if (it.data.isNullOrEmpty()) {
                    return@Consumer
                }
                for (i in it.data.indices) {
                    val item = Item()
                    item.bean1.value = it.data[i]
                    item.isMe.value = isMe.value
                    items.add(item)
                }


            })
        } else {
            val map = HashMap<String, Any>()
            if ( uid.value.isNullOrEmpty()) {
                map["traderUid"] = UserDataService.getInstance().userInfo4UserId
            } else {
                map["traderUid"] = uid.value.toString()
            }
            startTask(apiService.myTrader(map), Consumer {
                if (it.data.isNullOrEmpty()) {
                    return@Consumer
                }
                for (i in it.data.indices) {
                    val item = Item()
                    item.bean.value = it.data[i]
                    item.isMe.value = isMe.value
                    items.add(item)
                }

            })
        }

    }

}