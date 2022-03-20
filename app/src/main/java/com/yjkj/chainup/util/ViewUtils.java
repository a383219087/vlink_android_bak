package com.yjkj.chainup.util;

import android.app.Activity;
import android.graphics.Rect;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import com.yjkj.chainup.app.ChainUpApp;
import com.yjkj.chainup.new_version.kline.ViewCallBack;

/**
 * Created by Bertking on 2018/6/8.
 */
public class ViewUtils {

    /**
     * @param view
     * @param text
     * @param isSuc 是否是成功的状态
     */
    public static void showSnackBar(View view, String text, boolean isSuc) {
        if (view != null) {
//            TSnackbar snackbar = TSnackbar
//                    .make(view, text, TSnackbar.LENGTH_SHORT);
//            View snackbarView = snackbar.getView();
//            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,SizeUtils.dp2px(64));
//            snackbarView.setLayoutParams(layoutParams);
//            TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
//
//            if (isSuc) {
//                snackbarView.setBackgroundColor(ContextCompat.getColor(ChainUpApp.appContext, R.color.feedback_success));
//            } else {
//                snackbarView.setBackgroundColor(ContextCompat.getColor(ChainUpApp.appContext, R.color.feedback_error));
//            }
//
//            textView.setTextColor(Color.WHITE);
//            textView.setGravity(Gravity.CENTER);
//            textView.setTextSize(16f);
//            snackbar.show();
        }
    }

    public static int[] aa(View view, Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int i = rect.top;
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        iArr[1] = iArr[1] - i;
        return iArr;
    }
    public static void a(View view, boolean z) {
        if (view != null) {
            if (z) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }
    public static int a() {
        int identifier = ChainUpApp.appContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            return ChainUpApp.appContext.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }

    public static void viewBack(final View view, final ViewCallBack<View> bVar) {
        if (view != null) {
            if (!view.isInEditMode()) {
                view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (bVar != null) {
                            bVar.onCallback(view);
                        }
                    }
                });
            } else if (bVar != null) {
                bVar.onCallback(view);
            }
        }
    }

//
//    var snackbar = TSnackbar
//            .make(v_container, "手机格式错误，请重新输入", TSnackbar.LENGTH_SHORT)
//    val snackbarView = snackbar.view
////            snackbarView.setBackgroundColor(Color.parseColor("#ffef5a61"))
//            snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
//
//    val textView = snackbarView.findViewById<TextView>(com.androidadvance.topsnackbar.R.id.snackbar_text)
//            textView.setTextColor(Color.WHITE)
//    textView.gravity = Gravity.CENTER
//    textView.textSize = 18f
//            snackbar.show()
//
    public static void setEditTextLength(EditText view, int length) {
        view.setFilters(new InputFilter[]{new InputFilter.LengthFilter((length))});
    }


}
