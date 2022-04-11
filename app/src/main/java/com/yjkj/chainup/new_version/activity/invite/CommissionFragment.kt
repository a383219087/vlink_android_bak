package com.yjkj.chainup.new_version.activity.invite

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R

import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentCommissionBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.invite.vm.CommissionViewModel


@Route(path = RoutePath.CommissionFragment)
class CommissionFragment : BaseMVFragment<CommissionViewModel?, FragmentCommissionBinding>() {
    override fun setContentView(): Int = R.layout.fragment_commission

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

