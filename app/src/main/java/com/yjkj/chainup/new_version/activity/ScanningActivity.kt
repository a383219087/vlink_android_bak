package com.yjkj.chainup.new_version.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.yjkj.chainup.R
import com.yjkj.chainup.base.BaseActivity
import kotlinx.android.synthetic.main.activity_scanning.*


//二维码扫描
class ScanningActivity : BaseActivity(), DecoratedBarcodeView.TorchListener {
    lateinit var mCaptureManager: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanning)
//        if (hasFlash()) {
//            btn_switch.visibility = View.VISIBLE
//        } else {
//            btn_switch.visibility = View.GONE
//        }
        mCaptureManager = CaptureManager(this, dbv_custom)
        mCaptureManager.initializeFromIntent(intent, savedInstanceState)
        mCaptureManager.decode()
        initListener()
    }


    private fun initListener() {
        dbv_custom?.setTorchListener(this)
//        v_title.tv_menu.setOnClickListener {
//            if (isLightOn) {
//                dbv_custom.setTorchOff();
//            } else {
//                dbv_custom.setTorchOn();
//            }
//        }
        iv_back?.setOnClickListener { finish() }


    }

    var isLightOn = false
    override fun onTorchOn() {
        isLightOn = true
    }

    override fun onTorchOff() {
        isLightOn = false
    }


    // 判断是否有闪光灯功能
    private fun hasFlash(): Boolean {
        return applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    override fun onPause() {
        super.onPause()
        mCaptureManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        mCaptureManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCaptureManager.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        mCaptureManager.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return dbv_custom.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

}
