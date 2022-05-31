package com.yjkj.chainup.common.binding;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

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
     * 生成二维码
     */
    @BindingAdapter("content")
    public static void generateBitmap(ImageView view, String content) {
        if (content.isEmpty()){
            return;
        }
        view.setImageBitmap(BitmapUtils.generateBitmap(content,400,400));
    }



}
