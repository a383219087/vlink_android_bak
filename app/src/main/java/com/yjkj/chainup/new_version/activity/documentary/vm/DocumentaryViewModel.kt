package com.yjkj.chainup.new_version.activity.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import io.reactivex.functions.Consumer


class DocumentaryViewModel : BaseViewModel() {

    //    申请进度 -1: 未申请; 0: 申请中，1 : 已是交易员, 2: 拒绝

    var status = MutableLiveData<Int>()



    fun currentStatus() {


    }



}