package com.yjkj.chainup.new_version.activity.invite

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityContractAgent1Binding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.invite.vm.ContractAgentViewModel

/**
 * @Author lianshangljl
 * @Date 2020-05-04-20:53
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.ContractAgentActivity)
class ContractAgentActivity1 : BaseMVActivity<ContractAgentViewModel?, ActivityContractAgent1Binding?>(){


    override fun setContentView() = R.layout.activity_contract_agent1
    override fun initData() {
        mViewModel?.myBonus()
        mViewModel?.top10()

    }

    override fun onResume() {
        super.onResume()
        mViewModel?.myInviteCodes()
    }


}