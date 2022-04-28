package com.yjkj.chainup.new_version.activity.financial

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.bean.ProjectBean
import com.yjkj.chainup.bean.ProjectInfo
import com.yjkj.chainup.databinding.ActivitySaveBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.financial.vm.SaveViewModel


@Route(path = RoutePath.SaveActivity)
class SaveActivity : BaseMVActivity<SaveViewModel?, ActivitySaveBinding?>(){
    @Autowired(name = "bean")
    @JvmField
    var bean: ProjectInfo? = null
    @Autowired(name = "id")
    @JvmField
    var id: String? = null

    override fun setContentView() = R.layout.activity_save
    override fun initData() {
        mViewModel?.bean?.value=bean
        mViewModel?.id?.value=id

    }


}