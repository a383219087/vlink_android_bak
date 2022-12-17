package com.yjkj.chainup.ui.contract

import android.annotation.SuppressLint
import com.chainup.contract.adapter.CpHoldContractNewAdapter
import com.chainup.contract.app.CpCommonConstant
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.bean.CpContractPositionBean
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.view.CpEmptyOrderForAdapterView
import com.chainup.contract.view.CpMyLinearLayoutManager
import com.google.gson.Gson
import com.yjkj.chainup.R
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.util.LogUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_contract_hold.*
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


    var mPositionObj: JSONObject? = null
    override fun initView() {
        adapter = CpHoldContractNewAdapter(mList)
        adapter!!.setMySelf(false)
        rv_hold_contract.layoutManager = CpMyLinearLayoutManager(context)
        rv_hold_contract.adapter = adapter
        adapter?.setEmptyView(CpEmptyOrderForAdapterView(context ?: return))
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