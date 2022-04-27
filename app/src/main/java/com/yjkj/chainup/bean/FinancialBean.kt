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















