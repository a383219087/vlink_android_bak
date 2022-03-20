package com.yjkj.chainup.new_version.activity.oldgrid

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.net_new.JSONUtil
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.activity.oldgrid.adapter.OldAiGridAdapter
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
//@Route(path = RoutePath.HistoryGridActivity)
class OldHistoryGridActivity : NBaseActivity() {


    @JvmField
    @Autowired(name = ParamConstant.COIN_SYMBOL)
    var symbol = ""


    override fun setContentView() = R.layout.activity_history_grid

    var oldAiGridAdapter: OldAiGridAdapter? = null

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
            oldAiGridAdapter?.loadMoreModule?.isEnableLoadMore = false
            getData(false)
        }

        oldAiGridAdapter = OldAiGridAdapter(list, null, true)
        oldAiGridAdapter?.loadMoreModule?.isEnableLoadMore = false
        recycler_view?.layoutManager = LinearLayoutManager(this@OldHistoryGridActivity)
        oldAiGridAdapter?.setEmptyView(EmptyForAdapterView(this@OldHistoryGridActivity))
        oldAiGridAdapter?.loadMoreModule?.setOnLoadMoreListener {
            getData(true)
        }

        recycler_view?.adapter = oldAiGridAdapter
        getData(false)
        swipe_refresh.setColorSchemeColors(ContextUtil.getColor(R.color.colorPrimary))
    }

    fun getData(status: Boolean) {
        if (!status) page = 1
        getStrategyList(status)
    }


    fun getStrategyList(refresh: Boolean) {
        addDisposable(getMainModel().getStrategyList(symbol, "0", page.toString(), "20", object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data") ?: return
                var strategyVoList = json.optJSONArray("strategyVoList") ?: return
                var list = JSONUtil.arrayToList(strategyVoList) ?: arrayListOf()
                initData(refresh, list)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(this@OldHistoryGridActivity, false, msg)
                swipe_refresh?.isRefreshing = false
            }
        }))
    }

    fun initData(isMoreRef: Boolean, list: ArrayList<JSONObject>) {
        if (list.isNotEmpty()) {
            if (!isMoreRef) {
                oldAiGridAdapter?.setNewData(list)
            } else {
                oldAiGridAdapter?.addData(list)
            }
            val isMore = list.size == 20
            if (isMore) page++
            oldAiGridAdapter?.apply {
                oldAiGridAdapter?.loadMoreModule?.isEnableLoadMore = true
                if (!isMore) {
                    oldAiGridAdapter?.loadMoreModule?.loadMoreEnd(!isMoreRef)
                } else {
                    oldAiGridAdapter?.loadMoreModule?.loadMoreComplete()
                }
            }

        } else {
            if (!isMoreRef) oldAiGridAdapter?.setNewData(null)
            if (!isMoreRef) oldAiGridAdapter?.setEmptyView(EmptyForAdapterView(this))
            oldAiGridAdapter?.loadMoreModule?.loadMoreEnd(isMoreRef)
        }
        swipe_refresh?.isRefreshing = false
    }


}