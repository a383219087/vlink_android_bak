package com.chainup.contract.kline.view.vice

import android.graphics.Canvas
import android.graphics.Paint
import com.chainup.contract.kline.base.CpIChartViewDraw
import com.chainup.contract.kline.view.CpBaseKLineChartView
import com.chainup.contract.kline.view.CpKLineChartView

import com.chainup.contract.kline.formatter.CpValueFormatter
import com.chainup.contract.kline.base.CpIValueFormatter
import com.yjkj.chainup.new_version.kline.bean.vice.CpIRSI


/**
 * @Author: Bertking
 * @Date：2019/3/11-11:23 AM
 * @Description:
 */

class CpRSIView(view: CpKLineChartView) : CpIChartViewDraw<CpIRSI> {
    val TAG = CpRSIView::class.java.simpleName

    private val paint4RSI1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint4RSI2 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint4RSI3 = Paint(Paint.ANTI_ALIAS_FLAG)


    override fun drawTranslated(lastPoint: CpIRSI?, curPoint: CpIRSI, lastX: Float, curX: Float, canvas: Canvas, view: CpBaseKLineChartView, position: Int) {

        if (lastPoint?.RSI != 0f) {
            view.drawChildLine(canvas, paint4RSI1, lastX, lastPoint?.RSI ?: 0f, curX, curPoint.RSI)
        }

    }

    override fun drawText(canvas: Canvas, view: CpBaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as CpIRSI?
        if (point!!.RSI != 0f) {
            var text = "RSI(14)  "
            canvas.drawText(text, x, y, view.textPaint)
            var textLen = x
            textLen += view.textPaint.measureText(text)
            text = view.formatValue(point.RSI)
            canvas.drawText(text, textLen, y, paint4RSI1)
        }

    }

    override fun getMaxValue(point: CpIRSI): Float {
        return point.RSI
    }

    override fun getMinValue(point: CpIRSI): Float {
        return point.RSI
    }

    override fun getValueFormatter(): CpIValueFormatter {
        return CpValueFormatter()
    }

    override fun setTextSize(textSize: Float) {
        paint4RSI2.textSize = textSize
        paint4RSI3.textSize = textSize
        paint4RSI1.textSize = textSize
    }

    override fun setLineWidth(width: Float) {
        paint4RSI1.strokeWidth = width
        paint4RSI2.strokeWidth = width
        paint4RSI3.strokeWidth = width
    }

    fun setRSI1Color(color: Int) {
        paint4RSI1.color = color
    }

    fun setRSI2Color(color: Int) {
        paint4RSI2.color = color
    }

    fun setRSI3Color(color: Int) {
        paint4RSI3.color = color
    }

}