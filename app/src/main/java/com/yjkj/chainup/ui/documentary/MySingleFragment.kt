package com.yjkj.chainup.ui.documentary

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentSingleBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.ui.documentary.vm.SingleViewModel
import com.yjkj.chainup.util.FmPagerAdapter
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


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
        mViewModel?.uid?.value=arguments?.getString(ParamConstant.MARKET_NAME)

        mFragments = ArrayList()
        mFragments?.add(NowDocumentaryFragment.newInstance(2,mViewModel?.status?.value!!,arguments?.getString(ParamConstant.MARKET_NAME)!!))
        mFragments?.add(HisDocumentaryFragment.newInstance(0,mViewModel?.status?.value!!,arguments?.getString(ParamConstant.MARKET_NAME)!!))
        mFragments?.add(MyTradersFragment.newInstance(mViewModel?.status?.value!!,arguments?.getString(ParamConstant.MARKET_NAME)!!,false))
        mBinding?.viewPager?.adapter = FmPagerAdapter(mFragments, childFragmentManager)




    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            mViewModel?.getDetail()
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {

            MessageEvent.refresh_MyInviteCodesActivity -> {
                mViewModel?.getDetail()
            }
        }
    }
}