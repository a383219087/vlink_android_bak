package com.yjkj.chainup.manager

import android.app.IntentService
import android.content.Intent
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.net_new.JSONUtil
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NetworkLineErrorService(name: String = "NetworkLineService") : IntentService(name) {

    val TAG = "NetworkLineErrorService"
    override fun onCreate() {
        super.onCreate()
    }

    var mdDisposable = CompositeDisposable()
    var liksArray: ArrayList<JSONObject> = arrayListOf()
    var linesNetwork: ArrayList<JSONObject> = arrayListOf()
    var linesSpeed: HashMap<String, ArrayList<String>> = hashMapOf()
    var linesNum: HashMap<String, Int> = hashMapOf()
    var retryCount: Int = 1
    var retryInterval: Int = 10
    var differance: Int = 500

    override fun onHandleIntent(p0: Intent?) {
        LogUtil.v(TAG, "onHandleIntent()")
//        if (!ApiConstants.isSaasNetwork()) {
//            return
//        }
        Thread(Runnable {
            try {
                val jsonFile = PublicInfoDataService.getInstance().cetData
                if (jsonFile != null) {
                    val cetString = jsonFile.optJSONArray("links")
                    if (cetString != null && cetString.length() != 0) {
                        // 测速
                        liksArray.addAll(JSONUtil.arrayToList(cetString) ?: arrayListOf())
                        for ((index, item) in liksArray.withIndex()) {
                            val line = item.optString("hostName")
                            linesSpeed.put(line, arrayListOf())
                            linesNum.put(line, (index + 1))
                        }
                        LogUtil.v(TAG, "liksArray ${liksArray.size}")
                        loopTime()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private var currentCheckIndex = 0
    private fun loopTime() {
        mdDisposable.add(Observable.interval(3, retryInterval.toLong(), TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe {
                    if (it < retryCount) {
                        LogUtil.e(TAG, "当前测速 ${it + 1}")
                        getHeath(currentCheckIndex, liksArray[currentCheckIndex].optString("hostName"))
                    } else {
                        mdDisposable.dispose()
                        // 发起请求
                    }
                })
    }

    private fun getHeath(index: Int, url: String) {
        val startTime = System.currentTimeMillis()
        val baseUrl = Utils.returnSpeedUrl(url, ApiConstants.BASE_URL)
        HttpClient.instance.checkNetworkLine(baseUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    var endTime = System.currentTimeMillis()
                    var consum = endTime - startTime
                    liksArray[index].put("networkAppapi", "$consum")
                    LogUtil.v(TAG, "getHeath  success  线路 ${index + 1}  ${consum}")
                    val tempArray = linesSpeed.get(liksArray[index].optString("hostName"))
                    tempArray?.run {
                        add(consum.toString())
                    }
                    val domainUrl = PublicInfoDataService.getInstance().newWorkURL
                    if (url != domainUrl) {
                        HttpClient.instance.changeNetwork(url)
                    }
                    resetCheckStatus()
                }, {
                    liksArray[index].put("error", "error")
                    LogUtil.v(TAG, "getHeath error 线路 ${index + 1}")
                    val tempArray = linesSpeed.get(liksArray[index].optString("hostName"))
                    tempArray?.run {
                        add("0")
                    }
                    if (currentCheckIndex != (liksArray.size - 1)) {
                        currentCheckIndex++
                        Observable.timer(10, TimeUnit.SECONDS)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread()).subscribe {
                                    getHeath(currentCheckIndex, liksArray[currentCheckIndex].optString("hostName"))
                                }
                    } else {
                        resetCheckStatus()

                    }
                })
    }

    private fun getSpeedResult(): Pair<ArrayList<Pair<String, Int>>, ArrayList<Pair<String, Int>>> {
        val tempLine = arrayListOf<Pair<String, Int>>()
        val speedError = arrayListOf<Pair<String, Int>>()
        // 计算最优线路 倒序
        for ((key, value) in linesSpeed) {
            LogUtil.v(TAG, "测速结果 ${key} ${value}")
            var count = 0
            var sum = 0
            value.forEach {
                val error = it.toInt()
                if (error == 0) {
                    count++
                }
                sum += error
            }
            tempLine.add(Pair(key, sum / linesSpeed.size))
            speedError.add(Pair(key, count))
        }
        return Pair(tempLine, speedError)
    }

    fun getCurrentItem(tempLine: ArrayList<Pair<String, Int>>): Pair<String, Int> {
        val domainUrl = PublicInfoDataService.getInstance().newWorkURL
        val item = tempLine.filter { it.first == domainUrl }
        if (item.isNotEmpty()) {
            return item.get(0)
        }
        return tempLine.get(0)
    }


    private fun resetCheckStatus() {
        currentCheckIndex = 0
        UserDataService.getInstance().isNetworkCheckIng = false
    }


}