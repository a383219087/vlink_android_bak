
package com.chainup.contract.view.material;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import com.chainup.contract.utils.CpColorUtils;


class CpMaterialBackgroundDetector {
    private static final String TAG = "MaterialDetector";
    private static final boolean DBG = false;

    private static final int DEFAULT_DURATION = 1200;
    private static final int DEFAULT_FAST_DURATION = 300;
    private static final int DEFAULT_TRANSPARENT_DURATION = 300;

    private static final int DEFAULT_ALPHA = 33;
    public static final int DEFAULT_COLOR = Color.BLACK;

    /*package*/ private View mView;
    /*package*/ private Callback mCallback;

    private int mColor;
    private Paint mCirclePaint;
    private int mFocusColor;
    private int mCircleColor;

    //The position of the initial circle center.
    private float mX;
    private float mY;
    //The position of curr circle center.
    private float mCenterX;
    private float mCenterY;
    private float mViewRadius;
    //The radius of curr circle.
    private float mRadius;
    //The size of view.
    private int mWidth;
    private int mHeight;
    private int mMinPadding;

    private ObjectAnimator mAnimator;
    /*package*/ private boolean mIsAnimation;
    /*package*/ private boolean mIsCancelAnimation;
    /*package*/ private boolean mHasDrawMask;
    private boolean mIsPressed;

    private boolean mIsPerformClick;
    private boolean mIsPerformLongClick;

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            mIsAnimation = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mHasDrawMask = false;
            mIsAnimation = false;
            mView.invalidate();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    private Animator.AnimatorListener mCancelAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            mIsCancelAnimation = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mHasDrawMask = false;
            mIsCancelAnimation = false;
            mView.invalidate();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public CpMaterialBackgroundDetector(Context context, View view, Callback callback, int color) {
        mView = view;
        mCallback = callback;
        setColor(color);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinPadding = configuration.getScaledEdgeSlop();
    }

    private void setColor(int color) {
        if (mColor != color) {
            if (DBG) {
                Log.d(TAG, "ColorChanged");
            }
            mColor = color;
            setAlpha(DEFAULT_ALPHA);
        }
    }

    private void resetPaint() {
        if (mCirclePaint == null) {
            mCirclePaint = new Paint();
        }
        mCirclePaint.setColor(mCircleColor);
    }

    private int computeCircleColor(int color, int alpha) {
        return CpColorUtils.getColorAtAlpha(color, alpha);
    }

    private int computeFocusColor(int color, int alpha) {
        return CpColorUtils.getColorAtAlpha(color, alpha);
    }

    /**
     * Called when view size changed.
     *
     * @param width
     * @param height
     */
    @SuppressWarnings("JavaDoc")
    public void onSizeChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
        mViewRadius = (float) Math.sqrt(mWidth * mWidth / 4 + mHeight * mHeight / 4);
    }

    public boolean onTouchEvent(MotionEvent event, boolean result) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsPressed = true;
                if (!mIsAnimation) {
                    //Ensure there is only one shadow animation.
                    startShadowAnimation(event);
                }
                // Ensure the following motion event can be received.
                if (!result) {
                    result = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = event.getX();
                final float y = event.getY();
                if (x >= 0 && x <= mWidth && y >= 0 && y <= mHeight) {
                    moveShadowCenter(x, y);
                } else {
                    //When this figure move outside, we should cancel animation.
                    cancelShadowAnimation();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelShadowAnimation();

                break;
        }
        return result;
    }


    /**
     *
     */
    private void startShadowAnimation(MotionEvent event) {
        //When the fingers touch the view, we let the mask appear slowly.
        mX = event.getX();
        mY = event.getY();
        mAnimator = ObjectAnimator.ofFloat(this, "radius", mMinPadding, mViewRadius);
        int mDuration = DEFAULT_DURATION;
        mAnimator.setDuration(mDuration);
        mAnimator.setInterpolator(mInterpolator);
        mAnimator.addListener(mAnimatorListener);
        mAnimator.start();
        if (DBG) {
            Log.i(TAG, "Down,from:" + 0 + ",to:" + mViewRadius);
        }
    }

    /**
     *
     */
    private void cancelShadowAnimation() {
        if (mIsCancelAnimation) {
            return;
        }
        mIsPressed = false;
        //Cancel the shadow animation before the fast cancel animation.
        cancelAnimator();
        mX = mCenterX;
        mY = mCenterY;
        mRadius = Math.max(mRadius, mViewRadius * 0.1f);
        int duration = (int) (DEFAULT_FAST_DURATION * (mViewRadius - mRadius) / mViewRadius);
        if (duration > 0) {
            //When the fingers leave the view, if the mask doesn't cover whole view, we let the mask appear fast.
            mAnimator = ObjectAnimator.ofFloat(this, "radius", mRadius, mViewRadius);
            mAnimator.setDuration(duration);
            mAnimator.setInterpolator(mInterpolator);
            mAnimator.addListener(mCancelAnimatorListener);
            mAnimator.start();
            if (DBG) {
                Log.i(TAG, "UP,from:" + mRadius + ",to:" + mViewRadius);
            }
        }
        if (duration > 0) {
            showAlphaAnimation();
        }
        mView.invalidate();
    }

    /**
     * We should let the mask layer disappear gradually.
     */
    private void showAlphaAnimation() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofInt(this, "alpha", DEFAULT_ALPHA, 0);
        alphaAnimator.setDuration(DEFAULT_TRANSPARENT_DURATION);
        alphaAnimator.setInterpolator(new AccelerateInterpolator());
        alphaAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //When the animation end, we should do something.
                mHasDrawMask = false;
                mIsAnimation = false;
                //Reset the alpha value.
                setAlpha(DEFAULT_ALPHA);
                //Handle the click event.
                if (mIsPerformClick) {
                    if (mCallback != null) {
                        mCallback.performClickAfterAnimation();
                    }
                    mIsPerformClick = false;
                }
                if (mIsPerformLongClick) {
                    if (mCallback != null) {
                        mCallback.performLongClickAfterAnimation();
                    }
                    mIsPerformLongClick = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alphaAnimator.start();
    }

    /**
     * @param x
     * @param y
     */
    @SuppressWarnings("JavaDoc")
    private void moveShadowCenter(float x, float y) {
        //When move, the shadow circle should be redraw.
        mHasDrawMask = false;
        mX = x;
        mY = y;
    }

    private void cancelAnimator() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    public void draw(Canvas canvas) {
        if (mIsPressed || mIsAnimation || mIsCancelAnimation) {
            //If client is focused or in animation, show focus layer.
            if (DBG) {
                Log.d(TAG, "DrawFocusColor");
            }
            mHasDrawMask = true;
            canvas.drawColor(mFocusColor);
            canvas.drawCircle(mCenterX, mCenterY, mRadius, mCirclePaint);
        }
    }

    /**
     * This method is called by ObjectAnimator to invalidate mView.
     *
     * @param radius
     */
    @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
    public void setRadius(float radius) {
        float percent = 0;
        if (mAnimator != null) {
            percent = mAnimator.getAnimatedFraction();
        }
        mRadius = radius;
        mCenterX = mX + percent * (mWidth / 2 - mX);
        mCenterY = mY + percent * (mHeight / 2 - mY);
        float distance = (float) Math.sqrt((mCenterX - mX) * (mCenterX - mX) + (mCenterY - mY) * (mCenterY - mY)) + mMinPadding;
        if (distance > radius) {
            mCenterX = mX + (mCenterX - mX) * radius / distance;
            mCenterY = mY + (mCenterY - mY) * radius / distance;
        }
        if (mView instanceof ViewGroup) {
            mView.setWillNotDraw(false);
        }
        if (mHasDrawMask) {
            //If mask has been drawn, we could only invalidate the circle rect.
            //To improve the efficiency.
            mView.invalidate((int) (mCenterX - mRadius), (int) (mCenterY - mRadius),
                    (int) (mCenterX + mRadius), (int) (mCenterY + mRadius));
        } else {
            mView.invalidate();
        }
    }

    /**
     * This method is called by ObjectAnimator to let the mask layer be transparent.
     *
     * @param alpha
     */
    @SuppressWarnings("JavaDoc")
    private void setAlpha(int alpha) {
        mFocusColor = computeFocusColor(mColor, alpha);
        mCircleColor = computeCircleColor(mColor, alpha);
        resetPaint();
        if (mView instanceof ViewGroup) {
            mView.setWillNotDraw(false);
        }
        mView.invalidate();
    }

    /**
     * It only happens when the fingers up.
     * See also {@link #handlePerformLongClick()}
     *
     * @return whether it is been handled.
     */
    public boolean handlePerformClick() {
        boolean result = mIsPerformClick;
        mIsPerformClick = true;
        return result;
    }

    /**
     * It only happens when the fingers up.
     * See also {@link #handlePerformClick()} ()}
     *
     * @return whether it is been handled.
     */
    public boolean handlePerformLongClick() {
        boolean result = mIsPerformLongClick;
        mIsPerformLongClick = true;
        return result;
    }

    /**
     * The Callback interface will call handle the click event after animation.
     */
    public interface Callback {
        /**
         * Handle click event after animation.
         */
        void performClickAfterAnimation();

        /**
         * Handle long click event after animation.
         */
        void performLongClickAfterAnimation();
    }
}
