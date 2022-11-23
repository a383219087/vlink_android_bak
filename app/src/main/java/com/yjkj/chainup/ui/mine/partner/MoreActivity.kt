package com.yjkj.chainup.ui.mine.partner

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityInvitesBinding
import com.yjkj.chainup.databinding.ActivityInvitesMoreBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.mine.invite.vm.MyInviteViewModel
import com.yjkj.chainup.ui.mine.partner.vm.MoreViewModel
import com.yjkj.chainup.util.FmPagerAdapter


@Route(path = RoutePath.PartnerMyInviteActivity)
class MoreActivity : BaseMVActivity<MoreViewModel?, ActivityInvitesMoreBinding?>() {

    private var mFragments: ArrayList<Fragment>? = null
    override fun setContentView() = R.layout.activity_invites_more
    override fun initData() {
        mFragments = ArrayList()
        mFragments?.add( PartnerFriendsFragment())
        mFragments?.add( PartnerCommissionFragment())
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, supportFragmentManager)
    }


}