package com.yjkj.chainup.new_version.activity.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.data.ContractTicker
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.TraderPositionBean
import com.yjkj.chainup.contract.extension.showQuoteName


class ClosePositionViewModel : BaseViewModel() {

    var bean = MutableLiveData<TraderPositionBean>()
    var priceUnit = MutableLiveData("")
    var price = MutableLiveData("")







     fun getData(){
         val contract: Contract? = ContractPublicDataAgent.getContract(bean.value!!.contractId)
         contract?.let {
             priceUnit.value=it.showQuoteName()

         }

         //价格 默认显示最新价格
         val ticker: ContractTicker? = ContractPublicDataAgent.getContractTicker(bean.value!!.contractId)
         if (ticker != null) {
             price.value=ticker.last_px
         }

         val contractPosition: ContractPosition? = ContractUserDataAgent.getContractPosition(bean.value!!.contractId)
//
//         val allQty = MathHelper.round(info.cur_qty, 0)
//         val maxClosePosVol = MathHelper.round(MathHelper.sub(info.cur_qty, info.freeze_qty), 0)
//         etVolume.setText("${allQty.toInt()}")

     }



}