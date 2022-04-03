package com.yjkj.chainup.new_version.activity.invite

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
                ARouter.getInstance().build(RoutePath.EditInviteCodesActivity)
                    .withInt("type", 1)
                    .navigation()

            }

            override fun onclickName() {
            }

            override fun onRealNameCertificat() {
            }

        }

    }

    override fun onResume() {
        super.onResume()
        mViewModel?.myInviteCodes()
    }


}