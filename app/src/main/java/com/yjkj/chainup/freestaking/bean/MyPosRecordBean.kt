package com.yjkj.chainup.freestaking.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

data class MyPosRecordBean(var tipMine: String? = null,
                           var tipNormal: String? = null,
                           var tipLock: String? = null,
                           var tipStatus: String? = null,
                           var count: Int = 0,
                           var pageSize: Int = 0,
                           var page: Int = 0,
                           var posList: ArrayList<PosListBean>) {
    @Parcelize
    data class PosListBean(var gainAmount: BigDecimal = BigDecimal.ZERO,
                           var revenueTime: String? = null,
                           var baseAmount: BigDecimal = BigDecimal.ZERO,
                           var totalUserGainAmount: BigDecimal = BigDecimal.ZERO,
                           var ltime: String? = null,
                           var totalAmount: BigDecimal = BigDecimal.ZERO,
                           var projectStatus: String? = null,
                           var gainRate: String? = null,
                           var baseCoin: String? = null,
                           var userGainList: ArrayList<UserGainListBean>) : Parcelable


}
