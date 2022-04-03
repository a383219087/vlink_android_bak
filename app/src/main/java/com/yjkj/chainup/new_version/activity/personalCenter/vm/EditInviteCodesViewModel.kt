package com.yjkj.chainup.new_version.activity.personalCenter.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel


class EditInviteCodesViewModel : BaseViewModel() {

    var type = MutableLiveData(1)


    var check = MutableLiveData(0)


    var remark = MutableLiveData("")


    fun checkNum(type:Int){
        check.value=type

    }



}