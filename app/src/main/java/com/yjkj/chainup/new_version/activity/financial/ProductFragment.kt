package com.yjkj.chainup.new_version.activity.financial

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentProductBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.financial.vm.ProductViewModel


@Route(path = RoutePath.ProductFragment)
class ProductFragment : BaseMVFragment<ProductViewModel?, FragmentProductBinding>() {
    override fun setContentView(): Int = R.layout.fragment_product
    override fun initView() {

    }
}

