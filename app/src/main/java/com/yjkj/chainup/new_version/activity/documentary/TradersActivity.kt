package com.yjkj.chainup.new_version.activity.documentary

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.databinding.ActivityTradersBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.TradersViewModel
import com.yjkj.chainup.util.FmPagerAdapter


@Route(path = RoutePath.TradersActivity)
class TradersActivity : BaseMVActivity<TradersViewModel?, ActivityTradersBinding?>(){

    @Autowired(name = "bean")
    @JvmField
    var item : CommissionBean?=null




    private var mFragments: ArrayList<Fragment>? = null
    var pageAdapter: CpNVPagerAdapter? = null
    override fun setContentView() = R.layout.activity_traders
    override fun initData() {
        mViewModel?.item?.value=item
        mFragments = ArrayList()
        mFragments?.add(ARouter.getInstance().build(RoutePath.NowDocumentaryFragment).navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.HistoryDocumentaryFragment).navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.MyTradersFragment).navigation() as Fragment)
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, supportFragmentManager)

    }



    override fun onResume() {
        super.onResume()
    }


}