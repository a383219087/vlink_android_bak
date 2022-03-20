package com.chainup.contract.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

/**
 * @Author lianshangljl
 * @Date 2018/11/12-9:50 PM
 * @Email buptjinlong@163.com
 * @description
 */
public class CpVerticalTextview4ChainUp extends TextSwitcher implements ViewSwitcher.ViewFactory {

    private static final int FLAG_START_AUTO_SCROLL = 1;
    private static final int FLAG_STOP_AUTO_SCROLL = 2;

    private float mTextSize = 12;
    private int mPadding = 5;
    private int textColor = Color.GREEN;
    private boolean isRight = false;
    /**
     * @param textSize  字号
     * @param padding   内边距
     * @param textColor 字体颜色
     */
    public void setText(float textSize, int padding, int textColor) {
        mTextSize = textSize;
        mPadding = padding;
        this.textColor = textColor;
    }

    private OnItemClickListener itemClickListener;
    private Context mContext;
    private int currentId = 0;
    private long time = 1000l;
    public ArrayList<String> textList;
    private int selectIndex = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FLAG_START_AUTO_SCROLL:
                    if (textList.size() > 0) {
                        selectIndex = currentId % textList.size();
                        currentId++;
                        setText(textList.get(selectIndex));
                    }
                    handler.sendEmptyMessageDelayed(FLAG_START_AUTO_SCROLL, time);
                    break;
                case FLAG_STOP_AUTO_SCROLL:
                    handler.removeMessages(FLAG_START_AUTO_SCROLL);
                    break;
            }
        }
    };

    public CpVerticalTextview4ChainUp(Context context) {
        this(context, null);
        mContext = context;
    }


    public void setRight(boolean isRight) {
        this.isRight = isRight;
    }

    public CpVerticalTextview4ChainUp(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        textList = new ArrayList<String>();
    }

    public void setAnimTime(long animDuration) {
        setFactory(this);
        AnimationSet set = new AnimationSet(false);
        Animation in = new TranslateAnimation(0, 0, 100, 0);
        in.setDuration(animDuration);
        in.setInterpolator(new AccelerateInterpolator());
        set.addAnimation(in);
        Animation alpha = new AlphaAnimation(0.0f, 1.0f);
        alpha.setDuration(animDuration);
        set.addAnimation(alpha);
        set.setZAdjustment(Animation.ZORDER_TOP);

        AnimationSet setOut = new AnimationSet(false);
        Animation out = new TranslateAnimation(0, 0, 0, -100);
        out.setDuration(animDuration);
        out.setInterpolator(new AccelerateInterpolator());
        setOut.addAnimation(out);
        Animation alphaOut = new AlphaAnimation(1.0f, 0.0f);
        alphaOut.setDuration(animDuration);
        setOut.addAnimation(alphaOut);
        setOut.setZAdjustment(Animation.ZORDER_BOTTOM);
        setInAnimation(set);
        setOutAnimation(setOut);
    }

    /**
     * 间隔时间
     *
     * @param time
     */
    public void setTextStillTime(final long time) {
        this.time = time;
        if (handler != null) {
            stopAutoScroll();
        }

    }

    /**
     * 设置数据源
     *
     * @param titles
     */
    public void setTextList(ArrayList<String> titles) {
        textList.clear();
        textList.addAll(titles);
        currentId = 0;
    }

    /**
     * 开始滚动
     */
    public void startAutoScroll() {
        boolean isLoop = handler.hasMessages(FLAG_START_AUTO_SCROLL);
        if(!isLoop){
            handler.sendEmptyMessageDelayed(FLAG_START_AUTO_SCROLL, 200L);
        }
    }

    /**
     * 停止滚动
     */
    public void stopAutoScroll() {
        if (handler == null) return;
        boolean isLoop = handler.hasMessages(FLAG_START_AUTO_SCROLL);
        if (isLoop) {
            handler.sendEmptyMessageDelayed(FLAG_STOP_AUTO_SCROLL, 100L);
        }
    }

    @Override
    public View makeView() {
        TextView t = new TextView(mContext);
        if (isRight){
            t.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        }else {
            t.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        }
        t.setMaxLines(1);
        t.setPadding(mPadding, 0, 0, 0);
        t.setTextColor(textColor);
        t.setTextSize(mTextSize);
        t.setClickable(true);
        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null && textList.size() > 0 && currentId != -1) {
                    itemClickListener.onItemClick(selectIndex);
                }
            }
        });
        return t;
    }

    /**
     * 设置点击事件监听
     *
     * @param itemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * 轮播文本点击监听器
     */
    public interface OnItemClickListener {
        /**
         * 点击回调
         *
         * @param position 当前点击ID
         */
        void onItemClick(int position);
    }

}

