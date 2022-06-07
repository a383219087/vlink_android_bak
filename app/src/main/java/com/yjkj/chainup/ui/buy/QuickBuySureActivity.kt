package com.yjkj.chainup.ui.buy

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityQuickBuySureBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.buy.vm.QuickBuySureViewModel


@Route(path = RoutePath.QuickBuySureActivity)
class QuickBuySureActivity : BaseMVActivity<QuickBuySureViewModel?, ActivityQuickBuySureBinding?>() {

    override fun setContentView() = R.layout.activity_quick_buy_sure
    override fun initData() {



    }








}

