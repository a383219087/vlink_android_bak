package com.yjkj.chainup.new_version.activity.personalCenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import kotlinx.android.synthetic.main.activity_real_name_success.*

/**
 * @Author lianshangljl
 * @Date 2019/5/20-9:29 AM
 * @Email buptjinlong@163.com
 * @description 实名制认证成功页面
 */
@Route(path = RoutePath.RealNameCertificaionSuccessActivity)
class RealNameCertificaionSuccessActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_real_name_success

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iv_back?.setOnClickListener { finish() }
        tv_common_tip_cerSubmitSuccess?.text = LanguageUtil.getString(this,"common_tip_cerSubmitSuccess")
        tv_common_tip_cerSubmitDesc?.text = LanguageUtil.getString(this,"common_tip_cerSubmitDesc")
    }


}
