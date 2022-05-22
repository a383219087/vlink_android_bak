package com.yjkj.chainup.new_version.activity.documentary

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentMyTradersBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.documentary.vm.MyTradersModel


class MyTradersFragment : BaseMVFragment<MyTradersModel?, FragmentMyTradersBinding>() {

    companion object {
        @JvmStatic
        fun newInstance(status: Int,uid:String): MyTradersFragment {
            val fg = MyTradersFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, status)
            bundle.putString(ParamConstant.MARKET_NAME, uid)
            fg.arguments = bundle
            return fg
        }

    }


    override fun setContentView(): Int = R.layout.fragment_my_traders
    override fun initView() {
        mViewModel?.activity?.value=mActivity
        mViewModel?.type?.value=arguments?.getInt(ParamConstant.CUR_INDEX)
        mViewModel?.uid?.value=arguments?.getString(ParamConstant.MARKET_NAME)
        mViewModel?.getList()

    }


}

