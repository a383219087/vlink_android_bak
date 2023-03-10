package com.yjkj.chainup.ui.mine.invite

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityInvitesBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.mine.invite.vm.MyInviteViewModel
import com.yjkj.chainup.util.FmPagerAdapter


@Route(path = RoutePath.MyInviteActivity)
class MyInviteActivity : BaseMVActivity<MyInviteViewModel?, ActivityInvitesBinding?>() {

    private var mFragments: ArrayList<Fragment>? = null
    override fun setContentView() = R.layout.activity_invites
    override fun initData() {
        mFragments = ArrayList()
        mFragments?.add(   ARouter.getInstance().build(RoutePath.FriendsFragment).navigation() as Fragment)
        mFragments?.add(   ARouter.getInstance().build(RoutePath.CommissionFragment).navigation() as Fragment)
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, supportFragmentManager)
    }


}