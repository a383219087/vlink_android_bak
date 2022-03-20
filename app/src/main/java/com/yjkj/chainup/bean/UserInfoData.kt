package com.yjkj.chainup.bean

import com.google.gson.annotations.SerializedName
/**
 * @Author lianshangljl
 * @Date 2020/10/22-4:04 PM
 * @Email buptjinlong@163.com
 * @description
 */



data class UserInfoData(
        @SerializedName("googleStatus") var googleStatus: Int,//是否开启googel验证
        @SerializedName("nickName") var nickName: String = "",
        @SerializedName("mobileNumber") var mobileNumber: String = "",
        @SerializedName("isCapitalPwordSet") var isCapitalPwordSet: Int,//是否开启资金密码验证
        @SerializedName("myMarket") var myMarket: List<Any> = listOf(),
        @SerializedName("feeCoinRate") var feeCoinRate: String = "",
        @SerializedName("useFeeCoinOpen") var useFeeCoinOpen: Int,
        @SerializedName("lastLoginIp") var lastLoginIp: String = "",
        @SerializedName("accountStatus") var accountStatus: Int,// 账户状态 0.正常 1.冻结交易，冻结提现 2冻结交易 3冻结提现
        @SerializedName("isOpenMobileCheck") var isOpenMobileCheck: Int,//是否开启手机验证
        @SerializedName("lastLoginTime") var lastLoginTime: String="",
        @SerializedName("realName") var realName: String = "",//实名认证的名字
        @SerializedName("feeCoin") var feeCoin: String="",
        @SerializedName("countryCode") var countryCode: String="",
        @SerializedName("gesturePwd") var gesturePwd: String="",
        @SerializedName("inviteUrl") var inviteUrl: String="",//邀请链接
        @SerializedName("userAccount") var userAccount: String="",
        @SerializedName("inviteCode") var inviteCode: String="",//邀请码
        @SerializedName("id") var id: Int,
        @SerializedName("notPassReason") var notPassReason: String = "",
        @SerializedName("authLevel") var authLevel: Int,//认证状态 0、未审核，1、通过，2、未通过  3未认证
        @SerializedName("email") var email: String = "",
        @SerializedName("creditGrade") var creditGrade: Double = 0.0//信用度
) {
    override fun toString(): String {
        return "UserInfoData(googleStatus=$googleStatus, nickName='$nickName', mobileNumber='$mobileNumber', isCapitalPwordSet=$isCapitalPwordSet, myMarket=$myMarket, feeCoinRate='$feeCoinRate', useFeeCoinOpen=$useFeeCoinOpen, lastLoginIp='$lastLoginIp', accountStatus=$accountStatus, isOpenMobileCheck=$isOpenMobileCheck, lastLoginTime='$lastLoginTime', realName='$realName', feeCoin='$feeCoin', countryCode='$countryCode', gesturePwd='$gesturePwd', inviteUrl='$inviteUrl', userAccount='$userAccount', inviteCode='$inviteCode', id=$id, notPassReason='$notPassReason', authLevel=$authLevel, email='$email', creditGrade=$creditGrade)"
    }
}