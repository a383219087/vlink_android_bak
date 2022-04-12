package com.yjkj.chainup.new_version.activity.documentary

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentHistoryDocumentaryBinding
import com.yjkj.chainup.databinding.FragmentNowDocumentaryBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.HistoryDocumentViewModel
import com.yjkj.chainup.new_version.activity.documentary.vm.NowDocumentViewModel


@Route(path = RoutePath.HistoryDocumentaryFragment)
class HistoryDocumentaryFragment : BaseMVFragment<HistoryDocumentViewModel?, FragmentHistoryDocumentaryBinding>() {
    override fun setContentView(): Int = R.layout.fragment_history_documentary
    override fun initView() {

    }


}

