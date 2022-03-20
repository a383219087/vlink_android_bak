package com.yjkj.chainup.new_version.activity.personalCenter

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.dev.MessageBean
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.new_version.view.PersonalCenterView
import com.yjkj.chainup.new_version.view.ScreeningPopupWindowView
import com.yjkj.chainup.new_version.adapter.MailMsgAdapter
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.zyyoona7.popup.EasyPopup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_mail.*


/**
 * 站内信
 */
class MailActivity : NewBaseActivity() {
    var msgTypes = ArrayList<MessageBean.Type>(6)
    lateinit var easyPopup: EasyPopup

    var msgList = ArrayList<MessageBean.UserMessage>(10)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail)
        v_title?.setContentTitle(LanguageUtil.getString(this, "personal_text_message"))
        initOnClickListener()
        getMessages(0)
        updataMessage()
    }

    private fun initView() {
        var mailMsgAdapter = MailMsgAdapter(msgList, msgTypes)
        rv_mail.adapter = mailMsgAdapter
        rv_mail.layoutManager = LinearLayoutManager(this)
        mailMsgAdapter.setEmptyView(EmptyForAdapterView(this))

        /**
         * TODO 长按删除消息
         */
        mailMsgAdapter.setOnItemLongClickListener { adapter, view, position ->
            //            ToastUtils.showToast("" + position)
            false
        }
    }

    var isfrist = true

    private fun initOnClickListener() {
        v_title.listener = object : PersonalCenterView.MyProfileListener {
            override fun onRealNameCertificat() {

            }

            override fun onclickName() {

            }

            override fun onclickRightIcon() {
                if (spw_layout.visibility == View.VISIBLE) {
                    spw_layout.visibility = View.GONE
                } else {
                    spw_layout.visibility = View.VISIBLE
                    if (isfrist) {
                        isfrist = false
                        spw_layout.setMage()
                    }

                }
            }

            override fun onclickHead() {

            }

        }
    }

    var mailType = arrayListOf<String>()

    fun initMailView(beans: ArrayList<MessageBean.Type>) {
        mailType.clear()
        beans.forEach {
            mailType.add(it.title)
        }
        spw_layout.initMailScreening(mailType)
        spw_layout.mailScreeningListener = object : ScreeningPopupWindowView.MailOrderListener {
            override fun returnMailOrderType(position: String) {
                var position = mailType.indexOf(position)
                if (position == -1) {
                    return
                }
                getMessages(msgTypes.get(position).tid)
            }

        }
    }

    /**
     * 获取当前委托
     */
    private fun getMessages(type: Int) {
        HttpClient.instance.getMessages(type = type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<MessageBean>() {
                    override fun onHandleSuccess(t: MessageBean?) {
                        msgList.clear()
                        if (null != t?.userMessageList) {
                            msgList.addAll(ArrayList(t?.userMessageList))
                        }
                        msgTypes.clear()
                        if (null != t?.typeList) {
                            msgTypes.addAll(ArrayList(t?.typeList))

                        }
                        initMailView(t?.typeList as ArrayList<MessageBean.Type>)
                        initView()
//                        var curEntrustAdapter = CurAdapter(t?.orderList)
//                        rv_cur_entrust.layoutManager = LinearLayoutManager(context)
//                        rv_cur_entrust.adapter = curEntrustAdapter
//                        curEntrustAdapter.bindToRecyclerView(rv_cur_entrust)
//                        curEntrustAdapter.setEmptyView(R.layout.ly_empty_withdraw_address)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)

                        Log.d(TAG, "======error:==" + code + ":msg+" + msg)
                    }
                })
    }

    fun updataMessage() {
        HttpClient.instance
                .updateMessageStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {

                    }

                })
    }

}
