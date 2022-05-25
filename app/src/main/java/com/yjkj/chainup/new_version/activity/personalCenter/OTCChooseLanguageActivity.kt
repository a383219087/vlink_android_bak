package com.yjkj.chainup.new_version.activity.personalCenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import com.chainup.contract.utils.CpLocalManageUtil
import com.contract.sdk.ContractSDKAgent
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.NetworkLanguage
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.SymbolManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.NewMainActivity
import com.yjkj.chainup.new_version.adapter.OTCChangeLanguageAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.LocalManageUtil
import com.yjkj.chainup.util.SystemUtils
import com.yjkj.chainup.util.Utils
import kotlinx.android.synthetic.main.activity_otc_choose_countries.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2018/10/13-下午5:37
 * @Email buptjinlong@163.com
 * @description 选择国家
 */
class OTCChooseLanguageActivity : NewBaseActivity() {

    var adapter: OTCChangeLanguageAdapter? = null

    companion object {
        fun newIntent(context: Context) {
            context.startActivity(Intent(context, OTCChooseLanguageActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otc_choose_countries)
        title_layout?.setContentTitle(LanguageUtil.getString(this, "customSetting_action_language"))
        initView()
        setSelcetOnclick()
    }

    var launch: JSONObject? = null
    var lauList: ArrayList<JSONObject>? = arrayListOf()
    var saveLanguage: String = ""
    fun initView() {
        launch = PublicInfoDataService.getInstance().getLan(null)
        saveLanguage = SymbolManager.instance.getOTCLanguage().toString()
        lauList = PublicInfoDataService.getInstance().lanList
        if (lauList == null) return

        if (TextUtils.isEmpty(saveLanguage)) {
            for (i in 0 until lauList?.size!!) {
                lauList!![i].putOpt("open", (LanguageUtil.getSelectLanguage() == lauList!![i].optString("id")))
            }
        } else {
            for (i in 0 until lauList?.size!!) {
                lauList!![i].putOpt("open", (saveLanguage == lauList!![i].optString("id")))
            }
        }

        rv_location.layoutManager = LinearLayoutManager(this)
        adapter = OTCChangeLanguageAdapter(lauList!!)

        adapter?.setEmptyView(EmptyForAdapterView(this))
        rv_location.adapter = adapter


    }

    fun setSelcetOnclick() {
        /**
         * 点击语言
         */
        adapter?.setOnItemClickListener { adapter, view, position ->
            launch?.putOpt("defLan", lauList?.get(position)?.optString("id")!!)
            SymbolManager.instance.saveOTCLanguage(lauList?.get(position)?.optString("id")!!)
            setLanguageStatus(lauList?.get(position)?.optString("id") ?: "")

        }

    }

    fun setLanguageStatus(lan: String) {
        var select = getNetString(lan)
        CpLocalManageUtil.saveSelectLanguage(this, lan)
        if (!select) {
            setLanguageDefault(lan)
        }

    }

    /**
     * 默认设置语言
     */
    fun setLanguageDefault(lan: String) {
        when (lan) {
            "zh_CN" -> {
                selectLanguage("zh_CN")
            }
            "zh" -> {
                selectLanguage("zh")
            }
            "en_US" -> {
                selectLanguage("en_US")
            }
            "ko_KR" -> {
                selectLanguage("ko_KR")
            }
            "el_GR" -> {
                selectLanguage("el_GR")
            }
            "mn_MN" -> {
                selectLanguage("mn_MN")
            }
            "ja_JP" -> {
                selectLanguage("ja_JP")
            }
            "ru_RU" -> {
                selectLanguage("ru_RU")
            }
            "vi_VN" -> {
                selectLanguage("vi_VN")
            }
            "es_ES" -> {
                selectLanguage("es_ES")
            }
            "id_ID" -> {
                selectLanguage("id_ID")
            }
            else -> {
                selectLanguage("en_US")
            }
        }
    }


    /**
     * TODO 具体实现
     */
    private fun getNetString(key: String): Boolean {
        var locales = PublicInfoDataService.getInstance().getLocalesList(null)
        if (locales == null) return false
        var url = locales.optString(key) ?: ""
        if (TextUtils.isEmpty(url)) {
            return false
        }
        var jsonObject: JSONObject? = PublicInfoDataService.getInstance().onlineText
        if (jsonObject == null) {
            downLoadLan(url, key)
        } else {
            var lan = jsonObject.optString(key, "") ?: ""
            if (TextUtils.isEmpty(lan)) {
                downLoadLan(url, key)
            } else {
                selectLanguage(key)
            }
        }
        return true
    }

    fun downLoadLan(url: String, key: String) {
        Thread(Runnable {
            try {
                var jsonFile = Utils.getJSONLastNews(url)
                PublicInfoDataService.getInstance().saveOnlineText(jsonFile)
                selectLanguage(key)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }


    fun selectLanguage(select: String) {
        LocalManageUtil.saveSelectLanguage(this, select)
        try {
            val isZhEnv = SystemUtils.isZh()
            //通知合约SDK语言环境
            ContractSDKAgent.isZhEnv = isZhEnv
        } catch (e: Exception) {
            e.printStackTrace()
        }
        NetworkLanguage().cleanLanguage()
        reStart(this)
    }

    fun reStart(context: Context) {
        finish()
        val intent = Intent(context, NewMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        HttpClient.instance.refresh()
        PublicInfoDataService.getInstance().saveData(JSONObject())
    }

}