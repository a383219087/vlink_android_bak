package com.chainup.talkingdata

import android.content.Context
import android.util.Log
import com.chainup.http.BuildConfig
import com.tendcloud.tenddata.TCAgent


class AppAnalyticsExt private constructor() {
    val TAG = AppAnalyticsExt::class.java.simpleName


    companion object {
        private var INSTANCE: AppAnalyticsExt? = null

        private var APP_EVENT: String = ""

        var APP_EVENT_SpotTransactionPage: String = "SpotTransactionPage"
        var APP_EVENT_LeveragePage: String = "LeveragePage"
        var APP_EVENT_MarketPage: String = "MarketPage"
        var APP_EVENT_HomePage: String = "HomePage"
        var APP_EVENT_FiatPage: String = "FiatPage"
        var APP_EVENT_ContractPage: String = "ContractPage"
        var APP_EVENT_AssetsPage: String = "AssetsPage"


        var APP_ACTION_OrderCreate: String = "OrderCreate"
        var APP_ACTION_OrderCancel: String = "OrderCancel"
        var APP_ACTION_LeverCreate: String = "LeverCreate"
        var APP_ACTION_LeverCancel: String = "LeverCancel"


        var APP_HTTP_httpTrack: String = "httpTrack"
        var APP_HTTP_httpTrackLow: String = "httpTrackLow"
        var APP_HTTP_httpError: String = "httpError"

        var APP_HTTP_wsTrack: String = "wsTrack"
        var APP_HTTP_wsTrackRetry: String = "wsTrackRetry"






        var CONTRACT_APP_ACTION_1: String = "合约-下单区-开仓"
        var CONTRACT_APP_ACTION_2: String = "合约-下单区-平仓"
        var CONTRACT_APP_ACTION_3: String = "合约-下单百分比-10%"
        var CONTRACT_APP_ACTION_4: String = "合约-下单百分比-20%"
        var CONTRACT_APP_ACTION_5: String = "合约-下单百分比-50%"
        var CONTRACT_APP_ACTION_6: String = "合约-下单百分比-100%"
        var CONTRACT_APP_ACTION_7: String = "合约-下单区-对手1档"
        var CONTRACT_APP_ACTION_8: String = "合约-下单区-对手5档"
        var CONTRACT_APP_ACTION_9: String = "合约-下单区-对手10档"
        var CONTRACT_APP_ACTION_10: String = "合约-下单区-划转"
        var CONTRACT_APP_ACTION_11: String = "合约-持仓-平仓"
        var CONTRACT_APP_ACTION_12: String = "合约-持仓-市价"
        var CONTRACT_APP_ACTION_13: String = "合约-持仓-对手方最优"
        var CONTRACT_APP_ACTION_14: String = "合约-持仓-本方最优"
        var CONTRACT_APP_ACTION_15: String = "合约-持仓-分享"

        var CONTRACT_APP_PAGE_1: String = "合约-首页"
        var CONTRACT_APP_PAGE_2: String = "合约-盈亏记录"
        var CONTRACT_APP_PAGE_3: String = "合约-资金划转"
        var CONTRACT_APP_PAGE_4: String = "合约-资金流水"
        var CONTRACT_APP_PAGE_5: String = "合约-合约信息"
        var CONTRACT_APP_PAGE_6: String = "合约-交易设置"
        var CONTRACT_APP_PAGE_7: String = "合约-计算器"
        var CONTRACT_APP_PAGE_8: String = "合约-全部委托"
        var CONTRACT_APP_PAGE_9: String = "合约-当前委托"
        var CONTRACT_APP_PAGE_10: String = "合约-历史委托"
        var CONTRACT_APP_PAGE_11: String = "合约-委托详情"


        val instance: AppAnalyticsExt
            get() {
                if (INSTANCE == null) {
                    synchronized(AppAnalyticsExt::class.java) {
                        if (INSTANCE == null) {
                            INSTANCE = AppAnalyticsExt()
                        }
                    }
                }
                return INSTANCE!!
            }
    }

    init {
        // params
    }

    private var mContext: Context? = null
    fun init(context: Context, array: HashMap<String, String>) {
        mContext = context
        TCAgent.init(context, if (BuildConfig.DEBUG) "D915B928831F435A98D5A62D729C4171" else "96B480C942B347258AD061CF092D5091", context.packageName)
        val paramsVersion = "app_version"
        val paramsLan = "lan"
        val paramsNetwork = "network"
        val paramsDevice = "device"
        val paramsOS = "os"
        val paramsIdentifier = "identifier"
        if (array.containsKey("exChainupBundleVersion")) {
            TCAgent.setGlobalKV(paramsVersion, array["exChainupBundleVersion"])
        }
        if (array.containsKey("language")) {
            TCAgent.setGlobalKV(paramsLan, array["language"])
        }
        if (array.containsKey("appNetwork")) {
            TCAgent.setGlobalKV(paramsNetwork, array["appNetwork"])
        }
        if (array.containsKey("Mobile-Model-CU")) {
            TCAgent.setGlobalKV(paramsDevice, array["Mobile-Model-CU"])
        }
        if (array.containsKey("osName")) {
            TCAgent.setGlobalKV(paramsOS, array["osName"])
        }
        if (array.containsKey("device")) {
            TCAgent.setGlobalKV(paramsIdentifier, array["device"])
        }

    }

    fun activityStart(name: String) {
        TCAgent.onPageStart(mContext!!, name)
    }

    fun activityStop(name: String) {
        TCAgent.onPageEnd(mContext!!, name)
    }

    fun network(name: String, params: Map<String, Any>) {
        TCAgent.onEvent(mContext!!, name, "", params)
    }

    fun clickAction(name: String, params: Map<String, Any>?) {
        TCAgent.onEvent(mContext!!, name, "", params)
    }


    fun clickAction(name: String) {
        TCAgent.onEvent(mContext!!, name)
    }

}