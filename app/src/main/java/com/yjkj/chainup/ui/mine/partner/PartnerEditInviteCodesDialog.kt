package com.yjkj.chainup.ui.mine.partner

import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.databinding.DialogNewInviteCodeBinding
import com.yjkj.chainup.databinding.DialogNewInviteCodePaetnerBinding
import com.yjkj.chainup.ui.mine.invite.vm.EditInviteCodesViewModel
import com.yjkj.chainup.ui.mine.partner.vm.PaetnerEditInviteCodesViewModel


class PartnerEditInviteCodesDialog : BaseDialogMVFragment<PaetnerEditInviteCodesViewModel?, DialogNewInviteCodePaetnerBinding>() {



    override fun setContentView() = R.layout.dialog_new_invite_code_paetner
    override fun initView() {
        mViewModel?.conetxt?.value=context
        mViewModel?.type?.value= arguments?.getInt("type")?.or(0)!!
        mViewModel?.bean?.value= arguments?.getSerializable("bean") as AgentCodeBean?
        mViewModel?.agentRoles()


    }





}