package com.chainup.contract.net

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.chainup.contract.app.CpAppConfig
import com.chainup.contract.app.CpMyApp
import com.chainup.contract.utils.CpHttpsUtils
import com.chainup.contract.utils.CpStringUtil
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * http helper负责创建ApiService实例
 */
class CpHttpHelper {
    public val mServiceMap = HashMap<String, Any>()//: HashMap<String, Any>?=null

    private var mOkHttpClient: OkHttpClient? = null
    var serverUrl: String = "http://8.219.93.19:8081/contract/appapi"

    init {
        initOkHttpClient()
    }

    fun clearServiceMap() {
        mServiceMap?.clear()
    }
    fun serviceUrl(socketUrl: String) {
        LogUtils.e("我是切换地址我是切换地址1111",socketUrl)
        this.serverUrl = socketUrl

    }

    private fun initOkHttpClient() {
        val buidler = OkHttpClient.Builder()
        val sslParams = CpHttpsUtils.getSslSocketFactory(null, null, null)
        buidler.protocols(Collections.singletonList(Protocol.HTTP_1_1))
        buidler.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
        buidler.readTimeout(CpAppConfig.read_time, TimeUnit.MILLISECONDS)
        buidler.writeTimeout(CpAppConfig.write_time, TimeUnit.MILLISECONDS)
        buidler.connectTimeout(CpAppConfig.connect_time, TimeUnit.MILLISECONDS)
        buidler.addInterceptor(CpNetInterceptor())
        buidler.retryOnConnectionFailure(true)
        buidler.cache(cache)
        if (CpAppConfig.IS_DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            buidler.addInterceptor(loggingInterceptor)
        }
        mOkHttpClient = buidler.build()
    }

    /*
    * return contractUrl ApiService
    */
    fun <S> getContractNewUrlService(serviceClass: Class<S>): S {
        if (getVersionName1() != "version_1"&&getVersionName1() != "dev"){
            if ( SPUtils.getInstance().getBoolean("simulate", false)){
                this.serverUrl= "http://8.219.93.19:8082/contract/appapi"
            }else{
                this.serverUrl= "http://8.219.93.19:8081/contract/appapi"
            }
        }
        return createRetrofit(serverUrl).create(serviceClass)
    }

   private fun getVersionName1(): String? {
        val versionName: String = try {
            CpMyApp.instance().packageManager.getPackageInfo( CpMyApp.instance().getPackageName(), 0).versionName
        } catch (e: Exception) {
            "version"
        }
        return versionName
    }



    private fun createRetrofit(baseUrl: String?): Retrofit {
        var url = baseUrl
        if (!CpStringUtil.isHttpUrl(url))
            url = CpAppConfig.default_host //容错处理
        if (!url!!.endsWith("/"))
            url += "/"
        return Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build()
    }

    companion object {
        private val cache = Cache(CpMyApp.instance().cacheDir, (1024 * 1024 * 10).toLong())
        private var mHttpHelper: CpHttpHelper? = null

        val instance: CpHttpHelper
            @Synchronized get() {
                if (null == mHttpHelper) {
                    mHttpHelper = CpHttpHelper()
                }
                return mHttpHelper!!
            }
    }

}
