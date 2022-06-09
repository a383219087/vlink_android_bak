package com.yjkj.chainup.ui.buy

import android.view.Gravity
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.bean.BuyInfo
import com.yjkj.chainup.bean.PaymentMethod
import com.yjkj.chainup.databinding.DialogPayQuickBinding
import com.yjkj.chainup.ui.buy.vm.PayViewModel


class PayDialog : BaseDialogMVFragment<PayViewModel?, DialogPayQuickBinding?>() {


    override fun setGravity(gravity: Int): Int = Gravity.BOTTOM
    override fun setContentView() = R.layout.dialog_pay_quick
    override fun initView() {
        mViewModel?.checkInfo?.value = arguments?.getSerializable("one") as BuyInfo?
        mViewModel?.checkTwoInfo?.value = arguments?.getSerializable("two") as BuyInfo?
        mViewModel?.money?.value =arguments?.getString("money")
        mViewModel?.bean?.value = arguments?.getSerializable("bean") as PaymentMethod?

        mViewModel?.getData()



    }






}