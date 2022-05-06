package com.yjkj.chainup.bean

import java.io.Serializable


data class PubilcBean(

    val enable_module_info: EnableModuleInfo


) : Serializable


data class EnableModuleInfo(

    val trader :Int?,
    val increment :Int?,
    val game :Int?,
    val futures :Int?,
    val share :Int?


    ) : Serializable








