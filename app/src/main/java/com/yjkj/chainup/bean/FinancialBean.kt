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
















