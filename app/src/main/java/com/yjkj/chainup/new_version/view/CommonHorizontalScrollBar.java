package com.yjkj.chainup.new_version.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.yjkj.chainup.R;

public class CommonHorizontalScrollBar extends View {
    private Paint a;
    private final RectF b;
    private float c;
    private int d;
    private float e;
    private int f;
    private int g;

    public CommonHorizontalScrollBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CommonHorizontalScrollBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.a = new Paint();
        this.b = new RectF();
        this.e = getResources().getDimension(R.dimen.dp_2);
        this.f = getResources().getColor(R.color.trade_coin_select_nor);
        this.g = getResources().getColor(R.color.code_line_color);
        this.a.setAntiAlias(true);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.d == 0) {
            this.d = getWidth() / 2;
        }
        this.b.set(0.0f, 0.0f, (float) getWidth(), (float) getHeight());
        this.a.setColor(this.g);
        RectF rectF = this.b;
        float f2 = this.e;
        canvas.drawRoundRect(rectF, f2, f2, this.a);
        float f3 = this.c;
        int width = getWidth();
        int i = this.d;
        float f4 = f3 * ((float) (width - i));
        this.b.set(f4, 0.0f, ((float) i) + f4, (float) getHeight());
        this.a.setColor(this.f);
        RectF rectF2 = this.b;
        float f5 = this.e;
        canvas.drawRoundRect(rectF2, f5, f5, this.a);
    }

    public void setRate(float f2) {
        this.c = f2;
        float min = Math.min(f2, 1.0f);
        this.c = min;
        this.c = Math.max(min, 0.0f);
        invalidate();
    }
}