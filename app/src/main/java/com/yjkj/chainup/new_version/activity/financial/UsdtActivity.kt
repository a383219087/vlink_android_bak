package com.yjkj.chainup.new_version.activity.financial

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.bean.ProjectBean
import com.yjkj.chainup.databinding.ActivityFinancialBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.financial.vm.FinancialViewModel


@Route(path = RoutePath.UsdtActivity)
class UsdtActivity : BaseMVActivity<FinancialViewModel?, ActivityFinancialBinding?>() {
    @Autowired(name = "bean")
    @JvmField
    var bean: ProjectBean? = null

    override fun setContentView() = R.layout.activity_usdt
    override fun initData() {


    }


}