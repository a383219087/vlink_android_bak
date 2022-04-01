package com.yjkj.chainup.common.binding.command;

/**
 * Created by 奔跑的狗子
 * on 2021/04/09.
 */
public interface BindingConsumer<T> {
    void call(T t);
}
