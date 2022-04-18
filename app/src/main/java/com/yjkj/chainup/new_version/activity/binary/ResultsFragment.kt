package com.yjkj.chainup.new_version.activity.binary

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentNowDocumentaryBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.binary.vm.CurrentViewModel

@Route(path = RoutePath.ResultsFragment)

class ResultsFragment : BaseMVFragment<CurrentViewModel?, FragmentNowDocumentaryBinding>() {
    override fun setContentView(): Int = R.layout.results_fragment
    override fun initView() {
        mViewModel?.activity?.value=mActivity

    }


}