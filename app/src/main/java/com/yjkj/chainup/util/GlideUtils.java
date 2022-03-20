package com.yjkj.chainup.util;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yjkj.chainup.R;
import com.yjkj.chainup.bean.coin.CoinBean;
import com.yjkj.chainup.db.service.PublicInfoDataService;
import com.yjkj.chainup.manager.DataManager;
import com.yjkj.chainup.new_version.view.GlideRoundTransform;

import java.io.File;

/**
 * Created by Bertking on 2018/5/23.
 */
public class GlideUtils {

    private static final String TAG = "GlideUtils";

    public static void load(Activity activity,
                            String url,
                            ImageView imageView,
                            RequestOptions options) {

        LogUtil.d(TAG, "GlideUtils==load==imageView is " + imageView + ",url is " + url);
        if (null == imageView || null == url)
            return;
        LogUtil.d(TAG, "GlideUtils==load==111111");

        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;
        LogUtil.d(TAG, "GlideUtils==load==222222");


        Glide.with(activity)
                .load(url)
                .apply(options)
                .into(imageView);

    }
    public static void loadFile(Activity activity,
                            String url,
                            ImageView imageView,
                            RequestOptions options) {

        LogUtil.d(TAG, "GlideUtils==load==imageView is " + imageView + ",url is " + url);
        if (null == imageView || null == url)
            return;
        LogUtil.d(TAG, "GlideUtils==load==111111");

        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;
        LogUtil.d(TAG, "GlideUtils==load==222222");


        Glide.with(activity)
                .load(new File(url))
                .transform(new GlideRoundTransform(activity,10))
                .apply(options)
                .into(imageView);

    }
    public static void loadRadius(Activity activity,
                            Bitmap url,
                            ImageView imageView,
                            RequestOptions options) {

        LogUtil.d(TAG, "GlideUtils==load==imageView is " + imageView + ",url is " + url);
        if (null == imageView || null == url)
            return;
        LogUtil.d(TAG, "GlideUtils==load==111111");

        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;
        LogUtil.d(TAG, "GlideUtils==load==222222");


        Glide.with(activity)
                .load(url)
                .transform(new GlideRoundTransform(activity,6))
                .apply(options)
                .into(imageView);

    }


    /**
     * 加载 币种的图标
     *
     * @param context
     * @param coinName
     * @param imageView
     */
    public static void loadCoinIcon(Context context, String coinName, ImageView imageView) {

        CoinBean coinBean = DataManager.Companion.getCoinByName(coinName);
        String imageUrl = "";

        if (coinBean != null) {
            imageUrl = coinBean.getIcon();
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_default_coin).placeholder(R.drawable.ic_default_coin);
        GlideUtils.load((Activity) context, imageUrl, imageView, requestOptions);
    }


    /**
     * 加载 网络币种的图标
     *
     * @param context
     * @param imageView
     */
    public static void loadNetCoinIcon(Context context, String imgUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_default_coin).placeholder(R.drawable.ic_default_coin);
        GlideUtils.load((Activity) context, imgUrl, imageView, requestOptions);
    }

    /**
     * 加载 支付方式的图标
     *
     * @param context
     * @param imageView
     */
    public static void loadImage(Context context, String imgUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.icon_send_image).placeholder(R.drawable.icon_send_image);
        GlideUtils.load((Activity) context, getImageUrl(imgUrl), imageView, requestOptions);
    }

    /**
     * 加载 增加支付方式的图标
     *
     * @param context
     * @param imageView
     */
    public static void loadPaymentImage(Context context, String imgUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_upload_image_otc);
        GlideUtils.load((Activity) context, getImageUrl(imgUrl), imageView, requestOptions);
    }

    /**
     * 加载 头像
     *
     * @param context
     * @param imageView
     */
    public static void loadImage4OTC(Context context, String imgUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.personal_headportrait).placeholder(R.drawable.personal_headportrait);
        GlideUtils.load((Activity) context, getImageUrl(imgUrl), imageView, requestOptions);
    }

    /**
     * 首页 功能服务
     *
     * @param context
     * @param imageView
     */
    public static void loadImage4HomepageService(Context context, String imgUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.icon_send_image);
        GlideUtils.load((Activity) context, imgUrl, imageView, requestOptions);
    }


    /**
     * 加载 头像
     *
     * @param context
     * @param imageView
     */
    public static void loadImageHeader(Activity context, String imgUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.ic_default_head).placeholder(R.drawable.ic_default_head);
        GlideUtils.load(context, getImageUrl(imgUrl), imageView, requestOptions);
    }

    /**
     * 加载 头像
     *
     * @param context
     * @param imageView
     */
    public static void loadImage4NewHomepage(Context context, String imgUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.icon_home_page_default).placeholder(R.drawable.icon_home_page_default);
        GlideUtils.load((Activity) context, getImageUrl(imgUrl), imageView, requestOptions);
    }

    private static String getImageUrl(String url) {
        return url;
    }

    /**
     * 加载 头像
     *
     * @param context
     * @param imageView
     */
    public static void loadImage(Activity context, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.mipmap.ic_launcher).placeholder(R.mipmap.ic_launcher);
        String imgUrl = PublicInfoDataService.getInstance().getSharingPage(null);
        imageView.setImageBitmap(ZXingUtils.createQRImage(imgUrl,  SizeUtils.dp2px(40f),SizeUtils.dp2px(40f)));
//        GlideUtils.load(context, getImageUrl(imgUrl), imageView, requestOptions);
    }

    /**
     * 加载 头像
     *
     * @param context
     * @param imageView
     */
    public static void loadImageQr(Activity context, ImageView imageView) {
        String imgUrl = PublicInfoDataService.getInstance().getSharingPage(null);
        BitmapDrawable back = new BitmapDrawable(context.getResources(),ZXingUtils.createQRImage(imgUrl, SizeUtils.dp2px(50f), SizeUtils.dp2px(50f)));
        imageView.setBackground(back);
    }

    /**
     * 加载 支付方式的图标
     *
     * @param context
     * @param imageView
     */
    public static void loadLocalImage(Context context, String imgUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions();
        GlideUtils.loadFile((Activity) context, getImageUrl(imgUrl), imageView, requestOptions);
    }


}
