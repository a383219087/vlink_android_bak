package com.yjkj.chainup.ui.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import me.tatarka.bindingcollectionadapter2.ItemBinding


class MineViewModel : BaseViewModel() {
    var index = MutableLiveData(0)
    fun setIndex(i :Int){
        index.value=i
    }
    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })

    // 显示资产
    var showMoney = MutableLiveData(UserDataService.getInstance().isShowAssets)


    fun setShowMoney() {
        showMoney.value=!showMoney.value!!
        UserDataService.getInstance().setShowAssetStatus(showMoney.value!!)
    }

    interface OnItemListener {
        fun onClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
        }



    }

    //成为交易员
    fun appTraders(){
        ARouter.getInstance().build(RoutePath.ApplyTradersActivity).navigation()
    }

    val itemBinding =
        ItemBinding.of<String>(BR.item, R.layout.item_documentary_mine).bindExtra(BR.onItemListener, onItemListener)
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