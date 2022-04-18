package com.yjkj.chainup.new_version.activity.binary

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityOptionsBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.binary.vm.OptionsViewModel
import com.yjkj.chainup.util.FmPagerAdapter


@Route(path = RoutePath.OptionsActivity)
class OptionsActivity : BaseMVActivity<OptionsViewModel?, ActivityOptionsBinding?>(){
    private var mFragments: ArrayList<Fragment>? = null
    var pageAdapter: CpNVPagerAdapter? = null
    override fun setContentView() = R.layout.activity_options
    override fun initData() {

        mFragments = ArrayList()
        mFragments?.add(ARouter.getInstance().build(RoutePath.CurrentFragment).navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.MydealFragment).navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.RankingFragment).navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.ResultsFragment).navigation() as Fragment)
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, supportFragmentManager)

    }


    override fun onResume() {
        super.onResume()
    }


}