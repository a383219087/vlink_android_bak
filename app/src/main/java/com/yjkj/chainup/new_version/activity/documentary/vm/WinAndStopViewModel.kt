package com.yjkj.chainup.new_version.activity.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel


class WinAndStopViewModel : BaseViewModel() {

    var checkIndex = MutableLiveData(0)

    fun setCheckIndex(type: Int) {
        checkIndex.value = type

    }
}