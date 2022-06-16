package com.yjkj.chainup.ui.documentary.vm


import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.ui.documentary.CreateTradersDialog


class TradersViewModel : BaseViewModel() {

    var activity = MutableLiveData<FragmentActivity>()

    var index = MutableLiveData(0)

    //    申请进度 -1: 未申请; 0: 申请中，1 : 已是交易员, 2: 拒绝

    var status = MutableLiveData<Int>()

    fun setIndex(i: Int) {
        index.value = i
    }

    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })


    var item = MutableLiveData<CommissionBean>()


    //跟单
    fun onClickDocumentary(view: View) {
        CreateTradersDialog().apply {
            val bundle = Bundle()
            bundle.putString("uid", item.value?.uid.toString())
            bundle.putSerializable("bean", item.value)
            bundle.putInt("type", if(item.value?.follow==1)2 else 1)
            this.arguments = bundle
        }
            .showDialog(activity.value?.supportFragmentManager, "")

    }

}