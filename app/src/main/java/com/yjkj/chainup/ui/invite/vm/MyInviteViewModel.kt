package com.yjkj.chainup.ui.invite.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer


class MyInviteViewModel : BaseViewModel() {


    var index = MutableLiveData(0)
    fun setIndex(i: Int) {
        index.value = i
    }
    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })
}
