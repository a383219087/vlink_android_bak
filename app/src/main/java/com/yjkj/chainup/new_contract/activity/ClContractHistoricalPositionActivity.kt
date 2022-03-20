package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.contract.sdk.ContractSDKAgent
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.data.bean.TabInfo
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.contract.widget.SlDialogHelper
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_contract.adapter.ClContractAssetRecordAdapter
import com.yjkj.chainup.new_contract.adapter.ClContractHistoricalPositionAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.cl_activity_asset_record.*
import org.json.JSONArray
import org.json.JSONObject


/**
 * 合约仓位历史
 */
class ClContractHistoricalPositionActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_historical_position
    }

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
    private var mContractId = -1
    private var side = ""

    //全部方向
    private var sideList = ArrayList<TabInfo>()
    private var mCurrSideInfo: TabInfo? = null
    private var sideDialog: TDialog? = null

    //合约
    private var contractList = ArrayList<TabInfo>()
    private var mCurrContractInfo: TabInfo? = null
    private var contractDialog: TDialog? = null

    //类型
    private var typeList = ArrayList<TabInfo>()
    private var mCurrTypeInfo: TabInfo? = null
    private var typeDialog: TDialog? = null

    private var assetAdapter: ClContractHistoricalPositionAdapter? = null
    private val mList = ArrayList<JSONObject>()

    private var isLoading = false


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        loadData()
        initView()
        initLoadMore()
        initClickListener()
    }


    override fun loadData() {
        super.loadData()
        mContractId = intent.getIntExtra("contractId", -1)
        val mContractCoinListJsonStr = LogicContractSetting.getContractJsonListStr(mActivity)
        if (mContractCoinListJsonStr != null && mContractCoinListJsonStr.isNotEmpty()) {
            var mContractList = JSONArray(mContractCoinListJsonStr)
            for (i in 0..(mContractList.length() - 1)) {
                var obj: JSONObject = mContractList.get(i) as JSONObject
                var id = obj.getInt("id")
                var symbol = LogicContractSetting.getContractShowNameById(ContractSDKAgent.context, id)
                contractList.add(TabInfo(symbol, i, id.toString()))
                if (mContractId == id) {
                    mCurrContractInfo = mCurrContractInfo ?: contractList[i]
                }
            }
        }
        if (mCurrContractInfo == null) {
            mCurrContractInfo = contractList[0]
        }
        updateContractUI()
        //方向和类型
        sideList.add(TabInfo(getLineText("sl_str_contract_side_none"), 0, ""))
        sideList.add(TabInfo(getLineText("cl_HistoricalPosition_1"), 1, "BUY"))
        sideList.add(TabInfo(getLineText("cl_HistoricalPosition_2"), 2, "SELL"))
        mCurrSideInfo = sideList[0]
        updateTypeUI()

        assetAdapter = ClContractHistoricalPositionAdapter(this, mList)

        loadDataFromNet()

    }


    override fun initView() {
        initAutoTextView()
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.let {
            it.setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            it.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            it.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            it.expandedTitleGravity = Gravity.BOTTOM
        }

        ll_layout.layoutManager = LinearLayoutManager(this)
        ll_layout.adapter = assetAdapter
        assetAdapter?.setEmptyView(EmptyForAdapterView(this))
        assetAdapter?.addChildClickViewIds(R.id.tv_key1)
        assetAdapter?.setOnItemChildClickListener(object :OnItemChildClickListener{
            override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val obj: JSONObject=   assetAdapter?.getItem(position) as JSONObject
                obj.put("marginCoin", LogicContractSetting.getContractMarginCoinById(this@ClContractHistoricalPositionActivity,mContractId))
                obj.put("marginCoinPrecision",LogicContractSetting.getContractMarginCoinPrecisionById(this@ClContractHistoricalPositionActivity,mContractId))
                obj?.let { SlDialogHelper.showProfitLossDetailsDialog(this@ClContractHistoricalPositionActivity, it) }
            }
        })
        updateContractUI()
        updateTypeUI()

    }

    private fun initAutoTextView() {
        collapsing_toolbar.title = getString(R.string.sl_str_history_hold)
    }

    private fun initClickListener() {
        //选择合约
        ll_tab_contract.setOnClickListener {
            showSelectContractDialog()
        }
        //选择类型
        ll_tab_type.setOnClickListener {
            showSelectTypeDialog()
        }
    }

    private fun initLoadMore() {
        assetAdapter?.loadMoreModule?.apply {
            setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore() {
                    loadDataFromNet()
                }
            })
            isAutoLoadMore = true
            isEnableLoadMoreIfNotFullPage = false
        }
    }

    private fun showSelectTypeDialog() {
        sideDialog = NewDialogUtils.showNewBottomListDialog(mActivity, sideList, mCurrSideInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                sideDialog?.dismiss()
                typeDialog = null
                mCurrSideInfo = sideList[index]
                updateTypeUI()
                pageInfo.reset()
                loadDataFromNet()
            }
        })
    }


    private fun showSelectContractDialog() {
        contractDialog = NewDialogUtils.showNewBottomListDialog(mActivity, contractList, mCurrContractInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                contractDialog?.dismiss()
                contractDialog = null
                mCurrContractInfo = contractList[index]
                updateContractUI()
                pageInfo.reset()
                loadDataFromNet()
            }
        })
    }

    private fun updateContractUI() {
        tv_tab_contract.text = mCurrContractInfo?.name
    }

    private fun updateTypeUI() {
        tv_tab_type.text = mCurrSideInfo?.name
    }

    private fun loadDataFromNet() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        isLoading = true
        var symbolName = LogicContractSetting.getContractShowNameById(this, mContractId)
        var mMarginCoinPrecision = LogicContractSetting.getContractMarginCoinPrecisionById(this, mContractId)
        addDisposable(getContractModel().getHistoryPositionList(mContractId.toString(), pageInfo.page.toString(), mCurrSideInfo?.extras.toString(),
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                                val mPositionList = optJSONArray("positionList")
                                val mListBuffer = ArrayList<JSONObject>()
                                if (mPositionList.length() != 0) {
                                    for (i in 0..(mPositionList.length() - 1)) {
                                        var obj: JSONObject = mPositionList.get(i) as JSONObject
                                        obj.put("symbol", symbolName)
                                        obj.put("marginCoinPrecision", mMarginCoinPrecision)
                                        mListBuffer.add(obj)
                                    }
                                }
                                if (pageInfo.isFirstPage) {
                                    assetAdapter?.setList(mListBuffer)
                                } else {
                                    assetAdapter?.addData(mListBuffer)
                                }
                                if (mListBuffer.size < 20) {
                                    assetAdapter?.loadMoreModule?.loadMoreEnd()
                                } else {
                                    assetAdapter?.loadMoreModule?.loadMoreComplete()
                                }
                                pageInfo.nextPage()
                                closeLoadingDialog()
                        }
                    }

                    override fun onError(e: Throwable) {
                        assetAdapter?.loadMoreModule?.isEnableLoadMore = true
                        assetAdapter?.loadMoreModule?.loadMoreFail();
                        closeLoadingDialog()
                    }
                }))
    }


    companion object {
        fun show(activity: Activity, contractId: Int) {
            val intent = Intent(activity, ClContractHistoricalPositionActivity::class.java)
            intent.putExtra("contractId", contractId)
            activity.startActivity(intent)
        }
    }
}