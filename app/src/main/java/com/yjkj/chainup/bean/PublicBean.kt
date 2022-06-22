package com.yjkj.chainup.bean

import java.io.Serializable


data class PubilcBean(

    val enable_module_info: EnableModuleInfo?


) : Serializable


data class EnableModuleInfo(

    val trader :Int?,
    val increment :Int?,
    val game :Int?,
    val futures :Int?,
    val share :Int?,
    val withdraw :Int?,
    val recharge :Int?,
    val options :Int?,
    val chat :Int?,
    val blocks :Int?,
    val crazy :Int?,
    val blocks_version :String?,
    val crazy_version :String?,
    val options_version :String?



    ) : Serializable








