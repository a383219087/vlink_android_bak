package com.yjkj.chainup.ui.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.common.sdk.LibCore.context
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.functions.Consumer


class ApplyTradersViewModel : BaseViewModel() {


  //    申请进度 -1: 未申请; 0: 申请中，1 : 已是交易员, 2: 拒绝

    var statusString = MutableLiveData(context.getString(R.string.traders_apply_text3))
    var status = MutableLiveData(-1)


    fun currentStatus() {
        startTask(apiService.currentStatus(), Consumer {
            status.value=it.data.status
            statusString.value=when(it.data.status)  {
                0->context.getString(R.string.traders_apply_text4)
                1->context.getString(R.string.traders_apply_text5)
                2->context.getString(R.string.traders_apply_text6)
                else->context.getString(R.string.traders_apply_text7)
            }

        })

    }



    fun applyCurrentStatus() {
        if (status.value==-1){
            startTask(apiService.applyBecomeTrader(), Consumer {
                ToastUtils.showToast(context.getString(R.string.traders_apply_text8))
                finish()
            })
        }else{
            finish()
        }


    }









}