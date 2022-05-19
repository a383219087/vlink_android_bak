package com.yjkj.chainup.new_version.activity.documentary.vm


import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.utils.CpClLogicContractSetting
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractOrder
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.TraderPositionBean
import com.yjkj.chainup.contract.extension.showBaseName
import com.yjkj.chainup.contract.extension.showMarginName
import com.yjkj.chainup.contract.extension.showPriceName
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.new_version.activity.documentary.AddMoneyDialog
import com.yjkj.chainup.new_version.activity.documentary.ClosePositionDialog
import com.yjkj.chainup.new_version.activity.documentary.ShareDialog
import com.yjkj.chainup.new_version.activity.documentary.WinAndStopDialog
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.sl_fragment_cotract_introduce.*
import me.tatarka.bindingcollectionadapter2.ItemBinding




class NowDocumentViewModel : BaseViewModel() {

    var activity = MutableLiveData<FragmentActivity>()


    //  1是跟单2是带单

    var type = MutableLiveData<Int>()

    //  1当前0是历史

    var status = MutableLiveData<Int>()

    //

    var uid = MutableLiveData<String>()


    interface OnItemListener {
        fun onClick()
        fun onShareClick()
        fun onShareClick1()
        fun onShareClick2()
        fun onShareClick3()

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick() {
            ARouter.getInstance().build(RoutePath.DocumentaryDetailActivity).navigation()
        }
       //分享
        override fun onShareClick() {
            ShareDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }
        //追加本金
        override fun onShareClick1() {

            AddMoneyDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }


        //止盈止亏
        override fun onShareClick2() {
            WinAndStopDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }

        //平仓
        override fun onShareClick3() {
            ClosePositionDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }
    }


     class Item{

         //  1是跟单2是带单

         var type = MutableLiveData<Int>()

         //  1当前0是历史

         var status = MutableLiveData<Int>()

         var bean = MutableLiveData<TraderPositionBean>()

         var contract = MutableLiveData<Contract>()

         var  contractType= MutableLiveData<String>()



     }

    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_documentary_single_now).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()



    fun getList(mActivity: FragmentActivity) {
        val map = HashMap<String, Any>()
        map["status"] = status.value.toString()
        map["traderUid"] =uid.value.toString()

        startTask(apiService.traderPositionList(map), Consumer {

            if (it.data.records.isNullOrEmpty()) {
                return@Consumer
            }
            for (i in it.data.records.indices) {
                val item = Item()
                item.status.value=status.value
                item.type.value=type.value
                item.bean.value=it.data.records[i]
                item.contract.value=ContractPublicDataAgent.getContract(it.data.records[i].contractId)

                if (it.data.records[i].positionType==1){
                    item.contractType.value=LanguageUtil.getString(mActivity, "sl_str_full_position")+"-"+it.data.records[i].leverageLevel+"X"

                }else{
                    item.contractType.value=LanguageUtil.getString(mActivity, "sl_str_gradually_position")+"-"+it.data.records[i].leverageLevel+"X"
                }


                items.add(item)
            }

        })

    }


}