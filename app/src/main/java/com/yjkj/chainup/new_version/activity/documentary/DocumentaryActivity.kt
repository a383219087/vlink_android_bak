package com.yjkj.chainup.new_version.activity.documentary

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityDocumentaryBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.DocumentaryViewModel


@Route(path = RoutePath.DocumentaryActivity)
class DocumentaryActivity : BaseMVActivity<DocumentaryViewModel?, ActivityDocumentaryBinding?>(){

        private var mFragments: ArrayList<Fragment>? = null
    override fun setContentView() = R.layout.activity_documentary
    override fun initData() {

        mViewModel?.currentStatus()
        mViewModel?.status?.observe(this, Observer {
            mFragments = ArrayList()
            if (it==1){
                mFragments?.add(   ARouter.getInstance().build(RoutePath.FirstFragment).navigation() as Fragment)
                mFragments?.add(   ARouter.getInstance().build(RoutePath.MineFragment).navigation() as Fragment)
                mFragments?.add(   ARouter.getInstance().build(RoutePath.MineFragment).navigation() as Fragment)
                mBinding?.subTabLayout?.setViewPager(mBinding?.vpOrder, arrayOf("首页","我的带单","带单收益"), this, mFragments)
            }else{
                mFragments?.add(   ARouter.getInstance().build(RoutePath.FirstFragment).navigation() as Fragment)
                mFragments?.add(   ARouter.getInstance().build(RoutePath.MineFragment).navigation() as Fragment)
                mBinding?.subTabLayout?.setViewPager(mBinding?.vpOrder, arrayOf("首页","我的跟单"), this, mFragments)
            }

        })



    }



    override fun onResume() {
        super.onResume()
    }


}

