package com.yjkj.chainup.new_version.activity.personalCenter.push

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.PushBean
import com.yjkj.chainup.bean.PushItem
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.TitleShowListener
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.SystemUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_push_settings.*

/**
 * @Author yishanxin
 * @Date 2019/3/27-5:35 PM
 * @Email
 * @description 设置页面
 */
class PushSettingsActivity : NewBaseActivity() {
    var mAdapter: PushAdapter? = null

    companion object {
        fun enter2(context: Context) {
            var intent = Intent()
            intent.setClass(context, PushSettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push_settings)
        title_layout?.setContentTitle(LanguageUtil.getString(this, "customSetting_action_push"))
        initView()
        setOnClick()
        initData()
    }

    override fun onResume() {
        super.onResume()
        mAdapter?.notifyDataSetChanged()
    }


    fun initView() {
        mAdapter = PushAdapter(arrayListOf())
        var linearLayoutManager = LinearLayoutManager(this@PushSettingsActivity)
        rv_push.layoutManager = linearLayoutManager
        rv_push.adapter = mAdapter
        rv_push.isLayoutFrozen = true

    }

    fun setOnClick() {
        listener = object : TitleShowListener {
            override fun TopAndBottom(status: Boolean) {
                title_layout?.slidingShowTitle(status)
            }
        }
        mAdapter?.setOnItemChildClickListener { adapter, view, position ->
            Log.e("LogUtils", "position ${position}")
            val item = adapter.data.get(position) as PushBean
            if (item.isSystemView()) {
                if (!SystemUtils.isOpenNotifications()) SystemUtils.startNotifactions()
            } else {
                if (mAdapter?.status == "0") {

                } else {
                    initPushCheck(position)
                }
            }
        }

    }

    /**
     * 退出登录
     */
    fun initData() {
        showProgressDialog()
        HttpClient.instance.getPushSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<PushItem>() {
                    override fun onHandleSuccess(list: PushItem?) {
                        cancelProgressDialog()
                        if (list == null) return
                        mAdapter?.setList(list.list)
                        rv_push.adapter = mAdapter
                        mAdapter?.notifyDataSetChanged()
                        Log.e("LogUtils", "position ${list.list} ${mAdapter?.data?.size}")
                    }

                    override fun onHandleError(msg: String?) {
                        super.onHandleError(msg)
                        cancelProgressDialog()
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })

    }

    /**
     * 退出登录
     */
    fun initPushCheck(position: Int) {
        showProgressDialog()
        val pushBean = mAdapter?.data?.get(position)
        HttpClient.instance.savePushType(pushBean?.type!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(list: Any?) {
                        cancelProgressDialog()
                        mAdapter?.data?.get(position)?.value = !pushBean.value
                        mAdapter?.notifyDataSetChanged()
                    }


                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                    }
                })

    }

}