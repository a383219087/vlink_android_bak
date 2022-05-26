package com.yjkj.chainup.ui.documentary

import android.view.Gravity
import com.chainup.contract.bean.CpContractPositionBean
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.databinding.DialogClosePositionBinding
import com.yjkj.chainup.ui.documentary.vm.ClosePositionViewModel


class ClosePositionDialog : BaseDialogMVFragment<ClosePositionViewModel?, DialogClosePositionBinding>() {


    override fun setGravity(gravity: Int): Int = Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_close_position
    override fun initView() {
        mViewModel?.bean?.value= arguments?.getSerializable("bean") as CpContractPositionBean?
         mViewModel?.getData(context!!)

    }






}