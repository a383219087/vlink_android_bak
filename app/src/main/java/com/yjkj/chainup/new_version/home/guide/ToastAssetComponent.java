package com.yjkj.chainup.new_version.home.guide;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.binioter.guideview.Component;
import com.yjkj.chainup.R;
import com.yjkj.chainup.new_version.home.GuideListener;
import com.yjkj.chainup.util.DisplayUtil;

/**
 * Created by binIoter on 16/6/17.
 */
public class ToastAssetComponent implements Component {
    private String message = "";
    private boolean isLeft = true;
    private GuideListener guideListener;

    public GuideListener getGuideListener() {
        return guideListener;
    }

    public void setGuideListener(GuideListener guideListener) {
        this.guideListener = guideListener;
    }

    public ToastAssetComponent(String message, boolean isLeft) {
        this.message = message;
        this.isLeft = isLeft;
    }

    @Override
    public View getView(LayoutInflater inflater) {

        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.guide_layout_asset_toast, null);
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
        return Component.FIT_END;
    }

    @Override
    public int getXOffset() {
        return 0;
    }

    @Override
    public int getYOffset() {
        return 10;
    }
}
