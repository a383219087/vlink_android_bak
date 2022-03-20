package com.yjkj.chainup.new_version.activity.leverage

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net_new.JSONUtil
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.HistoryLoan4LeverAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.activity_history_loan.*
import org.json.JSONObject

/**
 * 历史借贷
 */
@Route(path = RoutePath.HistoryLoanActivity)
class HistoryLoanActivity : NBaseActivity() {
    val list = arrayListOf<JSONObject>()
    val adapter = HistoryLoan4LeverAdapter(list)


    var page = 1
    var isScrollStatus = true

    @JvmField
    @Autowired(name = ParamConstant.symbol)
    var symbol = ""

    override fun setContentView() = R.layout.activity_history_loan
    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        initView()
    }

    override fun onResume() {
        super.onResume()
        getHistory(true)
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }

        collapsing_toolbar?.run {
            setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
        }
        collapsing_toolbar?.title = LanguageUtil.getString(this,"leverage_history_borrow")
        /* 过滤 */


        rv_history_loan?.layoutManager = LinearLayoutManager(mActivity)

        adapter.setEmptyView(EmptyForAdapterView(this))
        rv_history_loan?.adapter = adapter

        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            getHistory(true)
        }

        rv_history_loan?.setOnScrollListener(object : RecyclerView.OnScrollListener() {

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
                    getHistory(false)
                }
            }

        })

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
