package com.yjkj.chainup.new_version.activity.financial

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentProductBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.financial.vm.ProductViewModel
import com.yjkj.chainup.util.FmPagerAdapter


@Route(path = RoutePath.ProductFragment)
class ProductFragment : BaseMVFragment<ProductViewModel?, FragmentProductBinding>() {




    override fun setContentView(): Int = R.layout.fragment_product
    private var mFragments: ArrayList<Fragment>? = null
    var pageAdapter: CpNVPagerAdapter? = null
    override fun initView() {
        mFragments = ArrayList()
        mFragments?.add(ARouter.getInstance().build(RoutePath.AllFragment)
            .withInt("index",0)
            .navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.AllFragment)
            .withInt("index",1)
            .navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.AllFragment)
            .withInt("index",2)
            .navigation() as Fragment)
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, childFragmentManager)

    }
}

