package com.yjkj.chainup.common.binding.command;


import androidx.databinding.ObservableList;

/**
 * Created by 奔跑的狗子
 * on 2021/05/17.
 */
public class ObservableListCommand<T> {



    public ObservableListCommand(ObservableList<T> items, BindingAction execute) {
        items.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<T>>() {
            @Override
            public void onChanged(ObservableList<T> sender) {
                if (execute!=null){
                    execute.call();
                }

            }

            @Override
            public void onItemRangeChanged(ObservableList<T> sender, int positionStart, int itemCount) {
                if (execute!=null){
                    execute.call();
                }
            }

            @Override
            public void onItemRangeInserted(ObservableList<T> sender, int positionStart, int itemCount) {
                if (execute!=null){
                    execute.call();
                }
            }

            @Override
            public void onItemRangeMoved(ObservableList<T> sender, int fromPosition, int toPosition, int itemCount) {
                if (execute!=null){
                    execute.call();
                }
            }

            @Override
            public void onItemRangeRemoved(ObservableList<T> sender, int positionStart, int itemCount) {
                if (execute!=null){
                    execute.call();
                }
            }
        });

    }

}
