package com.yjkj.chainup.new_version.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
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
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.manager.NCoinManager
import com.yjkj.chainup.model.model.MainModel
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.like.view.SearchTopView
import com.yjkj.chainup.new_version.adapter.CoinMapAdapter
import com.yjkj.chainup.new_version.home.homeMarketRecommend
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.activity_coin_map.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject


/**
 * @date 2018-5-28
 * @description 搜索&添加币对
 *
 *
 * UI :https://lanhuapp.com/web/#/item/board/detail?pid=0ce4b503-1bb6-4195-a2e4-2a5bafc04f0e&project_id=0ce4b503-1bb6-4195-a2e4-2a5bafc04f0e&image_id=48dca8e8-1770-4e32-aeb1-ba89522e07bf
 */
@Route(path = RoutePath.CoinMapActivity)
class CoinMapActivity : NBaseActivity(), SearchTopView.SearchViewListener {
    override fun setContentView() = R.layout.activity_coin_map

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

    lateinit var searchView: SearchTopView

    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        isSearch = (type == ParamConstant.SEARCH_COIN_MAP)
        initCoins()
        adapter = CoinMapAdapter()
        tv_cancel?.text = LanguageUtil.getString(this, "common_text_btnCancel")
        et_search?.hint = LanguageUtil.getString(this, "assets_action_search")
        searchView = SearchTopView(this)
        searchView.searchViewListener = this
        adapter?.headerWithEmptyEnable = true
        initOnClickListener()

        if (leverStatus) {
            getBalanceList()
            return
        } else {
            adapter?.addHeaderView(searchView)
            val tempMarket = NCoinManager.getMarketArray(false)
            if (null != tempMarket && tempMarket.length() > 0) {
                markList.addAll(JSONUtil.arrayToList(tempMarket))
                markList.sortBy { it.optInt("sort") }
            }

            val tempLikeList = LikeDataService.getInstance().getCollecData(false)
            if (null != tempLikeList && tempLikeList.size > 0) {
                likeList.addAll(tempLikeList)
            }

            val tempHistoryList = SearchDataService.getInstance().searchData
            val isNull = tempHistoryList != null && tempHistoryList.length() != 0
            searchView.initItems(isNull)
            if (isNull) {
                val temp = getLikeData(JSONUtil.arrayToList(tempHistoryList), 5)
                if (null != temp) {
                    searchHistroylist.addAll(temp.reversed())
                }
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


    private fun initOnClickListener() {
        /**
         * 添加 隐藏软键盘
         */
        tv_cancel?.setOnClickListener {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(et_search.windowToken, 0)
            finish()
        }
    }


    fun initViews() {
        rv_coinmap.layoutManager = LinearLayoutManager(mActivity)
        adapter?.setList(searchHistroylist)
        /**
         * 添加操作
         *
         */
        adapter?.addChildClickViewIds(R.id.ib_add)
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
                finish()
            } else {
                ArouterUtil.forwardKLine(symbol, ParamConstant.BIBI_INDEX)

                SearchDataService.getInstance().saveSearchData(symbol)

                SearchDataService.getInstance().removeLastSearchData()
            }

        }

        /**
         * 点击添加自选
         */
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            var obj = adapter.data[position] as JSONObject

            val symbol = obj.optString("symbol")
            val isAdd = obj.optBoolean("isAdd")
            var hasAdd = isAdd
            hasAdd = if (hasAdd) {
                LikeDataService.getInstance().removeCollect(symbol)
                false
            } else {
                LikeDataService.getInstance().saveCollecData(symbol, null)
                true
            }
            if (!LoginManager.isLogin(this)) {
                if (hasAdd) {
                    NToastUtil.showTopToast(true, LanguageUtil.getString(this@CoinMapActivity, "kline_tip_addCollectionSuccess"))
                } else {
                    NToastUtil.showTopToast(true, LanguageUtil.getString(this@CoinMapActivity, "kline_tip_removeCollectionSuccess"))
                }
            }
            addOrDeleteSymbol(if (hasAdd) 1 else 2, symbol)

            try {
                obj.put("isAdd", hasAdd)
                adapter.notifyItemChanged((adapter as CoinMapAdapter).getCurrentPosition(position))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
//        adapter?.setHeaderAndEmpty(true)


        rv_coinmap?.adapter = adapter

        adapter?.setSearch(isSearch)
        adapter?.setLeverStatus(leverStatus)

        /**
         * 监听搜索编辑框
         */
        et_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var resultList = getSearchMatchList(s.toString())
                if (!leverStatus) {
                    resultList = getLikeData(resultList, 0)
                }
                if (TextUtils.isEmpty(s.toString())) {
                    adapter?.setList(searchHistroylist)
                } else {
                    if (null == resultList || resultList.size <= 0) {
                        adapter?.setList(null)
                        adapter?.setEmptyView(EmptyForAdapterView(this@CoinMapActivity))
                    } else {
                        adapter?.setList(resultList)
                    }
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    /*
     * 本地搜索
     */
    private fun getSearchMatchList(input: String?): ArrayList<JSONObject>? {

        if (null == input || input.trim().isEmpty() || markList.size <= 0) {
            return null
        }

        val temp = ArrayList<JSONObject>()
        markList.forEach {
            var name = NCoinManager.showAnoterName(it)
            if (null != name) {
                if (name.contains(input.toUpperCase()) || name.contains(input.toLowerCase()) || name.contains(input)) {
                    temp.add(it)
                }
            }
        }
        return if (leverStatus) {
            temp
        } else {
            getLikeData(temp, 0)
        }

    }

    /*
     * 判断是否存在于自选数据中,返回一个newList
     *
     * @Param  count = 5只取前5条
     */
    private fun getLikeData(list: ArrayList<JSONObject>?, count: Int): ArrayList<JSONObject>? {
        if (null == list || list.size <= 0)
            return list

        val tempList = ArrayList<JSONObject>()

        for (i in 0 until list.size) {
            if (count > 0 && i == count) {
                return tempList
            }
            val obj = list[i]
            for (v in likeList) {
                if (obj.optString("symbol").equals(v.optString("symbol"), true)) {
                    obj.put("isAdd", true)
                }
            }
            tempList.add(obj)
        }
        return tempList
    }

    /**
     * 添加或者删除自选数据
     * @param operationType 标识 0(批量添加)/1(单个添加)/2(单个删除)
     * @param symbol 单个币对名称
     */
    private fun addOrDeleteSymbol(operationType: Int = 0, symbol: String?) {

        if (null == symbol || !LoginManager.isLogin(this))
            return
        var list = ArrayList<String>()
        list.add(symbol)
        addDisposable(getMainModel().addOrDeleteSymbol(operationType, list,"BTC", object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                if (operationType == 1) {
                    NToastUtil.showTopToastNet(this@CoinMapActivity, true, LanguageUtil.getString(this@CoinMapActivity, "kline_tip_addCollectionSuccess"))
                    LikeDataService.getInstance().saveCollecData(symbol, null)
                } else {
                    NToastUtil.showTopToastNet(this@CoinMapActivity, true, LanguageUtil.getString(this@CoinMapActivity, "kline_tip_removeCollectionSuccess"))
                    LikeDataService.getInstance().removeCollect(symbol)
                }

            }
        }))
    }

    override fun clearSearch() {
        SearchDataService.getInstance().removeSearchData()
        searchHistroylist.clear()
        adapter?.setNewData(searchHistroylist)
    }

    override fun hotItemClick(name: String) {
        et_search.setText(name)
        et_search.setSelection(name.length)
    }

    /**
     * public_info_v4  接口数据
     */
    private fun initCoins() {
        MainModel().getCommonRecommendCoin(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                jsonObject.optJSONObject("data").apply {
                    val data = getString("recommendCoin")
                    searchView.initTopView(data)
                    if (!leverStatus) {
                        if (data.isNotEmpty())
                            homeMarketRecommend(this@CoinMapActivity, searchView.getItemView())
                    }
                }
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.symbol_switch_type == event.msg_type) {
            finish()
        }
    }


}
