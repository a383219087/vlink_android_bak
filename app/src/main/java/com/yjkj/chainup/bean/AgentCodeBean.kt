package com.yjkj.chainup.bean

import java.io.Serializable


data class AgentCodeBean(
    val ctime: String,
    val remark: String,
    val id: String,
    val inviteCode: String,
    val mtime: String,
    val rate: String,
    var rateInt: Int,
    val uid: String,
    val isDefault: String
):Serializable


data class InviteBean(
    val amount: Double,
    val uid: Int,
    val userCount: Int,
    var index: Int

):Serializable