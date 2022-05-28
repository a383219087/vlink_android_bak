package com.yjkj.chainup.ui.financial

import android.os.Bundle
import androidx.lifecycle.Observer
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.AllFragmentBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.ui.financial.vm.AllViewModel


class AllFragment : BaseMVFragment<AllViewModel?, AllFragmentBinding>() {


    companion object {
        @JvmStatic
        fun newInstance(index: Int): AllFragment {
            val fg = AllFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, index)
            fg.arguments = bundle
            return fg
        }

    }


    override fun setContentView(): Int = R.layout.all_fragment
    override fun initView() {
        mViewModel?.page?.value = arguments?.getInt(ParamConstant.CUR_INDEX)
        mViewModel?.getData()
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefresh()
        })

    }
}