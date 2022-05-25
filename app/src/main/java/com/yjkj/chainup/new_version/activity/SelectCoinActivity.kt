package com.yjkj.chainup.new_version.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant.*
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.manager.SymbolManager
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.asset.DepositActivity
import com.yjkj.chainup.new_version.activity.asset.WithdrawActivity
import com.yjkj.chainup.new_version.adapter.SelectCoinAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.wedegit.SectionDecoration
import kotlinx.android.synthetic.main.activity_search_coin.*
import org.json.JSONObject

/**
 * @description 新版的切换币种
 * @author Bertking
 * @date 2019-4-16
 *
 * 主要是进行  选择&搜素币种 not the CoinMap(币对)
 * 入口：
 *  1. 资金流水；
 *  2. 充值,的选择币种;
 *  3. 提现,的选择币种;
 */
@Route(path = RoutePath.SelectCoinActivity)
class SelectCoinActivity : NBaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_search_coin
    }

    var coinList = arrayListOf<String>()
    var rechargeList: ArrayList<JSONObject> = arrayListOf()
    var selectList: ArrayList<JSONObject> = arrayListOf()

    var mDataList: ArrayList<String> = arrayListOf()
    private lateinit var mSelectCoinAdapter: SelectCoinAdapter


    private lateinit var mHotCoinAdapter: HotCoinAdapter

    @JvmField
    @Autowired(name = ASSET_ACCOUNT_TYPE)
    var assetType = BIBI_ACCOUNT

    @JvmField
    @Autowired(name = OPTION_TYPE)
    var coinType = RECHARGE

    /**
     * 是否需要跳转
     */
    @JvmField
    @Autowired(name = COIN_FROM)
    var needSkip = false


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        tv_cancel?.setOnClickListener { finish() }
        getAccountBalance()
        et_search?.hint = LanguageUtil.getString(this, "assets_action_search")
        tv_cancel?.text = LanguageUtil.getString(this, "common_text_btnCancel")

        mSelectCoinAdapter = SelectCoinAdapter(rechargeList)
    }

    fun setClickListener(allCoinMap: JSONObject?) {
        if (allCoinMap == null) return
        val coinList: Iterator<String> = allCoinMap.keys()
        when (coinType) {
            RECHARGE -> {
                while (coinList.hasNext()) {
                    val jsonObject = allCoinMap.optJSONObject(coinList.next())
                    if (jsonObject?.optInt("depositOpen") == 1) {
                        rechargeList.add(jsonObject)
                    }
                }

                coinList.forEach {

                }
            }
            WITHDRAW -> {
                while (coinList.hasNext()) {
                    val jsonObject = allCoinMap.optJSONObject(coinList.next())
                    if (jsonObject?.optInt("withdrawOpen") == 1) {
                        rechargeList.add(jsonObject)
                    }
                }
            }
            INNEROPEN -> {
                while (coinList.hasNext()) {
                    val jsonObject = allCoinMap.optJSONObject(coinList.next())
                    if (jsonObject?.optInt("withdrawOpen") == 1 && jsonObject?.optInt("innerTransferOpen") == 1) {
                        rechargeList.add(jsonObject)
                    }
                }
            }
        }
        rechargeList.sortBy { it.optString("coinName") }
        selectList.clear()
        selectList.addAll(rechargeList)


        sb_coin?.setOnTouchingLetterChangedListener { s ->
            for (i in 0 until rechargeList.size) {
                if (rechargeList[i].optString("coinName")?.substring(0, 1)!!.toUpperCase().equals(s, true)) {
                    rv_coin?.scrollToPosition(i)
                    break
                }
            }
        }
        var selectCoin = arrayListOf<String>()
        for (i in 0 until rechargeList.size) {
            if (!selectCoin.contains(rechargeList[i].optString("coinName")?.substring(0, 1)!!.toUpperCase())) {
                selectCoin.add(rechargeList[i].optString("coinName")?.substring(0, 1)!!.toUpperCase())
            }
        }
        runOnUiThread {
            sb_coin?.updateCoin(selectCoin, this)
            sb_coin?.invalidate()
        }

        rv_coin?.layoutManager = LinearLayoutManager(this)
        mSelectCoinAdapter.setBean(rechargeList)
        mSelectCoinAdapter.setEmptyView(EmptyForAdapterView(this))
        rv_coin.adapter = mSelectCoinAdapter
        mSelectCoinAdapter.setOnItemClickListener { adapter, view, position ->
            val coin = selectList[position].optString("coinName")
            if (needSkip) {
                when (coinType) {
                    WITHDRAW -> {
                        Log.e("selectList[position]", "${selectList[position]}")
                        var symbol = SymbolManager.instance.getFundCoinByName(coin)
                        if (assetType == B2C_ACCOUNT) {
                            PublicInfoDataService.getInstance().saveCoinInfo4B2C(coin)
                            ArouterUtil.navigation(RoutePath.B2CWithdrawActivity, null)
                        } else {
                            WithdrawActivity.enter2(this, symbol.toString())
                        }
                    }
                    RECHARGE -> {
                        if (assetType == B2C_ACCOUNT) {
                            PublicInfoDataService.getInstance().saveCoinInfo4B2C(coin)
                            ArouterUtil.navigation(RoutePath.B2CRechargeActivity, null)
                        } else {
                            DepositActivity.enter2(this, coin)
                        }
                    }
                }
                finish()
            } else {
                val intent = Intent()
                intent.putExtra(CoinActivity.SELECTED_COIN, coin)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

        }

        mSelectCoinAdapter.listener = object : SelectCoinAdapter.FilterListener {
            override fun getFilterData(list: ArrayList<JSONObject>) {
                selectList.clear()
                selectList.addAll(list)
                mSelectCoinAdapter.setList(list)
            }

        }
        rv_coin?.addItemDecoration(SectionDecoration(this, object : SectionDecoration.DecorationCallback {
            override fun getGroupFirstLine(position: Int): String {
                if (selectList.size <= position) {
                    return ""
                } else {
                    return selectList[position].optString("coinName")?.substring(0, 1)!!.toUpperCase()
                }
            }

            override fun getGroupId(position: Int): Long {
                if (selectList.size == 0 || selectList.size <= position) {
                    return 0
                }
                return Character.toUpperCase(selectList[position].optString("coinName")?.substring(0, 1)!!.toUpperCase()[0]).toLong()
            }

        }))

        sb_coin?.visibility = View.VISIBLE


        et_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mSelectCoinAdapter.filter.filter(s)
            }

        })
    }


    /**
     * 获取账户信息 币币
     */
    private fun getAccountBalance() {
        addDisposable(getMainModel().accountBalance(object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")
                SymbolManager.instance.saveFundCoins(json)
                setClickListener(json.optJSONObject("allCoinMap"))
            }
        }))

        addDisposable(getMainModel().getHotcoin(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var data = jsonObject.optJSONObject("data") ?: return
                var hotCoin = data?.optString("hotCoin")
                if (!TextUtils.isEmpty(hotCoin)) {
                    val split: Array<String> = hotCoin.split(",").toTypedArray()
                    for (coin in split) {
                        mDataList.add(coin)
                    }
                    setHotView()
                }else{
                    tv_hot_coin?.visibility = View.GONE
                }
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                Log.e("getAccountBalance", "msg:$msg")
            }

        }))
    }


    fun setHotView() {
        if (mDataList != null && mDataList.size > 0) {
            tv_hot_coin?.visibility = View.VISIBLE
        }else{
            tv_hot_coin?.visibility = View.GONE
        }
        mHotCoinAdapter = HotCoinAdapter(R.layout.item_hot_coin, mDataList)

        rv_hot_coin?.layoutManager = LinearLayoutManager(this@SelectCoinActivity, LinearLayoutManager.HORIZONTAL, false)
        rv_hot_coin?.layoutManager = GridLayoutManager(this@SelectCoinActivity, 3)
        rv_hot_coin?.adapter = mHotCoinAdapter
        rv_hot_coin?.isNestedScrollingEnabled = false
        rv_hot_coin?.setHasFixedSize(true)
        mHotCoinAdapter?.setOnItemClickListener { adapter, view, position ->
            var coin = mDataList[position]
            if (needSkip) {
                when (coinType) {
                    WITHDRAW -> {
                        Log.e("selectList[position]", "${selectList[position]}")
                        var symbol = SymbolManager.instance.getFundCoinByName(coin)
                        if (assetType == B2C_ACCOUNT) {
                            PublicInfoDataService.getInstance().saveCoinInfo4B2C(coin)
                            ArouterUtil.navigation(RoutePath.B2CWithdrawActivity, null)
                        } else {
                            WithdrawActivity.enter2(this, symbol.toString())
                        }
                    }
                    RECHARGE -> {
                        if (assetType == B2C_ACCOUNT) {
                            PublicInfoDataService.getInstance().saveCoinInfo4B2C(coin)
                            ArouterUtil.navigation(RoutePath.B2CRechargeActivity, null)
                        } else {
                            DepositActivity.enter2(this, coin)
                        }
                    }
                }
                finish()
            } else {
                val intent = Intent()
                intent.putExtra(CoinActivity.SELECTED_COIN, coin)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

        }
    }

    class HotCoinAdapter(layoutResId: Int, data: MutableList<String>) :
            BaseQuickAdapter<String, BaseViewHolder>(layoutResId, data) {
        override fun convert(helper: BaseViewHolder, item: String) {
            helper?.setText(R.id.tv_parent_content, NCoinManager.getShowMarket(item))
        }
    }
}