package com.yjkj.chainup.new_version.activity.like

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.chainup.contract.utils.CpClLogicContractSetting
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.HeYueLikeDataService
import com.yjkj.chainup.db.service.LikeDataService
import com.yjkj.chainup.db.service.SearchDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.net_new.rxjava.CpNDisposableObserver
import com.yjkj.chainup.new_version.activity.like.view.SearchTopView
import com.yjkj.chainup.new_version.adapter.HeYueMapAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.activity_map_heyue.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/**
 * @date 2022-5-28
 * @description 合约搜索
 *
 */
@Route(path = RoutePath.HeYueMapActivity)
class HeYueMapActivity : NBaseActivity(), SearchTopView.SearchViewListener {
    override fun setContentView() = R.layout.activity_map_heyue

    var markList = ArrayList<JSONObject>()
    var searchHistroylist = ArrayList<JSONObject>()
    var likeList = ArrayList<JSONObject>()

    var adapter: HeYueMapAdapter? = null


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        ArouterUtil.inject(this)
        adapter = HeYueMapAdapter()
        tv_cancel?.text = LanguageUtil.getString(this, "common_text_btnCancel")
        et_search?.hint = LanguageUtil.getString(this, "assets_action_search")

        adapter?.headerWithEmptyEnable = true
        initOnClickListener()

        var json = CpClLogicContractSetting.getContractJsonListStr(this); //有可能json没有数据
        if (!TextUtils.isEmpty(json)) {
            val tempMarket = JSONArray(json)
            if (tempMarket.length() > 0) {
                markList.addAll(JSONUtil.arrayToList(tempMarket))
                markList.sortBy { it.optInt("sort") }
            }
            val tempLikeList = HeYueLikeDataService.getInstance().getCollecData(false)
            if (null != tempLikeList && tempLikeList.size > 0) {
                likeList.addAll(tempLikeList)
            }
            initViews()
        } else {
            addDisposable(
                getContractModel().getPublicInfo(
                    consumer = object : CpNDisposableObserver(mActivity, true) {
                        override fun onResponseSuccess(jsonObject: JSONObject) {
                            jsonObject.optJSONObject("data").run {
                                var contractList = optJSONArray("contractList")
                                if (contractList != null) {
                                    CpClLogicContractSetting.setContractJsonListStr(
                                        mActivity,
                                        contractList.toString()
                                    )
                                }
                                json = contractList.toString()
                            }
                            if (!TextUtils.isEmpty(json)) {
                                val tempMarket = JSONArray(json)
                                if (tempMarket.length() > 0) {
                                    markList.addAll(JSONUtil.arrayToList(tempMarket))
                                    markList.sortBy { it.optInt("sort") }
                                }
                                val tempLikeList =
                                    HeYueLikeDataService.getInstance().getCollecData(false)
                                if (null != tempLikeList && tempLikeList.size > 0) {
                                    likeList.addAll(tempLikeList)
                                }
                                initViews()
                            }
                        }
                    })
            )
        }
    }


    private fun initOnClickListener() {
        /**
         * 添加 隐藏软键盘
         */
        tv_cancel?.setOnClickListener {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(et_search.windowToken, 0)
            finish()
        }
    }


    fun initViews() {
        rv_coinmap.layoutManager = LinearLayoutManager(mActivity)
        /**
         * 添加操作
         *
         */
        adapter?.addChildClickViewIds(R.id.ib_add)

        /**
         * 点击添加自选
         */
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            val obj = adapter.data[position] as JSONObject
            val symbol = obj.optString("symbol")
            val isAdd = obj.optBoolean("isAdd")
            var hasAdd = isAdd
            hasAdd = if (hasAdd) {
                HeYueLikeDataService.getInstance().removeCollect(symbol)
                false
            } else {
                HeYueLikeDataService.getInstance().saveCollecData(symbol, obj)
                true
            }
            if (!LoginManager.isLogin(this)) {
                if (hasAdd) {
                    NToastUtil.showTopToast(
                        true,
                        LanguageUtil.getString(
                            this@HeYueMapActivity,
                            "kline_tip_addCollectionSuccess"
                        )
                    )
                } else {
                    NToastUtil.showTopToast(
                        true,
                        LanguageUtil.getString(
                            this@HeYueMapActivity,
                            "kline_tip_removeCollectionSuccess"
                        )
                    )
                }
            }

            addOrDeleteSymbol(if (hasAdd) 1 else 2, symbol)


            try {
                obj.put("isAdd", hasAdd)
                adapter.notifyItemChanged((adapter as HeYueMapAdapter).getCurrentPosition(position))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }


        rv_coinmap?.adapter = adapter
        adapter?.setList(getLikeData2(markList))


        /**
         * 监听搜索编辑框
         */
        et_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var resultList = getSearchMatchList(s.toString())
                if (null == resultList || resultList.size <= 0) {
                    adapter?.setList(null)
                    adapter?.setEmptyView(EmptyForAdapterView(this@HeYueMapActivity))
                } else {
                    adapter?.setList(getLikeData2(resultList))
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
            return markList
        }

        val temp = ArrayList<JSONObject>()
        markList.forEach {
            var name = it.optString("symbol")
            if (name.contains(input.toUpperCase()) || name.contains(input.toLowerCase()) || name.contains(
                    input
                )
            ) {
                temp.add(it)
            }
        }
        return getLikeData(temp, 0)

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
     * 判断是否在自选数据中
     */
    private fun getLikeData2(list: ArrayList<JSONObject>?): ArrayList<JSONObject>? {
        if (null == list || list.size <= 0)
            return list

        val tempList = ArrayList<JSONObject>()
        for (i in 0 until list.size) {
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
        addDisposable(
            getMainModel().addOrDeleteSymbol(
                operationType,
                list,
                "BTC-USDT",
                object : NDisposableObserver() {
                    override fun onResponseSuccess(jsonObject: JSONObject) {
                        if (operationType == 1) {
                            NToastUtil.showTopToastNet(
                                this@HeYueMapActivity,
                                true,
                                LanguageUtil.getString(
                                    this@HeYueMapActivity,
                                    "kline_tip_addCollectionSuccess"
                                )
                            )
                            HeYueLikeDataService.getInstance().saveCollecData(symbol, null)
                        } else {
                            NToastUtil.showTopToastNet(
                                this@HeYueMapActivity,
                                true,
                                LanguageUtil.getString(
                                    this@HeYueMapActivity,
                                    "kline_tip_removeCollectionSuccess"
                                )
                            )
                            HeYueLikeDataService.getInstance().removeCollect(symbol)
                        }

                    }
                })
        )
    }

    override fun clearSearch() {
        SearchDataService.getInstance().removeSearchData()
    }

    override fun hotItemClick(name: String) {
        et_search.setText(name)
        et_search.setSelection(name.length)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (MessageEvent.symbol_switch_type == event.msg_type) {
            finish()
        }
    }


}
