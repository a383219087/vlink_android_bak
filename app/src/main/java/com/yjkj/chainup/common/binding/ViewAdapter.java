package com.yjkj.chainup.common.binding;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.View;

import androidx.databinding.BindingAdapter;
import androidx.databinding.BindingConversion;

import com.yjkj.chainup.R;
import com.yjkj.chainup.util.StringUtils;
import com.yjkj.chainup.util.TimeUtil;
import com.yjkj.chainup.util.ToastUtils;


/**
 * Created by 奔跑的狗子
 * on 2021/04/09.
 */
public class ViewAdapter {


    /**
     * 复制文本
     */
    @BindingAdapter(value = {"copyText"}, requireAll = false)
    public static void copyText(View view, String copyText) {
        view.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("simple text", copyText);
            clipboard.setPrimaryClip(clip);
            ToastUtils.showToast("复制成功");
        });


    }

    /**
     * 跳转QQ添加好友
     */
    @BindingAdapter(value = {"QQText"}, requireAll = false)
    public static void QQText(View view, String copyText) {
        view.setOnClickListener(v -> {
            try {
                String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + copyText;//uin是发送过去的qq号码
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e) {
                e.printStackTrace();
                ToastUtils.showToast("请检查是否安装QQ");
            }
        });


    }

    /**
     * 带权限防重复点击事件1读写2拍照3麦克风4设备信息
     */
    @BindingAdapter(value = {"onClick", "permission", "toast"}, requireAll = false)
    public static void onClickPermission(final View view, final View.OnClickListener listener, String permission, String toast) {
        view.setOnClickListener(v -> {
            if (!TimeUtil.getInstance().isFastDoubleClick()) {
                if (null != listener) {
                    listener.onClick(view);
                }
            }
        });
    }

    /**
     * 长按点击事件
     */
    @BindingAdapter(value = {"onLongClick"}, requireAll = false)
    public static void onLongClick(final View view, final View.OnClickListener listener) {
        view.setOnLongClickListener(v -> {
            if (null != listener) {
                listener.onClick(view);
            }
            return false;
        });
    }

    /**
     * 设置背景
     * radius圆角单位dp,默认0
     * ResColor,ResColor1,传两个是渐变,默认白色
     * StrokeWidth,StrokeResColor线宽单位dp,默认1dp,边框背景默认白色
     * topLeftRadius,topRightRadius,bottomLeftRadius,bottomRightRadius四个圆角(同radius)
     * angle,渐变角度默认是从下到上
     */
    @BindingAdapter(value = {"radius", "ResColor", "ResColor1", "StrokeWidth", "StrokeResColor",
            "topLeftRadius", "topRightRadius", "bottomLeftRadius", "bottomRightRadius", "angle",
            "ResRadius", "ResTopLeftRadius", "ResTopRightRadius", "ResBottomLeftRadius", "ResBottomRightRadius"
            , "StrColor", "StrColor1"},
            requireAll = false)
    public static void setBackground(final View view, int radius, int ResColor, int ResColor1, int StrokeWidth,
                                     int StrokeResColor, int topLeftRadius, int topRightRadius,
                                     int bottomLeftRadius, int bottomRightRadius, int angle, int ResRadius,
                                     int ResTopLeftRadius, int ResTopRightRadius, int ResBottomLeftRadius,
                                     int ResBottomRightRadius, String StrColor, String StrColor1) {
        int start = view.getResources().getColor(R.color.white, null);
        if (ResColor != 0) {
            start = view.getResources().getColor(ResColor, null);
        }
        if (!StringUtils.isEmpty(StrColor)) {
            start = Color.parseColor(StrColor);
        }

        int end = start;
        if (ResColor1 != 0) {
            end = view.getResources().getColor(ResColor1, null);
        }
        if (!StringUtils.isEmpty(StrColor1)) {
            end = Color.parseColor(StrColor1);
        }

        int[] colors = {start, end};
        GradientDrawable drawable = new GradientDrawable();
        if (angle != 0) {
            drawable.setOrientation(setAngle(angle));
        }

        drawable.setShape(GradientDrawable.RECTANGLE);


        if (topLeftRadius != 0 || topRightRadius != 0 || bottomLeftRadius != 0 || bottomRightRadius != 0) {
            float[] radiusList = {(float) dip2px(view.getContext(), topLeftRadius),
                    (float) dip2px(view.getContext(), topLeftRadius),
                    (float) dip2px(view.getContext(), topRightRadius),
                    (float) dip2px(view.getContext(), topRightRadius),
                    (float) dip2px(view.getContext(), bottomRightRadius),
                    (float) dip2px(view.getContext(), bottomRightRadius),
                    (float) dip2px(view.getContext(), bottomLeftRadius),
                    (float) dip2px(view.getContext(), bottomLeftRadius)};
            drawable.setCornerRadii(radiusList);
        }

        if (ResTopLeftRadius != 0 || ResTopRightRadius != 0 || ResBottomLeftRadius != 0 || ResBottomRightRadius != 0) {
            float[] radiusList = {view.getContext().getResources().getDimension(ResTopLeftRadius),
                    view.getContext().getResources().getDimension(ResTopLeftRadius),
                    view.getContext().getResources().getDimension(ResTopRightRadius),
                    view.getContext().getResources().getDimension(ResTopRightRadius),
                    view.getContext().getResources().getDimension(ResBottomRightRadius),
                    view.getContext().getResources().getDimension(ResBottomRightRadius),
                    view.getContext().getResources().getDimension(ResBottomLeftRadius),
                    view.getContext().getResources().getDimension(ResBottomLeftRadius)};
            drawable.setCornerRadii(radiusList);
        }

        if (radius != 0) {
            drawable.setCornerRadius(dip2px(view.getContext(), radius));
        }
        if (ResRadius != 0) {
            drawable.setCornerRadius(view.getContext().getResources().getDimensionPixelSize(ResRadius));
        }


        if (StrokeResColor != 0) {
            int with = dip2px(view.getContext(), 1);
            if (StrokeWidth != 0) {
                with = dip2px(view.getContext(), StrokeWidth);
            }
            drawable.setStroke(with, view.getResources().getColor(StrokeResColor, null));
        }

        drawable.setColors(colors);

        view.setBackground(drawable);
    }
    /**
     * dip转换到px
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dip2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 设置渐变角度
     */
    public static GradientDrawable.Orientation setAngle(int angle) {
        int a = 0;
        if (angle != 0) {
            a = (angle / 45) % 8;
        }

        if (a == 0) {
            return GradientDrawable.Orientation.BOTTOM_TOP;
        } else if (a == 1) {
            return GradientDrawable.Orientation.TL_BR;
        } else if (a == 2) {
            return GradientDrawable.Orientation.LEFT_RIGHT;
        } else if (a == 3) {
            return GradientDrawable.Orientation.BL_TR;
        } else if (a == 4) {
            return GradientDrawable.Orientation.TOP_BOTTOM;
        } else if (a == 5) {
            return GradientDrawable.Orientation.BR_TL;
        } else if (a == 6) {
            return GradientDrawable.Orientation.RIGHT_LEFT;
        } else if (a == 7) {
            return GradientDrawable.Orientation.TR_BL;
        }

        return GradientDrawable.Orientation.TOP_BOTTOM;
    }

    /**
     * 整型的颜色值转换为drawable对象
     */
    @BindingConversion
    public static ColorDrawable convertColorToDrawable(int color) {
        return new ColorDrawable(color);
    }

}
