package com.yjkj.chainup.wedegit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.zxing.ResultPoint;
import com.yjkj.chainup.R;
import com.yjkj.chainup.util.ColorUtil;
import com.yjkj.chainup.wedegit.zxing.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * <p>
 * rectangle and partial transparency outside it, as well as the laser scanner
 * <p>
 * animation and result points.
 */

public class ViewfinderViewV2 extends View {

    private static final String TAG = "log";

    /**
     * 刷新界面的时间
     */

    private static final long ANIMATION_DELAY = 10L;

    private static final int OPAQUE = 0xFF;


    /**
     * 四个绿色边角对应的长度
     */

    private int ScreenRate;


    /**
     * 四个绿色边角对应的宽度
     */

    private static final int CORNER_WIDTH = 10;

    /**
     * 扫描框中的中间线的宽度
     */

    private static final int MIDDLE_LINE_WIDTH = 6;


    /**
     * 扫描框中的中间线的与扫描框左右的间隙
     */

    private static final int MIDDLE_LINE_PADDING = 5;


    /**
     * 中间那条线每次刷新移动的距离
     */

    private static final int SPEEN_DISTANCE = 5;


    /**
     * 手机的屏幕密度
     */

    private static float density;

    /**
     * 字体大小
     */

    private static final int TEXT_SIZE = 14;

    /**
     * 字体距离扫描框下面的距离
     */

    private static final int TEXT_PADDING_TOP = 40;


    /**
     * 画笔对象的引用
     */

    private Paint paint;

    private TextPaint textPaint;


    /**
     * 中间滑动线的最顶端位置
     */

    private int slideTop;


    /**
     * 中间滑动线的最底端位置
     */

    private int slideBottom;


    private Bitmap resultBitmap;

    private final int maskColor;

    private final int resultColor;


    private final int resultPointColor;
    private CameraManager cameraManager;

    private Collection<ResultPoint> possibleResultPoints;

    private Collection<ResultPoint> lastPossibleResultPoints;


    boolean isFirst;


    public ViewfinderViewV2(Context context, AttributeSet attrs) {

        super(context, attrs);


        density = context.getResources().getDisplayMetrics().density;

        //将像素转换成dp

        ScreenRate = (int) (20 * density);


        paint = new Paint();
        textPaint = new TextPaint();
        Resources resources = getResources();

        maskColor = ContextCompat.getColor(context, R.color.barcode_viewfinder_mask);

        resultColor = ContextCompat.getColor(context, R.color.barcode_result_view);


        resultPointColor = ContextCompat.getColor(context, R.color.barcode_possible_result_points);

        possibleResultPoints = new HashSet<ResultPoint>(5);

    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override

    public void onDraw(Canvas canvas) {

        //中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        cameraManager.setFramingViewSize(new Point(this.getWidth(), this.getHeight()));
        Rect frame = cameraManager.getFramingRect();
        if (frame == null) {
            return;
        }

        //初始化中间线滑动的最上边和最下边

        if (!isFirst) {

            isFirst = true;

            slideTop = frame.top;

            slideBottom = frame.bottom;

        }


        //获取屏幕的宽和高

        int width = canvas.getWidth();

        int height = canvas.getHeight();


        paint.setColor(resultBitmap != null ? resultColor : maskColor);


        //画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面

        //扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边

        canvas.drawRect(0, 0, width, frame.top, paint);

        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);

        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,

                paint);

        canvas.drawRect(0, frame.bottom + 1, width, height, paint);


        if (resultBitmap != null) {

            // Draw the opaque result bitmap over the scanning rectangle

            paint.setAlpha(OPAQUE);

            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);

        } else {


            //画扫描框边上的角，总共8个部分

            paint.setColor(Color.GREEN);

            canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,

                    frame.top + CORNER_WIDTH, paint);

            canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top

                    + ScreenRate, paint);

            canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,

                    frame.top + CORNER_WIDTH, paint);

            canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top

                    + ScreenRate, paint);

            canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left

                    + ScreenRate, frame.bottom, paint);

            canvas.drawRect(frame.left, frame.bottom - ScreenRate,

                    frame.left + CORNER_WIDTH, frame.bottom, paint);

            canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,

                    frame.right, frame.bottom, paint);

            canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,

                    frame.right, frame.bottom, paint);


            //绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE

            slideTop += SPEEN_DISTANCE;

            if (slideTop >= frame.bottom) {

                slideTop = frame.top;

            }

//            canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH / 2, frame.right - MIDDLE_LINE_PADDING, slideTop + MIDDLE_LINE_WIDTH / 2, paint);
            Rect lineRect = new Rect();

            lineRect.left = frame.left;

            lineRect.right = frame.right;

            lineRect.top = slideTop;

            lineRect.bottom = slideTop + 18;

            canvas.drawBitmap(((BitmapDrawable) (getResources().getDrawable(R.drawable.line_scan))).getBitmap(), null, lineRect, paint);

            //画扫描框下面的字

            paint.setColor(ColorUtil.INSTANCE.getColor(R.color.bg_card_color));

            paint.setTextSize(TEXT_SIZE * density);


            paint.setTypeface(Typeface.create("System", Typeface.NORMAL));

            int leftTo = (frame.right - frame.left) / 2;
            paint.setTextAlign(Paint.Align.CENTER);

            textPaint.setColor(ColorUtil.INSTANCE.getColor(R.color.bg_card_color));
            textPaint.setTextSize(TEXT_SIZE * density);
            textPaint.setTypeface(Typeface.create("System", Typeface.NORMAL));
            textPaint.setTextAlign(Paint.Align.CENTER);

            StaticLayout layout = new StaticLayout(getResources().getString(R.string.scan_tip_aimToScan), textPaint, lineRect.right - lineRect.left,
                    Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
//            canvas.drawText(getResources().getString(R.string.scan_text), frame.centerX(), (float) (frame.bottom + (float) TEXT_PADDING_TOP * density), paint);
            canvas.save();
            canvas.translate(frame.centerX(), (float) (frame.bottom + (float) TEXT_PADDING_TOP * density));
            layout.draw(canvas);
            canvas.restore();



            //只刷新扫描框的内容，其他地方不刷新

            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,

                    frame.right, frame.bottom);


        }

    }


    public void drawViewfinder() {

        resultBitmap = null;

        invalidate();

    }


    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * <p>
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */

    public void drawResultBitmap(Bitmap barcode) {

        resultBitmap = barcode;

        invalidate();

    }


    public void addPossibleResultPoint(ResultPoint point) {

        possibleResultPoints.add(point);

    }


}
