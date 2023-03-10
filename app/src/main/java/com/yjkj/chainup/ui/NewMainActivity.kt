package com.yjkj.chainup.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.SPUtils
import com.chainup.contract.app.CpCommonConstant
import com.chainup.contract.eventbus.CpCollectionEvent
import com.chainup.contract.eventbus.CpCollectionEvent2
import com.chainup.contract.eventbus.CpMessageEvent
import com.chainup.contract.ui.fragment.CpContractNewTradeFragment
import com.chainup.contract.utils.CpClLogicContractSetting
import com.didichuxing.doraemonkit.DoraemonKit
import com.igexin.sdk.PushManager
import com.jaeger.library.StatusBarUtil
import com.yjkj.chainup.BuildConfig
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.HomeTabMap
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.db.service.RateDataService
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.extra_service.eventbus.NLiveDataUtil
import com.yjkj.chainup.manager.LoginManager
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.NDisposableObserver
import com.yjkj.chainup.new_version.activity.leverage.TradeFragment
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.fragment.MarketFragment
import com.yjkj.chainup.new_version.home.*
import com.yjkj.chainup.ui.asset.AssetPFragment
import com.yjkj.chainup.ui.home.NewVersionHomepageFragment
import com.yjkj.chainup.util.*
import com.yjkj.chainup.ws.WsAgentManager
import com.yjkj.chainup.ws.WsContractAgentManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_main.*
import kotlinx.android.synthetic.main.check_visit_status.*
import kotlinx.android.synthetic.main.no_network_remind.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// TODO ??????
@Route(path = RoutePath.NewMainActivity)
class NewMainActivity : NBaseActivity() {


    override fun setContentView() = R.layout.activity_new_main

    /*
     * ??????tab??????????????????????????????
     */
    var curPosition = 0
    var lastPosition = 0
    var connectCount = 0

    private var assetsTab = -1


    //??????
    private var marketFragment = MarketFragment()

    //??????
    private var tradeFragment = TradeFragment()

    //??????
    private var slCoContractFragment = CpContractNewTradeFragment()

    //??????
    private var assetFragment = AssetPFragment()

    private var fragmentManager: FragmentManager? = null
    var subscribe: Disposable? = null
    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        fragmentManager = supportFragmentManager
//    val intent = Intent(this, MyFirebaseMessagingService::class.java)
//    startService(intent)
        loadData()
        DoraemonKit.disableUpload()
        DoraemonKit.install(application, "cb190f56cf")
        DoraemonKit.setAwaysShowMainIcon(false)
        DoraemonKit.setDebug(true)
        DoraemonKit.show()
        netChangeStatus()
//    uploadFCMToken()
    }

//  /**
//   * ??????????????????FCM token??????????????????
//   */
//  private fun uploadFCMToken() {
//    try {
//      val googlePlayServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
//      if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
//        FirebaseMessaging.getInstance().token
//          .addOnCompleteListener {
//            if (it.isSuccessful) {
//              val token = it.result
//            } else {
//              ToastUtils.showToast("????????????")
//            }
//
//          }
//      } else {
//        ToastUtils.showToast("????????????")
//      }
//    } catch (e: Exception) {
//      e.printStackTrace();
//      ToastUtils.showToast("????????????")
//    }
//  }

    /*
    *??????????????????
    */
    private fun netChangeStatus() {
        NetUtil.registerNetConnChangedReceiver(this)
        NetUtil.addNetConnChangedListener(object : NetUtil.Companion.NetConnChangedListener {
            override fun onNetConnChanged(connectStatus: NetUtil.Companion.ConnectStatus) {
                WsAgentManager.instance.changeNotice(connectStatus)
                Handler().postDelayed({
                    EventBusUtil.post(MessageEvent(MessageEvent.net_status_change))
                }, 1500)
            }
        })


    }


    private var fragmentList = arrayListOf<Fragment>()
    private var mImageViewList = ArrayList<Int>()
    private var mTextviewList = ArrayList<String>()
    private var contractOpen = false
    private fun initTabsData(data: JSONObject?) {

        contractOpen = PublicInfoDataService.getInstance().contractOpen(data)
        val cid = PublicInfoDataService.getInstance().getCompanyId(data)
        WsAgentManager.instance.saveCID(cid)
        fragmentList.add(NewVersionHomepageFragment())
        mImageViewList.add(R.drawable.bg_homepage_tab)
        mTextviewList.add(LanguageUtil.getString(this, "mainTab_text_home"))


        fragmentList.add(marketFragment)
        fragmentList.add(tradeFragment)
        mImageViewList.add(R.drawable.bg_market_tab)
        mImageViewList.add(R.drawable.bg_trade_tab)

        mTextviewList.add(LanguageUtil.getString(this, "mainTab_text_market"))

        mTextviewList.add(LanguageUtil.getString(this, "assets_action_transaction"))

        if (contractOpen) {
            fragmentList.add(slCoContractFragment)
            UserDataService.getInstance().token
            UserDataService.getInstance().notifyContractLoginStatusListener()
            mImageViewList.add(R.drawable.bg_contract_tab)
            mTextviewList.add(LanguageUtil.getString(this, "mainTab_text_contract"))
        }
        fragmentList.add(assetFragment)
        mImageViewList.add(R.drawable.bg_asset_tab)
        mTextviewList.add(LanguageUtil.getString(this, "mainTab_text_assets"))
        assetsTab = fragmentList.size - 1


        getAdvert()
        HomeTabMap.initMaps(data)
        initView()
        val isNewForceContract = PublicInfoDataService.getInstance().isNewForceContract
        if (isNewForceContract && contractOpen) {
            showLogoutDialog()
        }
    }


    override fun initView() {
        showTabs()
        no_network_check?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        no_network_retry_btn?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        initNetWorkRemind()
    }

    private fun initNetWorkRemind() {
        val spanStrStart = SpannableString(getString(R.string.check_network_settings))
        val spanStrClick = SpannableString(getString(R.string.check_network))
        val index = spanStrStart.indexOf(spanStrClick.toString(), 0)
        val spanStrStartSub = spanStrStart.subSequence(0, index)
        val spanStrEnd = spanStrStart.subSequence(index + spanStrClick.length, spanStrStart.length)

        spanStrClick.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = mActivity.resources.getColor(R.color.color_nonetwork_btn_blue) //????????????
                //??????????????????????????????????????????
                ds.isUnderlineText = false
            }
        }, 0, spanStrClick.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        no_network_check.append(spanStrStartSub)
        no_network_check.append(spanStrClick)
        no_network_check.append(spanStrEnd)
        no_network_check.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun showTabs() {
        bottomtab_group?.setData(mImageViewList, mTextviewList, this, contractIndex, false)
        for (i in 0 until fragmentList.size) {
            val fg = fragmentList[i]
            val transaction = fragmentManager?.beginTransaction()
            transaction?.add(R.id.fragment_container, fg, fg.javaClass.name)
                ?.commitAllowingStateLoss()
        }
        setCurrentItem()
    }


    override fun onClick(view: View) {
        super.onClick(view)
        val tag = view.tag
        if (tag is Int) {
            if (assetsTab > 0 && tag == assetsTab) {
                if (!LoginManager.checkLogin(mActivity, true)) {
                    return
                }
            }
            curPosition = tag
            if (lastPosition != curPosition) {
                lastPosition = curPosition
            }

            setCurrentItem()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }

    }

    @SuppressLint("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        if (event.msg_type == MessageEvent.coin_payment) {

            val msg_content = event.msg_content
            if (null != msg_content && msg_content is JSONObject) {
                val position = msg_content.optInt("position")
                if (ParamConstant.TYPE_COIN == position) {
                    curPosition = HomeTabMap.maps[HomeTabMap.coinTradeTab] ?: 2
                    setCurrentItem()
                } else if (ParamConstant.TYPE_FAIT == position) {
                    if (SPUtils.getInstance().getBoolean(ParamConstant.simulate, false)) {
                        ToastUtils.showToast(getString(R.string.important_hint1))
                        return
                    }
                    ArouterUtil.navigation(RoutePath.NewVersionOTCActivity, null)
                }
            }
        } else if (MessageEvent.hometab_switch_type == event.msg_type) {
            //????????????tab??????
            var msg_content = event.msg_content
            if (null != msg_content && msg_content is Bundle) {
                curPosition = msg_content.getInt(ParamConstant.homeTabType)
                if (HomeTabMap.maps[HomeTabMap.coinTradeTab] == curPosition) {
                    Handler().postDelayed({
                        forwardConinTradeTab(event.msg_content as Bundle)
                    }, 150)

                } else if (HomeTabMap.maps[HomeTabMap.assetsTab] == curPosition) {
                    Handler().postDelayed({
                        forwardAssetsTab(event.msg_content as Bundle)
                    }, 150)
                }
                setCurrentItem()
            }
        } else if (MessageEvent.contract_switch_type == event.msg_type) {
            /**
             * ????????????
             */
            curPosition = HomeTabMap.maps[HomeTabMap.contractTab] ?: 0
            setCurrentItem()
        } else if (MessageEvent.market_switch_type == event.msg_type) {
            //????????????tab??????
            val msg_content = event.msg_content
            if (null != msg_content) {
                curPosition = 1
                setCurrentItem()
            }
        } else if (MessageEvent.sl_contract_force_event == event.msg_type) {
            LogUtil.e("LogUtils", "?????????????????????")
            showLogoutDialog()
        } else if (event.msg_type == MessageEvent.refresh_ws_error_change) {
            LogUtil.e("LogUtils", "ws ????????????????????????")
            connectCount++
            if (connectCount == 11) {
                wsConnectCount()
            }
        } else if (event.msg_type == MessageEvent.refresh_ws_open_change) {
            LogUtil.e("LogUtils", "ws ????????????")
            connectCount = 0
            wsConnectCount()
            val klineTime = event.msg_content as Long
            sendWsHomepage(
                mActivity,
                0,
                NetworkDataService.KEY_PAGE_MARKET_SERVICE,
                NetworkDataService.KEY_WS_OPEN,
                klineTime
            )
        } else if (MessageEvent.login_bind_type == event.msg_type) {
            LogUtil.e(
                "LogUtils",
                "???????????? ${UserDataService.getInstance().token}  [] ${
                    PushManager.getInstance().getClientid(this)
                }"
            )
            CpClLogicContractSetting.setToken(UserDataService.getInstance().token)


        }
    }

    /**
     * ??????ws????????????????????????
     */
    private fun wsConnectCount() {
        if (connectCount > 10) {
            if (!mActivity.let(NetUtil.Companion::isNetConnected)) {
                no_network_main_bg?.visibility = View.VISIBLE
                main_bg?.visibility = View.GONE
            } else {
                no_network_main_bg?.visibility = View.GONE
                main_bg?.visibility = View.VISIBLE
            }
        } else {
            no_network_main_bg?.visibility = View.GONE
            main_bg?.visibility = View.VISIBLE
        }
        EventBusUtil.post(MessageEvent(MessageEvent.net_status_change))
    }

    override fun onCpMessageEvent(event: CpMessageEvent) {
        when {
            CpMessageEvent.sl_contract_go_login_page == event.msg_type -> {
                LoginManager.checkLogin(this, true)
            }
            CpMessageEvent.sl_contract_go_fundsTransfer_page == event.msg_type -> {
                ArouterUtil.navigation(RoutePath.NewVersionTransferActivity, Bundle().apply {
                    putString(ParamConstant.TRANSFERSTATUS, ParamConstant.TRANSFER_CONTRACT)
                    putString(ParamConstant.TRANSFERSYMBOL, event.msg_content.toString())
                })
            }
            (CpMessageEvent.contract_switch_type == event.msg_type) -> {
                /**
                 * ????????????
                 */
                curPosition = HomeTabMap.maps[HomeTabMap.contractTab] ?: 0
                setCurrentItem()
            }
            CpMessageEvent.sl_contract_go_kyc_page == event.msg_type -> {
                ArouterUtil.greenChannel(RoutePath.RealNameCertificationActivity, null)
            }
            CpMessageEvent.sl_contract_transfer == event.msg_type -> {
                ArouterUtil.forwardTransfer(ParamConstant.TRANSFER_CONTRACT, "USDT")
            }
        }
    }

    /*
     * ???????????????tab
     */
    private fun forwardConinTradeTab(bundle: Bundle) {
        LogUtil.d(TAG, "onMessageEvent==NewMainActivity==????????????==bundle is $bundle")
        var msg_event = MessageEvent(MessageEvent.coinTrade_tab_type)
        msg_event.msg_content = bundle
        EventBusUtil.post(msg_event)
    }


    /*
     * ???????????????
     */
    private fun forwardAssetsTab(bundle: Bundle) {
        var msg_event = MessageEvent(MessageEvent.assetsTab_type)
        msg_event.msg_content = bundle
        EventBusUtil.post(msg_event)
    }


    private var exitTime = 0L
    override fun onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            UIUtils.showToast(LanguageUtil.getString(this, "exit_remind"))
            exitTime = System.currentTimeMillis()
            return
        }
        HttpClient.instance.setToken("")
        super.onBackPressed()
    }

    private fun setCurrentItem() {
        mActivity.runOnUiThread {
            bottomtab_group?.showCurTabView(curPosition)
        }
        if (mTextviewList[curPosition].equals(
                LanguageUtil.getString(
                    this,
                    "mainTab_text_assets"
                )
            )
        ) {
            StatusBarUtil.setColor(
                this,
                ColorUtil.getColorByMode(R.color.market_status_bar_color_day),
                0
            )
        } else {
            StatusBarUtil.setColor(
                this,
                ColorUtil.getColorByMode(R.color.market_status_bar_color_day),
                0
            )
        }
        for (i in 0 until fragmentList.size) {
            val transaction = fragmentManager?.beginTransaction()
            val fg = fragmentList[i]
            if (i == curPosition) {
                mActivity.runOnUiThread {
                    transaction?.show(fg)?.commitAllowingStateLoss()
                }
            } else {
                if (!fg.isHidden) {
                    transaction?.hide(fg)?.commitAllowingStateLoss()
                }
            }
        }
    }


    override fun loadData() {
        super.loadData()
        val catchObj = PublicInfoDataService.getInstance().getData(null)
        if (null != catchObj && catchObj.length() > 0) {
            initTabsData(catchObj)
        } else {
            addDisposable(getMainModel().public_info_v4(MyNDisposableObserver(mActivity)))
        }
        contractOpen = PublicInfoDataService.getInstance().contractOpen(catchObj)
        WsContractAgentManager.instance.connectionSocket()
        if (LoginManager.isLogin(this)) {
            getMainModel().saveUserInfo()
        }
    }

    private fun loopStart() {
        loopStop()
        subscribe = Observable.interval(0L, CpCommonConstant.coinLoopTime, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                getLimitIpLogin()
            }
    }


    private fun getLimitIpLogin() {
        addDisposable(getMainModel().limit_ip_login(object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                check_visitstatus.visibility = View.GONE
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                if (code == 109109) {
                    check_visitstatus.visibility = View.VISIBLE
                    check_visit_tv2.text = msg
                }
            }
        }))
    }

    /*
    *  ????????????????????????????????????????????????????????????
    * */
    inner class MyNDisposableObserver(activity: Activity) : NDisposableObserver(activity, false) {
        override fun onResponseSuccess(jsonObject: JSONObject) {
            var data = jsonObject.optJSONObject("data")
            if (null != data && data.length() > 0) {
                var rate = data.optJSONObject("rate")
                RateDataService.getInstance().saveData(rate)
            }

            PublicInfoDataService.getInstance().saveData(data)
            initTabsData(data)
        }

        override fun onResponseFailure(code: Int, msg: String?) {
            super.onResponseFailure(code, msg)
            var cachObj = PublicInfoDataService.getInstance().getData(null)
            initTabsData(cachObj)
        }
    }


    override fun onResume() {
        super.onResume()
        if (BuildConfig.isRelease) {
            loopStart()
        }
    }

    override fun onPause() {
        super.onPause()
        LogUtil.e("ForegroundCallbacks", "MainActivity onPause")
        loopStop()
    }

    override fun onStop() {
        super.onStop()
        LogUtil.e("ForegroundCallbacks", "MainActivity onStop")
        loopStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        NLiveDataUtil.removeObservers()
        WsAgentManager.instance.stopWs()
        WsContractAgentManager.instance.stopWs()
        dialogType = 0
        NetUtil.unregisterNetConnChangedReceiver(this)
        loopStop()
    }


    private fun loopStop() {
        LogUtil.e("-----", "????????????")
        if (subscribe != null) {
            subscribe?.dispose()
        }
    }


    private var contractIndex = -1


    /**
     * ???????????????Dialog
     */
    private fun showLogoutDialog(position: Int = 1) {
        NewDialogUtils.showSingleForceDialog(
            this,
            LanguageUtil.getString(this, "newContract_force_changeCo_desc"),
            object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    initPushCheck(position)
                }
            })
    }

    /**
     * ????????????
     */
    fun initPushCheck(position: Int) {
        PublicInfoDataService.getInstance().contractMode = position
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(0)
    }

    @SuppressLint("CheckResult")
    private fun getAdvert() {
        homeAdvert(this)
        HttpClient.instance.getHomeAdvert()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it != null) AdvertDataService.instance.getAdvertAndCacheLocal(it.data)
            }, {
                it.printStackTrace()
            })
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.getAction() == MotionEvent.ACTION_DOWN) {
            val v = getCurrentFocus()
            v?.let {
                if (isShouldHideKeyboard(it, ev)) {
                    val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
                    it.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isShouldHideKeyboard(v: View, event: MotionEvent): Boolean {
        if (v != null && (v is EditText)) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            return !(event.x > left && event.x < right
                    && event.y > top && event.y < bottom)
        }
        return false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCpCollectionEvent(event: CpCollectionEvent) {
        Log.e("yubch", "aaaaaaaa:" + event.type)
        if(TextUtils.equals(event.type, CpCollectionEvent.TYPE_REQUEST)) {
            if(UserDataService.getInstance().isLogined && PublicInfoDataService.getInstance().isOptionalSymbolServerOpen(null)) {
                //?????????
                getOptionalSymbol()
            }
        } else if(TextUtils.equals(event.type, CpCollectionEvent.TYPE_ADD_DEL)) {
            if(!UserDataService.getInstance().isLogined) {
                //????????????
                ArouterUtil.greenChannel(RoutePath.NewVersionLoginActivity, null)
                return
            }
            addOrDeleteSymbol(event.operationType, event.symbol)
        }
    }

    val getUserSelfDataReqType = 2 // ???????????????????????????
    val addCancelUserSelfDataReqType = 1 // ???????????????????????????
    /*
     * ?????????????????????????????????
     * var req_type = type
     */
    private fun getOptionalSymbol() {
        addDisposable(getMainModel().getOptionalSymbol(MyNDisposableObserver2(getUserSelfDataReqType), ""))
    }

    inner class MyNDisposableObserver2(type: Int) : NDisposableObserver(mActivity) {

        var req_type = type
        override fun onResponseSuccess(jsonObject: JSONObject) {
            //?????????NewMainActivity
            EventBus.getDefault().post(CpCollectionEvent2(req_type, jsonObject))
            /*if (getUserSelfDataReqType == req_type) {
                showServerSelfSymbols(jsonObject.optJSONObject("data"))
            } else if (addCancelUserSelfDataReqType == req_type) {
                var hasCollect = false
                if (operationType == 2) {
                    serverSelfSymbols.remove(symbol)
                } else {
                    hasCollect = true
                    serverSelfSymbols.add(symbol)
                }
                showImgCollect(hasCollect, true, true)
            }*/
        }
    }

    /**
     * ??????????????????????????????
     * @param operationType ?????? 0(????????????)/1(????????????)/2(????????????)
     * @param symbol ??????????????????
     */
    private fun addOrDeleteSymbol(operationType: Int = 0, symbol: String?) {
        if (null == symbol)
            return
        var list = ArrayList<String>()
        list.add(symbol)
        //list.add("e-$symbol")
        addDisposable(getMainModel().addOrDeleteSymbol(operationType, list,"", MyNDisposableObserver2(addCancelUserSelfDataReqType)))
    }
}


