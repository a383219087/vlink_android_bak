package com.yjkj.chainup.new_version.activity.financial.vm

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.IncrementActDetail
import com.yjkj.chainup.bean.Pos
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.net.DataHandler
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding
import java.util.*

class HolddetailViewModel:BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()
    var bean = MutableLiveData<Pos>()

     fun onSave() {
         ARouter.getInstance().build(RoutePath.SaveActivity)
             .withString("id",bean.value?.projectId.toString())
             .navigation()

    }
     fun onOut() {
         ARouter.getInstance().build(RoutePath.OutActivity)
             .withSerializable("bean",bean.value)
             .navigation()

    }




    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_holddetail)
    val items: ObservableList<Item> = ObservableArrayList()


    var detailType = MutableLiveData(4)

    var isRefreshing = MutableLiveData(false)
    var isLoadMore = MutableLiveData(false)

    fun setDetailType(type:Int){
        detailType.value=type
        page = 1
        getList(1)
    }
    private var page = 1
    var onRefreshCommand = BindingCommand<Any>(BindingAction {
        page = 1
        getList(1)
    })
    var onLoadMoreCommand = BindingCommand<Any>(BindingAction {
        page++
        getList(page)
    })
    //列表item
    class Item{
        var item = MutableLiveData<IncrementActDetail>()
        var typeString = MutableLiveData<String>()
    }

    fun getList(page:Int) {
        val map = TreeMap<String, String>()
        map["page"] = page.toString()
        map["pageSize"] = "20"
        map["projectId"] = bean.value?.projectId.toString()
        map["detailType"] = detailType.value.toString()
        startTask(apiService.incrementActDetail(toRequestBody(DataHandler.encryptParams(map))), Consumer {
            if (page == 1) {
                items.clear()
                isRefreshing.value = !isRefreshing.value!!
            } else {
                isLoadMore.value = !isLoadMore.value!!
            }
            if (it.data.detailList.isNotEmpty()){
                for (i in it.data.detailList.indices) {
                    val item= Item()
                    item.item.value=it.data.detailList[i]
                    item.typeString.value=when(it.data.detailList[i].type){
                        "gain"->"计息"
                        "apply_0"->"存入"
                        "apply_1"->"赎回"
                        "apply_2"->"赎回中"
                        "apply_3"->"自动存入"
                        else->"计息"
                    }
                    items.add(item)

                }
            }


        }, Consumer {

        });
    }

}