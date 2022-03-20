package com.chainup.contract.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import androidx.annotation.IdRes
import com.blankj.utilcode.util.BarUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.bean.CpDepthBean
import com.yjkj.chainup.bean.kline.cp.DepthItem
import com.yjkj.chainup.manager.CpLanguageUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

fun String.numToScalePer(): String {
    return StringBuffer(this).append("%").toString()
}


fun DepthItem.parseDepth(): DepthItem {
    val arrays = this.buys
    for (item in arrays) {
        val bean = CpDepthBean()
        val array = item as ArrayList<Double>
        bean.price = array[0].toString()
        bean.vol = array[1].toString()
        bean.sum = array[2].toString()
        this.buyItem.add(bean)
    }
    val sellArrays = this.asks
    for (item in sellArrays) {
        val bean = CpDepthBean()
        val array = item as ArrayList<Double>
        bean.price = array[0].toString()
        bean.vol = array[1].toString()
        bean.sum = array[2].toString()
        this.sellItem.add(bean)
    }
    return this
}

fun String.toTextPrice(scale: Int = 0): String {
    try {
        return BigDecimal(this).setScale(scale, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "0.0"
}

fun String.getDoMainByUrl(): String {
    val host = URL(this).host
    return host
}

fun String.getHostDomainByUrl(): String {
    val host = this.split(".")
    val length = host.size
    return host[length - 2] + "." + host[length - 1]
}

fun String.getHelpUrl(fileName: String): String {
    return StringBuffer(this).append("/").append(CpLanguageUtil.getSelectLanguage()).append("/app_operation/cms?id=${fileName}").toString()
}

fun Int.byMarketGroupTypeGetName(mContext: Context): String {
    return when (this) {
        1 -> {
            CpLanguageUtil.getString(mContext, "transaction_text_mainZone")
        }
        2 -> {
            CpLanguageUtil.getString(mContext, "market_text_innovationZone")
        }
        3 -> {
            CpLanguageUtil.getString(mContext, "market_text_observeZone")
        }
        0 -> {
            CpLanguageUtil.getString(mContext, "common_text_halveZone")
        }
        4 -> {
            CpLanguageUtil.getString(mContext, "market_text_unlockZone")
        }
        else -> {
            ""
        }
    }

}

fun String.getSymbolChannel(): String {
    return StringBuilder("market_${this.toLowerCase()}_ticker").toString()
}

fun ArrayList<JSONObject>.getSymbols(): String {
    val builder = StringBuilder()
    this.forEach {
        builder.append("%s,".format(it.optString("symbol")))
    }
    return builder.substring(0, builder.length - 1)
}

fun HashMap<String, Boolean>.getSymbols(): String {
    val builder = StringBuilder()
    this.forEach {
        builder.append("%s,".format(it.key))
    }
    return builder.substring(0, builder.length - 1)
}

fun Set<String>.getArraysSymbols(): String {
    val builder = StringBuilder()
    this.forEach {
        builder.append("%s,".format(it))
    }
    return builder.substring(0, builder.length - 1)
}

fun String.getArraysSymbols(context: Context): String {
    return when (this) {
        "line" -> "kline_Line".getKlineScale(context)
        "1min" -> "kline_1min".getKlineScale(context)
        "15min" -> "kline_15min".getKlineScale(context)
        "60min" -> "kline_60min".getKlineScale(context)
        "1day" -> "kline_1day".getKlineScale(context)
        "1week" -> "kline_1week".getKlineScale(context)
        "1month" -> "kline_1month".getKlineScale(context)
        "5min" -> "kline_5min".getKlineScale(context)
        "30min" -> "kline_30min".getKlineScale(context)
        "4h" -> "kline_4h".getKlineScale(context)
        else -> this
    }
}

fun String.getKlineScale(context: Context): String {
    return CpLanguageUtil.getString(context,this)
}

fun BaseViewHolder.setGoneV3(@IdRes viewId: Int, isGone: Boolean): BaseViewHolder {
    return this.setGone(viewId, !isGone)
}

fun ArrayList<JSONObject>.getLeverGroupSort(): ArrayList<JSONObject> {
    this.apply {
        sortBy { it.optInt("sort") }
        sortBy { it.optInt("newcoinFlag") }
    }
    return this
}

fun ArrayList<JSONObject>.getCoinGroupSort(): ArrayList<JSONObject> {
    this.apply {
        sortBy { it.optInt("sort") }
        sortBy { it.optInt("newcoinFlag") }
    }
    return this
}

fun String.getImageBitmap(): Bitmap? {
    val myFileUrl: URL
    var bitmap: Bitmap? = null
    try {
        myFileUrl = URL(this)
        val conn: HttpURLConnection = myFileUrl.openConnection() as HttpURLConnection
        conn.doInput = true
        conn.connect()
        val `is` = conn.inputStream
        bitmap = BitmapFactory.decodeStream(`is`)
    } catch (e: MalformedURLException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bitmap
}

operator fun JSONArray.iterator(): Iterator<String> = (0 until length()).asSequence().map { get(it) as String }.iterator()

fun String.getCoinByContractFirst(): String {
    if (this.isEmpty()) {
        return ""
    }
    val coinListTemp = JSONArray(this)
    if (coinListTemp.length() != 0) {
        return coinListTemp.get(0) as String
    }
    return ""
}


fun Float.toPx(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

fun Float.toDp(): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (this / scale + 0.5f).toInt()
}

fun Int.toPx() = this.toFloat().toPx()

fun String.toViewDp(): Int {
    return if (this.isNotEmpty() && this.length > 20) 60.toPx() + BarUtils.getStatusBarHeight() else 44.toPx() + BarUtils.getStatusBarHeight()
}

fun Boolean.visiableOrGone(): Int {
    return when (this) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}

fun String.getPriceSplitZero(): String {
    return CpBigDecimalUtils.subZeroAndDot(this)
}

fun JSONObject.getPriceSplitZero(): String {
    val price = CpBigDecimalUtils.showSNormal(this.optString("price"))
    return CpBigDecimalUtils.subZeroAndDot(price)
}


