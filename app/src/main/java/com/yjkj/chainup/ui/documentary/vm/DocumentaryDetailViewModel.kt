package com.yjkj.chainup.ui.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.chainup.contract.bean.CpContractPositionBean
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.FollowerStatisticsBean
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class DocumentaryDetailViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()


    var bean = MutableLiveData<CpContractPositionBean>()


    var id = MutableLiveData<String>()





    class Item{
        var bean = MutableLiveData<FollowerStatisticsBean>()
        var tradeTime = MutableLiveData<String>()
    }



    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_documentary_detail_record)
    val items: ObservableList<Item> = ObservableArrayList()



    fun getData(){
        record()

    }


    //操作记录
    fun record() {
        items.clear()
        val map = HashMap<String, Any>()
        map["positionId"] = id.value.toString()
        startTask(contractApiService.positionLog(map), Consumer {
            if (it.data.isEmpty()){
                return@Consumer
            }
            for (i in 0 until it.data.size) {
                val item=Item()
                item.bean.value=it.data[i]
                item.tradeTime.value=it.data[i].tradeTime.replace("T"," ")
                items.add(item)

            }


        })

    }

}