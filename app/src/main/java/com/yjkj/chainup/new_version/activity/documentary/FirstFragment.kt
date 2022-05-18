package com.yjkj.chainup.new_version.activity.documentary

import android.os.Bundle
import androidx.lifecycle.Observer
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentFirstBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.new_version.activity.documentary.vm.FirstViewModel


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
        mViewModel?.status?.value=arguments?.getInt(ParamConstant.CUR_INDEX)
        mViewModel?.getList(1)
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefresh()
        })
        mViewModel?.isLoadMore?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishLoadMore(true)
        })
    }


}

