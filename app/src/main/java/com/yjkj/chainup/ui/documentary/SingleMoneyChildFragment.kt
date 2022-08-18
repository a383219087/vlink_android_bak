package com.yjkj.chainup.ui.documentary

import android.os.Bundle
import com.chainup.contract.bean.CpContractPositionBean
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentSingleMoneyBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.ui.documentary.vm.SingleMoneyViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SingleMoneyChildFragment : BaseMVFragment<SingleMoneyViewModel?, FragmentSingleMoneyBinding?>(){



    companion object {
        @JvmStatic
        fun newInstance(type: Int): SingleMoneyChildFragment {
            val fg = SingleMoneyChildFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.COIN_TYPE, type)
            fg.arguments = bundle
            return fg
        }

    }


    override fun setContentView() = R.layout.fragment_single_money_child

    override fun initView() {
        mViewModel?.index?.value=arguments?.getInt(ParamConstant.COIN_TYPE)
         mViewModel?.getList()




    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.refresh_MyInviteCodesActivity -> {
                mViewModel?.getList()
            }
        }
    }


}