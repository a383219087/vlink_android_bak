package com.yjkj.chainup.new_version.activity.personalCenter

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.dev.NoticeBean
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.WebTypeEnum
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.net_new.NLoadingDialog
import com.yjkj.chainup.new_version.activity.ItemDetailActivity
import com.yjkj.chainup.new_version.adapter.NoticeAdapter
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.StringUtil
import com.yjkj.chainup.util.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_notice.*

/**
 * 公告
 */
class NoticeActivity : NewBaseActivity() {

    var noticeList = arrayListOf<NoticeBean.NoticeInfo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.activity_notice)
        title_layout?.setContentTitle(LanguageUtil.getString(this,"personal_text_notice"))
        getNotices()
    }

    fun initView() {
        var noticeAdapter = NoticeAdapter(noticeList)
        rv_notice.layoutManager = LinearLayoutManager(context)
        noticeAdapter.setEmptyView(EmptyForAdapterView(this))
        rv_notice.adapter = noticeAdapter
        /**
         * 跳转至"公告详情"
         */
        noticeAdapter.setOnItemClickListener { adapter, view, position ->
            val noticeInfo = noticeAdapter.data[position]
            //ItemDetailActivity.enter2Announcement(context, noticeInfo.id.toString(), noticeInfo.title, true)

            var bundle = Bundle()
            bundle.putString(ParamConstant.web_url,noticeInfo.id.toString())
            bundle.putString(ParamConstant.head_title,noticeInfo.title)
            bundle.putInt(ParamConstant.web_type, WebTypeEnum.Notice.value)
            ArouterUtil.greenChannel("/web/ItemDetailActivity",bundle)
        }
    }


    /**
     * 获取当前委托
     */
    private fun getNotices() {
        HttpClient.instance.getNotices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<NoticeBean>() {
                    override fun onHandleSuccess(t: NoticeBean?) {
                        if(null!=t?.noticeInfoList){
                            noticeList = ArrayList(t?.noticeInfoList)
                        }
                        initView()
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.d(TAG, "======error:==" + code + ":msg+" + msg)
                        ToastUtils.showToast(msg)
                    }
                })
    }
}
