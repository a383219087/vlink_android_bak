package com.chainup.contract.bean

import java.io.Serializable



//{
//    "id":23,
//    "uid":501,
//    "contractId":1,
//    "positionType":2,
//    "side":"SELL",
//    "volume":1718,
//    "openPrice":29312.807195,
//    "avgPrice":29329.7,
//    "closePrice":0,
//    "leverageLevel":125,
//    "openAmount":0,
//    "holdAmount":0,
//    "closeVolume":0,
//    "pendingCloseVolume":0,
//    "realizedAmount":0,
//    "historyRealizedAmount":-0.75340549390237,
//    "tradeFee":0,
//    "capitalFee":2.14877840509763,
//    "closeProfit":0,
//    "shareAmount":0,
//    "freezeLock":0,
//    "status":1,
//    "ctime":"2022-05-19T14:10:07",
//    "mtime":"2022-05-21T17:21:54",
//    "brokerId":1,
//    "lockTime":"2022-05-19T14:10:07",
//    "reducePrice":29191.042547897486937,
//    "returnRate":0,
//    "unRealizedAmount":-2.5622920584136131056,
//    "openRealizedAmount":-2.5622920584136131056,
//    "positionBalance":5038.84246,
//    "indexPrice":29327.721584164223592,
//    "keepRate":0.004,
//    "maxFeeRate":0.00075
//}
data class CpContractPositionBean(
    /**
     * 可平数量
     */
    val canCloseVolume: String,
    /**
     * 可减少保证金
     */
    val canSubMarginAmount: String,
    /**
     * 可用
     */
    val canUseAmount: String,
    /**
     * 合约名称
     */
    val contractName: String,
    /**
     * 标记价格
     */
    val indexPrice: String,
    val isolatedMargin: String,
    /**
     * 保证金率
     */
    val marginRate: String,
    /**
     * 开仓均价
     */
    val openAvgPrice: String,
    /**
     * 持仓方向 BUY 多仓, SELL 空仓
     */
    val orderSide: String,
    /**
     * 持仓类型(1 全仓，2 仓逐)
     */
    val positionType: Int,
    /**
     * 合约ID
     */
    val contractId: Int,
    /**
     * 仓位id
     */
    val id: Int,
    /**
     * 杠杆倍数
     */
    val leverageLevel: Int,
    /**
     * 仓位
     */
    val positionVolume: String,
    /**
     * 已实现盈亏
     */
    val realizedAmount: String,
    /**
     * 未实现盈亏
     */
    val unRealizedAmount: String,
    /**
     * 强平价格
     */
    val reducePrice: String,
    /**
     * 回报率
     */
    val returnRate: Double,
    /**
     * 合约币对
     */
    val symbol: String,
    /**
     * 阶梯最低维持保证金率
     */
    val keepRate: String,
    /**
     * 平仓最大手续费率
     */
    val maxFeeRate: String,
    /**
     * 开仓未实现盈亏
     */

    val openRealizedAmount: String,
    /**
     * 持仓保证金
     */
    val holdAmount: String,
    val totalMargin: String,
    /**
     * 交易手续费
     */
    val tradeFee: String,
    /**
     * 资金费用
     */
    val capitalFee: String,
    /**
     * 平仓盈亏
     */
    val closeProfit: String,
    /**
     * 分摊金额
     */
    val shareAmount: String,

    /**
     * 持仓结算
     */
    val settleProfit: String,
    /**
     * 结算盈亏
     */
    val profitRealizedAmount: String,
    /**
     * 持仓均价
     */
    val avgPrice: Double,
    /**
     * 商户id，1表示SAAS总平台
     */
    val brokerId: Int,

    /**
     * 平仓均价
     */
    val closePrice: Double,

    /**
     * 已平仓数量
     */
    val closeVolume: Int,

    /**
     * 创建时间
     */
    val ctime: String,
    /**
     * 持仓冻结状态：0 正常，1爆仓冻结，2 交割冻结
     */
    val freezeLock: Int,
    /***
     * 历史累计已实现盈亏
     */
    val historyRealizedAmount: Double,

    /**
     * 爆仓锁仓时间
     */
    val lockTime: String,
    /**
     * 更新时间
     */
    val mtime: String,

    /**
     * 开仓保证金（包括变化量）
     */
    val openAmount: Double,
    /**
     * 开仓价格
     */
    val openPrice: Double,
    /**
     * 已挂出平仓单的数量
     */
    val pendingCloseVolume: Int,
    /**
     * 仓位价值
     */
    val positionBalance: Double,

    /**
     * 持仓方向
     */
    val side: String,
    /**
     * 仓位有效性，0无效 1有效
     */
    val status: Int,
    /**
     * 用户ID
     */
    val uid: Int,
    /**
     * 持仓数量
     */
    val volume: Double
) : Serializable