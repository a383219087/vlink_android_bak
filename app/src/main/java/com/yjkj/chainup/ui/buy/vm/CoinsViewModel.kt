package com.yjkj.chainup.ui.buy.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.BuyInfo
import me.tatarka.bindingcollectionadapter2.ItemBinding


class CoinsViewModel : BaseViewModel() {




    interface OnItemListener {
        fun onClick(item: String)
    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: String) {

        }
    }


    val itemBinding = ItemBinding.of<BuyInfo>(BR.item, R.layout.item_cions)
        .bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<BuyInfo> = ObservableArrayList()
}