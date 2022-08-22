package com.chainup.contract.utils


/**
 * @author ZhongWei
 * @time 2020/7/1 17:37
 * @description 合约买或卖数据辅助类 暂用
 **/
class CpContractBuyOrSellHelper {

    companion object {

        /**
         * 限价
         */
        const val CONTRACT_ORDER_LIMIT = 0

        /**
         * 市价
         */
        const val CONTRACT_ORDER_MARKET = 1

        /**
         * 计划
         */
        const val CONTRACT_ORDER_PLAN = 2

        /**
         * 买一价
         */
        const val CONTRACT_ORDER_BID_PRICE = 3

        /**
         * 卖一价
         */
        const val CONTRACT_ORDER_ASK_PRICE = 4

        /**
         * 限价(高级委托)
         */
        const val CONTRACT_ORDER_ADVANCED_LIMIT = 5

        /**
         * 仅有止盈条件
         */
        const val CONDITIONCOMMISSIONORDER_TYPE_PROFIT = 1

        /**
         * 仅有止损条件
         */
        const val CONDITIONCOMMISSIONORDER_TYPE_LOSS = 2

        /**
         * 止盈止损都有
         */
        const val CONDITIONCOMMISSIONORDER_TYPE_ALL = 3
    }

    /**
     * true 买
     * false 卖
     */
    var isBuy: Boolean = true

    /**
     * 0 开仓
     * 1 平仓
     */
    var tradeType: Int = 0

    var orderType: Int = 1

    var isOpen: Boolean = true
    var isOneWayPosition: Boolean = false
    var isOto: Boolean = false


    var rivalPricePosition: Int = 0




    /**
     * 价格类型
     */
    var priceType: Int = 0


    /**
     * 对手价类型
     */
    var rivalPriceType: Int = 0


    /**
     * 输入数量
     */
    var etPrice: String? = null

    /**
     * 输入数量
     */
    var etPosition: String? = null



}