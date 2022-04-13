package com.yjkj.chainup.new_version.activity.documentary

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityTradersBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.TradersViewModel


@Route(path = RoutePath.TradersActivity)
class TradersActivity : BaseMVActivity<TradersViewModel?, ActivityTradersBinding?>(){

    override fun setContentView() = R.layout.activity_traders
    override fun initData() {


    }



    override fun onResume() {
        super.onResume()
    }


}