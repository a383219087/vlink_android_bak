package com.yjkj.chainup.new_version.activity.invite

import android.view.Gravity
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.DialogInvitationPostersBinding
import com.yjkj.chainup.new_version.activity.invite.vm.InvitationPostersViewModel


class InvitationPostersDialog : BaseDialogMVFragment<InvitationPostersViewModel?, DialogInvitationPostersBinding?>() {


    override fun setGravity(gravity: Int): Int =Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_invitation_posters
    override fun initView() {
        mViewModel?.url?.value= arguments?.getString("code")
        context?.let { mViewModel?.getData(it) }

    }








}