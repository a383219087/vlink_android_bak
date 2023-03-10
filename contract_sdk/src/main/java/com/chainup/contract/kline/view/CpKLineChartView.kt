package com.chainup.contract.kline.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ProgressBar
import androidx.annotation.DimenRes
import com.chainup.contract.R
import com.chainup.contract.kline.view.vice.CpKDJView
import com.chainup.contract.kline.view.vice.CpMACDView
import com.chainup.contract.kline.view.vice.CpRSIView
import com.chainup.contract.kline.view.vice.CpWRView

import com.chainup.contract.utils.CpColorUtil


import org.jetbrains.anko.layoutInflater
import kotlin.math.abs

/**
 * @Author: Bertking
 * @Date：2019/3/13-10:31 AM
 * @Description:
 */
class CpKLineChartView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CpBaseKLineChartView(context, attrs, defStyleAttr), CpIFallRiseColor {

    val TAG = CpKLineChartView::class.java.simpleName


    var mProgressBar: ProgressBar? = null
    private var isRefreshing = false
    private var isLoadMoreEnd = false
    private var mLastScrollEnable: Boolean = false
    private var mLastScaleEnable: Boolean = false

    private var mRefreshListener: KChartRefreshListener? = null

    private var mMACDDraw: CpMACDView? = null
    private var mRSIDraw: CpRSIView? = null
    private var mMainDraw: CpMainKLineView? = null
    private var mKDJDraw: CpKDJView? = null
    private var mWRDraw: CpWRView? = null
    private var mVolumeDraw: CpVolumeView? = null
    private var startXX = 0
    private var startYY = 0
    private var pricePricision = 2
    private var mIsShowChild:Boolean = true


    init {
        initView()
        initAttrs(attrs)
    }

    private fun initView() {
        /**
         * 进度条
         */
        val view = context.layoutInflater.inflate(R.layout.cp_layout_kline_loading, null)
        mProgressBar = view.findViewById(R.id.pb_kline_load)
        mProgressBar!!.visibility = GONE
        mMACDDraw = CpMACDView(this)
        mWRDraw = CpWRView(this)
        mKDJDraw = CpKDJView(this)
        mRSIDraw = CpRSIView(this)

        /**
         * 交易量图
         */
        mVolumeDraw = CpVolumeView(this)

        /**
         * 主K图
         */
        mMainDraw = CpMainKLineView(this)

        addChildDraw(mMACDDraw)
        addChildDraw(mKDJDraw)
        addChildDraw(mRSIDraw)
        addChildDraw(mWRDraw)

        volDraw = mVolumeDraw
        mainDraw = mMainDraw
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CpKLineChartView)
        if (array != null) {
            try {
                /**
                 * 设置蜡烛线和交易量图柱的颜色
                 */
                setFallRiseColor(CpColorUtil.getMainColorType(), CpColorUtil.getMainColorType(false))

                /**
                 * 蜡烛线相关设置
                 */
                setCandleWidth(array.getDimension(R.styleable.CpKLineChartView_kc_candle_width, getDimension(R.dimen.chart_candle_width)))
                setCandleLineWidth(array.getDimension(R.styleable.CpKLineChartView_kc_candle_line_width, getDimension(R.dimen.chart_candle_line_width)))
                setCandleSolid(array.getBoolean(R.styleable.CpKLineChartView_kc_candle_solid, true))


                /**
                 * MA & 蜡烛线
                 */
                setMa5Color(array.getColor(R.styleable.CpKLineChartView_kc_dif_color, CpColorUtil.getColor(context,R.color.chart_ma5)))
                setMa10Color(array.getColor(R.styleable.CpKLineChartView_kc_dea_color, CpColorUtil.getColor(context,R.color.chart_ma10)))
                setMa30Color(array.getColor(R.styleable.CpKLineChartView_kc_macd_color, CpColorUtil.getColor(context,R.color.chart_ma30)))

                /**
                 * KLine图上的最大&最小值
                 */
                setMTextSize(array.getDimension(R.styleable.CpKLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)))
                val textMixColor = array.getColor(R.styleable.CpKLineChartView_kc_text_color, CpColorUtil.getColor(context,R.color.text_color))
                setMTextColor(textMixColor)


                /**
                 * KLineView，VolumeView右边的边界值
                 */
                setBoundaryValueColor(array.getColor(R.styleable.CpKLineChartView_kc_boundary_value, CpColorUtil.getColor(context,R.color.normal_text_color)))


                /**
                 * Point决定了蜡烛线之间的间隙
                 */
                setPointWidth(array.getDimension(R.styleable.CpKLineChartView_kc_point_width, getDimension(R.dimen.chart_point_width)))

                setSelectPointColor(array.getColor(R.styleable.CpKLineChartView_kc_background_color, CpColorUtil.getColor(context,R.color.bg_card_color)))

                /**
                 * KLine，Volume的边界上的值
                 */
                textSize = array.getDimension(R.styleable.CpKLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size))

                setTextColor(array.getColor(R.styleable.CpKLineChartView_kc_text_color, CpColorUtil.getColor(context,R.color.chart_max_min)))

                /**
                 * 主图(KLine,volume,副图)的背景色
                 */
                setBackgroundColor(array.getColor(R.styleable.CpKLineChartView_kc_background_color, CpColorUtil.getColor(context,R.color.bg_card_color)))
                /**
                 * 主图指标线的宽度
                 */
                lineWidth = array.getDimension(R.styleable.CpKLineChartView_kc_line_width, getDimension(R.dimen.chart_line_width))

                /**
                 * 选中X轴的颜色
                 */
                setSelectedXLineColor(CpColorUtil.getColor(context,R.color.chart_selected_x))
                setSelectedXLineWidth(getDimension(R.dimen.chart_line_width))
                /**
                 * 选中Y轴的颜色
                 */
                setSelectedYLineColor(CpColorUtil.getColor(context,R.color.chart_selected_y))
                setSelectedYLineWidth(getDimension(R.dimen.chart_selected_y_width))

                /**
                 * 网格线参数
                 */
                setGridLineWidth(array.getDimension(R.styleable.CpKLineChartView_kc_grid_line_width, getDimension(R.dimen.chart_grid_line_width)))
                setGridLineColor(array.getColor(R.styleable.CpKLineChartView_kc_grid_line_color, CpColorUtil.getColor(context,R.color.line_color)))


                /**
                 * MACD
                 */
                setMACDWidth(array.getDimension(R.styleable.CpKLineChartView_kc_macd_width, getDimension(R.dimen.chart_candle_width)))
                setDIFColor(array.getColor(R.styleable.CpKLineChartView_kc_dif_color, CpColorUtil.getColor(context,R.color.chart_ma5)))
                setDEAColor(array.getColor(R.styleable.CpKLineChartView_kc_dea_color, CpColorUtil.getColor(context,R.color.chart_ma10)))
                setMACDColor(array.getColor(R.styleable.CpKLineChartView_kc_macd_color, CpColorUtil.getColor(context,R.color.chart_ma30)))

                /**
                 * KDJ
                 */
                setKColor(array.getColor(R.styleable.CpKLineChartView_kc_dif_color, CpColorUtil.getColor(context,R.color.chart_ma5)))
                setDColor(array.getColor(R.styleable.CpKLineChartView_kc_dea_color, CpColorUtil.getColor(context,R.color.chart_ma10)))
                setJColor(array.getColor(R.styleable.CpKLineChartView_kc_macd_color, CpColorUtil.getColor(context,R.color.chart_ma30)))
                /**
                 * WR
                 */
                setRColor(array.getColor(R.styleable.CpKLineChartView_kc_dif_color, CpColorUtil.getColor(context,R.color.chart_ma5)))
                /**
                 * RSI
                 */
                setRSI1Color(array.getColor(R.styleable.CpKLineChartView_kc_dif_color, CpColorUtil.getColor(context,R.color.chart_ma5)))
                setRSI2Color(array.getColor(R.styleable.CpKLineChartView_kc_dea_color, CpColorUtil.getColor(context,R.color.chart_ma10)))
                setRSI3Color(array.getColor(R.styleable.CpKLineChartView_kc_macd_color, CpColorUtil.getColor(context,R.color.chart_ma30)))


                /**
                 * MarkView的背景色
                 */
                setSelectorBackgroundColor(array.getColor(R.styleable.CpKLineChartView_kc_selector_background_color, CpColorUtil.getColor(context,R.color.marker_bg)))
                setSelectorTextSize(array.getDimension(R.styleable.CpKLineChartView_kc_selector_text_size, getDimension(R.dimen.chart_selector_text_size)))
//                setMarkerTitleColor(array.getColor(R.styleable.CpKLineChartView_kc_text_color, CpColorUtil.getColor(context,R.color.normal_text_color)))
                setMarkerTitleColor( CpColorUtil.getColor(context,R.color.normal_text_color))
                setMarkerValueColor(array.getColor(R.styleable.CpKLineChartView_kc_marker_value_color, CpColorUtil.getColor(context,R.color.chart_max_min)))

                setSelectedTextColor(array.getColor(R.styleable.CpKLineChartView_kc_text_color, CpColorUtil.getColor(context,R.color.chart_max_min)))

                mIsShowChild = array.getBoolean(R.styleable.CpKLineChartView_kc_show_child, true)
                setIsShowChild(mIsShowChild)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                array.recycle()
            }
        }
    }

    private fun getDimension(@DimenRes resId: Int): Float {
        return resources.getDimension(resId)
    }


    override fun onLeftSide() {
        showLoading()
    }

    override fun onRightSide() {}

    fun showLoading() {
        if (!isLoadMoreEnd && !isRefreshing) {
            isRefreshing = true
            if (mProgressBar != null) {
                mProgressBar!!.visibility = VISIBLE
            }
            if (mRefreshListener != null) {
                mRefreshListener!!.onLoadMoreBegin(this)
            }
            mLastScaleEnable = isScaleEnable
            mLastScrollEnable = isScrollEnable
            super.setScrollEnable(mLastScaleEnable)
            super.setScaleEnable(mLastScaleEnable)
        }
    }

    fun justShowLoading() {
        if (!isRefreshing) {
            isLongPress = false
            isRefreshing = true
            if (mProgressBar != null) {
                mProgressBar!!.visibility = VISIBLE
            }
            if (mRefreshListener != null) {
                mRefreshListener!!.onLoadMoreBegin(this)
            }
            mLastScaleEnable = isScaleEnable
            mLastScrollEnable = isScrollEnable
            super.setScrollEnable(false)
            super.setScaleEnable(false)
        }
    }

    private fun hideLoading() {
        if (mProgressBar != null) {
            mProgressBar!!.visibility = GONE
        }
        super.setScrollEnable(mLastScrollEnable)
        super.setScaleEnable(mLastScaleEnable)
    }

    /**
     * 隐藏选择器内容
     */
    fun hideSelectData() {
        isLongPress = false
        invalidate()
    }

    /**
     * 刷新完成
     */
    fun refreshComplete() {
        isRefreshing = false
        hideLoading()
    }

    /**
     * 刷新完成，没有数据
     */
    fun refreshEnd() {
        isLoadMoreEnd = true
        isRefreshing = false
        hideLoading()
    }

    /**
     * 重置加载更多
     */
    fun resetLoadMoreEnd() {
        isLoadMoreEnd = false
    }

    fun setLoadMoreEnd() {
        isLoadMoreEnd = true
    }

    interface KChartRefreshListener {
        /**
         * 加载更多
         *
         * @param chart
         */
        fun onLoadMoreBegin(chart: CpKLineChartView)
    }

    override fun setScaleEnable(scaleEnable: Boolean) {
        if (isRefreshing) {
            throw IllegalStateException("请勿在刷新状态设置属性")
        }
        super.setScaleEnable(scaleEnable)

    }

    override fun setScrollEnable(scrollEnable: Boolean) {
        if (isRefreshing) {
            throw IllegalStateException("请勿在刷新状态设置属性")
        }
        super.setScrollEnable(scrollEnable)
    }

    override fun setFallRiseColor(riseColor: Int, fallColor: Int) {
        // TODO 这里必须相反,蜡烛线的逻辑反了
        mMainDraw?.setFallRiseColor(fallColor, riseColor)
        mVolumeDraw?.setFallRiseColor(riseColor, fallColor)
        mMACDDraw?.setFallRiseColor(riseColor, fallColor)
    }


    /**
     * 设置DIF颜色
     */
    fun setDIFColor(color: Int) {
        mMACDDraw!!.setDIFColor(color)
    }

    /**
     * 设置DEA颜色
     */
    fun setDEAColor(color: Int) {
        mMACDDraw!!.setDEAColor(color)
    }

    /**
     * 设置MACD颜色
     */
    fun setMACDColor(color: Int) {
        mMACDDraw!!.setMACDColor(color)
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    fun setMACDWidth(MACDWidth: Float) {
        mMACDDraw?.setMACDWidth(MACDWidth)
    }

    /**
     * 设置K颜色
     */
    fun setKColor(color: Int) {
        mKDJDraw?.setKColor(color)
    }

    /**
     * 设置D颜色
     */
    fun setDColor(color: Int) {
        mKDJDraw?.setDColor(color)
    }

    /**
     * 设置J颜色
     */
    fun setJColor(color: Int) {
        mKDJDraw?.setJColor(color)
    }

    /**
     * 设置R颜色
     */
    fun setRColor(color: Int) {
        mWRDraw?.setRColor(color)
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    fun setMa5Color(color: Int) {
        mMainDraw?.setMa5Color(color)
        mVolumeDraw?.setMa5Color(color)
    }


    /**
     * 设置ma10颜色
     *
     * @param color
     */
    fun setMa10Color(color: Int) {
        mMainDraw?.setMa10Color(color)
        mVolumeDraw?.setMa10Color(color)
    }

    /**
     * 设置ma20颜色
     *
     * @param color
     */
    fun setMa30Color(color: Int) {
        mMainDraw?.setMa30Color(color)
    }

    /**
     * 设置选择器文字大小
     *
     * @param textSize
     */
    fun setSelectorTextSize(textSize: Float) {
        mMainDraw?.setMarkerTextSize(textSize)
    }

    /**
     * 设置选择器背景
     *
     * @param color
     */
    fun setSelectorBackgroundColor(color: Int) {
        mMainDraw?.setMarkerBackgroundColor(color)
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    fun setCandleWidth(candleWidth: Float) {
        mMainDraw?.setCandleWidth(candleWidth)
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    fun setCandleLineWidth(candleLineWidth: Float) {
        mMainDraw!!.setCandleLineWidth(candleLineWidth)
    }

    /**
     * 蜡烛是否空心
     */
    fun setCandleSolid(candleSolid: Boolean) {
        mMainDraw!!.isCandleSolid = candleSolid
    }

    fun setRSI1Color(color: Int) {
        mRSIDraw!!.setRSI1Color(color)
    }

    fun setRSI2Color(color: Int) {
        mRSIDraw!!.setRSI2Color(color)
    }

    fun setRSI3Color(color: Int) {
        mRSIDraw!!.setRSI3Color(color)
    }

    override fun setTextSize(textSize: Float) {
        super.setTextSize(textSize)
        mMainDraw?.setTextSize(textSize)
        mRSIDraw?.setTextSize(textSize)
        mMACDDraw?.setTextSize(textSize)
        mKDJDraw?.setTextSize(textSize)
        mWRDraw?.setTextSize(textSize)
        mVolumeDraw?.setTextSize(textSize)
    }

    override fun setLineWidth(lineWidth: Float) {
        super.setLineWidth(lineWidth)
        mMainDraw!!.setLineWidth(lineWidth)
        mRSIDraw!!.setLineWidth(lineWidth)
        mMACDDraw!!.setLineWidth(lineWidth)
        mKDJDraw!!.setLineWidth(lineWidth)
        mWRDraw!!.setLineWidth(lineWidth)
        mVolumeDraw!!.setLineWidth(lineWidth)
    }


    fun setMarkerTitleColor(color: Int) {
        mMainDraw?.setMarkerTitleColor(color)
    }


    fun setMarkerValueColor(color: Int) {
        mMainDraw?.setMarkerValueColor(color)
    }

    /**
     * 设置刷新监听
     */
    fun setRefreshListener(refreshListener: KChartRefreshListener) {
        mRefreshListener = refreshListener
    }

    fun setMainDrawLine(isLine: Boolean) {
        mMainDraw!!.isLine = isLine
        mVolumeDraw!!.isLine = isLine
        invalidate()
    }

    fun hideVolDrawView(){
        volDraw = null
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startXX = ev.x.toInt()
                startYY = ev.y.toInt()
                Log.d("onInterceptTouchEvent", "====DOWN====x:${ev.x.toInt()},y:${ev.y.toInt()}========")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d("onInterceptTouchEvent", "====MOVE====x:${ev.x.toInt()},y:${ev.y.toInt()}========")
                val dX = (ev.x - startXX).toInt()
                val dY = (ev.y - startYY).toInt()
            }
            MotionEvent.ACTION_UP -> {
                Log.d("onInterceptTouchEvent", "====UP====x:${ev.x.toInt()},y:${ev.y.toInt()}========")

//                return Math.abs(dX) > Math.abs(dY)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action?.and(MotionEvent.ACTION_MASK)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                startXX = event.x.toInt()
                startYY = event.y.toInt()

                Log.d("onTouchEvent", "====DOWN====x:${event.x.toInt()},y:${event.y.toInt()}========")

            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.x.toInt() - startXX)
                val dy = (event.y.toInt() - startYY)
                // 按压手指超过1个
                if (event.pointerCount > 1) {
                    parent.requestDisallowInterceptTouchEvent(true)
                } else {
                    parent.requestDisallowInterceptTouchEvent((abs(dx) - abs(dy)) > 100)
                }
            }

            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)
                Log.d("onTouchEvent", "====UP====x:${event.x.toInt()},y:${event.y.toInt()}========")

            }
        }
        return super.onTouchEvent(event)
    }


    override fun onLongPress(e: MotionEvent) {
        if (!isRefreshing) {
            super.onLongPress(e)
        }
    }


    fun getMainDrawStatus(): MainKlineViewStatus? {
        return mMainDraw?.status
    }


}
