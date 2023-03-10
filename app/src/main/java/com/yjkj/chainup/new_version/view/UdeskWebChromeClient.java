package com.yjkj.chainup.new_version.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.ProgressBar;


/**
 * Created by user on 2016/12/15.
 */

public class UdeskWebChromeClient extends WebChromeClient {
    private Activity mContext;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    ICloseWindow closeWindow = null;
    ProgressBar mProgressBar = null;



    public UdeskWebChromeClient(Activity context, ICloseWindow closeWindow) {
        mContext = context;
        this.closeWindow = closeWindow;
    }
    public UdeskWebChromeClient(Activity context, ICloseWindow closeWindow, ProgressBar mProgressBar) {
        mContext = context;
        this.closeWindow = closeWindow;
        this.mProgressBar = mProgressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if(mProgressBar!=null){
            if (newProgress==100){
                mProgressBar.setVisibility(View.GONE);
            }else {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(newProgress);
            }

        }


        super.onProgressChanged(view, newProgress);
    }


    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> valueCallback) {
        uploadMessage = valueCallback;
        openImageChooserActivity();
    }

    // For Android  >= 3.0
    public void openFileChooser(ValueCallback valueCallback, String acceptType) {
        uploadMessage = valueCallback;
        openImageChooserActivity();
    }

    //For Android  >= 4.1
    public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
        uploadMessage = valueCallback;
        openImageChooserActivity();
    }

    // For Android >= 5.0
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        uploadMessageAboveL = filePathCallback;
        openImageChooserActivity();
        return true;
    }

    //???????????????????????????????????????activty?????????????????????ICloseWindow  ???????????????????????????
    @Override
    public void onCloseWindow(WebView window) {
        if (closeWindow !=null){
//            closeWindow.closeActivty();
        }
//        super.onCloseWindow(window);

    }

    @Override
    //??????
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
        quotaUpdater.updateQuota(requiredStorage*2);
    }

    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        Log.e("h5??????log", String.format("%s -- From line %s of %s", message, lineNumber, sourceID));
    }




    private void openImageChooserActivity() {

        Intent i=createFileItent();
        mContext.startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * ?????????????????????intent
     * @return
     */
    private Intent createFileItent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
//        intent.setType("image/*");
//        Intent   intent = new Intent(
//                Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setDataAndType(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                "image/*");
        return intent;
    }


    public  void onActivityResult(int requestCode, int resultCode, Intent data){

            if (requestCode == FILE_CHOOSER_RESULT_CODE) {
                if (null == uploadMessage&& null == uploadMessageAboveL){
                    return;
                }
                //???????????? ?????????????????????????????? ???????????????????????????????????????
                if (data == null){
                    if (uploadMessage != null){
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }
                    if (uploadMessageAboveL != null){
                        uploadMessageAboveL.onReceiveValue(null);
                        uploadMessageAboveL = null;
                    }
                    return;
                }
                if (uploadMessageAboveL != null) {//5.0??????
                    onActivityResultAboveL(requestCode, resultCode, data);
                }else if(uploadMessage != null) {
                    if (data != null &&  resultCode == Activity.RESULT_OK ){
                        Uri result = data.getData();
                        Log.e("xxx","5.0-result="+result);
                        uploadMessage.onReceiveValue(result);
                        uploadMessage = null;
                    }

                }


            }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        Log.e("xxx","5.0+ ?????????");
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null){
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

}
