package com.yjkj.chainup.common.binding;


import androidx.core.widget.NestedScrollView;
import androidx.databinding.BindingAdapter;

import com.yjkj.chainup.R;
import com.yjkj.chainup.common.binding.command.BindingCommand;


/**
 * Created by 奔跑的狗子
 * on 2021/04/09.
 */
public class NestedScrollViewAdapter {

    @BindingAdapter(value = {"setOnScrollChangeListener"}, requireAll = false)
    public static void addOnPageChangeListener(NestedScrollView layout, final BindingCommand<Boolean> setOnScrollChangeListener) {
         layout.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
              double distance=layout.getContext().getResources().getDimension(R.dimen.dp_64);
              if (setOnScrollChangeListener!=null){
                  if ((1 - scrollY / distance) < (0.0001)) {
                      setOnScrollChangeListener.execute(true);
                  } else {
                      setOnScrollChangeListener.execute(false);
                  }
              }

         });

    }

}
