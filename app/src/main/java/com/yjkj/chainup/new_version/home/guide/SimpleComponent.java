package com.yjkj.chainup.new_version.home.guide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.binioter.guideview.Component;
import com.yjkj.chainup.R;
import com.yjkj.chainup.app.ChainUpApp;
import com.yjkj.chainup.manager.LanguageUtil;
import com.yjkj.chainup.new_version.home.AdvertDataService;
import com.yjkj.chainup.new_version.home.GuideListener;
import com.yjkj.chainup.util.DisplayUtil;
import com.yjkj.chainup.util.StringOfExtKt;

/**
 * Created by binIoter on 16/6/17.
 */
public class SimpleComponent implements Component {
    private int index = 1;
    private int count = 1;
    private String type;
    private GuideListener guideListener;
    private Context context;

    public GuideListener getGuideListener() {
        return guideListener;
    }

    public void setGuideListener(GuideListener guideListener) {
        this.guideListener = guideListener;
    }

    public SimpleComponent(Context context, int index, int count, String type) {
        this.index = index;
        this.count = count;
        this.type = type;
        this.context = context;
    }

    @Override
    public View getView(LayoutInflater inflater) {

        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.guide_layout_search, null);
        TextView txTips = ll.findViewById(R.id.tx_tips);
        TextView tvTitle = ll.findViewById(R.id.tv_title);
        TextView tvNext = ll.findViewById(R.id.tv_progress);
        ImageView imType = ll.findViewById(R.id.im_src);
        View image = ll.findViewById(R.id.view_arrow);
        int item = DisplayUtil.INSTANCE.dip2px(16);
        int itemRight = DisplayUtil.INSTANCE.dip2px(6);
        String guideIndex = LanguageUtil.getString(context, "common_guide_skip_hint");
        if (type.equals("search")) {
            imType.setVisibility(View.GONE);
        } else if (type.equals("notice")) {
            imType.setVisibility(View.GONE);
        } else if (type.equals("symbolTop")) {
            imType.setVisibility(View.VISIBLE);
            imType.setImageResource(R.mipmap.gesture_currencybit);
        } else if (type.equals("cmsAppList")) {
            imType.setVisibility(View.VISIBLE);
            imType.setImageResource(R.mipmap.gesture_shortcutfunction);
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) image.getLayoutParams();
        if (type.equals("notice")) {
            layoutParams.setMarginEnd(itemRight);
            layoutParams.gravity = 5;
        } else {
            layoutParams.gravity = 16;
            layoutParams.setMarginStart(item);
        }
        image.setLayoutParams(layoutParams);
        tvTitle.setText(StringOfExtKt.getByTypeTips(type,context));
        txTips.setText(String.format(guideIndex + " (%s/%s)", index + 1, count));
        txTips.setOnClickListener(view -> {
            if (guideListener != null) {
                // 处理跳过流程
                AdvertDataService.Companion.getInstance().saveSkipGuide(AdvertDataService.KEY_GUIDE_HOME_SKIP);
                guideListener.onDismiss();
            }
        });
        tvNext.setOnClickListener(view -> {
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
        if (type.equals("notice")) return Component.FIT_END;
        else return Component.FIT_START;
    }

    @Override
    public int getXOffset() {
        if (type.equals("symbolTop") || type.equals("cmsAppList")) return 16;
        else return 0;
    }

    @Override
    public int getYOffset() {
        return 10;
    }
}
