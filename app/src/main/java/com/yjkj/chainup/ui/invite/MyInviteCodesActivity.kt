package com.yjkj.chainup.ui.invite

import android.os.Bundle
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityInvitesCodeBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.invite.vm.MyInviteCodesViewModel


@Route(path = RoutePath.MyInviteCodesActivity)
class MyInviteCodesActivity : BaseMVActivity<MyInviteCodesViewModel?, ActivityInvitesCodeBinding?>() {


    override fun setContentView() = R.layout.activity_invites_code
    override fun initData() {
        mViewModel?.context?.value = mActivity

        mViewModel?.isShowDialog?.observe(this, Observer {
            if (it==null){
                return@Observer
            }

            EditInviteCodesDialog().apply {
                val bundle = Bundle()
                bundle.putInt("type", 2)
                bundle.putSerializable("bean",it)
                this.arguments = bundle
            }.showDialog(supportFragmentManager,"")

        })

    }

    override fun onResume() {
        super.onResume()
        mViewModel?.myInviteCodes()
    }


}