package com.yjkj.chainup.new_version.activity.documentary

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityDocumentaryBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.new_version.activity.documentary.vm.DocumentaryViewModel
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Route(path = RoutePath.DocumentaryActivity)
class DocumentaryActivity : BaseMVActivity<DocumentaryViewModel?, ActivityDocumentaryBinding?>() {

    private var mFragments: ArrayList<Fragment>? = ArrayList()
    override fun setContentView() = R.layout.activity_documentary
    override fun initData() {
        mViewModel?.startTask( mViewModel?.apiService!!.currentStatus(), Consumer {
            if (it.data.status == 1) {
                mFragments?.add(FirstFragment.newInstance(it.data.status))
                mFragments?.add(MySingleFragment.newInstance(2,""))
                mFragments?.add(MySingleMoneyFragment.newInstance())
                mBinding?.subTabLayout?.setViewPager(mBinding?.vpOrder, arrayOf("首页", "我的带单", "带单收益"), this, mFragments)
            } else {
                mFragments?.add(FirstFragment.newInstance(it.data.status))
                mFragments?.add(ARouter.getInstance().build(RoutePath.MineFragment).navigation() as Fragment)
                mBinding?.subTabLayout?.setViewPager(mBinding?.vpOrder, arrayOf("首页", "我的跟单"), this, mFragments)
            }
        })



    }




    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.DocumentaryActivity_close -> {
                EventBusUtil.post(MessageEvent(MessageEvent.contract_switch_type))
                finish()
            }
            MessageEvent.DocumentaryActivity_index -> {
                mBinding?.subTabLayout?.setCurrentTab(1, true)

            }
        }
    }
}

