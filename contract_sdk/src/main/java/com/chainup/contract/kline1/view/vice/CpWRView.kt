package com.chainup.contract.kline1.view.vice

import android.graphics.Canvas
import android.graphics.Paint
import com.chainup.contract.kline1.base.CpIChartViewDraw
import com.chainup.contract.kline1.view.CpBaseKLineChartView
import com.chainup.contract.kline1.bean.vice.CpIWR
import com.chainup.contract.kline1.formatter.CpValueFormatter
import com.chainup.contract.kline1.base.CpIValueFormatter
import com.chainup.contract.kline1.view.CpKLineChartView


/**
 * @Author: Bertking
 * @Date：2019/3/11-11:25 AM
 * @Description:
 */
class CpWRView(view: CpKLineChartView) : CpIChartViewDraw<CpIWR> {
    val TAG = CpWRView::class.java.simpleName

    private val paint4R = Paint(Paint.ANTI_ALIAS_FLAG)


    override fun drawTranslated(lastPoint: CpIWR?, curPoint: CpIWR, lastX: Float, curX: Float, canvas: Canvas, view: CpBaseKLineChartView, position: Int) {

        if (lastPoint!!.R != -10f) {
            view.drawChildLine(canvas, paint4R, lastX, lastPoint.R, curX, curPoint.R)
        }
    }

    override fun drawText(canvas: Canvas, view: CpBaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as CpIWR
        if (point.R != -10f) {
            var text = "WR(14):"
            canvas.drawText(text, x, y, view.textPaint)
            var textLen = x
            textLen += view.textPaint.measureText(text)
            text = view.formatValue(point.R) + " "
            canvas.drawText(text, textLen, y, paint4R)
        }
    }

    override fun getMaxValue(point: CpIWR): Float {
        return point.R
    }

    override fun getMinValue(point: CpIWR): Float {
        return point.R
    }

    override fun getValueFormatter(): CpIValueFormatter {
        return CpValueFormatter()
    }

    override fun setTextSize(textSize: Float) {
        paint4R.textSize = textSize
    }

    override fun setLineWidth(width: Float) {
        paint4R.strokeWidth = width
    }

    /**
     * 设置%R颜色
     */
    fun setRColor(color: Int) {
        paint4R.color = color
    }

}