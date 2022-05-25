package com.yjkj.chainup.ui.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.chainup.contract.bean.CpContractPositionBean
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.InviteCodeBean
import io.reactivex.functions.Consumer


class ShareViewModel : BaseViewModel() {

    var bean = MutableLiveData<CpContractPositionBean>()

    var contractType = MutableLiveData<String>()

    //  1当前0是历史

    var status = MutableLiveData<Int>()




    var inviteCodeBean = MutableLiveData<InviteCodeBean>()



    fun getData(){
        if (bean.value?.orderSide == "BUY") {
            contractType.value = "多仓-" + bean.value?.leverageLevel + "X"
        } else {
            contractType.value = "空仓-" + bean.value?.leverageLevel + "X"
        }
    }

    fun userDefaultInviteCode(){
        val map = HashMap<String, Any>()
        map["uid"] = bean.value?.uid.toString()

        startTask(apiService.userDefaultInviteCode(map), Consumer {

            inviteCodeBean.value=it.data
        })
    }
}