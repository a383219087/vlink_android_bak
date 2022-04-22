package com.yjkj.chainup.new_version.activity.financial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityFinancialBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.new_version.activity.financial.vm.FinancialViewModel
import kotlinx.android.synthetic.main.activity_documentary.*



@Route(path = RoutePath.UsdtActivity)
class UsdtActivity : BaseMVActivity<FinancialViewModel?, ActivityFinancialBinding?>(){


    override fun setContentView() = R.layout.activity_usdt
    override fun initData() {


    }


}