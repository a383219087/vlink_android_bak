package com.yjkj.chainup.ui.documentary

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chainup.contract.utils.CpClLogicContractSetting
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseMVActivity
import com.yjkj.chainup.databinding.ActivityDocumentaryBinding
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.ui.documentary.vm.DocumentaryViewModel
import com.yjkj.chainup.util.FmPagerAdapter
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


@Route(path = RoutePath.DocumentaryActivity)
class DocumentaryActivity : BaseMVActivity<DocumentaryViewModel?, ActivityDocumentaryBinding?>() {

    private var mFragments: ArrayList<Fragment>? = ArrayList()
    override fun setContentView() = R.layout.activity_documentary
    override fun initData() {
        getContractPublicInfo()
        val map = HashMap<String, Any>()
        map["uid"] = UserDataService.getInstance().userInfo4UserId
        mViewModel?.startTask(mViewModel?.apiService!!.queryTrader(map), Consumer {
            if (it.data == null) {
                currentStatus(-1)
            } else {
                currentStatus(1)
            }

        })
        mViewModel?.index?.observe(this, Observer {
            mBinding?.vpOrder?.setCurrentItem(it, true)
        })


    }


    private fun currentStatus(status: Int) {
        mViewModel?.status?.value = status
        mFragments?.clear()
        if (status == 1) {
            //交易员页面
            mFragments?.add(FirstFragment.newInstance(status))
            mFragments?.add(MySingleFragment.newInstance(2, ""))
            mFragments?.add(MySingleMoneyFragment.newInstance())
            mBinding?.vpOrder?.adapter = FmPagerAdapter(mFragments, supportFragmentManager)
        } else {
            //首页
            mFragments?.add(FirstFragment.newInstance(status))
            //我的跟单
            mFragments?.add(
                ARouter.getInstance().build(RoutePath.MineFragment).navigation() as Fragment
            )
            mBinding?.vpOrder?.adapter = FmPagerAdapter(mFragments, supportFragmentManager)
        }
        mViewModel?.index?.value = 0
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.DocumentaryActivity_close -> {
                EventBusUtil.post(MessageEvent(MessageEvent.contract_switch_type))
                finish()
            }
            MessageEvent.DocumentaryActivity_index -> {
                mViewModel?.setIndex(1)
                mBinding?.vpOrder?.setCurrentItem(1, true)


            }

        }
    }


    private fun getContractPublicInfo() {
        addDisposable(
            getContractModel().getPublicInfo(
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        saveContractPublicInfo(jsonObject)
                    }
                })
        )
    }

    private fun saveContractPublicInfo(jsonObject: JSONObject) {
        jsonObject.optJSONObject("data").run {
            val contractList = optJSONArray("contractList")
            val marginCoinList = optJSONArray("marginCoinList")
            CpClLogicContractSetting.setContractJsonListStr(mActivity, contractList.toString())
            CpClLogicContractSetting.setContractMarginCoinListStr(
                mActivity,
                marginCoinList.toString()
            )

        }
    }
}

