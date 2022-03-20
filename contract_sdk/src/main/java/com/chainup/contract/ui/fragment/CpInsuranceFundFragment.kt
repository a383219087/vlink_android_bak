package com.chainup.contract.ui.fragment

import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chainup.contract.R
import com.chainup.contract.base.CpNBaseFragment
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.utils.CpBigDecimalUtils
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.CpColorUtil
import com.chainup.contract.utils.CpDateUtils
import com.chainup.contract.view.CpEmptyForAdapterView
import com.chainup.contract.view.CpNewMarkerView
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import kotlinx.android.synthetic.main.fragment_capital_rate.lineChart
import kotlinx.android.synthetic.main.fragment_capital_rate.ll_layout
import kotlinx.android.synthetic.main.fragment_cp_insurance_fund.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.text.DecimalFormat


private const val ARG_PARAM1 = "marginCoin"
class CpInsuranceFundFragment : CpNBaseFragment() {
    private var contractId: Int? = null
    private var marginCoin: String = "USDT"

    private var mContractTransactionRecordAdapter: ContractCapitalRateAdapter? = null
    private val mList = ArrayList<JSONObject>()

    override fun setContentView(): Int {
        return R.layout.fragment_cp_insurance_fund
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contractId = it.getInt(ARG_PARAM1)
            marginCoin=CpClLogicContractSetting.getContractMarginCoinById(context,contractId!!)
        }
    }

    override fun initView() {
        getData()

        mContractTransactionRecordAdapter = ContractCapitalRateAdapter(mList)
        ll_layout.layoutManager = LinearLayoutManager(activity)
        mContractTransactionRecordAdapter?.setEmptyView(CpEmptyForAdapterView(this!!.activity!!))
        ll_layout.adapter = mContractTransactionRecordAdapter
    }

    private fun setLineChartData(data: JSONArray) {
        lineChart.clear()
        lineChart.setScaleMinima(1.0f, 1.0f)
        lineChart.getViewPortHandler().refresh(Matrix(), lineChart, true)
        lineChart.getDescription().setEnabled(false)
        val entries: ArrayList<Entry> = ArrayList()
        val xValues = ArrayList<String>()
        val yValues = ArrayList<String>()
        for (i in 0..(data.length() - 1)) {
            var obj: JSONObject = data.get(i) as JSONObject
            entries.add(Entry(i.toFloat(), obj.optString("amount").toFloat()))
            xValues.add(CpDateUtils.long2StringMS(CpDateUtils.FORMAT_MONTH_DAY, obj.optString("ctime").toLong()))
            yValues.add(obj.optString("amount"))
        }
        val dataSet = LineDataSet(entries, "")
        dataSet.color = CpColorUtil.getColor(R.color.main_color)
        dataSet.lineWidth = 3f
        dataSet.setCircleColor(CpColorUtil.getColor(R.color.stroke_hint_color))
        dataSet.setDrawCircles(false)
        dataSet.setDrawFilled(true)
        dataSet.fillDrawable=getResources().getDrawable(R.drawable.cp_chart_fill)
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineChart.legend.isEnabled = false
        val xAxis: XAxis = lineChart.getXAxis()
        xAxis.textColor=CpColorUtil.getColor(R.color.stroke_hint_color)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.setGranularity(1f)
        xAxis.setDrawAxisLine(false)
        xAxis.setLabelCount(7, true);
        xAxis.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return xValues.get(value.toInt() % xValues.size)
            }
        })
        val yAxis = lineChart.axisLeft
        yAxis.textColor=CpColorUtil.getColor(R.color.stroke_hint_color)
        yAxis.setDrawAxisLine(false)
        yAxis.setDrawGridLines(true);
        yAxis.setGridDashedLine(DashPathEffect(floatArrayOf(4f,4f),0f))
        yAxis.gridColor=CpColorUtil.getColor(R.color.stroke_hint_color)
        val rightYAxis: YAxis = lineChart.getAxisRight()
        rightYAxis.isEnabled = false

        val description = Description()
        description.setEnabled(false)
        lineChart.description = description
        val ratio = xValues.size / 7
        lineChart.zoom(ratio.toFloat(),1f,0f,0f)
        val mNewMarkerView = CpNewMarkerView(activity, R.layout.cp_custom_marker_view_layout)
        mNewMarkerView.chartView = lineChart
        mNewMarkerView.setCallBack(object : CpNewMarkerView.CallBack {
            override fun onCallBack(x: Float, value: String?) {
                val index = x.toInt()
                if (index >= entries.size) {
                    return
                }
//                LogUtil.e(TAG, index.toString())
//                if (isMarginBalance){
//                    mNewMarkerView.getTvContent().setText(entries.get(index).y.toString() + " USDT" + "\n" + xValues.get(index).toString())
//                }else{
                val df = DecimalFormat("0.000")
                var ff= CpDateUtils.long2StringMS(CpDateUtils.FORMAT_MONTH_DAY_HOUR_MIN_SECOND,mList.get(index).optString("ctime").toLong())
                var tt= CpBigDecimalUtils.showSNormal(CpBigDecimalUtils.mul(entries.get(index).y.toString(),"100").toPlainString())
                tt= df.format(BigDecimal(entries.get(index).y.toString()))
                mNewMarkerView.getTvContent().setText(tt + "\n" + ff)
//                }
            }
        })
        lineChart.marker = mNewMarkerView
        lineChart.setScaleEnabled(false)
        val lineData = LineData(dataSet)
        lineChart.moveViewToX(lineData.entryCount.toFloat());
        lineData.setDrawValues(false)
        lineChart.setData(lineData)
        lineChart.invalidate()
        lineChart.animateY(200)
//        lineChart.
    }



    class ContractCapitalRateAdapter(data: ArrayList<JSONObject>) : BaseQuickAdapter<JSONObject, BaseViewHolder>(R.layout.cp_item_contract_capital_rate, data) {
        override fun convert(helper: BaseViewHolder, item: JSONObject) {
            helper?.run {
                val buff= DecimalFormat("0.000").format(BigDecimal(item.optString("amount")))
                //金额
                setText(R.id.tv_amount_value, buff)
                //时间
                setText(R.id.tv_time_value, CpDateUtils.long2StringMS(CpDateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND,item.optString("ctime").toLong()))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    override fun onMessageEvent(event: CpMessageEvent) {
        when (event.msg_type) {
            CpMessageEvent.sl_contract_calc_switch_margin_coin_event -> {
                marginCoin = event.msg_content as String
                getData()
            }
        }
    }

    private fun getData() {
        addDisposable(getContractModel().riskBalanceList(marginCoin, "1",
                consumer = object : CpNDisposableObserver(activity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            closeLoadingDialog()
                            setLineChartData(optJSONArray("brokenLineList"))
                            if (!isNull("brokenLineList")) {
                                mList.clear()
                                val mHistoryList = optJSONArray("brokenLineList")
                                if (mHistoryList.length() != 0) {
                                    for (i in 0..(mHistoryList.length() - 1)) {
                                        var obj: JSONObject = mHistoryList.get(i) as JSONObject
                                        mList.add(obj)
                                    }
                                    mList.reverse()
                                }
                            }
                            mContractTransactionRecordAdapter?.notifyDataSetChanged()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        closeLoadingDialog()
                    }
                }))


        addDisposable(getContractModel().getRiskAccount(marginCoin,
                consumer = object : CpNDisposableObserver(activity, true) {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        jsonObject.optJSONObject("data").run {
                            closeLoadingDialog()
                            tv_margin_balance_msg.setText( marginCoin + " "+getString(R.string.cp_contract_data_text19))
                            tv_margin_balance.setText(CpBigDecimalUtils.showSNormal(optString("amount"), 2))
                            tv_margin_coin.setText(marginCoin)
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        closeLoadingDialog()
                    }
                }))
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
                CpInsuranceFundFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PARAM1, param1)
                    }
                }
    }
}