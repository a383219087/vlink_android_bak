package com.yjkj.chainup.extra_service.push

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Log
import com.google.gson.annotations.SerializedName
import com.igexin.sdk.GTIntentService
import com.igexin.sdk.message.GTCmdMessage
import com.igexin.sdk.message.GTNotificationMessage
import com.igexin.sdk.message.GTTransmitMessage
import com.yjkj.chainup.R
import com.yjkj.chainup.app.ChainUpApp
import com.yjkj.chainup.contract.activity.SlContractKlineActivity
import com.yjkj.chainup.extra_service.eventbus.EventBusUtil
import com.yjkj.chainup.extra_service.eventbus.MessageEvent
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.JSONUtil
import io.karn.notify.Notify
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.Serializable


/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br></br>
 * onReceiveMessageData 处理透传消息<br></br>
 * onReceiveClientId 接收 cid <br></br>
 * onReceiveOnlineState cid 离线上线通知 <br></br>
 * onReceiveCommandResult 各种事件处理回执 <br></br>
 */
class HandlePushIntentService : GTIntentService() {

    override fun onReceiveServicePid(context: Context, pid: Int) {
        Log.e(TAG, "HandlePush_onReceiveServicePid -> pid = $pid")

    }

    //应用收到透传数据.
    override fun onReceiveMessageData(context: Context, msg: GTTransmitMessage) {
        val data = String(msg.payload)
        Log.d(TAG, "HandlePush_onReceiveMessageData>>> messageId:" + msg.messageId + ", taskId:" + msg.taskId + ", payloadId:" + msg.payloadId + ", payloadData:" + data)
        try {
            val payload = JSONUtil.objectFromJson(data, PushPayloadData::class.java)
            Log.d(TAG, "HandlePush_onReceiveMessageData>>> pushUrl:" + payload.parseRouteUrl())

        } catch (exce: Exception) {
            exce.printStackTrace()
        }
    }

    @SuppressLint("CheckResult")
    override fun onReceiveClientId(context: Context, clientid: String) {
        Log.e(TAG, "HandlePush_onReceiveClientId -> clientid = $clientid")
        HttpClient.instance.bindToken(clientid).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

            }, {
                it.printStackTrace()
            })
    }

    override fun onReceiveOnlineState(context: Context, online: Boolean) {
        Log.e(TAG, "HandlePush_onReceiveOnlineState -> online = $online")
    }

    override fun onReceiveCommandResult(context: Context, cmdMessage: GTCmdMessage) {
        Log.d(TAG, "HandlePush_onReceiveCommandResult" + cmdMessage.action)
    }

    //收到push消息.
    override fun onNotificationMessageArrived(context: Context, msg: GTNotificationMessage) {
        Log.d(TAG, "HandlePush_onNotificationMessageArrived " + msg.messageId + ", " + msg.taskId + ", " + msg.title + ", " + msg.content)
//        ChainApp.app.component.settingManager().updateRedDot(KEY_1_MINE, KEY_2_MESSAGE, num = PushMsgQueue.msgCount()+1)
    }

    //在通知栏上点击消息
    override fun onNotificationMessageClicked(context: Context, msg: GTNotificationMessage) {
        Log.d(TAG, "HandlePush_onNotificationMessageClicked" + msg.messageId + ", " + msg.taskId + ", " + msg.title + ", " + msg.content)
    }
}

data class PushPayloadData(@SerializedName("title") var title: String,
                           @SerializedName("message") var message: String? = "",
                           @SerializedName("url") var url: String? = "",
                           @SerializedName("native") var contract_address: String? = "1") : Serializable {
    fun parseRouteUrl(): String? {
        return url
    }
}



val ACTION_BACK_UP = "back_up"
val ACTION_TRANSACTION = "transaction"
val ACTION_MESSAGE_CENTER = "message_center"

class RouteApp private constructor() {
    companion object {
        @Volatile
        var instances: RouteApp? = null
        val ROUTE_HOME: String = "home"

        val ROUTE_MARKET: String = "market"
        val ROUTE_SLCONTRACT: String = "slContract/"
        val ROUTE_SLCONTRACT_DETAIL: String = ROUTE_SLCONTRACT + "/detail"
        fun getInstance(): RouteApp {
            if (instances == null) {
                synchronized(RouteApp::class) {
                    if (instances == null) {
                        instances = RouteApp()
                    }
                }
            }
            return instances!!
        }
    }

    fun execApp(routeUrl: String, activity: Activity) {
        if (routeUrl.isEmpty()) {
            return
        }
        val url = Uri.parse(routeUrl)
        val path = url.path
        if (path.isNullOrEmpty()) {
            return
        }
        if (url.authority == ROUTE_HOME) {
            val newPath = path.substring(path.indexOf("/") + 1, path.length)
            when (newPath) {
                ROUTE_MARKET -> {
                    Log.e("LogUtils", "跳转 行情币对ETF")
                    val name = url.getQueryParameter("name") ?: ""
                    if (name.isNotEmpty()) {
                        Handler().postDelayed({
                            var messageEvent = MessageEvent(MessageEvent.market_switch_type)
                            messageEvent.msg_content = name
                            EventBusUtil.post(messageEvent)
                        }, 1000)
                    }
                }
                ROUTE_SLCONTRACT_DETAIL -> {
                    Log.e("LogUtils", "跳转 合约K线")
                    try {
                        val contractId = url.getQueryParameter("contractId") ?: ""
                        if (contractId.isNotEmpty()) {
                            SlContractKlineActivity.show(activity, contractId.toInt())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}

