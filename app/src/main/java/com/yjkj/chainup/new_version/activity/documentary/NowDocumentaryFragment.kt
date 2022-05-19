package com.yjkj.chainup.new_version.activity.documentary

import android.os.Bundle
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVFragment
import com.yjkj.chainup.databinding.FragmentNowDocumentaryBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.new_version.activity.documentary.vm.NowDocumentViewModel


class NowDocumentaryFragment : BaseMVFragment<NowDocumentViewModel?, FragmentNowDocumentaryBinding>() {

    companion object {
        @JvmStatic
        fun newInstance(status: Int,type: Int,uid:String): NowDocumentaryFragment {
            val fg = NowDocumentaryFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.CUR_INDEX, status)
            bundle.putInt(ParamConstant.COIN_TYPE, type)
            bundle.putString(ParamConstant.MARKET_NAME, uid)
            fg.arguments = bundle
            return fg
        }

    }


    override fun setContentView(): Int = R.layout.fragment_now_documentary
    override fun initView() {
        mViewModel?.activity?.value=mActivity
        mViewModel?.status?.value=arguments?.getInt(ParamConstant.CUR_INDEX)
        mViewModel?.type?.value=arguments?.getInt(ParamConstant.COIN_TYPE)
        mViewModel?.uid?.value=arguments?.getString(ParamConstant.MARKET_NAME)
        mViewModel?.getList(mActivity!!)

    }


}

