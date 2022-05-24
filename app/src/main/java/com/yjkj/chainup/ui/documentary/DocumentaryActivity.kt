package com.yjkj.chainup.ui.documentary

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityDocumentaryBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.ui.documentary.vm.DocumentaryViewModel
import com.yjkj.chainup.util.FmPagerAdapter
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Route(path = RoutePath.DocumentaryActivity)
class DocumentaryActivity : BaseMVActivity<DocumentaryViewModel?, ActivityDocumentaryBinding?>() {

    private var mFragments: ArrayList<Fragment>? = ArrayList()
    override fun setContentView() = R.layout.activity_documentary
    override fun initData() {
        mViewModel?.startTask( mViewModel?.apiService!!.currentStatus(), Consumer {
            currentStatus(it.data.status)
        })
        mViewModel?.index?.observe(this , Observer {
            mBinding?.vpOrder?.setCurrentItem(it,true)
        })


    }



    private fun  currentStatus(status:Int){
        mViewModel?.status?.value=status
        if (status == 1) {
            mFragments?.add(FirstFragment.newInstance(status))
            mFragments?.add(MySingleFragment.newInstance(2,""))
            mFragments?.add(MySingleMoneyFragment.newInstance())
            mBinding?.vpOrder?.adapter = FmPagerAdapter(mFragments, supportFragmentManager)

        } else {
            mFragments?.add(FirstFragment.newInstance(status))
            mFragments?.add(ARouter.getInstance().build(RoutePath.MineFragment).navigation() as Fragment)
            mBinding?.vpOrder?.adapter = FmPagerAdapter(mFragments, supportFragmentManager)
        }
        mViewModel?.index?.value=0
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
                mBinding?.vpOrder?.setCurrentItem(1,true)


            }
        }
    }
}

