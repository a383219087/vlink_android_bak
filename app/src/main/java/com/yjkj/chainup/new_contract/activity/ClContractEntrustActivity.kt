package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.contract.sdk.ContractSDKAgent.context
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.data.bean.TabInfo
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.utils.onLineText
import com.yjkj.chainup.contract.widget.ContractEntrustTabWidget
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_contract.adapter.ClContractPlanEntrustAdapter
import com.yjkj.chainup.new_contract.adapter.ClContractPriceEntrustAdapter
import com.yjkj.chainup.new_contract.adapter.ClContractPriceEntrustNewAdapter
import com.yjkj.chainup.new_contract.bean.ClCurrentOrderBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.cl_activity_contract_entrust.*
import org.json.JSONObject
import kotlin.math.abs

/**
 * 合约当前/历史委托
 */
class ClContractEntrustActivity : NBaseActivity() {
    override fun setContentView() = R.layout.cl_activity_contract_entrust

    internal class PageInfo {
        var page = 1
        fun nextPage() {
            page++
        }

        fun reset() {
            page = 1
        }

        val isFirstPage: Boolean
            get() = page == 1
    }

    private val pageInfo = PageInfo()


    //是否是当前委托
    private var isCurrentEntrust = true

    //全部方向/全部类型
    private var sideList = ArrayList<TabInfo>()
    private var mCurrSideInfo: TabInfo? = null
    private var sideDialog: TDialog? = null
    private var typeList = ArrayList<TabInfo>()
    private var mCurrTypeInfo: TabInfo? = null

    //限价和计划委托
    private var entrustList = ArrayList<TabInfo>()
    private var mCurrEntrustInfo: TabInfo? = null
    private var entrustDialog: TDialog? = null

    //合约
    private var contractList = ArrayList<TabInfo>()
    private var mCurrContractInfo: TabInfo? = null
    private var contractDialog: TDialog? = null

    private var mContractId = 0
    private var mClContractPriceEntrustNewAdapter: ClContractPriceEntrustNewAdapter? = null
    private var mContractPriceEntrustAdapter: ClContractPriceEntrustAdapter? = null
    private var mContractPlanEntrustAdapter: ClContractPlanEntrustAdapter? = null
    private lateinit var mCurrentOrderList: ArrayList<ClCurrentOrderBean>
    private lateinit var mCurrentOrderBuffList: ArrayList<ClCurrentOrderBean>
    private var isLoadMore: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
    }

    override fun initView() {
        initAutoTextView()
        updateContractUI()
        initClickListener()
        initSwipeRefreshListener()
        doSwitchTabSwitch()
        initLoadMore()
    }

    private fun initSwipeRefreshListener() {
        swipe_refresh?.setOnRefreshListener {
            pageInfo.reset()
            getOrderList()
        }
    }

    private fun initLoadMore() {
        mClContractPriceEntrustNewAdapter?.loadMoreModule?.apply {
            setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore() {
                    getOrderList()
                }
            })
            isAutoLoadMore = true
            isEnableLoadMoreIfNotFullPage = false
        }

    }


    private fun initAutoTextView() {
        tv_tab_entrust.onLineText("sl_str_limit_entrust")
        tv_cancel_orders.onLineText("sl_str_cancel_orders")
    }
    private val plus: CharSequence = "\n"

    override fun loadData() {
        mCurrentOrderList = ArrayList()
        mCurrentOrderBuffList = ArrayList()
        rv_hold_contract.layoutManager = LinearLayoutManager(this@ClContractEntrustActivity)
        rv_hold_plan_contract.layoutManager = LinearLayoutManager(this@ClContractEntrustActivity)
        mContractPriceEntrustAdapter = ClContractPriceEntrustAdapter(mCurrentOrderList)
        mClContractPriceEntrustNewAdapter = ClContractPriceEntrustNewAdapter(this, mCurrentOrderList)
//        rv_hold_contract.adapter = mContractPriceEntrustAdapter
        rv_hold_contract.adapter = mClContractPriceEntrustNewAdapter
        mClContractPriceEntrustNewAdapter?.addChildClickViewIds(R.id.tv_cancel_common, R.id.tv_cancel_plan, R.id.tv_order_type_common, R.id.img_error_tips, R.id.img_liquidation_tip)
        mClContractPriceEntrustNewAdapter?.setEmptyView(EmptyForAdapterView(this@ClContractEntrustActivity
                ?: return))



        mContractPlanEntrustAdapter = ClContractPlanEntrustAdapter(mCurrentOrderList)
        rv_hold_plan_contract.adapter = mContractPlanEntrustAdapter
        mContractPlanEntrustAdapter?.setEmptyView(EmptyForAdapterView(this@ClContractEntrustActivity
                ?: return))

        mClContractPriceEntrustNewAdapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.tv_cancel_common, R.id.tv_cancel_plan -> {
                    cancelOrder((adapter.data[position] as ClCurrentOrderBean).id)
                }
                R.id.tv_order_type_common -> {
                    if ((adapter.data[position] as ClCurrentOrderBean).type.equals("6")){
                        return@setOnItemChildClickListener
                    }
                    ClContractEntrustDetailActivity.show(mActivity, adapter.data[position] as ClCurrentOrderBean)
                }
                R.id.img_error_tips -> {
                    val tip = when ((adapter.data[position] as ClCurrentOrderBean).memo) {
                        1 -> getString(R.string.cl_contract_add_text15)
                        2 -> getString(R.string.cl_contract_add_text16)
                        3 -> getString(R.string.cl_contract_add_text17)
                        4 -> getString(R.string.cl_contract_add_text18)
                        5 -> getString(R.string.cl_contract_add_text19)
                        6 -> getString(R.string.cl_contract_add_text20)
                        else -> ""
                    }
                    NewDialogUtils.showDialog(mActivity!!, tip, true, null, getLineText("common_text_tip"), getLineText("alert_common_i_understand"))
                }
                R.id.img_liquidation_tip -> {
                    var tip = (adapter.data[position] as ClCurrentOrderBean).liqPositionMsg
                    if (TextUtils.isEmpty(tip)){
                        tip=""
                    }
                    if (tip.contains("\\n")){
                        tip = tip.replace("\\n", "<br />")
                    }
                    NewDialogUtils.showDialog(mActivity!!, tip, true, null, getLineText("common_text_tip"), getLineText("alert_common_i_understand"))
                }
            }
        }
        mContractPlanEntrustAdapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.tv_cancel -> {
                    cancelOrder(mCurrentOrderList[position].id)
                }
            }
        }
        //合约
        loadContractData()
        // 限价/计划委托
        entrustList.add(TabInfo(getLineText("cl_orderlist_tabtype2"), 0))
        entrustList.add(TabInfo(getLineText("cl_orderlist_tabtype3"), 1))
        mCurrEntrustInfo = entrustList[0]
        //方向和类型
        sideList.add(TabInfo(getString(R.string.sl_str_order_type_none), 0))
        sideList.add(TabInfo(getLineText("sl_str_buy_open"), 1))
        sideList.add(TabInfo(getLineText("sl_str_buy_close"), 2))
        sideList.add(TabInfo(getLineText("sl_str_sell_close"), 3))
        sideList.add(TabInfo(getLineText("sl_str_sell_open"), 4))
        mCurrSideInfo = sideList[0]
        typeList.add(TabInfo(getString(R.string.sl_str_order_type_none), 0))
        typeList.add(TabInfo(getLineText("cl_limit_order_str"), 1))
        typeList.add(TabInfo(getLineText("cl_market_order_str"), 2))
        typeList.add(TabInfo("IOC", 3))
        typeList.add(TabInfo("FOK", 4))
        typeList.add(TabInfo("Post Only", 5))
        mCurrTypeInfo = typeList[0]
    }

    /**
     * 取消订单
     */
    private fun cancelOrder(orderId: String) {
        NewDialogUtils.showDialog(mActivity, getLineText("sl_str_cancel_order_tips"), false, object : NewDialogUtils.DialogBottomListener {
            override fun sendConfirm() {
                addDisposable(getContractModel().orderCancel(mContractId.toString(), orderId, mCurrEntrustInfo?.index == 1,
                        consumer = object : NDisposableObserver(mActivity, true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                pageInfo.reset()
                                getOrderList()
                            }
                        }))
            }
        }, getLineText("common_text_tip"), getLineText("common_text_btnConfirm"), getLineText("common_text_btnCancel"))
    }


    private fun getOrderList() {
        if (isCurrentEntrust) {
            getCurrentOrderList()
        } else {
            getHistoryOrderList()
        }
    }


    private fun getHistoryOrderList() {
        if (mCurrEntrustInfo?.index == 0) {
            getHistoryCommonOrderList()
        } else {
            getHistoryPlanOrderList()
        }
    }

    private fun getCurrentOrderList() {
        if (mCurrEntrustInfo?.index == 0) {
            getCurrentCommonOrderList()
        } else {
            getCurrentPlanOrderList()
        }
    }

    /**
     * 加载合约列表
     */
    private fun loadContractData() {
        mContractId = intent.getIntExtra("contractId", 0)
        addDisposable(getContractModel().getPublicInfo(
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            var mContractList = optJSONArray("contractList")
                            for (i in 0..(mContractList.length() - 1)) {
                                var obj: JSONObject = mContractList.get(i) as JSONObject
                                var id = obj.getInt("id")
                                var symbol = LogicContractSetting.getContractShowNameById(context, id)
                                contractList.add(TabInfo(symbol, i, id.toString()))
                                if (mContractId == id) {
                                    mCurrContractInfo = mCurrContractInfo ?: contractList[i]
                                }
                            }
                            mCurrContractInfo = mCurrContractInfo ?: contractList[0]
                            updateContractUI()

                        }
                    }

                }))
    }

    /**
     * 获取当前普通委托订单
     */
    private fun getCurrentCommonOrderList() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        addDisposable(getContractModel().getCurrentOrderList(mContractId.toString(), mCurrTypeInfo?.index!!, pageInfo.page,
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<ClCurrentOrderBean>()
                        swipe_refresh?.isRefreshing = false
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("orderList")) {
                                val mOrderListJson = optJSONArray("orderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<ClCurrentOrderBean>(obj, ClCurrentOrderBean::class.java)
                                    mClCurrentOrderBean.isPlan = false
                                    mListBuffer.add(mClCurrentOrderBean)
                                }
                            }
                        }
                        if (pageInfo.isFirstPage) {
                            mClContractPriceEntrustNewAdapter?.setList(mListBuffer)
                        } else {
                            mClContractPriceEntrustNewAdapter?.addData(mListBuffer)
                        }
                        if (mListBuffer.size < 20) {
                            mClContractPriceEntrustNewAdapter?.loadMoreModule?.loadMoreEnd()
                        } else {
                            mClContractPriceEntrustNewAdapter?.loadMoreModule?.loadMoreComplete()
                        }
                        pageInfo.nextPage()
                        closeLoadingDialog()
//                        mClContractPriceEntrustNewAdapter?.data?.isEmpty()?.let { updateCancelUI(it) }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        swipe_refresh?.isRefreshing = false
                        mContractPriceEntrustAdapter?.loadMoreModule?.isEnableLoadMore = true
                        mContractPriceEntrustAdapter?.loadMoreModule?.loadMoreFail();
                        closeLoadingDialog()
                    }
                }))
    }

    /**
     * 获取当前计划委托订单
     */
    private fun getCurrentPlanOrderList() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        addDisposable(getContractModel().getCurrentPlanOrderList(mContractId.toString(), mCurrTypeInfo?.index!!, pageInfo.page,
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<ClCurrentOrderBean>()
                        swipe_refresh?.isRefreshing = false
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("trigOrderList")) {
                                val mOrderListJson = optJSONArray("trigOrderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<ClCurrentOrderBean>(obj, ClCurrentOrderBean::class.java)
                                    mClCurrentOrderBean.isPlan = true
                                    mListBuffer.add(mClCurrentOrderBean)
                                }
                            }
                        }
                        if (pageInfo.isFirstPage) {
                            mClContractPriceEntrustNewAdapter?.setList(mListBuffer)
                        } else {
                            mClContractPriceEntrustNewAdapter?.addData(mListBuffer)
                        }
                        if (mListBuffer.size < 20) {
                            mClContractPriceEntrustNewAdapter?.loadMoreModule?.loadMoreEnd()
                        } else {
                            mClContractPriceEntrustNewAdapter?.loadMoreModule?.loadMoreComplete()
                        }
                        pageInfo.nextPage()
                        closeLoadingDialog()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        swipe_refresh?.isRefreshing = false
                        mContractPriceEntrustAdapter?.loadMoreModule?.isEnableLoadMore = true
                        mContractPriceEntrustAdapter?.loadMoreModule?.loadMoreFail();
                        closeLoadingDialog()
                    }
                }))
    }

    /**
     * 获取历史普通委托订单
     */
    private fun getHistoryCommonOrderList() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        addDisposable(getContractModel().getHistoryOrderList(mContractId.toString(), mCurrTypeInfo?.index!!, pageInfo.page,
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<ClCurrentOrderBean>()
                        swipe_refresh?.isRefreshing = false
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("orderList")) {
                                val mOrderListJson = optJSONArray("orderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<ClCurrentOrderBean>(obj, ClCurrentOrderBean::class.java)
                                    mClCurrentOrderBean.isPlan = false
                                    mListBuffer.add(mClCurrentOrderBean)
                                }
                            }
                        }
                        if (pageInfo.isFirstPage) {
                            mClContractPriceEntrustNewAdapter?.setList(mListBuffer)
                        } else {
                            mClContractPriceEntrustNewAdapter?.addData(mListBuffer)
                        }
                        if (mListBuffer.size < 20) {
                            mClContractPriceEntrustNewAdapter?.loadMoreModule?.loadMoreEnd()
                        } else {
                            mClContractPriceEntrustNewAdapter?.loadMoreModule?.loadMoreComplete()
                        }
//                        mClContractPriceEntrustNewAdapter?.data?.isEmpty()?.let { updateCancelUI(it) }
                        pageInfo.nextPage()
                        closeLoadingDialog()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        swipe_refresh?.isRefreshing = false
                        mContractPriceEntrustAdapter?.loadMoreModule?.isEnableLoadMore = true
                        mContractPriceEntrustAdapter?.loadMoreModule?.loadMoreFail();
                        closeLoadingDialog()
                    }
                }))
    }

    /**
     * 获取历史计划委托订单
     */
    private fun getHistoryPlanOrderList() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        addDisposable(getContractModel().getHistoryPlanOrderList(mContractId.toString(), mCurrTypeInfo?.index!!, pageInfo.page,
                consumer = object : NDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<ClCurrentOrderBean>()
                        swipe_refresh?.isRefreshing = false
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("trigOrderList")) {
                                val mOrderListJson = optJSONArray("trigOrderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<ClCurrentOrderBean>(obj, ClCurrentOrderBean::class.java)
                                    mClCurrentOrderBean.isPlan = true
                                    mListBuffer.add(mClCurrentOrderBean)
                                }
                            }
                        }
                        if (pageInfo.isFirstPage) {
                            mClContractPriceEntrustNewAdapter?.setList(mListBuffer)
                        } else {
                            mClContractPriceEntrustNewAdapter?.addData(mListBuffer)
                        }
                        if (mListBuffer.size < 20) {
                            mClContractPriceEntrustNewAdapter?.loadMoreModule?.loadMoreEnd()
                        } else {
                            mClContractPriceEntrustNewAdapter?.loadMoreModule?.loadMoreComplete()
                        }
                        pageInfo.nextPage()
                        closeLoadingDialog()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        swipe_refresh?.isRefreshing = false
                        mContractPriceEntrustAdapter?.loadMoreModule?.isEnableLoadMore = true
                        mContractPriceEntrustAdapter?.loadMoreModule?.loadMoreFail();
                        closeLoadingDialog()
                    }
                }))
    }


    /**
     * 是否展示“全部撤销订单”
     */
    fun updateCancelUI(isShow: Boolean = false) {
        tv_cancel_orders.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    private fun initClickListener() {
        /**
         * 取消全部订单
         */
        tv_cancel_orders.setOnClickListener {
//            if (mClContractPriceEntrustNewAdapter?.data?.size!! > 0) {
//                cancelOrder("")
//            }
            if (mCurrentOrderList.size > 0) {
                cancelOrder("")
            }
        }
        ic_close?.setOnClickListener { finish() }
        //选择合约
        ll_tab_contract.setOnClickListener {
            showSelectContractDialog()
        }
        //选择 限价/计划委托
        ll_tab_entrust.setOnClickListener {
            showSelectEntrustDialog()
        }
        //选择类型
        ll_tab_side_layout.setOnClickListener {
            showSelectSideDialog()
        }
        sub_tab_layout.bindTabListener(object : ContractEntrustTabWidget.ContractEntrustTabListener {
            override fun onTab(index: Int) {
                isCurrentEntrust = index == 0
                doSwitchTabSwitch()
            }

        })
        ly_appbar?.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (abs(verticalOffset) >= 130) {
                    if (tv_title?.visibility == View.GONE) {
                        tv_title?.visibility = View.VISIBLE
                        //  sub_tab_layout?.visibility = View.GONE
                    }
                } else {
                    if (tv_title?.visibility == View.VISIBLE) {
                        tv_title?.visibility = View.GONE
                        // sub_tab_layout?.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun showSelectContractDialog() {
        if (contractList.size == 0) return
        contractDialog = NewDialogUtils.showNewBottomListDialog(mActivity, contractList, mCurrContractInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                mCurrContractInfo = contractList[index]
                contractDialog?.dismiss()
                mContractId = mCurrContractInfo?.extras?.toInt()!!
                pageInfo.reset()
                getOrderList()
                updateContractUI()
            }
        })
    }

    private fun showSelectEntrustDialog() {
        entrustDialog = NewDialogUtils.showNewBottomListDialog(mActivity, entrustList, mCurrEntrustInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                mCurrEntrustInfo = entrustList[index]
                entrustDialog?.dismiss()
                updateEntrustUI()
                pageInfo.reset()
                getOrderList()
            }
        })
    }

    private fun showSelectSideDialog() {
        sideDialog = NewDialogUtils.showNewBottomListDialog(mActivity, typeList, mCurrTypeInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                mCurrTypeInfo = typeList[index]
                sideDialog?.dismiss()
                updateSideUI()
                pageInfo.reset()
                getOrderList()
            }
        })
    }


    private fun updateContractUI() {
        tv_tab_contract.text = mCurrContractInfo?.name
    }

    private fun updateSideUI() {
        tv_tab_side.text = mCurrTypeInfo?.name
    }

    private fun updateEntrustUI() {
        tv_tab_entrust.text = mCurrEntrustInfo?.name
    }

    /**
     * 切换当前和历史委托
     */
    private fun doSwitchTabSwitch() {
        if (isCurrentEntrust) {
            tv_title.onLineText("contract_text_currentEntrust")
            updateCancelUI(true)
        } else {
            tv_title.onLineText("contract_text_historyCommision")
            updateCancelUI(false)
        }
        mClContractPriceEntrustNewAdapter?.setIsCurrentEntrust(isCurrentEntrust)
        pageInfo.reset()
        typeList.clear()
        if (isCurrentEntrust) {
            typeList.add(TabInfo(getString(R.string.sl_str_order_type_none), 0))
            typeList.add(TabInfo(getString(R.string.cl_limit_order_str), 1))
            typeList.add(TabInfo("Post Only", 5))
        } else {
            typeList.add(TabInfo(getString(R.string.sl_str_order_type_none), 0))
            typeList.add(TabInfo(getLineText("cl_limit_order_str"), 1))
            typeList.add(TabInfo(getLineText("cl_market_order_str"), 2))
            typeList.add(TabInfo("IOC", 3))
            typeList.add(TabInfo("FOK", 4))
            typeList.add(TabInfo("Post Only", 5))
        }
        mCurrTypeInfo = typeList[0]
        getOrderList()
        updateSideUI()
    }

    companion object {
        fun show(activity: Activity, contractId: Int = 0, entrustIndex: Int = 0) {
            val intent = Intent(activity, ClContractEntrustActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("contractId", contractId)
            bundle.putInt("entrustIndex", entrustIndex)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }
}