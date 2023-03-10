package com.yjkj.chainup.manager

import com.yjkj.chainup.util.LogUtil
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

/**
 *

 * @Description:

 * @Author:         wanghao

 * @CreateDate:     2019-09-20 19:10

 * @UpdateUser:     wanghao

 * @UpdateDate:     2019-09-20 19:10

 * @UpdateRemark:   更新说明

 */
class SymbolWsData {

    fun getNewSymbolObjContract(symbols: ArrayList<JSONObject>?, socketJSON: JSONObject?): JSONObject? {
        if (null == symbols || symbols.size <= 0 || null == socketJSON)
            return null

        val tick = socketJSON.optJSONObject("tick")
        if (null == tick || tick.length() <= 0)
            return null

        val channel = socketJSON.optString("channel")
        if (null == channel || !channel.contains("_")) {
            return null
        }

        var tickSymbol = channel.split("_")[1]+channel.split("_")[2]

        for (jsonObject in symbols) {
            //market_e_etcusdt_ticker
            val symbol = jsonObject.optString("contractType").toLowerCase()+jsonObject.optString("symbol").replace("-", "").toLowerCase()
            if (tickSymbol.equals(symbol, ignoreCase = true)) {
                try {
                    val close = tick.optString("close")
                    jsonObject.put("amount", tick.optString("amount"))
                    jsonObject.put("close", close)
                    jsonObject.put("high", tick.optString("high"))
                    jsonObject.put("low", tick.optString("low"))
                    jsonObject.put("open", tick.optString("open"))
                    jsonObject.put("rose", tick.optString("rose"))
                    jsonObject.put("vol", tick.optString("vol"))

                    var name = jsonObject?.optString("name")//NCoinManager.showAnoterName(jsonObject)
                    if(null!=name && name.contains("/")){
                        var split = name.split("/")
                        val rateResult = RateManager.getCNYByCoinName(split[1], close)
                        jsonObject.put("rateResult", rateResult)
                    }
                    return jsonObject
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }
        return null
    }
    fun getNewSymbolObj(symbols: ArrayList<JSONObject>?, socketJSON: JSONObject?): JSONObject? {
        if (null == symbols || symbols.size <= 0 || null == socketJSON)
            return null

        val tick = socketJSON.optJSONObject("tick")
        if (null == tick || tick.length() <= 0)
            return null

        val channel = socketJSON.optString("channel")
        if (null == channel || !channel.contains("_")) {
            return null
        }

        var tickSymbol = channel.split("_")[1]

        for (jsonObject in symbols) {

            val symbol = jsonObject.optString("symbol")
            if (tickSymbol.equals(symbol, ignoreCase = true)) {
                try {
                    val close = tick.optString("close")
                    jsonObject.put("amount", tick.optString("amount"))
                    jsonObject.put("close", close)
                    jsonObject.put("high", tick.optString("high"))
                    jsonObject.put("low", tick.optString("low"))
                    jsonObject.put("open", tick.optString("open"))
                    jsonObject.put("rose", tick.optString("rose"))
                    jsonObject.put("vol", tick.optString("vol"))

                    var name = jsonObject?.optString("name")//NCoinManager.showAnoterName(jsonObject)
                    if(null!=name && name.contains("/")){
                        var split = name.split("/")
                        val rateResult = RateManager.getCNYByCoinName(split[1], close)
                        jsonObject.put("rateResult", rateResult)
                    }
                    return jsonObject
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }
        return null
    }

    /**
     * 合约的
     */
    fun getNewSymbolObj2(symbols: ArrayList<JSONObject>?, socketJSON: JSONObject?): JSONObject? {
        if (null == symbols || symbols.size <= 0 || null == socketJSON){
            return null
        }
        val tick = socketJSON.optJSONObject("tick")
        if (null == tick || tick.length() <= 0){
            return null
        }
        val channel = socketJSON.optString("channel")
        if (!channel.contains("_")) {
            return null
        }
        val tickSymbol = channel.split("_")[2]
        for (jsonObject in symbols) {
            val symbol = jsonObject.optString("symbol")
            LogUtil.d("我是tickSymbol=11","${tickSymbol}====${symbol}")
            if (tickSymbol.equals(symbol.replace("-",""), ignoreCase = true)) {
                LogUtil.d("我是tickSymbol=","${tickSymbol}====${symbol}")
                try {
//                    {"amount":"180897329518350.4","close":"16835.4","high":"16910","low":"16723.6",
//                        "open":"16784.1","rose":"0.0030564642","vol":"10751871175"}
                    val close = tick.optString("close")
                    jsonObject.put("amount", tick.optString("amount"))
                    jsonObject.put("close", close)
                    jsonObject.put("high", tick.optString("high"))
                    jsonObject.put("low", tick.optString("low"))
                    jsonObject.put("open", tick.optString("open"))
                    jsonObject.put("rose", tick.optString("rose"))
                    jsonObject.put("vol", tick.optString("vol"))

                    val name = jsonObject.optString("name")//NCoinManager.showAnoterName(jsonObject)
                    if(name.contains("/")){
                        val split = name.split("/")
                        val rateResult = RateManager.getCNYByCoinName(split[1], close)
                        jsonObject.put("rateResult", rateResult)
                    }
                    return jsonObject
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }
        return null
    }

    fun getNewSymbolObj(symbols: ArrayList<JSONObject>?, socketJSON: JSONObject?, historyData: Boolean): JSONObject? {
        if (null == symbols || symbols.size <= 0 || null == socketJSON)
            return null

        var tick = socketJSON.optJSONObject("tick")

        if (null == tick || tick.length() <= 0) {
            tick = JSONObject()
        }


        val channel = socketJSON.optString("channel")
        if (null == channel || !channel.contains("_")) {
            return null
        }

        var tickSymbol = channel.split("_")[1]

        for (jsonObject in symbols) {

            val symbol = jsonObject.optString("symbol")
            if (tickSymbol.equals(symbol, ignoreCase = true)) {
                try {
                    val close = tick.optString("close", "")
                    val historyList = tick.optJSONArray("historyData")

                    jsonObject.put("amount", tick.optString("amount", ""))
                    jsonObject.put("close", close)
                    jsonObject.put("high", tick.optString("high", ""))
                    jsonObject.put("low", tick.optString("low", ""))
                    jsonObject.put("open", tick.optString("open", ""))
                    jsonObject.put("rose", tick.optString("rose", ""))
                    jsonObject.put("vol", tick.optString("vol", ""))
                    if (historyData && (null == historyList || historyList.length() == 0)) {
                        jsonObject.put("historyData", socketJSON.optJSONArray("data"))
                    }

                    var name = jsonObject?.optString("name")//NCoinManager.showAnoterName(jsonObject)
                    if (name!!.contains("/")) {
                        var split = name.split("/")
                        val rateResult = RateManager.getCNYByCoinName(split[1], close)
                        jsonObject.put("rateResult", rateResult)
                    }
                    return jsonObject
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }
        return null
    }



}