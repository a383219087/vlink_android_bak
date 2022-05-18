package com.yjkj.chainup.new_version.activity.invite.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.bean.InviteRate
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class EditInviteCodesViewModel : BaseViewModel() {

    var type = MutableLiveData(1)
    var bean = MutableLiveData<AgentCodeBean>()


    var remark = MutableLiveData("")

    var rate = MutableLiveData(0)

    var isCheck = MutableLiveData(false)


    var inviteCode = MutableLiveData("")


    interface OnItemListener {
        fun onClick(item: InviteRate)
    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: InviteRate) {
            rate.value = item.rate
            for (i in items.indices) {
                items[i].checkRate.value = item.rate
            }
        }
    }

    class Item {
        var bean = MutableLiveData<InviteRate>()
        var checkRate = MutableLiveData(0)
    }

    val itemBinding = ItemBinding.of<Item>(BR.item, R.layout.item_invite_code_rate)
        .bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()


    fun agentRoles() {
        if (type.value == 2) {
            initData()
        }
        startTask(apiService.agentRoles(), Consumer { res ->
            if (res.data.isEmpty()) {
                return@Consumer
            }
            items.clear()
            for (i in res.data.indices) {
                val item = Item()
                item.bean.value = res.data[i];
                item.checkRate.value = rate.value!!
                items.add(item)
//                val bean=res.data[i]
//                bean.checkRate=rate.value!!
//                items.add(bean)
            }


        })
    }


    fun initData() {
        remark.value = bean.value?.remark
        rate.value = bean.value?.rateInt
        inviteCode.value = bean.value?.inviteCode
        isCheck.value = bean.value?.isDefault == "1"
    }

    fun onSure() {
        if (inviteCode.value?.isEmpty()!!) {
            val map = HashMap<String, Any>()
            map["rate"] = rate.value.toString()
            map["remark"] = remark.value.toString()
            map["isDefault"] = if (isCheck.value == true) "1" else "0"
            startTask(apiService.createInviteCode(map), Consumer {
                finish()
            })

        } else {
            val map = HashMap<String, Any>()
            map["rate"] = rate.value.toString()
            map["remark"] = remark.value.toString()
            map["inviteCode"] = inviteCode.value.toString()
            map["isDefault"] = if (isCheck.value == true) "1" else "0"
            startTask(apiService.updateDefaultCode(map), Consumer {
                finish()
            })
        }

    }
}