package com.yjkj.chainup.new_version.activity.binary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.ClosePositionDialog
import com.yjkj.chainup.new_version.activity.documentary.ShareDialog
import me.tatarka.bindingcollectionadapter2.ItemBinding


class BinaryViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()

    interface OnItemListener {
        fun onClick()
        fun onShareClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
            ARouter.getInstance().build(RoutePath.OptionsActivity)
                .navigation()
        }

        override fun onShareClick() {
        }

    }


    val itemBinding =
        ItemBinding.of<String>(BR.item, R.layout.item_binary).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<String> = ObservableArrayList()

    override fun onCreate() {
        super.onCreate()

    }

}