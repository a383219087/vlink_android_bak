package com.yjkj.chainup.bean

import com.chainup.contract.bean.CpContractPositionBean
import java.io.Serializable
import java.time.LocalDateTime


data class CommissionBean(
    val ctime: Long,
    val follow: Int,
    val followerCount: Int,
    val mtime: Long,
    val orderCount: Int,
    val profitRatio: Double,
    val profitUSDT: Double,
    val searchKey: Int,
    val totalRatio: Double,
    val totalUSDT: Double,
    val uid: Int,
    val userCreateTime: Long,
    val winRatio: Double,
    val nickName: String



):Serializable

data class CurrentStatusBean(
    val uid: Long,
    val id: Int,
    val status: Int


):Serializable
data class TraderPositionInfo(
//    val records: List<TraderPositionBean>?
    val records: List<CpContractPositionBean>?



) : Serializable








data class TraderPositionBean(
    /**
     * 持仓均价
     */
    val avgPrice: Double,
    /**
     * 商户id，1表示SAAS总平台
     */
    val brokerId: Int,
    /**
     * 资金费用
     */
    val capitalFee: Int,
    /**
     * 平仓均价
     */
    val closePrice: Double,
    /**
     * 平仓盈亏
     */
    val closeProfit: Int,
    /**
     * 已平仓数量
     */
    val closeVolume: Int,
    /**
     * 合约ID
     */
    val contractId: Int,
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
     * 持仓保证金
     */
    val holdAmount: Int,
    val id: Int,
    val indexPrice: Double,
    val keepRate: Double,
    /**
     * 杠杆倍数
     */
    val leverageLevel: Int,
    /**
     * 爆仓锁仓时间
     */
    val lockTime: String,
    val maxFeeRate: Double,
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
    val openRealizedAmount: Double,
    /**
     * 已挂出平仓单的数量
     */
    val pendingCloseVolume: Int,
    val positionBalance: Double,

    /**
     * 持仓类型(1 全仓，2 仓逐)
     */
    val positionType: Int,
    /**
     * 已实现盈亏
     */
    val realizedAmount: Int,
    val returnRate: Double,
    /**
     * 分摊金额
     */
    val shareAmount: Int,
    /**
     * 持仓方向
     */
    val side: String,
    /**
     * 仓位有效性，0无效 1有效
     */
    val status: Int,
    /**
     * 交易手续费
     */
    val tradeFee: Int,
    /**
     * 用户ID
     */
    val uid: Int,
    val unRealizedAmount: Double,
    /**
     * 持仓数量
     */
    val volume: Double

):Serializable









