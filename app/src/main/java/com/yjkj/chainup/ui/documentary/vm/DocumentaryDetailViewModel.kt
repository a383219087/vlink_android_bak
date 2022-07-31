package com.yjkj.chainup.ui.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.ui.activity.CpContractStopRateLossActivity
import com.common.sdk.LibCore.context
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.FollowerStatisticsBean
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.new_contract.activity.ClHoldShareActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class DocumentaryDetailViewModel : BaseViewModel() {
    var activity = MutableLiveData<FragmentActivity>()


    var bean = MutableLiveData<CpContractPositionBean>()


    //  1当前0是历史

    var status = MutableLiveData<Int>()


    var  contractType= MutableLiveData<String>()


    //收益lv
    var returnRate = MutableLiveData<String>()

    //收益
    var revenue = MutableLiveData<String>()



    class Item{
        var bean = MutableLiveData<FollowerStatisticsBean>()

    }



    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_documentary_detail_record)
    val items: ObservableList<Item> = ObservableArrayList()



    fun getData(){
        record()
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
        //回报率
        returnRate.value = NumberUtil.getDecimal(2).format(
            MathHelper.round(MathHelper.mul(bean.value?.returnRate?.let {"0" }, "100"), 2)
        ).toString() + "%"
        val mMarginCoinPrecision =
            LogicContractSetting.getContractMarginCoinPrecisionById(context, bean.value!!.contractId)
        revenue.value =
            BigDecimalUtils.showSNormal(bean.value!!.profitRealizedAmount, mMarginCoinPrecision)
    }


    //操作记录
    fun record() {
        val map = HashMap<String, Any>()
        map["positionId"] = bean.value?.id.toString()
        startTask(contractApiService.positionLog(map), Consumer {
            if (it.data.isEmpty()){
                return@Consumer
            }
            for (i in 0 until it.data.size) {
                val item=Item()
                item.bean.value=it.data[i]
                items.add(item)

            }


        })

    }

}