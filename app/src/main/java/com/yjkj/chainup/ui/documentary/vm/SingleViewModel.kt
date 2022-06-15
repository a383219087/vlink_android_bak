package com.yjkj.chainup.ui.documentary.vm


import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import io.reactivex.functions.Consumer


class SingleViewModel : BaseViewModel() {


    //  1是跟单是别人,2是带单是自己

    var status = MutableLiveData<Int>()

    var uid = MutableLiveData<String>()

    var activity = MutableLiveData<FragmentActivity>()

    var index = MutableLiveData(0)
    fun setIndex(i: Int) {
        index.value = i
    }

    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })


    var bean = MutableLiveData<CommissionBean>()

    var onRefreshCommand = BindingCommand<Any>(BindingAction {
        getDetail()
        EventBusUtil.post(MessageEvent(MessageEvent.refresh_MyInviteCodesActivity))
    })

    //发起带单
    fun onClickDocumentary(view: View) {
        EventBusUtil.post(MessageEvent(MessageEvent.DocumentaryActivity_close))

    }

    //查看交易员详情
    fun getDetail() {
        val map = HashMap<String, Any>()
        if ( uid.value.isNullOrEmpty()) {
            map["uid"] = UserDataService.getInstance().userInfo4UserId
        } else {
            map["uid"] = uid.value.toString()
        }
        startTask(apiService.queryTrader(map), Consumer {
            if (uid.value.isNullOrEmpty()){
                bean.value=it.data
            } else{
                bean.value=it.data
            }


        })

    }

}