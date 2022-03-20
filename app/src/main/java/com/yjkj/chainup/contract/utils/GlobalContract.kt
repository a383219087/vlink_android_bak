package com.yjkj.chainup.contract.utils

import com.contract.sdk.data.Contract

/**
 * @author ZhongWei
 * @time 2020/9/1 10:37
 * @description
 **/
object GlobalContract {

    /**
     * Contract
     */
    lateinit var contract: Contract

    /**
     * 绑定Contract
     */
    fun bindContract(contract: Contract){
        this.contract = contract
    }

    /**
     * 是否开仓
     */
    var isOpen = true

    /**
     * 是否平仓
     */
    fun isClose(): Boolean{
        return !isOpen
    }

    /**
     * 是否买入
     */
    var isBuy = true

    /**
     * 是否卖出
     */
    fun isSell(): Boolean{
        return !isBuy
    }






}