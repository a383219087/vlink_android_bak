package com.yjkj.chainup.new_version.activity.invite

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R

import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentFriendsBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.invite.vm.MyFriendsViewModel


@Route(path = RoutePath.FriendsFragment)
class FriendsFragment : BaseMVFragment<MyFriendsViewModel?, FragmentFriendsBinding>() {
    override fun setContentView(): Int = R.layout.fragment_friends

    override fun initView() {
        mViewModel?.getList(1)
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefreshing()
        })
        mViewModel?.isLoadMore?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishLoadmore()
        })
    }


}

