package com.yjkj.chainup.new_version.activity.personalCenter.contract

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yjkj.chainup.R
import com.yjkj.chainup.bean.ContractBean
import com.yjkj.chainup.db.service.PublicInfoDataService
import com.yjkj.chainup.util.LanguageUtil
import com.yjkj.chainup.net.HttpClient
import com.yjkj.chainup.new_version.activity.NewBaseActivity
import com.yjkj.chainup.new_version.activity.SplashActivity
import com.yjkj.chainup.new_version.dialog.NewDialogUtils
import kotlinx.android.synthetic.main.activity_push_settings.*

/**
 * @Author yishanxin
 * @Date 2019/3/27-5:35 PM
 * @Email
 * @description 设置页面
 */
class ContractChangeActivity : NewBaseActivity() {
    var mAdapter: ContractAdapter? = null

    companion object {
        fun enter2(context: Context) {
            var intent = Intent()
            intent.setClass(context, ContractChangeActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contract_settings)
        title_layout?.setContentTitle(LanguageUtil.getString(this, "customSetting_action_changeContract"))
        initView()
        setOnClick()
    }


    fun initView() {
        val array: ArrayList<ContractBean> = arrayListOf()
        array.add(ContractBean(LanguageUtil.getString(this, "customSetting_text_coOld"), "0"))
        array.add(ContractBean(LanguageUtil.getString(this, "customSetting_text_coNew"), "1"))
        mAdapter = ContractAdapter(array)
        mAdapter?.status = PublicInfoDataService.getInstance().contractMode.toString()
        var linearLayoutManager = LinearLayoutManager(this@ContractChangeActivity)
        rv_push.layoutManager = linearLayoutManager
        rv_push.adapter = mAdapter

    }

    fun setOnClick() {
        mAdapter?.setOnItemClickListener { adapter, view, position ->
            showLogoutDialog(position)
        }

    }

    /**
     * 退出登录的Dialog
     */
    private fun showLogoutDialog(position: Int) {
        NewDialogUtils.showNormalDialog(this, LanguageUtil.getString(this, "newContract_changeCo_desc"), object : NewDialogUtils.DialogBottomListener {
            override fun sendConfirm() {
                initPushCheck(position)
            }
        })
    }

    /**
     * 退出登录
     */
    fun initPushCheck(position: Int) {
        mAdapter?.status = position.toString()
        mAdapter?.data?.get(position)?.value = true
        mAdapter?.notifyDataSetChanged()
        HttpClient.instance.refresh()
        PublicInfoDataService.getInstance().contractMode = position
        val intent = Intent(context, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        finish()
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(0)
    }

}