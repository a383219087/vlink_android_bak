package com.yjkj.chainup.new_version.activity.documentary

import android.view.Gravity
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.DialogClosePositionBinding
import com.yjkj.chainup.new_version.activity.documentary.vm.AddMoneyViewModel


class AddMoneyDialog : BaseDialogMVFragment<AddMoneyViewModel?, DialogClosePositionBinding>() {


    override fun setGravity(gravity: Int): Int = Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_add_money
    override fun initView() {


    }






}