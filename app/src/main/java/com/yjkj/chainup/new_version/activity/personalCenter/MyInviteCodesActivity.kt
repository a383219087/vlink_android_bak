package com.yjkj.chainup.new_version.activity.personalCenter

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityContractAgent1Binding
import com.yjkj.chainup.databinding.ActivityInvitesCodeBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.personalCenter.vm.ContractAgentViewModel
import com.yjkj.chainup.new_version.activity.personalCenter.vm.MyInviteCodesViewModel


@Route(path = RoutePath.MyInviteCodesActivity)
class MyInviteCodesActivity : BaseMVActivity<MyInviteCodesViewModel?, ActivityInvitesCodeBinding?>(){


    override fun setContentView() = R.layout.activity_invites_code
    override fun initData() {
        mViewModel?.myInviteCodes()

    }


}