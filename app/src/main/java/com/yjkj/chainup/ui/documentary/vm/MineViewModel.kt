package com.yjkj.chainup.ui.documentary.vm


import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.StatisticsBean
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.ui.documentary.ApplyTradersDialog
import io.reactivex.functions.Consumer


class MineViewModel : BaseViewModel() {

    var bean = MutableLiveData<StatisticsBean>()

    var context = MutableLiveData<FragmentActivity>()
    var index = MutableLiveData(0)
    fun setIndex(i :Int){
        index.value=i
    }
    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })

    // 显示资产
    var showMoney = MutableLiveData(UserDataService.getInstance().isShowAssets)

    // 显示法币转换
    var cnyString = MutableLiveData("")


    fun setShowMoney() {
        showMoney.value=!showMoney.value!!
        UserDataService.getInstance().setShowAssetStatus(showMoney.value!!)
    }



    //成为交易员
    fun appTraders(){
        val map = HashMap<String, Any>()
        map["traderUid"] = UserDataService.getInstance().userInfo4UserId
        startTask(contractApiService.checkFuturesUser(map), Consumer {
            if (it.data.result=="1"){
                ApplyTradersDialog().showDialog(context.value?.supportFragmentManager, "")
            }else{
                EventBusUtil.post(MessageEvent(MessageEvent.DocumentaryActivity_close))
            }

        })
    }

    fun getData1() {
        startTask(apiService.myStatistics(), Consumer {
            bean.value=it.data
            cnyString.value =RateManager.getHomeCNYByCoinName("USDT", it.data.followTotalAmount, isOnlyResult = true)
        })

    }



}