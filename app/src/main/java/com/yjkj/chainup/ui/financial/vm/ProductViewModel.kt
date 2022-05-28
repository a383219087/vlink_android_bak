package com.yjkj.chainup.ui.financial.vm



import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import io.reactivex.functions.Consumer


open class ProductViewModel : BaseViewModel() {

    var index = MutableLiveData(0)
    fun setIndex(i :Int){
        index.value=i
    }
    var onPageChangeListener = BindingCommand(BindingConsumer<Int> { setIndex(it) })

    val notice= MutableLiveData<String>("活期存款次日16点(UTC+8)发放利息，定期存款到期归还本金并发放利息")



    fun getData() {
        startTask(apiService.projectIndex(), Consumer {
            notice.value=it.data.detail
        })



    }





}