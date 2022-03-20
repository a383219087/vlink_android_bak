package com.chainup.contract

import android.os.Bundle
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.eventbus.CpMessageEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class CpMainActivity : CpNBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cp_activity_main
    }

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
//        supportFragmentManager
//            .beginTransaction()
//            .add(R.id.fl_content, CpContractFragment())
//            .commit()
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_go_login_page -> {

            }
            CpMessageEvent.sl_contract_go_fundsTransfer_page -> {

            }
        }
    }

}