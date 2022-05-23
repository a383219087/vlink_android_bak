package com.yjkj.chainup.ui.financial

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.bean.ProjectBean
import com.yjkj.chainup.databinding.ActivityFinancialBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.financial.vm.UsdtViewModel


@Route(path = RoutePath.UsdtActivity)
class UsdtActivity : BaseMVActivity<UsdtViewModel?, ActivityFinancialBinding?>() {
    @Autowired(name = "bean")
    @JvmField
    var bean: ProjectBean? = null

    override fun setContentView() = R.layout.activity_usdt
    override fun initData() {
        mViewModel?.bean1?.value=bean
     mViewModel?.getData(bean?.id.toString())

    }


}