package com.yjkj.chainup.ui.buy.vm


import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil


class QuickBuyViewModel : BaseViewModel() {





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

}