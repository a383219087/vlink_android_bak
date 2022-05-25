package com.yjkj.chainup.new_version.activity.b2c

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.B2CCashFlowAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.new_version.view.ScreeningPopupWindowView
import kotlinx.android.synthetic.main.activity_b2_ccash_flow.*
import org.json.JSONObject

/**
 * @description:资金流水(B2C)
 * @author Bertking
 * @date 2019-10-22 AM
 */
@Route(path = RoutePath.B2CCashFlowActivity)
class B2CCashFlowActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_b2_ccash_flow

    val list = arrayListOf<JSONObject>()
    val adapter = B2CCashFlowAdapter(list)
    var isFrist = true
    var isRecharge = true

    var symbol = ""
    var startTime = ""
    var endTime = ""


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        if (isRecharge) {
            fiatDepositList()
        } else {
            fiatWithdrawList()
        }
    }

    override fun initView() {
        rv_cash_flow?.layoutManager = LinearLayoutManager(mActivity)
        adapter.setEmptyView(EmptyForAdapterView(this))
        rv_cash_flow?.adapter = adapter

        adapter?.setOnItemClickListener { adapter, view, position ->
            if (list.isNotEmpty()) {
                ArouterUtil.navigation(RoutePath.B2CCashFlowDetailActivity, Bundle().apply {
                    putString("detail_data", list[position].toString())
                    putBoolean("isRecharge", isRecharge)
                })
            }

        }

        showTitle()
        fiatDepositList()
    }


    private fun showTitle() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.run {
            setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            expandedTitleGravity = Gravity.BOTTOM
            title =  LanguageUtil.getString(mActivity, "assets_action_journalaccount")
        }
        iv_filter?.setOnClickListener {
            if (spw_layout?.visibility == View.GONE) {
                spw_layout?.visibility = View.VISIBLE
            } else {
                spw_layout?.visibility = View.GONE
            }
        }
        spw_layout?.cashFlow4B2CListener = object : ScreeningPopupWindowView.OTCFundFlowingWaterListenre {
            override fun returnOTCFundFlowingWater(coin: String, waterType: String, begin: String, end: String) {
                symbol = coin
                startTime = begin
                endTime = end
                when (waterType) {
                    // 充值
                    ParamConstant.TRANSFER_RECHARGE_RECORD -> {
                        isRecharge = true
                        fiatDepositList()
                    }
                    // 提现
                    ParamConstant.TRANSFER_WITHDRAW_RECORD -> {
                        isRecharge = false
                        fiatWithdrawList()
                    }
                }
            }

        }
    }

    /**
     * 充值记录
     */
    private fun fiatDepositList() {
        addDisposable(getMainModel().fiatDepositList(symbol = symbol, startTime = startTime, endTime = endTime,
                consumer = MyDisposableObserver()))
    }


    /**
     * 提现记录
     */
    private fun fiatWithdrawList() {
        addDisposable(getMainModel().fiatWithdrawList(symbol = symbol,
                startTime = startTime,
                endTime = endTime,
                consumer = MyDisposableObserver()))
    }


    inner class MyDisposableObserver : NDisposableObserver(mActivity) {
        override fun onResponseSuccess(jsonObject: JSONObject) {
            list.clear()
            jsonObject.optJSONObject("data")?.run {
                val orderJsonArray = optJSONArray("financeList")
                if (orderJsonArray != null && orderJsonArray.length() > 0) {
                    orderJsonArray.run {
                        for (i in 0 until orderJsonArray.length()) {
                            list.add(orderJsonArray.optJSONObject(i))
                        }
                    }
                }
            }
            renderData()
        }

        override fun onResponseFailure(code: Int, msg: String?) {
            super.onResponseFailure(code, msg)
            renderData(true)
        }

    }

    private fun renderData(isClear: Boolean = false) {
        adapter.isRecharge = isRecharge
        if (isClear) list.clear()
        adapter.setList(list)
    }

}
