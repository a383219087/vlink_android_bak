package com.yjkj.chainup.util


import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.yjkj.chainup.bean.QuotesData
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.new_version.bean.QuotesBeanTypeAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import org.json.JSONArray
import org.json.JSONException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

object JsonUtils {
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
        gsonBuilder.registerTypeAdapter(QuotesData::class.java, QuotesBeanTypeAdapter())
        gsonBuilder.setPrettyPrinting()
        gson = gsonBuilder.create()
    }


    fun convert2Quote(json: String): QuotesData {
        return gson.fromJson(json, QuotesData::class.java)
    }


    fun getCertification(context: Context?, title: String=""): Boolean {
        if (null == context)
            return false
        if (PublicInfoDataService.getInstance().isEnforceGoogleAuth(null)) {
            if (UserDataService.getInstance().nickName.isEmpty() || UserDataService.getInstance().authLevel != 1 || UserDataService.getInstance().googleStatus != 1) {
                NewDialogUtils.OTCTradingMustPermissionsDialog(context, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().nickName.isEmpty()) {
                            //认证状态 0、审核中，1、通过，2、未通过  3未认证

                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)

                        } else if (UserDataService.getInstance().authLevel != 1) {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }

                                2, 3 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                        } else {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                        }

                    }
                }, title = title)
                return false
            }
        } else {
            if (UserDataService.getInstance().nickName.isEmpty() || UserDataService.getInstance().authLevel != 1 || (UserDataService.getInstance().isOpenMobileCheck != 1 && UserDataService.getInstance().googleStatus != 1)) {
                NewDialogUtils.OTCTradingPermissionsDialog(context, object : NewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        if (UserDataService.getInstance().nickName.isEmpty()) {
                            //认证状态 0、审核中，1、通过，2、未通过  3未认证
                            //PersonalInfoActivity.enter2(mActivity)
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                        } else if (UserDataService.getInstance().authLevel != 1) {
                            when (UserDataService.getInstance().authLevel) {
                                0 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificaionSuccessActivity, null)
                                }
                                2, 3 -> {
                                    ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                                }
                            }
                        } else {
                            ArouterUtil.greenChannel(RoutePath.SafetySettingActivity, null)
                        }
                    }

                })
                return false
            }
        }
        return true
    }

    fun getLanguage(): String {
        val language = when {
            SystemUtils.isZh() -> {
                "zh_CN"
            }
            SystemUtils.isMn() -> {
                "mn_MN"
            }
            SystemUtils.isRussia() -> {
                "ru_RU"
            }
            SystemUtils.isKorea() -> {
                "ko_KR"
            }
            SystemUtils.isJapanese() -> {
                "ja_JP"
            }
            SystemUtils.isTW() -> {
                "el_GR"
            }
            SystemUtils.isVietNam() -> {
                "vi_VN"
            }
            SystemUtils.isSpanish() -> {
                "es_ES"
            }
            SystemUtils.isID() -> {
                "id_ID"
            }
            else -> {
                "en_US"
            }
        }
        return language
    }

    fun getAreaData(context: Context): JsonObject {
        val stream: InputStream = context.assets.open("area.json")
        val size = stream.available()
        val byteArray = ByteArray(size)
        stream.read(byteArray)
        stream.close()
        val json: String = String(byteArray, Charset.defaultCharset())
        val jsonObject = JsonParser().parse(json).asJsonObject

        return jsonObject

    }

}
