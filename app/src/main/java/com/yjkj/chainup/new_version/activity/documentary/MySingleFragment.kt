package com.yjkj.chainup.new_version.activity.documentary

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.databinding.ActivityTradersBinding
import com.yjkj.chainup.databinding.FragmentSingleBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.SingleViewModel
import com.yjkj.chainup.new_version.activity.documentary.vm.TradersViewModel
import com.yjkj.chainup.util.FmPagerAdapter


class MySingleFragment : BaseMVFragment<SingleViewModel?, FragmentSingleBinding?>(){



    companion object {
        @JvmStatic
        fun newInstance(status: Int): MySingleFragment {
            val fg = MySingleFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, status)
            fg.arguments = bundle
            return fg
        }

    }


    override fun setContentView() = R.layout.fragment_single

    private var mFragments: ArrayList<Fragment>? = null
    var pageAdapter: CpNVPagerAdapter? = null
    override fun initView() {
        mViewModel?.status?.value=arguments?.getInt(ParamConstant.CUR_INDEX)
        mFragments = ArrayList()
        mFragments?.add(ARouter.getInstance().build(RoutePath.NowDocumentaryFragment).navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.HistoryDocumentaryFragment).navigation() as Fragment)
        mFragments?.add(ARouter.getInstance().build(RoutePath.MyTradersFragment).navigation() as Fragment)
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, childFragmentManager)



    }

}