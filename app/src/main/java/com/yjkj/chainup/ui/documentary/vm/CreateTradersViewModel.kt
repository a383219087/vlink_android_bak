package com.yjkj.chainup.ui.documentary.vm


import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.common.sdk.LibCore.context
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.AboutUSBean
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.adapter.AbountAdapter
import com.yjkj.chainup.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_about.*


class CreateTradersViewModel : BaseViewModel() {
    var uid = MutableLiveData("")
    var mAcitiviy = MutableLiveData<Activity>()


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

    var rate = MutableLiveData("")
    var isRed = MutableLiveData(true)



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
            ToastUtils.showToast(context.getString(R.string.dialog_create_trader_text19))
            return
        }
        if (checkIndex.value==1&&documentaryMoney.value.isNullOrBlank()){
            ToastUtils.showToast(context.getString(R.string.dialog_create_trader_text20))
            return
        }
        if (maxEarnestMoney.value.isNullOrBlank()){
            ToastUtils.showToast(context.getString(R.string.dialog_create_trader_text21))
            return
        }
        if (winRate.value.isNullOrBlank()){
            ToastUtils.showToast(context.getString(R.string.dialog_create_trader_text22))
            return
        }
        if (stopRate.value.isNullOrBlank()){
            ToastUtils.showToast(context.getString(R.string.dialog_create_trader_text23))
            return
        }

        val map = HashMap<String, Any>()
        if ( uid.value.isNullOrEmpty()) {
            map["traderUid"] = UserDataService.getInstance().userInfo4UserId
        } else {
            map["traderUid"] = uid.value.toString()
        }
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
            EventBusUtil.post(MessageEvent(MessageEvent.refresh_MyInviteCodesActivity))
          finish()

        })
    }
    fun getData(){
        setCheckIndex(bean.value!!.type)
        if (bean.value!!.type==0){
           documentaryRate.value=bean.value!!.rate
        }else{
            documentaryMoney.value=bean.value!!.amount
        }
        maxEarnestMoney.value=bean.value!!.deposit
        winRate.value=bean.value!!.profitRatio.toInt().toString()
        stopRate.value= bean.value!!.lossRatio.toInt().toString()

    }

    fun cancel(view:View) {
        val map = HashMap<String, Any>()
        if ( uid.value.isNullOrEmpty()) {
            map["traderUid"] = UserDataService.getInstance().userInfo4UserId
        } else {
            map["traderUid"] = uid.value.toString()
        }
        startTask(apiService.cancelTrader(map), Consumer {
            ToastUtils.showToast(it.msg)
            EventBusUtil.post(MessageEvent(MessageEvent.refresh_MyInviteCodesActivity))
            finish()

        })

    }


    //获取分成比例
    fun getViewRate(){
        startTask(contractApiService.traderBonusRate(), Consumer {
          rate.value= DecimalUtil.cutValueByPrecision(it.data,2)
         isRed.value=  rate.value?.toDouble()!! >0
        })
    }

    //用户协议
    fun getAgree(){
        HttpClient.instance.getAboutUs()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : NetObserver<ArrayList<AboutUSBean>>() {
                override fun onHandleSuccess(list: ArrayList<AboutUSBean>?) {
                    if (list == null) return
                    if (list.last().content.isHttpUrl()){
                        val bundle = Bundle()
                        bundle.putString(ParamConstant.URL_4_SERVICE, list.last().content)
                        ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)
                    }

                }
            })

    }


}