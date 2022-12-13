package com.yjkj.chainup.util

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.db.service.ColorDataService
import com.yjkj.chainup.db.service.PublicInfoDataService
import java.math.BigDecimal

/**
 * @Author: Bertking
 * @Date：2019/3/9-2:15 PM
 * @Description:
 */
object ColorUtil {

    val TAG = "ColorUtil"

    const val COLOR_SELECT = "color_selected"

    /**
     * 绿涨红跌
     */
    const val GREEN_RISE = 0

    /**
     * 红涨绿跌
     */
    const val RED_RISE = 1


    fun getColor(context: Context, colorId: Int) =
            ContextCompat.getColor(context, colorId)

    fun getColor(colorId: Int) = getColor(ChainUpApp.appContext, colorId)


    fun getMainGreen(): Int {
        return getColor(R.color.main_green)
    }

    fun getMainRed(): Int {
        return getColor(R.color.main_red)
    }

    /**
     * 红涨绿跌 OR 绿涨红跌
     * 0 ---- 绿涨红跌
     * 1 ---- 红涨绿跌
     *
     */
    fun getColorType(): Int {
        return ColorDataService.getInstance().colorType
    }

    /**
     *获取主要颜色(红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMainColorType(isRise: Boolean = true): Int {
        var colorSelect = getColorType()
//        LogUtil.d(TAG, "getMainColorType==isRise is $isRise,colorSelect is $colorSelect")
        val mainGreen = getColor(R.color.main_green)
        val mainRed = getColor(R.color.main_red)

        if (colorSelect == GREEN_RISE) {
            if (isRise) {
                return mainGreen
            }
            return mainRed
        } else {
            if (isRise) {
                return mainRed
            }
            return mainGreen
        }

    }

    /**
     *获取主要颜色(红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMainColorBgType(isRise: Boolean = true): Pair<Int, Int> {
        var colorSelect = getColorType()
//        LogUtil.d(TAG, "getMainColorType==isRise is $isRise,colorSelect is $colorSelect")
        val mainGreen = getColor(R.color.main_green)
        val mainRed = getColor(R.color.main_red)

        val drawableGreen = R.drawable.bg_trade_green_rose
        val drawableRed = R.drawable.bg_trade_red_rose

        if (colorSelect == GREEN_RISE) {
            if (isRise) {
                return Pair(mainGreen, drawableGreen)
            }
            return Pair(mainRed, drawableRed)
        } else {
            if (isRise) {
                return Pair(mainRed, drawableRed)
            }
            return Pair(mainGreen, drawableGreen)
        }

    }

    /**
     *获取主要颜色(红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMainColorType(colorSelect: Int, isRise: Boolean = true): Int {
        val mainGreen =
                getColor(R.color.main_green)
        val mainRed = getColor(R.color.main_red)
        return if (colorSelect == GREEN_RISE) {
            if (isRise) {
                mainGreen
            } else {
                mainRed
            }
        } else {
            if (isRise) {
                mainRed
            } else {
                mainGreen
            }
        }
    }


    /**
     * 获取次要颜色(带透明度的红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMinorColorType(isRise: Boolean = true): Int {
        var colorSelect = getColorType()

        val minorGreen = getColor(R.color.main_green_15)
        val minorRed = getColor(R.color.main_red_15)

        return if (colorSelect == GREEN_RISE) {
            if (isRise) {
                minorGreen
            } else {
                minorRed
            }
        } else {
            if (isRise) {
                minorRed
            } else {
                minorGreen
            }
        }

    }

    /**
     * 交易界面(买卖TAB drawable)，特殊处理
     */
    fun getOrientationTabDrawable(isBuy: Boolean = true): Int {
        var colorSelect = getColorType()
        /*PublicInfoManager.liveData4Color.observeForever {
            colorSelect = it!!
        }*/

        val drawableGreen = R.drawable.bg_buy_line
        val drawableRed = R.drawable.bg_sell_line
        return if (colorSelect == GREEN_RISE) {
            if (isBuy) {
                drawableGreen
            } else {
                drawableRed
            }
        } else {
            if (isBuy) {
                drawableRed
            } else {
                drawableGreen
            }
        }
    }

    /**
     * 合约交易界面(百分比背景 drawable)
     */
    fun getContractRateDrawable(isRise: Boolean = true): Int {
        var colorSelect = getColorType()
        /*PublicInfoManager.liveData4Color.observeForever {
            colorSelect = it!!
        }*/

        val drawableGreen = R.drawable.sl_border_green_fill
        val drawableRed = R.drawable.sl_border_red_fill
        return if (colorSelect == GREEN_RISE) {
            if (isRise) {
                drawableGreen
            } else {
                drawableRed
            }
        } else {
            if (isRise) {
                drawableRed
            } else {
                drawableGreen
            }
        }
    }


    /**
     * otc 交易页面买卖
     */
    fun getOTCBuyOrSellDrawable(): Int {
        val drawableBlue = R.drawable.bg_otc_buy_or_sell_line

        return drawableBlue
    }


    /**
     * 获取交易界面的交易量比例的ColorStateList
     * TODO 后期添加灵活配置
     */
    fun getCheck4ColorStateList(isRise: Boolean = true): ColorStateList {
        val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
        )

        val colorArray = intArrayOf(
                getMainColorType(isRise),
                getColor(R.color.trade_hint_color)
        )
        return ColorStateList(states, colorArray)
    }

    /**
     * 获取交易界面的交易量比例的StateListDrawable
     * TODO 后期添加灵活配置
     */
    fun getCheck4StateListDrawable(isRise: Boolean = true): StateListDrawable {
        val normalDrawable = GradientDrawable()
        normalDrawable.setColor(getColor(R.color.transparent))

        val checkedDrawable = GradientDrawable()
        checkedDrawable.setColor(getMinorColorType(isRise))

        val stateDrawable = StateListDrawable()
        stateDrawable.addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
        stateDrawable.addState(intArrayOf(), normalDrawable)
        return stateDrawable
    }


    /**
     * 交易界面(买卖TAB drawable)，特殊处理
     * @param flag 0 默认; 1 买盘 2 卖盘
     */
    fun setTapeIcon(imageView: ImageView, flag: Int = 0) {
        var colorSelect = getColorType()
        /*PublicInfoManager.liveData4Color.observeForever {
            colorSelect = it!!
        }*/
        return if (colorSelect == GREEN_RISE) {
            when (flag) {
                1 -> {
                    imageView.setImageResource(R.mipmap.coins_handicap_buy)
                }

                2 -> {
                    imageView.setImageResource(R.mipmap.coins_handicap_sell)
                }

                else -> {
                    imageView.setImageResource(R.mipmap.coins_handicap)
                }
            }

        } else {
            when (flag) {
                1 -> {
                    imageView.setImageResource(R.mipmap.coins_handicap_buy_red)
                }

                2 -> {
                    imageView.setImageResource(R.mipmap.coins_handicap_sell_green)
                }

                else -> {
                    imageView.setImageResource(R.mipmap.coins_handicap_greenred)
                }
            }

        }
    }

    fun getColorByMode(resId: Int ): Int {
        return getColorByMode(resId,false)
    }
    fun getColorByMode(resId: Int , iskline: Boolean = false): Int {
        val mResources = ChainUpApp.appContext.getResources()
        val originColor = ContextCompat.getColor(ChainUpApp.appContext, resId)
        var resName: String = ChainUpApp.appContext.getResources().getResourceEntryName(resId)
        //判断是日间模式还是夜间模式
        if (PublicInfoDataService.getInstance().getThemeModeNew().equals("day")) {
            resName = resName.replace("night", "day")
            if (iskline) {
                if (PublicInfoDataService.getInstance().klineThemeMode != 0) {
                    resName = resName.replace("day", "night")
                }
            }
        } else if (PublicInfoDataService.getInstance().getThemeModeNew().equals("night")) {
            resName = resName.replace("day", "night")
        }

        val trueResId: Int = mResources.getIdentifier(resName, "color", ChainUpApp.appContext.packageName)
        var trueColor = 0
        trueColor = try {
            ResourcesCompat.getColor(mResources, trueResId, null)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            originColor
        }
        return trueColor
    }


    fun getBuyOrSell(isBuy: Boolean = true): Int {
        val colorSelect = getColorType()
        if (colorSelect == GREEN_RISE) {
            if (isBuy) {
                return R.mipmap.coins_exchange_buy_green
            }
            return R.mipmap.coins_exchange_sell_red
        } else {
            if (isBuy) {
                return R.mipmap.coins_exchange_buy_red
            }
            return R.mipmap.coins_exchange_sell_green
        }
    }

    /**
     *获取主要颜色(红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMainFocusColorType(isBuy: Boolean = true): Int {
        var colorSelect = getColorType()
//        LogUtil.d(TAG, "getMainColorType==isRise is $isBuy,colorSelect is $colorSelect")
        val mainGreen = R.drawable.bg_trade_et_focused_green
        val mainRed = R.drawable.bg_trade_et_focused_red

        if (colorSelect == GREEN_RISE) {
            if (isBuy) {
                return mainGreen
            }
            return mainRed
        } else {
            if (isBuy) {
                return mainRed
            }
            return mainGreen
        }

    }

    /**
     *获取主要颜色(红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMainTickColorType(isBuy: Boolean = true): Int {
        var colorSelect = getColorType()
//        LogUtil.d(TAG, "getMainColorType==isRise is $isBuy,colorSelect is $colorSelect")
        val mainGreen = R.drawable.depth_sell_dot
        val mainRed = R.drawable.depth_buy_dot

        if (colorSelect == GREEN_RISE) {
            if (isBuy) {
                return mainGreen
            }
            return mainRed
        } else {
            if (isBuy) {
                return mainRed
            }
            return mainGreen
        }

    }

    /**
     *获取主要颜色(红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMainSelectColorType(isBuy: Boolean = true, position: Int = 0): Int {
        var colorSelect = getColorType()
//        LogUtil.d(TAG, "getMainColorType==isRise is $isBuy,colorSelect is $colorSelect")
        val mainGreen = R.drawable.bg_trade_et_focused_green
        val mainRed = R.drawable.bg_trade_et_focused_red

        if (colorSelect == GREEN_RISE) {
            if (isBuy) {
                return mainGreen
            }
            return mainRed
        } else {
            if (isBuy) {
                return mainRed
            }
            return mainGreen
        }

    }


    /**
     *获取主要颜色(红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMainColorV2Type(colorSelect: Int, isRise: Int = 0): Int {
        val mainGreen =
                getColor(R.color.main_green)
        val mainRed = getColor(R.color.main_red)
        val mainZero = getColorByMode(R.color.main_zero_day)
        return if (colorSelect == GREEN_RISE) {
            if (isRise > 0) {
                mainGreen
            } else if (isRise < 0) {
                mainRed
            } else {
                mainZero
            }
        } else {
            if (isRise > 0) {
                mainRed
            } else if (isRise < 0) {
                mainGreen
            } else {
                mainZero
            }
        }
    }

    fun getMainBgType(isRise: Boolean = true): Int {
        var colorSelect = getColorType()
//        LogUtil.d(TAG, "getMainColorType==isRise is $isRise,colorSelect is $colorSelect")
        val mainGreen = R.drawable.bg_buy_btn
        val mainRed = R.drawable.bg_sell_btn

        if (colorSelect == GREEN_RISE) {
            if (isRise) {
                return mainGreen
            }
            return mainRed
        } else {
            if (isRise) {
                return mainRed
            }
            return mainGreen
        }

    }

    fun getMainGridResType(isRise: String = "0"): Int {
        val colorSelect = getColorType()
        val mainGreen = getColor(R.color.main_green)
        val mainRed = getColor(R.color.main_red)
        val mainZero = getColor(R.color.main_zero)
        if (colorSelect == GREEN_RISE) {
            if (isRise.isNotEmpty()) {
                val number = BigDecimal(isRise)
                val diff = BigDecimalUtils.compareTo(number, BigDecimal.ZERO)
                if (diff == 0) {
                    return mainZero
                } else if (diff == 1) {
                    return mainGreen
                } else if (diff == -1) {
                    return mainRed
                }
            }
        } else {
            if (isRise.isNotEmpty()) {
                val number = BigDecimal(isRise)
                val diff = BigDecimalUtils.compareTo(number, BigDecimal.ZERO)
                if (diff == 0) {
                    return mainZero
                } else if (diff == 1) {
                    return mainRed
                } else if (diff == -1) {
                    return mainGreen
                }
            }
        }
        return mainZero
    }

    fun getGridColorType(): Int {
        var colorSelect = getColorType()

        val mainGreen = R.drawable.bg_progressbar_grid_green
        val mainRed = R.drawable.bg_progressbar_grid_red

        if (colorSelect == GREEN_RISE) {
            return mainGreen
        } else {
            return mainRed
        }

    }

    fun getMainGridResTypeCorner(isRise: String = "0"): String {
        val number = BigDecimal(isRise)
        if (number == BigDecimal.ZERO) {
            return ""
        } else if (number > BigDecimal.ZERO) {
            return "+"
        } else if (number < BigDecimal.ZERO) {
            return ""
        }
        return ""
    }

}