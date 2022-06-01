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
    val currentFollowCount: Int,
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
    val nickname: String,
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


//    "positionList":[
//{
//    "id":1,
//    "contractId":1,
//    "contractName":"E-BTC-USDT",
//    "symbol":"BTC-USDT",
//    "positionVolume":10,
//    "canCloseVolume":10,
//    "openAvgPrice":0,
//    "indexPrice":0,
//    "reducePrice":null,
//    "holdAmount":10000083.45,
//    "marginRate":null,
//    "realizedAmount":100,
//    "returnRate":0,
//    "orderSide":"BUY",
//    "positionType":1,
//    "canUseAmount":9999983.45,
//    "canSubMarginAmount":0,
//    "openRealizedAmount":0,
//    "keepRate":0,
//    "maxFeeRate":0.05,
//    "unRealizedAmount":0,
//    "leverageLevel":1,
//    "positionBalance":0,
//    "tradeFee":"0",
//    "capitalFee":"0",
//    "closeProfit":"0",
//    "settleProfit":"0",
//    "shareAmount":"0",
//    "historyRealizedAmount":"0",
//    "profitRealizedAmount":"100",
//    "openAmount":0,
//    "coPosition":{
//    "id":1,
//    "uid":10027,
//    "contractId":1,
//    "positionType":1,
//    "side":"BUY",
//    "volume":10,
//    "openPrice":0,
//    "avgPrice":0,
//    "closePrice":0,
//    "leverageLevel":1,
//    "openAmount":0,
//    "holdAmount":0,
//    "closeVolume":10,
//    "pendingCloseVolume":0,
//    "realizedAmount":100,
//    "historyRealizedAmount":0,
//    "tradeFee":0,
//    "capitalFee":0,
//    "closeProfit":0,
//    "shareAmount":0,
//    "freezeLock":0,
//    "status":0,
//    "ctime":"2022-04-24T21:02:23",
//    "mtime":"2022-05-21T17:32:29",
//    "brokerId":1,
//    "lockTime":"2022-04-24T21:02:23"
//}
//}

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
    val date: String,
    val amount: Double,
    val followerAmount: Int,
    val profit: Double,
    val followerCount: Int

) : Serializable

data class GetAssetsTotalBean(
    val canUseAmount: Int,
    val allAmount: Double,
    val isolateMargin: Int,
    val lockAmount: Int,
    val realizedAmount: Int,
    val symbol: String,
    val totalAmount: Int,
    val totalMargin: Int,
    val totalMarginRate: Int,
    val unRealizedAmount: Int

) : Serializable
data class FollowerStatisticsBean(
    val allAmount: Double,
    val allReturnRate: Double,
    val allTradeVolume: Int,
    val ctime: String,
    val highFrequentIndex: Double,
    val lossNum: Int,
    val mtime: String,
    val perAmount: Double,
    val perAmountPoints: Double,
    val perAverageLoss: Int,
    val perAverageProfit: Double,
    val perLossPoints: Int,
    val perWinPoints: Double,
    val positionTime: Int,
    val profitNum: Int,
    val profitRate: Int,
    val totalAmount: Int,
    val tradeFrequency: Int,
    val tradeNum: Int,
    val uid: Int
) : Serializable

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
) : Serializable















