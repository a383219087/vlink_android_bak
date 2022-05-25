package com.yjkj.chainup.ui.documentary

import android.view.Gravity
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.ActivityTradersApplyBinding
import com.yjkj.chainup.ui.documentary.vm.ApplyTradersViewModel


class ApplyTradersDialog : BaseDialogMVFragment<ApplyTradersViewModel?, ActivityTradersApplyBinding?>(){


    override fun setGravity(gravity: Int): Int = Gravity.CENTER
    override fun setContentView() = R.layout.activity_traders_apply
    override fun initView() {
    mViewModel?.currentStatus()

    }




}