package com.yjkj.chainup.new_version.activity.financial.vm

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import me.tatarka.bindingcollectionadapter2.ItemBinding

class SaveViewModel:BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()

    interface OnItemListener {
        fun onClick()
        fun onSave()
        fun onOut()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
        }

        override fun onSave() {

        }
        override fun onOut() {

        }

    }




    override fun onCreate() {
        super.onCreate()

    }
}