package com.yjkj.chainup.new_version.activity.financial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.adapter.CpNVPagerAdapter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.AllFragmentBinding
import com.yjkj.chainup.databinding.FragmentProductBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.financial.vm.AllViewModel
import com.yjkj.chainup.new_version.activity.financial.vm.ProductViewModel
import com.yjkj.chainup.util.FmPagerAdapter


@Route(path = RoutePath.AllFragment)
class Allfragment : BaseMVFragment<AllViewModel?, AllFragmentBinding>() {
    override fun setContentView(): Int = R.layout.all_fragment

    override fun initView() {


    }
}