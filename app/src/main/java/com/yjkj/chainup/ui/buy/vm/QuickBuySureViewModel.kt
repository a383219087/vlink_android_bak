package com.yjkj.chainup.ui.buy.vm


import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.bean.BuyInfo
import com.yjkj.chainup.bean.PaymentMethod
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.ui.buy.PayDialog


class QuickBuySureViewModel : BaseViewModel() {

    var activity = MutableLiveData<FragmentActivity>()

    var money = MutableLiveData("0")



    var checkInfo = MutableLiveData<BuyInfo>()
    var checkTwoInfo = MutableLiveData<BuyInfo>()

    var index = MutableLiveData(2)

    var rate = MutableLiveData("")


    var bean = MutableLiveData<PaymentMethod>()



    fun setIndex(i:Int){
        index.value=i
    }






    /**
     * 确定
     */
    fun onclickRightIcon(){
        if (index.value==1){
            ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, null)
        } else{
            PayDialog() .apply {
                val bundle = Bundle()
                bundle.putSerializable("one", checkInfo.value)
                bundle.putSerializable("two", checkTwoInfo.value)
                bundle.putSerializable("bean", bean.value)
                bundle.putString("money", money.value)
                this.arguments = bundle

            }.showDialog(activity.value?.supportFragmentManager,"")



        }



    }

}