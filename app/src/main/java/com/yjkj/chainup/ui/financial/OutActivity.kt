package com.yjkj.chainup.ui.financial

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.bean.Pos
import com.yjkj.chainup.databinding.ActivityOutBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.financial.vm.OutViewModel


@Route(path = RoutePath.OutActivity)
class OutActivity : BaseMVActivity<OutViewModel?, ActivityOutBinding?>(){

    @Autowired(name = "bean")
    @JvmField
    var id: Pos? = null

    override fun setContentView() = R.layout.activity_out
    override fun initData() {
        mViewModel?.bean?.value=id

    }


}