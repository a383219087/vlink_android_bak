package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.view.CpContractEntrustTabWidget
import com.chainup.contract.view.CpEmptyForAdapterView
import com.chainup.contract.view.CpNewDialogUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.adapter.CpContractPriceEntrustNewAdapter
import com.yjkj.chainup.new_contract.bean.CpCurrentOrderBean
import kotlinx.android.synthetic.main.cp_activity_contract_entrust.*
import org.json.JSONObject
import kotlin.math.abs

/**
 * 合约当前/历史委托
 */
class CpContractEntrustActivity : CpNBaseActivity() {
    override fun setContentView() = R.layout.cp_activity_contract_entrust

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
    private var sideList = ArrayList<CpTabInfo>()
    private var mCurrSideInfo: CpTabInfo? = null
    private var sideDialog: TDialog? = null
    private var typeList = ArrayList<CpTabInfo>()
    private var mCurrTypeInfo: CpTabInfo? = null

    //限价和计划委托
    private var entrustList = ArrayList<CpTabInfo>()
    private var mCurrEntrustInfo: CpTabInfo? = null
    private var entrustDialog: TDialog? = null

    //合约
    private var contractList = ArrayList<CpTabInfo>()
    private var mCurrContractInfo: CpTabInfo? = null
    private var contractDialog: TDialog? = null

    private var mContractId = 0
    private var mClContractPriceEntrustNewAdapter: CpContractPriceEntrustNewAdapter? = null
    private lateinit var mCurrentOrderList: ArrayList<CpCurrentOrderBean>
    private lateinit var mCurrentOrderBuffList: ArrayList<CpCurrentOrderBean>
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
        tv_tab_entrust.setText(R.string.cp_extra_text20)
        tv_cancel_orders.setText(R.string.cp_order_text52)
    }

    override fun loadData() {
        mCurrentOrderList = ArrayList()
        mCurrentOrderBuffList = ArrayList()
        rv_hold_contract.layoutManager = LinearLayoutManager(this@CpContractEntrustActivity)
        rv_hold_plan_contract.layoutManager = LinearLayoutManager(this@CpContractEntrustActivity)
        mClContractPriceEntrustNewAdapter =
            CpContractPriceEntrustNewAdapter(this, mCurrentOrderList)
//        rv_hold_contract.adapter = mContractPriceEntrustAdapter
        rv_hold_contract.adapter = mClContractPriceEntrustNewAdapter
        mClContractPriceEntrustNewAdapter?.addChildClickViewIds(
            R.id.tv_cancel_common,
            R.id.tv_cancel_plan,
            R.id.tv_order_type_common,
            R.id.img_error_tips, R.id.img_liquidation_tip
        )
        mClContractPriceEntrustNewAdapter?.setEmptyView(
            CpEmptyForAdapterView(
                this@CpContractEntrustActivity
                    ?: return
            )
        )
        mClContractPriceEntrustNewAdapter?.setOnItemChildClickListener { adapter, view, position ->
            when (view.id) {
                R.id.tv_cancel_common, R.id.tv_cancel_plan -> {
                    cancelOrder((adapter.data[position] as CpCurrentOrderBean).id)
                }
                R.id.tv_order_type_common -> {
                    if ((adapter.data[position] as CpCurrentOrderBean).type.equals("6")){
                        return@setOnItemChildClickListener
                    }
                    CpContractEntrustDetailActivity.show(mActivity, adapter.data[position] as CpCurrentOrderBean)

                }
                R.id.img_error_tips -> {
                    val tip = when ((adapter.data[position] as CpCurrentOrderBean).memo) {
                        1 -> getString(R.string.cp_extra_text21)
                        2 -> getString(R.string.cp_extra_text22)
                        3 -> getString(R.string.cp_extra_text23)
                        4 -> getString(R.string.cp_extra_text24)
                        5 -> getString(R.string.cp_extra_text25)
                        6 -> getString(R.string.cp_extra_text26)
                        else -> ""
                    }
                    CpNewDialogUtils.showDialog(
                        this@CpContractEntrustActivity,
                        tip,
                        true,
                        null,
                        getString(R.string.cp_extra_text27),
                        getString(R.string.cp_extra_text28)
                    )
                }
                R.id.img_liquidation_tip -> {
                    var tip = (adapter.data[position] as CpCurrentOrderBean).liqPositionMsg
                    if (TextUtils.isEmpty(tip)){
                        tip=""
                    }
                    if (tip.contains("\\n")){
                        tip = tip.replace("\\n", "<br />")
                    }
                    CpNewDialogUtils.showDialog(mActivity!!, tip, true, null, getString(R.string.cp_extra_text27), getString(R.string.cp_extra_text28))
                }
            }
        }

        //合约
        loadContractData()
        // 限价/计划委托
        entrustList.add(CpTabInfo(getString(R.string.cp_extra_text20), 0))
        entrustList.add(CpTabInfo(getString(R.string.cp_extra_text29), 1))
        mCurrEntrustInfo = entrustList[0]
        //方向和类型
        sideList.add(CpTabInfo(getString(R.string.cp_order_text4), 0))
        sideList.add(CpTabInfo(getString(R.string.cp_overview_text13), 1))
        sideList.add(CpTabInfo(getString(R.string.cp_extra_text4), 2))
        sideList.add(CpTabInfo(getString(R.string.cp_extra_text5), 3))
        sideList.add(CpTabInfo(getString(R.string.cp_overview_text14), 4))
        mCurrSideInfo = sideList[0]
        typeList.add(CpTabInfo(getString(R.string.cp_order_text4), 0))
        typeList.add(CpTabInfo(getString(R.string.cp_overview_text3), 1))
        typeList.add(CpTabInfo(getString(R.string.cp_overview_text4), 2))
        typeList.add(CpTabInfo("IOC", 3))
        typeList.add(CpTabInfo("FOK", 4))
        typeList.add(CpTabInfo("Post Only", 5))
        mCurrTypeInfo = typeList[0]
    }

    /**
     * 取消订单
     */
    private fun cancelOrder(orderId: String) {
        CpNewDialogUtils.showDialog(
            mActivity,
            getString(R.string.cp_extra_text30),
            false,
            object : CpNewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    addDisposable(
                        getContractModel().orderCancel(mContractId.toString(),
                            orderId,
                            mCurrEntrustInfo?.index == 1,
                            consumer = object : CpNDisposableObserver(mActivity, true) {
                                override fun onResponseSuccess(jsonObject: JSONObject) {
                                    pageInfo.reset()
                                    getOrderList()
                                }
                            })
                    )
                }
            },
            getString(R.string.cp_extra_text27),
            getString(R.string.cp_calculator_text16),
            getString(R.string.cp_overview_text56)
        )
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
        addDisposable(
            getContractModel().getPublicInfo(
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            var mContractList = optJSONArray("contractList")
                            for (i in 0..(mContractList.length() - 1)) {
                                var obj: JSONObject = mContractList.get(i) as JSONObject
                                var id = obj.getInt("id")
                                var symbol = CpClLogicContractSetting.getContractShowNameById(
                                    this@CpContractEntrustActivity,
                                    id
                                )
                                contractList.add(CpTabInfo(symbol, i, id.toString()))
                                if (mContractId == id) {
                                    mCurrContractInfo = mCurrContractInfo ?: contractList[i]
                                }
                            }
                            mCurrContractInfo = mCurrContractInfo ?: contractList[0]
                            updateContractUI()

                        }
                    }

                })
        )
    }

    /**
     * 获取当前普通委托订单
     */
    private fun getCurrentCommonOrderList() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        addDisposable(
            getContractModel().getCurrentOrderList(mContractId.toString(),
                mCurrTypeInfo?.index!!,
                pageInfo.page,
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<CpCurrentOrderBean>()
                        swipe_refresh?.isRefreshing = false
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("orderList")) {
                                val mOrderListJson = optJSONArray("orderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<CpCurrentOrderBean>(
                                        obj,
                                        CpCurrentOrderBean::class.java
                                    )
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
                        closeLoadingDialog()
                    }
                })
        )
    }

    /**
     * 获取当前计划委托订单
     */
    private fun getCurrentPlanOrderList() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        addDisposable(
            getContractModel().getCurrentPlanOrderList(mContractId.toString(),
                mCurrTypeInfo?.index!!,
                pageInfo.page,
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<CpCurrentOrderBean>()
                        swipe_refresh?.isRefreshing = false
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("trigOrderList")) {
                                val mOrderListJson = optJSONArray("trigOrderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<CpCurrentOrderBean>(
                                        obj,
                                        CpCurrentOrderBean::class.java
                                    )
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

                        closeLoadingDialog()
                    }
                })
        )
    }

    /**
     * 获取历史普通委托订单
     */
    private fun getHistoryCommonOrderList() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        addDisposable(
            getContractModel().getHistoryOrderList(mContractId.toString(),
                mCurrTypeInfo?.index!!,
                pageInfo.page,
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<CpCurrentOrderBean>()
                        swipe_refresh?.isRefreshing = false
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("orderList")) {
                                val mOrderListJson = optJSONArray("orderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<CpCurrentOrderBean>(
                                        obj,
                                        CpCurrentOrderBean::class.java
                                    )
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

                        closeLoadingDialog()
                    }
                })
        )
    }

    /**
     * 获取历史计划委托订单
     */
    private fun getHistoryPlanOrderList() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        addDisposable(
            getContractModel().getHistoryPlanOrderList(mContractId.toString(),
                mCurrTypeInfo?.index!!,
                pageInfo.page,
                consumer = object : CpNDisposableObserver(mActivity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        val mListBuffer = ArrayList<CpCurrentOrderBean>()
                        swipe_refresh?.isRefreshing = false
                        jsonObject.optJSONObject("data").run {
                            if (!isNull("trigOrderList")) {
                                val mOrderListJson = optJSONArray("trigOrderList")
                                for (i in 0..(mOrderListJson.length() - 1)) {
                                    var obj = mOrderListJson.getString(i)
                                    val mClCurrentOrderBean = Gson().fromJson<CpCurrentOrderBean>(
                                        obj,
                                        CpCurrentOrderBean::class.java
                                    )
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

                        closeLoadingDialog()
                    }
                })
        )
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
        sub_tab_layout.bindTabListener(object :
            CpContractEntrustTabWidget.ContractEntrustTabListener {
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
        contractDialog = CpNewDialogUtils.showNewBottomListDialog(
            mActivity,
            contractList,
            mCurrContractInfo!!.index,
            object : CpNewDialogUtils.DialogOnItemClickListener {
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
        entrustDialog = CpNewDialogUtils.showNewBottomListDialog(
            mActivity,
            entrustList,
            mCurrEntrustInfo!!.index,
            object : CpNewDialogUtils.DialogOnItemClickListener {
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
        sideDialog = CpNewDialogUtils.showNewBottomListDialog(
            mActivity,
            typeList,
            mCurrTypeInfo!!.index,
            object : CpNewDialogUtils.DialogOnItemClickListener {
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
            tv_title.setText(R.string.cp_order_text2)
            updateCancelUI(true)
        } else {
            tv_title.setText(R.string.cp_order_text72)
            updateCancelUI(false)
        }
        mClContractPriceEntrustNewAdapter?.setIsCurrentEntrust(isCurrentEntrust)
        pageInfo.reset()
        typeList.clear()
        if (isCurrentEntrust) {
            typeList.add(CpTabInfo(getString(R.string.cp_order_text4), 0))
            typeList.add(CpTabInfo(getString(R.string.cp_overview_text3), 1))
            typeList.add(CpTabInfo("Post Only", 5))
        } else {
            typeList.add(CpTabInfo(getString(R.string.cp_order_text4), 0))
            typeList.add(CpTabInfo(getString(R.string.cp_overview_text3), 1))
            typeList.add(CpTabInfo(getString(R.string.cp_overview_text4), 2))
            typeList.add(CpTabInfo("IOC", 3))
            typeList.add(CpTabInfo("FOK", 4))
            typeList.add(CpTabInfo("Post Only", 5))
        }
        mCurrTypeInfo = typeList[0]
        getOrderList()
        updateSideUI()
    }

    companion object {
        fun show(activity: Activity, contractId: Int = 0, entrustIndex: Int = 0) {
            val intent = Intent(activity, CpContractEntrustActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("contractId", contractId)
            bundle.putInt("entrustIndex", entrustIndex)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }
}