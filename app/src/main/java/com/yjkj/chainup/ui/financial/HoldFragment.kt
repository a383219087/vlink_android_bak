package com.yjkj.chainup.ui.financial

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentHoldBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.financial.vm.HoldViewModel


@Route(path = RoutePath.HoldFragment)
class HoldFragment : BaseMVFragment<HoldViewModel?, FragmentHoldBinding>() {
    override fun setContentView(): Int = R.layout.fragment_hold
    override fun initView() {
      mViewModel?.getList(1)
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefresh()
        })
        mViewModel?.isLoadMore?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishLoadMore(true)
        })
    }
}

