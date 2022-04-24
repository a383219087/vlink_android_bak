package com.yjkj.chainup.new_version.activity.financial.vm

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.AddMoneyDialog
import com.yjkj.chainup.new_version.activity.documentary.ClosePositionDialog
import com.yjkj.chainup.new_version.activity.documentary.ShareDialog
import com.yjkj.chainup.new_version.activity.documentary.WinAndStopDialog
import me.tatarka.bindingcollectionadapter2.ItemBinding

class AllViewModel : BaseViewModel(){

    var activity = MutableLiveData<FragmentActivity>()
    var page = MutableLiveData<Int>()
    interface OnItemListener {
        fun onClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
            ARouter.getInstance().build(RoutePath.UsdtActivity)
                .navigation()
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
        ItemBinding.of<String>(BR.item, R.layout.item_all).bindExtra(BR.onItemListener, onItemListener)
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