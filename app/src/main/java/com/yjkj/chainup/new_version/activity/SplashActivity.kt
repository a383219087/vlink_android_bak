package com.yjkj.chainup.new_version.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.manager.NetworkLineErrorService
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.ToastUtils
import com.yjkj.chainup.util.Utils
import com.yjkj.chainup.util.permissionIsGranted
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {


    companion object {
        const val PERMISSION_REQUEST_CODE_STORAGE: Int = 101
        val REQUEST_PERMISSIONS = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private  var liksArray: ArrayList<String> = arrayListOf()
    private var currentCheckIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (Utils.checkDeviceHasNavigationBar2(this)) {
            iv_splash?.visibility = View.GONE
            rl_splash?.setBackgroundResource(R.drawable.bg_splash)
        }

        if (!this.isTaskRoot) {
            if (intent?.action != null) {
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.action)) {
                    finish()
                    return
                }
            }
        }
        liksArray.add("http://47.242.7.76:8091")
        liksArray.add("http://47.242.7.76:8091")
        liksArray.add("http://47.242.7.76:8091")

//        checkNetworkLine(liksArray[currentCheckIndex])
                        if (hasPermission()) {
                    Handler().postDelayed({ goHome() }, 150)
                } else {
                    requestPermission()
                }

    }

    @SuppressLint("CheckResult")
    fun checkNetworkLine(baseUrl:String){
        HttpClient.instance.checkNetworkLine(baseUrl)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                val domainUrl = PublicInfoDataService.getInstance().newWorkURL
//                    HttpClient.instance.changeNetwork(url)
//                if (hasPermission()) {
//                    Handler().postDelayed({ goHome() }, 150)
//                } else {
//                    requestPermission()
//                }
            }, {
//                    liksArray[index].put("error", "error")
                    LogUtil.v("sas", "getHeath error 线路 ${1 + 1}")
//                    val tempArray = linesSpeed.get(liksArray[index].optString("hostName"))
//                    tempArray?.run {
//                        add("0")
//                    }
//                    if (currentCheckIndex != (liksArray.size - 1)) {
//                        currentCheckIndex++
//                        Observable.timer(10, TimeUnit.SECONDS)
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread()).subscribe {
//                                    getHeath(currentCheckIndex, liksArray[currentCheckIndex])
//                                }
//                    } else {
//                        resetCheckStatus()
//
//                    }
            })

    }



    fun goHome() {
        startActivity(Intent(this@SplashActivity, NewMainActivity::class.java))//
        finish()
    }

    override fun onRestart() {
        super.onRestart()
        //用户第一次拒绝权限  home键退出应用后再次回来  再提示一次授权
        if (hasPermission()) {
            Handler().postDelayed({ goHome() }, 150)
        } else {
            requestPermission()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (PERMISSION_REQUEST_CODE_STORAGE == requestCode) {
            if (permissions.isNotEmpty() && grantResults.permissionIsGranted()) {
                goHome()
            }else if (! ActivityCompat.shouldShowRequestPermissionRationale
                    (this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                /**
                 * 用户点击拒绝且不再询问   可在此操作
                 * 可直接跳转设置界面打开权限  也可弹出 Toast 提示用户开启权限
                 * ToastUtils.showToast("请进入系统设置中授予相应权限")
                 */
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(REQUEST_PERMISSIONS, PERMISSION_REQUEST_CODE_STORAGE)
        }
    }


    override fun onBackPressed() {
        return
    }

}
