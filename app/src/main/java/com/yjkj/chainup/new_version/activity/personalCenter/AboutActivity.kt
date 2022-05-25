package com.yjkj.chainup.new_version.activity.personalCenter

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.base.NBaseActivity
import com.yjkj.chainup.bean.AboutUSBean
import com.yjkj.chainup.db.constant.ParamConstant
import com.yjkj.chainup.db.constant.RoutePath
import com.yjkj.chainup.extra_service.arouter.ArouterUtil
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.api.ApiConstants
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.adapter.AbountAdapter
import com.yjkj.chainup.new_version.view.CommonlyUsedButton
import com.yjkj.chainup.util.CheckUpdateUtil
import com.yjkj.chainup.util.PackageInfoUtils
import com.yjkj.chainup.util.isHttpUrl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_about.*

/**
 * 关于
 */
class AboutActivity : NBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun setContentView(): Int {
        return R.layout.activity_about
    }


    override fun onInit(savedInstanceState: Bundle?) {
        super.onInit(savedInstanceState)
        initView()
        loadData()
    }


    override fun initView() {
        if (!ApiConstants.isGooglePlay()) {
            cub_submit?.visibility = View.GONE
        }
        cub_submit.isEnable(true)
        cub_submit.listener = object : CommonlyUsedButton.OnBottonListener {
            override fun bottonOnClick() {
                CheckUpdateUtil.update(mActivity, false)
            }
        }
        v_title?.setContentTitle(LanguageUtil.getString(this, "personal_text_aboutus"))
        cub_submit?.setBottomTextContent(LanguageUtil.getString(this, "personal_action_checkUpdate"))
    }

    /**
     * 获取关于我们
     */
    override fun loadData() {
        super.loadData()
        HttpClient.instance.getAboutUs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ArrayList<AboutUSBean>>() {
                    override fun onHandleSuccess(list: ArrayList<AboutUSBean>?) {
                        if (list == null) return
                        list.add(0, AboutUSBean(LanguageUtil.getString(this@AboutActivity, "common_text_versionCode"), PackageInfoUtils.packageNameOrCode(this@AboutActivity)))
                        val adapter = AbountAdapter(list)
                        val linearLayoutManager = LinearLayoutManager(this@AboutActivity)
                        rv_about.layoutManager = linearLayoutManager
                        rv_about.adapter = adapter
                        rv_about.isLayoutFrozen = true
                        adapter.setOnItemClickListener { adapter, view, position ->
                            val item: AboutUSBean?= adapter.data[position] as AboutUSBean?
                            if (item?.content!!.isHttpUrl()){
                                val bundle = Bundle()
                                bundle.putString(ParamConstant.URL_4_SERVICE, item.content)
                                ArouterUtil.greenChannel(RoutePath.UdeskWebViewActivity, bundle)
                            }


                        }
                    }
                })
    }

}
