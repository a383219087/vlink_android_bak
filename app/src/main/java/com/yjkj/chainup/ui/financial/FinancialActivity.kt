package com.yjkj.chainup.ui.financial

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityFinancialBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.financial.vm.FinancialViewModel


@Route(path = RoutePath.FinancialActivity)
class FinancialActivity : BaseMVActivity<FinancialViewModel?, ActivityFinancialBinding?>(){

        private var mFragments: ArrayList<Fragment>? = null
    override fun setContentView() = R.layout.activity_financial
    override fun initData() {
        mFragments = ArrayList()
        mFragments?.add(   ARouter.getInstance().build(RoutePath.ProductFragment).navigation() as Fragment)
//        mFragments?.add(   ARouter.getInstance().build(RoutePath.AutomaticDepositFragment).navigation() as Fragment)
        mFragments?.add(   ARouter.getInstance().build(RoutePath.HoldFragment).navigation() as Fragment)
        mBinding?.subTabLayout?.setViewPager(mBinding?.vpOrder, arrayOf("产品","持有"), this, mFragments)

    }





}