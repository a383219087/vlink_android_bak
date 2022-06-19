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
import com.yjkj.chainup.db.service.UserDataService
import io.reactivex.functions.Consumer
import me.tatarka.bindingcollectionadapter2.ItemBinding


class NowDocumentViewModel : BaseViewModel() {

    var activity = MutableLiveData<FragmentActivity>()


    //  1是我的跟单2是交易员的带单

    var status = MutableLiveData<Int>()

    //

    var uid = MutableLiveData<String>()


    interface OnItemListener {


    }

    var onItemListener: OnItemListener = object : OnItemListener {


    }


    class Item {
//
//        //  1是跟单2是带单
//
//        var type = MutableLiveData<Int>()
//
//        //  1当前0是历史
//
//        var status = MutableLiveData<Int>()


        //  true是我的,false是别人的

        var isMySelf = MutableLiveData(true)

        var isGreen = MutableLiveData(true)

        var symbolName = MutableLiveData("")

        var mPricePrecision = MutableLiveData(0)

        var mMarginCoinPrecision = MutableLiveData(0)

        var bean = MutableLiveData<CpContractPositionBean>()


    }

    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_documentary_single_now).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()


    fun getList(context: FragmentActivity) {
        items.clear()
        val map = HashMap<String, Any>()
        map["status"] = status.value.toString()

        if (uid.value.isNullOrEmpty()) {
            map["traderUid"] = UserDataService.getInstance().userInfo4UserId
        } else {
            map["traderUid"] = uid.value.toString()
        }

        startTask(contractApiService.traderPositionList(map), Consumer {

            if (it.data?.positionList.isNullOrEmpty()) {
                return@Consumer
            }
            for (i in it.data.positionList!!.indices) {
                val item = Item()
                item.bean.value = it.data.positionList!![i]
                item.isMySelf.value = uid.value.isNullOrEmpty()


                val contractId = item.bean.value!!.contractId

                item.mPricePrecision.value = CpClLogicContractSetting.getContractSymbolPricePrecisionById(context, contractId)

                item.mMarginCoinPrecision.value  = CpClLogicContractSetting.getContractMarginCoinPrecisionById(context, contractId)

                val mMultiplierCoin = CpClLogicContractSetting.getContractMultiplierCoinPrecisionById(context, contractId)

                val mMultiplierPrecision = CpClLogicContractSetting.getContractMultiplierPrecisionById(context, contractId)

                val mMultiplier = CpClLogicContractSetting.getContractMultiplierById(context, contractId)

                item.isGreen.value = (CpBigDecimalUtils.compareTo(
                    CpBigDecimalUtils.showSNormal(
                        item.bean.value!!.openRealizedAmount,
                        item.mMarginCoinPrecision.value!!
                    ), "0"
                ) == 1)

                item.symbolName.value = CpClLogicContractSetting.getContractShowNameById(context, contractId)

                items.add(item)
            }


        })

    }


}