package com.yjkj.chainup.contract.cloud

import com.contract.sdk.ContractSDKAgent
import com.common.sdk.utlis.AESUtil
import com.contract.sdk.net.ContractApiConstants
import com.yjkj.chainup.util.PackageUtil
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Response
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * @author ZhongWei
 * @time 2020/8/19 10:35
 * @description HttpClient
 **/
object ContractCloudHttpClient {

    private const val TIMEOUT: Long = 15

    val JSON_TYPE = "application/json; charset=utf-8".toMediaTypeOrNull()

    val httpClient = initHttpClient()

    private fun initHttpClient(): OkHttpClient {
        val trustManager = CloudX509TrustManager()
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier(HostnameVerifier { _, _ -> true })
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .callTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .connectionPool(ConnectionPool(5, 1, TimeUnit.SECONDS))
                .addInterceptor(CommonHeaderInterceptor())
                .build()
    }

    /**
     * X509TrustManager
     */
    class CloudX509TrustManager : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            return arrayOfNulls(0)
        }
    }

    /**
     * 通用请求头
     */
    class CommonHeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val builder = chain.request().newBuilder()
            builder.apply {
                addHeader("Content-Type", "application/json")
                addHeader("Accept", "application/json")
                addHeader("charset", "utf-8")
                addHeader(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_HEADER_VER), PackageUtil.getVersionName())
                addHeader(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_HEADER_DEV), "Android")
            }
            return chain.proceed(builder.build())
        }
    }

    /**
     * 创建请求头
     */
    fun createHeaders(): HashMap<String, String> {
        val headers = HashMap<String, String>()
        val language = if (ContractSDKAgent.isZhEnv) "zh-cn" else "en"
        val timestamp = (System.currentTimeMillis() * 1000).toString()
        val token = ContractSDKAgent.contractToken
        if (!token.isNullOrEmpty()) {
            val sign: String = try {
                AESUtil.getInstance().encrypt(
                        token,
                        "lMYQry09AeIt6PNO",
                        timestamp)
                        .substring(0, ContractSDKAgent.httpConfig?.wsSignLength ?: 64)
            } catch (e: Exception) {
                ""
            }
            headers.apply {
                put(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_USER_SIGN), sign)
                put(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_UID), ContractSDKAgent.user.uid.toString())
                put(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_EXPIRED_TS), ContractSDKAgent.user.expiredTs)
                put(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_ACCESS_KEY), ContractSDKAgent.user.accessKey)
                put(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_USER_TOKEN), token)
            }
        }
        headers[ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_HEADER_LANGUAGE)] = language
        headers[ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_USER_TS)] = timestamp
        return headers
    }

}