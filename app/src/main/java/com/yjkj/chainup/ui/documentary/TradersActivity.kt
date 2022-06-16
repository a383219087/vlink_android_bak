package com.yjkj.chainup.ui.documentary

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.databinding.ActivityTradersBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.documentary.vm.TradersViewModel
import com.yjkj.chainup.util.FmPagerAdapter


@Route(path = RoutePath.TradersActivity)
class TradersActivity : BaseMVActivity<TradersViewModel?, ActivityTradersBinding?>(){

    @Autowired(name = "bean")
    @JvmField
    var item : CommissionBean?=null

    @Autowired(name = "status")
    @JvmField
    var status : Int?=0


    private var mFragments: ArrayList<Fragment>? = null
    var pageAdapter: CpNVPagerAdapter? = null
    override fun setContentView() = R.layout.activity_traders
    override fun initData() {
        mViewModel?.item?.value=item
        mViewModel?.status?.value=status
        mViewModel?.activity?.value=mActivity
        mFragments = ArrayList()
        mFragments?.add(MySingleFragment.newInstance(status!!,item?.uid.toString()))

        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, supportFragmentManager)

    }



}