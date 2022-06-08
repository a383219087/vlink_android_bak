package com.yjkj.chainup.ui.buy

import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.DialogNewInviteCodeBinding
import com.yjkj.chainup.ui.buy.vm.CoinsViewModel


class CoinsDialog : BaseDialogMVFragment<CoinsViewModel?, DialogNewInviteCodeBinding?>() {



    override fun setContentView() = R.layout.dialog_new_invite_code
    override fun initView() {
//        mViewModel?.type?.value= arguments?.getInt("type")?.or(0)!!
//        mViewModel?.bean?.value= arguments?.getSerializable("bean") as AgentCodeBean?
//        mViewModel?.agentRoles()


    }


    private var onSuccessClickListener: OnSuccessClickListener? = null

    interface OnSuccessClickListener {
        fun OnSuccess(url: String?)
    }

    fun setOnSuccessClickListener(onSuccessClickListener: OnSuccessClickListener?) {
        this.onSuccessClickListener = onSuccessClickListener
    }




}