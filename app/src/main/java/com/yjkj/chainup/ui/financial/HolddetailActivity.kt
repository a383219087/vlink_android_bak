package com.yjkj.chainup.ui.financial

import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.bean.Pos
import com.yjkj.chainup.databinding.ActivityHolddetailBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.financial.vm.HolddetailViewModel


@Route(path = RoutePath.Holddetail)
class HolddetailActivity : BaseMVActivity<HolddetailViewModel?, ActivityHolddetailBinding?>(){
    @Autowired(name = "bean")
    @JvmField
    var bean: Pos? = null
    @Autowired(name = "queryType")
    @JvmField
    var queryType: Int? = 1

    override fun setContentView() = R.layout.activity_holddetail
    override fun initData() {
        mViewModel?.bean?.value=bean
        mViewModel?.queryType?.value=queryType
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefresh()
        })
        mViewModel?.isLoadMore?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishLoadMore(true)
        })

    }





}