package com.yjkj.chainup.new_version.activity.binary

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityDocumentaryBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.binary.vm.BinaryViewModel


@Route(path = RoutePath.BinaryActivity)
class BinaryActivity : BaseMVActivity<BinaryViewModel?, ActivityDocumentaryBinding?>(){

//        private var mFragments: ArrayList<Fragment>? = null
    override fun setContentView() = R.layout.activity_binary
    override fun initData() {
//        mFragments = ArrayList()
//        mFragments?.add(   ARouter.getInstance().build(RoutePath.FirstFragment).navigation() as Fragment)
//        mFragments?.add(   ARouter.getInstance().build(RoutePath.MineFragment).navigation() as Fragment)
//        mBinding?.subTabLayout?.setViewPager(mBinding?.vpOrder, arrayOf("首页","我的跟单"), this, mFragments)

    }



    override fun onResume() {
        super.onResume()
    }


}