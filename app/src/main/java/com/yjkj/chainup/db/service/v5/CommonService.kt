package com.yjkj.chainup.db.service.v5

import com.yjkj.chainup.db.MMKVDb
import org.json.JSONObject
import java.lang.Exception


class CommonService private constructor() {
    private val mmkvDb: MMKVDb?

    companion object {
        var commonService: CommonService? = null
        const val HOME_DATA = "home_data" // 24H的行情
        val instance: CommonService
            get() {
                if (commonService == null)
                    commonService = CommonService()
                return commonService!!
            }
    }

    init {
        mmkvDb = MMKVDb()
    }

    private fun setDataByKey(key: String, data: String?) {
        if (null != data) {
            mmkvDb?.saveData(key, data)
        }
    }

    private fun getDataByKey(key: String): String? {
        return mmkvDb?.getData(key)
    }

    fun saveHomeData(jsonArray: JSONObject) {
        jsonArray.remove("home_recommend_list")
        var cmsSymbolList = jsonArray.optJSONArray("header_symbol")
        if (cmsSymbolList != null) {
            for (item in 0..cmsSymbolList.length()) {
                val symbolItem = cmsSymbolList.optJSONObject(item)
                try {
                 symbolItem.remove("close")
                    symbolItem.remove("rose")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        setDataByKey(HOME_DATA, jsonArray.toString())
    }

    fun getHomeData(): JSONObject? {
        val data = getDataByKey(HOME_DATA)
        if (data.isNullOrEmpty()) {
            return null
        }
        return JSONObject(data)
    }

}