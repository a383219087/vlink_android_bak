package com.yjkj.chainup.ui.home.vm


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.yjkj.chainup.base.BaseViewModel
import com.yjkj.chainup.common.binding.command.BindingCommand
import com.yjkj.chainup.common.binding.command.BindingConsumer
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.new_version.activity.personalCenter.MailActivity


open class HomePageViewModel : BaseViewModel() {

    var mActivity = MutableLiveData<FragmentActivity>()

    var showMarketLine = MutableLiveData(false)


    var toTopBar = BindingCommand(BindingConsumer<Boolean> { showMarketLine.value=it })

    /**
     * 个人中心
     */
   fun onClickToPersonal(){
        ArouterUtil.navigation(RoutePath.PersonalCenterActivity, null)

   }
    /**
     * 搜索
     */
   fun onClickSearch(){
        ArouterUtil.greenChannel(RoutePath.CoinMapActivity, Bundle().apply {
            putString("type", ParamConstant.ADD_COIN_MAP)
        })

   }

    /**
     * 消息
     */
    fun onClickMarket(){
        if (LoginManager.checkLogin(mActivity.value, true)) {
            mActivity.value?.startActivity(Intent(mActivity.value, MailActivity::class.java))
        }

    }
}