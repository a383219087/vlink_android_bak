package com.yjkj.chainup.ui.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.chainup.contract.bean.CpContractPositionBean
import com.yjkj.chainup.base.BaseViewModel


class ShareViewModel : BaseViewModel() {

    var bean = MutableLiveData<CpContractPositionBean>()

}