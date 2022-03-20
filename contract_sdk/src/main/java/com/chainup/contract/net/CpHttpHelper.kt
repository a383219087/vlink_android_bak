package com.chainup.contract.net

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

    init {
        initOkHttpClient()
    }

    fun clearServiceMap() {
        mServiceMap?.clear()
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
     * return baseUrl ApiService
     */
    fun <S> getBaseUrlService(serviceClass: Class<S>): S {
        return createService(CpNetUrl.baseUrl(), serviceClass)
    }

    /**
     * 测速用的Service
     */
    fun <S> getspeedUrlService(url: String, serviceClass: Class<S>): S {
        return createService(url, serviceClass)
    }


    /*
     * return otcBaseUrl ApiService
     */
    fun <S> getOtcBaseUrlService(serviceClass: Class<S>): S {
        return createService(CpNetUrl.getotcBaseUrl(), serviceClass)
    }

    /*
     * return contractUrl ApiService
     */
    fun <S> getContractUrlService(serviceClass: Class<S>): S {
        return createService(CpNetUrl.getcontractUrl(), serviceClass)
    }

    /*
    * return contractUrl ApiService
    */
    fun <S> getContractNewUrlService(serviceClass: Class<S>): S {
        return createService(CpNetUrl.getContractNewUrl(), serviceClass)
    }

    /*
     * return redPackageUrl ApiService
     */
    fun <S> getRedPackageUrlService(serviceClass: Class<S>): S {
        return createService(CpNetUrl.getredPackageUrl(), serviceClass)
    }


    private fun <S> createService(url: String, serviceClass: Class<S>): S {
        if (mServiceMap.containsKey(serviceClass.name)) {
            return mServiceMap.get(serviceClass.name) as S
        } else {
            var obj = createRetrofit(url).create(serviceClass) //as S//createService(baseUrl,serviceClass);
            if (serviceClass.name != "com.yjkj.chainup.model.api.SpeedApiService") {
                mServiceMap.put(serviceClass.name, obj as Any)
            }
            return obj
        }
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
