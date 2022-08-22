package com.yjkj.chainup.new_version.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.data.bean.TabInfo
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.ClContractAssetRecordAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.cl_activity_asset_record.*
import org.json.JSONArray
import org.json.JSONObject


/**
 * 合约资金记录
 */
class ClContractAssetRecordActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_asset_record
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

    //合约
    private var contractList = ArrayList<TabInfo>()
    private var mCurrContractInfo: TabInfo? = null
    private var contractDialog: TDialog? = null

    //类型
    private var typeList = ArrayList<TabInfo>()
    private var mCurrTypeInfo: TabInfo? = null
    private var typeDialog: TDialog? = null

    private var assetAdapter: ClContractAssetRecordAdapter? = null
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
        val symbol = intent.getStringExtra("symbol") ?: ""
        val type = intent.getIntExtra("type", 0)
        val mContractMarginCoinListJsonStr = LogicContractSetting.getContractMarginCoinListStr(mActivity)
        if (mContractMarginCoinListJsonStr != null && mContractMarginCoinListJsonStr.isNotEmpty()) {
            val jsonArray = JSONArray(mContractMarginCoinListJsonStr)
            for (i in 0 until jsonArray.length()) {
                val mJSONObject = jsonArray[i] as String
                contractList.add(TabInfo(mJSONObject, i))
                if (symbol.equals(mJSONObject)) {
                    mCurrContractInfo = TabInfo(mJSONObject, i)
                }
            }
        } else {
            contractList.add(TabInfo("USDT", 0))
        }
        if (mCurrContractInfo == null) {
            mCurrContractInfo = contractList[0]
        }
        updateContractUI()
        //类型
        typeList.add(TabInfo(getLineText("sl_str_order_type_none"), 0))
        typeList.add(TabInfo(getString(R.string.cl_coflowingwater_transferin), 1))
        typeList.add(TabInfo(getString(R.string.cl_coflowingwater_transferout), 2))
        typeList.add(TabInfo(getString(R.string.cl_coflowingwater_fee), 5))
        typeList.add(TabInfo(getString(R.string.cl_coflowingwater_loss), 8))
        typeList.add(TabInfo(getString(R.string.contract_fee_share), 9))
        typeList.add(TabInfo(getString(R.string.cl_contract_add_text6), 10))
        typeList.add(TabInfo(getString(R.string.cl_contract_add_text7), 11))
        typeList.add(TabInfo(getString(R.string.cl_contract_add_text4), 6))
        typeList.add(TabInfo(getString(R.string.cl_contract_add_text5), 7))
        for (typeItem in typeList) {
            if (typeItem.index == type) {
                mCurrTypeInfo = typeItem
                break
            }
        }
        if (mCurrTypeInfo == null) {
            mCurrTypeInfo = typeList[0]
        }
        updateTypeUI()
        assetAdapter = ClContractAssetRecordAdapter(this, mList)

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
//        assetAdapter?.setOnLoadMoreListener {
//            if (!isLoading) {
//                loadDataFromNet()
//            }
//        }

        updateContractUI()
        updateTypeUI()

    }

    private fun initAutoTextView() {
        collapsing_toolbar.title = getLineText("sl_str_asset_record")
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
        typeDialog = NewDialogUtils.showNewBottomListDialog(mActivity, typeList, mCurrTypeInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                typeDialog?.dismiss()
                typeDialog = null
                mCurrTypeInfo = typeList[index]
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
        tv_tab_type.text = mCurrTypeInfo?.name
    }

    private fun loadDataFromNet() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        var mMarginCoinPrecision = LogicContractSetting.getContractMarginCoinPrecisionByMarginCoin(mActivity, mCurrContractInfo?.name.toString())
        isLoading = true
        addDisposable(getContractModel().getTransactionList(mCurrContractInfo?.name.toString(), mCurrTypeInfo?.index.toString(), pageInfo.page.toString(),
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            val mLadderList = optJSONArray("transList")
                            val mListBuffer = ArrayList<JSONObject>()
                            if (mLadderList.length() != 0) {
                                for (i in 0..(mLadderList.length() - 1)) {
                                    var obj: JSONObject = mLadderList.get(i) as JSONObject
                                    obj.put("mMarginCoinPrecision", mMarginCoinPrecision)
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
                        assetAdapter?.loadMoreModule?.isEnableLoadMore=true
                        assetAdapter?.loadMoreModule?.loadMoreFail();
                        closeLoadingDialog()
                    }
                }))
    }


    companion object {
        fun show(activity: Activity, symbol: String = "USDT", type: Int = 0) {
            val intent = Intent(activity, ClContractAssetRecordActivity::class.java)
            intent.putExtra("symbol", symbol)
            intent.putExtra("type", type)
            activity.startActivity(intent)
        }
    }
}