package com.yjkj.chainup.app

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.bilibili.boxing.BoxingCrop
import com.bilibili.boxing.BoxingMediaLoader
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpLocalManageUtil
import com.chainup.contract.ws.CpWsContractAgentManager
import com.chainup.talkingdata.AppAnalyticsExt
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.net.ContractHttpConfig
import com.contract.sdk.utils.SwapLogUtil
import com.igexin.sdk.PushManager
import com.yjkj.chainup.BuildConfig
import com.yjkj.chainup.contract.cloud.ContractCloudAgent
import com.yjkj.chainup.db.constant.CommonConstant
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.push.DemoPushService
import com.yjkj.chainup.extra_service.push.HandlePushIntentService
import com.yjkj.chainup.manager.DataInitService
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.net_new.NetUrl
import com.yjkj.chainup.new_version.activity.asset.BoxingGlideLoader
import com.yjkj.chainup.new_version.activity.asset.BoxingUcrop
import com.yjkj.chainup.new_version.view.ForegroundCallbacksObserver
import com.yjkj.chainup.util.*
import com.yjkj.chainup.wedegit.ForegroundCallbacks
import com.yjkj.chainup.ws.WsAgentManager
import com.yjkj.chainup.ws.WsContractAgentManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * @Author: Bertking
 * @Date：2019/3/6-10:52 AM
 * @Description:
 */
class ChainUpApp : CpMyApp() {

//    val TAG = ChainUpApp::class.java.simpleName
//    var appCount = 0
    private var appStateChangeListener: AppStateChangeListener? = null
    private var currentState: Int = 0
//    val STATE_FOREGROUND = 0
//    val STATE_BACKGROUND = 1

    companion object {
        lateinit var appContext: Context
        lateinit var app: Application
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        if (BuildConfig.APPLICATION_ID == getCurProcessName(this)) {
            appContext = this
            appStateChangeListener = getAppStateChangeListener()
            registerActivityLifecycleCallbacks(this)
            CommonComponent.getInstance().init(app)
            val headerParams = SystemUtils.getHeaderParams()
            LogUtil.e(TAG,"headerParams ${headerParams}")
            AppAnalyticsExt.instance.init(this, headerParams)

            CpClLogicContractSetting.setApiWsUrl(this,NetUrl.getContractNewUrl(),NetUrl.getContractSocketNewUrl())

            Handler().postDelayed({
                WsAgentManager.instance.socketUrl(ApiConstants.SOCKET_ADDRESS, true)
                CpWsContractAgentManager.instance.socketUrl(CpClLogicContractSetting.getWsUrl(this), true)
            }, 1500)
            setCurrentTheme()
            initAppStatusListener()

            /**
             * contract_version_settings    0 - 旧版合约   1 - 新版合约
             * isNewOldContract     true 新版  false 旧版
             */
            var isNewOldContract: Boolean
            val mContractMode = PublicInfoDataService.getInstance().getContractMode()
            if (mContractMode == 0 || mContractMode == -1) {
                //旧版合约
                isNewOldContract = false
//                openContract()
            } else {
                //新版合约
                isNewOldContract = true
            }

//            val isNewOldContract = PublicInfoDataService.getInstance().isNewOldContract
//            if (!isNewOldContract) {
//                openContract()
//            }
            AppConstant.IS_NEW_CONTRACT = isNewOldContract
            Debug.stopMethodTracing()
            BoxingMediaLoader.getInstance().init(BoxingGlideLoader()) // 需要实现IBoxingMediaLoader
            BoxingCrop.getInstance().init(BoxingUcrop())
        }
        webViewSetPath(this)
        //  com.getui.demo.ChainUpPushService 为第三方自定义推送服务
        PushManager.getInstance().initialize(this, DemoPushService::class.java)
        // com.getui.demo.DemoIntentService 为第三方自定义的推送服务事件接收类
        PushManager.getInstance().registerPushIntentService(this, HandlePushIntentService::class.java)
        PushManager.getInstance().setPrivacyPolicyStrategy(this, false)
    }

    private fun getProcessName(context: Context): String? {
        val am: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps: List<ActivityManager.RunningAppProcessInfo> = am.runningAppProcesses
                ?: return null
        for (proInfo in runningApps) {
            if (proInfo.pid == Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName
                }
            }
        }
        return null
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
        Log.d(TAG, "==========onConfigurationChanged==========")
        LocalManageUtil.setApplicationLanguage(applicationContext)
        CpLocalManageUtil.setApplicationLanguage(applicationContext)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "========现在横屏===")
        }
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
            Log.d(TAG, "========appTurnIntoBackGround===")
            isBackgroud = true
            WsAgentManager.instance.isBackgroud = true
            restart()
        }

        override fun appTurnIntoForeground() {
            Log.d(TAG, "========appTurnIntoForeground===")
            isBackgroud = true
            WsAgentManager.instance.isBackgroud = false
            startTime()
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

    private fun webViewSetPath(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!SystemUtil().isMainProcessFun(this)) {//判断不等于默认进程名称
                WebView.setDataDirectorySuffix(SystemV2Utils.getProcessName(context) ?: "")
            }
        }
    }

    private var timeCount = 0
    private fun initStrictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build())
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build())
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

    private fun openContract() {
        //合约SDK初始化  主进程才实例化
        val contractHttpConfig = ContractHttpConfig()
        contractHttpConfig.prefixHeader = "ex"
        contractHttpConfig.contractUrl = NetUrl.getcontractUrl() + "fe-cov2-api/swap/"
        contractHttpConfig.contractWsUrl = NetUrl.getContractSocketUrl()
        contractHttpConfig.headerParams = SystemUtils.getHeaderParams()
        contractHttpConfig.wsSignLength = 128
        if (ContractCloudAgent.isCloudOpen) {
            //是否是合约云SDK
            ContractSDKAgent.isContractCloudSDK = true
        } else {
            contractHttpConfig.aesSecret = "lMYQry09AeIt6PNO"
            //是否是合约云SDK
            ContractSDKAgent.isContractCloudSDK = false
        }
        //合约SDK Http配置初始化
        ContractSDKAgent.httpConfig = contractHttpConfig
        //是否打开合约API异常日志收集
        ContractSDKAgent.openErrorLogCollect = true
        //通知合约SDK语言环境
        ContractSDKAgent.isZhEnv = SystemUtils.isZh()
        //合约SDK 必须设置 在最后调用
        ContractSDKAgent.init(this)
        UserDataService.getInstance().token
        //延迟2秒初始化合约token
        UserDataService.getInstance().notifyContractLoginStatusListener()
    }

    override fun onActivityPaused(p0: Activity) {
        Log.d(TAG, "========onActivityPaused===")
    }

    override fun onActivityStarted(p0: Activity) {
        Log.d(TAG, "========onActivityStarted===")

        if (appCount == 0) {
            currentState = STATE_FOREGROUND
            appStateChangeListener?.appTurnIntoForeground()
        }
        appCount++
        Log.d(TAG, "========onActivityStarted=== ${appCount}")
    }

    override fun onActivityDestroyed(p0: Activity) {
        Log.d(TAG, "========onActivityDestroyed===")
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        Log.d(TAG, "========onActivitySaveInstanceState===")
    }

    override fun onActivityStopped(p0: Activity) {
        appCount--
        Log.d(TAG, "========onActivityStopped=== ${appCount}")
        if (appCount == 0) {
            currentState = STATE_BACKGROUND
            appStateChangeListener?.appTurnIntoBackGround()
        }
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        Log.d(TAG, "========onActivityCreated===")
    }

    override fun onActivityResumed(p0: Activity) {
        Log.d(TAG, "========onActivityResumed===")
    }

}
