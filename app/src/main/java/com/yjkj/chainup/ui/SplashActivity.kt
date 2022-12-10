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
import com.chainup.contract.net.CpHttpHelper
import com.chainup.contract.ws.CpWsContractAgentManager
import com.igexin.sdk.PushManager
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.contract.utils.UtilSystem
import com.yjkj.chainup.databinding.ActivitySplashBinding
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.push.HandlePushIntentService
import com.yjkj.chainup.manager.DataInitService
import com.yjkj.chainup.model.api.HttpResultUrlData
import com.yjkj.chainup.model.api.SpeedApiService
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.util.LogUtil
import com.yjkj.chainup.util.ToastUtils
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
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private var mBinding: ActivitySplashBinding? = null
    private var liksArray: ArrayList<String> = arrayListOf()
    private var links: ArrayList<String> = arrayListOf()
    private var currentCheckIndex = 0
    private var mRetrofit: Retrofit? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        initRetrofit()


        if (!this.isTaskRoot) {
            if (intent?.action != null) {
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
                    finish()
                    return
                }
            }
        }
        if (UtilSystem.getVersionName1() == "version_1") {

            goToMain(
                HttpResultUrlData(
                    baseUrl = "http://47.242.41.181/base/appapi",
                    contractSocketAddress = "ws://47.242.41.181/contract/kline/ws",
                    contractUrl = "http://47.242.41.181/contract/appapi",
                    httpHostUrlContractV2 = "http://47.242.41.181/contract/appapi",
                    otcBaseUrl = "http://47.242.41.181/otc/appapi",
                    otcSocketAddress = "ws://47.242.41.181/otc/chat/ws",
                    redPackageUrl = "https://dev5redpacket.chaindown.com/app-redPacket-api/",
                    socketAddress = "ws://47.242.41.181/base/kline/ws",
                    wssHostContractV2 = "ws://47.242.41.181/contract/kline/ws",
                    optionUrl = "",
                    blocksUrl = "",
                    chatUrl = "",
                    chatApiUrl = ""
                )
            )
//      goToMain(
//        HttpResultUrlData(
//          baseUrl = "http://120.78.198.245/base/appapi",
//          contractSocketAddress ="ws://120.78.198.245/contract/kline/ws",
//          contractUrl = "http://120.78.198.245/contract/appapi",
//          httpHostUrlContractV2 = "http://120.78.198.245/contract/appapi",
//          otcBaseUrl = "http://120.78.198.245/otc/appapi",
//          otcSocketAddress = "ws://120.78.198.245/otc/chat/ws",
//          redPackageUrl =  "https://dev5redpacket.chaindown.com/app-redPacket-api/",
//          socketAddress =  "ws://120.78.198.245/base/kline/ws",
//          wssHostContractV2 = "ws://120.78.198.245/contract/kline/ws",
//          optionUrl = "http://option.zwwbit.com/wallet/#/pages/index/index",
//          blocksUrl ="http://blocks.zwwbit.com/block/#/pages/index/index_one",
//          chatUrl = "",
//          chatApiUrl = "http://47.254.214.243:8011",
//
//        )
//      )

        } else {
//      val linkData = SPUtils.getInstance().getString("links", "")
//        if(linkData.isNotEmpty()){
//            liksArray.addAll(linkData.split(","))
//        }
//      liksArray.add("http://8.219.206.92:8091")
//      liksArray.add("http://8.219.215.188:8091")
//      liksArray.add("http://8.219.217.227:8091")

            liksArray.add("http://120.78.198.245:8091")
            liksArray.add("http://120.79.19.80:8091")

            ///其他服务的节点
//      liksArray.add("http://119.23.59.211:8091")
//      liksArray.add("http://120.77.40.245:8091")
//            liksArray.add("http://www.qyrx.me/gate")
            checkNetworkLine(liksArray[currentCheckIndex])


            if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)&& UserDataService.getInstance().isLogined) {
                goToMain(
                    HttpResultUrlData(
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
                )
            }else{
                goToMain(
                    HttpResultUrlData(
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
                )
            }



        }


    }


    @SuppressLint("CheckResult")
    fun checkNetworkLine(baseUrl: String) {
        mRetrofit!!.create(SpeedApiService::class.java).getHealth("$baseUrl/api/query/address")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                LogUtil.d("我是SplashActivity", it.toString())
                goToMain(it)
            }, {
                LogUtil.d("我是SplashActivity", it.toString())
                if (currentCheckIndex < liksArray.size - 1) {
                    currentCheckIndex++
                    checkNetworkLine(liksArray[currentCheckIndex])
                } else {
                    ToastUtils.showToast(it.message)

                }

            })

    }

    private fun goToMain(it: HttpResultUrlData) {
        LogUtil.d("我是SplashActivity", it.toString())
        ChainUpApp.url = it
        if (it.baseUrl.contains("http://") || it.baseUrl.contains("https://")) {
            HttpClient.instance.changeNetwork(it.baseUrl.split("//")[1])
        } else {
            HttpClient.instance.changeNetwork(it.baseUrl)
        }
//                links.remove(it.baseUrl.replace("/base/appapi",""))
//                links.add(0,it.baseUrl.replace("/base/appapi",""))
//                var linkData=""
//                for(link in links){
//                    linkData= "$linkData,$link"
//                }
//                SPUtils.getInstance().put("links",linkData.substring(1))
        PushManager.getInstance()
            .registerPushIntentService(this, HandlePushIntentService::class.java)
        WsAgentManager.instance.socketUrl(it.socketAddress, true)
        CpWsContractAgentManager.instance.socketUrl(it.contractSocketAddress, true)
        CpHttpHelper.instance.serviceUrl(it.contractUrl)
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
    }

    fun initRetrofit() {
        val retrofitBuilder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://47.242.7.76:9091")
        val mBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .proxy(Proxy.NO_PROXY)
        val client: OkHttpClient = mBuilder.build()
        mRetrofit = retrofitBuilder.client(client).build()
    }


    private fun goHome() {
        startActivity(Intent(this@SplashActivity, NewMainActivity::class.java))//
        finish()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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
