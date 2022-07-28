package com.yjkj.chainup.net

import com.yjkj.chainup.app.AppConfig
import com.yjkj.chainup.app.GlobalAppComponent
import com.yjkj.chainup.util.HttpsUtils
import com.yjkj.chainup.util.StringUtil
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * http helper负责创建ApiService实例
 */
class HttpHelper {
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
        val logging = HttpLoggingInterceptor(HttpLoggingInterceptor { message -> })
        logging.level = HttpLoggingInterceptor.Level.BODY
        val sslParams = HttpsUtils.getSslSocketFactory(null, null, null)

        buidler.protocols(Collections.singletonList(Protocol.HTTP_1_1))
        buidler.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)


        buidler.readTimeout(AppConfig.read_time, TimeUnit.MILLISECONDS)
        buidler.writeTimeout(AppConfig.write_time, TimeUnit.MILLISECONDS)
        buidler.connectTimeout(AppConfig.connect_time, TimeUnit.MILLISECONDS)
        buidler.addInterceptor(logging)
        buidler.addInterceptor(NetInterceptor())
        buidler.retryOnConnectionFailure(true)
        buidler.cache(cache)
        mOkHttpClient = buidler.build()

    }





    /*
     * return baseUrl ApiService
     */
    fun <S> getBaseUrlService(serviceClass: Class<S>): S {
        return createService(NetUrl.baseUrl(), serviceClass)
    }


    /*
     * return otcBaseUrl ApiService
     */
    fun <S> getOtcBaseUrlService(serviceClass: Class<S>): S {
        return createService(NetUrl.getotcBaseUrl(), serviceClass)
    }

    /*
     * return contractUrl ApiService
     */
    fun <S> getContractUrlService(serviceClass: Class<S>): S {
        return createService(NetUrl.getcontractUrl(), serviceClass)
    }

    /*
    * return contractUrl ApiService
    */
    fun <S> getContractNewUrlService(serviceClass: Class<S>): S {
        return createService(NetUrl.getContractNewUrl(), serviceClass)
    }

    /*
     * return redPackageUrl ApiService
     */
    fun <S> getRedPackageUrlService(serviceClass: Class<S>): S {
        return createService(NetUrl.getredPackageUrl(), serviceClass)
    }


    private fun <S> createService(url: String, serviceClass: Class<S>): S {
        return if (mServiceMap.containsKey(serviceClass.name)) {
            mServiceMap.get(serviceClass.name) as S
        } else {
            val obj = createRetrofit(url).create(serviceClass) //as S//createService(baseUrl,serviceClass);
            obj
        }
    }


    private fun createRetrofit(baseUrl: String?): Retrofit {
        var url = baseUrl
        if (!StringUtil.isHttpUrl(url))
            url = AppConfig.default_host //容错处理
        if (!url!!.endsWith("/"))
            url += "/"
        return Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build()
    }

    companion object {
        private val cache = Cache(GlobalAppComponent.getContext().cacheDir, (1024 * 1024 * 10).toLong())
        private var mHttpHelper: HttpHelper? = null

        val instance: HttpHelper
            @Synchronized get() {
                if (null == mHttpHelper) {
                    mHttpHelper = HttpHelper()
                }
                return mHttpHelper!!
            }
    }

}
