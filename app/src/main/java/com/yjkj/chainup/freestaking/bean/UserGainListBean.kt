package com.yjkj.chainup.freestaking.bean
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
data class UserGainListBean(var gainAmount:BigDecimal= BigDecimal.ZERO, var gainTime: String? = null,var gainCoin:String?=null) : Parcelable
