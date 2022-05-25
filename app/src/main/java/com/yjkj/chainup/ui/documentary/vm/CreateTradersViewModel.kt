package com.yjkj.chainup.ui.documentary.vm


import android.view.View
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.functions.Consumer


class CreateTradersViewModel : BaseViewModel() {
    var uid = MutableLiveData("")


    //1是新建2是编辑
    var type = MutableLiveData(1)

    var checkIndex = MutableLiveData(0)


    //跟单金额
    var documentaryMoney = MutableLiveData("")

    //保证金使用上限
    var maxEarnestMoney = MutableLiveData("")

    //跟单必留
    var documentaryRate = MutableLiveData("")

    //止盈比例
    var winRate = MutableLiveData("")

    //止损
    var stopRate = MutableLiveData("")


    var bean = MutableLiveData<CommissionBean>()


    fun setCheckIndex(type: Int) {
        checkIndex.value = type

    }


    fun setWinRate(rate:Int) {
        winRate.value=rate.toString()

    }

    fun setStopRate(rate:Int) {
        stopRate.value=rate.toString()

    }



    fun create() {
        if (checkIndex.value==0&&documentaryRate.value.isNullOrBlank()){
            ToastUtils.showToast("请输入跟单比例")
            return
        }
        if (checkIndex.value==1&&documentaryMoney.value.isNullOrBlank()){
            ToastUtils.showToast("请输入跟单金额")
            return
        }
        if (maxEarnestMoney.value.isNullOrBlank()){
            ToastUtils.showToast("请输入保证金")
            return
        }
        if (winRate.value.isNullOrBlank()){
            ToastUtils.showToast("止盈比例不能为空")
            return
        }
        if (stopRate.value.isNullOrBlank()){
            ToastUtils.showToast("止损比例不能为空")
            return
        }

        val map = HashMap<String, Any>()
        map["traderUid"] = uid.value.orEmpty()
        map["type"] =checkIndex.value.toString()
        if (checkIndex.value==0){
            map["rate"] =documentaryRate.value.toString()
        }else{
            map["amount"] =documentaryMoney.value.toString()
        }
        map["deposit"] =maxEarnestMoney.value.toString()
        map["profitRatio"] =winRate.value.toString()
        map["lossRatio"] =stopRate.value.toString()
        startTask(apiService.createTrader(map), Consumer {
            ToastUtils.showToast(it.msg)
          finish()

        })
    }

    fun cancel(view:View) {

        NewDialogUtils.showNormalDialog(view.context!!, "取消跟单后，已跟随未平仓的合约仍然会参与收益", object : NewDialogUtils.DialogBottomListener {
            override fun sendConfirm() {
                val map = HashMap<String, Any>()
                map["traderUid"] = uid.value.orEmpty()
                startTask(apiService.cancelTrader(map), Consumer {
                    ToastUtils.showToast(it.msg)
                    finish()

                })
            }

        }, "", "关闭", "确认取消")


    }


}