package com.yjkj.chainup.new_version.activity.financial

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.AllFragmentBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.financial.vm.AllViewModel


@Route(path = RoutePath.AllFragment)
class AllFragment : BaseMVFragment<AllViewModel?, AllFragmentBinding>() {
    @Autowired(name = "index")
    @JvmField
    var index =0





    override fun setContentView(): Int = R.layout.all_fragment
    override fun initView() {
        mViewModel?.page?.value=index

    }
}