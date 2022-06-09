package com.yjkj.chainup.ui.buy

import androidx.lifecycle.Observer
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseDialogMVFragment
import com.yjkj.chainup.bean.BuyInfo
import com.yjkj.chainup.databinding.DialogConinsListBinding
import com.yjkj.chainup.ui.buy.vm.CoinsViewModel


class CoinsDialog : BaseDialogMVFragment<CoinsViewModel?, DialogConinsListBinding?>() {



    override fun setContentView() = R.layout.dialog_conins_list
    override fun initView() {
        mViewModel?.fiat?.value= arguments?.getString("type")
        mViewModel?.getData()
        mViewModel?.type?.observe(this, Observer{
            if (it>0){
                onSuccessClickListener?.OnSuccess(mViewModel?.item?.value!!)
            }

        })





    }


    private var onSuccessClickListener: OnSuccessClickListener? = null

    interface OnSuccessClickListener {
        fun OnSuccess(info: BuyInfo)
    }

    fun setOnSuccessClickListener(onSuccessClickListener: OnSuccessClickListener?) {
        this.onSuccessClickListener = onSuccessClickListener
    }




}


