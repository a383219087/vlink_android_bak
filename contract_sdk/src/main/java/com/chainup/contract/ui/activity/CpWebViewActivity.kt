package com.chainup.contract.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseActivity
import kotlinx.android.synthetic.main.activity_cp_web_view.*
import wendu.dsbridge.DWebView

class CpWebViewActivity : CpNBaseActivity() {

    override fun setContentView(): Int {
     return R.layout.activity_cp_web_view
    }

    private lateinit var kkWebView: DWebView
    private var mUrl: String = ""


    fun loadUrl(context: Context, url: String) {
        val intent = Intent(context, CpWebViewActivity::class.java)
        intent.putExtra("url", url)
        context.startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        img_back.setOnClickListener {
            if (kkWebView!!.canGoBack()) {
                kkWebView!!.goBack()
            } else {
                finish()
            }
        }
    }
    override fun loadData() {
        mUrl = intent.getStringExtra("url").toString()
        initWebView()
    }


    private fun initWebView() {
        kkWebView = DWebView(this)
        val settings = kkWebView!!.getSettings()
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        kkWebView!!.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)
        kkWebView!!.setWebViewClient(WebViewClient())
        kkWebView!!.setWebChromeClient(webChromeClient)
        layoutWebView.addView(kkWebView)
        kkWebView!!.loadUrl(mUrl)
    }

    private val webChromeClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            tv_title.setText(title)
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE)
            } else {
                progressBar.setVisibility(View.VISIBLE)
                progressBar.setProgress(newProgress)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && kkWebView != null && kkWebView!!.canGoBack()) {
            if (kkWebView!!.canGoBack()) {
                kkWebView!!.goBack()
            } else {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}