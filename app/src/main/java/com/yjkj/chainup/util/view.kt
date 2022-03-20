package com.yjkj.chainup.util

import android.view.View


fun Boolean.getVisible(): Int {
    return when (this) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}
fun Boolean.getVisibleIN(): Int {
    return when (this) {
        true -> View.VISIBLE
        else -> View.INVISIBLE
    }
}