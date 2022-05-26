package com.yjkj.chainup.bean

import com.chainup.contract.bean.CpContractPositionBean
import java.io.Serializable


data class QueryTraderBean(
    val traderUser: CommissionBean,
    val my: CommissionBean


    ) : Serializable

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
    val nickName: String,
    val entryDay: String


) : Serializable

data class InviteCodeBean(
    val ctime: Long,
    val id: Int,
    val inviteCode: String,
    val inviteLevel: Int,
    val isDefault: Int,
    val mtime: Long,
    val rate: Int,
    val remark: String,
    val uid: Int
)

data class CurrentStatusBean(
    val uid: Long,
    val id: Int,
    val status: Int


) : Serializable

data class TraderPositionInfo(

    val records: List<CpContractPositionBean>?


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
    val date: String,
    val amount: Double,
    val followerAmount: Int,
    val profit: Double

): Serializable

data class ContractConfig(
    val base: Any,
    val brokerId: Int,
    val capitalDepthMoney: Double,
    val volume: String,
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
): Serializable















