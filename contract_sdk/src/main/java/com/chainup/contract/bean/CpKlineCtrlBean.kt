package com.yjkj.chainup.new_contract.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

data class CpKlineCtrlBean(
        var time: String,
        var isSelect:Boolean,
        var layoutType: Int
) : Serializable, MultiItemEntity {
    override val itemType: Int
        get() = layoutType
}
