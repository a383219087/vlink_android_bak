package com.yjkj.chainup.new_version.kline.view

/**
 * @Author: Bertking
 * @Date：2019/3/11-11:46 AM
 * @Description:
 */
enum class MainKlineViewStatus(val status: Int) {
    MA(0), BOLL(1), NONE(2)
}


enum class YLabelModel {
    LABEL_WITH_GRID, LABEL_NONE_GRID
}

enum class YLabelAlign {
    LEFT, RIGHT
}