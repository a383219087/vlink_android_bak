package com.yjkj.chainup.new_version.activity.binary.vm

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.new_version.activity.documentary.ClosePositionDialog
import com.yjkj.chainup.new_version.activity.documentary.ShareDialog
import me.tatarka.bindingcollectionadapter2.ItemBinding

class CurrentViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()

    interface OnItemListener {
        fun onClick()
        fun onShareClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
            ClosePositionDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }

        override fun onShareClick() {
            ShareDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }
    }


    val itemBinding =
        ItemBinding.of<String>(BR.item, R.layout.item_current).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<String> = ObservableArrayList()


}