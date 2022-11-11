package com.yjkj.chainup.new_version.activity

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.gson.JsonObject
import com.jaeger.library.StatusBarUtil
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.BuildConfig
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.contract.activity.SlContractAssetRecordActivity
import com.yjkj.chainup.contract.utils.ShareToolUtil
import com.yjkj.chainup.db.constant.HomeTabMap
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.dsbridge.CompletionHandler
import com.yjkj.chainup.dsbridge.DWebView
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.net.NetUrl
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.personalCenter.BindMobileOrEmailActivity
import com.yjkj.chainup.new_version.activity.personalCenter.GoogleValidationActivity
import com.yjkj.chainup.new_version.activity.personalCenter.RealNameCertificationChooseCountriesActivity
import com.yjkj.chainup.new_version.bean.ImageTokenBean
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.JsEchoApi
import com.yjkj.chainup.new_version.view.UploadHelper
import com.yjkj.chainup.util.*
import com.yjkj.chainup.util.QRCodeUtils.Companion.getDecodeAbleBitmap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_item_detail.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


/**
 * 公告&帮助中心详情
 *  此类不允许再加参数字段
 *  //web_url = "https://www.baidu.com/"
//web_url = "https://www.taobao.com/"
// web_url = "https://m.biki.com/noticeDetail?id=AboutUs&type=cms&isapp=1&lan=zh_CN&au=android"
 */

@Route(path = RoutePath.ItemDetailActivity)
class ItemDetailActivity : NBaseActivity() {

    override fun setContentView() = R.layout.activity_item_detail

    /*
     * 要加载的内容(链接url或html内容)
     */
    @JvmField
    @Autowired(name = ParamConstant.web_url)
    var web_url = ""

    /**
     * 拍照的工具类
     */
    var imageTool: ImageTools? = null

    val temp = "{\"_dscbstub\":\"routerName\",\"data\":\"{\"routerName\":\"coinmap_trading\"}\"}"

    /*
     * 顶部head显示的标题
     */
    @JvmField
    @Autowired(name = ParamConstant.head_title)
    var head_title = ""

    /*
     *  web页面类型，常量值参见WebTypeEnum类
     */
    @JvmField
    @Autowired(name = ParamConstant.web_type)
    var web_type = 0


    @JvmField
    @Autowired(name = ParamConstant.DIALING_CODE)
    var dialingCode = "86"

    @JvmField
    @Autowired(name = ParamConstant.NUMBER_CODE)
    var numberCode = "156"

    @JvmField
    @Autowired(name = ParamConstant.COUNTRY_NAME)
    var countryName = "中国"

    @JvmField
    @Autowired(name = ParamConstant.SPEED_WEB_API)
    var speedWebApi = ""

    @JvmField
    @Autowired(name = ParamConstant.SPEED_WS_API)
    var speedWsApi = ""

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        changeTitleStyle()
        v_title?.setTitle(head_title)
        v_title?.apply {
            setWeb(true)
            ibBack.setOnClickListener {
                canFinish()
            }
        }
        imageTool = ImageTools(this)
        initLoadWebview()
    }


    fun initLoadWebview() {

        getPermissions(this)
        initWebView()
        when (web_type) {
            WebTypeEnum.Notice.value -> {
                getNoticeDetail(web_url)
            }
            WebTypeEnum.HELP_CENTER.value -> {
                getWK()
            }
            WebTypeEnum.ROLE_INDEX.value -> {
                v_title?.setTitle("")
                getRoleIndex(web_url)
            }

            WebTypeEnum.NORMAL_INDEX.value -> {
//                web_view.loadData(web_url, "text/html; charset=UTF-8", null)// 解决乱码问题
                activity_new_video_loading_image?.visibility = View.GONE
//                web_view?.loadUrl(web_url)
                showWeb(web_url)
            }

            WebTypeEnum.SING_PASS.value -> {
                v_title?.setTitle("")
//                web_view.loadData(web_url, "text/html; charset=UTF-8", null)// 解决乱码问题
                activity_new_video_loading_image?.visibility = View.GONE
//                web_view?.loadUrl(web_url)
                if (!StringUtil.checkStr(web_url))
                    return
//                http://m.hiup.pro/zh_CN/personal/kycAuth?isapp=1&ua=ios&lan=zh_CN
                LogUtil.d(TAG, "Lan = ${LanguageUtil.getSelectLanguage()}")
                LogUtil.d(TAG, "url:${web_url + "?isapp=1&ua=android&lan=${LanguageUtil.getSelectLanguage()}&country=${numberCode}&countryKeyCode=${dialingCode}"}")
                web_url = getCookieDomain(web_url)
                if (web_url?.contains("?") == true) {
                    web_view?.loadUrl(web_url + "&isapp=1" + PublicInfoDataService.getInstance().getOldContractUrl(false))
                } else {
                    web_view?.loadUrl(web_url + "?isapp=1&ua=android&lan=${LanguageUtil.getSelectLanguage()}&country=${numberCode}&countryKeyCode=${dialingCode}" + PublicInfoDataService.getInstance().getOldContractUrl(true))
                }

            }

            else -> {
                showWeb(web_url)
            }
        }
        Log.d(TAG, "===webURL:$web_url===")
    }

    /*
     * web页面重定向标记
     */
    private var mIsRedirect = false

    private fun initWebView() {
        setCookie()

        //设置WebView属性，能够执行Javascript脚本
        web_view.settings.javaScriptEnabled = true
        web_view.canGoBack()
        //网页加载时不加载图片，等网页加载完成时再开启
        //web_view.settings.blockNetworkImage = true
        //不使用缓存
        web_view.settings.cacheMode = WebSettings.LOAD_NO_CACHE //LOAD_NO_CACHE
        web_view.settings.domStorageEnabled = true //开启本地DOM存储,解决加载一些链接出现空白页面的现象
        web_view.settings.allowContentAccess = true
        // 设置可以支持缩放
        web_view.settings.setSupportZoom(true)
        //设置出现缩放工具
        web_view.settings.builtInZoomControls = true
        //适应webview
        web_view.settings.useWideViewPort = true
        web_view.settings.loadWithOverviewMode = true
        web_view.settings.javaScriptCanOpenWindowsAutomatically = true
        //fix在Android5.0及以上版本，系统默认禁用mixed content, 加载一些https资源时安全证书不被认可的问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web_view.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            web_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            web_view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // 加快HTML网页加载完成速度
        if (Build.VERSION.SDK_INT >= 19) {
            web_view.settings.loadsImagesAutomatically = true
        } else {
            web_view.settings.loadsImagesAutomatically = false
        }

        // 开启Application H5 Caches 功能
        web_view.settings.setAppCacheEnabled(true)
        DWebView.setWebContentsDebuggingEnabled(true)
        web_view.disableJavascriptDialogBlock(false)
        web_view.setDownloadListener { s, s2, s3, s4, l ->
            LogUtil.e(TAG, "setDownloadListener ${s}")
            if (s.isNotEmpty()) {
                loadImage(s)
            }
        }
        web_view.addJavascriptObject(JsApi(this), null)
        web_view.addJavascriptObject(JsEchoApi(), "exchange")
        //      打开内置浏览器
        web_view.addJavascriptInterface(jsLoginHandler(this, web_view, this), "jsLoginHandler")
        web_view.setWebViewClient(object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                mIsRedirect = false
                super.onPageStarted(view, url, favicon)
                indicator?.start()
            }



            //解决无法调用拨打电话问题,还要重写下面这个重载函数
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.i(TAG, "shouldOverrideUrlLoading==url is $url,view is $view")
                if (null == url)
                    return true

                if (url.startsWith("mailto:")) {
                    //Handle mail Urls
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                } else if (url.startsWith("tel:")) {
                    //Handle telephony Urls
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                } else {
                    if (StringUtil.isHttpUrl(url)) {
                        mIsRedirect = true
                        view?.loadUrl(url)
                        // WebView加载该Url
                        return false
                    }
                }
//                // WebView不加载该Url
                return true
            }

            //解决无法调用拨打电话问题,还要重写下面这个重载函数
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                var uri = request?.url
                Log.v(TAG, "shouldOverrideUrlLoading==uri is $uri,view is $view")
                if (null == uri)
                    return true

                if (uri.toString().startsWith("mailto:")) {
                    //Handle mail Urls
                    startActivity(Intent(Intent.ACTION_SENDTO, uri))
                } else if (uri.toString().startsWith("tel:")) {
                    //Handle telephony Urls
                    startActivity(Intent(Intent.ACTION_DIAL, uri))
                } else {
                    if (StringUtil.isHttpUrl(uri.toString())) {
                        mIsRedirect = true
                        view?.loadUrl(uri.toString())
                        // WebView加载该Url
                        return false
                    }
                }
                // WebView不加载该Url
                return true
            }


            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                LogUtil.d(TAG, "webChromeClient==onReceivedError")
                runOnUiThread {
                    activity_new_video_loading_image.visibility = View.GONE
//                    if (AppConfig.IS_DEBUG) {
//                        var msg = error?.description
//                        NToastUtil.showTopToastNet(this@ItemDetailActivity,false, msg?.toString())
//                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                LogUtil.d(TAG, "webChromeClient==onPageFinished")
                indicator?.complete()
                if (mIsRedirect) {
                    return
                }
                //html标签加载完成之后在加载图片内容
//                web_view.settings.blockNetworkImage = false
                runOnUiThread {
                    activity_new_video_loading_image.visibility = View.GONE
                }
            }

        })

        web_view.setWebChromeClient(object : WebChromeClient() {
            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                return super.onConsoleMessage(cm)
            }

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                return super.onJsAlert(view, url, message, result)
            }


            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            // Work on Android 4.4.2 Zenfone 5
            fun showFileChooser(filePathCallback: ValueCallback<Array<String>>,
                                acceptType: String, paramBoolean: Boolean) {


                // TODO Auto-generated method stub
            }

            //for  Android 4.0+
            fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {

                if (nFilePathCallback != null) {
                    nFilePathCallback?.onReceiveValue(null)
                }
                nFilePathCallback = uploadMsg
                if ("image/*" == acceptType) {
                    var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                        var photoFile: File? = null
                        try {
                            photoFile = createImageFile()
                            takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                        } catch (ex: IOException) {
                            Log.e("TAG", "Unable to create Image File", ex)
                        }

                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + photoFile.absolutePath
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile))
                        } else {
                            takePictureIntent = null
                        }
                    }
                    startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE)
                } else if ("video/*" == acceptType) {
                    val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    if (takeVideoIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takeVideoIntent, INPUT_VIDEO_CODE)
                    }
                }
            }

            @SuppressLint("NewApi")
            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                                           fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
                if (mFilePathCallback != null) {
                    mFilePathCallback?.onReceiveValue(null)
                }
                mFilePathCallback = filePathCallback
                val acceptTypes = fileChooserParams.acceptTypes
                if (acceptTypes[0] == "image/*") {
                    var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                        var photoFile: File? = null
                        try {
                            photoFile = createImageFile()
                            takePictureIntent!!.putExtra("PhotoPath", mCameraPhotoPath)
                        } catch (ex: IOException) {
                            Log.e("TAG", "Unable to create Image File", ex)
                        }

                        //适配7.0
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            if (photoFile != null) {
                                photoURI = FileProvider.getUriForFile(this@ItemDetailActivity,
                                        BuildConfig.APPLICATION_ID + ".fileProvider", photoFile!!)
                                takePictureIntent!!.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                takePictureIntent!!.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            }
                        } else {
                            if (photoFile != null) {
                                mCameraPhotoPath = "file:" + photoFile!!.absolutePath
                                takePictureIntent!!.putExtra(MediaStore.EXTRA_OUTPUT,
                                        Uri.fromFile(photoFile))
                            } else {
                                takePictureIntent = null
                            }
                        }
                    }
                    startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE)
                } else if (acceptTypes[0] == "video/*") {
                    val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    if (takeVideoIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takeVideoIntent, INPUT_VIDEO_CODE)
                    }
                }
                return true
            }

        })
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setCookie() {
        if (!StringUtil.checkStr(web_url)) {
            return
        }


        CookieSyncManager.createInstance(this)
        var cookie: CookieManager = CookieManager.getInstance()
        cookie.removeSessionCookie()
        cookie.setAcceptCookie(true)
        val baseApi = getDomain()
        syncCookie(baseApi, cookie)
        CookieSyncManager.getInstance().sync()

    }

    private fun syncCookie(domain: String, cookieManager: CookieManager) {
        try {
            val url = URL(domain)
            var host =  url.host
            if(!StringUtil.isDoMainIPUrl(host)){
                host = "." + host
            }
            val cookieDomain = "; Max-Age=3600; Domain=$host; Path = /"
            val cookie = "ex_token=" + UserDataService.getInstance().token + cookieDomain
            cookieManager.setCookie(domain, cookie)
            val uuid = SystemUtils.getUUID()
            val cookie1 = "device=" + uuid + cookieDomain
            cookieManager.setCookie(domain, cookie1)

            val cookie11 = "UUID-CU=" + uuid + cookieDomain
            cookieManager.setCookie(domain, cookie11)

            val cookieModel = "Mobile-Model-CU=" + SystemUtils.getSystemModel() + cookieDomain
            cookieManager.setCookie(domain, cookieModel)

            val cookieBuild = "Build-CU=" + PackageUtil.getVersionCode() + cookieDomain
            cookieManager.setCookie(domain, cookieBuild)

            val cookieNetwork = "Network-CU=" + NetworkUtils.getNetType() + cookieDomain
            cookieManager.setCookie(domain, cookieNetwork)

            val cookieVersionCode = "haveCallback=" + PackageUtil.getVersionCode() + cookieDomain
            cookieManager.setCookie(domain, cookieVersionCode)



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun showWeb(url: String?) {

        if (!StringUtil.checkStr(url))
            return
        val tempUrl = getCookieDomain(url!!)
        if (tempUrl.contains("baidu") == true) {
            web_view?.loadUrl(tempUrl)
        } else if (tempUrl.contains("?") == true) {
            web_view?.loadUrl(tempUrl + "&isapp=1" + PublicInfoDataService.getInstance().getOldContractUrl(false))
        } else {
            web_view?.loadUrl(tempUrl + "?isapp=1" + PublicInfoDataService.getInstance().getOldContractUrl(true))
        }

    }
    /**
     * 帮助中心
     */
    private fun getWK() {
        var disposabl = getMainModel().getCommonKV(null, MyNDisposableObserver(common_kv_type))
        addDisposable(disposabl)
    }


    /**
     * 获取公告详情
     */
    private fun getNoticeDetail(id: String) {
        var disposabl = getMainModel().getNoticeDetail(id, MyNDisposableObserver(notice_detail_type))
        addDisposable(disposabl)
    }

    /**
     * 用户体系首页
     */
    private fun getRoleIndex(id: String) {
        var disposabl = getMainModel().getRoleIndex(MyNDisposableObserver(role_index_type))
        addDisposable(disposabl)
    }


    val notice_detail_type = 1
    val common_kv_type = 2
    val role_index_type = 3

    private inner class MyNDisposableObserver(type: Int) : NDisposableObserver() {

        val reqType = type
        override fun onResponseSuccess(jsonObject: JSONObject) {
            LogUtil.d(TAG, "getNoticeDetail==jsonObject is $jsonObject")

            if (notice_detail_type == reqType) {
                activity_new_video_loading_image?.visibility = View.GONE
                showNoticeDetail(jsonObject)
            } else if (role_index_type == reqType) {
                activity_new_video_loading_image?.visibility = View.GONE
                showNoticeDetail(jsonObject)
            }else if (common_kv_type == reqType) {
                showCommonKV(jsonObject)

        }
        }

        override fun onResponseFailure(code: Int, msg: String?) {
            super.onResponseFailure(code, msg)
            activity_new_video_loading_image?.visibility = View.GONE
        }
    }

    private fun showNoticeDetail(jsonObject: JSONObject) {
        var data = jsonObject.optJSONObject("data")
        if (null == data || data.length() <= 0)
            return

        var html = data.optString("html")
        if (StringUtil.checkStr(html)) {
            web_view.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)// 解决乱码问题
        }
    }


    private fun showCommonKV(jsonObject: JSONObject) {
        var data = jsonObject.optJSONObject("data")
        if (null == data || data.length() <= 0)
            return

        var h5_url = data.optString("h5_url")
        if (StringUtil.checkStr(h5_url)) {
            showWeb(h5_url + "/noticeDetail?id=${web_url}" + "&type=cms&isapp=1&lan=" + LanguageUtil.getSelectLanguage() + "&au=android")
        }
    }

    override fun onDestroy() {
        clearWebview()
        super.onDestroy()

    }

    private fun clearWebview() {
        try {
            if (web_view != null) {
                // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
                // destory()
                val parent = web_view.getParent()
                if (parent != null) {
                    (parent as ViewGroup).removeView(web_view)
                }
                  if(web_view!=null){
//                      web_view.stopLoading()
//                      // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
//                      web_view.settings.javaScriptEnabled = false
//                      web_view.clearHistory()
//                      web_view.clearView()
//                      web_view.removeAllViews()
                      web_view.destroy()
                  }

            }
        }finally {

        }

    }


    class JsApi(var mContext: ItemDetailActivity) {
        companion object {
            var handlers: CompletionHandler<String>? = null
            var uploadExchangeHandlers: CompletionHandler<String>? = null
        }

        @JavascriptInterface
        fun exchangeInfo(args: Any, handler: CompletionHandler<String>) {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("exchange_token", UserDataService.getInstance().token)
                jsonObject.put("exchange_lan", JsonUtils.getLanguage())
                jsonObject.put("exchange_skin", PublicInfoDataService.getInstance().themeModeNew)
                handler.complete(jsonObject.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }


        @JavascriptInterface
        fun exchangeRouter(args: Any) {
            try {
                val jsonObject = JSONObject(args.toString())
                val routerName = jsonObject.optString("routerName")
                val symbol = jsonObject.optString("symbol")
                val type = jsonObject.optInt("type")
                val profitRate = jsonObject.optDouble("profit_rate")
                val winRateWeek = jsonObject.optDouble("win_rate_week")
                val winRate = jsonObject.optDouble("win_rate")
                val labe = jsonObject.optString("labe")
                val userName = jsonObject.optString("user_name")
                val instrumentId = jsonObject.optString("instrument_id")
                val side = jsonObject.optString("side")
                val kol_name = jsonObject.optString("kol_name")
                val avg_cost_px = jsonObject.optString("avg_cost_px")
                val rate = jsonObject.optString("rate")

                mContext.enter2Activity(routerName, symbol, type, profitRate, winRateWeek, winRate, labe, userName, instrumentId, side, kol_name, avg_cost_px, rate)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        /**
         * 上传图片
         */
        @JavascriptInterface
        fun exchangeUploadInfo(args: Any, handler: CompletionHandler<String>) {
            try {
                val jsonObject = JSONObject(args.toString())
                val routerName = jsonObject.optString("routerName")
                when (routerName) {
                    // 上传照片
                    "uploadImg" -> {
                        LogUtil.d("exchangeUploadInfo:", "======上传图片:$args===")
                        mContext.showBottomMenu(jsonObject.optString("index"))
                    }
                }

                handlers = handler
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        @JavascriptInterface
        fun uploadExchange(args: Any, handler: CompletionHandler<String>) {
            try {
                val jsonObject = JSONObject(args.toString())
                val routerName = jsonObject.optString("routerName")
                when (routerName) {
                    // 上传照片
                    "uploadImg" -> {
                        LogUtil.d("uploadExchange:", "======上传:$args===")
                    }
                }
                uploadExchangeHandlers = handler
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }


    fun enter2Activity(routerName: String, symbol: String, type: Int, profitRate: Double = 0.0, winRateWeek: Double = 0.0, winRate: Double = 0.0,
                       labe: String = "", userName: String = "", instrumentId: String = "", side: String = "", kol_name: String = "", avg_cost_px: String = "", rate: String = "") {
        when (routerName) {
            "login" -> {
                /**
                 * 登录
                 */
                LoginManager.checkLogin(mContext, true)
            }
            "bindGoogle" -> {
                /**
                 * 绑定谷歌
                 */
                startActivity(Intent(mContext, GoogleValidationActivity::class.java))

            }
            "bindPhone" -> {
                /**
                 * 绑定手机
                 */
                var intent = Intent()
                intent.setClass(this, BindMobileOrEmailActivity::class.java)
                intent.putExtra(BindMobileOrEmailActivity.VERIFY_TYPE, BindMobileOrEmailActivity.MOBILE_TYPE)
                intent.putExtra(BindMobileOrEmailActivity.BIND_OR_CHANGE, BindMobileOrEmailActivity.VALIDATION_BIND)
                startActivity(intent)
            }
            // singpass 不授权
            "singpasscancel" -> {
                NToastUtil.showTopToastNet(this@ItemDetailActivity, false, LanguageUtil.getString(this, "common_text_cancelkyc"))
            }
            // kyc完成认证
            "kyccomplete" -> {
                ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
            }
            // 模版1
            "choosekycfirst" -> {
                RealNameCertificationChooseCountriesActivity.enter(this, "+${dialingCode}", countryName, numberCode)
            }
            "idAuth" -> {
                /**
                 * 实名制认证
                 */

                if (UserDataService.getInstance()?.authLevel == 0) {
                    ArouterUtil.greenChannel(RoutePath.RealNameCertificaionSuccessActivity, null)
                } else {
                    ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                }
            }
            "setUp" -> {
                /**
                 * 增加收款方式
                 */
                var intent = Intent(this, OTCChangePaymentActivity::class.java)
                intent.putExtra(CHOOSE_PAYMENT, 3)
                intent.putExtra(SYMBOL_OPEN, 0)
                intent.putExtra(CHOOSE_PAYMENT_LIST, arrayListOf<String>())
                startActivity(intent)
            }
            "modifySettings" -> {
                /**
                 * 资金密码
                 */
                ArouterUtil.forwardModifyPwdPage(ParamConstant.SET_PWD, ParamConstant.FROM_OTC)
            }
            "personal_information" -> {
                /**
                 *个人资料
                 */
                if (LoginManager.checkLogin(this, true)) {
                    ArouterUtil.greenChannel(RoutePath.PersonalInfoActivity, null)
                }
            }
            "safe_set" -> {
                /**
                 *安全设置
                 */
                if (LoginManager.checkLogin(this, true)) {
                    ArouterUtil.navigation(RoutePath.SafetySettingActivity, null)
                }
            }
            "contract_transaction" -> {
                /**
                 * 去合约交易页面
                 */
                forwardContractTab()
                finish()
            }
            "contract_record" -> {
                /**
                 * 合约资金记录
                 */
                if (PublicInfoDataService.getInstance().isNewOldContract) {
                    ClContractAssetRecordActivity.show(this, symbol, type)
                } else {
                    SlContractAssetRecordActivity.show(this, symbol, type)
                }
                finish()
            }
            "personal" -> {
                /**
                 * 个人中心
                 */
                ArouterUtil.navigation(RoutePath.PersonalCenterActivity, null)
                finish()
            }

            "coinmap_trading" -> {
                /**
                 * 币对交易页
                 */
                var tabType = HomeTabMap.maps[HomeTabMap.coinTradeTab]
                homeTabSwitch(tabType, type, symbol)
                finish()
            }
            "real_name" -> {
                /**
                 *实名认证
                 */
                if (LoginManager.checkLogin(this, true)) {
                    //认证状态 0、审核中，1、通过，2、未通过  3未认证
                    when (UserDataService.getInstance().authLevel) {
                        0 -> {
                            NToastUtil.showTopToastNet(this@ItemDetailActivity, false, LanguageUtil.getString(this, "noun_login_pending"))


                        }
                        1 -> {
                            NToastUtil.showTopToastNet(this@ItemDetailActivity, true, LanguageUtil.getString(this, "personal_text_verified"))
                        }
                        /**
                         * 审核未通过
                         */
                        2 -> {
                            ArouterUtil.navigation(RoutePath.PersonalInfoActivity, null)
                        }

                        3 -> {
                            ArouterUtil.navigation(RoutePath.RealNameCertificationActivity, null)
                        }
                    }
                }
            }
            "native_close" -> {
                finish()
            }
            "market_etf" -> {
                LogUtil.e(TAG, "market_etf")
                var tabType = HomeTabMap.maps[HomeTabMap.marketTab]
                homeTabSwitch(tabType, 0, "")
                var messageEvent = MessageEvent(MessageEvent.market_switch_type)
                messageEvent.msg_content = "ETF"
                EventBusUtil.post(messageEvent)
                finish()
            }
            "personal_invitation" -> {
                LogUtil.e(TAG, "personal_invitation")
                if (!LoginManager.checkLogin(this, true)) {
                    return
                }
                ArouterUtil.navigation(RoutePath.ContractAgentActivity, null)
                finish()
            }
            "kolShare_dialog" -> {
                kolDialog = NewDialogUtils.webShare(this, object : NewDialogUtils.DialogWebViewShareListener {
                    override fun webviewSaveImage(view: View) {
                        val rxPermissions: RxPermissions = RxPermissions(this@ItemDetailActivity)
                        /**
                         * 获取读写权限
                         */
                        rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .subscribe { granted ->
                                    if (granted) {
                                        var bitmap = ScreenShotUtil.getScreenshotBitmap(view
                                                ?: return@subscribe)
                                        if (bitmap != null) {
                                            val saveImageToGallery = ImageTools.saveImageToGallery(this@ItemDetailActivity, bitmap)
                                            if (saveImageToGallery) {
                                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@ItemDetailActivity, "common_tip_saveImgSuccess"), true)
                                            } else {
                                                DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@ItemDetailActivity, "common_tip_saveImgFail"), false)
                                            }
                                        } else {
                                            DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@ItemDetailActivity, "common_tip_saveImgFail"), false)
                                        }
                                    } else {
                                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@ItemDetailActivity, "common_tip_saveImgFail"), false)
                                    }

                                }
                        kolDialog?.dismiss()
                    }

                    override fun confirmShare(view: View) {
                        val rxPermissions: RxPermissions = RxPermissions(this@ItemDetailActivity)
                        /**
                         * 获取读写权限
                         */
                        rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .subscribe { granted ->
                                    if (granted) {
                                        var bitmap = ScreenShotUtil.getScreenshotBitmap(view
                                                ?: return@subscribe)
                                        if (bitmap != null) {
                                            ShareToolUtil.sendLocalShare(mActivity, bitmap)
                                        }
                                    }

                                }
                        kolDialog?.dismiss()
                    }

                }, profitRate, winRateWeek, winRate, labe, userName)
            }
            "share_dialog" -> {
                getPositionList(side, kol_name, avg_cost_px, rate, symbol)
            }
        }
    }

    var kolDialog: TDialog? = null

    private fun getPositionList(side: String = "", kol_name: String = "", avg_cost_px: String = "", rate: String = "", symbol: String = "") {
        KolShareActivity.show(this, side, kol_name, avg_cost_px, rate, symbol)
    }


    /*
   * 首页底部tab跳转的处理
   */
    private fun homeTabSwitch(tabType: Int?, buyOrSell: Int = 0, symbol: String = "") {
        var msgEvent = MessageEvent(MessageEvent.hometab_switch_type)
        var bundle = Bundle()
        bundle.putInt(ParamConstant.homeTabType, tabType ?: 0)
        if (symbol.isNotEmpty()) {
            bundle.putInt(ParamConstant.transferType, buyOrSell)
            bundle.putString(ParamConstant.symbol, symbol)
        }
        msgEvent.msg_content = bundle
        EventBusUtil.post(msgEvent)
    }

    private fun forwardContractTab() {
        var messageEvent = MessageEvent(MessageEvent.contract_switch_type)
        EventBusUtil.post(messageEvent)
    }


    /**
     * 照片位置
     */
    var imageMenuDialog: TDialog? = null
    var indexList: ArrayList<String> = arrayListOf()

    fun showBottomMenu(index: String) {
        imageMenuDialog = NewDialogUtils.showBottomListDialog(this, arrayListOf(LanguageUtil.getString(this, "noun_camera_takeAlbum"), LanguageUtil.getString(this, "noun_camera_takephoto")), 0
                , object : NewDialogUtils.DialogOnclickListener {
            override fun clickItem(data: ArrayList<String>, item: Int) {
                indexList.add(index)
                when (item) {
                    0 -> {
                        imageTool?.openGallery(index)
                    }
                    1 -> {
                        imageTool?.openCamera(index)
                    }
                }
                imageMenuDialog?.dismiss()
            }

        })
    }

    fun getFileSize(file: File): Int {
        var size = 0
        if (file.exists()) {
            var fis: FileInputStream? = null
            fis = FileInputStream(file)
            size = fis.available()
        }
        return size
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != INPUT_FILE_REQUEST_CODE && requestCode != INPUT_VIDEO_CODE) {
            var index = indexList[0]
            indexList.removeAt(0)
            imageTool?.onAcitvityResult(requestCode, resultCode, data
            ) { scanBitmap, path ->
                Log.e("我是大小1",getFileSize(File(path)).toString())
                val bitmap = getDecodeAbleBitmap(path)
                val jsonObject = JSONObject()
                jsonObject.put("index", index)
                JsApi.uploadExchangeHandlers?.complete(jsonObject.toString())

                /**
                 * 默认图片是ARGB——8888,占4B
                 * byteCount
                 * allocationByteCount
                 * 都不准确
                 * 宽 * 高 * 景深 也不行
                 */
                LogUtil.d(TAG, "====getFileSize:${getFileSize(File(path)) / 1024f}=====")

                if (getFileSize(File(path)) / 1024f / 1024f > 8) {
                    NToastUtil.showTopToastNet(this@ItemDetailActivity, false, LanguageUtil.getString(this, "upload_image_limit"))
                    val jsonObject = JSONObject().apply {
                        put("code", "-1")
                        put("msg", "")
                        put("index", index)
                    }
                    JsApi.handlers?.complete(jsonObject.toString())
                    return@onAcitvityResult
                }

                if (PublicInfoDataService.getInstance().getUploadImgType(null) == "1") {
                    getImageToken(operate_type = "1")
                    LogUtil.d(TAG, "=====上传图片:阿里云======")
                    val uploadHelper = UploadHelper.uploadImage(path, imageTokenBean.AccessKeyId, imageTokenBean.AccessKeySecret, imageTokenBean.bucketName,
                            imageTokenBean.ossUrl, imageTokenBean.SecurityToken, imageTokenBean.catalog)
                    val substring = uploadHelper.substring(uploadHelper.indexOf(imageTokenBean.catalog))

                    val jsonObject = JSONObject().apply {
                        put("filename", substring)
                        /**
                         * 给H5完整的路径
                         */
                        put("filenameStr", uploadHelper.indexOf(imageTokenBean.catalog))
                        put("index", index)

                    }
                    JsApi.handlers?.complete(jsonObject.toString())
                } else {
                    Log.e("我是大小2",(bitmap!!.getRowBytes() * bitmap.getHeight()).toString())
                    LogUtil.d(TAG, "=====上传图片:服务器======")
                    val bitmap2Base64 = imageTool?.bitmap2Base64(bitmap)
                    uploadImg(bitmap2Base64 ?: return@onAcitvityResult, index)
                }
            }
            return
        } else {
            var results: Array<Uri>? = null
            var mUri: Uri? = null
            if (resultCode == Activity.RESULT_OK && requestCode == INPUT_FILE_REQUEST_CODE) {
                if (data == null) {
                    val intentData = handerOpenCamera()
                    results = intentData.first
                    mUri = intentData.second
                } else {
                    val nUri = data.data
                    if (nUri != null) {
                        mUri = nUri
                        results = arrayOf(nUri)
                    } else {
                        val intentData = handerOpenCamera()
                        results = intentData.first
                        mUri = intentData.second
                    }
                }
            } else if (resultCode == Activity.RESULT_OK && requestCode == INPUT_VIDEO_CODE) {
                mUri = data?.getData()
                results = arrayOf(mUri!!)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                nFilePathCallback?.onReceiveValue(mUri)
                nFilePathCallback = null
                return
            } else {
                if (mFilePathCallback == null) {
                    return
                }
                mFilePathCallback?.onReceiveValue(results)
                mFilePathCallback = null
                return
            }
        }

    }


    /**
     * 新接口 获取token 图片
     */
    var imageTokenBean: ImageTokenBean = ImageTokenBean()

    fun getImageToken(operate_type: String = "1") {
        HttpClient.instance.getImageToken(operate_type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ImageTokenBean>() {
                    override fun onHandleSuccess(t: ImageTokenBean?) {
                        t ?: return
                        imageTokenBean = t

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }

                })


    }


    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.webview_refresh_type == event.msg_type) {
            initLoadWebview()
        }

    }


    /**
     * 上传照片  旧接口
     */
    fun uploadImg(imageBase: String, index: String): String {
        var jsonObject: JSONObject? = null
        HttpClient.instance.uploadImg(imgBase64 = imageBase, name = index)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(t: JsonObject?) {
                        DisplayUtil.showSnackBar(window?.decorView, LanguageUtil.getString(this@ItemDetailActivity, "toast_upload_pic_suc"), isSuc = true)
                        if (t == null) return
                        val jsonObject = JSONObject(t.toString())
                        JsApi.handlers?.complete(jsonObject.toString())
                        LogUtil.d(TAG, "===上传成功:${t}==")
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.d(TAG, "======error:==" + code + ":msg+" + msg)
                        jsonObject = JSONObject()
                        jsonObject?.put("code", code.toString())
                        jsonObject?.put("msg", msg)
                        jsonObject?.put("index", index)
                        JsApi.handlers?.complete(jsonObject.toString())
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
        return jsonObject?.toString() ?: ""

    }

    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var nFilePathCallback: ValueCallback<Uri>? = null
    private var mCameraPhotoPath: String? = null
    val INPUT_FILE_REQUEST_CODE = 1
    val INPUT_VIDEO_CODE = 2
    private val REQUEST_CAMERA_CODE = 1
    private var photoURI: Uri? = null
    private fun getPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_CODE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* 前缀 */
                ".jpg", /* 后缀 */
                storageDir      /* 文件夹 */
        )
        mCameraPhotoPath = image.absolutePath
        return image
    }

    fun selected() {

        var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent!!.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.e("TAG", "Unable to create Image File", ex)
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile))
            } else {
                takePictureIntent = null
            }
        }
    }

    fun getCookieDomain(webUrl: String): String {
        val domain = webUrl.getDoMainByUrl()
        val newDomain = PublicInfoDataService.getInstance().newWorkURL
        Log.e(TAG, "${webUrl}  ${domain}   " + StringUtil.isDoMainUrl(domain) + " [] " + newDomain)
        if (StringUtil.isDoMainUrl(domain)) {
            // 是加速域名 替换域名
            val newWebUrl = Utils.returnSpeedUrlV2(newDomain,webUrl)
            Log.e(TAG, " newUrl ${newWebUrl} ")
            return newWebUrl
        }
        return webUrl
    }

    fun getDomain(): String {
        val domain = NetUrl.baseUrl()
        val newDomain = PublicInfoDataService.getInstance().getDomainPage(null)
        Log.e(TAG, "getDomain() ${domain}   " + StringUtil.isDoMainUrl(domain) + " [] " + newDomain)
        return if (StringUtil.isDoMainUrl(web_url)) {
            // 是加速域名 替换域名
            val newUrl = PublicInfoDataService.getInstance().newWorkURL.getHostByPublicUrl()
            Log.e(TAG, "加速域名 newUrl  ${newUrl} ")
            newUrl
        } else {
            if (newDomain.isNotEmpty()) {
                val newUrl = newDomain.getHostByPublicUrl()
                newUrl
            } else {
                val newUrl = domain.getHostByUrl()
                newUrl
            }
        }
    }

    fun canFinish() {
        if (cmdFinish()) {
            web_view.goBack()
        } else {
            finish()
        }
    }

    private fun cmdFinish(): Boolean {
        return web_view.canGoBack()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.e("jinlong", "keyCode:${keyCode}")
        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK && cmdFinish()) {
                web_view.goBack()
                return true
            } else {
                finish()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun loadImage(dataImage: String) {
        showLoadingDialog()
        val rxPermissions = RxPermissions(this)
        /**
         * 获取读写权限
         */
        rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted) {
                        Thread(Runnable {
                            mHandler.obtainMessage(SAVE_BEGIN).sendToTarget()
                            val bitmap = SystemUtils.base64ToPicture(dataImage)
                            if (bitmap != null) {
                                SystemV2Utils.saveImageToPhotos(this, bitmap, mHandler)
                            }
                        }).start()
                    }
                }

    }

    private val SAVE_SUCCESS = 0//保存图片成功
    private val SAVE_FAILURE = 1//保存图片失败
    private val SAVE_BEGIN = 2//开始保存图片
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SAVE_BEGIN -> {

                }
                SAVE_SUCCESS -> {
                    closeLoadingDialog()
                    ToastUtils.showToast(LanguageUtil.getString(this@ItemDetailActivity, "common_tip_saveImgSuccess"))
                }
                SAVE_FAILURE -> {
                    closeLoadingDialog()
                    ToastUtils.showToast(LanguageUtil.getString(this@ItemDetailActivity, "common_tip_saveImgFail"))
                }
            }
        }
    }

    private fun handerOpenCamera(): Pair<Array<Uri>?, Uri?> {
        var results: Array<Uri>? = null
        var mUri: Uri? = null
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                mUri = photoURI
                results = arrayOf<Uri>(mUri!!)
            } else {
                if (mCameraPhotoPath != null) {
                    mUri = Uri.parse(mCameraPhotoPath)
                    results = arrayOf(Uri.parse(mCameraPhotoPath))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(results, mUri)
    }

    private fun showFullScreen() {
        val params = window.attributes
        params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.attributes = params;
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private fun changeStatusBarStyle() {
        StatusBarUtil.setColorNoTranslucent(this, ColorUtil.getColor(R.color.white))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            layoutView?.fitsSystemWindows = false
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun changeTitleStyle() {

        val url = Uri.parse(web_url)
        val isNaStyle = url.getQueryParameter("navi_style")
        if (isNaStyle != null && isNaStyle == "transparent") {
            layout_na_back.visibility = View.VISIBLE
            v_title.visibility = View.GONE
            changeStatusBarStyle()
            QMUIStatusBarHelper.translucent(this)
            QMUIStatusBarHelper.setStatusBarDarkMode(this)
            val isTitle = url.getQueryParameter("navi_titleHide")
            if (isTitle != null && isTitle == "1") {
                na_tv_title.visibility = View.GONE
                na_tv_title?.text = ""
            } else {
                na_tv_title?.text = head_title
            }
        } else {
            layout_na_back.visibility = View.GONE
            v_title.visibility = View.VISIBLE
        }
    }


}


