package com.chainup.contract.utils


import android.text.TextUtils
import com.chainup.contract.bean.CpQuotesBeanTypeAdapter
import com.chainup.contract.bean.CpQuotesData
import com.chainup.contract.bean.KlineQuotesData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import java.util.*

object CpJsonUtils {
    lateinit var gson: Gson

    fun <T> jsonToList(data: String, tClass: Class<T>): List<T> {
        val mList = ArrayList<T>()
        if (TextUtils.isEmpty(data)) return mList
        try {
            val mArray = JSONArray(data)
            (0 until mArray.length()).mapTo(mList) { jsonToBean(mArray.get(it).toString(), tClass) }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return mList
    }

    fun <T> jsonToBean(data: String, tClass: Class<T>): T = Gson().fromJson(data, tClass)


    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(CpQuotesData::class.java, CpQuotesBeanTypeAdapter())
        gsonBuilder.setPrettyPrinting()
        gson = gsonBuilder.create()
    }


    fun convert2Quote(json: String): KlineQuotesData {
//        return gson.fromJson(json, CpQuotesData::class.java)
        return gson.fromJson(json, KlineQuotesData::class.java)
    }

    fun getLanguage(): String {
        val language = if (CpSystemUtils.isZh()) {
            "zh_CN"
        } else if (CpSystemUtils.isMn()) {
            "mn_MN"
        } else if (CpSystemUtils.isRussia()) {
            "ru_RU"
        } else if (CpSystemUtils.isKorea()) {
            "ko_KR"
        } else if (CpSystemUtils.isJapanese()) {
            "ja_JP"
        } else if (CpSystemUtils.isTW()) {
            "el_GR"
        } else if (CpSystemUtils.isVietNam()) {
            "vi_VN"
        } else if (CpSystemUtils.isSpanish()) {
            "es_ES"
        } else {
            "en_US"
        }
        return language
    }


}
