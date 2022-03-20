package com.yjkj.chainup.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.yjkj.chainup.app.ChainUpApp;
import com.yjkj.chainup.manager.LanguageUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 获取屏幕当前页面的截图
 * Created by ykn on 2018/4/17.
 */

public class ScreenShotUtil {

    /**
     * 当超过一屏时，截取scrollview的屏幕
     *
     * @param scrollView
     * @return
     */
    public static Bitmap getBitmapByView(Context context, ScrollView scrollView, int resourceId) {
        int childHeight = 0;
        Paint paint = new Paint();
        Matrix matrix = new Matrix();
        // 获取scrollview实际高度
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            childHeight += scrollView.getChildAt(i).getHeight();
        }
        // 创建对应大小的bitmap
        Bitmap bitmap = Bitmap.createBitmap(scrollView.getWidth(), childHeight,
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        // 设置背景
        BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(resourceId);
        Bitmap backgroundBitmap = bitmapDrawable.getBitmap();
        matrix.postScale(0.8f, 1f);
        canvas.drawBitmap(backgroundBitmap, matrix, paint);
        scrollView.draw(canvas);
        Log.d("yxy", "getBitmapByView: " + bitmap.getByteCount());
        return compressImage(bitmap, Bitmap.CompressFormat.JPEG);
    }

    /**
     * 当不超过一屏时，可调用此方法把当前view上显示的内容转化成bitmap
     *
     * @param view 需要获取的图片的view
     * @return 返回bitmap
     */
    public static Bitmap getScreenshotBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();  //启用DrawingCache并创建位图
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache()); //创建一个DrawingCache的拷贝，因为DrawingCache得到的位图在禁用后会被回收
        view.setDrawingCacheEnabled(false);  //禁用DrawingCahce否则会影响性能
        return bitmap;
    }

    /**
     * 压缩图片
     *
     * @param image
     * @return
     */
    private static Bitmap compressImage(Bitmap image, Bitmap.CompressFormat type) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Log.d("yxy", "compressImage0: " + image.getByteCount());
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(type, 100, baos);
        Log.d("yxy", "compressImage1: " + baos.toByteArray().length);
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    //使用IO流将bitmap对象存到本地指定文件夹
    public static void saveMyBitmap(final Bitmap bitmap,Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String filePath = Environment.getExternalStorageDirectory().getPath();
                File file = new File(filePath + "/DCIM/Camera/" + System.currentTimeMillis() + ".png");
                try {
                    file.createNewFile();

                    FileOutputStream fOut = null;
                    fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);


                    Message msg = Message.obtain();
                    msg.obj = file.getPath();
                    handler.sendMessage(msg);
                    //Toast.makeText(PayCodeActivity.this, "保存成功", Toast.LENGTH_LONG).show();

                    NToastUtil.showTopToastNet(activity,true, LanguageUtil.getString(activity, "common_tip_saveImgSuccess"));
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String picFile = (String) msg.obj;
            String[] split = picFile.split("/");
            String fileName = split[split.length - 1];
            try {
                MediaStore.Images.Media.insertImage(ChainUpApp.appContext
                        .getContentResolver(), picFile, fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 最后通知图库更新
            ChainUpApp.appContext.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"
                    + picFile)));
            Toast.makeText(ChainUpApp.appContext, "图片保存图库成功", Toast.LENGTH_LONG).show();

        }
    };


//将要存为图片的view传进来 生成bitmap对象

    public static Bitmap createViewBitmap(View v, int color) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                Bitmap.Config.ARGB_8888);
        bitmap = setBitmapBGColor(bitmap, color);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    /**
     * 设置位图的背景色
     *
     * @param bitmap 需要设置的位图
     * @param color  背景色
     */
    public static Bitmap setBitmapBGColor(Bitmap bitmap, int color) {
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                bitmap.setPixel(i, j, color);//将bitmap的每个像素点都设置成相应的颜色
            }
        }
        return bitmap;
    }


    /**
     * 拼接两个图片
     *
     * @param first
     * @param second
     * @return
     */
    public static Bitmap spliceBitmap(Context context, Bitmap first, Bitmap second) {
        int width = first.getWidth();
        int height = first.getHeight();
        Bitmap newSecond = compressImage(second, Bitmap.CompressFormat.PNG);
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(first, 0, 0, null);
        canvas.drawBitmap(newSecond, 0, first.getHeight() - second.getHeight(), null);
        return result;
    }


    /**
     * 拨打电话（跳转到拨号界面，用户手动点击拨打）
     *
     * @param phoneNum 电话号码
     */
    public static void diallPhone(Context context, String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        context.startActivity(intent);
    }


    public static Bitmap capture(Activity activity) {
        activity.getWindow().getDecorView().setDrawingCacheEnabled(true);
        Bitmap bmp = activity.getWindow().getDecorView().getDrawingCache();
        return bmp;
    }
}
