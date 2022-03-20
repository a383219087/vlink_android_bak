package com.yjkj.chainup.new_version.activity.personalCenter

import android.os.Bundle
import android.util.Log
import com.chad.library.adapter.base.entity.node.BaseNode
import com.google.gson.JsonObject
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.HelpCenterBean
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.adapter.HelpCenterNewAdapter
import com.yjkj.chainup.new_version.bean.FirstNode
import com.yjkj.chainup.new_version.bean.SecondNode
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.util.StringUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_notice.*


/**
 * 帮助中心
 * @author Bertking
 * @Date 2018-7-14
 */
class HelpCenterActivity : NewBaseActivity() {

    var helpCenterList = arrayListOf<HelpCenterBean>()

    var helpCenterAdapter = HelpCenterNewAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_center)
        context = this
        initView()
        getHelpCenter()
        getWK()
    }


    override fun onResume() {
        super.onResume()
        getHelpCenter()
    }

    fun initView() {
        title_layout?.setContentTitle(LanguageUtil.getString(this, "personal_text_helpcenter"))
//        var helpCenterAdapter = HelpCenterAdapter(helpCenterList)
         helpCenterAdapter = HelpCenterNewAdapter()
        rv_notice.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
//        helpCenterAdapter.bindToRecyclerView(rv_notice)
        helpCenterAdapter.setEmptyView(EmptyForAdapterView(this))
        rv_notice.adapter = helpCenterAdapter
//        /**
//         * 跳转至"详情"
//         */
//        helpCenterAdapter.setOnItemClickListener { adapter, view, position ->
//            val helpCenterBean = helpCenterList[position]
//            //ItemDetailActivity.enter2(context, helpCenterBean.fileName, getString(R.string.personal_text_helpcenter), false)
//            var bundle = Bundle()
//            var title = helpCenterBean.title
//            if(!StringUtil.checkStr(title)){
//                title = LanguageUtil.getString(this,"personal_text_helpcenter")
//            }
//            bundle.putString(ParamConstant.head_title,title)
//            bundle.putString(ParamConstant.web_url,helpCenterBean.fileName)
//            bundle.putInt(ParamConstant.web_type, WebTypeEnum.HELP_CENTER.value)
//            ArouterUtil.greenChannel(RoutePath.ItemDetailActivity,bundle)
//        }
    }

    /**
     * 获取当前委托
     */
    private fun getHelpCenter() {
        HttpClient.instance.getHelpCenterList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<ArrayList<HelpCenterBean>>() {
                    override fun onHandleSuccess(t: ArrayList<HelpCenterBean>?) {
                        val list: MutableList<BaseNode> = ArrayList()
                        for (index in t!!.indices) {
                            val secondNodeList: MutableList<BaseNode> = ArrayList()
                            for (n in t[index].cmsArticleList!!.indices) {
                                val seNode = SecondNode(t[index].cmsArticleList!![n].title, t[index].cmsArticleList!![n].fileName)
                                secondNodeList.add(seNode)
                            }
                            val entity = FirstNode(secondNodeList, t[index].articleTypeName)
                            entity.isExpanded = index == 0
                            list.add(entity)
                        }
                        helpCenterAdapter.setList(list);
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        Log.d(TAG, "=====帮助中心：error====$code:$msg===")
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }

    private fun getWK() {
        HttpClient.instance.getCommonKV()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<JsonObject>() {
                    override fun onHandleSuccess(json: JsonObject) {
                        if (json.has("h5_url")) {
                            var partUrl = json.get("h5_url").asString
                        }
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        DisplayUtil.showSnackBar(window?.decorView, msg, isSuc = false)
                    }
                })
    }

}
