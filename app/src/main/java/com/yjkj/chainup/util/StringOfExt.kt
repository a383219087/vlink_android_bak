package com.yjkj.chainup.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.BarUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.common.sdk.utlis.MathHelper
import com.common.sdk.utlis.NumberUtil
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractPublicDataAgent.getContractTickers
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractTicker
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.bean.DepthBean
import com.yjkj.chainup.bean.kline.DepthItem
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil.getString
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.fragment.MarketSLTrendFragment
import com.yjkj.chainup.new_version.view.ComVerifyView
import com.yjkj.chainup.ws.WsAgentManager
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.io.EOFException
import java.io.IOException
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

fun String.numToScalePer(): String {
    return StringBuffer(this).append("%").toString()
}

fun String.totalNumToDigDown(symbol: String = "USDT"): String {
    val coinPrecision = NCoinManager.getCoinShowPrecision(symbol)
    return BigDecimalUtils.divForDown(this, coinPrecision).toPlainString()
}


fun String.verifitionType(): Int {
    return when (this) {
        "1" -> ComVerifyView.GOOGLE
        "2" -> ComVerifyView.MOBILE
        "3" -> ComVerifyView.EMAIL
        "4" -> ComVerifyView.IDCard
        else -> ComVerifyView.GOOGLE
    }
}

fun String.verfitionTypeForPhone(): Int {
    return when (this) {
        "1" -> AppConstant.MOBILE_LOGIN
        "2" -> AppConstant.MOBILE_LOGIN
        "3" -> AppConstant.EMAIL_LOGIN
        else -> AppConstant.MOBILE_LOGIN
    }
}

fun String.verfitionTypeCheck(): String {
    return when (this) {
        "1" -> "googleCode"
        "2" -> "smsCode"
        "3" -> "emailCode"
        "4" -> "idCardCode"
        else -> ""
    }
}

fun String.verfitionTypeHint(): String {
    return when (this) {
        "1" -> "common_tip_googleAuth"
        "2" -> "personal_tip_inputPhoneCode"
        "3" -> "personal_tip_inputMailCode"
        "4" -> "personal_tip_inputIdnumber"
        else -> ""
    }
}

fun Buffer.isProbablyUtf8Of(): Boolean {
    try {
        val prefix = Buffer()
        val byteCount = size.coerceAtMost(64)
        copyTo(prefix, 0, byteCount)
        for (i in 0 until 16) {
            if (prefix.exhausted()) {
                break
            }
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                return false
            }
        }
        return true
    } catch (_: EOFException) {
        return false // Truncated UTF-8 sequence.
    }
}

fun Boolean.pushOpenStatus(): String {
    return LanguageUtil.getString(ChainUpApp.appContext, when (this) {
        true -> "personal_text_safeSettingOpen"
        else -> "personal_text_safeSettingOff"
    })
}

fun Boolean.visiable(): Int {
    return when (this) {
        true -> View.VISIBLE
        else -> View.INVISIBLE
    }
}

fun Boolean.visiableOrGone(): Int {
    return when (this) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}

fun String.getHostByUrl(): String {
    val host = "https://" + this.substring(this.indexOf(".") + 1, this.length - 1)
    return host
}

fun String.getHostByPublicUrl(): String {
    val host = "https://" + this
    return host
}

fun IntArray.permissionIsGranted(): Boolean {
    if (this.isNotEmpty()) {
        this.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
    return false
}

fun DepthItem.parseDepth(): DepthItem {
    val arrays = this.buys
    for (item in arrays) {
        val bean = DepthBean()
        val array = item as ArrayList<Double>
        bean.price = array[0].toString()
        bean.vol = array[1].toString()
        bean.sum = array[2].toString()
        this.buyItem.add(bean)
    }
    val sellArrays = this.asks
    for (item in sellArrays) {
        val bean = DepthBean()
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
    if (!StringUtil.isHttpUrl(this)) {
        return this
    }
    try {
        val host = URL(this).host
        return host
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return this
}

fun String.getHostDomainByUrl(): String {
    val host = this.split(".")
    val length = host.size
    return host[length - 2] + "." + host[length - 1]
}

fun String.getHelpUrl(fileName: String): String {
    return StringBuffer(this).append("/").append(LanguageUtil.getSelectLanguage()).append("/app_operation/cms?id=${fileName}").toString()
}

fun Int.byMarketGroupTypeGetName(mContext: Context): String {
    return when (this) {
        1 -> {
            LanguageUtil.getString(mContext, "transaction_text_mainZone")
        }
        2 -> {
            LanguageUtil.getString(mContext, "market_text_innovationZone")
        }
        3 -> {
            LanguageUtil.getString(mContext, "market_text_observeZone")
        }
        0 -> {
            LanguageUtil.getString(mContext, "common_text_halveZone")
        }
        4 -> {
            LanguageUtil.getString(mContext, "market_text_unlockZone")
        }
        else -> {
            ""
        }
    }

}

fun List<ContractTicker>.getConTractType(mContext: Context): Pair<ArrayList<String>, ArrayList<Fragment>> {
    val showTitles = java.util.ArrayList<String>()
    val fragments = java.util.ArrayList<Fragment>()
    var isHasU = false
    var isHasB = false
    var isHasM = false
    for (ticker in this) {
        if (ticker.block == Contract.CONTRACT_BLOCK_USDT) {
            isHasU = true
        } else if (ticker.block == Contract.CONTRACT_BLOCK_MAIN || ticker.block == Contract.CONTRACT_BLOCK_INNOVATION) {
            isHasB = true
        } else if (ticker.block == Contract.CONTRACT_BLOCK_SIMULATION) {
            isHasM = true
        }
    }
    ///USDT
    if (isHasU) {
        showTitles.add("USDT")
        fragments.add(MarketSLTrendFragment.newInstance(0))
    }
    ///币本位
    if (isHasB) {
        showTitles.add(getString(mContext, "sl_str_inverse"))
        fragments.add(MarketSLTrendFragment.newInstance(1))
    }
    ///模拟
    if (isHasM) {
        showTitles.add(getString(mContext, "sl_str_simulation"))
        fragments.add(MarketSLTrendFragment.newInstance(2))
    }
    return Pair(showTitles, fragments)


}


fun Int.byContractGetList(): Pair<ArrayList<ContractTicker>, ArrayList<ContractTicker>> {
    val tickers: java.util.ArrayList<ContractTicker> = java.util.ArrayList()
    val localTickers: java.util.ArrayList<ContractTicker> = java.util.ArrayList()
    val data: List<ContractTicker> = getContractTickers()
    for (i in data.indices) {
        val item = data[i]
        when (this) {
            0 -> {
                if (item.block == Contract.CONTRACT_BLOCK_USDT) {
                    tickers.add(item)
                    localTickers.add(item)
                }
            }
            1 -> {
                if (item.block == Contract.CONTRACT_BLOCK_MAIN || item.block == Contract.CONTRACT_BLOCK_INNOVATION) {
                    tickers.add(item)
                    localTickers.add(item)
                }
            }
            2 -> {
                if (item.block == Contract.CONTRACT_BLOCK_SIMULATION) {
                    tickers.add(item)
                    localTickers.add(item)
                }
            }
        }

    }
    return Pair(tickers, localTickers)
}

fun ContractTicker.getClosePrice(contract: Contract): String {
    val current: Double = MathHelper.round(this.last_px, contract.price_index)
    return NumberUtil.getDecimal(contract.price_index - 1).format(current)
}

fun ContractTicker.getClosePrice(): String {
    val contract: Contract? = ContractPublicDataAgent.getContract(this.instrument_id)
    if (contract == null) {
        return ""
    } else {
        val current: Double = MathHelper.round(this.last_px, contract.price_index)
        return NumberUtil.getDecimal(contract.price_index - 1).format(current)
    }
}

fun ContractTicker.getClosePriceSort(): Double? {
    return this.getClosePrice().toDoubleOrNull()
}

fun ContractTicker.getUsdPrice(contract: Contract): String {
    return RateManager.getCNYByCoinName(contract.quote_coin, this.last_px, precision = 2)
}

fun ContractTicker.get24Vol(contract: Contract): String {
    val amount24: Double = MathHelper.round(this.qty24, contract.vol_index)
    val dfVol: DecimalFormat = NumberUtil.getDecimal(contract.vol_index)
    return NumberUtil.getBigVolum(ChainUpApp.app, dfVol, amount24, SystemUtils.isZh())
}

fun ContractTicker.getChangeRate(): Double {
    return MathHelper.round(this.change_rate.toDouble() * 100, 2)
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
    return LanguageUtil.getString(context, this)
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

fun Int.getDip(): Int {
    return DisplayUtil.dip2px(this)
}

fun String.editable(): Editable {
    return SpannableStringBuilder(this)
}

fun JSONObject.getRose(): String {
    return this.optString("rose")
}

fun JSONObject.getClose(): String {
    return this.optString("close")
}

fun JSONObject.getHigh(): String {
    return this.optString("high")
}

fun JSONObject.getLow(): String {
    return this.optString("low")
}

fun JSONObject?.getMarketName(isLevel: Boolean = false): String {
    val name = NCoinManager.showAnoterName(this)
    if (isLevel) {
        val multiple = this?.optString("multiple", "")
        if (multiple != null) {
            return "%s %sX".format(name, multiple)
        }
        return name
    }
    return name

}

fun JSONArray.getPriceTick(depthLevel: Int): String {
    return SymbolInterceptUtils.interceptData(
        this[0].toString().replace("\"", "").trim(),
        depthLevel,
        "price"
    )

}

fun String.getPriceSplitZero(): String {
    return BigDecimalUtils.subZeroAndDot(this)
}

fun JSONObject.getPriceSplitZero(): String {
    val price = BigDecimalUtils.showSNormal(this.optString("price"))
    return BigDecimalUtils.subZeroAndDot(price)
}


fun String.getFundRate(): String {
    val item = PublicInfoDataService.getInstance().getFundRateForSymbol(this)
    if (item.isNotEmpty()) {
        return StringBuffer(item).append("%").toString()
    }
    return " --"
}

fun ArrayList<String>?.getDefaultLeverCoin(): String {
    if (this == null) {
        return ""
    }
    val coin = NCoinManager.getLeverGroupList(this[0])
    if (coin.isNotEmpty()) {
        return coin.getLeverGroupSort()[0].optString("symbol", "") ?: ""
    }
    return ""
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

fun String?.getCoinToUpper(): String {
    if (this.isNullOrEmpty()) {
        return ""
    }
    return this.toUpperCase(Locale.ROOT)
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

fun String.toNewLine(): String {
    return this.replace("\\n", "\n")
}

fun String.toNewLineHtml(): String {
    return this.replace("\\n", "<br />")
}

fun JSONObject?.getMarketCoinName(): String {
    val name = this?.optString("name", "") ?: ""
    return name
}

fun JSONObject?.getMarketNameByCoinList(): String {
    val name = this?.getMarketCoinName() ?: ""
    if (name.isNotEmpty()) {
        return name.split("/")[0]
    }
    return name
}

fun String?.getTradeCoinPrice(coinMapData: JSONObject?): String {
    val priceScale = coinMapData?.optInt("price", 2) ?: 2
    val number = BigDecimalUtils.divForDown(this, priceScale)
    return number.toPlainString()
}

fun String?.getTradeCoinVolume(coinMapData: JSONObject?): String {
    val volumeScale = coinMapData?.optInt("volume", 2) ?: 2
    val number = BigDecimalUtils.divForDown(this, volumeScale)
    return number.toPlainString()
}

fun String?.getTradeCoinPriceNumber8(): String {
    val number = BigDecimalUtils.divForDown(this, 8)
    return number.toPlainString()
}

fun String.isHttpUrl(): Boolean {
    try {
        val host = URL(this).host
        return host.isNotEmpty()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

fun String.getByTypeTips(context: Context): String {
    return LanguageUtil.getString(context, when (this) {
        "search" -> {
            "common_guide_search_hint"
        }
        "notice" -> {
            "common_guide_notice_hint"
        }
        "symbolTop", "cmsAppList" -> {
            "common_guide_top_24_hint"
        }
        else -> {
            ""
        }
    })
}

fun String?.getTradeCoinBalance(coinMapData: JSONObject?): String {
    val volumeScale = coinMapData?.optInt("volume", 2) ?: 2
    val number = BigDecimalUtils.divForDown(this, volumeScale)
    return number.toPlainString()
}

fun JSONObject?.byMarketCoinList(): String {
    val channel = this?.optString("channel", "")
    return if (channel != null) channel.split("_")[1] else ""
}

fun JSONObject?.coinIsEtf(): Boolean {
    if (true) return false
    return this?.optInt("etfOpen", 0) ?: 0 == 1
}

fun JSONObject?.coinEtfTips(context: Context?): SpannableString {
    val message = LanguageUtil.getString(context, "trade_etf_tips")
    val rate = LanguageUtil.getString(context, "trade_etf_drop_rate")
    val name = this?.getMarketNameETF()
    val nikeName = "Coin" // 默认
    // 暂无接口字段
    val times = rate.format(this?.getMarketNameMultiple())
    val text = message.format(name, nikeName, times)
    val spannableString = SpannableString(text)
    spannableString.addETFSpanBean(text, name)
    spannableString.addETFSpanBean(text, nikeName)
    spannableString.addETFSpanBean(text, times)
    return spannableString
}

fun JSONObject?.getMarketNameETF(): String {
    val symbol = this?.getMarketName()
    if (symbol.isNullOrEmpty()) {
        return ""
    }
    return symbol.split("/")[0]
}

fun JSONObject?.getMarketNameMultiple(isLevel: Boolean = true): String {
    if (isLevel) {
        val multiple = this?.optString("multiple", "")
        if (multiple != null) {
            return multiple
        }
    }
    return ""
}

fun SpannableString.addETFSpanBean(text: String, itemText: String? = "") {
    val nameStart = text.indexOf(itemText!!)
    val nameEnd = itemText.length
    val blueSpan = ForegroundColorSpan(ColorUtil.getColor(R.color.main_blue))
    this.setSpan(blueSpan,
            nameStart, nameStart + nameEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
}

fun SpannableString.addETFSenSpanBean(text: String, itemText: String? = "") {
    val namefirst = text.indexOf(itemText!!)
    val nameEnd = itemText.length
    val nameStart = text.indexOf(itemText!!, namefirst + nameEnd)
    val blueSpan = ForegroundColorSpan(ColorUtil.getColor(R.color.main_blue))
    this.setSpan(blueSpan,
            nameStart, nameStart + nameEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
}

fun Collection<Any>.getKlineByType(wsIsResult: Boolean): Int {
    return when (size) {
        0 -> {
            if (WsAgentManager.instance.isConnection()) {
                if (wsIsResult) {
                    3
                } else {
                    2
                }
            } else {
                1
            }
        }
        else -> {
            0
        }
    }
}

fun ArrayList<JSONObject>.getNumByLists(context: Context?): String {
    val message = LanguageUtil.getString(context, "quant_ordering")
    if (this.isEmpty()) {
        return message
    }
    val redNum = this.size
    val temp = if (redNum > 99) "99+" else redNum
    return message + " (${temp})"
}

fun JSONObject.getQuaintStatus(context: Context): String {
    val strategy = optInt("strategyStatus")
    val shutdownMeta = optInt("shutdownMeta")
    return LanguageUtil.getString(context, when (strategy) {
        0 -> "quant_grid_order_status_start_ing"
        1 -> "quant_grid_order_status_cmd_ing"
        2 -> "quant_grid_order_status_stop_ing"
        3 -> when (shutdownMeta) {
            1 -> "quant_grid_order_status_stop_user"
            2 -> "quant_grid_order_status_stop_loss"
            3 -> "quant_grid_order_status_stop_profit"
            4 -> "quant_grid_order_status_stop_sys"
            5 -> "quant_grid_order_status_stop_user_balance"
            6 -> "quant_grid_order_status_stop_sys_fix"
            7 -> "quant_grid_order_status_stop_grid_balance"
            8 -> "quant_grid_order_status_stop_grid_interval"
            else -> "otc_have_closed"
        }
        else -> "quant_grid_order_status_other"
    })
}

fun String.getQuaintStatus(context: Context): String {
    if (this.isNotEmpty()) {
        val number = BigDecimal(this)
        val diff = BigDecimalUtils.compareTo(number, BigDecimal.ZERO)
        if (diff <= 0) {
            return this
        } else if (diff == 1) {
            return "+$this"
        }
    }
    return ""
}


fun Int.getQuaintEndStatusIsShow(context: Context): Boolean {
    return false
}

fun Int.getQuaintEndStatus(context: Context, isSell: Boolean, isBuy: Boolean, value: String): String {
    // 判断逻辑 改动两次
    val messageType = if ((this == 0 || this == 1) && !isSell) {
        "quant_order_waiting_sell"
    } else if ((this == 2 || this == 3) && !isSell) {
        "quant_order_closed_notSold"
    } else if ((this == 0 || this == 1 || this == 3) && !isBuy) {
        "quant_order_closed_notBuy"
    } else {
        ""
    }
    if (messageType.isEmpty()) {
        return value.getQuaintStatus(context)
    }
    return LanguageUtil.getString(context, messageType)
}

fun String?.getGridPrice(context: Context): String {
    if (this != null && this.isNotEmpty() && this != "0") {
        return this
    }
    return LanguageUtil.getString(context, "quant_order_setting_off")
}

fun String?.getGridCount(context: Context?): String {
    var count = 0
    if (this != null && this.isNotEmpty() && StringUtil.checkStr(this)) {
        count = this.toInt()
    }
    return "$count ${LanguageUtil.getString(context, "otc_other_times")}"
}

fun String.getGridDepthBuyScale(close: String): String {
    val l = BigDecimalUtils.div(this, close)
    val buy = BigDecimalUtils.sub(1.toString(), l.toPlainString())
    val number = BigDecimalUtils.mulStr(buy.toString(), "100", 2)
    val diff = BigDecimalUtils.compareTo(number, "0")
    if (diff <= 0) {
        return "-${number}%"
    }
    return "-${number}%"
}

fun String.getGridDepthSellScale(close: String): String {
    val l = BigDecimalUtils.div(this, close)
    val sell = BigDecimalUtils.sub(l.toPlainString(), 1.toString())
    val number = BigDecimalUtils.mulStr(sell.toString(), "100", 2)
    val diff = BigDecimalUtils.compareTo(number, "0")
    if (diff >= 0) {
        return "+${number}%"
    }
    return "${number}%"
}

fun String.getSymbolByMarketName(): String {
    val coin = this.split("/")
    if (coin.isNotEmpty()) {
        return "${coin[0]}${coin[1]}".toLowerCase()
    }
    return this
}

fun Int.getQuaintEndStatusColor(context: Context, isSell: Boolean, isRise: String = "0"): Int {
    if (StringUtil.isNumeric(isRise)) {
        return ColorUtil.getMainGridResType(isRise)
    } else {
        return when (this) {
            0, 1 -> ColorUtil.getColor(context, R.color.text_color)
            2, 3 -> ColorUtil.getColor(context, R.color.normal_text_color)
            else -> ColorUtil.getColor(context, R.color.normal_text_color)
        }
    }
}

fun JSONObject.getQuaintTime(context: Context, isHistory: Boolean): String {
    var startTime = this.optString("startTime")
    var endTime = this.optString("endTime")
    if (!isHistory || endTime == "0") {
        endTime = System.currentTimeMillis().toString()
    }
    val time = BigDecimalUtils.sub(endTime, startTime).toPlainString()
    val isStart = BigDecimalUtils.compareTo(time, "0") != 0
    if (startTime == "0" || !isStart) {
        return "0${LanguageUtil.getString(context, "noun_date_day")}0${LanguageUtil.getString(context, "noun_date_hour")}0${LanguageUtil.getString(context, "noun_date_minute")}"
    } else {
        val day: Long = time.toLong() / (1000 * 3600 * 24)
        val hour: Long = (time.toLong() - day * (1000 * 3600 * 24)) / (1000 * 3600)
        val minute: Long = (time.toLong() - day * (1000 * 3600 * 24) - hour * (1000 * 3600)) / (1000 * 60)
        return "$day${LanguageUtil.getString(context, "noun_date_day")}$hour${LanguageUtil.getString(context, "noun_date_hour")}$minute${LanguageUtil.getString(context, "noun_date_minute")}"
    }
}

fun Float.getTradeCoinPrice(priceScale: Int): String {
    val number = BigDecimalUtils.divForDown(this.toString(), priceScale)
    return number.toPlainString()
}

fun String.getTradeCoinPrice(priceScale: Int): String {
    val number = this.toFloat().getTradeCoinPrice(priceScale)
    return number
}

fun ArrayList<JSONObject>.getLinksByCompany(companyID: ArrayList<JSONObject>): ArrayList<JSONObject> {
    return ArrayList<JSONObject>(this union companyID)
}

fun String.getHostByUrlNoHttp(): String {
    return this.substring(this.indexOf(".") + 1, this.length - 1)
}

fun String.getHostByUrlDomain(): String {
    val host = "https://" + this.substring(this.indexOf(".") + 1, this.length - 1)
    return host
}

fun JSONObject.isSpeedTime(isApi: Boolean): Boolean {
    val time = if (isApi) this.optString("networkAppapi") else this.optString("networkWs")
    val error = if (isApi) this.optString("error") else this.optString("error_ws")
    if (!TextUtils.isEmpty(error) || TextUtils.isEmpty(time)) {
        return true
    }
    return false
}

fun SpannableString.addETFSpanBeanHref(text: String, itemText: String? = "", url: String?) {
    val nameStart = text.indexOf(itemText!!)
    val nameEnd = itemText.length
    val blueSpan = ColorUtil.getColor(R.color.main_blue)
    this.setSpan(object : ClickableSpan() {
        override fun onClick(p0: View) {
            ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, Bundle().apply {
                putString(ParamConstant.head_title, itemText)
                putString(ParamConstant.web_url, url)
                putInt(ParamConstant.web_type, WebTypeEnum.NORMAL_INDEX.value)
            })
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = blueSpan
            ds.isUnderlineText = true
        }
    },
            nameStart, nameStart + nameEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}

fun JSONObject?.etfItemInfo(context: Context, info: String = "etf_notes_explain_tips"): SpannableString? {
    try {
        val name = this?.optString("etfBase") + this?.optString("etfMultiple") + this?.optString("etfSide")
        val base = this?.getString("etfBase")
        val upDown = getUpdown(context, this?.getString("etfSide"), this?.getString("etfMultiple"))
        val span = LanguageUtil.getString(context, info).format(name, base, upDown)
        val spannable = SpannableString(span)
        return spannable
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null

}

fun JSONObject?.etfItemKlineInfo(context: Context): SpannableString? {
    return this.etfItemInfo(context, "etf_notes_explain")
}

fun getUpdown(context: Context, etfSide: String?, etfmultiple: String?): String {
    if (etfSide.equals("L")) {
        return LanguageUtil.getString(context, "etf_notes_multipleL").format(etfmultiple)
    } else {
        return LanguageUtil.getString(context, "etf_notes_multipleS").format(etfmultiple)
    }
}

fun getUpDownKline(context: Context, etfSide: String?): String {
    if (etfSide.equals("L")) {
        return LanguageUtil.getString(context, "etf_notes_title_action_long")
    } else {
        return LanguageUtil.getString(context, "etf_notes_title_action_short")
    }
}

fun JSONObject?.etfItemKlineShowName(): String {
    val showName = NCoinManager.showAnoterName(this)
    if (StringUtil.checkStr(showName) && showName.contains("/")) {
        val split = showName.split("/")
        return split[0]
    }
    return showName
}

fun JSONObject?.etfItemKlineShowTitle(context: Context): String {
    val name = this.etfItemKlineShowName()
    val base = this?.optString("etfBase")
    val mul = this?.optString("etfMultiple")
    val coin = this?.optString("etfSide")
    val baseMul = LanguageUtil.getString(context, "etf_notes_title_mul").format(mul)
    val nameTips = "%s%s%s".format(baseMul, getUpDownKline(context, coin), base)
    return "$name(${nameTips})"
}

fun JSONArray.toList(): ArrayList<JSONObject> {
    val items = arrayListOf<JSONObject>()
    for (i in 0 until this.length()) {
        items.add(this.optJSONObject(i))
    }
    return items
}

fun SpannableString.addETFSenSpanBean(text: String, itemText: String = "", textColor: Int =R.color.main_blue ) {
    val nameStart = text.indexOf(itemText)
    val nameEnd = itemText.length
    val blueSpan = ForegroundColorSpan(ColorUtil.getColor(textColor))
    this.setSpan(blueSpan,
            nameStart, nameStart + nameEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
}