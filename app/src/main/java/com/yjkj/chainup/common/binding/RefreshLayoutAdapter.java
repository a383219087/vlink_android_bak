package com.yjkj.chainup.common.binding;


import androidx.databinding.BindingAdapter;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.yjkj.chainup.common.binding.command.BindingCommand;


public class RefreshLayoutAdapter {

    @BindingAdapter(value = {"onRefreshCommand", "onLoadMoreCommand"}, requireAll = false)
    public static void onRefreshAndLoadMoreCommand(SmartRefreshLayout layout, final BindingCommand onRefreshCommand, final BindingCommand onLoadMoreCommand) {


        layout.setOnRefreshListener(refreshLayout -> {
            if (onRefreshCommand != null) {
                onRefreshCommand.execute();
            }

        });
        if (onLoadMoreCommand != null) {
            layout.setEnableLoadMore(true);
            layout.setOnLoadMoreListener(refreshLayout -> {
                onRefreshCommand.execute();
            });

        }

    }
}
