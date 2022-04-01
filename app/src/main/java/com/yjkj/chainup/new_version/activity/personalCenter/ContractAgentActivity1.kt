package com.yjkj.chainup.new_version.activity.personalCenter

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityContractAgent1Binding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.personalCenter.vm.ContractAgentViewModel

/**
 * @Author lianshangljl
 * @Date 2020-05-04-20:53
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.ContractAgentActivity)
class ContractAgentActivity1 : BaseMVActivity<ContractAgentViewModel?, ActivityContractAgent1Binding?>(){


    override fun setContentView() = R.layout.activity_contract_agent1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}