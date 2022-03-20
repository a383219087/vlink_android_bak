package com.yjkj.chainup.new_version.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.manager.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.net.retrofit.NetObserver
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import com.yjkj.chainup.util.DisplayUtil
import com.yjkj.chainup.new_version.adapter.OTCBlackListAdapter
import com.yjkj.chainup.new_version.bean.BlackListData
import com.yjkj.chainup.new_version.view.EmptyForAdapterView
import com.yjkj.chainup.util.NToastUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_black_list.*

/***
 * @date 2018-10-15
 * @author Bertking
 * @description 屏蔽的人页面
 */
class BlackListActivity : NewBaseActivity() {
    var adapter = OTCBlackListAdapter(arrayListOf())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_black_list)
        context = this
        initView()
        getRelationShip()
        v_title?.setContentTitle(LanguageUtil.getString(this,"personal_text_blacklist"))
    }

    companion object {
        fun enter2(context: Context) {
            context.startActivity(Intent(context, BlackListActivity::class.java))
        }
    }


    fun initView() {
        rv_black_list?.layoutManager = LinearLayoutManager(context)
        rv_black_list?.adapter = adapter
        adapter.setEmptyView(EmptyForAdapterView(context))

        adapter.setOnItemLongClickListener { _, _, position ->
            NewDialogUtils.showNormalDialog(context, LanguageUtil.getString(this,"common_tip_removeBlackList")
                    , object : NewDialogUtils.DialogBottomListener {
                override fun sendConfirm() {
                    removeRelationFromBlack(list[position].userId, position)
                }
            }, "", LanguageUtil.getString(this,"common_text_btnConfirm"), LanguageUtil.getString(this,"common_action_thinkAgain"))

            true
        }
    }


    /**
     *  移除黑名单
     */
    fun removeRelationFromBlack(otherUid: String, position: Int) {
        HttpClient.instance.removeRelationFromBlack(otherUid.toInt())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<Any>() {
                    override fun onHandleSuccess(t: Any?) {
                        removeName(position)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        NToastUtil.showTopToastNet(this@BlackListActivity,false,msg)
                    }
                })
    }

    var list: ArrayList<BlackListData.Relationship> = arrayListOf()

    fun removeName(position: Int) {
        list.removeAt(position)
        adapter.setList(list)
    }

    /**
     * 获取黑名单列表
     */
    private fun getRelationShip() {
        showProgressDialog()
        HttpClient.instance
                .getRelationShip()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : NetObserver<BlackListData>() {
                    override fun onHandleSuccess(t: BlackListData?) {
                        cancelProgressDialog()
                        list = t?.relationshipList ?: arrayListOf()
                        adapter.setList(list)
                    }

                    override fun onHandleError(code: Int, msg: String?) {
                        super.onHandleError(code, msg)
                        cancelProgressDialog()
                        NToastUtil.showTopToastNet(this@BlackListActivity,false,msg)
                    }
                })
    }

}
