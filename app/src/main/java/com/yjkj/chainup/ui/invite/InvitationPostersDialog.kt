package com.yjkj.chainup.ui.invite

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.Gravity
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.chainup.contract.utils.CpShareToolUtil
import com.common.sdk.LibCore
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.contract.utils.ShareToolUtil
import com.yjkj.chainup.databinding.DialogInvitationPostersBinding
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.ui.invite.vm.InvitationPostersViewModel
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.ToastUtils


class InvitationPostersDialog : BaseDialogMVFragment<InvitationPostersViewModel?, DialogInvitationPostersBinding?>() {


    override fun setGravity(gravity: Int): Int = Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_invitation_posters
    override fun initView() {
        mViewModel?.url?.value = UserDataService.getInstance()?.inviteUrl?.split(UserDataService.getInstance()?.inviteCode!!)
            ?.get(0) + arguments?.getString("code")
        context?.let { mViewModel?.getData(it) }
        mBinding?.tvShare?.setOnClickListener {
            doShare()
        }


    }

    @SuppressLint("CheckResult")
    private fun doShare() {
        mBinding?.llShareAll1?.isDrawingCacheEnabled = true
        mBinding?.llShareAll1?.buildDrawingCache()
        val bitmap: Bitmap = Bitmap.createBitmap(mBinding?.llShareAll1!!.drawingCache)
//        ShareToolUtil.sendLocalShare(activity, bitmap)
        ToastUtils.showToast(LibCore.context.getString(R.string.share_text43))


    }


}