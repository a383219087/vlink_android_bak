package com.yjkj.chainup.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSONObject;
import com.jaeger.library.StatusBarUtil;
import com.yjkj.chainup.R;
import com.yjkj.chainup.app.ChainUpApp;
import com.yjkj.chainup.db.constant.ParamConstant;
import com.yjkj.chainup.db.constant.RoutePath;
import com.yjkj.chainup.db.service.PublicInfoDataService;
import com.yjkj.chainup.db.service.UserDataService;
import com.yjkj.chainup.new_version.view.UdeskWebChromeClient;
import com.yjkj.chainup.util.MD5Util;

import java.sql.Timestamp;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Route(path = RoutePath.ChatWebViewActivity)
public class ChatWebViewActivity extends Activity {
    private WebView mwebView;
    UdeskWebChromeClient udeskWebChromeClient;
    String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_webview);
        url = getIntent().getStringExtra(ParamConstant.URL_4_SERVICE);
        setBarColor(PublicInfoDataService.getInstance().getThemeMode());
        initViews();
        loginById( UserDataService.getInstance().getUserInfo4UserId(),"");
    }

    private void initViews() {
        try {
            udeskWebChromeClient = new UdeskWebChromeClient(this, () -> finish());
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

        //若setSupportZoom是false，则该WebView不可缩放，这个不管设置什么都不能缩放。
        settings.setSupportZoom(true);  //支持缩放，默认为true。是setBuiltInZoomControls的前提。
        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。
        settings.supportMultipleWindows();  //多窗口

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

        mwebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // 监听下载功能，当用户点击下载链接的时候，直接调用系统的浏览器来下载
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        mwebView.setWebChromeClient(udeskWebChromeClient);
        mwebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                ChatWebViewActivity.this.finish();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage(getString(R.string.base_error_prompt5));
                builder.setPositiveButton(getString(R.string.common_text_btnConfirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });

                builder.setNegativeButton(getString(R.string.common_text_btnCancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);
                return true;
            }
        });
        mwebView.loadUrl(url);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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



    public void loginById(String id, String vip) {
        int randomnum = 0;
        randomnum = Integer.parseInt(id);
        JSONObject object = new JSONObject();
        object.put("user_name", randomnum);
        object.put("vip", vip);
        JSONObject data = new JSONObject();
        data.put("Data", object);
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, data.toJSONString());
                Request request = new Request.Builder()
                        .url(ChainUpApp.Companion.getUrl().getChatApiUrl() + "/api/lottery/PostRegistIMUserId")
                        .post(body)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.isSuccessful()) {

                    String jsonString = response.body().string();
// android.util.Log.e("-------------------------",jsonString.toString());
//System.out.println(jsonString);
                    JSONObject jsonObject = JSONObject.parseObject(jsonString);
                    String user_id = jsonObject.getJSONObject("Data").getString("user_id");
                    String key = jsonObject.getJSONObject("Data").getString("key");
                    Timestamp d = new Timestamp(System.currentTimeMillis());
                    String timestamp = String.valueOf(d.getTime());
// android.util.Log.e("-------------------------",timestamp.toString());
// System.out.println(timestamp);

//生层code code = md5(sort(user_id+key+timestamp))

                    String code = user_id + key + timestamp;
                    byte bytes[] = code.getBytes();
                    ArrayList<Character> list = new ArrayList<Character>(code.length());
                    for (int i = 0; i < code.length(); i++) {
                        list.add(code.charAt(i));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        list.sort(Character::compare);
                    }
                    code = "";
                    for (Character ch : list) {
                        code += ch;
                    }
                    code = MD5Util.getMD5(code);
                    JSONObject object1 = new JSONObject();
                    object1.put("user_id", user_id);
                    object1.put("code", code);
                    object1.put("timestamp", timestamp);
                    JSONObject data1 = new JSONObject();
                    data1.put("Data", object1);

                    MediaType JSON1 = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body1 = RequestBody.create(JSON1, data1.toJSONString());
                    request = new Request.Builder()
                            .url(ChainUpApp.Companion.getUrl().getChatApiUrl()+ "/api/lottery/PostIMLoginCode")//访问连接
                            .post(body1).build();


                    call = client.newCall(request);

                    response = call.execute();

                    JSONObject mine = JSONObject.parseObject(response.body().string()).getJSONObject("Data").getJSONObject("mine");


                    String js = "window.localStorage.setItem('USERS_KEY','" + mine.toJSONString() + "');";

                    this.runOnUiThread(() -> mwebView.evaluateJavascript(js, s -> mwebView.reload()));


                }else {
                    Log.e("-------------------------","shib");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

}
