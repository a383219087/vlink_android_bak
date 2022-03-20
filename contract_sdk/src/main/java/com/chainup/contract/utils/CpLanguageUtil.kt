package com.yjkj.chainup.manager

import android.content.Context
import android.content.res.Resources
import android.text.TextUtils
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.utils.CpPreferenceManager
import com.chainup.contract.utils.CpSystemUtils
import java.util.*

/**
 * @Author: Bertking
 * @Date：2019-07-18-18:59
 * @Description:
 */
object CpLanguageUtil {

    private val TAG = CpLanguageUtil::class.java.simpleName

    private val SELECTED_LANGUAGE = "language_select"


    var systemCurrentLocal = Locale.getDefault()


    fun saveLanguage(currentLan: String) {
        CpPreferenceManager.getInstance(CpMyApp.Companion.instance()).putSharedString(CpPreferenceManager.PREF_LANGUAGE, currentLan);
    }


    @JvmStatic
    fun getSelectLanguage(): String {
//        var select = mmkv.decodeString(SELECTED_LANGUAGE, "") ?: ""
//
        var select = CpPreferenceManager.getInstance(CpMyApp.Companion.instance())
                .getSharedString(CpPreferenceManager.PREF_LANGUAGE, "");
        if (TextUtils.isEmpty(select)) {
            select = "zh_CN"
        }

//        if (systemCurrentLocal.language.contains("zh")) {
//            return "zh_CN"
//        }
//        if (systemCurrentLocal.language.contains("en")) {
//            return "en_US"
//        }
//
//        if (systemCurrentLocal.language.contains("ko")) {
//            return mmkv.decodeString(SELECTED_LANGUAGE, "ko_KR")
//        }
//
//        if (systemCurrentLocal.language.contains("ja")) {
//            return mmkv.decodeString(SELECTED_LANGUAGE, "ja_JP")
//        }
//        if (!TextUtils.isEmpty(select)) return select
//
//        var lan: String? = ""
//        val languageBean = PublicInfoDataService.getInstance().getLan(null)
//        if (languageBean != null) {
//            lan = languageBean.optString("defLan")
//        }
//
//        if (TextUtils.isEmpty(lan)) {
//            return mmkv.decodeString(SELECTED_LANGUAGE, "en_US")
//        }

        return select
    }

    /**
     * 获取多语言文案
     */
    @JvmStatic
    fun getString(context: Context?, key: String): String {
        return getLocalString(context, key)
    }

    private fun getLocalString(context: Context?, key: String): String {
        return try {
            var id = context?.resources?.getIdentifier(key, "string", CpMyApp.instance().packageName)
                    ?: 0
            if (context == null) {
                CpMyApp.instance().getString(id)
            } else {
                context.getString(id)
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            /**
             * 若找不到直接将key显示出来
             */
            key
        }
    }

    @JvmStatic
    fun getLanguage(): String? {
        var language = "en_US"
        if (CpSystemUtils.isZh()) {
            language = "zh_CN"
        } else if (CpSystemUtils.isMn()) {
            language = "mn_MN"
        } else if (CpSystemUtils.isRussia()) {
            language = "ru_RU"
        } else if (CpSystemUtils.isKorea()) {
            language = "ko_KR"
        } else if (CpSystemUtils.isJapanese()) {
            language = "ja_JP"
        } else if (CpSystemUtils.isTW()) {
            language = "el_GR"
        } else if (CpSystemUtils.isVietNam()) {
            language = "vi_VN"
        } else if (CpSystemUtils.isSpanish()) {
            language = "es_ES"
        }
        return language
    }


}