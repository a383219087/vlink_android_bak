package com.yjkj.chainup.new_version.activity.financial

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.AllFragmentBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_contract.fragment.ClCoinSearchItemFragment
import com.yjkj.chainup.new_version.activity.financial.vm.AllViewModel


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
        mViewModel?.page?.value=arguments?.getInt(ParamConstant.CUR_INDEX)
        mViewModel?.getList()

    }
}