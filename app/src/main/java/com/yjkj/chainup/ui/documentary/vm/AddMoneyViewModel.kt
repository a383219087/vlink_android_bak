package com.yjkj.chainup.ui.documentary.vm


import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.TraderPositionBean


class AddMoneyViewModel : BaseViewModel() {

    var bean = MutableLiveData<TraderPositionBean>()


    var context= MutableLiveData<Context>()




    fun  getData(mActivity: Context){
        context.value=mActivity
//        mPricePrecision = LogicContractSetting.getContractSymbolPricePrecisionById(mActivity, bean.value?.contractId!!)
//        mContractJson = LogicContractSetting.getContractJsonStrById(mActivity, bean.value?.contractId!!)
    }
}