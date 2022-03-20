package com.chainup.contract.view

import android.content.Context
import android.graphics.*
import android.util.Log
import com.chainup.contract.R
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpColorUtil
import com.chainup.contract.utils.CpDisplayUtil
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp

/**
 * @Author: Bertking
 * @Date：2019-07-25-10:27
 * @Description:
 */
class CpDepthMarkView(context: Context, layoutRes: Int) : MarkerView(context, layoutRes) {

    val TAG = CpDepthMarkView::class.java.simpleName

    var screenWidth = 0f
    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val markerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val markerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var volume = ""
    var price = ""
    var myCanvas: Canvas? = null


    init {
        circlePaint.style = Paint.Style.FILL
        circlePaint.strokeWidth = 4f

        markerBgPaint.style = Paint.Style.FILL
        markerBgPaint.color = CpColorUtil.getColor(R.color.marker_bg)


        markerBorderPaint.style = Paint.Style.STROKE
        markerBorderPaint.strokeWidth = dip(0.5f).toFloat()
        markerBorderPaint.color = CpColorUtil.getColor(R.color.marker_border)


        screenWidth = CpDisplayUtil.getScreenWidth().toFloat()

        textPaint.color = CpColorUtil.getColor(R.color.text_color)
        textPaint.textSize = sp(10f).toFloat()
        textPaint.textAlign = Paint.Align.LEFT
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        volume = CpBigDecimalUtils.showDepthVolume(e?.y.toString())
        price = e?.data.toString()

        Log.d(TAG, "==volume:${e?.y},price:${e?.data}==")

        super.refreshContent(e, highlight)
    }


    override fun getOffset(): MPPointF {
        Log.d(TAG, "width:$width,height:$height")
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }

    override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
        Log.d(TAG, "=======x:$posX,y:$posY======")
        super.draw(canvas, posX, posY)

        drawCircle(posX, canvas, posY)
        /**
         * 文字高度
         */
        val metrics = textPaint.fontMetrics
        val textHeight = metrics.descent - metrics.ascent
        val textWidth4Price = textPaint.measureText(price)

        val textPadding = dip(12f).toFloat()
        val height4Chart = dip(198).toFloat()

        val xRectF = RectF(posX - textWidth4Price / 2.0f - textPadding, height4Chart, posX + textWidth4Price / 2.0f + textPadding, height4Chart - dip(20).toFloat())
        canvas?.drawRect(xRectF, markerBgPaint)
        canvas?.drawText(price, posX - textWidth4Price / 2.0f, height4Chart - textHeight / 2.0f, textPaint)
        canvas?.drawRect(xRectF, markerBorderPaint)

        val textWidth4Volume = textPaint.measureText(volume)
        val yRectF = RectF(screenWidth, posY, screenWidth - textWidth4Volume - 2 * textPadding, posY - dip(20).toFloat())
        canvas?.drawRect(yRectF, markerBgPaint)
        canvas?.drawText(volume, screenWidth - textWidth4Volume - textPadding, posY - textHeight / 2.0f, textPaint)
        canvas?.drawRect(yRectF, markerBorderPaint)

        myCanvas = canvas

    }

    /**
     * 画圆圈
     */
    private fun drawCircle(posX: Float, canvas: Canvas?, posY: Float) {
        val center = CpDisplayUtil.getScreenWidth() / 2.0
        val pair = if (posX <= center) {
            Pair<Int, Int>(CpColorUtil.getMinorColorType(), CpColorUtil.getMainColorType())
        } else {
            Pair<Int, Int>(CpColorUtil.getMinorColorType(false), CpColorUtil.getMainColorType(false))
        }
        circlePaint.color = pair.first
        canvas?.drawCircle(posX, posY, dip(8f).toFloat(), circlePaint)
        circlePaint.color = pair.second
        canvas?.drawCircle(posX, posY, dip(4f).toFloat(), circlePaint)
    }

}