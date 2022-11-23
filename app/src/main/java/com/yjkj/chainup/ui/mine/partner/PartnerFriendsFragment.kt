package com.yjkj.chainup.ui.mine.partner

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R

import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentFriendsBinding
import com.yjkj.chainup.databinding.FragmentFriendsPartnerBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.mine.invite.vm.MyFriendsViewModel
import com.yjkj.chainup.ui.mine.partner.vm.PartnerMyFriendsViewModel


class PartnerFriendsFragment : BaseMVFragment<PartnerMyFriendsViewModel?, FragmentFriendsPartnerBinding>() {
    override fun setContentView(): Int = R.layout.fragment_friends_partner

    override fun initView() {
        mViewModel?.getList(1)
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefresh()
        })
        mViewModel?.isLoadMore?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishLoadMore()
        })
    }


}

