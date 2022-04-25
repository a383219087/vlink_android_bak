package com.yjkj.chainup.new_version.activity.financial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity

import com.yjkj.chainup.databinding.ActivitySaveBinding
import com.yjkj.chainup.db.constant.RoutePath

import com.yjkj.chainup.new_version.activity.financial.vm.SaveViewModel


@Route(path = RoutePath.SaveActivity)
class SaveActivity : BaseMVActivity<SaveViewModel?, ActivitySaveBinding?>(){


    override fun setContentView() = R.layout.activity_save
    override fun initData() {


    }


}