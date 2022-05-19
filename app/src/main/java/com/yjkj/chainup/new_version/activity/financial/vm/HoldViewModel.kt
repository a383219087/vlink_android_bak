package com.yjkj.chainup.new_version.activity.financial.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.MyPos
import com.yjkj.chainup.bean.Pos
import com.yjkj.chainup.common.binding.command.BindingAction
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.net.DataHandler
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding
import java.util.*


class HoldViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()
    //整页数据
    var bean = MutableLiveData<MyPos>()




    interface OnItemListener {
        fun onClick(item:Item)

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item:Item) {
            ARouter.getInstance().build(RoutePath.Holddetail)
                .withSerializable("bean",item.item.value)
                .withInt("queryType",item.queryType.value!!)
                .navigation()
        }



    }
    //列表item
    class Item{
        var item = MutableLiveData<Pos>()
        var queryType = MutableLiveData(1)
    }

    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_position).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()

    var isRefreshing = MutableLiveData(false)
    var isLoadMore = MutableLiveData(false)

    //1持仓中0历史持仓
    var queryType = MutableLiveData(1)

    fun setQuery(type:Int){
        queryType.value=type
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


    fun getList(page:Int) {
        val map = TreeMap<String, String>()
        map["page"] = page.toString()
        map["pageSize"] = "20"
        map["queryType"] = queryType.value.toString()
        startTask(apiService.myPos(toRequestBody(DataHandler.encryptParams(map))), Consumer {
                bean.value=it.data
            if (page == 1) {
                items.clear()
                isRefreshing.value = !isRefreshing.value!!
            } else {
                isLoadMore.value = !isLoadMore.value!!
            }
            if (it.data.posList.isNotEmpty()){
                for (i in it.data.posList.indices) {
                   val item= Item()
                    item.item.value=it.data.posList[i]
                    item.queryType.value=queryType.value
                    items.add(item)

                }
            }


        })
    }

}