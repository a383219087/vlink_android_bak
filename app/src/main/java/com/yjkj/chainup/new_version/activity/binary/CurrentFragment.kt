package com.yjkj.chainup.new_version.activity.binary


import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentNowDocumentaryBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.binary.vm.CurrentViewModel


@Route(path = RoutePath.CurrentFragment)

class CurrentFragment : BaseMVFragment<CurrentViewModel?, FragmentNowDocumentaryBinding>() {
    override fun setContentView(): Int = R.layout.current_fragment
    override fun initView() {
        mViewModel?.activity?.value=mActivity

    }


}