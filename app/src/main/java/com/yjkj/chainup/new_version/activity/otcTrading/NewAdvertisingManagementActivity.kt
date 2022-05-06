package com.yjkj.chainup.new_version.activity.otcTrading

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.JSONUtil
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.AdvertisingManagementAdapter
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.NToastUtil
import kotlinx.android.synthetic.main.activity_advertising_management.*
import org.json.JSONObject

/**
 * @Author lianshangljl
 * @Date 2019-10-14-15:07
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.NewAdvertisingManagementActivity)
class NewAdvertisingManagementActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_advertising_management


    var adapter: AdvertisingManagementAdapter? = null
    var adType = "BUY"
    var closeHide = "1"
    var list: ArrayList<JSONObject> = arrayListOf()
    var checkBoxIsCheckout = true

    var dialog: TDialog? = null


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        adType = "BUY"
        tv_advertising_trading?.text = LanguageUtil.getString(this, "otc_action_merchantBuy")
        initView()
        setOnclick()
        getAdavertisingList()
        getData(false)
        setTextContent()
    }

    fun setTextContent() {
        v_title?.setContentTitle(LanguageUtil.getString(this, "otc_publish_advertise"))
        v_title?.setRightTitle(LanguageUtil.getString(this, "otc_publish_advertise"))
        fragment_my_asset_order_hide?.text = LanguageUtil.getString(this, "otc_text_adHidden")

    }

    override fun initView() {
        v_title?.listener = object : PersonalCenterView.MyProfileListener {
            override fun onclickHead() {

            }

            override fun onclickRightIcon() {
                if (!DisplayUtil.getCerificationStatus(this@NewAdvertisingManagementActivity, beans)) {
                    getwantedDetailCheck()
                }


            }

            override fun onclickName() {

            }

            override fun onRealNameCertificat() {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        getUserPayment4OTC()
    }

    val pageSize = 20
    var page = 1
    var isScrollStatus = true

    fun setOnclick() {
        /**
         * 此处是刷新页面
         */
        swipe_refresh?.setOnRefreshListener {
            page = 1
            isScrollStatus = true
            getData(false)
        }
        recycler_view?.setOnScrollListener(object : RecyclerView.OnScrollListener() {

            var lastVisibleItem = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                var layoutManager: LinearLayoutManager = recyclerView?.layoutManager as LinearLayoutManager
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter?.itemCount && isScrollStatus) {
                    page += 1
                    getData(true)
                }
            }

        })
        /**
         * 点击购买广告
         */
        tv_advertising_trading_layout.setOnClickListener {
            dialog = NewDialogUtils.showBottomListDialog(this, arrayListOf(LanguageUtil.getString(this, "otc_action_merchantBuy"), LanguageUtil.getString(this, "otc_action_merchantSell")), -1, object : NewDialogUtils.DialogOnclickListener {
                override fun clickItem(data: ArrayList<String>, item: Int) {
                    when (item) {
                        0 -> {
                            adType = "BUY"
                            tv_advertising_trading?.text = LanguageUtil.getString(this@NewAdvertisingManagementActivity, "otc_action_merchantBuy")
                        }
                        1 -> {
                            adType = "SELL"
                            tv_advertising_trading?.text = LanguageUtil.getString(this@NewAdvertisingManagementActivity, "otc_action_merchantSell")
                        }
                    }
                    getData(false)
                    dialog?.dismiss()
                }
            })
        }

        /**
         * 点击隐藏或者关闭广告
         */
        fragment_my_asset_order_hide.setOnClickListener {
            checkBoxIsCheckout = !checkBoxIsCheckout
            if (checkBoxIsCheckout) {
                closeHide = "1"
            } else {
                closeHide = "0"
            }
            getData(false)
        }
    }

    fun initAdapter(adList: ArrayList<JSONObject>) {
        if (isFinishing || isDestroyed) {
            return
        }
        if (recycler_view == null) return
        list.clear()
        list.addAll(adList)
        adapter = AdvertisingManagementAdapter(list)
        recycler_view?.layoutManager = LinearLayoutManager(this)
        adapter?.setEmptyView(EmptyForAdapterView(this))
        recycler_view?.isNestedScrollingEnabled = false
        recycler_view?.adapter = adapter
        adapter?.setOnItemClickListener { adapter, view, position ->
            intoAdvertisiongDetailActivity(list[position].optString("advertId").toString())
        }
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            intoAdvertisiongDetailActivity(list[position].optString("advertId").toString())
        }

    }


    fun intoAdvertisiongDetailActivity(advertId: String) {
        var bundle = Bundle()
        bundle.putString(ParamConstant.advertID, advertId)
        ArouterUtil.navigation(RoutePath.NewAdvertisingDetailActivity, bundle)
    }

    fun refreshData(datas: ArrayList<JSONObject>) {
        list.addAll(datas)
        adapter?.notifyDataSetChanged()
    }

    fun getData(refresh: Boolean) {
        getAdavertisingList(adType, closeHide, refresh)
    }

    fun getAdavertisingList(adType: String = "", closeHide: String = "1", refresh: Boolean = false) {
        if (!LoginManager.checkLogin(this, false)) return

        addDisposable(getOTCModel().getNewPersonalAds(UserDataService.getInstance().userInfo4UserId, adType,
                closeHide, page.toString(), pageSize.toString(), object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var json = jsonObject.optJSONObject("data")

                if (null != json.optJSONArray("adList")) {
                    var jsonArray = json.optJSONArray("adList")
                    if (jsonArray.length() < 20) {
                        isScrollStatus = false
                    }
                    var adList = arrayListOf<JSONObject>()
                    for (json in 0 until jsonArray.length()) {
                        adList.add(jsonArray.optJSONObject(json))
                    }
                    if (refresh) {
                        refreshData(adList)
                    } else {
                        initAdapter(adList)
                    }
                } else {
                    if (refresh) {
                        list.clear()
                        adapter?.notifyDataSetChanged()
                    } else {
                        list.clear()
                        initAdapter(list)
                    }
                }



                swipe_refresh?.isRefreshing = false
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                swipe_refresh?.isRefreshing = false
            }

        }))

    }


    var beans: ArrayList<JSONObject> = arrayListOf()
    /**
     * 获取支付方式
     */
    private fun getUserPayment4OTC() {
        if (UserDataService.getInstance().isLogined) {
            addDisposable(getOTCModel().getUserPayment4OTC(consumer = object : NDisposableObserver() {
                override fun onResponseSuccess(jsonObject: JSONObject) {
                    beans.clear()
                    var json = jsonObject.optJSONArray("data")
                    var list = JSONUtil.arrayToList(json)
                    if (null != list) {
                        beans?.addAll(list)
                    }
                }

            }))


        }
    }

    /**
     * 发布前判断
     */
    private fun getwantedDetailCheck() {

        addDisposable(getOTCModel().getwantedDetailCheck(object : NDisposableObserver(this) {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                ArouterUtil.navigation(RoutePath.NewReleaseAdvertisingActivity, null)
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                NToastUtil.showTopToastNet(mActivity,false, msg)
            }
        }))

    }

}


