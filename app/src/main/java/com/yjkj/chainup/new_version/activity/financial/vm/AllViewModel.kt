package com.yjkj.chainup.new_version.activity.financial.vm

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.db.constant.RoutePath
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding

class AllViewModel : BaseViewModel(){

    var activity = MutableLiveData<FragmentActivity>()
    var page = MutableLiveData<Int>()

    var list = MutableLiveData<List<String>>()


    interface OnItemListener {
        fun onClick()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
            ARouter.getInstance().build(RoutePath.UsdtActivity)
                .navigation()
        }

    }



    val itemBinding =
        ItemBinding.of<String>(BR.item, R.layout.item_all).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<String> = ObservableArrayList()



     fun getList(){
         startTask(apiService.projectList(), Consumer {


         }, Consumer {

         });
     }



}