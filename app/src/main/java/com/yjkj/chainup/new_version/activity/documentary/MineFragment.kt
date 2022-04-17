package com.yjkj.chainup.new_version.activity.documentary

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentMineBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.MineViewModel
import com.yjkj.chainup.util.FmPagerAdapter


@Route(path = RoutePath.MineFragment)
class MineFragment : BaseMVFragment<MineViewModel?, FragmentMineBinding>() {
    override fun setContentView(): Int = R.layout.fragment_mine

    private var mFragments: ArrayList<Fragment>? = null
    var pageAdapter: CpNVPagerAdapter? = null
    override fun initView() {
        mFragments = ArrayList()
        mFragments?.add(ARouter.getInstance().build(RoutePath.NowDocumentaryFragment).navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.HistoryDocumentaryFragment).navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.MyTradersFragment).navigation() as Fragment)
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, childFragmentManager)



    }


}

