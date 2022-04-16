package com.yjkj.chainup.new_version.activity.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.CommissionBean
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class MyTradersModel : BaseViewModel() {
    interface OnItemListener {
        fun onClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {

        }
    }


    val itemBinding =
        ItemBinding.of<CommissionBean>(BR.item, R.layout.item_my_traders).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<CommissionBean> = ObservableArrayList()

    fun getList() {
        startTask(apiService.myTraders(), Consumer {
            if (it.data.isNullOrEmpty()) {
                return@Consumer
            }
            items.addAll(it.data)

        }, Consumer {

        });

    }

}