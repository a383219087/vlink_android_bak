package com.yjkj.chainup.ui.financial

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivitySaveBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.financial.vm.SaveViewModel


@Route(path = RoutePath.SaveActivity)
class SaveActivity : BaseMVActivity<SaveViewModel?, ActivitySaveBinding?>(){

    @Autowired(name = "id")
    @JvmField
    var id: String? = null

    override fun setContentView() = R.layout.activity_save
    override fun initData() {
        mViewModel?.id?.value=id
        id?.let { mViewModel?.getData(it) }

    }


}