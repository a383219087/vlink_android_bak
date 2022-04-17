package com.yjkj.chainup.bean

import java.io.Serializable


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
    val winRatio: Double



):Serializable

data class CurrentStatusBean(
    val uid: Long,
    val id: Int,
    val status: Int


):Serializable













