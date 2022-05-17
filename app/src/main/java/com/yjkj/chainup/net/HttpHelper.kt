package com.yjkj.chainup.net

import android.app.DownloadManager
import android.content.IntentFilter
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import com.yjkj.chainup.app.AppConfig
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.app.GlobalAppComponent
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.util.HttpsUtils
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.wedegit.DownLoadReceiver
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
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

     fun initOkHttpClient() {
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

    fun downPciture(mDownLoadPath: String) {
        // 创建下载任务
        var request = DownloadManager.Request(Uri.parse(mDownLoadPath))
        // 漫游网络是否可以下载
        request.setAllowedOverRoaming(false)

        // 设置文件类型，可以在下载结束后自动打开该文件
        var mimeTypeMap = MimeTypeMap.getSingleton()
        var mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(mDownLoadPath))
        request.setMimeType(mimeString)

        // 在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        // sdcard的目录下的download文件夹，必须设置
        request.setDestinationInExternalPublicDir(ChainUpApp.appContext.getPackageName()
                + File.separator + "cer" + File.separator, ParamConstant.MFILENAME)
        //  request.setDestinationInExternalFilesDir(),也可以自己制定下载路径

        // 将下载请求加入下载队列
        val downloadManager = ChainUpApp.appContext.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        // 加入下载队列后会给该任务返回一个long型的id，
        // 通过该id可以取消任务，重启任务等等
        val taskId = downloadManager.enqueue(request)

        //注册广播接收者，监听下载状态
        val downLoadReceiver = DownLoadReceiver(ChainUpApp.appContext, downloadManager, taskId, ParamConstant.MFILENAME)
        ChainUpApp.appContext.registerReceiver(downLoadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
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
