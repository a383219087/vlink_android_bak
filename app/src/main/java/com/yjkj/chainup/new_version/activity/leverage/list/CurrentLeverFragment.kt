package com.yjkj.chainup.new_version.activity.leverage.list

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.CurrencyLendingAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.activity_current_levert.*
import org.json.JSONObject

/**
 * @Author: Bertking
 * @Date：2019/4/4-10:26 AM
 * @Description: 所有当前委托(币币)
 */

class CurrentLeverFragment : NBaseFragment() {


    var isShowCanceled = "0"
    var side = ""
    var type = ""
    var startTime = ""
    var endTime = ""
    val pageSize = 100
    var page = 1
    var isScrollStatus = true

    val list = arrayListOf<JSONObject>()
    var adapter = CurrencyLendingAdapter(list)

    fun initData() {
        getCurrent(true)
    }

    var symbol = ""

    companion object {
        @JvmStatic
        fun newInstance(symbol: String) =
                CurrentLeverFragment().apply {
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
        adapter.setEmptyView(EmptyForAdapterView(context ?: return))
        rv_current_loan?.adapter = adapter
        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            getCurrent(true)
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
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter?.itemCount && isScrollStatus) {
                    page += 1
                    getCurrent(false)
                }
            }

        })
        initData()
    }

    private fun getCurrent(refresh: Boolean) {
        addDisposable(getMainModel().borrowNew(symbol = symbol, startTime = "", endTime = "",
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {

                        jsonObject.optJSONObject("data")?.run {
                            val orderJsonArray = optJSONArray("financeList")
                            if (refresh) {
                                list.clear()
                            }
                            var listJson = JSONUtil.arrayToList(orderJsonArray)
                            if (null != listJson && listJson.size != 0) {
                                list?.addAll(listJson)
                                if (listJson.size < 20) {
                                    isScrollStatus = false
                                }
                            } else {
                                isScrollStatus = false
                            }
                            adapter.setList(list)
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
