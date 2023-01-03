package com.yjkj.chainup.ui.contract

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.blankj.utilcode.util.LogUtils
import com.chainup.contract.adapter.CpHoldContractNewAdapter
import com.chainup.contract.app.CpCommonConstant
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.bean.CpCreateOrderBean
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.listener.CpDoListener
import com.chainup.contract.ui.activity.CpContractStopRateLossActivity
import com.chainup.contract.utils.*
import com.chainup.contract.view.*
import com.coorchice.library.SuperTextView
import com.google.gson.Gson
import com.yjkj.chainup.R
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.util.LogUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_contract_hold.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit


/**
 * 当前持仓
 */
class ContractHoldFragment : CpNBaseFragment() {

    private var adapter: CpHoldContractNewAdapter? = null
    private var mList = ArrayList<CpContractPositionBean>()

    var subscribe: Disposable? = null


    override fun setContentView(): Int {
        return R.layout.fragment_contract_hold
    }


    override fun initView() {
        adapter = CpHoldContractNewAdapter(mList)
        adapter!!.setMySelf(false)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.setEmptyView(CpEmptyOrderForAdapterView(context ?: return))
        adapter?.addChildClickViewIds(
            com.chainup.contract.R.id.tv_forced_close_price_key,
            com.chainup.contract.R.id.tv_tag_price,
            com.chainup.contract.R.id.tv_settled_profit_loss_key
        )
        adapter?.setOnItemChildClickListener  { adapter, view, position ->
            if (CpClickUtil.isFastDoubleClick()) return@setOnItemChildClickListener
            val clickData = adapter.data[position] as CpContractPositionBean
            when (view.id) {
                com.chainup.contract.R.id.tv_tag_price -> {
                    CpNewDialogUtils.showDialogNew(
                        context!!,
                        getString(com.chainup.contract.R.string.cp_extra_text129),
                        true,
                        null,
                        getLineText("cl_margin_rate_str"),
                        getLineText("cp_extra_text28")
                    )
                }
                com.chainup.contract.R.id.tv_forced_close_price_key -> {
                    CpNewDialogUtils.showDialogNew(
                        context!!,
                        getString(com.chainup.contract.R.string.cp_extra_text130),
                        true,
                        null,
                        getLineText("cp_calculator_text4"),
                        getLineText("cp_extra_text28")
                    )
                }
                com.chainup.contract.R.id.tv_settled_profit_loss_key -> {
                    val obj: JSONObject = JSONObject()
                    obj.put("profitRealizedAmount", clickData.profitRealizedAmount)
                    obj.put("tradeFee", clickData.tradeFee)
                    obj.put("capitalFee", clickData.capitalFee)
                    obj.put("closeProfit", clickData.closeProfit)
                    obj.put("shareAmount", clickData.shareAmount)
                    obj.put("settleProfit", clickData.settleProfit)
                    obj.put(
                        "marginCoin",
                        CpClLogicContractSetting.getContractMarginCoinById(
                            context,
                            clickData.contractId
                        )
                    )
                    obj.put(
                        "marginCoinPrecision",
                        CpClLogicContractSetting.getContractMarginCoinPrecisionById(
                            context,
                            clickData.contractId
                        )
                    )
                    CpSlDialogHelper.showProfitLossDetailsDialog(context!!, obj, 0)
                }
            }
        }
        loopStart()
    }

    private fun loopStart() {
        loopStop()
        LogUtil.d("getPositionAssetsList","我是6")
        subscribe = Observable.interval(0L, CpCommonConstant.capitalRateLoopTime, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (!CpClLogicContractSetting.isLogin()) return@subscribe
                addDisposable(
                    getContractModel().getPositionAssetsList(
                        consumer = object : CpNDisposableObserver(true) {
                            @SuppressLint("SetTextI18n")
                            var  lidt = ArrayList<CpContractPositionBean>()
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                jsonObject.optJSONObject("data")?.run {
                                    if (!isNull("positionList")) {
                                        val mOrderListJson = optJSONArray("positionList")
                                        if (mOrderListJson != null) {
                                            for (i in 0 until mOrderListJson.length()) {
                                                val obj = mOrderListJson.getString(i)
                                                lidt.add(
                                                    Gson().fromJson(
                                                        obj,
                                                        CpContractPositionBean::class.java
                                                    )
                                                )
                                            }
                                        }
                                        mList=lidt
                                        adapter?.setList(mList)
                                    }
                                }
                            }
                        })
                )
            }
    }

    private fun loopStop() {
        if (subscribe != null) {
            subscribe?.dispose()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden){
            loopStop()
        }
    }

    override fun onPause() {
        super.onPause()
        loopStop()
    }



}