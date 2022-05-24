package com.yjkj.chainup.ui.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.db.service.UserDataService
import me.tatarka.bindingcollectionadapter2.ItemBinding


class SingleMoneyViewModel : BaseViewModel() {


    var index = MutableLiveData(0)
    fun setIndex(i :Int){
        index.value=i
    }



    // 显示资产
    var showMoney = MutableLiveData(UserDataService.getInstance().isShowAssets)


    fun setShowMoney() {
        showMoney.value=!showMoney.value!!
        UserDataService.getInstance().setShowAssetStatus(showMoney.value!!)
    }

    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })

    interface OnItemListener {
        fun onClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
        }



    }

    class Item{
        var index = MutableLiveData(0)
    }


    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_single_money).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()


    fun getList(){
        var item=Item()
        item.index.value=0
        items.add(item)
        var item1=Item()
        item1.index.value=1
        items.add(item1)

    }

}