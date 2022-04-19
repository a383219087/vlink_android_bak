package com.yjkj.chainup.new_version.activity.documentary.vm


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


class DocumentaryDetailViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()



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
        ItemBinding.of<String>(BR.item, R.layout.item_documentary_detail_record)
    val items: ObservableList<String> = ObservableArrayList()
}