package com.yjkj.chainup.ui.documentary.vm


import android.os.Bundle
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSONObject
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.ui.activity.CpContractStopRateLossActivity
import com.chainup.contract.utils.CpClLogicContractSetting
import com.common.sdk.LibCore.context
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.yjkj.chainup.BR
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.new_contract.activity.ClHoldShareActivity
import com.yjkj.chainup.new_contract.activity.CpContractEntrustNewActivity.Companion.mContractId
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.ui.documentary.ClosePositionDialog
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.LanguageUtil
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
        fun onClick(item: Item)
        fun onShareClick(item: Item)
        fun onShareClick1(item: Item)
        fun onShareClick2(item: Item)
        fun onShareClick3(item: Item)

    }

    var onItemListener: OnItemListener = object : OnItemListener {
        override fun onClick(item: Item) {
            ARouter.getInstance().build(RoutePath.DocumentaryDetailActivity)
                .withSerializable("bean", item.bean.value)
                .withInt("type", item.type.value!!)
                .withInt("status", item.status.value!!)
                .navigation()
        }

        //分享
        override fun onShareClick(item: Item) {

            val bean :CpContractPositionBean=item.bean.value!!
            if (bean.contractName.isNullOrEmpty()){
                bean.contractName=CpClLogicContractSetting.getContractShowNameById(activity.value, mContractId)
            }
            ClHoldShareActivity.show(activity.value!!,bean )

        }

        //追加本金
        override fun onShareClick1(item: Item) {
            NewDialogUtils.adjustDepositDialog(activity.value!!, JSONObject.toJSON(item.bean.value) as org.json.JSONObject,
                object : NewDialogUtils.DialogBottomAloneListener {
                    override fun returnContent(content: String) {
                        val map = HashMap<String, Any>()
                        map["amount"] = content
                        map["contractId"] = item.bean.value?.contractId!!
                        map["positionId"] = item.bean.value?.id!!
                        startTask(contractApiService.transferMargin4Contract1(toRequestBody(map)), Consumer {
                            NToastUtil.showTopToastNet(
                                activity.value!!,
                                true,
                                LanguageUtil.getString(activity.value!!, "contract_modify_the_success")
                            )

                        })
                    }
                })
        }


        //止盈止亏
        override fun onShareClick2(item: Item) {
            CpContractStopRateLossActivity.show(activity.value!!, item.bean.value!!)
        }

        //平仓
        override fun onShareClick3(item: Item) {
            ClosePositionDialog().apply {
                val bundle = Bundle()
                bundle.putSerializable("bean", item.bean.value)
                this.arguments = bundle

            }.showDialog(activity.value?.supportFragmentManager, "")
        }
    }


    class Item {

        //  1是跟单2是带单

        var type = MutableLiveData<Int>()

        //  1当前0是历史

        var status = MutableLiveData<Int>()


        //  true是我的,false是别人的

        var isMySelf = MutableLiveData(true)

        var bean = MutableLiveData<CpContractPositionBean>()

        //收益
        var revenue = MutableLiveData<String>()

        //收益lv
        var returnRate = MutableLiveData<String>()


        var contractType = MutableLiveData<String>()


        var time = MutableLiveData<String>()

    }

    val itemBinding =
        ItemBinding.of<Item>(BR.item, R.layout.item_documentary_single_now).bindExtra(BR.onItemListener, onItemListener)
    val items: ObservableList<Item> = ObservableArrayList()


    fun getList(mActivity: FragmentActivity) {
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
                item.status.value = status.value
                item.type.value = type.value
                item.bean.value = it.data.positionList!![i]
                item.isMySelf.value = uid.value.isNullOrEmpty()
                val orderSide = if (it.data.positionList!![i].orderSide == "BUY") {
                    context.getString(R.string.cl_HistoricalPosition_1) + "-"
                } else {
                    context.getString(R.string.cl_HistoricalPosition_2) + "-"
                }
                val positionType = if (it.data.positionList!![i].positionType == 1) {
                    context.getString(R.string.cl_currentsymbol_marginmodel1) + "-"
                } else {
                    context.getString(R.string.cl_currentsymbol_marginmodel2) + "-"
                }
                item.contractType.value = "${orderSide}${positionType}${it.data.positionList!![i].leverageLevel}X"


                if (it.data.positionList!![i].coPosition == null) {
                    item.time.value = ""
                } else {
                    item.time.value =
                        "${it.data.positionList!![i].coPosition?.ctime}->${it.data.positionList!![i].coPosition?.mtime}"
                }

                val mMarginCoinPrecision =
                    LogicContractSetting.getContractMarginCoinPrecisionById(mActivity, it.data.positionList!![i].contractId)

                item.revenue.value =
                    BigDecimalUtils.showSNormal(it.data.positionList!![i].profitRealizedAmount, mMarginCoinPrecision)
                //回报率
                item.returnRate.value = NumberUtil.getDecimal(2).format(
                    MathHelper.round(MathHelper.mul(it.data.positionList!![i].returnRate.toString(), "100"), 2)
                ).toString() + "%"

                items.add(item)
            }


        })

    }


}