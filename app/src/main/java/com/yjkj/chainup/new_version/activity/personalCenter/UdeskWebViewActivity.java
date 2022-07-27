package com.yjkj.chainup.new_version.activity.personalCenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.jaeger.library.StatusBarUtil;
import com.yjkj.chainup.R;
import com.yjkj.chainup.db.constant.ParamConstant;
import com.yjkj.chainup.db.constant.RoutePath;
import com.yjkj.chainup.db.service.PublicInfoDataService;
import com.yjkj.chainup.new_version.view.UdeskWebChromeClient;

@Route(path = RoutePath.UdeskWebViewActivity)
public class UdeskWebViewActivity extends AppCompatActivity {
    private WebView mwebView;
    UdeskWebChromeClient udeskWebChromeClient;
    String url = "";
    boolean clean = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_webview);
        url = getIntent().getStringExtra(ParamConstant.URL_4_SERVICE);
//        url = "http://8.219.40.68/wallet/#/pages/index/index?token=3f941afdb53a3cac949d49fb2ff2e0314f4ce8f88a5e48ea864ccc0b0ead3541";
//        url = "file:///android_asset/test.html";
        clean = getIntent().getBooleanExtra(ParamConstant.DEFAULT_NAME_ERROR,false);
        setBarColor(PublicInfoDataService.getInstance().getThemeMode());
        initViews();
    }

    private void initViews() {
        try {
            udeskWebChromeClient = new UdeskWebChromeClient(this, this::finish);
            mwebView = (WebView) findViewById(R.id.webview);
            settingWebView(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @SuppressLint("NewApi")
    private void settingWebView(String url) {

        //支持获取手势焦点，输入用户名、密码或其他
        mwebView.requestFocusFromTouch();
        mwebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mwebView.setScrollbarFadingEnabled(false);

        final WebSettings settings = mwebView.getSettings();
        settings.setJavaScriptEnabled(true);  //支持js
        //  设置自适应屏幕，两者合用
        settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        settings.setCacheMode(clean?WebSettings.LOAD_NO_CACHE:WebSettings.LOAD_DEFAULT);
        //若setSupportZoom是false，则该WebView不可缩放，这个不管设置什么都不能缩放。
        settings.setSupportZoom(true);  //支持缩放，默认为true。是setBuiltInZoomControls的前提。
        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。
        settings.supportMultipleWindows();  //多窗口
        settings.setUserAgentString("User-Agent:Android");
        settings.setAllowFileAccess(true);  //设置可以访问文件
        settings.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点

        settings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        //设置编码格式
        settings.setDefaultTextEncodingName("UTF-8");
        // 关于是否缩放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            settings.setDisplayZoomControls(false);
        }
        /**
         *  Webview在安卓5.0之前默认允许其加载混合网络协议内容
         *  在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        }
//        dealJavascriptLeak();
        settings.setLoadsImagesAutomatically(true);  //支持自动加载图片

        settings.setDomStorageEnabled(true); //开启DOM Storage
        mwebView.setDownloadListener((url1, userAgent, contentDisposition, mimetype, contentLength) -> {
            // 监听下载功能，当用户点击下载链接的时候，直接调用系统的浏览器来下载
            Uri uri = Uri.parse(url1);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        mwebView.setWebChromeClient(udeskWebChromeClient);

        mwebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                UdeskWebViewActivity.this.finish();
            }



            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);
                return true;
            }
        });
        mwebView.addJavascriptInterface(new MyJavascriptInterface1(this), "Android");
        mwebView.loadUrl(url);
        mwebView.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && mwebView.canGoBack()) {
                    mwebView.goBack();
                    return true;
                }
            }
            return false;
        });
    }

//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    private void dealJavascriptLeak() {
//        try {
//            mwebView.removeJavascriptInterface("searchBoxJavaBridge_");
//            mwebView.removeJavascriptInterface("accessibility");
//            mwebView.removeJavascriptInterface("accessibilityTraversal");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private class MyJavascriptInterface1 {

        private Context context;

        public MyJavascriptInterface1(Context context) {
            this.context = context;
        }

        /**
         * 返回主页
         *
         */
        @JavascriptInterface
        public void finishActivity() {
          finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        udeskWebChromeClient.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mwebView.removeAllViews();
            mwebView.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置状态栏的颜色
     *
     *  0 是 白天模式，状态栏是白底黑字  1是夜间模式 状态栏是黑底白字
     */
    private void setBarColor(int index) {
        if (index == 0) {
            StatusBarUtil.setLightMode(this);
        } else {
            StatusBarUtil.setDarkMode(this);
        }


    }

}
