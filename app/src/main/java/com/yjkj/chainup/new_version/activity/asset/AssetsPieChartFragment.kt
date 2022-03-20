package com.yjkj.chainup.new_version.activity.asset

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.timmy.tdialog.base.BaseDialogFragment
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.util.BigDecimalUtils
import com.yjkj.chainup.util.ColorUtil
import com.yjkj.chainup.util.JsonUtils
import com.yjkj.chainup.util.LogUtil
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat


class AssetsPieChartFragment : BaseDialogFragment() {

    companion object {
        @Volatile
        private var INSTANCE: AssetsPieChartFragment? = null

        val instance: AssetsPieChartFragment
            get() {
                if (INSTANCE == null) {
                    synchronized(AssetsPieChartFragment::class.java) {
                        if (INSTANCE == null) {
                            INSTANCE = AssetsPieChartFragment()
                        }
                    }
                }
                return INSTANCE!!
            }
    }

    private lateinit var mDataList: ArrayList<JSONObject>
    private lateinit var mBuffList: ArrayList<JSONObject>
    private lateinit var mAssetsAdapter: AssetsAdapter
    private lateinit var mPieChart: PieChart

    private val mDataColor = arrayOf(
            Color.parseColor("#3078FF"),
            Color.parseColor("#ED6821"),
            Color.parseColor("#7A2AE3"),
            Color.parseColor("#13B887"),
            Color.parseColor("#FFB965"),
            Color.parseColor("#461B72"))

    private lateinit var totalBalance: String

    override fun getLayoutRes(): Int {
        return R.layout.fragment_assets_pie_chart
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        if (window != null) {
            val layoutParams = window.attributes
            //透明度
            layoutParams.dimAmount = 0.8f
            //位置
            layoutParams.gravity = gravity
            window.attributes = layoutParams
        }
    }

    override fun bindView(view: View?) {
        mDataList = ArrayList()
        mBuffList = ArrayList()
        mDataList.addAll(JsonUtils.jsonToList(tag.toString(), JSONObject::class.java))
        totalBalance = "0"
        for (buff in mDataList) {
            totalBalance = BigDecimalUtils.add(buff?.optString("allBtcValuatin"),totalBalance).toPlainString()

        }
        for (buff in mDataList) {
            val value = buff?.optString("allBtcValuatin")
            buff.put("proportion", BigDecimalUtils.div(value, totalBalance).toPlainString())
        }
        mDataList.sortByDescending { BigDecimal(it.optString("allBtcValuatin")) }

        if (mDataList.size >= 6) {
            mBuffList.addAll(mDataList.subList(0, 5))

            var otherTotal: BigDecimal = BigDecimal.ZERO
            var total: BigDecimal = BigDecimal.ONE
            for (i in mDataList.indices) {
                if (i < 5) {
                    otherTotal = BigDecimalUtils.add(otherTotal.toPlainString(), mDataList[i].optString("proportion"))
                }
            }
            mDataList[5].putOpt("coinName", LanguageUtil.getString(context, "other"))
            mDataList[5].putOpt("proportion", BigDecimalUtils.sub(total.toPlainString(), otherTotal.toPlainString()).toPlainString())
            mBuffList.add(mDataList[5])
            mDataList.clear()
            mDataList.addAll(mBuffList)
        }

        for (index in mDataList.indices) {
            mDataList[index].put("color", mDataColor[index])
        }

        val pie_chart = view?.findViewById<PieChart>(R.id.pie_chart)
        val rv_asset_proportion = view?.findViewById<RecyclerView>(R.id.rv_asset_proportion)
        val tv_cancel = view?.findViewById<TextView>(R.id.tv_cancel)
        tv_cancel?.setOnClickListener { dismiss() }
        pie_chart?.let { initChart(it) }
        rv_asset_proportion?.let { showRv(it) }
    }

    private fun showRv(it: RecyclerView) {


        mAssetsAdapter = AssetsAdapter(R.layout.item_asset_proportion, mDataList)
        it.apply {
            layoutManager = GridLayoutManager(activity, 2)
            adapter = mAssetsAdapter
        }
        mAssetsAdapter.setOnItemClickListener { adapter, view, position ->
            for (index in mDataList.indices) {
                mDataList[index].put("isSelect", index == position)
            }
            mPieChart.highlightValue(position.toFloat(), 0)
            mPieChart.setCenterText(generateCenterSpannableText(mDataList[position].optString("coinName"), DecimalFormat("0.00%").format(BigDecimal(mDataList[position].optString("proportion"))), position))
            mPieChart.invalidate()
            mAssetsAdapter.notifyDataSetChanged()

        }
    }

    private fun initChart(piechart: PieChart) {
        mPieChart = piechart
        piechart.legend.isEnabled = false
        piechart.description.isEnabled = false
        piechart.holeRadius = 68f
        piechart.setHoleColor(ColorUtil.getColorByMode(R.color.open_postions_coin_cancel_color_day))
        piechart.setCenterTextColor(ColorUtil.getColorByMode(R.color.assets_center_text_color_day))
        piechart.setDrawEntryLabels(false)
        piechart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {

            }

            override fun onValueSelected(e: Entry?, h: Highlight) {
                if (e == null) {
                    return
                }
                LogUtil.e("h.x", h.x.toString())
                LogUtil.e("h.y", h.y.toString())
                LogUtil.e("h.dataIndex", h.dataIndex.toString())
                val pe = e as PieEntry
                for (buff in mDataList) {
                    buff.put("isSelect", buff.optString("coinName").equals(pe?.label))
                    if (buff.optString("coinName").equals(pe?.label)) {
                        if (BigDecimalUtils.compareTo(buff.optString("proportion"),"1")>=0){
                            piechart.setCenterText(generateCenterSpannableText(NCoinManager.getShowMarket(buff.optString("coinName")), DecimalFormat("0%").format(BigDecimal("1")), 0))
                        }else{
                            piechart.setCenterText(generateCenterSpannableText(NCoinManager.getShowMarket(buff.optString("coinName")), DecimalFormat("0.00%").format(BigDecimal(buff.optString("proportion"))), h.x.toInt()))
                        }
                    }
                }
                mAssetsAdapter.notifyDataSetChanged()
            }
        })

        if (BigDecimalUtils.compareTo(mDataList[0].optString("proportion"),"1")>=0){
            piechart.setCenterText(generateCenterSpannableText(NCoinManager.getShowMarket(mDataList[0].optString("coinName")), DecimalFormat("0%").format(BigDecimal("1")), 0))
        }else{
            piechart.setCenterText(generateCenterSpannableText(NCoinManager.getShowMarket(mDataList[0].optString("coinName")), DecimalFormat("0.00%").format(BigDecimal(mDataList[0].optString("proportion"))), 0))
        }
        mDataList[0].put("isSelect", true)
        val yVals: MutableList<PieEntry> = ArrayList()
        val colors: MutableList<Int> = ArrayList()
        val xVals = ArrayList<String>()
        for (index in mDataList.indices) {
            var buff = mDataList[index].optString("proportion").toFloat() * 100
            buff = if (buff < 1f) 1f else buff
            yVals.add(PieEntry(buff, mDataList[index].optString("coinName")))
            colors.add(mDataColor[index])
        }
        val pieDataSet = PieDataSet(yVals, "")
        pieDataSet.setColors(colors)
        pieDataSet.sliceSpace = 4f
        pieDataSet.xValuePosition
        val pieData = PieData(pieDataSet)
        pieData.setDrawValues(false)
        piechart.setData(pieData)
    }


    class AssetsAdapter(layoutResId: Int, data: MutableList<JSONObject>) :
            BaseQuickAdapter<JSONObject, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: JSONObject) {
            helper.setText(R.id.tv_coin_name, NCoinManager.getShowMarket(item.optString("coinName")))
            helper.setText(R.id.tv_value, DecimalFormat("0.00%").format(BigDecimal(item.optString("proportion"))))
            helper.setTextColor(R.id.tv_coin_name, ColorUtil.getColorByMode(R.color.assets_center_text_color_day))
            helper.setTextColor(R.id.tv_value, ColorUtil.getColorByMode(R.color.assets_center_text_color_day))
            helper.setBackgroundColor(R.id.img_tag, item.optInt("color"))
            helper.setBackgroundResource(R.id.rl_asset_proportion, if (item.optBoolean("isSelect")) R.drawable.shape_coin_select else R.drawable.shape_coin_select_no)
        }
    }

    override fun getDialogView(): View? {
        return null
    }

    private fun generateCenterSpannableText(coinName: String, value: String, position: Int): SpannableString? {
        val s = SpannableString(coinName + "\n" + value)
        s.setSpan(RelativeSizeSpan(1.2f), 0, coinName.length, 0);
        s.setSpan(ForegroundColorSpan(mDataColor[position]), 0, coinName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(RelativeSizeSpan(1.7f), coinName.length + 1, s.length, 0);
        s.setSpan(StyleSpan(Typeface.BOLD), coinName.length + 1, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s
    }

    override fun onDestroy() {
        super.onDestroy()
        INSTANCE = null
    }
}
