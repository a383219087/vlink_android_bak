package com.yjkj.chainup.new_version.activity.documentary.vm


import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSONObject
import com.blankj.utilcode.util.JsonUtils
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.ui.activity.CpContractStopRateLossActivity
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.didichuxing.doraemonkit.util.JsonUtil
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.new_version.activity.documentary.ClosePositionDialog
import com.yjkj.chainup.new_version.activity.documentary.ShareDialog
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.functions.Consumer
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
        fun onClick(item:Item)
        fun onShareClick(item:Item)
        fun onShareClick1(item:Item)
        fun onShareClick2(item:Item)
        fun onShareClick3(item:Item)

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item:Item) {
            ARouter.getInstance().build(RoutePath.DocumentaryDetailActivity)
                .withSerializable("bean",item.bean.value)
                .withInt("type",item.type.value!!)
                .withInt("status",item.status.value!!)
                .navigation()
        }
       //分享
        override fun onShareClick(item:Item) {
            ShareDialog(). showDialog(activity.value?.supportFragmentManager,"")
        }
        //追加本金
        override fun onShareClick1(item:Item) {
            NewDialogUtils.adjustDepositDialog(activity.value!!, JSONObject.toJSON(item.bean.value) as org.json.JSONObject,
                object : NewDialogUtils.DialogBottomAloneListener {
                    override fun returnContent(content: String) {
                        val map = HashMap<String, Any>()
                        map["amount"] =content
                        map["contractId"] =item.bean.value?.contractId!!
                        map["positionId"] =item.bean.value?.id!!
                        startTask(contractApiService.transferMargin4Contract1(toRequestBody(map)), Consumer {
                            NToastUtil.showTopToastNet(activity.value!!,true, LanguageUtil.getString(activity.value!!, "contract_modify_the_success"))

                        })
                    }
                })

//            AddMoneyDialog().apply {
//                val bundle = Bundle()
//                bundle.putSerializable("bean", item.bean.value)
//                this.arguments = bundle
//
//            }. showDialog(activity.value?.supportFragmentManager,"")
        }


        //止盈止亏
        override fun onShareClick2(item:Item) {
            CpContractStopRateLossActivity.show(activity.value!!, item.bean.value!!)
        }

        //平仓
        override fun onShareClick3(item:Item) {
            ClosePositionDialog().apply {
                val bundle = Bundle()
                bundle.putSerializable("bean", item.bean.value)
                this.arguments = bundle

            }. showDialog(activity.value?.supportFragmentManager,"")
        }
    }


     class Item{

         //  1是跟单2是带单

         var type = MutableLiveData<Int>()

         //  1当前0是历史

         var status = MutableLiveData<Int>()

         var bean = MutableLiveData<CpContractPositionBean>()

         var contract = MutableLiveData<Contract>()

         var  contractType= MutableLiveData<String>()


         var  time= MutableLiveData<String>()

     }

    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_documentary_single_now).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()



    fun getList(mActivity: FragmentActivity) {
        val map = HashMap<String, Any>()
        map["status"] = status.value.toString()
        map["traderUid"] =uid.value.toString()

        startTask(contractApiService.traderPositionList(map), Consumer {

            if (it.data?.records.isNullOrEmpty()) {
                return@Consumer
            }
            for (i in it.data.records!!.indices) {
                val item = Item()
                item.status.value=status.value
                item.type.value=type.value
                item.bean.value=it.data.records!![i]
                item.contract.value=ContractPublicDataAgent.getContract(it.data.records!![i].contractId)

                if (it.data.records!![i].orderSide=="BUY"){
                    item.contractType.value="多仓-"+it.data.records!![i].leverageLevel+"X"
                }else{
                    item.contractType.value="空仓-"+it.data.records!![i].leverageLevel+"X"
                }
                item.time.value="${it.data.records!![i].ctime}->${it.data.records!![i].mtime}"

                items.add(item)
            }

        })

    }


}