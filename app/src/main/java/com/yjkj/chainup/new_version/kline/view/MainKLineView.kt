package com.yjkj.chainup.kline.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.core.content.ContextCompat
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.RateManager
import com.yjkj.chainup.new_version.kline.base.IChartViewDraw
import com.yjkj.chainup.new_version.kline.base.IValueFormatter
import com.yjkj.chainup.new_version.kline.bean.CandleBean
import com.yjkj.chainup.new_version.kline.bean.IKLine
import com.yjkj.chainup.new_version.kline.formatter.ValueFormatter
import com.yjkj.chainup.new_version.kline.view.BaseKLineChartView
import com.yjkj.chainup.new_version.kline.view.IFallRiseColor
import com.yjkj.chainup.new_version.kline.view.MainKlineViewStatus
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.DisplayUtil
import org.jetbrains.anko.dip

/**
 * @Author: Bertking
 * @Date：2019/3/11-11:28 AM
 * @Description: K线主图
 */
class MainKLineView(kLineChartView: KLineChartView) : IChartViewDraw<CandleBean>, IFallRiseColor {

    val TAG = MainKLineView::class.java.simpleName

    private var candleWidth = 0f

    private var candleLineWidth = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val fallPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val risePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paint4MA5 = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paint4MA10 = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paint4MA30 = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mPricePrecision = -1

    /**
     * marker
     */
    private val markerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 蜡烛线是否是实线
    var isCandleSolid = true
    // 是否分时
    var isLine = false
        set(value) {
            field = value
            if (isLine != value) {
                isLine = value
                if (isLine) {
                    kChartView.setCandleWidth(
                            context.dip(7f).toFloat())
                } else {
                    kChartView.setCandleWidth(
                            context.dip(6).toFloat())
                }
            }

        }

    lateinit var context: Context

    var status = MainKlineViewStatus.MA


    lateinit var kChartView: KLineChartView

    init {
        context = kLineChartView.context
        kChartView = kLineChartView
        mLinePaint.color = ContextCompat.getColor(context, R.color.main_color)
        paint.color = ContextCompat.getColor(context, R.color.chart_line_background)
        markerBorderPaint.color = ContextCompat.getColor(context, R.color.marker_border)
        markerBorderPaint.style = Paint.Style.STROKE
    }


    override fun drawTranslated(lastPoint: CandleBean?, curPoint: CandleBean, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (isLine) {

            view.drawMainLine(canvas, mLinePaint, lastX, lastPoint?.closePrice
                    ?: 0f, curX, curPoint.closePrice)

            /**
             * 画分时线
             */
            view.drawMainMinuteLine(canvas, paint, lastX, lastPoint?.closePrice
                    ?: 0f, curX, curPoint.closePrice)
        } else {
            drawCandle(view, canvas, curX, curPoint.highPrice, curPoint.lowPrice, curPoint.openPrice, curPoint.closePrice)

            if (status == MainKlineViewStatus.MA) {
                //画ma5
                if (lastPoint!!.price4MA5 != 0f) {
                    view.drawMainLine(canvas, paint4MA5, lastX, lastPoint.price4MA5, curX, curPoint.price4MA5)
                }
                //画ma10
                if (lastPoint.price4MA10 != 0f) {
                    view.drawMainLine(canvas, paint4MA10, lastX, lastPoint.price4MA10,
                            curX, curPoint.price4MA10)
                }
                //画ma30
                if (lastPoint.price4MA30 != 0f) {
                    view.drawMainLine(canvas, paint4MA30, lastX, lastPoint.price4MA30,
                            curX, curPoint.price4MA30)
                }
            } else if (status == MainKlineViewStatus.BOLL) {
                //画boll
                if (lastPoint!!.up != 0f) {
                    view.drawMainLine(canvas, paint4MA5, lastX, lastPoint.up, curX, curPoint.up)
                }
                if (lastPoint.mb != 0f) {
                    view.drawMainLine(canvas, paint4MA10, lastX, lastPoint.mb, curX, curPoint.mb)
                }
                if (lastPoint.dn != 0f) {
                    view.drawMainLine(canvas, paint4MA30, lastX, lastPoint.dn, curX, curPoint.dn)
                }
            }
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as IKLine?
        var textHeight = y
        textHeight -= 5
        if (isLine) {
            if (status == MainKlineViewStatus.MA) {
                if (point!!.price4MA60 != 0f) {
                    val text = "MA60:" + view.formatValue(if (mPricePrecision==-1) point.price4MA60 else BigDecimalUtils.showSNormal(point.price4MA60.toString(),mPricePrecision).toFloat()) + "     "
                    canvas.drawText(text, x + DisplayUtil.dip2px(5f), textHeight, paint4MA10)
                }
            } else if (status == MainKlineViewStatus.BOLL) {
                if (point!!.mb != 0f) {
                    val text = "BOLL:" + view.formatValue(if (mPricePrecision==-1) point.mb else BigDecimalUtils.showSNormal(point.mb.toString(),mPricePrecision).toFloat()) + "     "
                    canvas.drawText(text, +DisplayUtil.dip2px(5f), textHeight, paint4MA10)
                }
            }
        } else {
            if (status == MainKlineViewStatus.MA) {
                var text: String
                var textLen = x
                if (point!!.price4MA5 != 0f) {
                    text = "MA5:" + view.formatValue(if (mPricePrecision==-1) point.price4MA5 else BigDecimalUtils.showSNormal(point.price4MA5.toString(),mPricePrecision).toFloat())
                    canvas.drawText(text, textLen + DisplayUtil.dip2px(15f), textHeight, paint4MA5)
                    textLen += paint4MA5.measureText(text)
                }
                if (point.price4MA10 != 0f) {
                    text = "MA10:" + view.formatValue(if (mPricePrecision==-1) point.price4MA10 else BigDecimalUtils.showSNormal(point.price4MA10.toString(),mPricePrecision).toFloat())
                    canvas.drawText(text, textLen + DisplayUtil.dip2px(25f), textHeight, paint4MA10)
                    textLen += paint4MA10.measureText(text)
                }
                if (point.price4MA30 != 0f) {
                    text = "MA30:" + view.formatValue(if (mPricePrecision==-1) point.price4MA30 else BigDecimalUtils.showSNormal(point.price4MA30.toString(),mPricePrecision).toFloat())
                    canvas.drawText(text, textLen + DisplayUtil.dip2px(35f), textHeight, paint4MA30)
                }
            } else if (status == MainKlineViewStatus.BOLL) {
                if (point!!.mb != 0f) {
                    var textLen = x
                    var text = "BOLL:" + view.formatValue(if (mPricePrecision==-1) point.mb else BigDecimalUtils.showSNormal(point.mb.toString(),mPricePrecision).toFloat()) + "     "
                    canvas.drawText(text, textLen + DisplayUtil.dip2px(5f), textHeight, paint4MA10)
                    textLen += paint4MA5.measureText(text)
                    text = "UB:" + view.formatValue(if (mPricePrecision==-1) point.up else BigDecimalUtils.showSNormal(point.up.toString(),mPricePrecision).toFloat()) + "     "
                    canvas.drawText(text, textLen, textHeight, paint4MA5)
                    textLen += paint4MA10.measureText(text)
                    text = "LB:" + view.formatValue(if (mPricePrecision==-1) point.dn else BigDecimalUtils.showSNormal(point.dn.toString(),mPricePrecision).toFloat())
                    canvas.drawText(text, textLen, textHeight, paint4MA30)
                }
            }
        }
        if (view.isLongPress) {
            drawMarker(view, canvas)
        }

    }

    override fun getMaxValue(point: CandleBean): Float {
        return if (status == MainKlineViewStatus.BOLL) {
            Log.d(TAG, "getMaxValue ${point.up} ${point.up.isNaN()} ${point.mb}")
            Math.max(point.up, point.highPrice)
        } else {
            maxOf(point.highPrice, point.price4MA30)
        }
    }

    override fun getMinValue(point: CandleBean): Float {
        return if (status == MainKlineViewStatus.BOLL) {
            Math.min(point.dn,point.lowPrice)
        } else {
            if (point.price4MA30 == 0f) point.lowPrice else Math.min(point.price4MA30, point.lowPrice)
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun setTextSize(textSize: Float) {
        paint4MA30.textSize = textSize
        paint4MA10.textSize = textSize
        paint4MA5.textSize = textSize
    }

    override fun setLineWidth(width: Float) {
        paint4MA30.strokeWidth = width
        paint4MA10.strokeWidth = width
        paint4MA5.strokeWidth = width
        mLinePaint.strokeWidth = width
        markerBorderPaint.strokeWidth = width
    }

    override fun setFallRiseColor(riseColor: Int, fallColor: Int) {
        fallPaint.color = fallColor
        risePaint.color = riseColor
    }


    /**
     * 画Candle
     *
     * @param canvas
     * @param x      x轴坐标
     * @param high   最高价
     * @param low    最低价
     * @param open   开盘价
     * @param close  收盘价
     */
    private fun drawCandle(view: BaseKLineChartView, canvas: Canvas, x: Float, high: Float, low: Float, open: Float, close: Float) {

        Log.d(TAG, "============画蜡烛线啦...======")

        var high = high
        var low = low
        var open = open
        var close = close
        high = view.getMainY(high)
        low = view.getMainY(low)
        open = view.getMainY(open)
        close = view.getMainY(close)
        val r = candleWidth / 2
        val lineR = candleLineWidth / 2
        if (open > close) {

            Log.d(TAG, "2222222222${fallPaint.color == ColorUtil.getMainColorType(false)},open:$open,close:$close")

            //实心
            if (isCandleSolid) {
                canvas.drawRect(x - r, close, x + r, open, fallPaint)
                canvas.drawRect(x - lineR, high, x + lineR, low, fallPaint)
            } else {
                fallPaint.strokeWidth = candleLineWidth
                canvas.drawLine(x, high, x, close, fallPaint)
                canvas.drawLine(x, open, x, low, fallPaint)
                canvas.drawLine(x - r + lineR, open, x - r + lineR, close, fallPaint)
                canvas.drawLine(x + r - lineR, open, x + r - lineR, close, fallPaint)
                fallPaint.strokeWidth = candleLineWidth * view.scaleX
                canvas.drawLine(x - r, open, x + r, open, fallPaint)
                canvas.drawLine(x - r, close, x + r, close, fallPaint)
            }

        } else if (open < close) {
            Log.d(TAG, "444444444${risePaint.color == ColorUtil.getMainColorType()},open:$open,close:$close")
            canvas.drawRect(x - r, open, x + r, close, risePaint)
            canvas.drawRect(x - lineR, high, x + lineR, low, risePaint)
        } else {
            canvas.drawRect(x - r, open, x + r, close + 1, fallPaint)
            canvas.drawRect(x - lineR, high, x + lineR, low, fallPaint)
        }
    }

    /**
     *
     * MarkerView
     * @param view
     * @param canvas
     */
    private fun drawMarker(view: BaseKLineChartView, canvas: Canvas) {
        val metrics = markerTitlePaint.fontMetrics
        val textHeight = metrics.descent - metrics.ascent

        val index = view.selectedIndex
        val padding = context.dip(5f).toFloat()
        val margin = context.dip(5f).toFloat()
        /**
         * 设置MarkerView的宽度
         */
        var width = context.dip(108).toFloat()
        width += padding * 2


        val left: Float
        val top = margin + view.topPadding
        val height = padding * 8 + textHeight * 9

        val point = view.getItem(index) as IKLine

        var map = linkedMapOf<String, String>()
        map[LanguageUtil.getString(context, "kline_text_dealTime")] = view.adapter?.getDate(index).toString()
        // 开
        map[LanguageUtil.getString(context, "kline_text_open")] = BigDecimalUtils.showSNormal(point.openPrice.toString())
        // 高
        map[LanguageUtil.getString(context, "kline_text_high")] = BigDecimalUtils.showSNormal(point.highPrice.toString())
        // 低
        map[LanguageUtil.getString(context, "kline_text_low")] = BigDecimalUtils.showSNormal(point.lowPrice.toString())
        // 收
        map[LanguageUtil.getString(context, "kline_text_close")] = BigDecimalUtils.showSNormal(point.closePrice.toString())
        //涨幅额
        var lines = BigDecimalUtils.sub(point.closePrice.toString(), point.openPrice.toString()).toPlainString()
        map[LanguageUtil.getString(context, "kline_text_changeValue")] = RateManager.getAbsoluteText4Kline(lines)

        //涨幅度
        var fist = BigDecimalUtils.div(lines, point.openPrice.toString()).toPlainString()

        map[LanguageUtil.getString(context, "kline_text_changeRate")] = RateManager.getRoseText4Kline(fist)
        //成交量
        map[LanguageUtil.getString(context, "volume")] = BigDecimalUtils.showSNormal(point.volume.toString())


        val x = view.translateXtoX(view.getX(index))
        left = if (x > view.chartWidth / 2) {
            margin
        } else {
            view.chartWidth - width - margin
        }
        val r = RectF(left, top, left + width, top + height + padding)
        canvas.drawRoundRect(r, padding, padding, markerBgPaint)
        Log.d(TAG, "=========padding:$padding=====")
        canvas.drawRoundRect(r, DisplayUtil.dip2px(1.5f), DisplayUtil.dip2px(1.5f), markerBorderPaint)

        var y = top + padding * 2 + (textHeight - metrics.bottom - metrics.top) / 2
        /**
         * 设置文字R->L
         */
        markerValuePaint.textAlign = Paint.Align.RIGHT
        for ((k, v) in map) {
            if (LanguageUtil.getString(context,"kline_text_changeRate") == k || LanguageUtil.getString(context, "kline_text_changeValue") == k) {
                markerValuePaint.color = ColorUtil.getMainColorType(!v.contains("-"))
            } else {
                if (PublicInfoDataService.getInstance().klineThemeMode == 1){
                    markerValuePaint.color = ColorUtil.getColor(context,R.color.chart_max_min_kline_night)
                }else{
                    markerValuePaint.color = ColorUtil.getColor(context,R.color.chart_max_min)
                }
            }
            canvas.drawText(k, left + padding, y, markerTitlePaint)
            canvas.drawText(v, width - padding + left, y, markerValuePaint)
            y += textHeight + padding
        }

    }


    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    fun setCandleWidth(candleWidth: Float) {
        this.candleWidth = candleWidth
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    fun setCandleLineWidth(candleLineWidth: Float) {
        this.candleLineWidth = candleLineWidth
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    fun setMa5Color(color: Int) {
        this.paint4MA5.color = color
    }

    /**
     * 设置ma10颜色
     *
     * @param color
     */
    fun setMa10Color(color: Int) {
        this.paint4MA10.color = color
    }

    fun setMaPricePrecision(pricePrecision: Int) {
        this.mPricePrecision = pricePrecision
    }

    /**
     * 设置ma30颜色
     *
     * @param color
     */
    fun setMa30Color(color: Int) {
        this.paint4MA30.color = color
    }

    /**
     * 设置选择器标题文字颜色(时间，开，高，低，收)
     * @param color
     */
    fun setMarkerTitleColor(color: Int) {
        markerTitlePaint.color = color
    }

    /**
     * 设置选择器数值文字颜色
     * @param color
     */
    fun setMarkerValueColor(color: Int) {
        markerValuePaint.color = color
    }

    /**
     * 设置选择器文字大小
     * @param textSize
     */
    fun setMarkerTextSize(textSize: Float) {
        markerTitlePaint.textSize = textSize
        markerValuePaint.textSize = textSize
    }

    /**
     * 设置选择器背景
     *
     * @param color
     */
    fun setMarkerBackgroundColor(color: Int) {
        markerBgPaint.color = color
    }


}