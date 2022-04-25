package com.yjkj.chainup.new_version.activity.financial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity

import com.yjkj.chainup.databinding.ActivityHolddetailBinding

import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.financial.vm.HolddetailViewModel


@Route(path = RoutePath.Holddetail)
class HolddetailActivity : BaseMVActivity<HolddetailViewModel?, ActivityHolddetailBinding?>(){


    override fun setContentView() = R.layout.activity_holddetail
    override fun initData() {


    }





}