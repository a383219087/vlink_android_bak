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
import com.yjkj.chainup.databinding.FragmentSingleMoneyBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.SingleMoneyViewModel
import com.yjkj.chainup.new_version.activity.documentary.vm.SingleViewModel
import com.yjkj.chainup.new_version.activity.documentary.vm.TradersViewModel
import com.yjkj.chainup.util.FmPagerAdapter


class MySingleMoneyFragment : BaseMVFragment<SingleMoneyViewModel?, FragmentSingleMoneyBinding?>(){



    companion object {
        @JvmStatic
        fun newInstance(): MySingleMoneyFragment {
            val fg = MySingleMoneyFragment()
            val bundle = Bundle()
            fg.arguments = bundle
            return fg
        }

    }


    override fun setContentView() = R.layout.fragment_single_money

    private var mFragments: ArrayList<Fragment>? = null
    var pageAdapter: CpNVPagerAdapter? = null
    override fun initView() {
        mFragments = ArrayList()
        mFragments?.add(NowDocumentaryFragment.newInstance(1,1,""))
        mFragments?.add(NowDocumentaryFragment.newInstance(0,1,""))
        mFragments?.add(MyTradersFragment.newInstance(1,""))
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, childFragmentManager)



    }


}