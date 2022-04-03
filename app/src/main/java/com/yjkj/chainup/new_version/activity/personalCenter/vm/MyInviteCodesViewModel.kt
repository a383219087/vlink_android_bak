package com.yjkj.chainup.new_version.activity.personalCenter.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.R
import com.yjkj.chainup.BR
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean

import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class MyInviteCodesViewModel : BaseViewModel() {


    interface OnItemListener {
        fun onClick(item: AgentCodeBean)
    }
    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: AgentCodeBean) {

        }

    }





    val itemBinding = ItemBinding.of<AgentCodeBean>(BR.item, R.layout.item_invite_code)
    val items: ObservableList<AgentCodeBean> = ObservableArrayList()

    fun myInviteCodes() {
        startTask(apiService.myInviteCodes(), Consumer{
            items.clear()
            for (i in 0 until it.data.size){
                val bean=it.data[i]
                bean.rateInt=it.data[i].rate.toInt()
                items.add(bean)
            }


        }, Consumer {

        });

    }

}