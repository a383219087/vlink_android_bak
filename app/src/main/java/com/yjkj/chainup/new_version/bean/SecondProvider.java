package com.yjkj.chainup.new_version.bean;


import android.os.Bundle;
import android.view.View;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.yjkj.chainup.R;
import com.yjkj.chainup.db.constant.ParamConstant;
import com.yjkj.chainup.db.constant.RoutePath;
import com.yjkj.chainup.db.constant.WebTypeEnum;
import com.yjkj.chainup.extra_service.arouter.ArouterUtil;

import org.jetbrains.annotations.NotNull;

public class SecondProvider extends BaseNodeProvider {

    @Override
    public int getItemViewType() {
        return 2;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_node_second;
    }

    @Override
    public void convert(@NotNull BaseViewHolder helper, @NotNull BaseNode data) {
        SecondNode entity = (SecondNode) data;
        helper.setText(R.id.title, entity.getTitle());
    }

    @Override
    public void onClick(@NotNull BaseViewHolder helper, @NotNull View view, BaseNode data, int position) {
        super.onClick(helper, view, data, position);

        SecondNode entity = (SecondNode) data;
        Bundle bundle = new Bundle();
        bundle.putString(ParamConstant.head_title, entity.getTitle());
        bundle.putString(ParamConstant.web_url, entity.getFileName());
        bundle.putInt(ParamConstant.web_type, WebTypeEnum.HELP_CENTER.getValue());
        ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle);
    }
}
