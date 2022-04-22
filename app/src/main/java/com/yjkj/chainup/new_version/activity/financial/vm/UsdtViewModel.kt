package com.yjkj.chainup.new_version.activity.financial.vm

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.new_version.activity.documentary.AddMoneyDialog
import com.yjkj.chainup.new_version.activity.documentary.ClosePositionDialog
import com.yjkj.chainup.new_version.activity.documentary.ShareDialog
import com.yjkj.chainup.new_version.activity.documentary.WinAndStopDialog
import me.tatarka.bindingcollectionadapter2.ItemBinding

class UsdtViewModel: BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()

    interface OnItemListener {
        fun onClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {

        }


    }

    fun onShareClick() {
        ShareDialog(). showDialog(activity.value?.supportFragmentManager,"")
    }

    fun onShareClick1() {
        AddMoneyDialog(). showDialog(activity.value?.supportFragmentManager,"")
    }

    fun onShareClick2() {
        WinAndStopDialog(). showDialog(activity.value?.supportFragmentManager,"")
    }

    fun onShareClick3() {
        ClosePositionDialog(). showDialog(activity.value?.supportFragmentManager,"")

    }

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