package com.yjkj.chainup.ui.financial.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel


class FinancialViewModel : BaseViewModel() {



    var index = MutableLiveData<Int>()





    fun setIndex(int: Int) {
        index.value=int
    }

}