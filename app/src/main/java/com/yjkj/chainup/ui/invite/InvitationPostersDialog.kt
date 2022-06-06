package com.yjkj.chainup.ui.invite

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.Gravity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.contract.utils.ShareToolUtil
import com.yjkj.chainup.databinding.DialogInvitationPostersBinding
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.ui.invite.vm.InvitationPostersViewModel
import com.yjkj.chainup.util.DisplayUtil


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
        val rxPermissions = activity?.let { RxPermissions(it) }
        rxPermissions?.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            ?.subscribe { granted ->
                if (granted) {
                    mBinding?.rlShare?.isDrawingCacheEnabled = true
                    mBinding?.rlShare?.buildDrawingCache()
                    val bitmap: Bitmap = Bitmap.createBitmap(mBinding?.rlShare!!.drawingCache)
                    ShareToolUtil.sendLocalShare(activity, bitmap)
                } else {
                    DisplayUtil.showSnackBar(activity?.window?.decorView, getString(R.string.warn_storage_permission), false)
                }

            }
    }


}