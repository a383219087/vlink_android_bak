package com.yjkj.chainup.ui.financial

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentProductBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.financial.vm.ProductViewModel
import com.yjkj.chainup.util.FmPagerAdapter


@Route(path = RoutePath.ProductFragment)
class ProductFragment : BaseMVFragment<ProductViewModel?, FragmentProductBinding>() {




    override fun setContentView(): Int = R.layout.fragment_product
    private val mFragments = mutableListOf<Fragment>()
    var pageAdapter: CpNVPagerAdapter? = null
    override fun initView() {
        mFragments.add(AllFragment.newInstance(0))
        mFragments.add(AllFragment.newInstance(1))
        mFragments.add(AllFragment.newInstance(2))
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, childFragmentManager)

    }
}

