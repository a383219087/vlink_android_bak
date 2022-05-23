package com.yjkj.chainup.ui.documentary

import android.view.Gravity
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.DialogWinAndStopBinding
import com.yjkj.chainup.ui.documentary.vm.WinAndStopViewModel


class WinAndStopDialog : BaseDialogMVFragment<WinAndStopViewModel?, DialogWinAndStopBinding>() {


    override fun setGravity(gravity: Int): Int = Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_win_and_stop
    override fun initView() {


    }






}