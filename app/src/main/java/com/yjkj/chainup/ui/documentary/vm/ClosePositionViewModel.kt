package com.yjkj.chainup.ui.documentary.vm


import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.utils.CpClLogicContractSetting
import com.yjkj.chainup.base.BaseViewModel
import io.reactivex.functions.Consumer
import java.util.*


class ClosePositionViewModel : BaseViewModel() {

    var bean = MutableLiveData<CpContractPositionBean>()

    var contractName= MutableLiveData<String>()

    var context= MutableLiveData<Context>()








     fun getData(c:Context){
         context.value=c
         contractName.value= CpClLogicContractSetting.getContractShowNameById(c, bean.value!!.contractId)

     }



    fun onClick(){

        val map = TreeMap<String, Any>()
        map["contractId"] = bean.value!!.contractId.toString()
        map["positionType"] =  bean.value!!.positionType.toString()
        map["open"] =  "CLOSE"
        map["side"] = if (bean.value!!.side == "BUY") "SELL" else "BUY"
        map["type"] = "1"
        map["leverageLevel"] =  bean.value!!.leverageLevel.toString()
        map["price"] =  bean.value!!.closeProfit.toString()
        map["volume"] =  bean.value!!.volume.toString()
        map["isConditionOrder"] = false
        map["triggerPrice"] =  ""
        map["expireTime"] =   CpClLogicContractSetting.getStrategyEffectTimeStr(context.value)
        map["isOto"] = false //是否OTO订单
        map["takerProfitTrigger"] = "" //止盈触发价格
        map["takerProfitPrice"] = "0" //止盈委托价格
        map["takerProfitType"] = "2" //止盈类型
        map["stopLossTrigger"] = "" //止损触发价格
        map["stopLossPrice"] = "0" //止损委托价格
        map["stopLossType"] = "2" //止损类型
        map["priceType"] = ""
        startTask(cpContractApiService.createOrder(toRequestBody(map)), Consumer {
          finish()
        })

    }



}