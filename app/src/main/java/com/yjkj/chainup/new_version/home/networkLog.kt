package com.yjkj.chainup.new_version.home

import android.app.Activity
import android.view.View
import com.yjkj.chainup.db.MMKVDb
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject


/**
 *  首页引导流程
 */

fun sendWsHomepage(context: Activity, errorType: Int = 0, page: String, action: String, duration: Long?) {
    // 默认处理首页数据  顶部搜索
//
//    val isActive = !context.isFinishing
//    sendWsHomepage(isActive, errorType, page, action, duration)
}

fun sendWsHomepage(isActive: Boolean, errorType: Int = 0, page: String, action: String, duration: Long?) {
    if (!isActive) {
        return
    }
    if (duration != null && duration >= 12000) {
        return
    }
    val domainUrl = PublicInfoDataService.getInstance().newWorkURL
    HttpClient.instance.uploadNetWorkInfoLog(domainUrl, errorType, page, action, duration)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

            }, {

            })
}

class NetworkDataService private constructor() {
    var mMMKVDb: MMKVDb = MMKVDb()

    companion object {
        private const val TAG = "PublicInfoDataService"
        private const val key = "advertJson"
        private const val key_advert_time = "advertTime"
        const val KEY_GUIDE_HOME_SKIP = "guideHomeSkip"

        const val KEY_PAGE_KLINE = "kline"
        const val KEY_PAGE_HOME = "home"
        const val KEY_PAGE_MARKET = "market"
        const val KEY_PAGE_MARKET_SERVICE = "wsService"
        const val KEY_PAGE_TRANSACTION = "transaction"
        const val KEY_SUB_KLINE_HISTORY = "sub_history"
        const val KEY_SUB_TRAN_DEPTH = "sub_depth"
        const val KEY_SUB_MARKET_BATCH = "sub_batch"
        const val KEY_HTTP_HOME = "http_home"
        const val KEY_WS_OPEN = "ws_open"
        private var mPublicInfoDataService: NetworkDataService? = null
        val instance: NetworkDataService
            get() {
                if (null == mPublicInfoDataService) {
                    mPublicInfoDataService = NetworkDataService()
                }
                return mPublicInfoDataService!!
            }
    }

    fun showAdvert() {
        // 获取当前有没有可展示的广告
        // 处理消息机制流程
    }


}
