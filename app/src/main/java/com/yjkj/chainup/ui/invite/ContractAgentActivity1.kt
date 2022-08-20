package com.yjkj.chainup.ui.invite

import android.os.Bundle
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityContractAgent1Binding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.ui.invite.vm.ContractAgentViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @Author lianshangljl
 * @Date 2020-05-04-20:53
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.ContractAgentActivity)
class ContractAgentActivity1 : BaseMVActivity<ContractAgentViewModel?, ActivityContractAgent1Binding?>(){


    override fun setContentView() = R.layout.activity_contract_agent1
    override fun initData() {
        mViewModel?.activity?.value=this
        mViewModel?.myBonus()
        mViewModel?.top10()
        mViewModel?.isShowDialog?.observe(this, Observer {
            if (it==0){
                return@Observer
            }
            EditInviteCodesDialog().apply {
             val bundle = Bundle()
                bundle.putInt("type", 2)
                bundle.putSerializable("bean",mViewModel?.bean?.value)
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