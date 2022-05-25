package com.yjkj.chainup.new_version.activity.asset

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractAccount
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.coin.CoinBean
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.new_version.activity.SelectCoinAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.iterator
import kotlinx.android.synthetic.main.activity_new_coin.*
import org.json.JSONArray

/**
 * @Author lianshangljl
 * @Date 2020/11/3-10:17 AM
 * @Email buptjinlong@163.com
 * @description
 */
class NewCoinActivity : NBaseActivity() {

    var coinList = ArrayList<CoinBean>()

    var coinType = ""
    private lateinit var selectedCoin: String
    var cashStatus = false
    private var needHasAll: Boolean = false
    var selectPosition = 0

    companion object {
        const val SELECTED_COIN = "selected_coin"
        const val SELECTED_ID = "selected_id"
        const val SELECTED_STATUS = "selected_status"
        const val SELECTED_TYPE = "SELECTED_TYPE"
        const val HAS_ALL = "has_all"
        const val OTC_TYPE = "OTC_TYPE"
        const val OTC_CONTRACT = "OTC_CONTRACT"
        const val COIN_REQUEST_CODE = 2018

        /**
         * @param type 币种显示取值  otc  取币对中otcOpen ==1的  contract 目前只有btc
         */
        fun enter4Result(context: Context, selectedCoin: String, needHasAll: Boolean, position: Int, cashStatus: Boolean = false, type: String = "") {
            var intent = Intent(context, NewCoinActivity::class.java)
            intent.putExtra(SELECTED_COIN, selectedCoin)
            intent.putExtra(HAS_ALL, needHasAll)
            intent.putExtra(SELECTED_STATUS, cashStatus)
            intent.putExtra(SELECTED_ID, position)
            intent.putExtra(SELECTED_TYPE, type)
            (context as Activity).startActivityForResult(intent, COIN_REQUEST_CODE)
        }
    }


    override fun setContentView() = R.layout.activity_new_coin


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.run {
            setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
            collapsing_toolbar?.title = LanguageUtil.getString(this@NewCoinActivity, "b2c_text_changecoin")
        }


        selectedCoin = intent.getStringExtra(SELECTED_COIN) ?: ""
        cashStatus = intent.getBooleanExtra(SELECTED_STATUS, false)
        needHasAll = intent.getBooleanExtra(HAS_ALL, false)
        selectPosition = intent.getIntExtra(SELECTED_ID, selectPosition)
        coinType = intent.getStringExtra(SELECTED_TYPE) ?: ""


        if (coinType.isEmpty()) {
            val tempList = DataManager.getCoinsFromDB()
            coinList.clear()
            coinList.addAll(tempList)

            if (needHasAll) {
                val coinBean = CoinBean(0, "0", LanguageUtil.getString(this, "select_all_coin"), "", 0, false, 0, 0, "", LanguageUtil.getString(this, "select_all_coin"))
                coinList.add(0, coinBean)
                coinBean.isSelected = TextUtils.isEmpty(selectedCoin)
            }
        } else {
            when (coinType) {
                OTC_TYPE -> {
                    coinList = DataManager.getCoinsFromDB(true)
                }
                OTC_CONTRACT -> {
                    if (AppConstant.IS_NEW_CONTRACT) {
                        val coinJsonStr = PreferenceManager.getInstance(this).getSharedString("contract#bibi#coin", "")
                        val mContractMarginCoinListJsonStr = LogicContractSetting.getContractMarginCoinListStr(this)
                        if (mContractMarginCoinListJsonStr != null && mContractMarginCoinListJsonStr.isNotEmpty()) {
                            val tempArray = tempCoin(coinJsonStr, mContractMarginCoinListJsonStr)
                            for (element in tempArray) {
                                val coinBean = CoinBean(0, "0", element, "", 0, false, 0, 0, "", element)
                                coinList.add(coinBean)
                            }
                        }
                    } else {
                        //合约划转选择币种，币种列表 来自合约接口
                        val coinJsonStr = PreferenceManager.getInstance(this).getSharedString("contract#bibi#coin", "")
                        val contractAccounts: List<ContractAccount> = ContractUserDataAgent.getContractAccounts().filter { !ContractPublicDataAgent.isVirtualCoin(it.coin_code) }
                        contractAccounts.forEach { item ->
                            if (coinJsonStr.isNotEmpty()) {
                                if (coinJsonStr.contains(item.coin_code)) {
                                    val coinBean = CoinBean(0, "0", item.coin_code, "", 0, false, 0, 0, "", item.coin_code)
                                    coinList.add(coinBean)
                                }
                            } else {
                                val coinBean = CoinBean(0, "0", item.coin_code, "", 0, false, 0, 0, "", item.coin_code)
                                coinList.add(coinBean)
                            }
                        }
                    }
                }
            }
        }

        coinList.forEach {
            if (it.name == selectedCoin) {
                it.isSelected = true
            }
        }
        coinList.sortBy { it.name }

        initViews()
    }

    fun initViews() {
        rv_coin?.layoutManager = LinearLayoutManager(this)
        val adapter = SelectCoinAdapter(coinList, selectPosition)
        adapter.setEmptyView(EmptyForAdapterView(this))
        rv_coin?.adapter = adapter
        adapter.setOnItemClickListener { adapter, view, position ->
            val intent = Intent()
            if (cashStatus) {
                if (position == 0) {
                    intent.putExtra(SELECTED_COIN, "")
                } else {
                    intent.putExtra(SELECTED_COIN, coinList[position].name)
                }
            } else {
                intent.putExtra(SELECTED_COIN, coinList[position].name)
            }

            intent.putExtra(SELECTED_ID, position)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }


        adapter.setListener { list ->
            /**
             *在这里拿到过滤后的数据
             */
            /**
             *在这里拿到过滤后的数据
             */
            Log.d(TAG, "======过滤后的数据：====" + list?.size)
            coinList.clear()
            coinList.addAll(list ?: arrayListOf())
            adapter.setList(list)
        }
//        rv_coin?.addItemDecoration(SectionDecoration(this, object : SectionDecoration.DecorationCallback {
//            override fun getGroupFirstLine(position: Int): String {
//                if (position >= coinList.size) {
//                    return ""
//                }
//                var stick = coinList[position].getStickItem()
//                if (StringUtil.checkStr(stick)) {
//                    return stick.substring(0, 1).toLowerCase()
//                }
//                return ""
//            }
//
//            override fun getGroupId(position: Int): Long {
//                if (coinList.isNotEmpty() && coinList.size > position) {
//                    return Character.toUpperCase(coinList[position].getStickItem()[0]).toLong()
//                } else {
//                    return 0
//                }
//            }
//
//        }))

    }

    private fun tempCoin(coinJsonStr: String, mContractMarginCoinListJsonStr: String): Set<String> {
        if(coinJsonStr.isEmpty()){
            return linkedSetOf()
        }
        val coinListTemp = JSONArray(coinJsonStr)
        val coinListCTemp = JSONArray(mContractMarginCoinListJsonStr)

        val coinList = arrayListOf<String>()
        for (item in coinListTemp.iterator()) {
            coinList.add(item)
        }
        val coinListContract = arrayListOf<String>()
        for (item in coinListCTemp.iterator()) {
            coinListContract.add(item)
        }
        return coinList intersect coinListContract
    }

}