package com.chainup.contract.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import androidx.core.content.ContextCompat
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.chainup.contract.R
import com.chainup.contract.app.CpMyApp

/**
 * @Author: Bertking
 * @Date：2019/3/9-2:15 PM
 * @Description:
 */
object CpColorUtil {

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

    fun getColor(colorId: Int) = getColor(CpMyApp.instance(), colorId)


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
        return 0
    }

    /**
     *获取主要颜色(红绿)
     * @param isRise 是否是上涨状态
     */
    fun getMainColorType(isRise: Boolean = true): Int {
        var colorSelect = getColorType()
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
     * 合约交易界面(百分比背景 drawable)
     */
    fun getContractRateDrawable(isRise: Boolean = true): Int {
        var colorSelect = getColorType()
        /*PublicInfoManager.liveData4Color.observeForever {
            colorSelect = it!!
        }*/

        val drawableGreen = R.drawable.cp_border_green_fill
        val drawableRed = R.drawable.cp_border_red_fill
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
        val drawableBlue = R.drawable.cp_bg_otc_buy_or_sell_line

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
                getColor(R.color.hint_color)
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
                    imageView.setImageResource(R.drawable.cp_buy_tape)
                }

                2 -> {
                    imageView.setImageResource(R.drawable.cp_sell_tape)
                }

                else -> {
                    imageView.setImageResource(R.drawable.cp_default_tape)
                }
            }

        } else {
            when (flag) {
                1 -> {
                    imageView.setImageResource(R.drawable.cp_sell_tape)
                }

                2 -> {
                    imageView.setImageResource(R.drawable.cp_buy_tape)
                }

                else -> {
                    imageView.setImageResource(R.drawable.cp_reverse_tape)
                }
            }

        }
    }


    fun getColorByMode(resId: Int): Int {
        val mResources = CpMyApp.instance().getResources()
        val originColor = ContextCompat.getColor(CpMyApp.instance(), resId)
        var resName: String =CpMyApp.instance().getResources().getResourceEntryName(resId)
        //判断是日间模式还是夜间模式
        if (CpClLogicContractSetting.getThemeMode(CpMyApp.instance())==0) {
            resName = resName.replace("night", "day")
        } else if (CpClLogicContractSetting.getThemeMode(CpMyApp.instance())==1) {
            resName = resName.replace("day", "night")
        }
        val trueResId: Int = mResources.getIdentifier(resName, "color", CpMyApp.instance().packageName)
        var trueColor = 0
        trueColor = try {
            ResourcesCompat.getColor(mResources, trueResId, null)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            originColor
        }
        return trueColor
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
            if (isRise> 0) {
                mainRed
            } else if (isRise < 0) {
                mainGreen
            }else{
                mainZero
            }
        }
    }
}