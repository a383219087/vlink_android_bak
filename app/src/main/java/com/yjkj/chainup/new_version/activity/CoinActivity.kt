package com.yjkj.chainup.new_version.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractAccount
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.bean.coin.CoinBean
import com.yjkj.chainup.contract.uilogic.LogicContractSetting
import com.yjkj.chainup.contract.utils.PreferenceManager
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.wedegit.SectionDecoration
import kotlinx.android.synthetic.main.activity_coin.*
import org.json.JSONArray

/**
 * @description 币种
 * @author Bertking
 * @date 2018-6-7
 *
 * 主要是进行  选择&搜素币种 not the CoinMap(币对)
 * 入口：
 *  1. 资金流水；
 *  2. 充值,的选择币种;
 *  3. 提现,的选择币种;
 *
 */
class CoinActivity : NewBaseActivity() {

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
            var intent = Intent(context, CoinActivity::class.java)
            intent.putExtra(SELECTED_COIN, selectedCoin)
            intent.putExtra(HAS_ALL, needHasAll)
            intent.putExtra(SELECTED_STATUS, cashStatus)
            intent.putExtra(SELECTED_ID, position)
            intent.putExtra(SELECTED_TYPE, type)
            (context as Activity).startActivityForResult(intent, COIN_REQUEST_CODE)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_coin)

        tv_cancel?.setOnClickListener { finish() }

        tv_cancel?.text = LanguageUtil.getString(this, "common_text_btnCancel")
        et_search?.hint = LanguageUtil.getString(this, "common_action_searchCoinPair")

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
                et_search.hint = LanguageUtil.getString(this, "common_action_searchCoinPair")
                coinList.add(0, coinBean)
                coinBean.isSelected = TextUtils.isEmpty(selectedCoin)
            }
        } else {
            when (coinType) {
                OTC_TYPE -> {
                    coinList = DataManager.getCoinsFromDB(true)
                }
                OTC_CONTRACT -> {
//                    val coinBean = CoinBean(0, "0", "BTC", "", 0, true, 0, 0, "", "BTC")
//                    coinList = arrayListOf(coinBean)
                    if (AppConstant.IS_NEW_CONTRACT) {
                        val mContractMarginCoinListJsonStr = LogicContractSetting.getContractMarginCoinListStr(this)
                        if (mContractMarginCoinListJsonStr != null && mContractMarginCoinListJsonStr.isNotEmpty()) {
                            val jsonArray = JSONArray(mContractMarginCoinListJsonStr)
                            for (i in 0 until jsonArray.length()) {
                                val codeName = jsonArray[i] as String
                                val coinBean = CoinBean(0, "0", codeName, "", 0, false, 0, 0, "", codeName)
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

        sb_coin?.setOnTouchingLetterChangedListener { s ->
            for (i in 0 until coinList.size) {
                if (coinList[i].getStickItem().equals(s, true)) {
                    rv_coin?.scrollToPosition(i)
                    break
                }
            }
        }
        initViews()
    }

    fun initViews() {
        rv_coin?.layoutManager = LinearLayoutManager(context)
        val adapter = SelectCoinAdapter(coinList, selectPosition)
        adapter.setEmptyView(EmptyForAdapterView(context))
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
        rv_coin?.addItemDecoration(SectionDecoration(this, object : SectionDecoration.DecorationCallback {
            override fun getGroupFirstLine(position: Int): String {
                if (position >= coinList.size) {
                    return ""
                }
                var stick = coinList[position].getStickItem()
                if (StringUtil.checkStr(stick)) {
                    return stick.substring(0, 1).toLowerCase()
                }
                return ""
            }

            override fun getGroupId(position: Int): Long {
                if (coinList.isNotEmpty() && coinList.size > position) {
                    return Character.toUpperCase(coinList[position].getStickItem()[0]).toLong()
                } else {
                    return 0
                }
            }

        }))


        /**
         * 监听搜索编辑框
         */
        et_search?.setSearch()
        et_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 如果adapter不为空的话就根据编辑框中的内容来过滤数据
                Log.d(TAG, "=========" + (adapter == null) + (adapter.filter == null))
                adapter.filter?.filter(s)
            }

        })

    }


}
