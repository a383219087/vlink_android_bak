package com.yjkj.chainup.new_version.activity.financial.vm

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import me.tatarka.bindingcollectionadapter2.ItemBinding

class AllViewModel : BaseViewModel(){

    var activity = MutableLiveData<FragmentActivity>()
    var page = MutableLiveData<Int>()




    val itemBinding =
        ItemBinding.of<String>(BR.item, R.layout.item_all)
    val items: ObservableList<String> = ObservableArrayList()

    override fun onCreate() {
        super.onCreate()
        items.add("")
        items.add("")
        items.add("")
        items.add("")
        items.add("")
        items.add("")
        items.add("")
        items.add("")
    }
}