package com.yjkj.chainup.common.binding;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;



public class ImageViewAdapter {


    /**
     * 显示图片
     */
    @BindingAdapter("bitmap")
    public static void bindTextSize(ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }



}
