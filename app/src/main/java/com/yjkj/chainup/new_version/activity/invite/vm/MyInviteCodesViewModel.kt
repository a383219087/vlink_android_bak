package com.yjkj.chainup.new_version.activity.invite.vm


import android.content.Context
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.BR
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.db.constant.RoutePath

import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class MyInviteCodesViewModel : BaseViewModel() {

    var context = MutableLiveData<Context>()

    interface OnItemListener {
        fun onClick(item: AgentCodeBean)
        fun onEditClick(item: AgentCodeBean)
        fun onDefault(item: AgentCodeBean)
    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: AgentCodeBean) {

        }

        override fun onEditClick(item: AgentCodeBean) {
            ARouter.getInstance().build(RoutePath.EditInviteCodesActivity)
                .withInt("type", 2)
                .withSerializable("bean", item)
                .navigation()
        }

        override fun onDefault(item: AgentCodeBean) {
            if (item.isDefault=="1"){
                return
            }
            val map = HashMap<String, Any>()
            map["inviteCode"] = item.inviteCode
            startTask(apiService.updateDefaultCode(map), Consumer {
                myInviteCodes()
            }, Consumer {

            })
        }


    }


    val itemBinding =
        ItemBinding.of<AgentCodeBean>(BR.item, R.layout.item_invite_code).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<AgentCodeBean> = ObservableArrayList()

    fun myInviteCodes() {
        startTask(apiService.myInviteCodes(), Consumer {
            items.clear()
            for (i in 0 until it.data.size) {
                val bean = it.data[i]
                bean.rateInt = it.data[i].rate.toDouble().toInt()
                items.add(bean)
            }


        }, Consumer {

        });

    }

}