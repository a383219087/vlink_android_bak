package com.yjkj.chainup.new_version.activity.documentary

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentMyTradersBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.MyTradersModel


@Route(path = RoutePath.MyTradersFragment)
class MyTradersFragment : BaseMVFragment<MyTradersModel?, FragmentMyTradersBinding>() {
    override fun setContentView(): Int = R.layout.fragment_my_traders
    override fun initView() {
        mViewModel?.getList()

    }


}

