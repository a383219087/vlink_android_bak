package com.yjkj.chainup.new_version.activity.documentary.vm


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




class NowDocumentViewModel : BaseViewModel() {

    var activity = MutableLiveData<FragmentActivity>()


    interface OnItemListener {
        fun onClick()
        fun onShareClick()
        fun onShareClick1()
        fun onShareClick2()
        fun onShareClick3()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
            ARouter.getInstance().build(RoutePath.DocumentaryDetailActivity).navigation()
        }

        override fun onShareClick() {
            ShareDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }

        override fun onShareClick1() {

            AddMoneyDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }

        override fun onShareClick2() {
            WinAndStopDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }

        override fun onShareClick3() {
            ClosePositionDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }
    }


    val itemBinding =
        ItemBinding.of<String>(BR.item, R.layout.item_documentary_single_now).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<String> = ObservableArrayList()

    override fun onCreate() {
        super.onCreate()
        items.add("")
        items.add("")
        items.add("")
        items.add("")
        items.add("")
    }
}