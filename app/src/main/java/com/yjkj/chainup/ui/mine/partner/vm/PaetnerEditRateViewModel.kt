package com.yjkj.chainup.ui.mine.partner.vm



import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ToastUtils
import com.common.sdk.LibCore
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.bean.InviteBean
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import io.reactivex.functions.Consumer


class PaetnerEditRateViewModel : BaseViewModel() {

    var bean = MutableLiveData<InviteBean>()


    var remark = MutableLiveData("")

    var rate = MutableLiveData("0")



    var inviteCode = MutableLiveData("")




    fun agentRoles() {
        initData()

    }


    fun initData() {
        remark.value = bean.value?.remark
        rate.value = bean.value?.rate?.toDoubleOrNull()?.toInt().toString()
        inviteCode.value = bean.value?.inviteCode

    }

    fun onSure() {
         if (rate.value==null||rate.value!!.isEmpty()){
             ToastUtils.showShort(LibCore.context.getString(R.string.traders_apply_text81))
             return
         }
         if (bean.value?.rate?.toDoubleOrNull()!! > rate.value!!.toDoubleOrNull()!!){
             ToastUtils.showShort(LibCore.context.getString(R.string.traders_apply_text83))
             return
         }

         if(rate.value?.toDoubleOrNull()?.toInt()!!<=0||rate.value?.toDoubleOrNull()?.toInt()!!>=100){
             ToastUtils.showShort(LibCore.context.getString(R.string.traders_apply_text82))
             return
         }

        val map = HashMap<String, Any>()
        map["rate"] = rate.value.toString()
        map["remark"] = remark.value.toString()
        map["inviteCode"] = inviteCode.value.toString()
        startTask(apiService.updateDefaultCode(map), Consumer {
            EventBusUtil.post(MessageEvent(MessageEvent.refresh_MyInviteCodesActivity))
            finish()
        })



    }
}