package com.yjkj.chainup.new_version.adapter;


import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;
import com.yjkj.chainup.new_version.bean.FirstNode;
import com.yjkj.chainup.new_version.bean.FirstProvider;
import com.yjkj.chainup.new_version.bean.SecondNode;
import com.yjkj.chainup.new_version.bean.SecondProvider;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class HelpCenterNewAdapter extends BaseNodeAdapter {

    public static final int EXPAND_COLLAPSE_PAYLOAD = 110;


    public HelpCenterNewAdapter() {
        super();
        addNodeProvider(new FirstProvider());
        addNodeProvider(new SecondProvider());
    }

    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> list, int i) {
        BaseNode node = list.get(i);
        if (node instanceof FirstNode) {
            return 1;
        } else if (node instanceof SecondNode) {
            return 2;
        }
        return -1;
    }
}
