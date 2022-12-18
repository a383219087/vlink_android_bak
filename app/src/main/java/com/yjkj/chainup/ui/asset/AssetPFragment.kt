package com.yjkj.chainup.ui.asset

import androidx.fragment.app.FragmentTransaction
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment


open class AssetPFragment : NBaseFragment() {

    override fun setContentView() = R.layout.fragment_asset_p

   var  fragment:AssetFragment?=null



    override fun onVisibleChanged(isVisible: Boolean) {
        super.onVisibleChanged(isVisible)
         if (isVisible){
             fragment=AssetFragment()
             val fragmentTransaction: FragmentTransaction = childFragmentManager.beginTransaction()
             fragmentTransaction.replace(R.id.fl_content,fragment!!)
             fragmentTransaction.commitAllowingStateLoss()
         }


    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
          if (hidden){
              val fragmentTransaction: FragmentTransaction = childFragmentManager.beginTransaction()
              fragment?.let { fragmentTransaction.remove(it) }
          }

    }



    override fun initView() {
        fragment=AssetFragment()
        val fragmentTransaction: FragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fl_content,fragment!!)
        fragmentTransaction.commitAllowingStateLoss()
    }


}





