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
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.BorrowDetailAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.activity_borrow_records.*
import org.json.JSONObject

/**
 * 借贷记录详情
 */
@Route(path = RoutePath.BorrowRecordsActivity)
class BorrowRecordsActivity : NBaseActivity() {
    val list = arrayListOf<JSONObject>()
    val adapter = BorrowDetailAdapter(list)

    @JvmField
    @Autowired(name = ParamConstant.ORDER_ID)
    var orderId = ""

    var page = 1
    var isScrollStatus = true

    override fun setContentView() = R.layout.activity_borrow_records

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        initView()
    }

    override fun onResume() {
        super.onResume()
        getInfo(true)
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

        rv_detail?.layoutManager = LinearLayoutManager(mActivity)
        adapter.setEmptyView(EmptyForAdapterView(this))
        rv_detail?.adapter = adapter
        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            getInfo(true)
        }

        rv_detail?.setOnScrollListener(object : RecyclerView.OnScrollListener() {

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
                    getInfo(false)
                }
            }

        })
    }


    private fun getInfo(refresh: Boolean) {
        addDisposable(getMainModel().getDetail4Lever(id = orderId, page = page.toString(),
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        swipe_refresh?.isRefreshing = false
                        if (refresh) {
                            list.clear()
                        }
                        val json = jsonObject.optJSONObject("data")
                        val orderJsonArray = json?.optJSONArray("financeList")
                        orderJsonArray?.run {

                            val jsonList = JSONUtil.arrayToList(orderJsonArray)
                            if (null != jsonList && jsonList.size != 0) {
                                list.addAll(jsonList)
                                if (jsonList.size < 20) {
                                    isScrollStatus = false
                                }
                            }else {
                                isScrollStatus = false
                            }

                            adapter.setList(list)
                        }
                    }
                }))
    }


}
