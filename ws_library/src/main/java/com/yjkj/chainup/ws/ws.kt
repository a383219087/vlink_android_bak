package com.yjkj.chainup.ws

import android.util.Log
import com.elvishew.xlog.XLog
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.yjkj.chainup.bean.kline.WsLinkBean
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okio.ByteString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class WsAgentManager private constructor() {
    var mOkHttpClient: OkHttpClient? = null
    var mWebSocket: WebSocket? = null
    var serverUrl: String = ""
    private val TAG = this.javaClass.simpleName
    private var mCount = 0
    private var reCount = 9
    private var isAppStopWs = true
    private var isAppStopLast = false
    private var isAppRestartStopWs = false
    var isBackgroud = false
    private val callbacks = arrayListOf<WsResultCallback>()
    var subscribePong: Disposable? = null//保存订阅者
    private val mapSubCallbacks = hashMapOf<String, HashMap<String, WsLinkBean>>()
    private val mapAnySubCallbacks = hashMapOf<String, HashMap<String, Any>>()

    private val subCallbacks = hashMapOf<String, WsResultCallback>()
    private val subCallbackTimes = hashMapOf<String, Boolean>()
    private val subCallbackTimeCount = hashMapOf<String, Long>()
    private val subCallbackTimeCountInit = hashMapOf<String, Boolean>()
    private var marketInit = 0
    var cid: Int = 0
    var wsConnectionTime = 0L
    private var isAppRep = false
    var reqJson: HashMap<String, LinkedTreeMap<String, Any>>? = null

    interface WsResultCallback {
        fun onWsMessage(json: String)
    }

    companion object {
        var wsAgentManager: WsAgentManager? = null
        const val WEBSOCKET_tickerFor24HLink = "tickerFor24HLink" // 24H的行情
        const val WEBSOCKET_getDepthLink = "depthLink" //深度盘口
        val instance: WsAgentManager
            get() {
                if (wsAgentManager == null)
                    wsAgentManager = WsAgentManager()
                return wsAgentManager!!
            }
    }

    init {
        val newBuilder = OkHttpClient().newBuilder()
        val sslParams = HttpsUtils.getSslSocketFactory(null, null, null)
        newBuilder.protocols(Collections.singletonList(Protocol.HTTP_1_1))
        newBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)

        mOkHttpClient = newBuilder
                .retryOnConnectionFailure(true)
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

    }

    fun socketUrl(socketUrl: String, isMainThread: Boolean = true) {
        this.serverUrl = socketUrl
        if (isMainThread) {
            initWS()
        }
    }

    fun sendMessage(message: HashMap<String, Any>, callback: WsResultCallback?) {
        if (!isConnection()) {
            initWS()
        }
        if (callback != null) {
            val key = callback.javaClass.simpleName
//            Log.e(TAG, "sendMessage ${key} 是否存在数据 ${mapSubCallbacks.containsKey(key)}")
            if (mapSubCallbacks.containsKey(key)) {
//                Log.e(TAG, "sendMessage ${key} 数据 ${mapSubCallbacks.get(key)}")
                lastNew = key
                when (key) {
                    "NCVCTradeFragment" -> {
                        subCallbackTimes.remove(key)
                        mapAnySubCallbacks.put(key, message)
                        val symbol = message.get("symbol") as String
                        val step = if (message.containsKey("step")) message.get("step") as String else "0"
                        val team = mapSubCallbacks.get(key) as HashMap
                        val temp = if (team.containsKey("depthLink")) (team.get("depthLink") as WsLinkBean).symbol else ""
                        val tempStep = if (team.containsKey("depthLink")) (team.get("depthLink") as WsLinkBean).step else ""
                        val symbolEquls = temp != symbol
                        val symbolStep = tempStep != step && tempStep.isNotEmpty()
                        val reSend = symbolEquls || symbolStep
//                        Log.e(TAG, "判断当前是否 symbol != ${symbolEquls}  step  old ${tempStep} new ${step}  != ${symbolStep} ")
                        if (team.isNotEmpty() && team.size != 0 && reSend) {
                            unbind(callback, false, symbolEquls, symbolStep)
                        }
                        val isReSend = temp.isEmpty() || reSend
                        if (isReSend) {
//                            Log.e(TAG, "bind ws set")
                            val ticker = WsLinkUtils.tickerFor24HLinkBatchBean(symbol)
                            val depthLink = WsLinkUtils.getDepthLink(symbol.split(",").first(), true, if (step.isEmpty()) "0" else step)
                            val map = hashMapOf("ticker24H" to ticker, "depthLink" to depthLink)
                            mapSubCallbacks.put(key, map)
                            // ticker
                            if (!symbolStep || symbolEquls) {
                                sendData(ticker)
                            }
                            // depthLink
                            sendData(depthLink)
                        }
                    }
                    "MarketFragment", "NewVersionHomepageFragment", "NewVersionHomepageFirstFragment" -> {
                        subCallbackTimes.remove(key)
                        subCallbackTimeCount.put(key, System.currentTimeMillis())
                        subCallbackTimeCountInit.remove(key)
                        val symbol = message.get("symbols") as String
                        val bind = message.get("bind") as Boolean
                        val type = JsonWSUtils.gson.fromJson<Array<String>>(symbol, object : TypeToken<Array<String>>() {}.type)
                        val arrays = StringBuffer()
                        for (item in type) {
                            arrays.append(item + ",")
                        }
                        if (arrays.isEmpty()) {
                            return
                        }
                        val event = WsLinkUtils.tickerFor24HLinkBatchBean(arrays.substring(0, arrays.length - 1), bind)
                        unbind(callback, false)
                        sendData(event)
                        mapAnySubCallbacks.put(key, message)
                        mapSubCallbacks.put(key, hashMapOf("batchMarket" to event))
                    }
                    "CoinSearchDialogFg" -> {
                        val symbol = message.get("symbols") as String
                        val bind = message.get("bind") as Boolean
                        val type = JsonWSUtils.gson.fromJson<Array<String>>(symbol, object : TypeToken<Array<String>>() {}.type)
                        val arrays = StringBuffer()
                        for (item in type) {
                            arrays.append(item + ",")
                        }
                        val event = WsLinkUtils.tickerFor24HLinkBatchBean(arrays.substring(0, arrays.length - 1), bind)
                        unbind(callback, false)
                        sendData(event)
                        mapAnySubCallbacks.put(key, message)
                        mapSubCallbacks.put(key, hashMapOf("batchMarket" to event))
                    }
                    "MarketDetail4Activity" -> {
                        subCallbackTimes.remove(key)
                        val symbol = message.get("symbol") as String
                        val step = if (message.containsKey("line")) message.get("line") as String else "15min"

                        val team = mapSubCallbacks.get(key) as HashMap
                        val temp = if (team.containsKey("depthLink")) (team.get("depthLink") as WsLinkBean).symbol else ""

                        val symbolEquls = temp != symbol
                        // 不等于 说明新币种
                        val isSend = temp.isNotEmpty()
                        if (team.isNotEmpty() && team.size != 0 && isSend) {
                            unbind(callback, false, symbolEquls, false)
                        }

                        var map = hashMapOf<String, WsLinkBean>()
                        val isReSend = temp.isEmpty() || symbolEquls
                        if ((team.isEmpty() || team.size == 0) || isReSend) {
                            val ticker = WsLinkUtils.tickerFor24HLinkBean(symbol)
                            sendData(ticker)
                            map["ticker24H"] = ticker

                            val depthLink = WsLinkUtils.getDepthLink(symbol)
                            sendData(depthLink)
                            map["depthLink"] = depthLink

                            val depthList = WsLinkUtils.getDealHistoryLink(symbol)
                            sendData(depthList)
                            map["depthList"] = depthList
                        } else {
                            map = mapSubCallbacks.get(key) as HashMap<String, WsLinkBean>
                        }
                        //
                        val kineHistory = WsLinkUtils.getKLineHistoryLink(symbol, step)
                        sendData(kineHistory)
                        map["line"] = kineHistory
                        mapAnySubCallbacks.put(key, message)


                        mapSubCallbacks.put(key, map)
                    }
                    "HorizonMarketDetailActivity" -> {
                        val symbol = message.get("symbol") as String
                        val step = if (message.containsKey("line")) message.get("line") as String else "15min"

                        val team = mapSubCallbacks.get(key) as HashMap
                        val temp = if (team.containsKey("line")) (team.get("line") as WsLinkBean).symbol else ""

                        val symbolEquls = temp != symbol
                        // 不等于 说明新币种
                        val isSend = temp.isNotEmpty()
                        if (team.isNotEmpty() && team.size != 0 && isSend) {
                            unbind(callback, false, symbolEquls, false)
                        }
                        var map = hashMapOf<String, WsLinkBean>()
                        val isReSend = temp.isEmpty() || symbolEquls
                        if ((team.isEmpty() || team.size == 0) || isReSend) {
                            val ticker = WsLinkUtils.tickerFor24HLinkBean(symbol)
                            sendData(ticker)
                            map["ticker24H"] = ticker
                        } else {
                            map = mapSubCallbacks.get(key) as HashMap<String, WsLinkBean>
                        }
                        //
                        val kineHistory = WsLinkUtils.getKLineHistoryLink(symbol, step)
                        sendData(kineHistory)
                        map["line"] = kineHistory
                        mapAnySubCallbacks.put(key, message)
                        mapSubCallbacks.put(key, map)
                    }
                    "NGridFragment" -> {
                        mapAnySubCallbacks.put(key, message)
                        val symbol = message.get("symbol") as String
                        val ticker = WsLinkUtils.tickerFor24HLinkBean(symbol)
                        sendData(ticker)
                        var map = hashMapOf("ticker24H" to ticker)
                        mapSubCallbacks.put(key, map)
                    }
                    "GridExecutionDetailsActivity" -> {
                        mapAnySubCallbacks.put(key, message)
                        val symbol = message.get("symbol") as String
                        val ticker = WsLinkUtils.tickerFor24HLinkBean(symbol)
                        sendData(ticker)
                        var map = hashMapOf("grid_ticker24H" to ticker)
                        mapSubCallbacks.put(key, map)
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun sendData(wsLinkBean: WsLinkBean) {
        if (isConnection()) {
            val send = mWebSocket?.send(wsLinkBean.json)
//            Log.e(TAG, "sendData ${send}  data =  ${wsLinkBean.json}  ")
        } else {
//            Log.e(TAG, "ws sendData  处于断线状态 无法发送  ${wsLinkBean.json} ")
        }
    }

    fun sendData(wsLinkBean: String) {
//        Log.e(TAG, "ws sendData wsLinkBean ${wsLinkBean}")
        if (isConnection()) {
            val sendStatus = mWebSocket?.send(wsLinkBean)
//            Log.d(TAG, "ws sendData sendStatus ${sendStatus}")
        } else {
//            Log.e(TAG, "ws sendData  处于断线状态 无法发送 ")
        }
    }


    fun unbind(callback: WsResultCallback?, isStop: Boolean = true, isSymbol: Boolean = false, step: Boolean = false, time: Boolean = false) {
//        Log.e(TAG, "unbind ws unset")
        if (callback != null) {
            val key = callback.javaClass.simpleName
            Log.e(TAG, "unbind ws ${key} ")
            if (mapSubCallbacks.containsKey(key)) {
                when (key) {
                    "NCVCTradeFragment" -> {
                        val map = mapSubCallbacks.get(key) as HashMap
                        if (map.contains("depthLink")) {
                            val bean = map.get("depthLink") as WsLinkBean
                            val symbol = bean.symbol
                            val ticker = WsLinkUtils.tickerFor24HLinkBean(removeOtherhaveString(key), false)
                            val depthLink = WsLinkUtils.getDepthLink(symbol, false, bean.step)
                            if (!step || isSymbol) {
                                sendData(ticker)
                            }
                            sendData(depthLink)
                        }

                    }
                    "MarketFragment", "NewVersionHomepageFragment", "CoinSearchDialogFg", "NewVersionHomepageFirstFragment" -> {
                        val map = mapSubCallbacks.get(key) as HashMap
                        if (map.contains("batchMarket")) {
                            val bean = map.get("batchMarket") as WsLinkBean
                            val symbol = bean.symbol
                            val event = WsLinkUtils.tickerFor24HLinkBatchBean(removeOtherhaveString(key), false)
                            sendData(event)
                        }
                    }
                    "MarketDetail4Activity" -> {
                        val map = mapSubCallbacks.get(key) as HashMap
                        if (map.contains("line")) {
                            val bean = map.get("line") as WsLinkBean
                            val symbol = bean.symbol

                            val ticker = WsLinkUtils.tickerFor24HLinkBean(removeOtherhaveString(key), false)
                            if (isSymbol || isStop) {
                                sendData(ticker)

                                val depthLink = WsLinkUtils.getDepthLink(symbol, false)
                                sendData(depthLink)

                                val depthNew = WsLinkUtils.getDealNewLink(symbol, false)
                                sendData(depthNew)
                            }
                            val time = if (bean.time != "") bean.time else "15min"
                            val depthKline = WsLinkUtils.getKlineNewLink(symbol, time, false)
                            sendData(depthKline)

                        }
                    }
                    "HorizonMarketDetailActivity" -> {
                        val map = mapSubCallbacks.get(key) as HashMap
                        if (map.contains("line")) {
                            val bean = map.get("line") as WsLinkBean
                            val symbol = bean.symbol
                            val ticker = WsLinkUtils.tickerFor24HLinkBean(removeOtherhaveString(key), false)
                            if (isSymbol || isStop) {
                                sendData(ticker)
                            }
                            val time = if (bean.time != "") bean.time else "15min"
                            val depthNew = WsLinkUtils.getKlineNewLink(symbol, time, false)
                            sendData(depthNew)
                        }
                    }
                    "GridExecutionDetailsActivity" -> {
                        val map = mapSubCallbacks.get(key) as HashMap
                        if (map.contains("grid_ticker24H")) {
                            try {
                                val bean = map.get("grid_ticker24H") as WsLinkBean
                                val ticker = WsLinkUtils.tickerFor24HLinkBean(bean.symbol, false)
                                sendData(ticker)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    else -> {

                    }

                }
                if (isStop) {
                    removeWsCallback(callback)
                }
            } else {
//                Log.e(TAG, "unbind ws ${key} 找不到")
            }
        }
    }

    fun reConnection() {
//        Log.e(TAG, "WS 是否不重连1 ${isAppStopWs} mCount ${mCount} isChange ${isAppRestartStopWs}")
        mWebSocket = null
        isConnection = false
        isAppStopLast = true
        wsConnectionTime = 0
        if (!isAppStopWs) {
            if (!isAppRestartStopWs) {
                errorChangeNetwork()
            }
            if (mCount <= reCount) {
//                Log.e(TAG, "WS 是重连")
                Observable.timer(1, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe {
                            initWS()
                            mCount++
                        }
            } else {
//                Log.e(TAG, "WS 不重连")
                resetParams()
            }
        }
    }

    fun changeNotice(status: NetUtil.Companion.ConnectStatus) {
        if (status != NetUtil.Companion.ConnectStatus.NO_NETWORK) {
            if (!isConnection()) {
                resetParams()
                stopPong()
                reConnection()
            }
        }
    }

    fun resetParams() {
        mCount = 0
    }

    @Synchronized
    fun addWsCallback(callback: WsResultCallback) {
        val key = callback.javaClass.simpleName
        if (mapSubCallbacks.contains(key)) {
//            Log.e(TAG, "${callback.javaClass.name}  exist in callbacks, index is ${callbacks.indexOf(callback)} ")
//            subCallbacks.remove(key)
//            mapSubCallbacks.remove(key)
//            subCallbacks.put(key, callback)
//            callbacks.forEach {
//                if (it.javaClass.simpleName == key) {
//                    callbacks.remove(it)
//                }
//            }
        }
        mapSubCallbacks.put(key, hashMapOf())
        subCallbacks.put(key, callback)
    }

    @Synchronized
    fun removeWsCallback(callback: WsResultCallback): Boolean {
        val key = callback.javaClass.simpleName
        if (mapSubCallbacks.size == 0) {
//            Log.e(TAG, "remove callback failed ,because callbacks size == 0, name is  ${callback.javaClass.name}")
            return false
        }
        mapSubCallbacks.set(key, hashMapOf())
        return true
    }

    private var isConnection = false
    private fun initWS() {
//        Log.e("我是ws", "initWS()  ${isAppStopWs} isConnection ${isConnection} serverUrl ${serverUrl}")
        if (isConnection) {
            return
        }
        if (serverUrl.isEmpty() || !serverUrl.startsWith("ws")) {
            return
        }
//        print("init ws ${serverUrl}")
        isAppStopWs = false
        isConnection = true
        val request = Request.Builder().url(this.serverUrl).build()
        wsConnectionTime = System.currentTimeMillis()
        mOkHttpClient?.newWebSocket(request, wsEventList)
        initPong()

    }

    fun isConnection(): Boolean {
        if (mWebSocket != null) {
            return true
        }
        return false
    }

    private var wsEventList = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            wsConnectionTime = System.currentTimeMillis() - wsConnectionTime
//            Log.e(TAG, "onOpen() data =  ${response.code} time ${wsConnectionTime}ms")
//            print("open ws ${response.code}")
            mWebSocket = webSocket
            isConnection = false
            isAppRestartStopWs = false
            resetParams()
//            Log.d(TAG, "重新发送 lastNew =  ${lastNew}")
            if (!isBackgroud) {
                wsBackgroupChange(lastNew)
            }
            if (marketInit == 0) {
                val event = WsLinkUtils.tickerFor24HLinkReqBean(cid)
                sendData(event)
            }
            val message = MessageEvent(MessageEvent.refresh_ws_open_change)
            message.msg_content = wsConnectionTime
            EventBusUtil.post(message)
            wsConnectionTime = 0
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            if (bytes.size == 0) {
                return
            }
            val data = GZIPUtils.uncompressToString(bytes.toByteArray())
            if (data.isNullOrEmpty()) {
                return
            }
            if (data.contains("ping")) {
//                Log.v(TAG, "onMessage() data =  ${data}")
                val replace = data.replace("ping", "pong")
                sendData(replace)
            } else {
                try {
//                    Log.v(TAG, "onMessage() data =  ${data}")
                    val jsonObj = JSONObject(data)
                    if (!jsonObj.isNull("tick")) {
                        val channel = jsonObj.optString("channel")
                        subCallbacks.forEach {
                            val key = it.key
                            val callValue = it.value
                            if (mapSubCallbacks.containsKey(key)) {
                                val hashMap = mapSubCallbacks.get(key)
                                hashMap?.apply {
                                    for (hashItem in hashMap.entries) {
                                        val isSend = dataCall(channel, hashItem.value, callValue, data, key)
                                        if (isSend) {
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (!jsonObj.isNull("data")) {
                            val channel = jsonObj.optString("channel")
                            val key = channel.contains("kline") || channel.contains("trade")
                            if (key) {
                                dataCall(channel, WsLinkBean("", ""), subCallbacks.get(keyLine)!!, data)
                            } else {
                                dataCall(channel, WsLinkBean("", ""), subCallbacks.get("MarketFragment")!!, data)
                            }

                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            t.printStackTrace()
//            print("onFailure ws ${response?.code}")
//            Log.e(TAG, "onFailure() data =  ${t.message} response ${response?.code}")
            reConnection()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
//            Log.e(TAG, "onClosed() data =  ${code}")
//            print("onClosed ws $code")
            reConnection()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
//            Log.e(TAG, "onClosing() data =  ${code}")
//            print("onClosing ws $code")
            reConnection()
        }
    }

    fun stopWs() {
        isAppStopWs = true
        stopPong()
        mWebSocket?.cancel()
        mWebSocket?.close(1000, null)
        mapSubCallbacks.clear()
        mapAnySubCallbacks.clear()
        callbacks.clear()
        subCallbacks.clear()
        resetParams()
        marketInit = 0


    }

    private fun initPong() {
        if (subscribePong == null || (subscribePong != null && subscribePong?.isDisposed != null && subscribePong?.isDisposed!!)) {
            subscribePong = Observable.interval(20, TimeUnit.SECONDS)//按时间间隔发送整数的Observable
                    .observeOn(AndroidSchedulers.mainThread())//切换到主线程修改UI
                    .subscribe {
                        sendData(WsLinkUtils.pongBean().json)
                    }
        }
    }

    private fun stopPong() {
        subscribePong?.dispose()
        wsConnectionTime = 0
    }

    private fun dataCall(dataChannel: String, mapChannel: WsLinkBean, wsResultCallback: WsResultCallback, json: String, keySub: String = ""): Boolean {
        if (dataChannel == mapChannel.channel) {
            wsResultCallback.onWsMessage(json)
            val key = wsResultCallback.javaClass.simpleName
            coinCountTime(key)
        } else {
            if (dataChannel.contains("kline") || dataChannel.contains("trade")) {
                wsResultCallback.onWsMessage(json)
                if (wsResultCallback.javaClass.simpleName == "MarketDetail4Activity") {
                    val isSend = dataChannel.contains("kline")
                    val event = JSONObject(json).optString("event_rep")
                    val isReq = event.contains("rep")
                    if (isSend && isReq) {
                        subCallbackTimes.put(wsResultCallback.javaClass.simpleName, true)
                    }
                } else if (wsResultCallback.javaClass.simpleName == "NCVCTradeFragment") {
                    val isSend = dataChannel.contains("depth_step")
                    if (isSend) {
                        subCallbackTimes.put(wsResultCallback.javaClass.simpleName, true)
                    }
                }
            } else if (dataChannel.contains("ticker")) {
                if (mapChannel.event.contains("batch")) {
                    if (keySub == "CoinSearchDialogFg") {
                        wsResultCallback.onWsMessage(json)
                    } else {
                        val arrays = mapChannel.channel.split(",")
                        for (item in arrays) {
                            if (item == dataChannel) {
                                wsResultCallback.onWsMessage(json)
                                val key = wsResultCallback.javaClass.simpleName
                                coinCountTime(key)
                            }
                        }
                    }
                } else {
                    return false
                }
            } else if (dataChannel.contains("review") || dataChannel.contains("reviewV2")) {
                marketInit++
                wsResultCallback.onWsMessage(json)
                val repObject = JSONObject(json).optJSONObject("data")
                if (repObject != null) {
                    reqJson = JsonWSUtils.gson.fromJson(repObject.toString(), HashMap<String, LinkedTreeMap<String, Any>>()::class.java)
                }
            } else {
                return false
            }
        }
        return true
    }

    var lastNew = ""
    fun wsBackgroupChange(fragmentName: String, unBind: Boolean = false) {
        if (mapSubCallbacks.containsKey(fragmentName)) {
//            Log.e(TAG, "wsBackgroupChange() 切换 =  ${fragmentName} ${unBind}")
            lastNew = fragmentName
            val arrays = mapSubCallbacks.get(fragmentName)
            if (arrays != null) {
                if (unBind) {
                    unbind(subCallbacks.get(fragmentName), true)
                } else {
                    if (mapAnySubCallbacks.containsKey(fragmentName)) {
                        mapSubCallbacks.set(lastNew, hashMapOf())
                        sendMessage(mapAnySubCallbacks.get(fragmentName)!!, subCallbacks.get(fragmentName))
                    }
                }
            } else {
//                Log.e(TAG, "wsBackgroupChange() 切换 = 没有找到 ${fragmentName} ${unBind}")
            }
        } else {
//            Log.e(TAG, "wsBackgroupChange() 切换 找不到 =  ${fragmentName} ${unBind} ")
        }
    }

    var keyLine = ""
    fun changeKlineKey(name: String) {
        this.keyLine = name
    }

    enum class WSStatus {
        CONNECTIONING,
        CONNECTION
    }

    fun saveCID(cid: String?) {
        if (cid != null && cid.isNotEmpty()) {
            this.cid = cid.toInt()
        }
    }

    private fun print(message: String) {
        try {
//            XLog.e(StringBuffer("ws: ${message}"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopWs(url: String, isReStop: Boolean = true) {
        isAppStopWs = false
        isAppRestartStopWs = isReStop
        stopPong()
        mWebSocket?.cancel()
        mWebSocket?.close(1000, null)
        callbacks.clear()
        resetParams()
        marketInit = 0


    }

    private fun errorChangeNetwork() {
//        Log.e(TAG, "WS 断开 isChange ${isAppRestartStopWs} 发起切换服务")
        val message = MessageEvent(MessageEvent.refresh_ws_error_change)
        EventBusUtil.post(message)
    }

    fun pageSubWs(wsResultCallback: WsResultCallback): Boolean {
//        Log.e(TAG, "WS pageSubWs ${subCallbackTimes.size}")
        return subCallbackTimes.containsKey(wsResultCallback.javaClass.simpleName)
    }

    fun pageSubWsTime(wsResultCallback: WsResultCallback): Long {
        val page = wsResultCallback.javaClass.simpleName
        val key = subCallbackTimeCount.containsKey(page)
        if (key) {
            return subCallbackTimeCount.get(page) as Long
        }
        return 0
    }

    fun getMarketDataBySymbol(item: JSONObject): LinkedTreeMap<String, Any>? {
        if (null != reqJson) {
            val key = item.getString("symbol")
//            Log.e(TAG, "WS getMarketDataBySymbol ${key}")
            if (reqJson?.containsKey(key)!!) {
                val tick = reqJson?.get(key)
                return tick
            }
        }
        return null
    }

    private fun coinCountTime(pageKey: String) {
        val key = pageKey
        val isCountTime = subCallbackTimeCountInit.containsKey(key)
        if (key == "MarketFragment" && !isCountTime) {
            val time = System.currentTimeMillis() - subCallbackTimeCount.get(key) as Long
            subCallbackTimeCount.put(key, time)
            subCallbackTimeCountInit.put(key, true)
        }
    }

    @Synchronized
    private fun removeOtherhaveString(key: String): String {
        if (mapAnySubCallbacks.containsKey(key)) {
            val keymap = mapAnySubCallbacks.get(key)!!
            var resule = ""
            if (keymap.containsKey("symbols")) {
                resule = JsonWSUtils.toJsonArray(keymap.get("symbols").toString()).splicAppend(",")
            } else if (keymap?.containsKey("symbol")) {
                resule = keymap.get("symbol").toString()
            }
            if (resule.isEmpty()) {
                return ""
            }
            for ((classname, map) in mapAnySubCallbacks) {
                if (classname.equals(key)) {
                    continue
                }
                var substr = ""
                substr = if (map.containsKey("symbol")) {
                    map["symbol"].toString()
                } else if (map.containsKey("symbols")) {
                    JsonWSUtils.toJsonArray(map.get("symbols").toString()).splicAppend(",")
                } else {
                    continue
                }
                var subarr = substr.split(",")
                for (sub in subarr) {
                    if (resule.contains(sub)) {
                        resule = resule.replace(sub, "")
                        resule = resule.replace(",,", ",")
                        if (resule.isNotEmpty() && resule.first() == ',') {
                            resule = resule.removeRange(0, 1)
                        }
                    }
                }
            }
//            Log.e(TAG, "NoHaveOtherString===$resule")
            return resule.toString()
        }
        return ""
    }
}