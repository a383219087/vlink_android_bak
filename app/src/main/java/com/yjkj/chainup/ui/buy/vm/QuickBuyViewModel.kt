package com.yjkj.chainup.ui.buy.vm


import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil


class QuickBuyViewModel : BaseViewModel() {


    var money = MutableLiveData("0")





    /**
     * g购买记录
     */
    fun onclickRightIcon(){

    }




    /**
     * g购买
     */
    fun buy(){
        ArouterUtil.navigation(RoutePath.QuickBuySureActivity, null)
    }




    fun setInput(input :String){
        if (input=="."&&money.value!!.contains(".")){
            return
        }
        if (money.value?.toDouble()==0.0){
            money.value= input
        }else{
            money.value=money.value+input
        }
    }

    fun delString(){
        if(money.value?.length==0){
            return
        }
        money.value=money.value!!.substring(0,money.value!!.length-1)
        if (money.value?.isEmpty()!!){
            money.value="0"
        }


    }

}