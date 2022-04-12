package com.yjkj.chainup.common.binding;

import androidx.databinding.BindingAdapter;

import com.yjkj.chainup.common.binding.command.BindingCommand;
import com.yjkj.chainup.new_version.view.CommonlyUsedButton;


public class CommonlyUsedButtonAdapter {


    /**
     * 显示图片
     */
    @BindingAdapter("listener")
    public static void onCommonlyUsedButton(CommonlyUsedButton view, final BindingCommand onCommand) {
        view.setListener1(v -> {
            if (onCommand!=null){
                onCommand.execute(v);
            }

        });
    }



}
