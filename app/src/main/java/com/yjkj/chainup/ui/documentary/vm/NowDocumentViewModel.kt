package com.yjkj.chainup.ui.documentary.vm


import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel


class NowDocumentViewModel : BaseViewModel() {

    var activity = MutableLiveData<FragmentActivity>()


    //  1是我的跟单2是交易员的带单

    var status = MutableLiveData<Int>()

    //

    var uid = MutableLiveData<String>()





}