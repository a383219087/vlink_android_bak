package com.yjkj.chainup.new_version.activity.grid

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.grid.adapter.AiGridAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.ContextUtil
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.activity_history_grid.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2021/2/1-5:30 PM
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.HistoryGridActivity)
class HistoryGridActivity : NBaseActivity() {


    @JvmField
    @Autowired(name = ParamConstant.COIN_SYMBOL)
    var symbol = ""


    override fun setContentView() = R.layout.activity_history_grid

    var aiGridAdapter: AiGridAdapter? = null

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
    }

    var page = 1

    var isScrollStatus = true
    var isShowMorelist = true

    var list: ArrayList<JSONObject> = arrayListOf()
    override fun initView() {
        super.initView()
        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            aiGridAdapter?.loadMoreModule?.isEnableLoadMore = false
            getData(false)
        }

        aiGridAdapter = AiGridAdapter(list, null, true)
        aiGridAdapter?.loadMoreModule?.isEnableLoadMore = false
        recycler_view?.layoutManager = LinearLayoutManager(this@HistoryGridActivity)
        aiGridAdapter?.setEmptyView(EmptyForAdapterView(this@HistoryGridActivity))
        aiGridAdapter?.loadMoreModule?.setOnLoadMoreListener {
            getData(true)
        }

        recycler_view?.adapter = aiGridAdapter
        getData(false)
        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))
    }

    fun getData(status: Boolean) {
        if (!status) page = 1
        getStrategyList(status)
    }


    fun getStrategyList(refresh: Boolean) {
        addDisposable(getMainModel().getStrategyList(true, symbol, "0", page.toString(), "20", object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data") ?: return
                var strategyVoList = json.optJSONArray("strategyVoList") ?: return
                var list = JSONUtil.arrayToList(strategyVoList) ?: arrayListOf()
                initData(refresh, list)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(this@HistoryGridActivity, false, msg)
                swipe_refresh?.isRefreshing = false
            }
        }))
    }

    fun initData(isMoreRef: Boolean, list: ArrayList<JSONObject>) {
        if (list.isNotEmpty()) {
            if (!isMoreRef) {
                aiGridAdapter?.setNewData(list)
            } else {
                aiGridAdapter?.addData(list)
            }
            val isMore = list.size == 20
            if (isMore) page++
            aiGridAdapter?.apply {
                aiGridAdapter?.loadMoreModule?.isEnableLoadMore = true
                if (!isMore) {
                    aiGridAdapter?.loadMoreModule?.loadMoreEnd(!isMoreRef)
                } else {
                    aiGridAdapter?.loadMoreModule?.loadMoreComplete()
                }
            }

        } else {
            if (!isMoreRef) aiGridAdapter?.setNewData(null)
            if (!isMoreRef) aiGridAdapter?.setEmptyView(EmptyForAdapterView(this))
            aiGridAdapter?.loadMoreModule?.loadMoreEnd(isMoreRef)
        }
        swipe_refresh?.isRefreshing = false
    }


}