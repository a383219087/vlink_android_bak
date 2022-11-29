package com.yjkj.chainup.ui.mine.partner

import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.bean.InviteBean
import com.yjkj.chainup.databinding.DialogNewInviteCodeBinding
import com.yjkj.chainup.databinding.DialogNewInviteCodePaetnerBinding
import com.yjkj.chainup.databinding.DialogNewRatrPaetnerBinding
import com.yjkj.chainup.ui.mine.invite.vm.EditInviteCodesViewModel
import com.yjkj.chainup.ui.mine.partner.vm.PaetnerEditInviteCodesViewModel
import com.yjkj.chainup.ui.mine.partner.vm.PaetnerEditRateViewModel


class PartnerEditInviteRateDialog : BaseDialogMVFragment<PaetnerEditRateViewModel?, DialogNewRatrPaetnerBinding>() {



    override fun setContentView() = R.layout.dialog_new_ratr_paetner
    override fun initView() {
        mViewModel?.bean?.value= arguments?.getSerializable("bean") as InviteBean?
        mViewModel?.agentRoles()


    }





}