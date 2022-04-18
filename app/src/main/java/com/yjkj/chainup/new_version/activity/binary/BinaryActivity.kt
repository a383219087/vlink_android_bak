package com.yjkj.chainup.new_version.activity.binary

import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityDocumentaryBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.binary.vm.BinaryViewModel


@Route(path = RoutePath.BinaryActivity)
class BinaryActivity : BaseMVActivity<BinaryViewModel?, ActivityDocumentaryBinding?>(){

    override fun setContentView() = R.layout.activity_binary
    override fun initData() {

    }



    override fun onResume() {
        super.onResume()
    }


}