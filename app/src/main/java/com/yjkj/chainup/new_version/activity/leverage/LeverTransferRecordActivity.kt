package com.yjkj.chainup.new_version.activity.leverage

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net_new.JSONUtil
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.LeverTransferAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.new_version.view.ScreeningPopupWindowView
import com.yjkj.chainup.util.LogUtil
import kotlinx.android.synthetic.main.activity_lever_transfer.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-11-14-23:00
 * @Email buptjinlong@163.com
 * @description 杠杆划转记录页面
 */
@Route(path = RoutePath.LeverTransferRecordActivity)
class LeverTransferRecordActivity : NBaseActivity() {

    override fun setContentView() = R.layout.activity_lever_transfer


    @JvmField
    @Autowired(name = ParamConstant.symbol)
    var symbol = ""

    @JvmField
    @Autowired(name = ParamConstant.COIN_SYMBOL)
    var coinSymbol = ""


    val list = arrayListOf<JSONObject>()
    var adapter = LeverTransferAdapter(list)

    var page = 1
    var isScrollStatus = true
    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        initView()
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.title = NCoinManager.getShowMarket(coinSymbol) + " "+ LanguageUtil.getString(this, "transfer_text_record")

        iv_filter?.setOnClickListener {
            if (spw_layout?.visibility == View.GONE) {
                spw_layout?.visibility = View.VISIBLE
            }
        }

        collapsing_toolbar?.run {
            setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
        }
        getTransferList(true)

        spw_layout?.leverTransferListener = object : ScreeningPopupWindowView.LeverTransferScreeningListener {
            override fun confirmLeverTransferScreening(leverTransferDirection: String) {
                transactionType = leverTransferDirection
                page = 1
                isScrollStatus = true
                getTransferList(true)
            }

        }

        rv_history_loan?.layoutManager = LinearLayoutManager(mActivity)

        adapter.setEmptyView(EmptyForAdapterView(this))
        rv_history_loan?.adapter = adapter


        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            getTransferList(true)
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
                    getTransferList(false)
                }
            }

        })
    }


    var transactionType = ""

    private fun getTransferList(refresh: Boolean) {
        addDisposable(getMainModel().getTransferList(symbol, transactionType, coinSymbol, page.toString(), consumer = object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                if (refresh) {
                    list.clear()
                }
                val json = jsonObject.optJSONObject("data")
                json?.optJSONArray("financeList").run {

                    val jsonList = JSONUtil.arrayToList(this)

                    if (null != jsonList && jsonList.size != 0) {
                        list?.addAll(jsonList)
                        if (jsonList.size < 20) {
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
        }))
    }

}