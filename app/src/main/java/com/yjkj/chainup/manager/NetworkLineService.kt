package com.yjkj.chainup.manager

import android.app.IntentService
import android.content.Intent
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.util.LogUtil
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject

class NetworkLineService(name: String = "NetworkLineService") : IntentService(name) {

    val TAG = "NetworkLineService"
    override fun onCreate() {
        super.onCreate()
    }

    var mdDisposable = CompositeDisposable()
    var liksArray: ArrayList<JSONObject> = arrayListOf()
    var linesNetwork: ArrayList<JSONObject> = arrayListOf()
    var linesSpeed: HashMap<String, ArrayList<String>> = hashMapOf()
    var linesNum: HashMap<String, Int> = hashMapOf()
    var retryCount: Int = 5
    var retryInterval: Int = 10
    var differance: Int = 500

    override fun onHandleIntent(p0: Intent?) {
        LogUtil.v(TAG, "onHandleIntent()")
        if (!ApiConstants.isSaasNetwork()) {
            return
        }
//        Thread(Runnable {
//            try {
//                val jsonFile = Utils.getJSONLink(null)
//                val jsonFileCompanyID = Utils.getJSONLink(ApiConstants.APP_COMPANY_ID)
//                PublicInfoDataService.getInstance().saveCetData(jsonFile)
//                PublicInfoDataService.getInstance().saveCompanyIDData(jsonFileCompanyID)
//                if (!TextUtils.isEmpty(jsonFile)) {
//                    var jsonObject = JSONObject(jsonFile)
//                    val links = PublicInfoDataService.getInstance().getLinkData(true)
//                    retryCount = jsonObject.optInt("retryCount", retryCount)
//                    retryInterval = jsonObject.optInt("retryInterval", retryInterval)
//                    differance = jsonObject.optInt("differance", differance)
//                    if (links != null && links.size != 0) {
//                        // 测速
//                        liksArray.addAll(links)
//                        for ((index, item) in liksArray.withIndex()) {
//                            val line = item.optString("hostName")
//                            linesSpeed.put(line, arrayListOf())
//                            linesNum.put(line, (index + 1))
//                        }
//                        LogUtil.v(TAG, "liksArray ${liksArray.size}")
//                        loopTime()
//                    }
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }).start()
    }

//    private fun loopTime() {
//        mdDisposable.add(Observable.interval(1, retryInterval.toLong(), TimeUnit.SECONDS).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread()).subscribe {
//                    if (it < retryCount) {
//                        LogUtil.e(TAG, "当前测速 ${it + 1}")
//                        for (num in 0 until liksArray.size) {
//                            getHeath(num, liksArray[num].optString("hostName"))
//                        }
//                    } else {
//                        mdDisposable.dispose()
//                        // 发起请求
//                        autoSwitchLine()
//                    }
//                })
//    }

//    fun getHeath(index: Int, url: String) {
//        var startTime = System.currentTimeMillis()
//        var baseUrl = Utils.returnSpeedUrl(url, ApiConstants.BASE_URL)
//        HttpClient.instance.checkNetworkLine(baseUrl)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    var endTime = System.currentTimeMillis()
//                    var consum = endTime - startTime
//                    liksArray[index].put("networkAppapi", "$consum")
//                    LogUtil.v(TAG, "getHeath  success  线路 ${index + 1}  ${consum}")
//                    val tempArray = linesSpeed.get(liksArray[index].optString("hostName"))
//                    tempArray?.run {
//                        add(consum.toString())
//                    }
//                }, {
//                    liksArray[index].put("error", "error")
//                    LogUtil.v(TAG, "getHeath error 线路 ${index + 1}")
//                    val tempArray = linesSpeed.get(liksArray[index].optString("hostName"))
//                    tempArray?.run {
//                        add("0")
//                    }
//                })
//    }
//
//    private fun getSpeedResult(): Pair<ArrayList<Pair<String, Int>>, ArrayList<Pair<String, Int>>> {
//        val tempLine = arrayListOf<Pair<String, Int>>()
//        val speedError = arrayListOf<Pair<String, Int>>()
//        // 计算最优线路 倒序
//        for ((key, value) in linesSpeed) {
//            LogUtil.v(TAG, "测速结果 ${key} ${value}")
//            var count = 0
//            var sum = 0
//            value.forEach {
//                val error = it.toInt()
//                if (error == 0) {
//                    count++
//                }
//                sum += error
//            }
//            tempLine.add(Pair(key, sum / retryCount))
//            speedError.add(Pair(key, count))
//        }
//        return Pair(tempLine, speedError)
//    }
//
//    fun getCurrentItem(tempLine: ArrayList<Pair<String, Int>>): Pair<String, Int> {
//        val domainUrl = PublicInfoDataService.getInstance().newWorkURL
//        val item = tempLine.filter { it.first == domainUrl }
//        if (item.isNotEmpty()) {
//            return item.get(0)
//        }
//        return tempLine.get(0)
//    }
//
//    private fun autoSwitchLine() {
//        val speedResult = getSpeedResult()
//        val tempLine = speedResult.first
//        val speedError = speedResult.second
//        val errorSum = speedError.filter { it.second != 0 }
//        val item = getCurrentItem(tempLine)
//        if (errorSum.isNotEmpty()) {
//            // 测速数据 全部都是正常数据
//            if (tempLine.size == errorSum.size) {
//                // 说明全部错误
//                errorSum.sortedBy { it.second }
//                val topError = errorSum.filter {
//                    errorSum.get(0).second == it.second
//                }
//                // 处理当前相同错误的速度
//                val tempErrorLine = arrayListOf<Pair<String, Int>>()
//                topError.forEach { item ->
//                    val tempItem = tempLine.filter { it.first == item.first }
//                    tempErrorLine.addAll(tempItem)
//                }
//                speedTest(tempErrorLine, item, tempLine, speedError)
//            } else {
//                // 筛选出没出错的节点 走流程一 排序最小时间 处理当前相同错误的速度
//                val tempErrorLine = arrayListOf<Pair<String, Int>>()
//                tempLine.forEach { item ->
//                    val tempItem = errorSum.filter { it.first == item.first }
//                    if (tempItem.isEmpty()) tempErrorLine.add(item)
//                }
//                speedTest(tempErrorLine, item, tempLine, speedError)
//            }
//        } else {
//            speedTest(tempLine, item, tempLine, speedError)
//        }
//    }
//
//    private fun speedTest(tempLine: ArrayList<Pair<String, Int>>, item: Pair<String, Int>, lines: ArrayList<Pair<String, Int>>, linesError: ArrayList<Pair<String, Int>>) {
//        tempLine.sortBy { it.second }
//        LogUtil.v(TAG, "测速平均值结果 ${tempLine} ")
//        // 打包 上传json
//        val speed = item.second - tempLine.get(0).second
//        LogUtil.v(TAG, "当前线路 ${item} 最优线路 ${tempLine.get(0)}  速度差额${speed} ")
//        if (speed > differance || item.first != PublicInfoDataService.getInstance().newWorkURL) {
//            LogUtil.v(TAG, "切换线路 ")
//            HttpClient.instance.changeNetwork(tempLine.get(0).first)
//        } else {
//            LogUtil.v(TAG, "不切换线路 ")
//        }
//        val uploadLines = arrayListOf<SpeedItem>()
//        for ((index, item) in lines.withIndex()) {
//            val lineNum = if (linesNum.containsKey(item.first)) linesNum.get(item.first) else 0
//            val speedItem = SpeedItem(lineNum.toString(), item.second.toString(), linesError[index].second)
//            uploadLines.add(speedItem)
//        }
//        HttpClient.instance.uploadNetWorkLog(item.first, tempLine.get(0).first, JsonUtils.gson.toJson(uploadLines))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//
//                }, {
//
//                })
//    }


}