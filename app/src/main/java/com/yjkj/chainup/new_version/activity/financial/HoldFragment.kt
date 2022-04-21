package com.yjkj.chainup.new_version.activity.financial

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentHoldBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.financial.vm.HoldViewModel


@Route(path = RoutePath.HoldFragment)
class HoldFragment : BaseMVFragment<HoldViewModel?, FragmentHoldBinding>() {
    override fun setContentView(): Int = R.layout.fragment_hold
    override fun initView() {

    }
}

