package com.yjkj.chainup.new_version.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.util.LanguageUtil
import kotlinx.android.synthetic.main.activity_inner_browser.*

class InnerBrowserActivity : NBaseActivity() {


    companion object {
        const val TITLE = "title"
        const val URL = "url"

        fun enter2(context: Context, title: String, url: String?) {
            var intent = Intent(context, InnerBrowserActivity::class.java)
            intent.putExtra(TITLE, title)
            intent.putExtra(URL, url)
            context.startActivity(intent)
        }

    }

    override fun setContentView() = R.layout.activity_inner_browser

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initWebView()
        initView()
        val url = intent.getStringExtra(URL) ?: ""
        val title = intent.getStringExtra(TITLE) ?: ""
        if (!TextUtils.isEmpty(title))
            v_title?.setTitle(title)
        web_view.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        //能够和js交互
        web_view.settings.javaScriptEnabled = true
        //缩放,设置为不能缩放可以防止页面上出现放大和缩小的图标
        web_view.settings.builtInZoomControls = false
        //缓存
        web_view.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        //开启DOM storage API功能
        web_view.settings.domStorageEnabled = true
        //开启application Cache功能
        web_view.settings.setAppCacheEnabled(false)
        web_view.settings.allowFileAccess = true
        web_view.settings.setSupportMultipleWindows(true)

        web_view.settings.blockNetworkImage = false//解决图片不显示
        web_view.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        web_view.webViewClient =object : WebViewClient(){
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                //忽略证书的错误继续Load页面内容，不会显示空白页面
                var builder = AlertDialog.Builder(view?.getContext());
                builder.setMessage(LanguageUtil.getString(this@InnerBrowserActivity, "base_error_prompt5"))
                builder.setPositiveButton(LanguageUtil.getString(this@InnerBrowserActivity, "common_text_btnConfirm")) { dialog, which -> handler?.proceed(); };

                builder.setNegativeButton(LanguageUtil.getString(this@InnerBrowserActivity, "common_text_btnCancel")) { dialog, which -> handler?.cancel() };

                var dialog = builder.create()
                dialog.show()
            }
        }

        web_view.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                if (newProgress == 100) {
                    progress_bar.visibility = View.GONE
                } else {
                    progress_bar.visibility = View.VISIBLE
                    progress_bar.progress = newProgress
                }
                super.onProgressChanged(view, newProgress)
            }
        }

        web_view.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && web_view.canGoBack()) {
                    web_view.goBack()
                    return@OnKeyListener true
                }
            }
            false
        })
    }

}
