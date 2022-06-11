package com.yjkj.chainup.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.SPUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.ws.CpWsContractAgentManager
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.databinding.ActivitySplashBinding
import com.yjkj.chainup.manager.DataInitService
import com.yjkj.chainup.model.api.SpeedApiService
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.new_version.activity.NewMainActivity
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.Utils
import com.yjkj.chainup.util.permissionIsGranted
import com.yjkj.chainup.ws.WsAgentManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {


    companion object {
        const val PERMISSION_REQUEST_CODE_STORAGE: Int = 101
        val REQUEST_PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private var mBinding: ActivitySplashBinding? = null
    private var liksArray: ArrayList<String> = arrayListOf()
    private var currentCheckIndex = 0
    private var mRetrofit: Retrofit? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        initRetrofit()
        if (Utils.checkDeviceHasNavigationBar2(this)) {
            mBinding?.ivSplash?.visibility = View.GONE
            mBinding?.rlSplash?.setBackgroundResource(R.drawable.bg_splash)
        }

        if (!this.isTaskRoot) {
            if (intent?.action != null) {
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
                    finish()
                    return
                }
            }
        }
//        liksArray.add("http://8.219.64.81:8091")
//        liksArray.add("http://8.219.72.62:8091")
        liksArray.add("http://gate.ylty1688.com")

        checkNetworkLine(liksArray[currentCheckIndex])
//                        if (hasPermission()) {
//                    Handler().postDelayed({ goHome() }, 150)
//                } else {
//                    requestPermission()
//                }

    }


    @SuppressLint("CheckResult")
    fun checkNetworkLine(baseUrl: String) {
        mRetrofit!!.create(SpeedApiService::class.java).getHealth("$baseUrl/api/query/address")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                LogUtil.d("我是SplashActivity", it.toString())
                ChainUpApp.url = it
                if (it.baseUrl.contains("http://") || it.baseUrl.contains("https://")) {
                    HttpClient.instance.changeNetwork(it.baseUrl.split("//")[1])
                } else {
                    HttpClient.instance.changeNetwork(it.baseUrl)
                }

//                PushManager.getInstance().registerPushIntentService(this, HandlePushIntentService::class.java)
                CpClLogicContractSetting.setApiWsUrl(this, it.contractUrl, it.contractSocketAddress)
                WsAgentManager.instance.socketUrl(it.socketAddress, true)
                CpWsContractAgentManager.instance.socketUrl(it.contractSocketAddress, true)
                if (SPUtils.getInstance().getBoolean("SplashActivityIsFirst", true)) {
                    val intent = Intent(this, DataInitService::class.java)
                    intent.putExtra("isFirst", true)
                    startService(intent)
                    SPUtils.getInstance().put("SplashActivityIsFirst", false)
                }
                runBlocking {
                    Thread.sleep(200)
                    if (hasPermission()) {
                        goHome()
                    } else {
                        requestPermission()
                    }
                }



            }, {
                if (currentCheckIndex < liksArray.size - 1) {
                    currentCheckIndex++
                    checkNetworkLine(liksArray[currentCheckIndex])
                }


            })

    }

    fun initRetrofit() {
        val retrofitBuilder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://47.242.7.76:9091")
        val mBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .proxy(Proxy.NO_PROXY)
        val client: OkHttpClient = mBuilder.build()
        mRetrofit = retrofitBuilder.client(client).build()
    }


    private fun goHome() {
        startActivity(Intent(this@SplashActivity, NewMainActivity::class.java))//
        finish()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (PERMISSION_REQUEST_CODE_STORAGE == requestCode) {
            if (permissions.isNotEmpty() && grantResults.permissionIsGranted()) {
                goHome()
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale
                    (this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
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
        return checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(REQUEST_PERMISSIONS, PERMISSION_REQUEST_CODE_STORAGE)
    }


    override fun onBackPressed() {
        return
    }

}