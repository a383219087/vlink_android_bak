package com.yjkj.chainup.common.binding;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;


/**
 * Created by 奔跑的狗子
 * on 2021/04/09.
 */
public class TextViewAdapter {


    /**
     * 设置字体大小
     */
    @BindingAdapter("textSize")
    public static void bindTextSize(TextView view, int textSize) {
        view.setTextSize(textSize);
    }

    /**
     * drawableLeft
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    @BindingAdapter(value = {"drawableLeft"}, requireAll = false)
    public static void drawable(TextView view, int drawableLeft) {
        Drawable drawable = view.getContext().getResources().getDrawable(drawableLeft, null);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(),
                drawable.getMinimumHeight());
        view.setCompoundDrawables(drawable, null, null, null);

    }


}
