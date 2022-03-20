package com.yjkj.chainup.update;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yjkj.chainup.model.api.MainApiService;
import com.yjkj.chainup.util.AppUtils;
import com.yjkj.chainup.util.InstallRationale;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


public class ApkDownloadUtils {
    private static final String TAG = "DownloadUtils";
    private static final int DEFAULT_TIMEOUT = 15;
    private Retrofit retrofit;
    private ApkDownloadListener listener;
    private String baseUrl;
    private String downloadUrl;
    public ApkDownloadUtils(String baseUrl, ApkDownloadListener listener) {
        this.baseUrl = baseUrl;
        this.listener = listener;
        ApkDownloadInterceptor mInterceptor = new ApkDownloadInterceptor(listener);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(mInterceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
    /**
     * 开始下载
     *
     * @param url
     * @param filePath
     */
    public void download(@NonNull String url, final String filePath, Context mContext) {
        listener.onStartDownload();
        retrofit.create(MainApiService.class)
                .downloadAppFile(url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, InputStream>() {
                    @Override
                    public InputStream apply(ResponseBody responseBody) throws Exception {
                        return responseBody.byteStream();
                    }
                })
                .observeOn(Schedulers.computation()) // 用于计算任务
                .doOnNext(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {
                        writeFile(inputStream, filePath);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {
                        listener.onFinishDownload();
                        if (checkApk(mContext,new File(filePath))) {
                            Log.i("UpdateFun TAG", "APK路径:" + new File(filePath));
                            installPackage(mContext, new File(filePath));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        listener.onFail("下载失败");
                    }
                });
    }
    /**
     * 将输入流写入文件
     *
     * @param inputString
     * @param filePath
     */
    private void writeFile(InputStream inputString, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] b = new byte[1024];
            int len=0;
            while ((len=inputString.read(b)) != -1) {
                fos.write(b,0,len);
            }
            inputString.close();
            fos.close();
        } catch (FileNotFoundException e) {
            listener.onFail("FileNotFoundException");
        } catch (IOException e) {
            listener.onFail("IOException");
        }
    }


    private  boolean checkApk(Context context,File apkFile) {
        String apkName = AppUtils.INSTANCE.getAPKPackageName(context, apkFile.getAbsolutePath());
        String appName =  AppUtils.INSTANCE.getPackageName(context);
        if (apkName.equals(appName)) {
            Log.i("UpdateFun TAG", "apk检验:包名相同,安装apk");
            return true;
        } else {
            Log.i("UpdateFun TAG", String.format("apk检验:包名不同。该app包名:%s，apk包名:%s", appName, apkName));
            listener.onFail("包名不同,无法进行安装！");
            return false;
        }
    }

    private  void installPackage(Context context, File pathFile) {
        AndPermission.with(context)
                .install()
                .file(pathFile)
                .rationale(new InstallRationale())
                .onGranted(new Action<File>() {
                    @Override
                    public void onAction(File data) {
                    }
                })
                .onDenied(new Action<File>() {
                    @Override
                    public void onAction(File data) {
                    }
                })
                .start();
    }
}
