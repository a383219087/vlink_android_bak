package com.yjkj.chainup.ui.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel


class WinAndStopViewModel : BaseViewModel() {

    var checkIndex = MutableLiveData(0)

    fun setCheckIndex(type: Int) {
        checkIndex.value = type

    }
}