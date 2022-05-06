package com.yjkj.chainup.bean

import java.io.Serializable


data class ProjectBean(
    val activeStatus: Int,
    val baseCoin: String,
    val buyAmountAll: Double,
    val buyAmountMax: Double,
    val buyAmountMin: Double,
    val gainCoin: String,
    val gainRate: Double,
    val id: Int,
    val labelType: Int,
    val lockDay: Int,
    val logo: String,
    val name: String,
    val progress: String,
    val projectType: Int,
    val raiseAmount: Double

) : Serializable

data class ProjectInfo(
    val activeStatus: Int,
    val banner: String,
    val baseCoin: String,
    val buyAmountMax: Double,
    val buyAmountMin: Double,
    val currentDate: String,
    val details: String,
    val endApplyTime: String,
    val gainCoin: String,
    val gainEndTime: String,
    val gainRate: Double,
    val gainStartTime: String,
    val info: String,
    val isShowBuy: Int,
    val lockDay: Int,
    val logo: String,
    val name: String,
    val projectType: Int,
    val startApplyTime: String,
    val tipMine: String,
    val title: String,
    val url: String,
    val userNormalAmount: Double


) : Serializable
data class MyPos(
    val comCoin: String,
    val count: Int,
    val countryCoin: String,
    val page: Int,
    val pageSize: Int,
    val posList: List<Pos>,
    val tipLock: String,
    val tipMine: String,
    val tipNormal: String,
    val tipStatus: String,
    val totalUserCurrentAmount: Double,
    val totalUserCurrentAmountCom: Double,
    val totalUserGainAmount: Double,
    val totalYesterdayUserGainAmount: Double



) : Serializable


data class Pos(
    val activeStatus: Int,
    val almostUserGainAmount: Double,
    val baseCoin: String,
    val currentDate: String,
    val endApplyTime: String,
    val gainCoin: String,
    val gainEndTime: String,
    val gainRate: Double,
    val gainStartTime: String,
    val isAutoApply: Int,
    val isShowBuy: Int,
    val projectId: Int,
    val projectName: String,
    val projectType: Int,
    val redeemStatus: Int,
    val startApplyTime: String,
    val userCurrentAmount: Double,
    val userGainAmount: Double,
    val userNormalAmount: Double,
    val yesterdayUserGainAmount: Double
): Serializable


data class IncrementActList(
    val detailList: List<IncrementActDetail>

) : Serializable

data class IncrementActDetail(
    val amount: String,
    val time: String,
    val timeLong: Long,
    val type: String




) : Serializable









