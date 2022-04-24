package com.yjkj.chainup.new_version.activity.documentary

import android.view.Gravity
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.bean.AgentCodeBean
import com.yjkj.chainup.databinding.DialogDocumentCreateTraderBinding
import com.yjkj.chainup.databinding.DialogDocumentShareBinding
import com.yjkj.chainup.new_version.activity.documentary.vm.CreateTradersViewModel
import com.yjkj.chainup.new_version.activity.documentary.vm.ShareViewModel


class CreateTradersDialog : BaseDialogMVFragment<CreateTradersViewModel?, DialogDocumentCreateTraderBinding?>() {


    override fun setGravity(gravity: Int): Int =Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_document_create_trader
    override fun initView() {
        mViewModel?.uid?.value= arguments?.getString("uid")

    }






}