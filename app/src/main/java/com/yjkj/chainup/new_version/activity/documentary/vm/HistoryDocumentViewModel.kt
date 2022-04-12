package com.yjkj.chainup.new_version.activity.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import me.tatarka.bindingcollectionadapter2.ItemBinding


class HistoryDocumentViewModel : BaseViewModel() {


    interface OnItemListener {
        fun onClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {

        }
    }


    val itemBinding =
        ItemBinding.of<String>(BR.item, R.layout.item_documentary_single_history).bindExtra(BR.onItemListener, onItemListener)
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