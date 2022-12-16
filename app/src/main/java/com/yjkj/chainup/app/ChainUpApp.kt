package com.yjkj.chainup.app

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.Process
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import com.bilibili.boxing.BoxingCrop
import com.bilibili.boxing.BoxingMediaLoader
import com.blankj.utilcode.util.SPUtils
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.net.CpHttpHelper
import com.chainup.contract.utils.CpLocalManageUtil
import com.chainup.contract.ws.CpWsContractAgentManager
import com.contract.sdk.ContractSDKAgent
import com.igexin.sdk.PushManager
import com.yjkj.chainup.BuildConfig
import com.yjkj.chainup.contract.utils.UtilSystem
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.push.DemoPushService
import com.yjkj.chainup.extra_service.push.HandlePushIntentService
import com.yjkj.chainup.manager.DataInitService
import com.yjkj.chainup.model.api.HttpResultUrlData
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.new_version.activity.asset.BoxingGlideLoader
import com.yjkj.chainup.new_version.activity.asset.BoxingUcrop
import com.yjkj.chainup.new_version.view.ForegroundCallbacksObserver
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.ForegroundCallbacks
import com.yjkj.chainup.ws.WsAgentManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * @Author: Bertking
 * @Date：2019/3/6-10:52 AM
 * @Description:
 */
class ChainUpApp : CpMyApp() {


    private var appStateChangeListener: AppStateChangeListener? = null
    private var currentState: Int = 0


    companion object {
        lateinit var appContext: Context
        lateinit var app: Application
         var url: HttpResultUrlData?=HttpResultUrlData(
             baseUrl = "http://8.219.93.19:8081/base/appapi",
             contractSocketAddress = "ws://8.219.93.19:8081/contract/kline/ws",
             contractUrl = "http://8.219.93.19:8081/contract/appapi",
             httpHostUrlContractV2 = "http://8.219.93.19:8081/contract/appapi",
             otcBaseUrl = "http://8.219.93.19:8081/otc/appapi",
             otcSocketAddress = "ws://8.219.93.19:8081/otc/chat/ws",
             redPackageUrl = "https://dev5redpacket.chaindown.com/app-redPacket-api/",
             socketAddress = "ws://8.219.93.19:8081/base/kline/ws",
             wssHostContractV2 = "ws://8.219.93.19:8081/contract/kline/ws",
             optionUrl = "",
             blocksUrl = "",
             chatUrl = "",
             chatApiUrl = ""
         )
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        if (BuildConfig.APPLICATION_ID == getCurProcessName(this)) {
            appContext = this
            appStateChangeListener = getAppStateChangeListener()
            registerActivityLifecycleCallbacks(this)
            CommonComponent.getInstance().init(app)
            setCurrentTheme()
            initAppStatusListener()


            Debug.stopMethodTracing()
            BoxingMediaLoader.getInstance().init(BoxingGlideLoader()) // 需要实现IBoxingMediaLoader
            BoxingCrop.getInstance().init(BoxingUcrop())
            //  com.getui.demo.ChainUpPushService 为第三方自定义推送服务
            PushManager.getInstance().initialize(this, DemoPushService::class.java)
            // com.getui.demo.DemoIntentService 为第三方自定义的推送服务事件接收类
            PushManager.getInstance().setPrivacyPolicyStrategy(this, false)
        }

    }

     fun changeNetwork(simulate: Boolean,first:Boolean=false){
         if (UtilSystem.getVersionName1() == "version_1"){
             return
         }
         if (UtilSystem.getVersionName1() == "dev"){
             return
         }
         if(simulate){
              //模拟盘
             url= HttpResultUrlData(
                 baseUrl = "http://8.219.93.19:8082/base/appapi",
                 contractSocketAddress = "ws://8.219.93.19:8082/contract/kline/ws",
                 contractUrl = "http://8.219.93.19:8082/contract/appapi",
                 httpHostUrlContractV2 = "http://8.219.93.19:8082/contract/appapi",
                 otcBaseUrl = "http://8.219.93.19:8082/otc/appapi",
                 otcSocketAddress = "ws://8.219.93.19:8082/otc/chat/ws",
                 redPackageUrl = "https://dev5redpacket.chaindown.com/app-redPacket-api/",
                 socketAddress = "ws://8.219.93.19:8082/base/kline/ws",
                 wssHostContractV2 = "ws://8.219.93.19:8082/contract/kline/ws",
                 optionUrl = "",
                 blocksUrl = "",
                 chatUrl = "",
                 chatApiUrl = ""
             )
         }else{
             //实盘
             url= HttpResultUrlData(
                 baseUrl = "http://8.219.93.19:8081/base/appapi",
                 contractSocketAddress = "ws://8.219.93.19:8081/contract/kline/ws",
                 contractUrl = "http://8.219.93.19:8081/contract/appapi",
                 httpHostUrlContractV2 = "http://8.219.93.19:8081/contract/appapi",
                 otcBaseUrl = "http://8.219.93.19:8081/otc/appapi",
                 otcSocketAddress = "ws://8.219.93.19:8081/otc/chat/ws",
                 redPackageUrl = "https://dev5redpacket.chaindown.com/app-redPacket-api/",
                 socketAddress = "ws://8.219.93.19:8081/base/kline/ws",
                 wssHostContractV2 = "ws://8.219.93.19:8081/contract/kline/ws",
                 optionUrl = "",
                 blocksUrl = "",
                 chatUrl = "",
                 chatApiUrl = ""
             )
         }
//           if (first||simulate!=SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)){
               HttpClient.instance.changeNetwork(url!!.baseUrl.split("//")[1])
               PushManager.getInstance()
                   .registerPushIntentService(this, HandlePushIntentService::class.java)
               WsAgentManager.instance.socketUrl(url!!.socketAddress, true)
               CpWsContractAgentManager.instance.socketUrl(url!!.contractSocketAddress, true)
               CpHttpHelper.instance.serviceUrl(url!!.contractUrl)
               SPUtils.getInstance().put(ParamConstant.simulate, simulate)
//           }




     }




    private fun initAppStatusListener() {

        ForegroundCallbacks.init(this).addListener(object : ForegroundCallbacks.Listener {
            override fun onBecameForeground() {

                ForegroundCallbacksObserver.getInstance().ForegroundListener()
            }

            override fun onBecameBackground() {

                ForegroundCallbacksObserver.getInstance().CallBacksListener()
            }

            override fun onBackChange(visiable: Boolean) {
                ForegroundCallbacksObserver.getInstance().BackAppListener(visiable)
            }
        })
    }

    private fun setCurrentTheme() {
        val themeMode = PublicInfoDataService.getInstance().themeMode
        when (themeMode) {
            PublicInfoDataService.THEME_MODE_DAYTIME -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            PublicInfoDataService.THEME_MODE_NIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocalManageUtil.setApplicationLanguage(applicationContext)
        CpLocalManageUtil.setApplicationLanguage(applicationContext)

        val isZhEnv = SystemUtils.isZh()
        //通知合约SDK语言环境
        ContractSDKAgent.isZhEnv = isZhEnv
        if (isZhEnv) {
            Log.d(TAG, "====中文")
            LocalManageUtil.saveSelectLanguage(this, "zh_CN")
            CpLocalManageUtil.saveSelectLanguage(this, "zh_CN")
        } else if (SystemUtils.isVietNam()) {
            Log.d(TAG, "====英文")
            LocalManageUtil.saveSelectLanguage(this, "vi_VN")
            CpLocalManageUtil.saveSelectLanguage(this, "vi_VN")
        }

    }


    interface AppStateChangeListener {
        fun appTurnIntoForeground()
        fun appTurnIntoBackGround()
    }

    var isBackgroud = false
    private fun getAppStateChangeListener() = object : AppStateChangeListener {
        override fun appTurnIntoBackGround() {
            isBackgroud = true
            WsAgentManager.instance.isBackgroud = true
            restart()
        }

        override fun appTurnIntoForeground() {
            isBackgroud = true
            WsAgentManager.instance.isBackgroud = false
            if (!SPUtils.getInstance().getBoolean("SplashActivityIsFirst",true)){
                startTime()
            }
        }
    }

    var subscribe: Disposable? = null//保存订阅者
    fun startTime() {
        Log.e("LogUtils", "startTime time")
        restart()
        subscribe = io.reactivex.Observable.interval(0, CommonConstant.rateLoopTime, TimeUnit.SECONDS)//按时间间隔发送整数的Observable
                .observeOn(AndroidSchedulers.mainThread())//切换到主线程修改UI
                .subscribe {
                    val intent = Intent(this, DataInitService::class.java)
                    if (it > 1L) {
                        intent.putExtra("isFirst", true)
                    }
                    try {
                        startService(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
    }

    /**
     * 结束计时,重新开始
     */
    fun restart() {
        Log.e("LogUtils", "dispose ${subscribe}")
        if (subscribe != null) {
            subscribe?.dispose()//取消订阅
            Log.e("LogUtils", "dispose time")
        }
    }




    private fun getCurProcessName(context: Context): String? {
        val pid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in activityManager.runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName
            }
        }
        return null
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStarted(p0: Activity) {

        if (appCount == 0) {
            currentState = STATE_FOREGROUND
            appStateChangeListener?.appTurnIntoForeground()
        }
        appCount++
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStopped(p0: Activity) {
        appCount--
        if (appCount == 0) {
            currentState = STATE_BACKGROUND
            appStateChangeListener?.appTurnIntoBackGround()
        }
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityResumed(p0: Activity) {
    }

}
