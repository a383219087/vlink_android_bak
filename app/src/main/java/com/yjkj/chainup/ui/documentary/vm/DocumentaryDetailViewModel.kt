package com.yjkj.chainup.ui.documentary.vm


import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.ui.activity.CpContractStopRateLossActivity
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.ui.documentary.ClosePositionDialog
import com.yjkj.chainup.ui.documentary.ShareDialog
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class DocumentaryDetailViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()


    var bean = MutableLiveData<CpContractPositionBean>()

    //  1是跟单2是带单

    var type = MutableLiveData<Int>()

    //  1当前0是历史

    var status = MutableLiveData<Int>()


    var  contractType= MutableLiveData<String>()

     fun onShareClick() {
        ShareDialog(). showDialog(activity.value?.supportFragmentManager,"")
    }

     fun onShareClick1() {
         NewDialogUtils.adjustDepositDialog(activity.value!!, JSONObject.toJSON(bean.value) as org.json.JSONObject,
             object : NewDialogUtils.DialogBottomAloneListener {
                 override fun returnContent(content: String) {
                     val map = HashMap<String, Any>()
                     map["amount"] =content
                     map["contractId"] =bean.value?.contractId!!
                     map["positionId"] =bean.value?.id!!
                     startTask(contractApiService.transferMargin4Contract1(toRequestBody(map)), Consumer {
                         NToastUtil.showTopToastNet(activity.value!!,true, LanguageUtil.getString(activity.value!!, "contract_modify_the_success"))

                     })
                 }
             })
    }

     fun onShareClick2() {
         CpContractStopRateLossActivity.show(activity.value!!,bean.value!!)
    }

     fun onShareClick3() {
         ClosePositionDialog().apply {
             val bundle = Bundle()
             bundle.putSerializable("bean", bean.value)
             this.arguments = bundle

         }. showDialog(activity.value?.supportFragmentManager,"")

    }

    val itemBinding =
        ItemBinding.of<String>(BR.item, R.layout.item_documentary_detail_record)
    val items: ObservableList<String> = ObservableArrayList()



    fun getData(){

        if (bean.value?.orderSide=="BUY"){
            contractType.value="多仓-"+bean.value?.leverageLevel+"X"

        }else{
            contractType.value="空仓-"+bean.value?.leverageLevel+"X"
        }


    }

}