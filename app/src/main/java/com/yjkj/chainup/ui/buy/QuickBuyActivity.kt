package com.yjkj.chainup.ui.buy

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityQuickBuyBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.buy.vm.QuickBuyViewModel


@Route(path = RoutePath.QuickBuyActivity)
class QuickBuyActivity : BaseMVActivity<QuickBuyViewModel?, ActivityQuickBuyBinding?>() {

    override fun setContentView() = R.layout.activity_quick_buy
    override fun initData() {
        mViewModel?.activity?.value=mActivity
    mViewModel?.getData()



    }








}

