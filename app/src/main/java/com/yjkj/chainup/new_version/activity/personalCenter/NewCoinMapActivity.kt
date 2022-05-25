package com.yjkj.chainup.new_version.activity.personalCenter

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.LikeDataService
import com.yjkj.chainup.db.service.SearchDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.CoinMapAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import kotlinx.android.synthetic.main.activity_new_coin_map.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2020/11/3-10:40 AM
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.NewCoinMapActivity)
class NewCoinMapActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_new_coin_map


    var markList = ArrayList<JSONObject>()
    var searchHistroylist = ArrayList<JSONObject>()
    var likeList = ArrayList<JSONObject>()

    var adapter: CoinMapAdapter? = null

    @JvmField
    @Autowired(name = ParamConstant.TYPE)
    var type = ""
    var isSearch = false

    @JvmField
    @Autowired(name = ParamConstant.SEARCH_COIN_MAP_FOR_LEVER)
    var leverStatus = false

    @JvmField
    @Autowired(name = ParamConstant.SEARCH_COIN_MAP_FOR_LEVER_UNREFRESH)
    var refreshLever = false
    @JvmField
    @Autowired(name = ParamConstant.SEARCH_COIN_MAP_FOR_LEVER_INTO_TRANSFER)
    var intoTransfer = false


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        collapsing_toolbar?.run {
            setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
            collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
            collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
            collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
            collapsing_toolbar?.title = LanguageUtil.getString(this@NewCoinMapActivity, "title_select_account")
        }

        adapter = CoinMapAdapter()

        if (leverStatus) {
            getBalanceList()
            return
        } else {
            val tempMarket = NCoinManager.getMarketArray(false)
            if (null != tempMarket && tempMarket.length() > 0) {
                markList.addAll(JSONUtil.arrayToList(tempMarket))
            }

            val tempLikeList = LikeDataService.getInstance().getCollecData(false)
            if (null != tempLikeList && tempLikeList.size > 0) {
                likeList.addAll(tempLikeList)
            }
        }
        initViews()

    }

    /**
     * 获取杠杆账户列表
     */
    fun getBalanceList() {
        addDisposable(getMainModel().getBalanceList(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                val json = jsonObject.optJSONObject("data")
                val jsonLeverMap = json?.optJSONObject("leverMap")
                searchHistroylist.clear()
                searchHistroylist.addAll(NCoinManager.getLeverMapList(jsonLeverMap)
                        ?: arrayListOf())
                searchHistroylist.sortBy { it?.optInt("sort") }
                markList.clear()
                markList.addAll(searchHistroylist)
                initViews()
            }
        }))
    }


    fun initViews() {

        rv_coinmap.layoutManager = LinearLayoutManager(mActivity)
        adapter?.setList(searchHistroylist)
        /**
         * 跳转 -> 交易详情
         */
        adapter?.setOnItemClickListener { adapter, view, position ->

            var obj = adapter.data[position] as JSONObject
            var symbol = obj.optString("symbol")
            if (leverStatus) {
                if (refreshLever) {
                    if (intoTransfer) {
                        ArouterUtil.navigation(RoutePath.NewVersionTransferActivity, Bundle().apply {
                            putString(ParamConstant.TRANSFERSTATUS, ParamConstant.LEVER_INDEX)
                            putString(ParamConstant.TRANSFERSYMBOL, "")
                            putString(ParamConstant.TRANSFERCURRENCY, symbol)
                        })
                    } else {
                        ArouterUtil.navigation(RoutePath.NewVersionBorrowingActivity, Bundle().apply {
                            putString(ParamConstant.symbol, symbol)
                        })
                    }
                } else {
                    intent?.putExtra(ParamConstant.symbol, symbol)
                    setResult(Activity.RESULT_OK, intent)
                }

            } else {
                ArouterUtil.forwardKLine(symbol, ParamConstant.BIBI_INDEX)

                SearchDataService.getInstance().saveSearchData(symbol)

                SearchDataService.getInstance().removeLastSearchData()
            }

            finish()
        }
        adapter?.setEmptyView(EmptyForAdapterView(this))

        rv_coinmap?.adapter = adapter

        adapter?.setSearch(isSearch)
        adapter?.setLeverStatus(leverStatus)
    }


}