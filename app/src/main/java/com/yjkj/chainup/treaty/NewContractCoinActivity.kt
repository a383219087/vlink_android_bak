package com.yjkj.chainup.treaty

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.coin.CoinBean
import com.yjkj.chainup.manager.DataManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.treaty.adapter.ChooesCoinadapter
import kotlinx.android.synthetic.main.activity_change_coin_list.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019/1/17-8:45 PM
 * @Email buptjinlong@163.com
 * @description
 */
const val NEW_CONTRACT_COIN = 1125

class NewContractCoinActivity : NewBaseActivity() {


    var selectedCoin = ""
    var coinlist = ArrayList<CoinBean>()

    companion object {

        const val CHOOSE_COIN = "choose_coin"
        const val SELECTED_COIN = "selected_coin"
        fun enter2(context: Context, chooseCoin: String) {
            var intent = Intent(context, NewContractCoinActivity::class.java)
            intent.putExtra(CHOOSE_COIN, chooseCoin)
            (context as Activity).startActivityForResult(intent, NEW_CONTRACT_COIN)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_coin_list)
        initData()
        coinlist = DataManager.getCoinsFromDB(true)
        coinlist.forEach {
            if (it.name == selectedCoin) {
                it.isSelected = true
            }
        }
        initView()
    }

    fun initData() {
        if (intent != null) {
            selectedCoin = intent.getStringExtra(CHOOSE_COIN) ?: ""
        }
    }

    fun initView() {
        var adapter = ChooesCoinadapter(coinlist)
        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter.setEmptyView(EmptyForAdapterView(this))
        recycler_view.adapter = adapter

        adapter.setOnItemClickListener { _, view, position ->
            val intent = Intent()
            intent.putExtra(SELECTED_COIN, coinlist[position].name)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }
}