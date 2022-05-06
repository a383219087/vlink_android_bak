package com.yjkj.chainup.new_version.activity.grid

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseFragment
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.grid.adapter.AlreadyPerformedAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.fragment_already_perform.*
import kotlinx.android.synthetic.main.fragment_already_perform.recycler_view

import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2021/2/3-6:13 PM
 * @Email buptjinlong@163.com
 * @description
 */
class AlreadyPerformedGridFragment : NBaseFragment() {

    var alreadyPerformedAdapter: AlreadyPerformedAdapter? = null

    companion object {
        @JvmStatic
        fun newInstance(strategyId: String, coin: String, coinInfo: String = "") =
                AlreadyPerformedGridFragment().apply {
                    arguments = Bundle().apply {
                        putString(ParamConstant.GIRD_ID, strategyId)
                        putString(ParamConstant.GIRD_COIN, coin)
                        putString(ParamConstant.GIRD_COIN_INFO, coinInfo)
                    }
                }
    }

    var page = 1

    var isScrollStatus = true
    var list: ArrayList<JSONObject> = arrayListOf()
    var coin = ""
    var strategyId = ""
    override fun initView() {
        arguments?.let {
            strategyId = it.getString(ParamConstant.GIRD_ID, "")
            coin = it.getString(ParamConstant.GIRD_COIN, "")
        }
        tv_profits?.text = "${LanguageUtil.getString(context, "grid_profit")}(${NCoinManager.getShowMarket(coin)})"
        alreadyPerformedAdapter = AlreadyPerformedAdapter(list)
        alreadyPerformedAdapter?.loadMoreModule?.isEnableLoadMore = false
        recycler_view?.layoutManager = LinearLayoutManager(activity)
        alreadyPerformedAdapter?.setEmptyView(EmptyForAdapterView(activity ?: return))
        alreadyPerformedAdapter?.loadMoreModule?.setOnLoadMoreListener {
            getData(true)
        }
        recycler_view?.adapter = alreadyPerformedAdapter
        getData(false)
    }

    fun getData(status: Boolean) {
        if (!status) page = 1
        getFinishGridList(strategyId, status)
    }

    override fun setContentView() = R.layout.fragment_already_perform

    fun initData(isMoreRef: Boolean, list: ArrayList<JSONObject>) {
        if (list.isNotEmpty()) {
            if (!isMoreRef) {
                alreadyPerformedAdapter?.setNewData(list)
            } else {
                alreadyPerformedAdapter?.addData(list)
            }
            val isMore = list.size == 20
            if (isMore) page++
            alreadyPerformedAdapter?.apply {
                alreadyPerformedAdapter?.loadMoreModule?.isEnableLoadMore = true
                if (!isMore) {
                    alreadyPerformedAdapter?.loadMoreModule?.loadMoreEnd(!isMoreRef)
                } else {
                    alreadyPerformedAdapter?.loadMoreModule?.loadMoreComplete()
                }
            }

        } else {
            if (!isMoreRef) alreadyPerformedAdapter?.setNewData(null)
            if (!isMoreRef) alreadyPerformedAdapter?.setEmptyView(EmptyForAdapterView(activity
                    ?: return))
            alreadyPerformedAdapter?.loadMoreModule?.loadMoreEnd(isMoreRef)
        }

    }


    fun getFinishGridList(id: String, refresh: Boolean) {
        addDisposable(getMainModel().getFinishGridList(id, page.toString(), object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                var finishList = json?.optJSONArray("list")
                var count = json?.optInt("count")
                var acy = activity
                if (acy != null && acy is GridExecutionDetailsActivity) {
                    acy.updateTitleHasBeen(count ?: 0)
                }
                var list = JSONUtil.arrayToList(finishList) ?: arrayListOf()

                initData(refresh, list)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(activity, false, msg)
            }
        }))
    }


}