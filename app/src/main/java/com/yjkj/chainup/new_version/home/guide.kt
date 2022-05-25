package com.yjkj.chainup.new_version.home

import android.app.Activity
import android.os.Handler
import android.view.View
import com.binioter.guideview.GuideBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.gson.annotations.SerializedName
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.MMKVDb
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.new_version.dialog.DialogUtil
import com.yjkj.chainup.new_version.home.guide.SimpleComponent
import com.yjkj.chainup.new_version.home.guide.ToastAssetComponent
import com.yjkj.chainup.new_version.home.guide.ToastComponent
import com.yjkj.chainup.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.io.File
import java.util.*


/**
 *  首页引导流程
 */

fun showGuideHomepage(context: Activity?, array: Array<View>, data: JSONObject?) {
    // 默认处理首页数据  顶部搜索
    //  根据首页接口配置接口 来处理引导页数量
    //  最后保留状态 保重只提示一次
    LogUtil.e(TAG_ADVERT, "showGuideHomepage dialogType ${dialogType}")
    if (dialogType != 0) {
        return
    }
    val isShow = AdvertDataService.instance.getSkipAdvert(AdvertDataService.KEY_GUIDE_HOME_SKIP)
    LogUtil.e(TAG_ADVERT, "showGuideHomepage isShow ${isShow}")
    if (isShow) {
        return
    }
    dialogType = 4
    val tempMapArray = arrayListOf("search")
    val tempArray = arrayListOf(array[0])
    if (data != null) {
        val cmsSymbolList = data.optJSONArray("header_symbol")
        val noticeInfoList = data.optJSONArray("noticeInfoList")
        val cmsAppDataList = data.optJSONArray("cmsAppDataList")
        if (noticeInfoList != null && noticeInfoList.length() > 0) {
            tempArray.add(array[1])
            tempMapArray.add("notice")
        }
        if (cmsSymbolList != null && cmsSymbolList.length() > 3) {
            tempArray.add(array[2])
            tempMapArray.add("symbolTop")
        }
        if (cmsAppDataList != null && cmsAppDataList.length() > 8) {
            tempArray.add(array[3])
            tempMapArray.add("cmsAppList")
        }
        showGuideHome(context, 0, tempArray, tempMapArray)
    }
}

fun showGuideHome(context: Activity?, index: Int = 0, tempArray: ArrayList<View>, tempMapArray: ArrayList<String>) {
    val count = tempMapArray.size
    homeSearch(context, tempArray[index], index, tempArray.size, tempMapArray[index], getGuideChange(context, index, count, tempArray, tempMapArray))
}

fun getGuideChange(context: Activity?, index: Int, count: Int, array: ArrayList<View>, tempArray: ArrayList<String>) = object : GuideBuilder.OnVisibilityChangedListener {
    override fun onShown() {

    }

    override fun onDismiss() {
        val isSkip = AdvertDataService.instance.getSkipAdvert(AdvertDataService.KEY_GUIDE_HOME_SKIP)
        if (!isSkip) {
            // 没有点击跳过就继续跳出
            if (index + 1 <= count - 1) {
                // 弹出下一个
                val nextIndex = index + 1
                showGuideHome(context, nextIndex, array, tempArray)
            } else {
                AdvertDataService.instance.saveSkipGuide(AdvertDataService.KEY_GUIDE_HOME_SKIP)
                dialogType = 0
            }
        } else {
            AdvertDataService.instance.saveSkipGuide(AdvertDataService.KEY_GUIDE_HOME_SKIP)
            dialogType = 0
        }
    }
}

const val alpha = 150
var highCorner = DisplayUtil.dip2px(4)
const val highPadding = 10
var dialogType = 0 // 1 升级 2 // 广告 3  用户协议  4 新手引导
const val highItemPadding = 20

const val TAG_ADVERT = "advertLog"

interface GuideListener {
    fun onDismiss()
}

fun homeSearch(context: Activity?, view: View, position: Int, count: Int, type: String, guideListener: GuideBuilder.OnVisibilityChangedListener) {
    val padding = when (type) {
        "notice" -> {
            highItemPadding
        }
        "symbolTop", "cmsAppList" -> {
            0
        }
        else -> {
            highPadding
        }
    }
    val corner = when (type) {
        "symbolTop", "cmsAppList" -> {
            0
        }
        else -> {
            highCorner
        }
    }
    LogUtil.e(TAG_ADVERT, "homeSearch ${view.windowVisibility}")
    val builder = GuideBuilder()
    builder.setTargetView(view)
            .setAlpha(alpha)
            .setHighTargetCorner(corner)
            .setHighTargetPadding(padding)
            .setOutsideTouchable(false)
    builder.setOnVisibilityChangedListener(guideListener)
    val component = SimpleComponent(context,position, count, type)
    builder.addComponent(component)
    val guide = builder.createGuide()
    component.guideListener = object : GuideListener {
        override fun onDismiss() {
            guide.dismiss()
        }
    }
    guide.show(context)


}

fun getGuideChangeToast(view: View) = object : GuideBuilder.OnVisibilityChangedListener {
    override fun onShown() {}
    override fun onDismiss() {

    }
}

fun homeMarketEdit(context: Activity?, view: View) {
    if (AdvertDataService.instance.getSkipAdvert(AdvertDataService.KEY_GUIDE_MARKET)) {
        return
    }
    homeToast(context, view, "common_guide_coin_edit_hint", true)
    AdvertDataService.instance.saveSkipGuide(AdvertDataService.KEY_GUIDE_MARKET)
}

fun homeMarketRecommend(context: Activity?, view: View) {
    if (AdvertDataService.instance.getSkipAdvert(AdvertDataService.KEY_GUIDE_SEARCH)) {
        return
    }
    homeToast(context, view, "common_guide_coin_recommend_hint", true)
    AdvertDataService.instance.saveSkipGuide(AdvertDataService.KEY_GUIDE_SEARCH)
}

fun homeAssetPieChart(context: Activity?, view: View) {
    if (AdvertDataService.instance.getSkipAdvert(AdvertDataService.KEY_GUIDE_ASSET)) {
        return
    }
    homeToast(context, view, "common_guide_asset_hint", false)
    AdvertDataService.instance.saveSkipGuide(AdvertDataService.KEY_GUIDE_ASSET)
}

fun homeToast(context: Activity?, view: View, message: String, isLeft: Boolean) {
    val builder = GuideBuilder()
    val padding = when (message) {
        "common_guide_coin_recommend_hint" -> {
            0
        }
        else -> {
            highPadding
        }
    }
    val corner = when (message) {
        "common_guide_coin_recommend_hint" -> {
            0
        }
        else -> {
            highCorner
        }
    }
    builder.setTargetView(view)
            .setAlpha(alpha)
            .setHighTargetCorner(corner)
            .setHighTargetPadding(padding)
            .setOutsideTouchable(false)
    builder.setOnVisibilityChangedListener(getGuideChangeToast(view))
    val component = if (isLeft) ToastComponent(LanguageUtil.getString(context, message), message) else ToastAssetComponent(
        LanguageUtil.getString(context, message), isLeft)
    builder.addComponent(component)
    val guide = builder.createGuide()
    if (component is ToastComponent) {
        component.guideListener = object : GuideListener {
            override fun onDismiss() {
                guide.dismiss()
            }
        }
    } else if (component is ToastAssetComponent) {
        component.guideListener = object : GuideListener {
            override fun onDismiss() {
                guide.dismiss()
            }
        }
    }
    Handler().postDelayed({
        guide.show(context)
    }, 600)

}

fun homeAdvert(context: Activity?) {
    LogUtil.e(TAG_ADVERT, "homeAdvert ${dialogType}")
    Handler().postDelayed({
        if (dialogType != 0) {
            return@postDelayed
        }
        dialogType = 2
        val advert = AdvertDataService.instance.getAdvert()
        if (advert != null && advert.isDayAdvert()) {
            // 根据启动类型 判断是否提示 每日提示 每次打开提示
            val time = AdvertDataService.instance.getAdvertTime()
            val zero: Long = -1
            val isShow = advert.isDayShow(time)
            if (isShow) {
                AdvertDataService.instance.saveAdvertTime()
                DialogUtil.showAdvertDialog(context!!, advert)
            } else {
                dialogType = 0
            }
        } else {
            dialogType = 0
        }
    }, 600)
}

data class AdvertModel(
        @SerializedName("picturePath") val picturePath: String = "", //下载地址
        @SerializedName("isLogin") val isLogin: Int = -1, // 展示类型
        @SerializedName("id") val id: Int = -1, // 广告ID
        @SerializedName("startTime") val startTime: Long = -1, // 开始时间
        @SerializedName("endTime") val endTime: Long = -1, // 发布时间
        @SerializedName("pictureUrl") val pictureUrl: String = "",// 跳转链接
        @SerializedName("activityTitle") val activityTitle: String? = "" //下载地址
) {
    var localFile: String = ""

    /**
     * 是否每天显示一次
     */
    fun isDayShow(time: Long): Boolean {
        return when (isLogin) {
            0 -> {
                true
            }
            1 -> {
                UserDataService.getInstance().isLogined
            }
            2 -> {
                val show = !DateUtils.isSameDay(time, Date().time)
                show && UserDataService.getInstance().isLogined
            }
            3 -> {
                val show = !DateUtils.isSameDay(time, Date().time)
                show
            }
            else ->
                false
        }
    }

    /**
     * 效验是否正常的url地址
     */
    fun isImageDownloadUrl(): Boolean {
        if (picturePath.isNotEmpty())
            return picturePath.isHttpUrl()
        return false
    }

    fun imageDownloadUrl(): String {
        return picturePath
    }

    fun imageHttpUrl(): String {
        if (pictureUrl.isNotEmpty()) {
            return pictureUrl
        }
        return "about:blank"
    }

    fun title(): String {
        if (activityTitle != null) {
            return pictureUrl
        }
        return ""
    }

    /**
     * 是否满足日期要求
     */
    fun isDayAdvert(): Boolean {
        return DateUtils.dayIsRegion(startTime, endTime, Date().time)
    }

    fun toJson(): String {
        return JsonUtils.gson.toJson(this)
    }

}

class AdvertDataService private constructor() {
    var mMMKVDb: MMKVDb = MMKVDb()

    companion object {
        private const val TAG = "PublicInfoDataService"
        private const val key = "advertJson"
        private const val key_advert_time = "advertTime"
        const val KEY_GUIDE_HOME_SKIP = "guideHomeSkip"

        const val KEY_GUIDE_ASSET = "guideAsset"
        const val KEY_GUIDE_MARKET = "guideMarket"
        const val KEY_GUIDE_SEARCH = "guideSearch"
        private var mPublicInfoDataService: AdvertDataService? = null
        val instance: AdvertDataService
            get() {
                if (null == mPublicInfoDataService) {
                    mPublicInfoDataService = AdvertDataService()
                }
                return mPublicInfoDataService!!
            }
    }

    fun showAdvert() {
        // 获取当前有没有可展示的广告

    }

    fun getAdvertAndCacheLocal(advert: AdvertModel) {
        if (advert.isImageDownloadUrl()) {
            // 归档数据
            LogUtil.e(TAG_ADVERT, "开始下载广告 ${advert.id}")
            val imageUrl = advert.imageDownloadUrl()
            saveDownloadImage(imageUrl).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        advert.localFile = it
                        saveAdvert(advert)
                    }, {

                    })
        } else {
            LogUtil.e(TAG_ADVERT, "广告已经存在 ${advert.id}")
        }
    }

    private fun saveDownloadImage(imageUrl: String): Observable<String> {
        val mContext = ChainUpApp.app
        LogUtil.e(TAG_ADVERT, "开始下载广告 ${imageUrl}")
        return Observable.just(imageUrl).map {
            val file = Glide.with(mContext)
                    .load(imageUrl)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get()
            val chche: File = mContext.getCacheDir()
            //第二个参数为你想要保存的目录名称
            val appDir = File(chche, "advert")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + ".jpg"
            val destFile = File(appDir, fileName)
            //把gilde下载得到图片复制到定义好的目录中去
            FileUtils.copy(file, destFile)
            LogUtil.e(TAG_ADVERT, "下载成功 " + destFile.path)
            destFile.path
        }.compose(RxUtil.applySchedulersToObservable())

    }

    private fun saveAdvert(advert: AdvertModel) {
        clearAdvert()
        mMMKVDb.saveData(key, advert.toJson())
    }

    fun clearAdvert() {
        mMMKVDb.removeValueForKey(key)
    }

    fun getAdvert(): AdvertModel? {
        val json = mMMKVDb.getData(key)
        if (json.isNotEmpty()) {
            return JsonUtils.gson.fromJson(mMMKVDb.getData(key), AdvertModel::class.java)
        }
        return null
    }

    fun saveAdvertTime() {
        clearAdvertTime()
        mMMKVDb.saveLongData(key_advert_time, Date().time)
    }

    fun clearAdvertTime() {
        mMMKVDb.removeValueForKey(key_advert_time)
    }

    fun getAdvertTime(): Long {
        return mMMKVDb.getLongData(key_advert_time, -1)
    }

    fun saveSkipGuide(key: String) {
        clearSkipAdvert(key)
        mMMKVDb.saveBooleanData(key, true)
    }

    fun clearSkipAdvert(key: String) {
        mMMKVDb.removeValueForKey(key)
    }

    fun getSkipAdvert(key: String): Boolean {
        return mMMKVDb.getBooleanData(key, false)
    }

    /**
     * 广告是否存在本地
     */
    fun advertIsLocal(advert: AdvertModel): Boolean {
        if (advert.id == getAdvert()?.id) {
            return true
        }
        return false
    }

}
