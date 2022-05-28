package com.yjkj.chainup.common.binding;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.yjkj.chainup.R;
import com.yjkj.chainup.common.binding.command.BindingCommand;
import com.yjkj.chainup.wedegit.VerticalTextview4ChainUp;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by 奔跑的狗子
 * on 2021/04/09.
 */
public class VerticalViewAdapter {


    /**
     * 滚动播报
     */
    @BindingAdapter(value = {"verticalTextviewListText","setOnItemClickListener"}, requireAll = false)
    public static void verticalTextviewListText(VerticalTextview4ChainUp view, String text , final BindingCommand setOnItemClickListener) {
        if (text==null||text.isEmpty()){
            return;
        }
        ArrayList<String> list = new ArrayList<>(Arrays.asList(text.split(",,,")));
        view.setText(12f, 0, ContextCompat.getColor(view.getContext(), R.color.text_color), true);
        view.setTextStillTime(4000);//设置停留时长间隔
//        view.setAnimTime(400);//设置进入和退出的时间间隔/
        view.setTextList(list);
        view.startAutoScroll();
        view.setOnItemClickListener(position -> {
         if (setOnItemClickListener!=null){
             setOnItemClickListener.execute(position);
         }

        });

    }


}
