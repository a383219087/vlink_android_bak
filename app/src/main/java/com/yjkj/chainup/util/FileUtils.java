package com.yjkj.chainup.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.yjkj.chainup.app.ChainUpApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * @Author lianshangljl
 * @Date 2020-02-20-17:07
 * @Email buptjinlong@163.com
 * @description
 */
public class FileUtils {


    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";


    /**
     * sdcard
     */
    private static final String SD_ROOT = Environment.getExternalStorageDirectory().getPath();

    /**
     * app根目录
     */
    public static final String PICTURE_DIR = SD_ROOT + File.separator + ChainUpApp.appContext.getPackageName()
            + File.separator + "cer" + File.separator;

    private FileUtils() {
    }

    /**
     * 是否存在 SDCard
     *
     * @return 是否存在
     */
    public static boolean sdExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean initPictureDir() {
        if (!sdExist()) {
            return false;
        }
        File picFile = new File(PICTURE_DIR);
        boolean exists = picFile.exists();
        boolean mkdirs = picFile.mkdirs();
        return exists || mkdirs;
    }

    // 获取文件大小
    public static void deDuplication(File file) {
        if (file.exists()) {
            Log.d("fileUtil", "存在了：" + file.getPath());
            boolean isDelete = file.delete();
            Log.d("fileUtil", "删除文件结果：" + isDelete);
        } else {
            Log.d("fileUtil", "文件不存在：" + file.getPath());
        }
    }

    public static String getDownloadApkCachePath(Context context) {
        String appCachePath;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            appCachePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/ChainUp/";
        } else {
            appCachePath = context.getFilesDir().getAbsolutePath() + "/ChainUp/";

        }
        File file = new File(appCachePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return appCachePath;
    }

    public static File getCacheDirectory(Context context) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            Log.w("UpdateFun TAG", "Can't define system cache directory! The app should be re-installed.");
        }
        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w("UpdateFun TAG", "Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Log.i("UpdateFun TAG", "Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public static void downloadAdvert(Context mContext, String imageUrl) {
        LogUtil.e("LogUtils","开始下载图片 "+imageUrl);
        Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> e) throws Exception {
                //通过gilde下载得到file文件,这里需要注意android.permission.INTERNET权限
                e.onNext(Glide.with(mContext)
                        .load(imageUrl)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get());
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        //获取到下载得到的图片，进行本地保存
                        File chche = mContext.getCacheDir();
                        //第二个参数为你想要保存的目录名称
                        File appDir = new File(chche, "advert");
                        if (!appDir.exists()) {
                            appDir.mkdirs();
                        }
                        String fileName = System.currentTimeMillis() + ".jpg";
                        File destFile = new File(appDir, fileName);
                        //把gilde下载得到图片复制到定义好的目录中去
                        copy(file, destFile);
                        LogUtil.e("LogUtils","下载成功 "+destFile.getPath());

                    }

                });
    }

    /**
     * 复制文件
     *
     * @param source 输入文件
     * @param target 输出文件
     */
    public static void copy(File source, File target) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(source);
            fileOutputStream = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            while (fileInputStream.read(buffer) > 0) {
                fileOutputStream.write(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
