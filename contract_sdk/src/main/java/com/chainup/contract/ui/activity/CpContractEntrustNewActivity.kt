package com.chainup.contract.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.LogUtils
import com.chainup.contract.R
import com.chainup.contract.adapter.CpCoinSelectLeftAdapter
import com.chainup.contract.adapter.CpCoinSelectRightAdapter
import com.chainup.contract.adapter.CpTopPopAdapter
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.fragment.CpContractEntrustNewFragment
import com.chainup.contract.ui.fragment.CpContractHistoryEntrustNewFragment
import com.chainup.contract.ui.fragment.CpContractPLRecordFragment
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.utils.setSafeListener
import com.chainup.contract.view.CpNewDialogUtils
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_contract.bean.CpCurrentOrderBean
import kotlinx.android.synthetic.main.cp_activity_contract_entrust.*
import kotlinx.android.synthetic.main.cp_activity_contract_entrust_new.*
import kotlinx.android.synthetic.main.cp_activity_contract_entrust_new.ic_close
import kotlinx.android.synthetic.main.cp_activity_contract_entrust_new.sub_tab_layout
import kotlinx.android.synthetic.main.cp_activity_contract_entrust_new.tv_cancel_orders
import org.json.JSONArray
import org.json.JSONObject

/**
 * 合约当前/历史委托/盈亏记录
 */
class CpContractEntrustNewActivity : CpNBaseActivity() {
    override fun setContentView() = R.layout.cp_activity_contract_entrust_new

    //当前选择的币对
    private var mCurrContractInfo: CpTabInfo? = null

    //限价和计划委托
    private var entrustList = ArrayList<CpTabInfo>()
    private var mCurrEntrustInfo: CpTabInfo? = null

    //全部方向/全部类型
    private var sideList = ArrayList<CpTabInfo>()
    private var mCurrSideInfo: CpTabInfo? = null

    //合约订单类型
    private var typeList = ArrayList<CpTabInfo>()
    private var mCurrTypeInfo: CpTabInfo? = null

    private var viewPagePosition: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
    }

    override fun loadData() {
        mContractId = intent.getIntExtra("contractId", -1)
        // 限价/计划委托
        entrustList.add(CpTabInfo(getString(R.string.cp_extra_text20), 0))
        entrustList.add(CpTabInfo(getString(R.string.cp_order_text61), 1))
        mCurrEntrustInfo = entrustList[0]
        //方向和类型
        sideList.add(CpTabInfo(getString(R.string.cp_order_text98), 0, ""))
        sideList.add(CpTabInfo(getString(R.string.cp_order_text6), 1, "BUY"))
        sideList.add(CpTabInfo(getString(R.string.cp_order_text15), 2, "SELL"))

        mCurrSideInfo = sideList[0]
        //合约订单类型
        typeList.add(CpTabInfo(getString(R.string.cp_order_text4), 0))
        typeList.add(CpTabInfo(getString(R.string.cp_overview_text3), 1))
        typeList.add(CpTabInfo("Post Only", 5))
        mCurrTypeInfo = typeList[0]

        mCurrContractInfo = CpTabInfo("", 0)

        tv_coins_name.setText(CpClLogicContractSetting.getContractShowNameById(this, mContractId))
        getCurrentCommonOrderList()
    }

    var isShowEntrustTypeSelect: Boolean = false;
    var isShowContractSelect: Boolean = false;
    var isShowOrderTypeSelect: Boolean = false;
    var isShowSideTypeSelect: Boolean = false;
    override fun initView() {
        initTabInfo()
        ic_close.setOnClickListener {
            finish()
        }
        tv_cancel_orders.setSafeListener {
            cancelOrder("");
        }
        ll_dissmiss.setOnClickListener { ll_common_select.visibility = View.GONE }
        dissmiss.setOnClickListener { ll_coin_select.visibility = View.GONE }
        //选择合约币对
        ll_sel_coins.setOnClickListener {
            setCoinData()
            if (!isShowContractSelect) {
                ll_coin_select.visibility = View.VISIBLE
                ll_common_select.visibility = View.GONE
                img_coins_name_arrow.animate().setDuration(200).rotation(180f).start()
            } else {
                ll_coin_select.visibility = View.GONE
                img_coins_name_arrow.animate().setDuration(200).rotation(0f).start()
            }
            isShowContractSelect = !isShowContractSelect
            isShowEntrustTypeSelect = false
        }
        //选择委托类型
        ll_entrust_type.setOnClickListener {
            if (!isShowEntrustTypeSelect) {
                ll_coin_select.visibility = View.GONE
                ll_common_select.visibility = View.VISIBLE
                img_entrust_type_arrow.animate().setDuration(200).rotation(180f).start()
            } else {
                img_entrust_type_arrow.animate().setDuration(200).rotation(0f).start()
                ll_common_select.visibility = View.GONE
            }
            isShowEntrustTypeSelect = !isShowEntrustTypeSelect
            isShowContractSelect = false
            var adapter = CpTopPopAdapter(entrustList, mCurrEntrustInfo!!.index)
            recycler_view?.layoutManager = LinearLayoutManager(this)
            recycler_view?.adapter = adapter
            recycler_view?.setHasFixedSize(true)
            adapter.setOnItemClickListener { adapter, view, position ->
                tv_entrust_type.setText(entrustList[position].name)
                mCurrEntrustInfo = CpTabInfo(entrustList[position].name, position)
                val event = CpMessageEvent(CpMessageEvent.sl_contract_record_switch_entrust_type_event)
                event.msg_content = (position == 0)
                CpEventBusUtil.post(event)
                ll_common_select.visibility = View.GONE
                if (viewPagePosition == 0) {
                    getCurrentCommonOrderList()
                }
            }
        }
        //选择合约订单类型
        ll_contract_type.setOnClickListener {
            if (!isShowOrderTypeSelect) {
                ll_coin_select.visibility = View.GONE
                ll_common_select.visibility = View.VISIBLE
                img_order_type_arrow.animate().setDuration(200).rotation(180f).start()
            } else {
                img_order_type_arrow.animate().setDuration(200).rotation(0f).start()
                ll_common_select.visibility = View.GONE
            }
            isShowOrderTypeSelect = !isShowOrderTypeSelect
            var adapter = CpTopPopAdapter(typeList, mCurrTypeInfo!!.index)
            recycler_view?.layoutManager = LinearLayoutManager(this)
            recycler_view?.adapter = adapter
            recycler_view?.setHasFixedSize(true)
            adapter.setOnItemClickListener { adapter, view, position ->
                tv_order_type.setText(typeList[position].name)
                mCurrTypeInfo = CpTabInfo(typeList[position].name, position)
                val event = CpMessageEvent(CpMessageEvent.sl_contract_record_switch_order_type_event)
                event.msg_content = position
                CpEventBusUtil.post(event)
                ll_common_select.visibility = View.GONE
            }
        }
        //选择合约方向
        ll_contract_direction.setOnClickListener {
            if (!isShowSideTypeSelect) {
                ll_coin_select.visibility = View.GONE
                ll_common_select.visibility = View.VISIBLE
                img_side_type_arrow.animate().setDuration(200).rotation(180f).start()
            } else {
                img_side_type_arrow.animate().setDuration(200).rotation(0f).start()
                ll_common_select.visibility = View.GONE
            }
            isShowSideTypeSelect = !isShowSideTypeSelect
            var adapter = CpTopPopAdapter(sideList, mCurrSideInfo!!.index)
            recycler_view?.layoutManager = LinearLayoutManager(this)
            recycler_view?.adapter = adapter
            recycler_view?.setHasFixedSize(true)
            adapter.setOnItemClickListener { adapter, view, position ->
                tv_contract_direction.setText(sideList[position].name)
                mCurrSideInfo = CpTabInfo(sideList[position].name, position)
                val event = CpMessageEvent(CpMessageEvent.sl_contract_record_switch_contract_side_event)
                event.msg_content = sideList[position].extras
                CpEventBusUtil.post(event)
                ll_common_select.visibility = View.GONE
            }
        }
    }

    private fun setCoinData() {
        var sideList = ArrayList<CpTabInfo>()
        var sideListBuff = ArrayList<CpTabInfo>()
        var sideListU = ArrayList<CpTabInfo>()
        var sideListB = ArrayList<CpTabInfo>()
        var sideListH = ArrayList<CpTabInfo>()
        var sideListM = ArrayList<CpTabInfo>()

        var isHasU = false //正向合约
        var isHasB = false //币本位
        var isHasH = false //混合合约
        var isHasM = false //模拟合约
        val mContractList = JSONArray(CpClLogicContractSetting.getContractJsonListStr(this))
        var positionLeft = 0
        for (i in 0 until mContractList.length()) {
            val obj = mContractList.getJSONObject(i)
            val contractSide = obj.getInt("contractSide")
            val contractType = obj.getString("contractType")
            val id = obj.getInt("id")

            //classification 1,USDT合约 2,币本位合约 3,混合合约 4,模拟合约
            when (contractType) {
                "E" -> {
                    isHasU = true
                    sideListU.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                }
                "S" -> {
                    isHasM = true
                    sideListM.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                }
                else -> {
                    isHasH = true
                    sideListH.add(CpTabInfo(obj.getString("symbol"), obj.getInt("id")))
                }
            }
            if (mContractId == id) {
                positionLeft = when (contractType) {
                    "E" -> {
                        0
                    }
                    "S" -> {
                        3
                    }
                    else -> {
                        2
                    }
                }
            }
        }
        if (isHasU) {
            sideList.add(CpTabInfo(getString(R.string.cp_contract_data_text13), 0, if (positionLeft == 0) 0 else 1))
        }
        if (isHasB) {
            sideList.add(CpTabInfo(getString(R.string.cp_contract_data_text10), 1, if (positionLeft == 1) 0 else 1))
        }
        if (isHasH) {
            sideList.add(CpTabInfo(getString(R.string.cp_contract_data_text12), 2, if (positionLeft == 2) 0 else 1))
        }
        if (isHasM) {
            sideList.add(CpTabInfo(getString(R.string.cp_contract_data_text11), 3, if (positionLeft == 3) 0 else 1))
        }
        if (positionLeft == 0) {
            sideListBuff.addAll(sideListU)
        } else if (positionLeft == 1) {
            sideListBuff.addAll(sideListB)
        } else if (positionLeft == 3) {
            sideListBuff.addAll(sideListM)
        } else {
            sideListBuff.addAll(sideListH)
        }
        var mRightAdapter = CpCoinSelectRightAdapter(sideListBuff, mContractId)
        rv_right?.layoutManager = LinearLayoutManager(this)
        rv_right?.adapter = mRightAdapter
        rv_right?.setHasFixedSize(true)
        mRightAdapter.setOnItemClickListener { adapter, view, position ->
            mCurrContractInfo = CpTabInfo(sideListBuff[position].name, position)
            mContractId = sideListBuff[position].index
            LogUtils.e("--------------------"+ mContractId)
            tv_coins_name.setText(CpClLogicContractSetting.getContractShowNameById(this@CpContractEntrustNewActivity, mContractId))
            val event = CpMessageEvent(CpMessageEvent.sl_contract_record_switch_contract_event)
            event.msg_content = mContractId
            CpEventBusUtil.post(event)
            ll_coin_select.visibility = View.GONE
        }
        var adapter = CpCoinSelectLeftAdapter(sideList, positionLeft)
        rv_left?.layoutManager = LinearLayoutManager(this)
        rv_left?.adapter = adapter
        rv_left?.setHasFixedSize(true)
        adapter.setOnItemClickListener { adapter, view, position ->
            sideListBuff.clear()
            if (sideList[position].index == 0) {
                sideListBuff.addAll(sideListU)
            } else if (sideList[position].index == 1) {
                sideListBuff.addAll(sideListB)
            } else if (sideList[position].index == 2) {
                sideListBuff.addAll(sideListH)
            } else {
                sideListBuff.addAll(sideListM)
            }
            mRightAdapter.notifyDataSetChanged()

            for (buff in sideList) {
                buff.extrasNum = if (buff.index == sideList[position].index) 0 else 1
            }
            adapter.notifyDataSetChanged()
        }
    }

    private var mFragments: ArrayList<Fragment>? = null
    private fun initTabInfo() {
        mFragments = ArrayList()
        mFragments?.add(CpContractEntrustNewFragment())
        mFragments?.add(CpContractHistoryEntrustNewFragment())
        mFragments?.add(CpContractPLRecordFragment())
        sub_tab_layout.setViewPager(vp_order, arrayOf(getString(R.string.cp_order_text51), getString(R.string.cp_order_text72), getString(R.string.cp_order_text73)), this, mFragments)
        vp_order.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val event = CpMessageEvent(CpMessageEvent.sl_contract_record_switch_tab_event)
                event.msg_content = position
                CpEventBusUtil.post(event)
                ll_contract_type.visibility = if (position == 2) View.GONE else View.VISIBLE
                ll_contract_direction.visibility = if (position != 2) View.GONE else View.VISIBLE
                ll_entrust_type.visibility = if (position == 2) View.GONE else View.VISIBLE
                typeList.clear()
                viewPagePosition = position
                if (position == 0) {
                    typeList.add(CpTabInfo(getString(R.string.cp_order_text4), 0))
                    typeList.add(CpTabInfo(getString(R.string.cp_overview_text3), 1))
                    typeList.add(CpTabInfo("Post Only", 5))
                    getCurrentCommonOrderList()
                } else {
                    tv_cancel_orders.visibility = View.GONE
                    typeList.add(CpTabInfo(getString(R.string.cp_order_text4), 0))
                    typeList.add(CpTabInfo(getString(R.string.cp_overview_text3), 1))
                    typeList.add(CpTabInfo(getString(R.string.cp_overview_text4), 2))
                    typeList.add(CpTabInfo("IOC", 3))
                    typeList.add(CpTabInfo("FOK", 4))
                    typeList.add(CpTabInfo("Post Only", 5))
                }
            }
        })


    }


    fun getCurrentCommonOrderList() {
        tv_cancel_orders.visibility = View.GONE
        addDisposable(
                getContractModel().getCurrentOrderList(
                    mContractId.toString(),
                        0,
                        1,
                        consumer = object : CpNDisposableObserver(true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                jsonObject.optJSONObject("data").run {
                                    if (!isNull("orderList")) {
                                        if (mCurrEntrustInfo?.index == 0) {
                                            tv_cancel_orders.visibility = View.VISIBLE
                                        }
                                    }
                                }
                                getCurrentPlanOrderList()
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                            }
                        })
        )
    }

    fun getCurrentPlanOrderList() {
        addDisposable(
                getContractModel().getCurrentPlanOrderList(
                    mContractId.toString(),
                        0,
                        1,
                        consumer = object : CpNDisposableObserver(true) {
                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                val mListBuffer = ArrayList<CpCurrentOrderBean>()
                                swipe_refresh?.isRefreshing = false
                                jsonObject.optJSONObject("data").run {
                                    if (!isNull("trigOrderList")) {
                                        if (mCurrEntrustInfo?.index == 1) {
                                            tv_cancel_orders.visibility = View.VISIBLE
                                        }
                                    }
                                }
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                            }
                        })
        )
    }


    private fun cancelOrder(orderId: String) {
        CpNewDialogUtils.showDialog(
                mActivity,
                getString(R.string.cp_overview_text58),
                false,
                object : CpNewDialogUtils.DialogBottomListener {
                    override fun sendConfirm() {
                        addDisposable(
                                getContractModel().orderCancel(
                                    mContractId.toString(),
                                        orderId,
                                        mCurrEntrustInfo?.index == 1,
                                        consumer = object : CpNDisposableObserver(mActivity, true) {
                                            override fun onResponseSuccess(jsonObject: JSONObject) {
                                                val event = CpMessageEvent(CpMessageEvent.sl_contract_record_switch_entrust_type_event)
                                                event.msg_content = (mCurrEntrustInfo?.index == 0)
                                                CpEventBusUtil.post(event)
                                                getCurrentCommonOrderList()
                                            }
                                        })
                        )
                    }
                },
                getString(R.string.cp_overview_text59),
                getString(R.string.cp_calculator_text16),
                getString(R.string.cp_overview_text56)
        )
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.apply {
            if (!inRangeOfView(ll_sel_coins, ev) && !inRangeOfView(ll_coin_select, ev)) {
                ll_coin_select.visibility = View.GONE
            }
            if (!inRangeOfView(ll_entrust_type, ev) && !inRangeOfView(ll_contract_type, ev) && !inRangeOfView(ll_contract_direction, ev) && !inRangeOfView(ll_common_select, ev)) {
                ll_common_select.visibility = View.GONE
            }
            if (isShowContractSelect) {
                img_coins_name_arrow.animate().setDuration(200).rotation(0f).start()
            }
            if (isShowEntrustTypeSelect) {
                img_entrust_type_arrow.animate().setDuration(200).rotation(0f).start()
            }
            if (isShowOrderTypeSelect) {
                img_order_type_arrow.animate().setDuration(200).rotation(0f).start()
            }
            if (isShowSideTypeSelect) {
                img_side_type_arrow.animate().setDuration(200).rotation(0f).start()
            }
        }

        return super.dispatchTouchEvent(ev)

    }

//    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//        if (!inRangeOfView(ll_sel_coins, ev) && !inRangeOfView(ll_coin_select, ev)) {
//            ll_coin_select.visibility = View.GONE
//        }
//        if (!inRangeOfView(ll_entrust_type, ev) && !inRangeOfView(ll_contract_type, ev) && !inRangeOfView(ll_contract_direction, ev) && !inRangeOfView(ll_common_select, ev)) {
//            ll_common_select.visibility = View.GONE
//        }
//        if (isShowContractSelect){
//            img_coins_name_arrow.animate().setDuration(200).rotation(0f).start()
//        }
//        if (isShowEntrustTypeSelect){
//            img_entrust_type_arrow.animate().setDuration(200).rotation(0f).start()
//        }
//        if (isShowOrderTypeSelect){
//            img_order_type_arrow.animate().setDuration(200).rotation(0f).start()
//        }
//        if (isShowSideTypeSelect){
//            img_side_type_arrow.animate().setDuration(200).rotation(0f).start()
//        }
//        return super.dispatchTouchEvent(ev)
//    }

    private fun inRangeOfView(view: View, ev: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        return if (ev.x < x || ev.x > x + view.width || ev.y < y || ev.y > y + view.height) {
            false
        } else true
    }

    companion object {
        fun show(activity: Activity, contractId: Int = 0, contractName: String) {
            val intent = Intent(activity, CpContractEntrustNewActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("contractId", contractId)
            bundle.putString("contractName", contractName)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }

        var mContractId = -1
    }
}