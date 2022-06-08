package com.yjkj.chainup.ui.buy

import android.view.Gravity
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.DialogPayQuickBinding
import com.yjkj.chainup.ui.buy.vm.PayViewModel


class PayDialog : BaseDialogMVFragment<PayViewModel?, DialogPayQuickBinding?>() {


    override fun setGravity(gravity: Int): Int = Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_pay_quick
    override fun initView() {
//        mViewModel?.type?.value= arguments?.getInt("type")?.or(0)!!
//        mViewModel?.bean?.value= arguments?.getSerializable("bean") as AgentCodeBean?
//        mViewModel?.agentRoles()


    }






}