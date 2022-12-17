package com.chainup.contract.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpChainUtil
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.view.CpEmptyForAdapterView
import com.chainup.contract.view.CpNewDialogUtils
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.adapter.CpContractAssetRecordAdapter
import kotlinx.android.synthetic.main.cp_activity_coin_detail.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 合约币种详情
 */
class CpCoinDetailActivity : CpNBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cp_activity_coin_detail
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
    private lateinit var mAccountList: JSONArray

    //类型
    private var typeList = ArrayList<CpTabInfo>()
    private var mCurrTypeInfo: CpTabInfo? = null
    private var typeDialog: TDialog? = null

    private var assetAdapter: CpContractAssetRecordAdapter? = null
    private val mList = ArrayList<JSONObject>()

    private val mLimit = 0
    private var isLoading = false

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        marginCoin = intent.getStringExtra("marginCoin").toString()
        tv_title.setText(marginCoin)
        initView()
        initLoadMore()
        loadData()
    }

    override fun initView() {
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
            it.expandedTitleGravity = Gravity.TOP
            it.title = marginCoin
        }

//        val transaction = supportFragmentManager!!.beginTransaction()
//        transaction.add(R.id.fragment_container, holdFragment, "0000")
//        transaction.commitAllowingStateLoss()

        //类型
//        typeList.add(CpTabInfo(getString(R.string.cp_order_text4), 0))
//        typeList.add(CpTabInfo(getString(R.string.sl_str_transfer_bb2contract), 1))//转入
//        typeList.add(CpTabInfo(getString(R.string.sl_str_transfer_contract2bb), 2))//转出
//        typeList.add(CpTabInfo(getString(R.string.cl_funding_fee_str), 5))//资金费用
//        typeList.add(CpTabInfo(getString(R.string.cl_loss_amortization_str), 8))//分摊

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

        if (mCurrTypeInfo == null) {
            mCurrTypeInfo = typeList[0]
        }

        assetAdapter = CpContractAssetRecordAdapter(this, mList)
        ll_layout.layoutManager = LinearLayoutManager(this)
        ll_layout.adapter = assetAdapter
        assetAdapter?.setEmptyView(CpEmptyForAdapterView(this))
        ll_layout.adapter = assetAdapter

        //选择类型
        ll_tab_type.setOnClickListener {
            showSelectTypeDialog()
        }
    }

    private fun showSelectTypeDialog() {
        typeDialog = CpNewDialogUtils.showNewBottomListDialog(
            mActivity,
            typeList,
            mCurrTypeInfo!!.index,
            object : CpNewDialogUtils.DialogOnItemClickListener {
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

    override fun loadData() {
        super.loadData()
        getAccountBalanceByMarginCoin()
        loadDataFromNet()
    }

    private fun initLoadMore() {
        assetAdapter?.loadMoreModule?.apply {
            setOnLoadMoreListener { loadDataFromNet() }
            isAutoLoadMore = true
            isEnableLoadMoreIfNotFullPage = false
        }
    }

    private fun loadDataFromNet() {
        if (pageInfo.isFirstPage) {
            showLoadingDialog()
        }
        val mMarginCoinPrecision =
            CpClLogicContractSetting.getContractMarginCoinPrecisionByMarginCoin(mActivity, marginCoin)
        isLoading = true
        addDisposable(
            getContractModel().getTransactionList(marginCoin,
                mCurrTypeInfo?.index.toString(),
                pageInfo.page.toString(),
                consumer = object : CpNDisposableObserver() {
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
                        super.onError(e)
                        assetAdapter?.loadMoreModule?.isEnableLoadMore = true
                        assetAdapter?.loadMoreModule?.loadMoreFail();
                        closeLoadingDialog()
                    }
                })
        )
    }

    private fun getAccountBalanceByMarginCoin() {
        Log.d("getPositionAssetsList","我是5")
        addDisposable(
            getContractModel().getAccountBalanceByMarginCoin(marginCoin,
                consumer = object : CpNDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            mAccountList = optJSONArray("accountList")
                            var isShowAssets = true
                            for (i in 0..(mAccountList.length() - 1)) {
                                var obj = mAccountList.getJSONObject(i)
                                var symbol = obj.getString("symbol")
                                if (symbol.equals(marginCoin)) {
                                    CpChainUtil.assetsHideShow(
                                        isShowAssets,
                                        tv_total_balance,
                                        CpBigDecimalUtils.showSNormal(obj.getString("totalAmount"), 5)
                                    )
                                    CpChainUtil.assetsHideShow(
                                        isShowAssets,
                                            tv_canuse_balance,
                                        CpBigDecimalUtils.showSNormal(obj.getString("canUseAmount"), 5)
                                    )
                                    CpChainUtil.assetsHideShow(
                                        isShowAssets,
                                        tv_all_margin_balance_value,
                                        CpBigDecimalUtils.showSNormal(obj.getString("totalMargin"), 5)
                                    )
                                    CpChainUtil.assetsHideShow(
                                        isShowAssets,
                                        tv_small_margin_balance_value,
                                        CpBigDecimalUtils.showSNormal(
                                            obj.getString("isolateMargin"),
                                            5
                                        )
                                    )
                                    CpChainUtil.assetsHideShow(
                                        isShowAssets,
                                        tv_freeze_margin,
                                        CpBigDecimalUtils.showSNormal(obj.getString("lockAmount"), 5)
                                    )
//                                    CpChainUtil.assetsHideShow(
//                                        isShowAssets,
//                                        tv_realized,
//                                        CpBigDecimalUtils.showSNormal(
//                                            obj.getString("realizedAmount"),
//                                            5
//                                        )
//                                    )
//                                    CpChainUtil.assetsHideShow(
//                                        isShowAssets,
//                                        tv_un_realized,
//                                        CpBigDecimalUtils.showSNormal(
//                                            obj.getString("unRealizedAmount"),
//                                            5
//                                        )
//                                    )
                                }
                            }
                        }
                    }
                })
        )
    }


    companion object {
        fun show(activity: Activity, marginCoin: String) {
            val intent = Intent(activity, CpCoinDetailActivity::class.java)
            intent.putExtra("marginCoin", marginCoin)
            activity.startActivity(intent)
        }
    }


}