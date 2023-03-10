package com.chainup.contract.utils;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;


import com.chainup.contract.api.CpApiConstants;
import com.chainup.contract.app.CpMyApp;
import com.yjkj.chainup.manager.CpLanguageUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;

public class CpSystemUtils {

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return CpLanguageUtil.getSelectLanguage();
    }


    public static boolean isZh() {
        if (getSystemLanguage().equals("zh")) {
            return true;
        } else {
            return getSystemLanguage().equals("zh_CN");
        }
    }

    public static boolean isMn() {
        return getSystemLanguage().equals("mn_MN");
    }

    public static boolean isRussia() {
        return getSystemLanguage().equals("ru_RU");
    }

    public static boolean isKorea() {
        return getSystemLanguage().equals("ko_KR");
    }

    public static boolean isJapanese() {
        return getSystemLanguage().equals("ja_JP");
    }

    public static boolean isSpanish() {
        return getSystemLanguage().equals("es_ES");
    }

    public static boolean isVietNam() {
        return getSystemLanguage().equals("vi_VN");
    }

    public static boolean isTW() {
        return getSystemLanguage().equals("el_GR");
    }


    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return 语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取 当前网络状态
     *
     * @param context
     * @return
     */
    public static String getAPNType(Context context) {
        String netType = "4g";
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = "WIFI";// wifi
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager mTelephony = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE) {
                netType = "4G";// 4G
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    && !mTelephony.isNetworkRoaming()) {
                netType = "3G";// 3G
            } else {
                netType = "2G";// 2G
            }

        }
        return netType;
    }


    public static String requestSign(SortedMap<String, String> parameters, String key) {
        StringBuffer sb = new StringBuffer();
        StringBuffer sbkey = new StringBuffer();
        String characterEncoding = "UTF-8";
        // 所有参与传参的参数按照accsii排序（升序）
        Set<Map.Entry<String, String>> es = parameters.entrySet();
        Iterator<Map.Entry<String, String>> it = es.iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String k = entry.getKey();
            String v = entry.getValue();
            // 空值不传递，不参与签名组串
            if (!TextUtils.isEmpty(v)) {
                sb.append(k + "=" + v + "&");
                sbkey.append(k + "=" + v + "&");
            }
        }
        Log.e("字符串 {}", sb.toString());
        sbkey = sbkey.append("key=" + key);
        Log.e("字符串 {}", sbkey.toString());
        // MD5加密,结果转换为大写字符
        String sign = CpMD5Util.getMD5(sbkey.toString()).toUpperCase();
        Log.e(" MD5加密值 {}:", sign);
        return sign;
    }

    /**
     * 跳转到拨号界面，同时传递电话号码
     *
     * @param context
     * @param phone
     */
    public static void systemCallPhone(Context context, String phone) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        context.startActivity(dialIntent);
    }

    public static String getUUID() {
        String UUID = Settings.System.getString(CpMyApp.Companion.instance().getContentResolver(), Settings.System.ANDROID_ID);
        if (null == UUID) {
            UUID = "";
        }
        return UUID;
    }

    public static boolean isOpenNotifications() {
        NotificationManagerCompat manager = NotificationManagerCompat.from(CpMyApp.Companion.instance());
        boolean isOpened = manager.areNotificationsEnabled();
        return isOpened;
    }

    public static void startNotifactions() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", CpMyApp.Companion.instance().getPackageName(), null);
        intent.setData(uri);
        CpMyApp.Companion.instance().startActivity(intent);
    }


    private static HashMap<String, String> HEADER_PARAMS = new HashMap<String, String>();

    static {
        String UUID = CpSystemUtils.getUUID();
        HEADER_PARAMS.put("Content-Type", "application/json;charset=utf-8");
        HEADER_PARAMS.put("Build-CU", CpPackageUtil.getVersionCode() + "");
        HEADER_PARAMS.put("exChainupBundleVersion", CpApiConstants.EX_CHAINUP_BUNDLE_VERSION);
        HEADER_PARAMS.put("SysVersion-CU", CpSystemUtils.getSystemVersion());
        HEADER_PARAMS.put("SysSDK-CU", Build.VERSION.SDK_INT + "");
        HEADER_PARAMS.put("Channel-CU", "google play");
        HEADER_PARAMS.put("Mobile-Model-CU", CpSystemUtils.getSystemModel());
        HEADER_PARAMS.put("UUID-CU", UUID);
        HEADER_PARAMS.put("Platform-CU", "android");
        HEADER_PARAMS.put("Network-CU", CpNetworkUtils.getNetType());
        HEADER_PARAMS.put("exchange-client", "app");
        HEADER_PARAMS.put("appChannel", "google play");
        HEADER_PARAMS.put("appNetwork", CpSystemUtils.getAPNType(CpMyApp.Companion.instance()));
        HEADER_PARAMS.put("timezone", TimeZone.getDefault().getID());
        HEADER_PARAMS.put("osName", "android");
        HEADER_PARAMS.put("os", "android");
        HEADER_PARAMS.put("osVersion", CpSystemUtils.getSystemVersion());
        HEADER_PARAMS.put("platform", "android");
        HEADER_PARAMS.put("device", UUID);
        HEADER_PARAMS.put("clientType", "android");
        HEADER_PARAMS.put("language", "android");
    }

    public static HashMap<String, String> getHeaderParams() {
        if (!TextUtils.isEmpty(CpClLogicContractSetting.getToken())) {
            HEADER_PARAMS.put("exchange-token", CpClLogicContractSetting.getToken());
        } else {
            HEADER_PARAMS.remove("exchange-token");
        }
        HEADER_PARAMS.put("exchange-language",  CpLanguageUtil.getLanguage());
        HEADER_PARAMS.put("appAcceptLanguage", CpLanguageUtil.getLanguage());
        return HEADER_PARAMS;
    }

    public static Bitmap base64ToPicture(String imgBase64) {
        if (!TextUtils.isEmpty(imgBase64) && imgBase64.contains(",")) {
            //拿到真正的base64数据
            try {
                String base64Img = imgBase64.split(",")[1];
                byte[] decode = Base64.decode(base64Img, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    public static String getLogParams() {
        HashMap<String, String> map = getHeaderParams();
        try {
            if(map.containsKey("exchange-token")){
                map.remove("exchange-token");
            }
            return CpJsonUtils.INSTANCE.getGson().toJson(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "chainUP header Not Found";
    }
}
