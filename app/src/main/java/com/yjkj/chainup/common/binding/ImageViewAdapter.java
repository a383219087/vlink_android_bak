package com.yjkj.chainup.common.binding;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;


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

}
