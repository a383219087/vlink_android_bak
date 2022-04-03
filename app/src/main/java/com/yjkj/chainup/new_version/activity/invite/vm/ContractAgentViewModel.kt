package com.yjkj.chainup.new_version.activity.invite.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil

import io.reactivex.functions.Consumer



class ContractAgentViewModel : BaseViewModel() {


    val codeList: ObservableList<AgentCodeBean> = ObservableArrayList()
    var rate = MutableLiveData(0)
    var code = MutableLiveData("")


    fun myInviteCodes() {
        startTask(apiService.myInviteCodes(), Consumer{
            codeList.clear()
            codeList.addAll(it.data)
            if (it.data.isNotEmpty()){
                rate.value= codeList[0].rate.toInt()
                code.value=codeList[0].inviteCode
            }

        }, Consumer {

        });

    }


    fun toMyInviteCodesActivity() {
        ArouterUtil.navigation(RoutePath.MyInviteCodesActivity, null)
    }


}