package com.chainup.contract.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.view.CpDialogUtil
import com.chainup.contract.view.CpEmptyForAdapterView
import com.chainup.contract.view.CpNewDialogUtils
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.adapter.CpContractAssetRecordAdapter
import kotlinx.android.synthetic.main.cp_activity_asset_record.*
import kotlinx.android.synthetic.main.cp_activity_asset_record.img_back
import org.json.JSONArray
import org.json.JSONObject


/**
 * 合约资金记录
 */
class CpContractAssetRecordActivity : CpNBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cp_activity_asset_record
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
    private var contractList = ArrayList<CpTabInfo>()
    private var mCurrContractInfo: CpTabInfo? = null

    //类型
    private var typeList = ArrayList<CpTabInfo>()
    private var mCurrTypeInfo: CpTabInfo? = null

    private var assetAdapter: CpContractAssetRecordAdapter? = null
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
        val mContractMarginCoinListJsonStr = CpClLogicContractSetting.getContractMarginCoinListStr(mActivity)
        if (mContractMarginCoinListJsonStr != null && mContractMarginCoinListJsonStr.isNotEmpty()) {
            val jsonArray = JSONArray(mContractMarginCoinListJsonStr)
            for (i in 0 until jsonArray.length()) {
                val mJSONObject = jsonArray[i] as String
                contractList.add(CpTabInfo(mJSONObject, i,i))
                if (symbol == mJSONObject) {
                    mCurrContractInfo = CpTabInfo(mJSONObject, i,extras = i)
                }
            }
        } else {
            contractList.add(CpTabInfo("USDT", 0,extras = 0))
        }
        if (mCurrContractInfo == null) {
            mCurrContractInfo = contractList[0]
        }
        updateContractUI()
        //类型
        typeList.add(CpTabInfo(getString(R.string.cp_order_text4), 0,extras=0))
        typeList.add(CpTabInfo(getString(R.string.cp_extra_text13), 1,extras=1))
        typeList.add(CpTabInfo(getString(R.string.cp_extra_text14), 2,extras=2))
        typeList.add(CpTabInfo(getString(R.string.cp_position_text3), 5,extras=3))
        typeList.add(CpTabInfo(getString(R.string.cp_position_text5), 8,extras=4))
        typeList.add(CpTabInfo(getString(R.string.cp_fee_share), 9,extras=5))
        typeList.add(CpTabInfo(getString(R.string.cp_extra_text15), 10,extras=6))
        typeList.add(CpTabInfo(getString(R.string.cp_extra_text16), 11,extras=7))
        typeList.add(CpTabInfo(getString(R.string.cp_extra_text18), 6,extras=8))
        typeList.add(CpTabInfo(getString(R.string.cp_extra_text19), 7,extras=9))
        typeList.add(CpTabInfo(getString(R.string.cp_extra_text191), 13))
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
        assetAdapter = CpContractAssetRecordAdapter(this, mList)

        loadDataFromNet()

    }


    override fun initView() {
        initAutoTextView()
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        img_back?.setOnClickListener {
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
        assetAdapter?.setEmptyView(CpEmptyForAdapterView(this))

        updateContractUI()
        updateTypeUI()

    }

    private fun initAutoTextView() {
        collapsing_toolbar.title = getString(R.string.cp_assets_text8)
    }

    private fun initClickListener() {
        //选择合约
        ll_tab_contract.setOnClickListener {
            showSelectContractDialog(it)
        }
        //选择类型
        ll_tab_type.setOnClickListener {
            showSelectTypeDialog(it)
        }
    }

    private fun initLoadMore() {
        assetAdapter?.loadMoreModule?.apply {
            setOnLoadMoreListener { loadDataFromNet() }
            isAutoLoadMore = true
            isEnableLoadMoreIfNotFullPage = false
        }
    }

    private fun showSelectTypeDialog(view: View) {
        img_tab_type.animate().setDuration(300).rotation(180f).start()
        LogUtils.e("createTopListPop","|||||"+ mCurrTypeInfo!!.extrasNum)
        CpDialogUtil.showNewListDialog(this, typeList,  mCurrTypeInfo!!.extrasNum!!, object : CpNewDialogUtils.DialogOnItemClickListener {

            override fun clickItem(position: Int) {
                mCurrTypeInfo = typeList[position]
                updateTypeUI()
                pageInfo.reset()
                loadDataFromNet()
            }
        })

    }


    private fun showSelectContractDialog(view:View) {


        img_tab_contract.animate().setDuration(300).rotation(180f).start()
        CpDialogUtil.createTopListPop(this, mCurrContractInfo!!.extrasNum!!, contractList, view, object : CpNewDialogUtils.DialogOnSigningItemClickListener {
            override fun clickItem(position: Int, text: String) {
                mCurrContractInfo = contractList[position]
                LogUtils.e("createTopListPop",position.toString()+"|||||||"+text+"|||||"+ mCurrTypeInfo!!.extrasNum)
                updateTypeUI()
                pageInfo.reset()
                loadDataFromNet()
                updateContractUI()
            }
        }, object : CpNewDialogUtils.DialogOnDismissClickListener {
            override fun clickItem() {
                img_tab_contract.animate().setDuration(300).rotation(0f).start()
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
        clearDisposable()
        val mMarginCoinPrecision = CpClLogicContractSetting.getContractMarginCoinPrecisionByMarginCoin(mActivity, mCurrContractInfo?.name.toString())
        isLoading = true
        addDisposable(getContractModel().getTransactionList(mCurrContractInfo?.name.toString(), mCurrTypeInfo?.index.toString(), pageInfo.page.toString(),
                consumer = object : CpNDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            val mLadderList = this?.optJSONArray("transList")
                            val mListBuffer = ArrayList<JSONObject>()
                            if (mLadderList != null) {
                                if (mLadderList.length() != 0) {
                                    for (i in 0 until mLadderList.length()) {
                                                val obj: JSONObject = mLadderList.get(i) as JSONObject
                                                obj.put("mMarginCoinPrecision", mMarginCoinPrecision)
                                                mListBuffer.add(obj)
                                            }
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
        fun show(activity: Activity, symbol: String = "USDT", type: Int = 0) {
            val intent = Intent(activity, CpContractAssetRecordActivity::class.java)
            intent.putExtra("symbol", symbol)
            intent.putExtra("type", type)
            activity.startActivity(intent)
        }
    }
}