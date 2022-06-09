package com.yjkj.chainup.ui.buy

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.bean.BuyInfo
import com.yjkj.chainup.bean.PaymentMethod
import com.yjkj.chainup.databinding.ActivityQuickBuySureBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.ui.buy.vm.QuickBuySureViewModel
import com.yjkj.chainup.util.DecimalUtil


@Route(path = RoutePath.QuickBuySureActivity)
class QuickBuySureActivity : BaseMVActivity<QuickBuySureViewModel?, ActivityQuickBuySureBinding?>() {


    @JvmField
    @Autowired(name = "one")
    var checkInfo = BuyInfo()

    @JvmField
    @Autowired(name = "two")
    var checkTwoInfo = BuyInfo()

    @JvmField
    @Autowired(name = "bean")
    var bean: PaymentMethod? = null

    @JvmField
    @Autowired(name = "money")
    var money = "0"


    override fun setContentView() = R.layout.activity_quick_buy_sure
    override fun initData() {
        mViewModel?.activity?.value = mActivity
        mViewModel?.checkInfo?.value = checkInfo
        mViewModel?.checkTwoInfo?.value = checkTwoInfo
        mViewModel?.money?.value = money
        mViewModel?.bean?.value = bean

        if (bean?.price == null) {
            mViewModel?.rate?.value = "暂未获取到汇率"

        } else {
            mViewModel?.rate?.value =
                "您将获取约${
                    DecimalUtil.cutValueByPrecision(
                        (money.toDouble() / bean?.price!!.toDouble()).toString(),
                        2
                    )
                }${checkTwoInfo.fiat}"

        }


    }


}

