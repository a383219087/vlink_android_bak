package com.yjkj.chainup.ui.documentary

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentSingleBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.ui.documentary.vm.SingleViewModel
import com.yjkj.chainup.util.FmPagerAdapter


class MySingleFragment : BaseMVFragment<SingleViewModel?, FragmentSingleBinding?>(){



    companion object {
        @JvmStatic
        fun newInstance(status: Int,uid:String): MySingleFragment {
            val fg = MySingleFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, status)
            bundle.putString(ParamConstant.MARKET_NAME, uid)
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
        mFragments?.add(NowDocumentaryFragment.newInstance(1,mViewModel?.status?.value!!,arguments?.getString(ParamConstant.MARKET_NAME)!!))
        mFragments?.add(NowDocumentaryFragment.newInstance(0,mViewModel?.status?.value!!,arguments?.getString(ParamConstant.MARKET_NAME)!!))
        mFragments?.add(MyTradersFragment.newInstance(mViewModel?.status?.value!!,arguments?.getString(ParamConstant.MARKET_NAME)!!))
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, childFragmentManager)




    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            mViewModel?.getDetail(arguments?.getString(ParamConstant.MARKET_NAME)!!)
        }
    }
}