package com.yjkj.chainup.new_version.activity.asset

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jaeger.library.StatusBarUtil
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.app.AppConstant
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.address.AddressBean
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.*
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.activity.CoinActivity
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.SelectCoinActivity
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.new_version.adapter.OTCFundAdapter
import com.yjkj.chainup.new_version.adapter.SelectCoinAdapter
import com.yjkj.chainup.new_version.adapter.WithdrawAddressAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.BigDecimalUtils
import kotlinx.android.synthetic.main.activity_search_coin.et_search
import kotlinx.android.synthetic.main.activity_search_coin.rv_coin
import kotlinx.android.synthetic.main.activity_search_coin.tv_cancel
import kotlinx.android.synthetic.main.activity_withdraw_select_coin.*
import org.json.JSONObject


/**
 * @description 提现选择币种
 */
@Route(path = RoutePath.WithdrawSelectCoinActivity)
class WithdrawSelectCoinActivity : NBaseActivity() {

    override fun setContentView(): Int {
        return R.layout.activity_withdraw_select_coin
    }

    private lateinit var mDataList: ArrayList<JSONObject>
    private lateinit var mAllCoinList: ArrayList<JSONObject>
    private lateinit var mWithdrawSelectCoinAdapter: WithdrawSelectCoinAdapter


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        initViews()
    }

    /**
     * 是否需要跳转
     */
    @JvmField
    @Autowired(name = ParamConstant.COIN_FROM)
    var needSkip = false


    private fun initViews() {
        mDataList = ArrayList()
        mAllCoinList = ArrayList()
        mWithdrawSelectCoinAdapter = WithdrawSelectCoinAdapter(R.layout.item_withdraw_select_coin, mDataList, mAllCoinList)
        rv_coin.apply {
            layoutManager = LinearLayoutManager(this@WithdrawSelectCoinActivity)
            adapter = mWithdrawSelectCoinAdapter
        }
        mWithdrawSelectCoinAdapter?.setEmptyView(EmptyForAdapterView(this@WithdrawSelectCoinActivity
                ?: return))
        mWithdrawSelectCoinAdapter.setOnItemClickListener { adapter, view, position ->
            val coinName = mWithdrawSelectCoinAdapter.data[position].optString("coinName")
            if (needSkip) {
                var symbol = SymbolManager.instance.getFundCoinByName(coinName)
                WithdrawActivity.enter2(this, symbol.toString())
                finish()
            } else {
                val intent = Intent()
                intent.putExtra(CoinActivity.SELECTED_COIN, coinName)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s)) {
                    mDataList.clear()
                    for (buff in mAllCoinList) {
                        if (BigDecimalUtils.greaterThan(buff?.optString("normal_balance"), "0")) {
                            mDataList.add(buff)
                        }
                    }
                    mWithdrawSelectCoinAdapter.notifyDataSetChanged()
                } else {
                    mWithdrawSelectCoinAdapter.filter.filter(s.toString())
                }
            }

        })
        getAccountBalance()
        tv_cancel.setOnClickListener { finish() }
    }


    private fun getAccountBalance() {
        addDisposable(getMainModel().accountBalance(object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                SymbolManager.instance.saveFundCoins(json)
                val allCoinMap: JSONObject = json.optJSONObject("allCoinMap")
                val coinList: Iterator<String> = allCoinMap.keys()
                mDataList.clear()
                mAllCoinList.clear()
                while (coinList.hasNext()) {
                    val jsonObject = allCoinMap.optJSONObject(coinList.next())
                    mAllCoinList.add(jsonObject)
                    if (BigDecimalUtils.greaterThan(jsonObject?.optString("normal_balance"), "0")) {
                        mDataList.add(jsonObject)
                    }
                }
                mWithdrawSelectCoinAdapter.notifyDataSetChanged()
            }
        }))

    }


    class WithdrawSelectCoinAdapter(layoutResId: Int, var datas: ArrayList<JSONObject>, var allCoin: ArrayList<JSONObject>) :
            BaseQuickAdapter<JSONObject, BaseViewHolder>(layoutResId, datas), Filterable {
        override fun convert(helper: BaseViewHolder, item: JSONObject) {
            helper.setText(R.id.tv_coin_name, NCoinManager.getShowMarket(item?.optString("coinName")
                    ?: ""))
            helper.setText(R.id.tv_4th_value, RateManager.getCNYByCoinName("BTC", item?.optString("allBtcValuatin")
                    ?: "0", isOnlyResult = false))
            helper.setText(R.id.tv_coin_balance, BigDecimalUtils.divForDown(item?.optString("normal_balance"), NCoinManager.getCoinShowPrecision(item?.optString("coinName")
                    ?: "")).toPlainString())
        }

        private var filter: MyFilter? = null

        override fun getFilter(): Filter {
            if (filter == null) {
                filter = MyFilter(allCoin)
            }
            return filter ?: MyFilter(allCoin)
        }

        internal inner class MyFilter(originalData: ArrayList<JSONObject>) : Filter() {
            private var originalData = java.util.ArrayList<JSONObject>()

            init {
                this.originalData = originalData
            }

            override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
                val results = Filter.FilterResults()
                if (TextUtils.isEmpty(constraint)) {
                    results.values = originalData
                    results.count = originalData.size
                } else {
                    val filteredList = java.util.ArrayList<JSONObject>()
                    for (s in originalData) {
                        if (NCoinManager.getShowMarket(s?.optString("coinName")).toLowerCase().contains(constraint.toString().trim { it <= ' ' }.toLowerCase())) {
                            filteredList.add(s)
                        }
                    }
                    results.values = filteredList
                    results.count = filteredList.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
                if (results.values != null) {
                    data.clear()
                    data.addAll(results.values as ArrayList<JSONObject>)
                }
                notifyDataSetChanged()
            }
        }

    }
}
