package com.yjkj.chainup.ui.documentary.vm


import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer


class TradersViewModel : BaseViewModel() {

    var activity = MutableLiveData<FragmentActivity>()

    var index = MutableLiveData(0)



    fun setIndex(i: Int) {
        index.value = i
    }

    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })


    var item = MutableLiveData<CommissionBean>()




}