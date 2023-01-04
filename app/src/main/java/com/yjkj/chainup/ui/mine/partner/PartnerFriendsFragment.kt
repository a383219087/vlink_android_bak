package com.yjkj.chainup.ui.mine.partner

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.view.CpDialogUtil.Companion.showClosePositionDialog
import com.yjkj.chainup.R

import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentFriendsBinding
import com.yjkj.chainup.databinding.FragmentFriendsPartnerBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.ui.mine.invite.vm.MyFriendsViewModel
import com.yjkj.chainup.ui.mine.partner.vm.PartnerMyFriendsViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PartnerFriendsFragment : BaseMVFragment<PartnerMyFriendsViewModel?, FragmentFriendsPartnerBinding>() {
    override fun setContentView(): Int = R.layout.fragment_friends_partner

    override fun initView() {
        mViewModel?.activity?.value=mActivity
        mBinding?.twinklingRefreshLayout?.autoRefresh(0,300,3f)
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefresh()
        })
        mViewModel?.isLoadMore?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishLoadMore()
        })
    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {

            MessageEvent.refresh_MyInviteCodesActivity -> {
                mViewModel?.getList(1)
            }
        }
    }


}

