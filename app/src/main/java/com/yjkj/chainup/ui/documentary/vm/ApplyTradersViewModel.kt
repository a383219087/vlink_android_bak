package com.yjkj.chainup.ui.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.functions.Consumer


class ApplyTradersViewModel : BaseViewModel() {


  //    申请进度 -1: 未申请; 0: 申请中，1 : 已是交易员, 2: 拒绝

    var statusString = MutableLiveData("未申请")
    var status = MutableLiveData(-1)


    fun currentStatus() {
        startTask(apiService.currentStatus(), Consumer {
            status.value=it.data.status
            statusString.value=when(it.data.status)  {
                0->"资料正在审核中"
                1->"您已是交易员"
                2->"已拒绝了你的申请"
                else->"您未申请成为交易员"
            }

        })

    }



    fun applyCurrentStatus() {
        startTask(apiService.applyBecomeTrader(), Consumer {
            ToastUtils.showToast("申请成功")
            currentStatus()


        })

    }









}