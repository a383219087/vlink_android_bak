package com.yjkj.chainup.manager

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LogUtil
import io.reactivex.disposables.CompositeDisposable

class NetworkLineErrorService(name: String = "NetworkLineService") : IntentService(name) {

    val TAG = "NetworkLineErrorService"
    override fun onCreate() {
        super.onCreate()
    }

    var mdDisposable = CompositeDisposable()
    var liksArray: ArrayList<String> = arrayListOf()
//    var linesNetwork: ArrayList<JSONObject> = arrayListOf()
//    var linesSpeed: HashMap<String, ArrayList<String>> = hashMapOf()
//    var linesNum: HashMap<String, Int> = hashMapOf()
    var retryCount: Int = 1
    var retryInterval: Int = 10
    var differance: Int = 500
    private var currentCheckIndex = 0

    override fun onHandleIntent(p0: Intent?) {
        LogUtil.v(TAG, "onHandleIntent()")
//
//        liksArray.add("http://47.242.7.76/")
//        liksArray.add("http://47.242.7.76/")
//        liksArray.add("http://47.242.7.76/")
//        mdDisposable.add(Observable.interval(3, retryInterval.toLong(), TimeUnit.SECONDS).subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread()).subscribe {
//                if (it < retryCount) {
//                    LogUtil.e(TAG, "当前测速 ${it + 1}")
//                    getHeath(currentCheckIndex, liksArray[currentCheckIndex])
//                } else {
//                    mdDisposable.dispose()
//                    // 发起请求
//                }
//            })

    }




    @SuppressLint("CheckResult")
    private fun getHeath(index: Int, url: String) {
//        val startTime = System.currentTimeMillis()
//        val baseUrl = Utils.returnSpeedUrl(url, ApiConstants.BASE_URL)
//        HttpClient.instance.checkNetworkLine(baseUrl)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    val domainUrl = PublicInfoDataService.getInstance().newWorkURL
//                    if (url != domainUrl) {
//                        HttpClient.instance.changeNetwork(url)
//                    }
//                    resetCheckStatus()
//                }, {
//                    liksArray[index].put("error", "error")
//                    LogUtil.v(TAG, "getHeath error 线路 ${index + 1}")
//                    val tempArray = linesSpeed.get(liksArray[index].optString("hostName"))
//                    tempArray?.run {
//                        add("0")
//                    }
//                    if (currentCheckIndex != (liksArray.size - 1)) {
//                        currentCheckIndex++
//                        Observable.timer(10, TimeUnit.SECONDS)
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread()).subscribe {
//                                    getHeath(currentCheckIndex, liksArray[currentCheckIndex])
//                                }
//                    } else {
//                        resetCheckStatus()
//
//                    }
//                })
    }




    private fun resetCheckStatus() {
        currentCheckIndex = 0
        UserDataService.getInstance().isNetworkCheckIng = false
    }


}