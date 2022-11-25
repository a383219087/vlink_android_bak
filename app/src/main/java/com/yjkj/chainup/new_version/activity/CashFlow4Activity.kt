package com.yjkj.chainup.new_version.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.fund.CashFlowBean
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.adapter.CashFlow4Adapter
import com.yjkj.chainup.new_version.bean.CashFlowSceneBean
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.new_version.view.ScreeningPopupWindowView
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_cash_flow4.*


/**
 * @description:资金流水(4.0)
 * @author Bertking
 * @date 2019-5-15 AM
 *
 */

class CashFlow4Activity : NewBaseActivity() {


    var symbolForSelect = ""
    var transactionScene = ""
    var startTime = ""
    var endTime = ""
    var page = 1
    var pageSize = 10
    var adapter: CashFlow4Adapter? = null

    companion object {
        const val TRANSACTIONSCENE = "TRANSACTIONSCENE"
        fun enter2(context: Context, transactionScene: String) {
            val intent = Intent(context, CashFlow4Activity::class.java)
            intent.putExtra(TRANSACTIONSCENE, transactionScene)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cash_flow4)
        context = this
        status = LanguageUtil.getString(this, "otc_recharge")
        getData()
        if (transactionScene == ParamConstant.TYPE_OTC_TRANSFER) {
            status = LanguageUtil.getString(this, "otc_transfer")
            sceneType = ParamConstant.OTC_TRANSFER_RECORD
            sceneList.add(CashFlowSceneBean.Scene(ParamConstant.OTC_TRANSFER_RECORD, LanguageUtil.getString(this, "transfer_text_otc")))
            spw_layout?.initLineAdaptiveLayout(sceneList)
        } else {
            sceneType = ""
            getCashFlowScene()
        }
        adapter = CashFlow4Adapter(status)
        rv_cash_flow?.adapter = adapter
        adapter?.apply {
            setEmptyView(EmptyForAdapterView(this@CashFlow4Activity))
            setOnItemClickListener { adapter, view, position ->
                if (adapter.data.isNotEmpty()) {
                    val show = adapter.data.get(position) as CashFlowBean.Finance
                    CashFlowDetailActivity.liveData4CashFlowBean.postValue(show)
//                    CashFlowDetailActivity.enter2(context, sceneType)
                    CashFlowDetailActivity.enter2(context,transactionScene)
                }
            }
        }
        swipe_refresh.setOnRefreshListener {
            page = 1
            adapter?.loadMoreModule?.isEnableLoadMore = false
            getCashFlowList(symbolForSelect, transactionScene, page, startTime, endTime, status)
        }
        adapter?.loadMoreModule?.setOnLoadMoreListener {
            getCashFlowList(symbolForSelect, transactionScene, page, startTime, endTime, status)
        }
        rv_cash_flow?.layoutManager = LinearLayoutManager(context)
        pcv_title?.setContentTitle(LanguageUtil.getString(this, "assets_action_journalaccount"))
    }

    var isFrist = true
    fun getData() {
        intent ?: return
        transactionScene = intent.getStringExtra(TRANSACTIONSCENE) ?: ""
        pcv_title.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                if (spw_layout?.visibility == View.GONE) {
                    spw_layout?.visibility = View.VISIBLE
                } else {
                    spw_layout?.visibility = View.GONE
                }
                if (isFrist) {
                    spw_layout?.setMage()
                    isFrist = false
                }
            }

            override fun onclickName() {

            }
        }

        spw_layout?.fundFlowingWaterListenre = object : ScreeningPopupWindowView.OTCFundFlowingWaterListenre {
            override fun returnOTCFundFlowingWater(symbol: String, waterType: String, begin: String, end: String) {
                symbolForSelect = symbol
                page = 1
                transactionScene = waterType
                sceneList.forEach {
                    if (it.key == transactionScene) {
                        status = it.keyText ?: ""
                        sceneType = it.key
                    }
                }
                adapter?.status = status
                getCashFlowList(symbol, transactionScene, page, begin, end, status)
            }

        }
    }

    var sceneType = ""


    var status = "充值"

    override fun onResume() {
        super.onResume()
        getCashFlowList(symbolForSelect, transactionScene, page, startTime, endTime, status)
    }

    var sceneList: ArrayList<CashFlowSceneBean.Scene> = arrayListOf()

    /**
     * 获取资金流水的场景列表
     */
    private fun getCashFlowScene() {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        HttpClient.instance.getCashFlowScene()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<CashFlowSceneBean>() {
                    override fun onHandleSuccess(t: CashFlowSceneBean?) {
                        Log.d(TAG, "=======场景列表:${t?.toString()}=====")
                        t ?: return
                        if (t.sceneList.isEmpty()) {
                            return
                        }
                        /**
                         * TODO 选择场景发送对应的值
                         */
                        sceneList.clear()
                        sceneList.add(CashFlowSceneBean.Scene(key = "all",keyText = context.getString(R.string.otc_order_all)))
                        sceneList.addAll(t.sceneList)
                        spw_layout?.initLineAdaptiveLayout(t.sceneList)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        //DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                        NToastUtil.showTopToastNet(this@CashFlow4Activity, false, msg)
                        Log.d(TAG, "======error:==$code:msg+$msg")
                    }
                })
    }


    /**
     * 资金流水列表
     * @param transactionScene 默认场景为「充值」
     */
    private fun getCashFlowList(symbol: String,
                                transactionScene: String,
                                page: Int,
                                startTime: String,
                                endTime: String,
                                status: String = ""
    ) {
        if (!UserDataService.getInstance().isLogined) {
            return
        }
        if (transactionScene=="all"){
            this@CashFlow4Activity.transactionScene =""
        }

        HttpClient.instance.getCashFlowList(
                symbol = NCoinManager.setShowNameGetName(symbol),
                transactionScene = transactionScene,
                startTime = startTime,
                endTime = endTime,
                page = page.toString(), pageSize = pageSize.toString()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<CashFlowBean>() {
                    override fun onHandleSuccess(t: CashFlowBean?) {
                        Log.d(TAG, "======资金流水列表：=====" + t.toString())
                        initData(page != 1, t)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(this@CashFlow4Activity, false, msg)
                        Log.d(TAG, "======error:==$code:msg+$msg")
                        initData(page != 1, null)
                    }
                })
    }

    fun initData(isMoreRef: Boolean, bean: CashFlowBean?) {
        val list = bean?.financeList
        if (isFinishing || isDestroyed) {
            return
        }
        if (list != null && list.isNotEmpty()) {
            if (!isMoreRef) {
                adapter?.setList(list)
            } else {
                adapter?.addData(list)
            }
            val isMore = list.size == pageSize
            if (isMore) page++
            adapter?.apply {
                loadMoreModule.let {
                    it.isEnableLoadMore = true
                    if (!isMore) {
                        it.loadMoreEnd(!isMoreRef)
                    } else {
                        it.loadMoreComplete()
                    }
                }
            }

        } else {
            val isRef = !isMoreRef
            if (isRef) {
                adapter?.setList(null)
                adapter?.setEmptyView(EmptyForAdapterView(context))
            }
            adapter?.loadMoreModule?.loadMoreEnd(isRef)
        }
        swipe_refresh?.isRefreshing = false

    }


}
