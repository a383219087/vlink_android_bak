package com.yjkj.chainup.ui.financial

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentAutomaticDepositBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.financial.vm.AutomaticDepositViewModel


@Route(path = RoutePath.AutomaticDepositFragment)
class AutomaticDepositFragment : BaseMVFragment<AutomaticDepositViewModel?, FragmentAutomaticDepositBinding>() {
    override fun setContentView(): Int = R.layout.fragment_automatic_deposit
    override fun initView() {


    }
}

