package com.yjkj.chainup.new_version.view;

/**
 * @Author: Bertking
 * @Date：2019/3/21-11:18 AM
 * @Description:
 */
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class LabelTextView extends androidx.appcompat.widget.AppCompatTextView {

    LabelViewHelper utils;
    public LabelTextView(Context context) {
        this(context, null);
    }

    public LabelTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        utils = new LabelViewHelper(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        utils.onDraw(canvas, getMeasuredWidth(), getMeasuredHeight(),false);
    }

    public void setLabelHeight(int height) {
        utils.setLabelHeight(this, height);
    }

    public int getLabelHeight() {
        return utils.getLabelHeight();
    }

    public void setLabelDistance(int distance) {
        utils.setLabelDistance(this, distance);
    }

    public int getLabelDistance() {
        return utils.getLabelDistance();
    }

    public boolean isLabelEnable() {
        return utils.isLabelVisual();
    }

    public void setLabelEnable(boolean enable) {
        utils.setLabelVisual(this, enable);
    }

    public int getLabelOrientation() {
        return utils.getLabelOrientation();
    }

    public void setLabelOrientation(int orientation) {
        utils.setLabelOrientation(this, orientation);
    }

    public int getLabelTextColor() {
        return utils.getLabelTextColor();
    }

    public void setLabelTextColor(int textColor) {
        utils.setLabelTextColor(this, textColor);
    }
    public void label_strokeColor(int textColor) {
        utils.setLabelTextColor(this, textColor);
    }

    public int getLabelBackgroundColor() {
        return utils.getLabelBackgroundColor();
    }

    public void setLabelBackgroundColor(int backgroundColor) {
        utils.setLabelBackgroundColor(this, backgroundColor);
    }
    public void label_backgroundColor(int backgroundColor) {
        utils.setLabelBackgroundColor(this, backgroundColor);
    }

    public String getLabelText() {
        return utils.getLabelText();
    }

    public void setLabelText(String text) {
        utils.setLabelText(this, text);
    }

    public int getLabelTextSize() {
        return utils.getLabelTextSize();
    }

    public void setLabelTextSize(int textSize) {
        utils.setLabelTextSize(this, textSize);
    }

    public int getLabelTextStyle(){
        return utils.getLabelTextStyle();
    }

    public void setLabelTextStyle(int textStyle){
        utils.setLabelTextStyle(this, textStyle);
    }

    public boolean isblack;
}