package com.yjkj.chainup.bean

import com.google.gson.annotations.SerializedName

data class IncrementConfigBean(@SerializedName("isNew")
                               var isNew: Int = 0,
                               @SerializedName("name")
                               var name: String? = null,
                               @SerializedName("status")
                               var status: Int = 0)
