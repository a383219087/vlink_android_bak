package com.yjkj.chainup.new_version.activity.invite

import android.os.Bundle
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityInvitesCodeBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.invite.vm.MyInviteCodesViewModel
import com.yjkj.chainup.new_version.view.PersonalCenterView


@Route(path = RoutePath.MyInviteCodesActivity)
class MyInviteCodesActivity : BaseMVActivity<MyInviteCodesViewModel?, ActivityInvitesCodeBinding?>() {


    override fun setContentView() = R.layout.activity_invites_code
    override fun initData() {
        mViewModel?.context?.value = mActivity

        mBinding?.toolBar?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                EditInviteCodesDialog().apply {
                    val bundle = Bundle()
                    bundle.putInt("type", 1)
                    this.arguments = bundle

                }.showDialog(supportFragmentManager,"")

            }

            override fun onclickName() {
            }

            override fun onRealNameCertificat() {
            }

        }
        mViewModel?.isShowDialog?.observe(this, Observer {
            if (it==null){
                return@Observer
            }

            EditInviteCodesDialog().apply {
                val bundle = Bundle()
                bundle.putInt("type", 2)
                bundle.putSerializable("bean",it)
                this.arguments = bundle
            }.showDialog(supportFragmentManager,"")

        })

    }

    override fun onResume() {
        super.onResume()
        mViewModel?.myInviteCodes()
    }


}