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
    val txCount: Int,
    var index: Int

):Serializable

data class MyNextInvite(
    val accountAmountStr: Any,
    val auditStatus: Int,
    val auditTime: Long,
    val bonusAmountStr: Any,
    val childRoleId: Int,
    val childRoleName: String,
    val ctime: Long,
    val feeAmountStr: Any,
    val id: Int,
    val inviteCode: String,
    val level: Int,
    val mtime: Any,
    val pid: Int,
    val roleId: Int,
    val roleName: String,
    val status: Int,
    val takeDate: Long,
    val type: Int,
    val uid: Int,
    val uidPath: String
):Serializable

data class InviteRate(
    val childId: Any,
    val childName: Any,
    val ctime: Long,
    val id: Int,
    val isLeader: Int,
    val mtime: Long,
    val optionType: Int,
    val rate: Int,
    var checkRate: Int,
    val roleName: String,
    val status: Int,
    val symbolId: Int,
    val symbolName: Any
):Serializable
