package com.yjkj.chainup.new_version.activity.financial

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityContractAgent1Binding
import com.yjkj.chainup.databinding.ActivityDocumentaryBinding
import com.yjkj.chainup.databinding.ActivityFinancialBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.DocumentaryViewModel
import com.yjkj.chainup.new_version.activity.financial.vm.FinancialViewModel
import com.yjkj.chainup.new_version.activity.invite.vm.ContractAgentViewModel
import kotlinx.android.synthetic.main.activity_documentary.*


@Route(path = RoutePath.FinancialActivity)
class FinancialActivity : BaseMVActivity<FinancialViewModel?, ActivityFinancialBinding?>(){

        private var mFragments: ArrayList<Fragment>? = null
    override fun setContentView() = R.layout.activity_financial
    override fun initData() {
        mFragments = ArrayList()
        mFragments?.add(   ARouter.getInstance().build(RoutePath.FirstFragment).navigation() as Fragment)
        mFragments?.add(   ARouter.getInstance().build(RoutePath.MineFragment).navigation() as Fragment)
        mFragments?.add(   ARouter.getInstance().build(RoutePath.MineFragment).navigation() as Fragment)
        sub_tab_layout.setViewPager(vp_order, arrayOf("产品","自动存入","持有"), this, mFragments)

    }

    override fun onResume() {
        super.onResume()
    }


}