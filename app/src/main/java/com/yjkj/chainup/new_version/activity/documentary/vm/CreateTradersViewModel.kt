package com.yjkj.chainup.new_version.activity.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel


class CreateTradersViewModel : BaseViewModel() {


    var checkIndex = MutableLiveData(0)


    //跟单金额
    var documentaryMoney = MutableLiveData("")

    //保证金使用上限
    var maxEarnestMoney = MutableLiveData("")

    //跟单必留
    var documentaryRate = MutableLiveData("")

    //止盈比例
    var winRate = MutableLiveData("")

    //止损
    var stopRate = MutableLiveData("")


    fun setCheckIndex(type: Int) {
        checkIndex.value = type

    }


    fun setWinRate(rate:Int) {
        winRate.value=rate.toString()

    }

    fun setStopRate(rate:Int) {
        stopRate.value=rate.toString()

    }


}