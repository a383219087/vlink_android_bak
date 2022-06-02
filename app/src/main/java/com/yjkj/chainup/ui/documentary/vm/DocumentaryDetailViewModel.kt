package com.yjkj.chainup.ui.documentary.vm


import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.ui.activity.CpContractStopRateLossActivity
import com.common.sdk.LibCore.context
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.new_contract.activity.ClHoldShareActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.ui.documentary.ClosePositionDialog
import com.yjkj.chainup.util.LanguageUtil
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
         ClHoldShareActivity.show(activity.value!!, bean.value!!)

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
        val orderSide = if (bean.value?.orderSide == "BUY") {
            context.getString(R.string.cl_HistoricalPosition_1) + "-"
        } else {
            context.getString(R.string.cl_HistoricalPosition_2) + "-"
        }
        val positionType = if (bean.value?.positionType== 1) {
            context.getString(R.string.cl_currentsymbol_marginmodel1) + "-"
        } else {
            context.getString(R.string.cl_currentsymbol_marginmodel2) + "-"
        }
        contractType.value ="${orderSide}${positionType}${bean.value?.leverageLevel}X"


    }

}