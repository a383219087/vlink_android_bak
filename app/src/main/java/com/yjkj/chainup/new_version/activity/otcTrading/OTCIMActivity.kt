package com.yjkj.chainup.new_version.activity.otcTrading

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import com.yjkj.chainup.util.JsonUtils
import com.google.gson.JsonObject
import com.tbruyelle.rxpermissions2.RxPermissions
import com.timmy.tdialog.TDialog
import com.yjkj.chainup.R
import com.yjkj.chainup.db.service.UserDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.asset.FIRST_INDEX
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.new_version.adapter.OTCIMDetailsProblemBeanAdapter
import com.yjkj.chainup.new_version.adapter.OTCImAdapter
import com.yjkj.chainup.new_version.bean.OTCChatBean
import com.yjkj.chainup.new_version.bean.OTCIMDetailsProblemBean
import com.yjkj.chainup.new_version.bean.OTCIMMessageBean
import com.yjkj.chainup.util.GZIPUtils
import com.yjkj.chainup.util.ImageTools
import com.yjkj.chainup.util.ToastUtils
import com.yjkj.chainup.util.Utils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_otc_im.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

/**
 * @Author lianshangljl
 * @Date 2018/10/15-下午12:02
 * @Email buptjinlong@163.com
 * @description 聊天界面
 */


class OTCIMActivity : NewBaseActivity() {
    var chatType = 0
    var complainId = 0
    var orderTime: Long = 0
    var orderId = "0"
    var symbol = ""
    var amount = "0"
    var orderType = ""
    var amountSymbol = ""
    var nickName = ""
    var toId = 0
    var time = ""
    var isComplainUser = 0
    var orderRole = "buy"

    var detailsList: ArrayList<OTCIMDetailsProblemBean.RqReplyList>? = arrayListOf()
    var messageList: ArrayList<OTCIMMessageBean>? = arrayListOf()

    var fromId = UserDataService.getInstance().userInfo4UserId.toInt()

    var headUrl = ""

    companion object {

        val COMPLAINID = "complainId"
        val TYPE = "IM_TYPE"
        val TO_ID = "IM_TO_ID"
        val ORDER_ID = "IM_ORDER_ID"
        val IM_SYMBOL = "IM_SYMBOL"
        val TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT"
        val ORDER_TYPE = "ORDER_TYPE"
        val AMOUNT_SYMBOL = "AMOUNT_SYMBOL"
        val IM_NICKNAME = "IM_NICKNAME"
        val IM_TIME = "IM_TIME"
        val ORDER_ROLE = "ORDER_ROLE"
        val ORDER_TIME = "ORDER_TIME"
        val ISCOMPLAINUSER = "ISCOMPLAINUSER"
        val HEAD_URL = "HEAD_URL"

        val MESSAGE_OTC = 1
        val DETAILS_PROBLEM = 2


        /**
         * 与客服聊天 机器人
         *
         */
        fun newIntent(context: Context, uid: Int, symbol: String, amount: String, orderType: String, amountSymbol: String, time: String, toId: Int
                      , orderId: String, nickName: String, orderTime: Long = 0, isComplainUser: Int, orderRole: String) {
            var intent = Intent(context, OTCIMActivity::class.java)
            intent.putExtra(COMPLAINID, uid)
            intent.putExtra(IM_SYMBOL, symbol)
            intent.putExtra(ORDER_ID, orderId)
            intent.putExtra(TRANSACTION_AMOUNT, amount)
            intent.putExtra(AMOUNT_SYMBOL, amountSymbol)
            intent.putExtra(ORDER_TYPE, orderType)
            intent.putExtra(ORDER_TIME, orderTime)
            intent.putExtra(IM_TIME, time)
            intent.putExtra(TO_ID, toId)
            intent.putExtra(ORDER_ROLE, orderRole)
            intent.putExtra(IM_NICKNAME, nickName)
            intent.putExtra(ISCOMPLAINUSER, isComplainUser)
            intent.putExtra(TYPE, DETAILS_PROBLEM)
            context.startActivity(intent)
        }

        /**
         * 买家
         */
        fun newIntent4Buyer(context: Context, toId: Int, orderId: String,
                            symbol: String, amount: String, orderType: String,
                            amountSymbol: String, nickName: String, time: String,
                            orderTime: Long = 0, isComplainUser: Int, uid: Int, url: String = "") {
            var intent = Intent(context, OTCIMActivity::class.java)
            intent.putExtra(TO_ID, toId)
            intent.putExtra(IM_SYMBOL, symbol)
            intent.putExtra(TRANSACTION_AMOUNT, amount)
            intent.putExtra(AMOUNT_SYMBOL, amountSymbol)
            intent.putExtra(ORDER_TYPE, orderType)
            intent.putExtra(ISCOMPLAINUSER, isComplainUser)
            intent.putExtra(ORDER_ID, orderId)
            intent.putExtra(ORDER_ROLE, "sell")
            intent.putExtra(ORDER_TIME, orderTime)
            intent.putExtra(IM_TIME, time)
            intent.putExtra(IM_NICKNAME, nickName)
            intent.putExtra(COMPLAINID, uid)
            intent.putExtra(TYPE, MESSAGE_OTC)
            intent.putExtra(HEAD_URL, url)
            context.startActivity(intent)
        }

        /**
         * 卖家
         */
        fun newIntent4Seller(context: Context, toId: Int, orderId: String, symbol: String, amount: String, orderType: String, amountSymbol: String,
                             nickName: String, time: String, orderTime: Long = 0, isComplainUser: Int, uid: Int, url: String = "") {
            var intent = Intent(context, OTCIMActivity::class.java)
            intent.putExtra(TO_ID, toId)
            intent.putExtra(IM_SYMBOL, symbol)
            intent.putExtra(TRANSACTION_AMOUNT, amount)
            intent.putExtra(AMOUNT_SYMBOL, amountSymbol)
            intent.putExtra(ORDER_TYPE, orderType)
            intent.putExtra(ORDER_ID, orderId)
            intent.putExtra(ORDER_TIME, orderTime)
            intent.putExtra(ISCOMPLAINUSER, isComplainUser)
            intent.putExtra(ORDER_ROLE, "buy")
            intent.putExtra(IM_TIME, time)
            intent.putExtra(IM_NICKNAME, nickName)
            intent.putExtra(COMPLAINID, uid)
            intent.putExtra(TYPE, MESSAGE_OTC)
            intent.putExtra(HEAD_URL, url)
            context.startActivity(intent)
        }
    }

    /**
     * 拍照的工具类
     */
    var imageTool: ImageTools? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otc_im)
        getData()
        initData()

        if (orderType == "5" && isComplainUser == 1) {
            tv_change_title?.visibility = View.VISIBLE
            activity_otc_im_name?.text = LanguageUtil.getString(this,"staff_service")
            tv_change_title?.text = nickName
        } else {
            tv_change_title?.visibility = View.GONE
            if (nickName.isNotEmpty()) {
                activity_otc_im_name?.text = nickName
            }
        }


        /**
         * 获取读写权限
         */
//            val rxPermissions: RxPermissions = RxPermissions(this)
//            rxPermissions.request(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    .subscribe({ granted ->
//                        if (granted) {
//                            imageTool = ImageTools(this)
//                        } else {
//                            ToastUtils.showToast(LanguageUtil.getString(this,warn_storage_permission))
//                        }
//
//                    })

        setSelectOnclick()
    }

    fun getData() {
        intent ?: return
        chatType = intent?.getIntExtra(TYPE, 0)!!
        complainId = intent?.getIntExtra(COMPLAINID, 0)!!
        orderId = intent?.getStringExtra(ORDER_ID)!!
        toId = intent?.getIntExtra(TO_ID, 0)!!
        symbol = intent?.getStringExtra(IM_SYMBOL)!!
        nickName = intent?.getStringExtra(IM_NICKNAME)!!
        amount = intent?.getStringExtra(TRANSACTION_AMOUNT)!!
        orderType = intent?.getStringExtra(ORDER_TYPE)!!
        amountSymbol = intent?.getStringExtra(AMOUNT_SYMBOL)!!
        time = intent?.getStringExtra(IM_TIME) ?: ""
        headUrl = intent?.getStringExtra(HEAD_URL) ?: ""
        orderRole = intent?.getStringExtra(ORDER_ROLE) ?: ""
        orderTime = intent.getLongExtra(ORDER_TIME, 0)
        isComplainUser = intent?.getIntExtra(ISCOMPLAINUSER, 0)!!
    }

    /**
     * 初始化聊天上面文案
     */
    fun initData() {
        activity_otc_transaction_amount?.text = LanguageUtil.getString(this,"otc_text_tradingPrice")
        activity_otc_transaction_amount_content?.text = amount + amountSymbol
        if (orderRole == "buy") {
            activity_otc_im_pay_symbol_time?.text = Utils.getOrderType(this, orderType.toInt())
        } else {
            activity_otc_im_pay_symbol_time?.text = Utils.getOrderTypeSell(this, orderType.toInt())
        }

    }

    override fun onResume() {
        super.onResume()
        if (UserDataService.getInstance().isLogined) {
            if (chatType == MESSAGE_OTC) {
                getOTCMessage()
                initWebSocket()
            } else if (chatType == DETAILS_PROBLEM) {
                getDetailsProblem()
                refreshDetailsProblem()

            }
        }

    }

    fun setSelectOnclick() {
        iv_cancel?.setOnClickListener { finish() }
        /**
         * 点击发送按钮
         */
        activity_otc_im_send_content?.setOnClickListener {
            var content = activity_otc_im_bottom_et?.text.toString()
            if (TextUtils.isEmpty(content)) {
                ToastUtils.showToast(LanguageUtil.getString(this,"common_tip_pleaseInputWord"))
                return@setOnClickListener
            }
            if (chatType == MESSAGE_OTC) {
                if (content.length > 300) {
                    ToastUtils.showToast(LanguageUtil.getString(this,"otc_more_300"))
                    return@setOnClickListener
                }
            } else if (chatType == DETAILS_PROBLEM) {
                if (content.length > 500) {
                    ToastUtils.showToast(LanguageUtil.getString(this,"otc_more_500"))
                    return@setOnClickListener
                }
            }
            if (chatType == MESSAGE_OTC) {
                subMessage4User(content)
            } else if (chatType == DETAILS_PROBLEM) {
                subMessage4Robot(complainId, content, "1")

            }
        }
        /**
         * 点击图片
         */
        activity_otc_im_send_image?.setOnClickListener {

            //TODO 这里主要得请求发送图片

//            showBottomDialog()

        }

        /**
         * 切换聊天
         */
        tv_change_title?.setOnClickListener {

            if (chatType == DETAILS_PROBLEM) {
                if (orderRole == "buy") {
                    newIntent4Buyer(this, toId, orderId, symbol, amount, orderType, amountSymbol, nickName, time, orderTime, isComplainUser, complainId, headUrl)
                } else {
                    newIntent4Seller(this, toId, orderId, symbol, amount, orderType, amountSymbol, nickName, time, orderTime, isComplainUser, complainId, headUrl)
                }
            } else {
                newIntent(this, complainId, symbol, amount, orderType, amountSymbol, time, toId, orderId, nickName, orderTime, isComplainUser, orderRole)
            }
            finish()
        }

    }

    var msgAdapter: OTCImAdapter? = null
    var detailsAdapter: OTCIMDetailsProblemBeanAdapter? = null
    var data: ArrayList<OTCIMMessageBean>? = arrayListOf()
    /**
     * 初始化 用户聊天信息
     */
    fun initView4User(data: ArrayList<OTCIMMessageBean>) {
        this.data = data
        msgAdapter = OTCImAdapter(this, data, orderType, time, headUrl)
        activity_otc_list?.adapter = msgAdapter

    }

    /**
     * 初始化 工单 聊天信息
     */
    fun initView4Details(bean: OTCIMDetailsProblemBean) {

        detailsAdapter = OTCIMDetailsProblemBeanAdapter(this, bean.rqReplyList, orderType, time)
        activity_otc_list?.adapter = detailsAdapter

    }

    /**
     * 处理聊天消息
     */
    var handler: Handler = Handler(object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            if (msg?.what == 0) {
                val data: String = msg.obj as String
                val chatMessage = JsonUtils.jsonToBean(data,
                        OTCChatBean::class.java)
                refreshChatAdapter4User(chatMessage)
            }
            return true
        }
    })

    /**
     * 刷新聊天 用户聊天
     */
    fun refreshChatAdapter4User(bean: OTCChatBean) {
        var otcimMessageBean = OTCIMMessageBean(bean.chatId.toInt(), bean.chatId.toInt(), bean.message.orderId.toLong(), bean.message.from, "", bean.message.to, "",
                bean.message.content, 0, bean.message.time)
        data?.add(otcimMessageBean)

        runOnUiThread {
            msgAdapter?.setmList(data)
        }

    }

    /**
     * 刷新聊天 客服聊天
     */
    fun refreshChatAdapter4Robot(bean: OTCIMDetailsProblemBean) {
        runOnUiThread {
            detailsAdapter?.setmList(bean.rqReplyList)
        }
    }


    fun handleData(data: String) {
        try {
            val msg = Message.obtain()
            msg.obj = data
            msg.what = 0
            handler.sendMessage(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 开启ws
     */
    fun initWebSocket() {
        Log.d("======", "==initSocket===")

        var baseId = fromId.toString() + toId.toString()
        Log.e("shenogng", "baseId:" + fromId.toString() + toId.toString())
        Log.e("shenogng", "url:" + ApiConstants.SOCKET_OTC_ADDRESS + Base64.encodeToString(Utils.String2Byte(baseId), Base64.NO_WRAP))
        mSocketClient = object : WebSocketClient(URI(ApiConstants.SOCKET_OTC_ADDRESS + Base64.encodeToString(Utils.String2Byte(baseId), Base64.NO_WRAP))) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.e("shenogng", "onOpen")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.e("shenogng", "onClose" + reason)
                try {
                    mSocketClient.reconnect()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }


            override fun onMessage(bytes: ByteBuffer?) {
                super.onMessage(bytes)
                if (bytes == null) return
                val data = GZIPUtils.uncompressToString(bytes.array())
                if (!data.isNullOrBlank()) {
                }
                Log.e("shenogng", "onMessage ByteBuffer :$bytes")
            }

            override fun onMessage(message: String?) {
                Log.e("shenogng", "onMessage:$message")
                handleData(message!!)


            }

            override fun onError(ex: Exception?) {
                Log.e("shenogng", "onError：" + ex?.printStackTrace())
            }

        }
        mSocketClient.connect()
    }

    private lateinit var mSocketClient: WebSocketClient

    var disposable: Disposable? = null

    /**
     * 1分钟刷新一次接口
     */
    fun refreshDetailsProblem() {
        Observable.interval(1, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Long> {
                    override fun onNext(aLong: Long) {
                        Log.d("====onNext=====", "=====count:===along:$aLong")
                        getDetailsProblem()
                    }

                    override fun onSubscribe(d: Disposable) {
                        disposable = d
                        Log.e(TAG, "disposable:${disposable.toString()}")
                    }


                    override fun onError(e: Throwable) {
                    }

                    override fun onComplete() {

                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null && !disposable?.isDisposed!!) {
            disposable?.dispose()
        }
    }

    /**
     * 获取聊天记录  与卖家或者买家聊天
     * 历史记录
     */
    fun getOTCMessage() {
        var fromId = UserDataService.getInstance().userInfo4UserId.toInt()
        HttpClient.instance.gethistoryMessage(fromId, orderId, toId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ArrayList<OTCIMMessageBean>>() {
                    override fun onHandleSuccess(t: ArrayList<OTCIMMessageBean>?) {
                        initView4User(t ?: return)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        ToastUtils.showToast(msg)
                    }
                })
    }


    var firstRequest = true
    /**
     * 获取 客服聊天记录
     */
    fun getDetailsProblem() {
        HttpClient.instance.getDetailsProblem(complainId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<OTCIMDetailsProblemBean>() {
                    override fun onHandleSuccess(t: OTCIMDetailsProblemBean?) {
                        if (firstRequest) {
                            firstRequest = false
                            initView4Details(t ?:return)
                        } else {
                            refreshChatAdapter4Robot(t ?:return)
                        }

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        ToastUtils.showToast(msg)
                    }

                })
    }

    var lastId = "0"
    /**
     * 发送聊天 与客户之间
     */
    fun subMessage4User(content: String) {
        if (content == null || content.isEmpty()) return
        if (!::mSocketClient.isInitialized || !mSocketClient.isOpen) {
            return
        }


        Thread(Runnable {


            var chatbean = "{\"type\":\"message\",\"chatId\":\"$lastId\",\"message\":{\"from\":\"$fromId\",\"to\":\"$toId\",\"content\":\"$content\",\"time\":\"${System.currentTimeMillis()}\",\"orderId\":\"$orderId\"}}"
            Log.e(TAG, "jsonObject : $chatbean")

            try {
                mSocketClient.send(chatbean)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
        runOnUiThread {
            activity_otc_im_bottom_et?.setText("")
        }

    }

    /**
     * 发送聊天与客服
     */
    fun subMessage4Robot(rqId: Int, rqReplyContent: String, contentType: String) {
        HttpClient.instance.getReplyCreate(rqId, rqReplyContent, contentType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        activity_otc_im_bottom_et?.setText("")
                        getDetailsProblem()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        ToastUtils.showToast(msg)
                    }

                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageTool?.onAcitvityResult(requestCode, resultCode, data
        ) { bitmap, path ->
            val bitmap2Base64 = imageTool?.bitmap2Base64(bitmap)
            uploadImg(bitmap2Base64!!, FIRST_INDEX)
        }
    }

    var firstImgPath = ""

    var dialog: TDialog? = null
    /**
     * 照片选择 或者  证件选择
     * titleText 标题
     * fristText 第一个选项
     * secondTextUtils 第二个选项
     */
    fun showBottomDialog() {
        dialog = NewDialogUtils.showBottomListDialog(this, arrayListOf(LanguageUtil.getString(this,"noun_camera_takeAlbum"), LanguageUtil.getString(this,"noun_camera_takephoto")), -1, object : NewDialogUtils.DialogOnclickListener {
            override fun clickItem(data: ArrayList<String>, item: Int) {
                when (item) {
                    0 -> {
                        if (isFinishing && isDestroyed) return
                        imageTool?.openGallery("")
                    }
                    1 -> {
                        if (isFinishing && isDestroyed) return
                        openCamera()
                    }
                }
                dialog?.dismiss()
            }

        })


    }

    /**
     * 获取相机权限
     */
    private fun openCamera() {
        val rxPermissions: RxPermissions = RxPermissions(this)
        rxPermissions.request(android.Manifest.permission.CAMERA)
                .subscribe({ granted ->
                    if (granted) {
                        imageTool?.openCamera("")
                    } else {
                        ToastUtils.showToast(LanguageUtil.getString(this,"warn_camera_permission"))
                    }

                })
    }

    /**
     * 上传照片
     */
    fun uploadImg(imageBase: String, index: Int) {
        HttpClient.instance.uploadImg(imgBase64 = imageBase)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(t: JsonObject?) {
                        if (t == null) return
                        var json = JSONObject(t.toString())
                        cancelProgressDialog()

                        val baseImgUrl = json.getString("base_image_url")
                        val fileName = json.getString("filename")
                        if (TextUtils.isEmpty(fileName)) {
                            ToastUtils.showToast(LanguageUtil.getString(this@OTCIMActivity,"otc_input_image_error"))
                            return
                        }


                        firstImgPath = baseImgUrl + fileName

                        subMessage4Robot(complainId, firstImgPath, "2")

                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        ToastUtils.showToast(LanguageUtil.getString(this@OTCIMActivity,"toast_upload_pic_failed"))
                    }
                })
    }


}