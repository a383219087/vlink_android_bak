package com.yjkj.chainup.ui.documentary

import android.view.Gravity
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.bean.CommissionBean
import com.yjkj.chainup.databinding.DialogDocumentCreateTraderBinding
import com.yjkj.chainup.ui.documentary.vm.CreateTradersViewModel


class CreateTradersDialog : BaseDialogMVFragment<CreateTradersViewModel?, DialogDocumentCreateTraderBinding?>() {


    override fun setGravity(gravity: Int): Int =Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_document_create_trader
    override fun initView() {
        mViewModel?.uid?.value= arguments?.getString("uid")
        mViewModel?.type?.value= arguments?.getInt("type")
           arguments?.getSerializable("bean")?.let {
               mViewModel?.bean?.value= it as CommissionBean
           }

    }






}