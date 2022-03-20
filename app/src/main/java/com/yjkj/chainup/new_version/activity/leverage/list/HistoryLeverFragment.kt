package com.yjkj.chainup.new_version.activity.leverage.list

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net_new.JSONUtil
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.HistoryLoan4LeverAdapter
import com.yjkj.chainup.new_version.adapter.NCurrentEntrustAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.activity_contract_current_entrust.swipe_refresh
import kotlinx.android.synthetic.main.activity_current_levert.*
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019/4/4-10:26 AM
 * @Description: 所有当前委托(币币)
 */

class HistoryLeverFragment : NBaseFragment() {


    var isShowCanceled = "0"
    var side = ""
    var type = ""
    var startTime = ""
    var endTime = ""
    val pageSize = 100
    var page = 1
    var isScrollStatus = true


    fun initData() {
        getHistory(true)
    }


    var list = ArrayList<JSONObject>()
    var curEntrustAdapter  = HistoryLoan4LeverAdapter(list)

    var symbol = ""

    companion object {
        @JvmStatic
        fun newInstance(symbol: String) =
                HistoryLeverFragment().apply {
                    arguments = Bundle().apply {
                        putString(ParamConstant.symbol, symbol)
                    }
                }
    }

    override fun loadData() {
        arguments.let {
            orderType = it?.getString(ParamConstant.symbol, "")
                    ?: ""
        }
    }

    var orderType = ParamConstant.BIBI_INDEX

    override fun setContentView() = R.layout.activity_current_levert


    override fun initView() {


        rv_current_loan?.layoutManager = LinearLayoutManager(mActivity)
        curEntrustAdapter.setEmptyView(EmptyForAdapterView(context ?: return))
        rv_current_loan?.adapter = curEntrustAdapter
        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            getHistory(true)
        }

        rv_current_loan?.setOnScrollListener(object : RecyclerView.OnScrollListener() {

            var lastVisibleItem = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                var layoutManager: LinearLayoutManager = recyclerView?.layoutManager as LinearLayoutManager
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == curEntrustAdapter.itemCount && isScrollStatus) {
                    page += 1
                    getHistory(false)
                }
            }

        })
        initData()
    }

    private fun getHistory(refresh: Boolean) {
        addDisposable(getMainModel().borrowHistory(symbol = symbol, startTime = "", endTime = "",
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        if (refresh) {
                            list.clear()
                        }
                        jsonObject.optJSONObject("data")?.run {
                            val orderJsonArray = optJSONArray("financeList")
                            var jsonList = JSONUtil.arrayToList(orderJsonArray)
                            if (null != jsonList && jsonList.size > 0) {
                                list.addAll(jsonList)
                                if (jsonList.size < 20) {
                                    isScrollStatus = false
                                }
                            }else {
                                isScrollStatus = false
                            }

                            curEntrustAdapter.setList(list)
                        }
                        swipe_refresh?.isRefreshing = false
                    }

                    override fun onResponseFailure(code: Int, msg: String?) {
                        super.onResponseFailure(code, msg)
                        swipe_refresh?.isRefreshing = false
                    }
                }, page = page.toString()))
    }
}
