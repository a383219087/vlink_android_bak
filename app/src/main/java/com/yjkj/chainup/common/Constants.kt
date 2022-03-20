package com.yjkj.chainup.common

import com.yjkj.chainup.R
import com.yjkj.chainup.util.SystemUtils

object Constants {

    // 图一
    fun getGuideOne(): Int {
        return when (SystemUtils.isZh()) {
            true -> R.drawable.setupabot_cn
            else -> R.drawable.setupabot_en
        }
    }

    // 图二
    fun getGuideTwo(): Int {
        return when (SystemUtils.isZh()) {
            true -> R.drawable.runabot_cn
            else -> R.drawable.runabot_en
        }
    }

    // 图三
    fun getGuideThree(): Int {
        return when (SystemUtils.isZh()) {
            true -> R.drawable.closeabot_cn
            else -> R.drawable.closeabot_en
        }
    }
}


