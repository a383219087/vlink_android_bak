package com.yjkj.chainup.new_version.activity.invite.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.bean.InviteBean
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil

import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class ContractAgentViewModel : BaseViewModel() {


    val codeList: ObservableList<AgentCodeBean> = ObservableArrayList()
    var rate = MutableLiveData(0)
    var code = MutableLiveData("")
    var bean = MutableLiveData<AgentCodeBean>()
    var myBonusBean = MutableLiveData<InviteBean>()

    val itemBinding = ItemBinding.of<InviteBean>(BR.item, R.layout.item_invite_rank)
    val items: ObservableList<InviteBean> = ObservableArrayList()


    fun myInviteCodes() {
        startTask(apiService.myInviteCodes(), Consumer {
            codeList.clear()
            codeList.addAll(it.data)
            bean.value = it.data.first { it.isDefault == "1" }
            bean.value!!.rateInt = bean.value!!.rate.toDouble().toInt()
            rate.value = bean.value!!.rate.toDouble().toInt()
            code.value = bean.value!!.inviteCode

        }, Consumer {

        })

    }

    fun myBonus() {
        startTask(apiService.myBonus(), Consumer {
            myBonusBean.value = it.data
        }, Consumer {

        })

    }

    fun top10() {
        startTask(apiService.top10(), Consumer {
            items.clear()
            for (i in 0 until it.data.size) {
                val bean = it.data[i]
                bean.index = i
                items.add(bean)
            }

        }, Consumer {

        })

    }


    fun toMyInviteCodesActivity() {
        ArouterUtil.navigation(RoutePath.MyInviteCodesActivity, null)
    }

    fun onEditClick() {
        ARouter.getInstance().build(RoutePath.EditInviteCodesActivity)
            .withInt("type", 2)
            .withSerializable("bean", bean.value)
            .navigation()
    }

}