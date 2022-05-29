package com.yjkj.chainup.ui.financial.vm

import androidx.databinding.ObservableArrayList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.ProjectBean
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.db.constant.RoutePath
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding
import androidx.databinding.ObservableList as ObservableList1

class AllViewModel : BaseViewModel() {

    var isRefreshing = MutableLiveData(false)

    var activity = MutableLiveData<FragmentActivity>()
    var page = MutableLiveData<Int>()


    interface OnItemListener {
        fun onClick(item: Item)

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: Item) {
            ARouter.getInstance().build(RoutePath.UsdtActivity)
                .withSerializable("bean", item.bean.value)
                .navigation()
        }

    }

    var onRefreshCommand = BindingCommand<Any>(BindingAction {

        getData()
    })

    class Item {
        var bean = MutableLiveData<ProjectBean>()
        var money = MutableLiveData<String>()

    }


    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_all).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList1<Item> = ObservableArrayList()


    fun getData() {
       startTask(apiService.projectList(), Consumer {
            if (it.data.isEmpty()) {
                return@Consumer
            }
           items.clear()
           for (i in it.data!!.indices) {
               val item = Item()
               val bean = it.data[i]
               item.bean.value = bean
               item.money.value = if (bean.projectType == 0 || bean.projectType == 1) {
                   "${bean.buyAmountMin}起存"
               } else {
                   "不限综合"
               }
               when (page.value) {
                   0 -> {
                       items.add(item)
                   }
                   1 -> {
                       if (bean.projectType == 0 || bean.projectType == 1) {
                           items.add(item)
                       }
                   }
                   2 -> {
                       if (bean.projectType == 2 || bean.projectType == 3) {
                           items.add(item)
                       }
                   }
               }


           }

        })



    }


}