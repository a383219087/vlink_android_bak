package com.yjkj.chainup.base;

import android.animation.ObjectAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.yjkj.chainup.R;
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil;
import com.yjkj.chainup.extra_service.eventbus.MessageEvent;
import com.yjkj.chainup.util.SoftKeyboardUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @Description:  DialogFragment基类分装 用法同NBaseFragment
 * @Author: wanghao
 * @CreateDate: 2019-11-04 12:13
 * @UpdateUser: wanghao
 * @UpdateDate: 2019-11-04 12:13
 * @UpdateRemark: 更新说明
 */
public abstract class NBaseDialogFragment extends DialogFragment implements View.OnClickListener {

    protected View layoutView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == layoutView) {
            //setTheme();
            layoutView = inflater.inflate(setContentView(), container,false);
            loadData();
        } else {
            ViewParent viewParent = layoutView.getParent();
            if(null!=viewParent && viewParent instanceof ViewGroup){
                ViewGroup vg = (ViewGroup)viewParent;
                vg.removeView(layoutView);
            }
        }
        return layoutView;
    }

    public void setTheme(){
        Window window = this.getDialog().getWindow();
        //去掉dialog默认的padding
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //设置dialog的位置在底部
        lp.gravity = Gravity.BOTTOM;
        //设置dialog的动画
        lp.windowAnimations = R.style.leftin_rightout_DialogFg_animstyle;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());
    }


    protected abstract int setContentView();

    protected <T extends View> T findViewById(int id){
        return layoutView.findViewById(id);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBusUtil.register(this);
        hideKeyboard();
        initView();

    }

    protected abstract void initView();

    protected abstract void loadData();

    /*@NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
        Window window = getDialog().getWindow();
        //window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.leftin_rightout_DialogFg_animstyle);
        //window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }*/

    @Override
    public void onStart() {
        super.onStart();

        //Window win = getDialog().getWindow();
       // win.setBackgroundDrawable( new ColorDrawable(Color.TRANSPARENT));
        /*DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics( dm );
        WindowManager.LayoutParams params = win.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width =  ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        win.setAttributes(params);*/
        //win.getAttributes().windowAnimations = R.style.leftin_rightout_DialogFg_animstyle;
    }

    /*
     * 处理线程跟发消息线程一致
     * 子类重载
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEvent(MessageEvent event) {
    }

    /*
     * 黏性事件处理
     * 子类重载处理完事件后需调用 EventBusUtil.removeStickyEvent(event);
     */
    @Subscribe(threadMode = ThreadMode.POSTING,sticky=true)
    public void  onMessageStickyEvent(MessageEvent event) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusUtil.unregister(this);
    }


    @Override
    public void onClick(View v) {
        dismissDialog();
    }

    protected void dismissDialog(){
        if(isVisible()){
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        hideKeyboard();
        super.dismiss();
    }

    private void hideKeyboard(){
        View view = getDialog().getCurrentFocus();
        SoftKeyboardUtil.hideSoftKeyboard(view);
    }

    /*
     * 展示dialog
     */
    protected void showDialog(FragmentManager manager, String tag){
        show(manager,tag);
    }

    /* access modifiers changed from: protected */
    public void doCoverViewShowAnimation(View view) {
        if (view != null) {
            ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f).setDuration(getDuration()).start();
        }
    }

    /* access modifiers changed from: protected */
    public void doCoverViewHideAnimation(View view) {
        if (view != null) {
            ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f).setDuration(getDuration()).start();
        }
    }

    /* access modifiers changed from: protected */
    public long getDuration() {
        return 300;
    }
}
