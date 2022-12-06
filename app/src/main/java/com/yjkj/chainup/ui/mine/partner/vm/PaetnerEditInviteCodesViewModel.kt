package com.yjkj.chainup.ui.mine.partner.vm



import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ToastUtils
import com.common.sdk.LibCore
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import io.reactivex.functions.Consumer


class PaetnerEditInviteCodesViewModel : BaseViewModel() {
    var conetxt = MutableLiveData<Context>()


    var type = MutableLiveData(1)
    var bean = MutableLiveData<AgentCodeBean>()


    var remark = MutableLiveData("")

    var rate = MutableLiveData("0")

    var isCheck = MutableLiveData(false)


    var inviteCode = MutableLiveData("")

    var myRate = MutableLiveData("0")


    fun agentRoles() {
        if (type.value == 2) {
            initData()
        }
        startTask(apiService.parentRate()) {
            if (it.data != null)
                myRate.value = it.data
        }

    }


    fun initData() {
        remark.value = bean.value?.remark
        rate.value = bean.value?.rate?.toDoubleOrNull()?.toInt().toString()
        inviteCode.value = bean.value?.inviteCode
        isCheck.value = bean.value?.isDefault == "1"
    }

    fun onSure() {
        if (rate.value == null || rate.value!!.isEmpty()) {
            ToastUtils.showShort(conetxt.value?.getString(R.string.traders_apply_text81))
            return
        }

        if (rate.value?.toDoubleOrNull()?.toInt()!! <= 0 || rate.value?.toDoubleOrNull()
                ?.toInt()!! >= myRate.value?.toDoubleOrNull()?.toInt()!!
        ) {
            ToastUtils.showShort(conetxt.value?.getString(R.string.traders_apply_text82))
            return
        }

        if (inviteCode.value?.isEmpty()!!) {
            val map = HashMap<String, Any>()
            map["rate"] = rate.value!!.toInt().toString()
            map["remark"] = remark.value.toString()
            map["isDefault"] = if (isCheck.value == true) "1" else "0"
            startTask(apiService.createInviteCode(map), Consumer {
                EventBusUtil.post(MessageEvent(MessageEvent.refresh_MyInviteCodesActivity))
                finish()
            })

        } else {
            val map = HashMap<String, Any>()
            map["rate"] = rate.value.toString()
            map["remark"] = remark.value.toString()
            map["inviteCode"] = inviteCode.value.toString()
            map["isDefault"] = if (isCheck.value == true) "1" else "0"
            startTask(apiService.updateDefaultCode(map), Consumer {
                EventBusUtil.post(MessageEvent(MessageEvent.refresh_MyInviteCodesActivity))
                finish()
            })
        }

    }
}