package com.yjkj.chainup.new_version.activity.documentary.vm


import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.new_version.activity.documentary.CreateTradersDialog


class SingleViewModel : BaseViewModel() {


    //  1是跟单2是带单

    var status = MutableLiveData<Int>()

    var activity = MutableLiveData<FragmentActivity>()

    var index = MutableLiveData(0)
    fun setIndex(i: Int) {
        index.value = i
    }

    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })


    var item = MutableLiveData<CommissionBean>()

    //发起带单
    fun onClickDocumentary(view: View) {
        EventBusUtil.post(MessageEvent(MessageEvent.DocumentaryActivity_close))

    }


}