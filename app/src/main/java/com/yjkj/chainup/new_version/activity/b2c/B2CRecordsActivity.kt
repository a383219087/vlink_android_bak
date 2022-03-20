package com.yjkj.chainup.new_version.activity.b2c

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.B2CRecordsAdapter
import com.yjkj.chainup.new_version.view.ASSETTOPUP
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.new_version.view.ScreeningPopupWindowView
import kotlinx.android.synthetic.main.activity_b2_crecords.*
import org.json.JSONObject

/**
 * @description:提现,充值记录(B2C)
 * @author Bertking
 * @date 2019-10-23 AM
 *
 * Done
 */
@Route(path = RoutePath.B2CRecordsActivity)
class B2CRecordsActivity : NBaseActivity() {
    val list = arrayListOf<JSONObject>()
    val adapter = B2CRecordsAdapter(list)

    var page: Int = 1
    var isScrollStatus = true

    var isFirst = true

    var symbol: String = PublicInfoDataService.getInstance().coinInfo4B2c

    var startTime: String = ""
    var endTime: String = ""
    @JvmField
    @Autowired(name = ParamConstant.OPTION_TYPE)
    var recordType = ParamConstant.RECHARGE

    override fun setContentView() = R.layout.activity_b2_crecords

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }

    override fun initView() {

        showTitle()

        iv_filter?.setOnClickListener {

            if (spw_layout?.visibility == View.VISIBLE) {
                spw_layout?.visibility = View.GONE
            } else {
                spw_layout?.visibility = View.VISIBLE
                if (isFirst) {
                    isFirst = false
                    spw_layout?.setMage()
                }
            }
        }

        //展示过滤框
        spw_layout?.setInitView(ASSETTOPUP)
        spw_layout?.assetTopUpListener = object : ScreeningPopupWindowView.AssetTopUpListener {
            override fun returnAssetTopUpTime(beginTime: String, end: String) {
                Log.d(TAG, "======S:$startTime,E:$end=======")

                startTime = beginTime
                endTime = end
                when (recordType) {
                    ParamConstant.RECHARGE -> {
                        fiatDepositList()
                    }

                    ParamConstant.WITHDRAW -> {
                        fiatWithdrawList()
                    }
                }
                spw_layout?.visibility = View.GONE
            }
        }


        /**
         * 下拉刷新
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            when (recordType) {
                ParamConstant.RECHARGE -> {
                    fiatDepositList()
                }

                ParamConstant.WITHDRAW -> {
                    fiatWithdrawList()
                }
            }
        }


        rv_records?.layoutManager = LinearLayoutManager(mActivity)
        adapter.setEmptyView(EmptyForAdapterView(this))
        rv_records?.adapter = adapter
        when (recordType) {
            ParamConstant.RECHARGE -> {
                fiatDepositList()
            }

            ParamConstant.WITHDRAW -> {
                fiatWithdrawList()
            }
        }
    }

    private fun showTitle() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM

        val title = when (recordType) {
            ParamConstant.RECHARGE -> {
                getString(R.string.b2c_text_rechargeRecord)
            }

            ParamConstant.WITHDRAW -> {
                getString(R.string.b2c_text_withdrawRecord)
            }

            else -> {
                ""
            }
        }

        collapsing_toolbar?.title = symbol + title

        tv_number_title?.text = when (recordType) {
            ParamConstant.RECHARGE -> {
                getString(R.string.b2c_text_rechargeNum)
            }

            ParamConstant.WITHDRAW -> {
                getString(R.string.b2c_text_withdrawNum)
            }

            else -> {
                ""
            }
        }

    }


    /**
     * 充值记录
     */
    private fun fiatDepositList() {
        addDisposable(getMainModel().fiatDepositList(symbol = symbol,
                startTime = startTime, endTime = endTime,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        swipe_refresh?.isRefreshing = false
                        list.clear()
                        jsonObject.optJSONObject("data")?.run {
                            val orderJsonArray = optJSONArray("financeList")
                            if (orderJsonArray?.length() != 0) {
                                orderJsonArray?.run {
                                    for (i in 0 until orderJsonArray.length()) {
                                        list.add(orderJsonArray.optJSONObject(i))
                                    }
                                }
                                adapter.setList(list)
                            }else{
                                adapter.setList(arrayListOf())
                            }
                        }
                    }

                    override fun onResponseFailure(code: Int, msg: String?) {
                        super.onResponseFailure(code, msg)
                        swipe_refresh?.isRefreshing = false
                    }
                }))
    }


    /**
     * 提现记录
     */
    private fun fiatWithdrawList() {
        addDisposable(getMainModel().fiatWithdrawList(symbol = symbol,
                startTime = startTime, endTime = endTime,
                consumer = object : NDisposableObserver(mActivity) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        swipe_refresh?.isRefreshing = false
                        list.clear()
                        jsonObject.optJSONObject("data")?.run {
                            val orderJsonArray = optJSONArray("financeList") ?: return
                            if (orderJsonArray.length() != 0) {
                                orderJsonArray.run {
                                    for (i in 0 until orderJsonArray.length()) {
                                        list.add(orderJsonArray.optJSONObject(i))
                                    }
                                }
                                adapter.setList(list)
                            } else {
                                adapter.setList(arrayListOf())
                            }
                        }
                    }

                    override fun onResponseFailure(code: Int, msg: String?) {
                        super.onResponseFailure(code, msg)
                        swipe_refresh?.isRefreshing = false
                    }
                }))
    }

}
