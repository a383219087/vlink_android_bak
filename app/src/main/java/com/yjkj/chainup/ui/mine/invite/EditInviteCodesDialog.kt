package com.yjkj.chainup.ui.mine.invite

import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.databinding.DialogNewInviteCodeBinding
import com.yjkj.chainup.ui.mine.invite.vm.EditInviteCodesViewModel


class EditInviteCodesDialog : BaseDialogMVFragment<EditInviteCodesViewModel?, DialogNewInviteCodeBinding?>() {



    override fun setContentView() = R.layout.dialog_new_invite_code
    override fun initView() {
        mViewModel?.type?.value= arguments?.getInt("type")?.or(0)!!
        mViewModel?.bean?.value= arguments?.getSerializable("bean") as AgentCodeBean?
        mViewModel?.agentRoles()


    }





}