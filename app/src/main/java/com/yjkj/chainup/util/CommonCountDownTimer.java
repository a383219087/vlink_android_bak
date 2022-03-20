package com.yjkj.chainup.util;

import android.os.CountDownTimer;

/**
 * 公共倒计时类
 */
public class CommonCountDownTimer extends CountDownTimer {

    private OnCountDownTimerListener countDownTimerListener;


    public void setCountDownTimerListener(OnCountDownTimerListener listener) {
        this.countDownTimerListener = listener;

    }



    public CommonCountDownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture,countDownInterval);

    }


    @Override
    public void onTick(long millisUntilFinished) {

        if (null != countDownTimerListener) {
            countDownTimerListener.onTick(millisUntilFinished);

        }

    }


    @Override
    public void onFinish() {
        if (null != countDownTimerListener) {
            countDownTimerListener.onFinish();

        }

    }


    public interface OnCountDownTimerListener {

        /**
         * 更新倒计时时间
         *
         * @param millisUntilFinished
         */

        void onTick(long millisUntilFinished);


        /**
         * 完成倒计时
         */

        void onFinish();

    }
}
