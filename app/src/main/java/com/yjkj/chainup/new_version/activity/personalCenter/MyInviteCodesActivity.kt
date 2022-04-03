package com.yjkj.chainup.new_version.activity.personalCenter

import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityInvitesCodeBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.new_version.activity.personalCenter.vm.MyInviteCodesViewModel
import com.yjkj.chainup.new_version.view.PersonalCenterView


@Route(path = RoutePath.MyInviteCodesActivity)
class MyInviteCodesActivity : BaseMVActivity<MyInviteCodesViewModel?, ActivityInvitesCodeBinding?>() {


    override fun setContentView() = R.layout.activity_invites_code
    override fun initData() {
        mViewModel?.context?.value = mActivity
        mViewModel?.myInviteCodes()
        mBinding?.toolBar?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                ARouter.getInstance().build(RoutePath.MyInviteCodesActivity)
                    .withInt("type", 1)
                    .navigation()

            }

            override fun onclickName() {
            }

            override fun onRealNameCertificat() {
            }

        }

    }


}