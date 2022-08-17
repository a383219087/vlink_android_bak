package com.chainup.contract.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.chainup.contract.listener.CpForegroundCallbacks
import com.chainup.contract.listener.CpForegroundCallbacksObserver
import com.chainup.contract.utils.ChainUpLogUtil
import com.chainup.contract.utils.CpClLogicContractSetting


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
    }

    override fun onActivityStarted(activity: Activity) {
        if (appCount == 0) {
            currentState = STATE_FOREGROUND
            appStateChangeListener?.appTurnIntoForeground()
        }
        appCount++
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
        appCount--
        if (appCount == 0) {
            currentState = STATE_BACKGROUND
            appStateChangeListener?.appTurnIntoBackGround()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    interface AppStateChangeListener {
        fun appTurnIntoForeground()
        fun appTurnIntoBackGround()
    }
}
