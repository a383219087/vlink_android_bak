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
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding




class NowDocumentViewModel : BaseViewModel() {

    var activity = MutableLiveData<FragmentActivity>()


    //  1是跟单2是带单

    var type = MutableLiveData<Int>()

    //  1当前0是历史

    var status = MutableLiveData<Int>()

    //

    var uid = MutableLiveData<String>()


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
       //分享
        override fun onShareClick() {
            ShareDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }
        //追加本金
        override fun onShareClick1() {

            AddMoneyDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }


        //止盈止亏
        override fun onShareClick2() {
            WinAndStopDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }

        //平仓
        override fun onShareClick3() {
            ClosePositionDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }
    }


     class Item{

         //  1是跟单2是带单

         var type = MutableLiveData<Int>()

         //  1当前0是历史

         var status = MutableLiveData<Int>()

     }

    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_documentary_single_now).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()



    fun getList() {
        val map = HashMap<String, Any>()
        map["status"] = status.value.toString()
        map["traderUid"] =uid.value.toString()

        startTask(apiService.traderUserList(map), Consumer {

            if (it.data.isNullOrEmpty()) {
                return@Consumer
            }
            for (i in it.data.indices) {
                val item = Item()
                item.status.value=status.value
                items.add(item)
            }

        })

    }


}