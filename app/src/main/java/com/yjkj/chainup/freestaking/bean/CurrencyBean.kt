package com.yjkj.chainup.freestaking.bean

data class CurrencyBean( var configTypes: String? = null,
                                     var name: String? = null,
                                     var logo: String? = null,
                                     var gainRate: Double = 0.0,
                                     var projectType: Int = 0,
                                     var labelType: Int = 0,
                                     var progress: String? = null,
                                     var id: Int = 0,
                                     var baseCoin: String? = null,
                                     var lockDay: Int = 0,
                                     var status: Int = 0,
                                     var shortName: String? = null)
