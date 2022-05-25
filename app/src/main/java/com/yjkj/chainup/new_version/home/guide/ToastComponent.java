package com.yjkj.chainup.new_version.home.guide;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.binioter.guideview.Component;
import com.yjkj.chainup.R;
import com.yjkj.chainup.new_version.home.GuideListener;

/**
 * Created by binIoter on 16/6/17.
 */
public class ToastComponent implements Component {
    private String message = "";
    private String isLeft = "";
    private GuideListener guideListener;

    public GuideListener getGuideListener() {
        return guideListener;
    }

    public void setGuideListener(GuideListener guideListener) {
        this.guideListener = guideListener;
    }

    public ToastComponent(String message, String isLeft) {
        this.message = message;
        this.isLeft = isLeft;
    }

    @Override
    public View getView(LayoutInflater inflater) {

        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.guide_layout_toast, null);
        TextView txTips = ll.findViewById(R.id.tv_title);
        txTips.setText(message);
        ll.setOnClickListener(view -> {
            if (guideListener != null) {
                guideListener.onDismiss();
            }
        });
        return ll;
    }

    @Override
    public int getAnchor() {
        return Component.ANCHOR_BOTTOM;
    }

    @Override
    public int getFitPosition() {
        return Component.FIT_START;
    }

    @Override
    public int getXOffset() {
        if (isLeft.equals("common_guide_coin_recommend_hint")) return 16;
        else return 0;
    }

    @Override
    public int getYOffset() {
        return 10;
    }
}
