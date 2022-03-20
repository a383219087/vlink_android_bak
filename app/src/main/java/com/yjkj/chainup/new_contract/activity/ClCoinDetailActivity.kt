package com.yjkj.chainup.new_contract.activity

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
import com.yjkj.chainup.contract.fragment.SlContractHoldFragment
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.getLineText
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_contract.adapter.ClContractAssetRecordAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.Utils
import kotlinx.android.synthetic.main.cl_activity_coin_detail.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 合约币种详情
 */
class ClCoinDetailActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cl_activity_coin_detail
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

    var marginCoin = "USDT"
    private var holdFragment = SlContractHoldFragment()
    private lateinit var mAccountList: JSONArray

    //类型
    private var typeList = ArrayList<TabInfo>()
    private var mCurrTypeInfo: TabInfo? = null
    private var typeDialog: TDialog? = null

    private var assetAdapter: ClContractAssetRecordAdapter? = null
    private val mList = ArrayList<JSONObject>()

    private val mLimit = 0
    private var isLoading = false

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        marginCoin = intent.getStringExtra("marginCoin") ?: "USDT"
        initView()
        initLoadMore()
        loadData()
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
            it.expandedTitleGravity = Gravity.TOP
            it.title = NCoinManager.getShowMarket(marginCoin)
        }

//        val transaction = supportFragmentManager!!.beginTransaction()
//        transaction.add(R.id.fragment_container, holdFragment, "0000")
//        transaction.commitAllowingStateLoss()

        //类型
//        typeList.add(TabInfo(getString(R.string.sl_str_order_type_none), 0))
//        typeList.add(TabInfo(getString(R.string.sl_str_transfer_bb2contract), 1))//转入
//        typeList.add(TabInfo(getString(com.chainup.contract.R.string.sl_str_transfer_contract2bb), 2))//转出
//        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cl_funding_fee_str), 5))//资金费用
//        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cl_loss_amortization_str), 8))//分摊


        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_order_text4), 0))
        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_extra_text13), 1))
        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_extra_text14), 2))
        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_position_text3), 5))
        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_position_text5), 8))
        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_fee_share), 9))
        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_extra_text15), 10))
        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_extra_text16), 11))
        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_extra_text18), 6))
        typeList.add(TabInfo(getString(com.chainup.contract.R.string.cp_extra_text19), 7))


        if (mCurrTypeInfo == null) {
            mCurrTypeInfo = typeList[0]
        }

        assetAdapter = ClContractAssetRecordAdapter(this, mList)
        ll_layout.layoutManager = LinearLayoutManager(this)
        ll_layout.adapter = assetAdapter
        assetAdapter?.setEmptyView(EmptyForAdapterView(this))
        ll_layout.adapter = assetAdapter

        //选择类型
        ll_tab_type.setOnClickListener {
            showSelectTypeDialog()
        }
    }

    private fun showSelectTypeDialog() {
        typeDialog = NewDialogUtils.showNewBottomListDialog(mActivity, typeList, mCurrTypeInfo!!.index, object : NewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                mCurrTypeInfo = typeList[index]
                typeDialog?.dismiss()
                typeDialog = null
                updateTypeUI()
                pageInfo.reset()
                loadDataFromNet()
            }
        })
    }

    private fun updateTypeUI() {
        tv_tab_type.text = mCurrTypeInfo?.name
    }

    private fun initAutoTextView() {
//        tv_account_equity_label.apply {
//            onLineText("contract_assets_account_equity")
//            onClick {
//                ContractDialog.showDialog4AccountRights(this@SlCoinDetailActivity)
//            }
//        }
    }

    override fun loadData() {
        super.loadData()
        getAccountBalanceByMarginCoin()
        loadDataFromNet()
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

    private fun loadDataFromNet() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        var mMarginCoinPrecision = LogicContractSetting.getContractMarginCoinPrecisionByMarginCoin(mActivity, marginCoin)
        isLoading = true
        addDisposable(getContractModel().getTransactionList(marginCoin, mCurrTypeInfo?.index.toString(), pageInfo.page.toString(),
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

//                            val mLadderList = optJSONArray("transList")
//                            isLoading = false
//                            if (mOffset == 1) {
//                                mList.clear()
//                            }
//                            if (mLadderList.length() != 0) {
//                                for (i in 0..(mLadderList.length() - 1)) {
//                                    var obj: JSONObject = mLadderList.get(i) as JSONObject
//                                    obj.put("mMarginCoinPrecision", mMarginCoinPrecision)
//                                    mList.add(obj)
//                                }
//                                assetAdapter?.notifyDataSetChanged()
////                                assetAdapter?.setEnableLoadMore(true)
////                                assetAdapter?.loadMoreComplete()
//                            } else {
////                                assetAdapter?.loadMoreEnd()
//                            }
//                            assetAdapter?.notifyDataSetChanged()
//                            assetAdapter?.disableLoadMoreIfNotFullPage()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        assetAdapter?.loadMoreModule?.isEnableLoadMore = true
                        assetAdapter?.loadMoreModule?.loadMoreFail();
                        closeLoadingDialog()
                    }
                }))
    }

    private fun getAccountBalanceByMarginCoin() {
        addDisposable(getContractModel().getAccountBalanceByMarginCoin(marginCoin,
                consumer = object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            mAccountList = optJSONArray("accountList")
                            var isShowAssets = UserDataService.getInstance().isShowAssets
                            for (i in 0..(mAccountList.length() - 1)) {
                                var obj = mAccountList.getJSONObject(i)
                                var symbol = obj.getString("symbol")
                                if (symbol.equals(marginCoin)) {
                                    Utils.assetsHideShow(isShowAssets, tv_total_balance, BigDecimalUtils.showSNormal(obj.getString("totalAmount"), 5))
                                    Utils.assetsHideShow(isShowAssets, tv_all_margin_balance_value, BigDecimalUtils.showSNormal(obj.getString("totalMargin"), 5))
                                    Utils.assetsHideShow(isShowAssets, tv_small_margin_balance_value, BigDecimalUtils.showSNormal(obj.getString("isolateMargin"), 5))
                                    Utils.assetsHideShow(isShowAssets, tv_freeze_margin, BigDecimalUtils.showSNormal(obj.getString("lockAmount"), 5))
                                    Utils.assetsHideShow(isShowAssets, tv_realized, BigDecimalUtils.showSNormal(obj.getString("realizedAmount"), 5))
                                    Utils.assetsHideShow(isShowAssets, tv_un_realized, BigDecimalUtils.showSNormal(obj.getString("unRealizedAmount"), 5))
                                }
                            }
                        }
                    }
                }))
    }


    companion object {
        fun show(activity: Activity, marginCoin: String) {
            val intent = Intent(activity, ClCoinDetailActivity::class.java)
            intent.putExtra("marginCoin", marginCoin)
            activity.startActivity(intent)
        }
    }


}