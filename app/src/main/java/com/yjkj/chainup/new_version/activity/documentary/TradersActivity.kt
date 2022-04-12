package com.yjkj.chainup.new_version.activity.documentary

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityContractAgent1Binding
import com.yjkj.chainup.databinding.ActivityDocumentaryBinding
import com.yjkj.chainup.databinding.ActivityTradersBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.DocumentaryViewModel
import com.yjkj.chainup.new_version.activity.documentary.vm.TradersViewModel
import com.yjkj.chainup.new_version.activity.invite.vm.ContractAgentViewModel
import kotlinx.android.synthetic.main.activity_documentary.*


@Route(path = RoutePath.TradersActivity)
class TradersActivity : BaseMVActivity<TradersViewModel?, ActivityTradersBinding?>(){

    override fun setContentView() = R.layout.activity_traders
    override fun initData() {


    }



    override fun onResume() {
        super.onResume()
    }


}