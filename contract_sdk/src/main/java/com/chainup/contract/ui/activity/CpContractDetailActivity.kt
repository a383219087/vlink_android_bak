package com.yjkj.chainup.new_contract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.chainup.contract.R
import com.chainup.contract.adapter.CpCoinSelectLeftAdapter
import com.chainup.contract.adapter.CpCoinSelectRightAdapter
import com.chainup.contract.adapter.CpTopPopAdapter
import com.chainup.contract.base.CpNBaseActivity
import com.chainup.contract.bean.CpTabInfo
import com.chainup.contract.eventbus.CpEventBusUtil
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.activity.CpContractEntrustNewActivity
import com.chainup.contract.ui.fragment.CpCapitalRateFragment
import com.chainup.contract.ui.fragment.CpContractParaFragment
import com.chainup.contract.ui.fragment.CpInsuranceFundFragment
import com.chainup.contract.utils.CpClLogicContractSetting
import com.chainup.contract.view.CpNewDialogUtils
import com.timmy.tdialog.TDialog
import kotlinx.android.synthetic.main.cp_activity_contract_detail.*
import org.json.JSONArray


/**
 * 合约详情
 */
class CpContractDetailActivity : CpNBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.cp_activity_contract_detail
    }

    var isShowContractSelect: Boolean = false;
    var isShowMarginCoinSelect: Boolean = false;
    private var contractId = -1
    private var mCurrContractInfo: CpTabInfo? = null
    private var mFragments: ArrayList<Fragment>? = null
    private var selPosition = 0

    //保证金币种
    private var contractList = ArrayList<CpTabInfo>()
    private var mCurrMarginCoinInfo: CpTabInfo? = null
    private var contractDialog: TDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        initView()
    }


    override fun initView() {
        super.initView()
        initAutoTextView()
        initTabInfo()

        var adapter = CpTopPopAdapter(contractList, mCurrMarginCoinInfo!!.index)
        recycler_view?.layoutManager = LinearLayoutManager(this)
        recycler_view?.adapter = adapter
        recycler_view?.setHasFixedSize(true)
        adapter.setOnItemClickListener { adapter, view, position ->
            mCurrMarginCoinInfo = contractList[position]
            tv_order_type.setText(mCurrMarginCoinInfo?.name)
            val event = CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_margin_coin_event)
            event.msg_content = mCurrMarginCoinInfo?.name
            CpEventBusUtil.post(event)
            ll_common_select.visibility = View.GONE

            img_order_type.animate().setDuration(200).rotation(0f).start()
            isShowMarginCoinSelect = false
        }
        ll_order_type.setOnClickListener {
            if (selPosition == 1 || selPosition == 0) {
                setCoinData()
                if (!isShowContractSelect) {
                    ll_coin_select.visibility = View.VISIBLE
                    img_order_type.animate().setDuration(200).rotation(180f).start()
                } else {
                    ll_coin_select.visibility = View.GONE
                    img_order_type.animate().setDuration(200).rotation(0f).start()
                }
                isShowContractSelect = !isShowContractSelect
            } else {
                if (!isShowMarginCoinSelect) {
                    ll_common_select.visibility = View.VISIBLE
                    img_order_type.animate().setDuration(200).rotation(180f).start()
                } else {
                    ll_common_select.visibility = View.GONE
                    img_order_type.animate().setDuration(200).rotation(0f).start()
                }
                isShowMarginCoinSelect = !isShowMarginCoinSelect


                var adapter = CpTopPopAdapter(contractList, mCurrMarginCoinInfo!!.index)
                recycler_view?.layoutManager = LinearLayoutManager(this)
                recycler_view?.adapter = adapter
                recycler_view?.setHasFixedSize(true)
                adapter.setOnItemClickListener { adapter, view, position ->
                    mCurrMarginCoinInfo = contractList[position]
                    tv_order_type.setText(mCurrMarginCoinInfo?.name)
                    val event = CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_margin_coin_event)
                    event.msg_content = mCurrMarginCoinInfo?.name
                    CpEventBusUtil.post(event)
                    ll_common_select.visibility = View.GONE

                    img_order_type.animate().setDuration(200).rotation(0f).start()
                    isShowMarginCoinSelect = false
                }
            }
        }
        dissmiss.setOnClickListener {
            isShowContractSelect = false
            ll_coin_select.visibility = View.GONE
            img_order_type.animate().setDuration(200).rotation(0f).start()
        }
        ll_dissmiss.setOnClickListener {
            isShowMarginCoinSelect = false
            ll_common_select.visibility = View.GONE
            img_order_type.animate().setDuration(200).rotation(0f).start()
        }

        ic_close.setOnClickListener { finish() }
    }

    private fun initAutoTextView() {
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
            if (CpContractEntrustNewActivity.mContractId == id) {
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
        var mRightAdapter = CpCoinSelectRightAdapter(sideListBuff, contractId)
        rv_right?.layoutManager = LinearLayoutManager(this)
        rv_right?.adapter = mRightAdapter
        rv_right?.setHasFixedSize(true)
        mRightAdapter.setOnItemClickListener { adapter, view, position ->
            mCurrContractInfo = CpTabInfo(sideListBuff[position].name, position)
            CpContractEntrustNewActivity.mContractId = sideListBuff[position].index
            contractId = sideListBuff[position].index
            tv_order_type.setText(CpClLogicContractSetting.getContractShowNameById(this@CpContractDetailActivity, contractId))
            val event = CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_contract_event)
            event.msg_content = CpContractEntrustNewActivity.mContractId
            CpEventBusUtil.post(event)
            img_order_type.animate().setDuration(200).rotation(0f).start()
            isShowContractSelect = false
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


    private fun initTabInfo() {
        mFragments = ArrayList()
        mFragments?.add(CpContractParaFragment.newInstance(contractId))
        mFragments?.add(CpCapitalRateFragment.newInstance(contractId))
        mFragments?.add(CpInsuranceFundFragment.newInstance(contractId))
        sub_tab_layout.setViewPager(vp_order, arrayOf(getString(R.string.cp_contract_info_text2), getString(R.string.cp_contract_info_text3), getString(R.string.cp_contract_info_text4)), this, mFragments)
        vp_order.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                ll_order_type.visibility = View.VISIBLE
                selPosition = position
                if (position == 2) {
                    tv_order_type.setText(mCurrMarginCoinInfo?.name)
                } else {
                    tv_order_type.text = CpClLogicContractSetting.getContractShowNameById(mActivity, contractId)
                }
            }
        })
    }


    override fun loadData() {
        super.loadData()
        contractId = intent.getIntExtra("contractId", -1)
        tv_order_type.text = CpClLogicContractSetting.getContractShowNameById(mActivity, contractId)

        val mContractMarginCoinListJsonStr = CpClLogicContractSetting.getContractMarginCoinListStr(mActivity)
        if (mContractMarginCoinListJsonStr != null && mContractMarginCoinListJsonStr.isNotEmpty()) {
            val jsonArray = JSONArray(mContractMarginCoinListJsonStr)
            for (i in 0 until jsonArray.length()) {
                val mJSONObject = jsonArray[i] as String
                contractList.add(CpTabInfo(mJSONObject, i))
            }
        } else {
            contractList.add(CpTabInfo("USDT", 0))
        }
        if (mCurrMarginCoinInfo == null) {
            for (buff in contractList) {
                if (buff.name.equals(CpClLogicContractSetting.getContractMarginCoinById(this, contractId))) {
                    mCurrMarginCoinInfo = buff
                }
            }
        }
    }

    private fun showSelectContractDialog() {
        contractDialog = CpNewDialogUtils.showNewBottomListDialog(mActivity, contractList, mCurrMarginCoinInfo!!.index, object : CpNewDialogUtils.DialogOnItemClickListener {
            override fun clickItem(index: Int) {
                contractDialog?.dismiss()
                contractDialog = null
                mCurrMarginCoinInfo = contractList[index]
                tv_order_type.setText(mCurrMarginCoinInfo?.name)
                val event = CpMessageEvent(CpMessageEvent.sl_contract_calc_switch_margin_coin_event)
                event.msg_content = mCurrMarginCoinInfo?.name
                CpEventBusUtil.post(event)
            }
        })
    }

    companion object {
        fun show(activity: Activity, contractId: Int = 0) {
            val intent = Intent(activity, CpContractDetailActivity::class.java)
            intent.putExtra("contractId", contractId)
            activity.startActivity(intent)
        }
    }


}