package com.yjkj.chainup.bean


data class AgentCodeBean(
    val ctime: String,
    val remark: String,
    val id: String,
    val inviteCode: String,
    val mtime: String,
    val rate: String,
    var rateInt: Int,
    val uid: String
)