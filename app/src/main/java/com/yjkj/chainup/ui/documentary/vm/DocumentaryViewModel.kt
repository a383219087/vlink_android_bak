package com.yjkj.chainup.ui.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel


class DocumentaryViewModel : BaseViewModel() {

    //    申请进度 -1: 未申请; 0: 申请中，1 : 已是交易员, 2: 拒绝

    var status = MutableLiveData<Int>()



    var index = MutableLiveData<Int>()





    fun setIndex(int: Int) {
        index.value=int
    }




}