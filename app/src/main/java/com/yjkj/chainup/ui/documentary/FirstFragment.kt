package com.yjkj.chainup.ui.documentary

import android.os.Bundle
import androidx.lifecycle.Observer
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentFirstBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.ui.documentary.vm.FirstViewModel


class FirstFragment : BaseMVFragment<FirstViewModel?, FragmentFirstBinding>() {


    companion object {
        @JvmStatic
        fun newInstance(status: Int): FirstFragment {
            val fg = FirstFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, status)
            fg.arguments = bundle
            return fg
        }

    }
    override fun setContentView(): Int = R.layout.fragment_first
    override fun initView() {
        mViewModel?.context?.value=mActivity
        mViewModel?.status?.value=arguments?.getInt(ParamConstant.CUR_INDEX)
        mViewModel?.getList(1)
        mViewModel?.getData()


        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefresh()
        })


        mViewModel?.isLoadMore?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishLoadMore(true)
        })


    }

    override fun fragmentVisibile(isVisibleToUser: Boolean) {
        super.fragmentVisibile(isVisibleToUser)
        if (isVisibleToUser) {
            mViewModel?.getList(1)
            mViewModel?.getData()
        }
    }

}

