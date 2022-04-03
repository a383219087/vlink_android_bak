package com.yjkj.chainup.new_version.activity.invite

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.DialogNewInviteCodeBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.invite.vm.EditInviteCodesViewModel


@Route(path = RoutePath.EditInviteCodesActivity)
class EditInviteCodesActivity : BaseMVActivity<EditInviteCodesViewModel?, DialogNewInviteCodeBinding?>() {
    @Autowired(name = "type")
    @JvmField
    var type = 1

    override fun setContentView() = R.layout.dialog_new_invite_code
    override fun initData() {
     mViewModel?.type?.value=type

    }


}