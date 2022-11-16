package com.yjkj.chainup.ui.documentary.vm


import androidx.lifecycle.MutableLiveData
import com.common.sdk.LibCore.context
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.functions.Consumer


class ApplyTradersViewModel : BaseViewModel() {


  //    申请进度 -1: 未申请; 0: 申请中，1 : 已是交易员, 2: 拒绝

    var statusString = MutableLiveData(context.getString(R.string.traders_apply_text3))
    var status = MutableLiveData(-1)


    fun currentStatus() {
        startTask(apiService.currentStatus(), Consumer {
            status.value=it.data.status
            statusString.value=when(it.data.status)  {
                0->context.getString(R.string.traders_apply_text4)
//                1->context.getString(R.string.traders_apply_text5)
                2->context.getString(R.string.traders_apply_text6)
                else->context.getString(R.string.traders_apply_text7)
            }

        })

    }



    fun applyCurrentStatus() {
        if (status.value==-1||status.value==1||status.value==-2||status.value==2){
            startTask(apiService.applyBecomeTrader(), Consumer {
                ToastUtils.showToast(context.getString(R.string.common_tip_cerSubmitSuccess))
                finish()
//                val bundle = Bundle()
//              val  visiter_id= UserDataService.getInstance().userInfo4UserId
//              val  visiter_name= UserDataService.getInstance().nickName
//
//                val url="http://kefuadmin.zwwbit.com/index/index/home?theme=7571f9&visiter_id=${visiter_id}&visiter_name${visiter_name}=&avatar=&business_id=1&groupid=0"
//
//                bundle.putString(ParamConstant.URL_4_SERVICE, url)
//                ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)

            })
        }else{
            finish()
        }


    }









}