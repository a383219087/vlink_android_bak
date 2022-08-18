package com.yjkj.chainup.bean

import com.chainup.contract.bean.CpContractPositionBean
import java.io.Serializable




data class CommissionBean(
    val ctime: Long,
    val follow: Int,
    val followerCount: Int,
    val currentFollowCount: Int,
    val mtime: Long,
    val orderCount: Int,
    val profitRatio: Double,
    val profitUSDT: Double,
    val searchKey: String,
    val totalRatio: Double,
    val totalUSDT: Double,
    val uid: Int,
    val userCreateTime: Long,
    val winRatio: Double,
    val nickname: String,
    val description: String?,
    val entryDay: String,
    val followerUid: String,
    val type: Int,
    val amount: String,
    val rate: String,
    val deposit: String,
    val lossRatio: Double,
    val status: String,
    val followAmount: String


) : Serializable
data class CommissionBean1(
    val amount: Double,
    val ctime: Long,
    val currentFollowCount: Int,
    val deposit: Double,
    val followAmount: Any,
    val followerUid: Int,
    val lossRatio: Double,
    val mtime: Long,
    val nickname: String,
    val profitRatio: Double,
    val profitUSDT: Double,
    val rate: Double,
    val status: Int,
    val type: Int,
    val uid: Int


) : Serializable


data class CurrentStatusBean(
    val uid: Long,
    val id: Int,
    val status: Int


) : Serializable


data class StatisticsBean(
    val followTotalAmount: String,
    val rate: Double,
    val profit: String


) : Serializable

data class TraderPositionInfo(



    val positionList: List<CpContractPositionBean>?


) : Serializable

data class TraderTransactionInfo(

    val records: List<TraderTransactionBean>?


) : Serializable

//"coinSymbol": "USDT",
//"date": "2022-05-26",
//"amount": 2.9012415061E-4
data class TraderTransactionBean(
    val contractConfig: ContractConfig,
    val ctime: String,
    val follower: String,
    val coinSymbol: String,
    val contractName: String,
    val date: String,
    val leverageLevel: String,
    val amount: String="0.0",
    val followerAmount: String="0.0",
    val profit: String="0.0",
    val followerCount: String

) : Serializable

data class GetAssetsTotalBean(
    val canUseAmount: String="0.0",
    val allAmount: String="0.0",
    val allReturnRate: String="0.0",
    val positionBalance: String="0.0",
    val isolateMargin: String,
    val lockAmount: String="0.0",
    val realizedAmount: String="0.0",
    val symbol: String,
    val totalAmount: String="0.0",
    val totalMargin: String="0.0",
    val totalMarginRate: String,
    val unRealizedAmount: String="0.0",
    val result: String

) : Serializable
data class FollowerStatisticsBean(
    val amount: Double,
    val fee: String,
    val id: String,
    val name: String,
    val orderId: String,
    val price: String,
    val scene: String,
    val side: String,
    val tradeTime: String,
    val type: String,
    val volume: String
) : Serializable

data class ContractConfig(
    val base: Any,
    val brokerId: Int,
    val capitalDepthMoney: Double,
    val volume: String="",
    val capitalFrequency: Int,
    val capitalIntervalMax: Double,
    val capitalIntervalMin: Double,
    val capitalPremiumMax: Double,
    val capitalPremiumMin: Double,
    val capitalRate: Double,
    val capitalStartTime: Int,
    val closeMakerFee: Double,
    val closeTakerFee: Double,
    val configLadder: Any,
    val configLever: Any,
    val contractName: String,
    val contractOtherName: String,
    val contractSide: Int,
    val contractSideDesc: Any,
    val contractType: String,
    val contractTypeName: Any,
    val ctime: String,
    val deliveryKind: String,
    val id: Int,
    val ladderConfigId: Int,
    val leverConfigId: Int,
    val lossSubsidy: Double,
    val marginCoin: String,
    val marginRate: Double,
    val marginRateType: Int,
    val minMakerFee: Double,
    val minTakerFee: Double,
    val mtime: String,
    val multiplier: Double,
    val multiplierCoin: String,
    val openMakerFee: Double,
    val openTakerFee: Double,
    val positive: Boolean,
    val quote: Any,
    val quoteValue: Double,
    val quoteValueCoin: String,
    val riskAlarm: Double,
    val settlementFrequency: Int,
    val simpleSymbol: String,
    val sort: Any,
    val status: Int,
    val statusDesc: Any,
    val symbol: String
) : Serializable















