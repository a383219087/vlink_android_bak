package com.yjkj.chainup.new_version.activity.documentary

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityTradersApplyBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.ApplyTradersViewModel


@Route(path = RoutePath.ApplyTradersActivity)
class ApplyTradersActivity : BaseMVActivity<ApplyTradersViewModel?, ActivityTradersApplyBinding?>(){

    override fun setContentView() = R.layout.activity_traders_apply
    override fun initData() {
    mViewModel?.currentStatus()

    }




}