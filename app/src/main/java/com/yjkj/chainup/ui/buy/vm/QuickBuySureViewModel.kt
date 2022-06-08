package com.yjkj.chainup.ui.buy.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel


class QuickBuySureViewModel : BaseViewModel() {



    var index = MutableLiveData(2)


    fun setIndex(i:Int){
        index.value=i
    }






    /**
     * g购买记录
     */
    fun onclickRightIcon(){

    }

}