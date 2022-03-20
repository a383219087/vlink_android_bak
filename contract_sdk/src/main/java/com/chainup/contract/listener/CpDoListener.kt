package com.chainup.contract.listener

/**
 * @author ZhongWei
 * @time 2020/7/2 12:07
 * @description 执行事件
 **/
interface CpDoListener {

    /**
     * 执行操作
     */
    fun doThing(obj: Any? = null): Boolean

}