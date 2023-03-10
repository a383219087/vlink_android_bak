package com.yjkj.chainup.ui.mine.invite

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.sdk.LibCore
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.contract.utils.ShareToolUtil
import com.yjkj.chainup.databinding.ActivityContractAgent1Binding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.ui.mine.invite.vm.ContractAgentViewModel
import com.yjkj.chainup.util.ToastUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @Author lianshangljl
 * @Date 2020-05-04-20:53
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.ContractAgentActivity)
class ContractAgentActivity1 : BaseMVActivity<ContractAgentViewModel?, ActivityContractAgent1Binding?>() {

    var dialog: TDialog? = null
    override fun setContentView() = R.layout.activity_contract_agent1
    override fun initData() {
        mViewModel?.activity?.value = this
        mViewModel?.myBonus()
        mViewModel?.top10()
        mViewModel?.isShowDialog?.observe(this, Observer {
            if (it == 0) {
                return@Observer
            }
            EditInviteCodesDialog().apply {
                val bundle = Bundle()
                bundle.putInt("type", 2)
                bundle.putSerializable("bean", mViewModel?.bean?.value)
                this.arguments = bundle

            }.showDialog(supportFragmentManager, "")

        })
        mBinding?.tvShare?.setOnClickListener {
            val list: ArrayList<String> = arrayListOf()
            val url = UserDataService.getInstance()?.inviteUrl?.split(UserDataService.getInstance()?.inviteCode!!)
                ?.get(0) + mViewModel?.bean?.value?.inviteCode
            list.add(url)
            list.add(url)
            dialog = NewDialogUtils.showInvitationPosters(this, list, object : NewDialogUtils.DialogSharePostersListener {
                override fun saveIamgePosters(imageUrl: String, shareView: View, type: Int) {
                    createShareView(shareView, type)
                    dialog?.dismiss()
                }

                override fun saveIamgePostersNew(imageUrl: String) {

                }
            })
        }

    }

    private fun createShareView(shareView: View, type: Int) {
        if (type == 1) {
            ShareToolUtil.sendLocalShare(this, createViewBitmap(shareView))
        } else {
            ShareToolUtil.saveImageToGallery(this, createViewBitmap(shareView))
        }
        ToastUtils.showToast(LibCore.context.getString(R.string.share_text43))
    }

    private fun createViewBitmap(v: View): Bitmap? {
        val bitmap: Bitmap = Bitmap.createBitmap(
            v.width, v.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        v.draw(canvas)
        return bitmap
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