package com.yjkj.chainup.new_version.activity.documentary

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R

import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentFirstBinding
import com.yjkj.chainup.databinding.FragmentFriendsBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.FirstViewModel
import com.yjkj.chainup.new_version.activity.invite.vm.MyFriendsViewModel


@Route(path = RoutePath.FirstFragment)
class FirstFragment : BaseMVFragment<FirstViewModel?, FragmentFirstBinding>() {
    override fun setContentView(): Int = R.layout.fragment_first

    override fun initView() {

    }


}

