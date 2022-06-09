package com.yjkj.chainup.ui.buy.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.BuyInfo
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class CoinsViewModel : BaseViewModel() {

    var fiat = MutableLiveData<String>()

      var type=MutableLiveData(0)

    var item=MutableLiveData<BuyInfo>()

    interface OnItemListener {
        fun onClick(bean: BuyInfo)
    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(bean: BuyInfo) {
           item.value=bean
            type.value= type.value?.plus(1)
            finish()
        }
    }


    val itemBinding = ItemBinding.of<BuyInfo>(BR.item, R.layout.item_cions)
        .bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<BuyInfo> = ObservableArrayList()



    /**
     * 获取币种信息
     */
    fun getData() {
        items.clear()
        startTask(apiService.coinList(), Consumer {
            if (it.data.isEmpty()) {
                return@Consumer
            }
            for (i in it.data.indices) {
                if (fiat.value.isNullOrEmpty()){
                    items.add(it.data[i])
                }else if (fiat.value==it.data[i].fiat){
                    it.data[i].coins?.forEach { it1 ->
                        val info1=BuyInfo()
                        info1.fiat=it1.key
                        if (it1.value!=null){
                            info1.logo= it1.value!!
                        }
                        items.add(info1)
                    }
                }

            }
        })
    }
}