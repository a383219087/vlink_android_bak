package com.yjkj.chainup.util;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.yjkj.chainup.BuildConfig;
import com.yjkj.chainup.R;
import com.yjkj.chainup.app.ChainUpApp;
import com.yjkj.chainup.db.service.PublicInfoDataService;
import com.yjkj.chainup.new_version.view.OnSaveSuccessListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class Utils {


    public static void copyString(TextView textView) {
        if (textView == null) {
            return;
        }
        ClipboardManager cm = (ClipboardManager) textView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(null, textView.getText()));
//            UIUtils.showToast("????????????");
        }
    }


    public static int getScreemWidth(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;     // ????????????????????????
    }

    public static String getOrderType(Context context, int num) {
        String type = "";
        switch (num) {
            case 1:
                type = LanguageUtil.getString(context, "otc_text_orderWaitPay");
                break;
            case 2:
            case 6:
                type = LanguageUtil.getString(context, "otc_text_waitReceiveCoin");
                break;
            case 3:
            case 8:
                type = LanguageUtil.getString(context, "otc_text_orderComplete");
                break;
            case 4:
            case 9:
                type = LanguageUtil.getString(context, "filter_otc_cancel");
                break;
            case 5:
                type = LanguageUtil.getString(context, "otc_text_orderAppeal");
                break;
            case 7:
                type = LanguageUtil.getString(context, "otc_abnormal_orders");
                break;
            default:
                break;

        }
        return type;
    }


    public static String getOrderTypeSell(Context context, int num) {
        String type = "";
        switch (num) {
            case 1:
                type = LanguageUtil.getString(context, "otc_text_orderWaitMoney");
                break;
            case 2:
            case 6:
                type = LanguageUtil.getString(context, "otc_text_waitSendCoin");
                break;
            case 3:
            case 8:
                type = LanguageUtil.getString(context, "otc_text_orderComplete");
                break;
            case 4:
                type = LanguageUtil.getString(context, "filter_otc_cancel");
                break;
            case 5:
                type = LanguageUtil.getString(context, "otc_text_orderAppeal");
                break;
            case 7:
                type = LanguageUtil.getString(context, "otc_abnormal_orders");
                break;
            case 9:
                type = LanguageUtil.getString(context, "filter_otc_appealCancel");
                break;
            default:
                break;
        }
        return type;
    }

    /**
     * ????????????
     *
     * @param string
     */
    public static void copyString(String string) {
        ClipboardManager cm = (ClipboardManager) ChainUpApp.appContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(null, string));
        }
    }


    //??????????????????
    public static Bitmap blurBitmap(Bitmap bitmap, Context context) {
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs,
                Element.U8_4(rs));

        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        blurScript.setRadius(20.f);

        // Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        // Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        // recycle the original bitmap
        bitmap.recycle();

        // After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;
    }

    public static Bitmap createBlurBitmap(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        if (radius < 1) {
            return (null);
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;
        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int[] vmin = new int[Math.max(w, h)];
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }
        yw = yi = 0;
        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;
        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];
                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }


    public static Bitmap stringtoBitmap(String string) {
        //?????????????????????Bitmap??????
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static byte[] String2Byte(String string) {
        byte[] bytes = string.getBytes();
        return bytes;
    }


    /**
     * ?????????????????????
     *
     * @param context
     * @return
     */
    public static int[] getScreenDispaly(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();// ?????????????????????
        int height = windowManager.getDefaultDisplay().getHeight();// ?????????????????????
        int[] result = {width, height};
        return result;
    }


    public static void isShowPass(boolean isShow, ImageView imageView, EditText editText) {
        if (isShow) {
            imageView.setImageResource(R.drawable.visible);
            //????????????type?????????????????????TYPE_CLASS_TEXT???????????????????????????
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            imageView.setImageResource(R.drawable.hide);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        //??????????????????
        editText.setSelection(editText.length());
    }

    /*
     * ???????????????????????????????????????
     */
    public static void assetsHideShow(boolean isShow, TextView textView, String content) {
        if (isShow) {
            if (StringUtil.checkStr(content)) {
                textView.setText(content + "");
            } else {
                textView.setText("0");
            }
        } else {
            textView.setText("*****");
        }
    }


    /*
     * ???????????????????????????????????????,????????????????????????
     */
    public static void assetsHideShowJrLongData(boolean isShow, TextView textView, String content1, String content2) {
        if (isShow) {
            StringBuilder builder = new StringBuilder();
            builder.append(content1).append(content2);
            if (StringUtil.checkStr(content1) && StringUtil.checkStr(content2)) {
                SpannableString spannableString = new SpannableString(builder.toString());

                ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#4B5687"));
                spannableString.setSpan(span, 0, content1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

                AbsoluteSizeSpan relativeSizeSpanleft = new AbsoluteSizeSpan(28, true);


                spannableString.setSpan(relativeSizeSpanleft, 0, content1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

                StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);

                spannableString.setSpan(styleSpan, 0, content1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

                AbsoluteSizeSpan relativeSizeSpanRight = new AbsoluteSizeSpan(12, true);

                spannableString.setSpan(relativeSizeSpanRight, content1.length(), builder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);


                textView.setText(spannableString);
            } else {
                textView.setText("0");
            }
        } else {
            textView.setText("*****");
        }
    }

    public static void showAssetsSwitch(boolean isShow, ImageView imageView) {
        if (imageView == null) return;
        if (isShow) {
            imageView.setImageResource(R.drawable.visible);
        } else {
            imageView.setImageResource(R.drawable.hide);
        }
    }

    public static void showAssetsNewSwitch(boolean isShow, ImageView imageView) {
        if (imageView == null) return;
        if (isShow) {
            imageView.setImageResource(R.drawable.assets_visible);
        } else {
            imageView.setImageResource(R.drawable.assets_invisible);
        }
    }

    /*
     * ????????????????????????????????????
     */
    public static void assetsVisible(boolean isShow, View view) {
        view.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
    }

    public static String deleteHeader(String code) {
        int start = code.indexOf("<header>");
        int end = code.indexOf("</header>");
        if (start != 0 && end != 0 && start < end) {
            //?????????????????????????????????????????????????????????
            String content = code.substring(start, end + 9);
            code = code.replace(content, "");
        }
        return code;
    }


    public static String getVolUnit(float num) {

        int e = (int) Math.floor(Math.log10(num));

        if (e >= 8) {
            return "??????";
        } else if (e >= 4) {
            return "??????";
        } else {
            return "???";
        }
    }


    /**
     * ???assets ??????????????????
     *
     * @param context
     * @param fileName
     * @return
     */
    private String getCert(Context context, String fileName) {
        InputStream inputStream = null;
        String string = null;
        try {
            inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] byteArray = new byte[size];
            inputStream.read(byteArray);
            string = new String(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return string;
    }

    /**
     * ????????????
     */
    public static String formatDate(long time, String format) {
        DateFormat dateFormat2 = new SimpleDateFormat(format, Locale.getDefault());
        String formatDate = dateFormat2.format(time);
        return formatDate;
    }


    public static String formatDate(long time) {
        DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formatDate = dateFormat2.format(time);
        return formatDate;
    }




    /**
     * ??????????????????????????????
     *
     * @return
     */
    private static final int FAST_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;


    public synchronized static boolean isFastClick() {
        boolean flag = false;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) <= FAST_CLICK_DELAY_TIME) {
            return true;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

    public synchronized static boolean isFastClick(int time) {
        boolean flag = false;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) <= time) {
            return true;
        }
        lastClickTime = currentClickTime;
        return flag;
    }






    //????????????
    public static int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    //????????????
    public static int getMinute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    //?????????
    public static int getWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    //?????????
    public static int getWeek(int year, int moth, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, moth - 1, day);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    //?????????
    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    //?????????
    public static int getMoth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    //?????????
    public static int getDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DATE);
    }

    public static Date getDate(int year, int moth, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, moth - 1, day, hour, minute);
        return calendar.getTime();
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }


    public static void main(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            Date date = format.parse("2016-12-15 12");
            System.out.println(getHour(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public static String getPastDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 7);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        Log.e(null, result);
        return result;
    }

    public static String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = format.format(today);
        Log.e(null, result);
        return result;
    }

    public static Date parseServerTime(String serverTime) {
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINESE);
        Date date = null;
        try {
            date = sdf.parse(serverTime);
        } catch (Exception e) {
            date = new Date();
        }
        return date;
    }

    /**
     * ?????????path?????????bitmap????????????????????????????????????
     *
     * @param path ?????????????????????
     * @return ????????????????????????????????????
     */
    public static void saveBitmap(String path, OnSaveSuccessListener onSaveSuccessListener) {
        String compressdPicPath = "";

//      ???????????????????????????????????????????????????????????????????????????????????????
      /*  //???????????????????????????path??????bitmap?????????bitmap????????????????????????????????????100kb???????????????????????????
        // ?????????????????????????????????100k???options???????????????????????????????????????
        //??? ???????????????????????????options?????????????????????????????????????????????UI????????????????????????????????????????????????????????????bitmap
        Bitmap bitmap=BitmapFactory.decodeFile(path);*/
//      ???????????????????????????????????????????????????????????????????????????????????????
        Bitmap bitmap = decodeSampledBitmapFromPath(path, 720, 1280);
        if (bitmap == null) return;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /* options?????? ??????????????????100?????????????????????0????????????70????????????????????????70???????????????30%; */
        int options = 100;
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        while (baos.toByteArray().length / 1024 > 200) {
// ?????????????????????????????????????????????500kb????????????

            baos.reset();
            options -= 10;
            if (options < 11) {//???????????????????????????????????????200kb???options?????????????????????options<0??????????????????????????????
                // ???????????????????????????200kb??????????????????10???
                bitmap.compress(Bitmap.CompressFormat.PNG, options, baos);
                break;
            }
// ????????????options%?????????????????????????????????baos???
            bitmap.compress(Bitmap.CompressFormat.PNG, options, baos);
        }

        String mDir = Environment.getExternalStorageDirectory() + "/FNComman";
        File dir = new File(mDir);
        if (!dir.exists()) {
            dir.mkdirs();//?????????????????????????????????
        }
        String fileName = String.valueOf(System.currentTimeMillis());
        File file = new File(mDir, fileName + ".jpg");
        FileOutputStream fOut = null;

        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(baos.toByteArray());
            out.flush();
            out.close();
            onSaveSuccessListener.onSuccess(file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * ??????????????????????????????????????????????????????????????????OOM
     *
     * @param path
     * @param width  ????????????imageview?????????
     * @param height ????????????imageview?????????
     * @return
     */
    private static Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {

//      ????????????????????????????????????????????????????????????
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = caculateInSampleSize(options, width, height);
//      ??????????????????inSampleSize??????????????????(??????options???????????????????????? options.inSampleSize???????????????????????????????????????????????????oom??? )
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;

    }

    /**
     * ????????????????????????????????????????????????????????????SampleSize
     *
     * @param options
     * @param reqWidth  ????????????imageview?????????
     * @param reqHeight ????????????imageview?????????
     * @return
     * @compressExpand ???????????????????????????????????????????????????????????????????????????imageview???????????????????????????????????????
     */
    private static int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;

        if (width >= reqWidth || height >= reqHeight) {

            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(width * 1.0f / reqHeight);

            inSampleSize = Math.max(widthRadio, heightRadio);

        }

        return inSampleSize;
    }




    private static String getStringFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // ???????????? ????????????
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();// ????????????????????????????????????,??????????????????utf-8(?????????????????????)
        os.close();
        return state;
    }


    public static String getJSONLastNews(String path) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            InputStream json = conn.getInputStream();
            String str = getStringFromInputStream(json);
            return str;
        }
        return null;
    }




    public static InputStream read_user(String filename) throws Exception {
        FileInputStream inStream = ChainUpApp.appContext.openFileInput(filename);
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();//??????????????????????????????
        Log.i("jinlong", new String(data));
        InputStream is = new ByteArrayInputStream(data);
        outStream.close();
        inStream.close();
        return is;
    }

    public static String returnSpeedUrlV2(String url, String mainUrl) {
        String domain = getAPIInsideStringIP(mainUrl, url);
        return domain;
    }



    public static String returnAPIUrl(String url, boolean isApi,String type) {

        if (ChainUpApp.Companion.getUrl()!=null){
            String returnUrl ="";
            if (type.equals("baseUrl")){
                returnUrl= ChainUpApp.Companion.getUrl().getBaseUrl();
            }
            if (type.equals("contractSocketAddress")){
                returnUrl=  ChainUpApp.Companion.getUrl().getContractSocketAddress();
            }
            if (type.equals("contractUrl")){
                returnUrl=  ChainUpApp.Companion.getUrl().getContractUrl();
            }
            if (type.equals("httpHostUrlContractV2")){
                returnUrl=  ChainUpApp.Companion.getUrl().getHttpHostUrlContractV2();
            }
            if (type.equals("otcBaseUrl")){
                returnUrl=  ChainUpApp.Companion.getUrl().getOtcBaseUrl();
            }
            if (type.equals("otcSocketAddress")){
                returnUrl=  ChainUpApp.Companion.getUrl().getOtcSocketAddress();
            }
            if (type.equals("redPackageUrl")){
                returnUrl=  ChainUpApp.Companion.getUrl().getRedPackageUrl();
            }
            if (type.equals("socketAddress")){
                returnUrl=  ChainUpApp.Companion.getUrl().getSocketAddress();
            }
            if (type.equals("wssHostContractV2")){
                returnUrl=  ChainUpApp.Companion.getUrl().getWssHostContractV2();
            }

            return  returnUrl+(isApi?"/":"");

        }

        return  url;


    }




    public static <T> ArrayList<T> jsonToArrayList(String json, Class<T> clazz) {
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();
        ArrayList<JsonObject> jsonObjects = new Gson().fromJson(json, type);

        ArrayList<T> arrayList = new ArrayList<>();
        for (JsonObject jsonObject : jsonObjects) {
            arrayList.add(new Gson().fromJson(jsonObject, clazz));
        }
        return arrayList;
    }

    public static boolean checkDeviceHasNavigationBar2(Activity activity) {
        //????????????????????????????????????????????????(???????????????,???????????????????????????)??????????????????navigation bar
        boolean hasMenuKey = ViewConfiguration.get(activity)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            // ???????????????????????? true
            return true;
        }
        // ????????????????????? false
        return false;
    }


    public static String getAPIInsideStringIP(String str, String newUrl) {
        if (str.indexOf(".") < 0) {
            return "";
        }
        if (str.lastIndexOf("/") < 0) {
            return "";
        }
        URI url = URI.create(str);
        String urlHost = url.getHost();
        boolean isIp = StringUtil.isDoMainIPUrl(urlHost);
        boolean isNewIp = StringUtil.isDoMainIPUrl(newUrl);

        if (isIp && !isNewIp) {
            // ip ?????????????????????
            String prefix = str.substring(0, str.indexOf(":"));
            String path = "//";
            String companyId = str.substring(prefix.length() + path.length() + 1).split("/")[0];
            StringBuilder stringBuffer = new StringBuilder(prefix);
            stringBuffer.append("://");
            stringBuffer.append(companyId).append(".");
            stringBuffer.append(newUrl).append("/");
            String methodUrl = str.substring(str.indexOf(companyId));
            String urlPath = methodUrl.substring(methodUrl.indexOf("/") + 1);
            if (methodUrl.indexOf("/") + 1 != methodUrl.length()) {
                stringBuffer.append(urlPath);
            }

            Log.e("?????????????????????1", stringBuffer.toString());
            return stringBuffer.toString();

        } else if (isNewIp && !isIp) {
            // ?????????????????????ip
            String prefix = str.substring(0, str.indexOf(":"));
            String path = "//";
            int index = str.indexOf(path) + path.length();
            String companyId = str.substring(index, str.indexOf("."));
            String methodUrl = str.substring(index);
            String urlPath = methodUrl.substring(methodUrl.indexOf("/") + 1);
            StringBuilder stringBuffer = new StringBuilder(prefix);
            stringBuffer.append("://");
            stringBuffer.append(newUrl).append("/");
            stringBuffer.append(companyId).append("/");
            if (methodUrl.indexOf("/") + 1 != methodUrl.length()) {
                stringBuffer.append(urlPath);
            }
            Log.e("?????????????????????2", stringBuffer.toString());
            return stringBuffer.toString();
        } else {
//            if (urlHost.split("\\.").length == 3) {
//                Log.e("?????????????????????3", str.replace(urlHost.substring(urlHost.indexOf(".") + 1), newUrl));
//                return str.replace(urlHost.substring(urlHost.indexOf(".") + 1), newUrl);
//            }
             if (newUrl.contains("http://")||newUrl.contains("https://")){
                 return str.replace(urlHost, newUrl.split("//")[1]);
             }

            Log.e("?????????????????????4", str.replace(urlHost, newUrl));
            return str.replace(urlHost, newUrl);
        }
    }




    public static String netUrl(boolean isApi) {
        if (isApi) return PublicInfoDataService.getInstance().getNewWorkURL();
        else return PublicInfoDataService.getInstance().getNewWorkWSURL();
    }

}


