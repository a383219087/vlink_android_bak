package com.yjkj.chainup.ui.mine.partner

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R

import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentCommissionBinding
import com.yjkj.chainup.databinding.FragmentCommissionPartnerBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.mine.invite.vm.CommissionViewModel
import com.yjkj.chainup.ui.mine.partner.vm.PartnerCommissionViewModel


class PartnerCommissionFragment : BaseMVFragment<PartnerCommissionViewModel?, FragmentCommissionPartnerBinding>() {
    override fun setContentView(): Int = R.layout.fragment_commission_partner

    override fun initView() {
        mBinding?.twinklingRefreshLayout?.autoRefresh(0,300,3f)
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefresh()
        })
        mViewModel?.isLoadMore?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishLoadMore()
        })
    }


}

