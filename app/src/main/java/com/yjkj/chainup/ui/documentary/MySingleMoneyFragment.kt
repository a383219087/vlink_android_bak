package com.yjkj.chainup.ui.documentary

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentSingleMoneyBinding
import com.yjkj.chainup.ui.documentary.vm.SingleMoneyViewModel
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
        mFragments?.add(SingleMoneyChildFragment.newInstance(0))
        mFragments?.add(SingleMoneyChildFragment.newInstance(1))
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, childFragmentManager)



    }


}