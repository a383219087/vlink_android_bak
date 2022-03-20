package com.chainup.contract.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.chainup.contract.listener.CpForegroundCallbacks
import com.chainup.contract.listener.CpForegroundCallbacksObserver
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.ChainUpLogUtil
import com.chainup.contract.utils.CpLocalManageUtil
import com.chainup.contract.utils.CpSystemUtils


open class CpMyApp : Application(), Application.ActivityLifecycleCallbacks {
    val TAG = CpMyApp::class.java.simpleName
    var appCount = 0
    private var currentState: Int = 0
    private var appStateChangeListener: AppStateChangeListener? = null
    val STATE_FOREGROUND = 0
    val STATE_BACKGROUND = 1


    companion object {
        private var instance: CpMyApp? = null
        fun instance() = instance!!
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setCurrentTheme()
        initAppStatusListener()
        initSocket()
    }

    private fun initSocket() {

    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        CpLocalManageUtil.setApplicationLanguage(applicationContext)
//        val isZhEnv = CpSystemUtils.isZh()
//        if (isZhEnv) {
//            Log.d(TAG, "====中文")
//            CpLocalManageUtil.saveSelectLanguage(this, "zh_CN")
//        } else if (CpSystemUtils.isVietNam()) {
//            Log.d(TAG, "====英文")
//            CpLocalManageUtil.saveSelectLanguage(this, "vi_VN")
//        }
//    }

    private fun setCurrentTheme() {
        val themeMode = CpClLogicContractSetting.getThemeMode(this)
        when (themeMode) {
            CpClLogicContractSetting.THEME_MODE_DAYTIME -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            CpClLogicContractSetting.THEME_MODE_NIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun initAppStatusListener() {
        CpForegroundCallbacks.init(this).addListener(object : CpForegroundCallbacks.Listener {
            override fun onBecameForeground() {
                CpForegroundCallbacksObserver.getInstance().ForegroundListener()
            }

            override fun onBecameBackground() {
                CpForegroundCallbacksObserver.getInstance().CallBacksListener()
            }

            override fun onBackChange(visiable: Boolean) {
                CpForegroundCallbacksObserver.getInstance().BackAppListener(visiable)
            }
        })
    }

    override fun onActivityPaused(activity: Activity) {
        ChainUpLogUtil.e(TAG, "------------ onActivityPaused ------------")
    }

    override fun onActivityStarted(activity: Activity) {
        ChainUpLogUtil.e(TAG, "------------ onActivityStarted ------------")
        if (appCount == 0) {
            currentState = STATE_FOREGROUND
            appStateChangeListener?.appTurnIntoForeground()
        }
        appCount++
    }

    override fun onActivityDestroyed(activity: Activity) {
        ChainUpLogUtil.e(TAG, "------------ onActivityDestroyed ------------")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        ChainUpLogUtil.e(TAG, "------------ onActivitySaveInstanceState ------------")
    }

    override fun onActivityStopped(activity: Activity) {
        ChainUpLogUtil.e(TAG, "------------ onActivityStopped ------------")
        appCount--
        if (appCount == 0) {
            currentState = STATE_BACKGROUND
            appStateChangeListener?.appTurnIntoBackGround()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ChainUpLogUtil.e(TAG, "------------ onActivityCreated ------------")
    }

    override fun onActivityResumed(activity: Activity) {
        ChainUpLogUtil.e(TAG, "------------ onActivityResumed ------------")
    }

    interface AppStateChangeListener {
        fun appTurnIntoForeground()
        fun appTurnIntoBackGround()
    }
}
