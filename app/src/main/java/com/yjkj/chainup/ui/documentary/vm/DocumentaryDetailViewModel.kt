package com.yjkj.chainup.ui.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.FollowerStatisticsBean
import com.yjkj.chainup.util.DecimalUtil
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding
import java.math.BigDecimal


class DocumentaryDetailViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()


    var bean = MutableLiveData<CpContractPositionBean>()


    var id = MutableLiveData<String>()
    var contractId = MutableLiveData(0)









    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_documentary_detail_record)
    val items: ObservableList<Item> = ObservableArrayList()



    fun getData(){

        record()

    }
    class Item{
        var bean = MutableLiveData<FollowerStatisticsBean>()
        var type = MutableLiveData(1)
        var tradeTime = MutableLiveData<String>()
        var title = MutableLiveData("")
        var amount = MutableLiveData("")
        var entrustAmountKey = MutableLiveData("")

        var entrustPrice = MutableLiveData("")
        var dealPrice = MutableLiveData("")
        var dealAmount = MutableLiveData("")
        var dealAmountKey = MutableLiveData("")


    }


    //操作记录
    fun record() {
        val context=activity.value

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
                if (it.data[i].transaction!=null){
                    it.data[i].transaction?.let {it1->
                        item.title.value=it1.meta
                        item.amount.value= DecimalUtil.cutValueByPrecision(it1.amount,2)
                        item.entrustAmountKey.value= context?.getString(R.string.journalAccount_text_amount)
                    }
                }else{
                    it.data[i].coOrder?.let {it1->
                        item.title.value= when (it1.status) {
                            "2" -> context?.getString(com.chainup.contract.R.string.cp_extra_text1)//完全成交
                            "3" -> context?.getString(com.chainup.contract.R.string.cp_status_text5)//"部分成交"
                            "4" -> context?.getString(com.chainup.contract.R.string.cp_status_text2)//"已撤销"
                            "5" -> context?.getString(com.chainup.contract.R.string.cp_status_text4)//"待撤销"
                            "6" -> context?.getString(com.chainup.contract.R.string.cp_status_text3)//"异常订单"
                            else -> "error"
                        }
                        //合约面值
                        val multiplier = CpClLogicContractSetting.getContractMultiplierById(context, contractId.value!!)
                        //合约面值单位
                        val multiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinById(context, contractId.value!!)
                        val multiplierBuff = BigDecimal(multiplier).stripTrailingZeros().toPlainString()
                        val mSymbolPricePrecision = CpClLogicContractSetting.getContractSymbolPricePrecisionById(context, contractId.value!!)
                        //面值精度
                        val multiplierPrecision = if (multiplierBuff.contains(".")) {
                            val index = multiplierBuff.indexOf(".")
                            if (index < 0) 0 else multiplierBuff.length - index - 1
                        } else {
                            multiplierBuff.length
                        }
                        val coUnit = CpClLogicContractSetting.getContractUint(context)
                        val showDealUnit: String = if (coUnit == 0) {
                            "(${context?.getString(com.chainup.contract.R.string.cp_overview_text9)})"
                        } else {
                            "($multiplierCoin)"
                        }
                        if (it1.open == "OPEN" && it1.type == "2") {
                            item.entrustAmountKey.value=context?.getString(com.chainup.contract.R.string.cp_extra_text9)
                            item.amount.value= DecimalUtil.cutValueByPrecision(it1.volume,2)
                        } else {
                            item.entrustAmountKey.value=context?.getString(com.chainup.contract.R.string.cp_order_text66)+ showDealUnit
                            item.amount.value= if (coUnit == 0) it1.volume else CpBigDecimalUtils.mulStr(it1.volume, multiplier, multiplierPrecision)
                        }
                        item.entrustPrice.value=if (it1.type.equals("2")) context?.getString(com.chainup.contract.R.string.cp_overview_text53) else CpBigDecimalUtils.showSNormal(it1.price,multiplierPrecision)
                        item.dealPrice.value=CpBigDecimalUtils.showSNormal(it1.avgPrice, mSymbolPricePrecision)
                        item.dealAmount.value= if (coUnit == 0) it1.dealVolume else CpBigDecimalUtils.mulStr(it1.dealVolume, multiplier, multiplierPrecision)
                        item.dealAmountKey.value= context?.getString(com.chainup.contract.R.string.cp_extra_text8) + showDealUnit
                    }
                }


                items.add(item)

            }


        })

    }

}