package com.yjkj.chainup.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSONObject;
import com.jaeger.library.StatusBarUtil;
import com.yjkj.chainup.R;
import com.yjkj.chainup.app.ChainUpApp;
import com.yjkj.chainup.db.constant.ParamConstant;
import com.yjkj.chainup.db.constant.RoutePath;
import com.yjkj.chainup.db.service.UserDataService;
import com.yjkj.chainup.new_version.activity.asset.CaptureActivity;
import com.yjkj.chainup.new_version.view.UdeskWebChromeClient;
import com.yjkj.chainup.util.MD5Util;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Route(path = RoutePath.ChatWebViewActivity)
public class ChatWebViewActivity extends AppCompatActivity {
    private WebView mwebView;
    UdeskWebChromeClient udeskWebChromeClient;
    String url = "";
    String vip = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_webview);
        url = getIntent().getStringExtra(ParamConstant.URL_4_SERVICE);
//        url = "http://8.219.176.94/#/";
        vip = getIntent().getStringExtra(ParamConstant.homeTabType);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.red));
        initViews();
        loginById(UserDataService.getInstance().getUserInfo4UserId(), vip);

    }

    private void initViews() {
        try {
            udeskWebChromeClient = new UdeskWebChromeClient(this, () -> finish());
            mwebView = (WebView) findViewById(R.id.webview);
//         findViewById(R.id.back).setOnClickListener(view -> finish());
            settingWebView(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            mwebView.removeAllViews();
            mwebView.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        url = getIntent().getStringExtra(ParamConstant.URL_4_SERVICE);
        initViews();
    }

    @SuppressLint("NewApi")
    private void settingWebView(String url) {

        //????????????????????????????????????????????????????????????
        mwebView.requestFocusFromTouch();
        mwebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mwebView.setScrollbarFadingEnabled(false);

        final WebSettings settings = mwebView.getSettings();
        settings.setJavaScriptEnabled(true);  //??????js
        //  ????????????????????????????????????
        settings.setUseWideViewPort(true); //????????????????????????webview?????????
        settings.setLoadWithOverviewMode(true); // ????????????????????????

        //???setSupportZoom???false?????????WebView?????????????????????????????????????????????????????????
        settings.setSupportZoom(true);  //????????????????????????true??????setBuiltInZoomControls????????????
        settings.setBuiltInZoomControls(true); //??????????????????????????????
        settings.supportMultipleWindows();  //?????????

        settings.setAllowFileAccess(true);  //????????????????????????
        settings.setNeedInitialFocus(true); //???webview??????requestFocus??????webview????????????
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setBlockNetworkImage(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true); //????????????JS???????????????
        //??????????????????
        settings.setDefaultTextEncodingName("UTF-8");
        // ??????????????????
        settings.setDisplayZoomControls(false);
        /**
         *  Webview?????????5.0???????????????????????????????????????????????????
         *  ?????????5.0??????????????????????????????http???https???????????????????????????webview???????????????????????????????????????
         */
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        //        dealJavascriptLeak();
        settings.setLoadsImagesAutomatically(true);  //????????????????????????

        settings.setDomStorageEnabled(true); //??????DOM Storage

        mwebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // ???????????????????????????????????????????????????????????????????????????????????????????????????
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

    private class MyJavascriptInterface1 {

        private Context context;

        public MyJavascriptInterface1(Context context) {
            this.context = context;
        }

        /**
         * ?????????
         *
         * @return
         */
        @JavascriptInterface
        public void qrcode() {
            Intent intent = new Intent(context, CaptureActivity.class);
            startActivityForResult(intent, 0x1111);

        }

        /**
         * ????????????
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
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0x1111) {
                String res =data.getStringExtra(CaptureActivity.SCAN_RESULT);
                this.runOnUiThread(() -> mwebView.evaluateJavascript("javascript:getQRcodeValue('" + res + "')", s -> mwebView.reload()));
//                if (res!=null&&!res.isEmpty()){
//                    if (res.startsWith("M")) {
//                        mwebView.loadUrl(newUrl+res);
//                    }
//
//
//                }


            }
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        mwebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mwebView.onResume();
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
//            try {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, data.toJSONString());
            Request request = new Request.Builder()
                    .url(ChainUpApp.Companion.getUrl().getChatApiUrl() + "/api/lottery/PostRegistIMUserId")
                    .post(body)
                    .build();

            Call call = client.newCall(request);
            Response response = null;
            try {
                response = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (response != null) {
                if (response.isSuccessful()) {

                    String jsonString = null;
                    try {
                        jsonString = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(jsonString);
                    JSONObject jsonObject = JSONObject.parseObject(jsonString);
                    String user_id = jsonObject.getJSONObject("Data").getString("user_id");
                    String key = jsonObject.getJSONObject("Data").getString("key");
                    Timestamp d = new Timestamp(System.currentTimeMillis());
                    String timestamp = String.valueOf(d.getTime());

                    System.out.println(timestamp);


                    String code = user_id + key + timestamp;
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
                    object1.put("token", UserDataService.getInstance().getToken());
                    JSONObject data1 = new JSONObject();
                    data1.put("Data", object1);

                    MediaType JSON1 = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body1 = RequestBody.create(JSON1, data1.toJSONString());
                    request = new Request.Builder()
                            .url(ChainUpApp.Companion.getUrl().getChatApiUrl() + "/api/lottery/PostIMLoginCode")//????????????
                            .post(body1).build();


                    call = client.newCall(request);

                    try {
                        response = call.execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    JSONObject mine = null;
                    try {
                        mine = JSONObject.parseObject(response.body().string()).getJSONObject("Data").getJSONObject("mine");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    String js = "window.localStorage.setItem('USERS_KEY','" + mine.toJSONString() + "');";

                    this.runOnUiThread(() -> mwebView.evaluateJavascript(js, s -> mwebView.reload()));


                } else {
                    Log.e("-------------------------", "shib");
                }
            }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        }).start();
    }

}

