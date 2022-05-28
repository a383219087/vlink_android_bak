package com.yjkj.chainup.ui.documentary.vm


import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.ui.documentary.ApplyTradersDialog


class MineViewModel : BaseViewModel() {



    var context = MutableLiveData<FragmentActivity>()
    var index = MutableLiveData(0)
    fun setIndex(i :Int){
        index.value=i
    }
    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })

    // 显示资产
    var showMoney = MutableLiveData(UserDataService.getInstance().isShowAssets)


    fun setShowMoney() {
        showMoney.value=!showMoney.value!!
        UserDataService.getInstance().setShowAssetStatus(showMoney.value!!)
    }



    //成为交易员
    fun appTraders(){
        ApplyTradersDialog().showDialog(context.value?.supportFragmentManager,"")
    }




}