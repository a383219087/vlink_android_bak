package com.yjkj.chainup.new_version.activity.otcTrading

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.new_version.fragment.NewVersionOTCTradingFragment

/**
 * @Author lianshangljl
 * @Date 2019/5/21-3:06 PM
 * @Email buptjinlong@163.com
 * @description 资产 activity版本
 */
@Route(path = RoutePath.NewVersionOTCActivity)
class NewVersionOtcActivity : NBaseActivity() {

    @JvmField
    @Autowired(name = ParamConstant.assetTabType)
    var position = 0

    override fun setContentView(): Int {
        return R.layout.activity_version_otc
    }

    val otcTradingFragment = NewVersionOTCTradingFragment()

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        initView()

    }


    override fun initView() {
        supportFragmentManager
                .beginTransaction().add(R.id.rl_fragme, otcTradingFragment).commitAllowingStateLoss()
        var tag = intent?.getIntExtra("tag", 0)?:0
        var bundle = Bundle()
        bundle.putInt("tag",tag)
        otcTradingFragment.arguments=bundle

    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (event.msg_type == MessageEvent.assets_activity_finish_event) {
            finish()
        }
    }

}
