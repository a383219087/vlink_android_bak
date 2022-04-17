package com.yjkj.chainup.new_version.activity.documentary

import android.view.Gravity
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.DialogClosePositionBinding
import com.yjkj.chainup.new_version.activity.documentary.vm.ClosePositionViewModel


class ClosePositionDialog : BaseDialogMVFragment<ClosePositionViewModel?, DialogClosePositionBinding>() {


    override fun setGravity(gravity: Int): Int = Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_close_position
    override fun initView() {


    }






}