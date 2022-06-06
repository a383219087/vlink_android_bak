package com.yjkj.chainup.ui.invite

import android.os.Bundle
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityInvitesCodeBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.ui.invite.vm.MyInviteCodesViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Route(path = RoutePath.MyInviteCodesActivity)
class MyInviteCodesActivity : BaseMVActivity<MyInviteCodesViewModel?, ActivityInvitesCodeBinding?>() {


    override fun setContentView() = R.layout.activity_invites_code
    override fun initData() {
        mViewModel?.context?.value = mActivity
        mViewModel?.isRefreshing?.observe(this, Observer {
            mBinding?.twinklingRefreshLayout?.finishRefresh()
        })
        mViewModel?.isShowDialog?.observe(this, Observer {
            if (it==null){
                return@Observer
            }

            EditInviteCodesDialog().apply {
                val bundle = Bundle()
                bundle.putInt("type", 2)
                bundle.putSerializable("bean",it)
                this.arguments = bundle
            }.showDialog(supportFragmentManager,"")

        })

    }

    override fun onResume() {
        super.onResume()
        mViewModel?.myInviteCodes()
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.refresh_MyInviteCodesActivity -> {
                mViewModel?.myInviteCodes()
            }


        }
    }

}