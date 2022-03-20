package com.yjkj.chainup.new_version.activity.personalCenter

import android.graphics.Typeface
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.yjkj.chainup.BuildConfig
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.net_new.HttpHelper
import com.yjkj.chainup.net_new.JSONUtil
import com.yjkj.chainup.net_new.rxjava.NDisposableObserver
import com.yjkj.chainup.new_version.adapter.ChangeNetworkAdapter
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_change_work.*
import okhttp3.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * @Author lianshangljl
 * @Date 2020-06-18-13:40
 * @Email buptjinlong@163.com
 * @description
 */
@Route(path = RoutePath.ChangenNetworkActivity)
class ChangenNetworkActivity : NBaseActivity() {
    override fun setContentView() = R.layout.activity_change_work


    var adapter: ChangeNetworkAdapter? = null
    var liksArray: ArrayList<JSONObject> = arrayListOf()
    var likesWSArray: ArrayList<JSONObject> = arrayListOf()
    var links = ""
    var linkTemp: ArrayList<JSONObject> = arrayListOf()
    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        collapsing_toolbar?.setCollapsedTitleTextColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleColor(ContextCompat.getColor(mActivity, R.color.text_color))
        collapsing_toolbar?.setExpandedTitleTypeface(Typeface.DEFAULT_BOLD)
        collapsing_toolbar?.expandedTitleGravity = Gravity.BOTTOM
        setTextContentView()
        initView()

    }

    fun setTextContentView() {
        collapsing_toolbar?.title = LanguageUtil.getString(this, "customSetting_action_changeHost")
    }


    override fun initView() {
        super.initView()
        toolbar?.setNavigationOnClickListener {
            finish()
        }
        links = PublicInfoDataService.getInstance().links
        linkTemp = PublicInfoDataService.getInstance().getLinkData(true)
        likesWSArray = PublicInfoDataService.getInstance().getLinkData(false)
        for ((index, item) in linkTemp.withIndex()) {
            item.put("hostNameWs", likesWSArray[index].optString("hostName"))
        }
        initAdapter()
        getOrderStateEachMin()
    }

    fun initAdapter() {
        adapter = ChangeNetworkAdapter(liksArray)
        recycler_view?.layoutManager = LinearLayoutManager(mActivity)
        adapter?.setHeaderView(View.inflate(this, R.layout.item_change_network_header, null))
        adapter?.setEmptyView(EmptyForAdapterView(this))
        recycler_view?.adapter = adapter
        adapter?.addChildClickViewIds(R.id.tv_content, R.id.tv_content_ws,R.id.tv_api_rb, R.id.tv_ws_rb)
        adapter?.setOnItemChildClickListener { adapter, view, position ->
            val likes = liksArray[position]
            when (view.id) {
                R.id.tv_content,R.id.tv_api_rb -> {
                    if (likes.isSpeedTime(true)) {
                        adapter.notifyDataSetChanged()
                        NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(this, "customSetting_action_host") + LanguageUtil.getString(this, "customSetting_action_unusable"))
                        return@setOnItemChildClickListener
                    }
                    HttpClient.instance.changeNetwork(liksArray[position].optString("hostName"))
                    adapter.notifyDataSetChanged()
                }
                R.id.tv_content_ws,R.id.tv_ws_rb -> {
                    if (likes.isSpeedTime(false)) {
                        adapter.notifyDataSetChanged()
                        NToastUtil.showTopToastNet(mActivity, false, LanguageUtil.getString(this, "customSetting_action_host") + LanguageUtil.getString(this, "customSetting_action_unusable"))
                        return@setOnItemChildClickListener
                    }
                    HttpClient.instance.changeNetwork(liksArray[position].optString("hostNameWs"), true)
                    adapter.notifyDataSetChanged()
                }

            }
        }
        tv_recharge_record?.onClick {
            speedWeb()
        }
        tv_reload?.setOnClickListener {
            getOrderStateEachMin()
        }
    }


    fun getHeath(index: Int, url: String) {
        val startTime = System.currentTimeMillis()
        val baseUrl = Utils.returnSpeedUrlV2(url, ApiConstants.BASE_URL)
        addDisposable(getSpeedModel().getHealth(baseUrl, object : NDisposableObserver() {
            override fun onResponseSuccess(jsonObject: JSONObject) {
                var endTime = System.currentTimeMillis()
                var consum = endTime - startTime
                liksArray[index].put("networkAppapi", "$consum")
                refreshView(index, true)
                val wsUrl = liksArray[index].optString("hostNameWs")
                initSocket(index, Utils.returnSpeedUrlV2(wsUrl, ApiConstants.SOCKET_ADDRESS))
            }

            override fun onResponseFailure(code: Int, msg: String?) {
                super.onResponseFailure(code, msg)
                liksArray[index].put("error", "error")
                Log.e(TAG, "onResponseFailure ${code}")
                refreshView(index, true)
                val wsUrl = liksArray[index].optString("hostNameWs")
                initSocket(index, Utils.returnSpeedUrlV2(wsUrl, ApiConstants.SOCKET_ADDRESS))
            }
        }))
    }


    private var mSocketClient: WebSocketClient? = null
    private fun initSocket(index: Int, url: String) {
        if (mSocketClient != null && mSocketClient?.isOpen == true) {
            return
        }
        val startTime = System.currentTimeMillis()
        mSocketClient = object : WebSocketClient(URI(url)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                var endTime = System.currentTimeMillis()
                var consum = endTime - startTime
                Log.e(TAG, "onOpen ${index}")
                liksArray[index].put("networkWs", "$consum")
                refreshView(index, false)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.e(TAG, "onClose:$code  index:$index")
                if (code == -1 || code == 1002) {
                    errorWsBy(index)
                }
            }

            override fun onMessage(bytes: ByteBuffer?) {
                super.onMessage(bytes)
                Log.e(TAG, "onMessage")
            }

            override fun onMessage(message: String?) {
                Log.e(TAG, "onMessage12")
            }

            override fun onError(ex: Exception?) {
                Log.e(TAG, "${index} onError: ${ex?.message}")
//                errorWsBy(index)
            }
        }
        mSocketClient?.connect()
    }


    fun refreshView(index: Int, isApi: Boolean) {
        if (isApi) {
            liksArray[index].put("isSpeedApi", true)
        } else {
            liksArray[index].put("isSpeedWs", true)
        }
        runOnUiThread {
            adapter?.notifyItemChanged(index + 1, null)
        }

        if (isApi) {
            val speedApiEnd = liksArray.filter {
                it.optBoolean("isSpeedApi", false)
            }
            Log.v(TAG, "refreshView ${speedApiEnd.size}")
            if (speedApiEnd.size == liksArray.size) {
                val tempLink = speedApiEnd.sortedBy { it.optString("networkAppapi", "5000").toInt() }
                val topUrl = tempLink[0].optString("hostName")
                if (topUrl != PublicInfoDataService.getInstance().newWorkURL) {
                    HttpClient.instance.changeNetwork(topUrl, false)
                    adapter?.notifyDataSetChanged()
                }
                Log.e(TAG, "refreshView api top ${index} ${topUrl} ${PublicInfoDataService.getInstance().newWorkURL}")
            }
        } else {
            val speedWsEnd = liksArray.filter {
                it.optBoolean("isSpeedWs", false)
            }
            Log.v(TAG, "refreshView ws ${speedWsEnd.size}")
            if (speedWsEnd.size == liksArray.size - 1) {
                val tempLink = speedWsEnd.sortedBy { it.optString("networkWs", "5000").toInt() }
                val topUrl = tempLink[0].optString("hostNameWs")
                if (topUrl != PublicInfoDataService.getInstance().newWorkWSURL) {
                    HttpClient.instance.changeNetwork(topUrl, true)
                    adapter?.notifyDataSetChanged()
                }
                Log.e(TAG, "refreshView ws top ${index} ${topUrl} ${PublicInfoDataService.getInstance().newWorkWSURL}")
            }
        }
    }

    fun loopNetworkState() {
        if (liksArray.isEmpty()) return
        for (num in 0 until liksArray.size) {
            try {
                if (!liksArray[num].isNull("isSpeedApi")) {
                    liksArray[num].remove("isSpeedApi")
                }
                if (!liksArray[num].isNull("isSpeedWs")) {
                    liksArray[num].remove("isSpeedWs")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            getHeath(num, liksArray[num].optString("hostName"))
        }
        mSocketClient?.close()
    }


    /**
     * 每1分钟调用一次接口x
     */
    private fun getOrderStateEachMin() {
        liksArray.clear()
        liksArray.addAll(linkTemp)
        adapter?.notifyDataSetChanged()
        loopNetworkState()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mSocketClient != null && mSocketClient?.isOpen == true) {
            mSocketClient?.closeBlocking()
            mSocketClient?.close()
        }
    }

    private fun speedWeb() {
        val api = StringBuffer()
        val ws = StringBuffer()

        liksArray.forEach {
            val netApi = it.optString("networkAppapi", "--")
            if (netApi != "--") {
                api.append("${netApi}ms,")
            } else {
                api.append("${netApi},")
            }
            val wsApi = it.optString("networkWs", "--")
            if (wsApi != "--") {
                ws.append("${wsApi}ms,")
            } else {
                ws.append("${wsApi},")
            }
        }
        val speedApi = api.substring(0, api.length - 1)
        val speedWs = ws.substring(0, ws.length - 1)
        var bundle = Bundle()
        var title = ""
        bundle.putString(ParamConstant.head_title, title)
        val speedUrl = if (BuildConfig.DEBUG) {
            "http://m.hiotc.pro/zh_CN/app_operation/network/"
        } else {
            "https://m0001003.lcuiww.top/zh_CN/app_operation/network/"
        }
        bundle.putString(ParamConstant.web_url, speedUrl)
        bundle.putInt(ParamConstant.web_type, WebTypeEnum.NORMAL_INDEX.value)
        bundle.putString(ParamConstant.SPEED_WEB_API, speedApi)
        bundle.putString(ParamConstant.SPEED_WS_API, speedWs)
        ArouterUtil.greenChannel(RoutePath.ItemDetailActivity, bundle)
    }

    private fun errorWsBy(index: Int) {
        liksArray[index].put("error_ws", "error")
        refreshView(index, false)
    }

}