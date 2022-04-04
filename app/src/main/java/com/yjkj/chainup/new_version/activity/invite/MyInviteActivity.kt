package com.yjkj.chainup.new_version.activity.invite

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityInvitesBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.invite.vm.MyInviteViewModel


@Route(path = RoutePath.MyInviteActivity)
class MyInviteActivity : BaseMVActivity<MyInviteViewModel?, ActivityInvitesBinding?>() {

//    private var mFragments: ArrayList<Fragment>? = null
    override fun setContentView() = R.layout.activity_invites
    override fun initData() {
        mViewModel?.getList(1)
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefreshing()
        })
        mViewModel?.isLoadMore?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishLoadmore()
        })


//        mFragments = ArrayList()
//        mFragments?.add(   ARouter.getInstance().build(RoutePath.FriendsFragment).navigation() as Fragment)
//        mFragments?.add(   ARouter.getInstance().build(RoutePath.FriendsFragment).navigation() as Fragment)
//        sub_tab_layout.setViewPager(vp_order, arrayOf("好友列表","我的下级"), this, mFragments)
    }


}