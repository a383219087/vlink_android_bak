package com.yjkj.chainup.common.binding;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yjkj.chainup.R;
import com.yjkj.chainup.util.BitmapUtils;


public class ImageViewAdapter {


    /**
     * 显示图片
     */
    @BindingAdapter("bitmap")
    public static void setBitmap(ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }

    /**
     * 显示图片
     */
    @BindingAdapter("src")
    public static void setSrc(ImageView view, int Res) {
        view.setImageResource(Res);
    }


    /**
     * 显示图片
     */
    @BindingAdapter("url")
    public static void setUrl(ImageView view, String url) {
        if (url==null||url.isEmpty()){
            return;
        }
        RequestOptions options = new RequestOptions().placeholder(R.drawable.ic_sample)
                .error(R.drawable.ic_sample);
        Glide.with(view.getContext())
                .load(url)
                .apply(options)
                .into(view);
    }

    /**
     * 生成二维码
     */
    @BindingAdapter("content")
    public static void generateBitmap(ImageView view, String content) {
        if (content == null || content.isEmpty()) {
            return;
        }
        view.setImageBitmap(BitmapUtils.generateBitmap(content, 400, 400));
    }


}
